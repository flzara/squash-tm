/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.domain.search;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.*;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.search.bridge.StringBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.Identified;

// TODO: extract the logic regarding finding and eventually managing a session(roughly the same than SessionFieldBridge)
// here, we try to extract it from a hibernate object, but it wont work on non-proxied hibernate entities
// the SessionFieldBridge is injected with the EntityManager via aspects and often fails to retrieve the current session, which is not much better
public class CollectionSizeBridge implements StringBridge {

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionSizeBridge.class);

	private static final int EXPECTED_LENGTH = 7;

	private static final Pattern PATTERN = Pattern.compile(	"^([\\w\\.]+)\\.(\\w+)$");

	private static final String ENTITY_ALIAS = "entity";
	private static final String COL_ALIAS = "coll";

	private String entityClass = null;
	private String collectionPath = null;

	private String padRawValue(Integer rawValue){
		return StringUtils.leftPad(Integer.toString(rawValue), EXPECTED_LENGTH, '0');
	}

	@Override
	public String objectToString(Object value) {

		Collection<?> collection = (Collection<?>) value;

		// if Hibernate : special treatment
		if (isHibernate(collection)){
			return handleHibernateCollection(collection);
		}
		// else, standard treatment
		else{
			return padRawValue(collection.size());
		}

	}


	/*

          MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
          MMMMMMMMMMMMds+:--------:+sdNMMMMMMMMMMM
          MMMMMMMMms:-+sdNMMMMMMMMNdy+--omMMMMMMMM
          MMMMMMh:` /mMMMMMMMMMMMMMMMMm+ `-yMMMMMM
          MMMMd--hN``--sNMMMMMMMMMMNy:..`md:.hMMMM
          MMM+`yMMMy hd+./hMMMMMMh/.+dd sMMMh`/MMM
          MM:.mMMMMM:.NMMh/.+dd+./hMMM--MMMMMm--NM
          M+`mMMMMMMN`+MMMMm-  .dMMMMo mMMMMMMN.:M
          d yMMMMMMMMy dNy:.omNs--sNm oMMMMMMMMh h
          /`MMMMMMMMMM.`.+dMMMMMMm+.``NMMMMMMMMM-:
          .:MMMMMMMd+./`oMMMMMMMMMMs /.+dMMMMMMM/`
          .:MMMMmo.:yNMs dMMMMMMMMm`oMNy:.omMMMM/`
          /`MNy:.omMMMMM--MMMMMMMM:.MMMMMNs--sNM.:
          d -` :++++++++: /++++++/ :++++++++:  : h
          M+ yddddddddddd+ yddddy /dddddddddddy`/M
          MM/.mMMMMMMMMMMM.-MMMM/.NMMMMMMMMMMm.:NM
          MMMo`sMMMMMMMMMMd sMMy hMMMMMMMMMMy`+MMM
          MMMMd--hMMMMMMMMM+`mN`/MMMMMMMMMh--hMMMM
          MMMMMMh:.omMMMMMMN.:/`NMMMMMMms.:hMMMMMM
          MMMMMMMMNs:./shmMMh  yMMNds/.:smMMMMMMMM
          MMMMMMMMMMMMdy+/---``---:+sdMMMMMMMMMMMM
          MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM


		Welcome to the Black Magic workshop. When the collection comes from hibernate, we cannot afford to just .size()
		it : it would trigger its initialization which can be very expensive (Hibernate loves left out joins). So we
		need to do it another way.

		* Method 1 : check whether the collection is initialized. If it is, just return the size.

		* Method 2 : use the session of that collection then go criteria

		* Method 3 : create a new session and go Criteria to find the value.

     */


	private String handleHibernateCollection(Collection<?> collection){

		Integer count = null;

		AbstractPersistentCollection hibCollection = (AbstractPersistentCollection)collection;

		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Indexing a Hibernate persistent collection, role is : " +hibCollection.getRole());
		}

		// method 1
		if (Hibernate.isInitialized(collection)){
			LOGGER.debug("the collection was initialized already, returning the size is fine");
			count = collection.size();
		}


		// method 2
		else if (hasLiveSession(hibCollection)){

			LOGGER.debug("the session is live and reusable, attempting to query the size from it.");

			/*
				Many things can fail here. For instance we tested above that the session for the collection is still ready for use, but if it
				belongs to another thread it may well have been closed in the time between. So here we try method 1, if it fails we use the fallback.
			 */
			try {

				count = countUsingCriteria(collection);

				LOGGER.debug("collection size was found using criteria");
			}
			catch(Exception ex){

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("something has gone wrong : ",ex);
				}

				LOGGER.debug("attempting the fallback method");
				// method 3
				count = fallbackHibernate(collection);
			}
		}

		// method 3
		else{
			LOGGER.debug("No live session usable. Attempting the fallback method");
			count = fallbackHibernate(collection);
		}

		return padRawValue(count);

	}

	/*
		The fallback implies to create a new session and execute a query with it in order to retrieve the collection size.
		Creating a new session requires to first get a hold to the session factory, which can be accessed through the (now
		dead) session. Creating the query implies to ask the persistent collection for required metadata such as the owning
		instance and its role.
	 */
	private Integer fallbackHibernate(Collection<?> collection){

		Session session = null;
		Transaction tx = null;

		AbstractPersistentCollection hibCollection = (AbstractPersistentCollection)collection;

		try{

			session = createNewSession(hibCollection);

			// TODO : we know there are no JTA involved here but in case this change in the future, have a look at AbstractPersistentCollection#withTemporarySessionIfNeeded

			tx = session.beginTransaction();

			Criteria criteria = createCriteria(hibCollection, session);

			Long count = (Long)criteria.uniqueResult();

			LOGGER.debug("found the size using the fallback method");

			return count.intValue();

		}
		catch(Exception ex){

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("the fallback has gone wrong too", ex);
			}

			// not supposed to work because in this branch we already know that the underlying session is dead
			// but hey, it worked anyway in previous versions so why not.
			LOGGER.debug("invoking collection.size() as last chance");
			return collection.size();
		}
		finally{
			if (tx != null){
				tx.commit();
			}
			if (session != null && session.isOpen()){
				session.close();
			}
		}

	}


	// ******************* predicates *******************


	private boolean isHibernate(Collection<?> collection){
		return AbstractPersistentCollection.class.isAssignableFrom(collection.getClass());
	}

	private boolean hasLiveSession(AbstractPersistentCollection hibCollection){
		boolean hasSession = false;
		boolean isLive = false;

		SessionImplementor implementor = hibCollection.getSession();
		if (implementor != null && Session.class.isAssignableFrom(implementor.getClass())){
			hasSession = true;
			Session s = (Session)implementor;
			isLive = s.isOpen() && s.isConnected();
		}

		return hasSession && isLive;
	}

	private Session getLiveSession(AbstractPersistentCollection hibCollection){
		// let run potential NPE
		return (Session)hibCollection.getSession();
	}


	private Integer countUsingCriteria(Collection<?> collection) {
		AbstractPersistentCollection hibCollection = (AbstractPersistentCollection) collection;

		// we require the session that created the collection
		Session session = getLiveSession(hibCollection);

		Criteria criteria = createCriteria(hibCollection, session);
		Long res = (Long)criteria.uniqueResult();
		return res.intValue();
	}



	private Criteria createCriteria(AbstractPersistentCollection hibCollection, Session session){
		if (entityClass == null){
			initCriteriaData(hibCollection);
		}

		Identified entity = (Identified)hibCollection.getOwner();

		return session.createCriteria(entityClass, ENTITY_ALIAS)
				   .setReadOnly(true)
				   .createAlias(collectionPath, COL_ALIAS)
				   .add(Restrictions.eq("id", entity.getId()))
				   .setProjection(Projections.rowCount());

	}

	private void initCriteriaData(AbstractPersistentCollection hibCollection){
		String role = hibCollection.getRole();
		Matcher matcher = PATTERN.matcher(role);
		if (matcher.matches()){
			entityClass = matcher.group(1);
			collectionPath = ENTITY_ALIAS+"."+matcher.group(2);
		}
		else{
			throw new RuntimeException("cannot extract entity and collection name from role : "+role);
		}

	}



	// ******************** fallback code *******************************

	private Session createNewSession(AbstractPersistentCollection hibCollection){
		// let run potential NPE
		SessionImplementor deadSession = hibCollection.getSession();
		SessionFactoryImplementor sf = deadSession.getFactory();

		Session newSession = sf.openSession();
		newSession.setDefaultReadOnly(true);
		newSession.setFlushMode(FlushMode.MANUAL);

		return newSession;

	}


}

