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
package org.squashtest.tm.core.dynamicmanager.factory;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.squashtest.tm.core.dynamicmanager.internal.handler.ArbitraryQueryHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.DynamicComponentInvocationHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.EntityFinderNamedQueryHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.EntityModifierHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.FindAllByIdsHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.FindByIdHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.ListOfEntitiesFinderNamedQueryHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.PersistEntityHandler;


/**
 * This class is a Spring bean factory for DynamicManager instances. It creates "Manager" services which are able to
 * dynamically handle simple modifications of an entity's properties.
 *
 * The dynamic manager is created using an interface. This interface can contain methods which signature are :
 *
 * <dl>
 * <dt>
 * <code>void changeSomething(long entityId, SOMETHING newSomething)</code></dt>
 * <dd>Will fetch the entity of id <code>entityId</code> and set its <code>something</code> property to
 * <code>newSomething</code> using the entity's public <code>setSomething</code> method</dd>
 * </dl>
 *
 * One can override or add custom methods to a dynamic manager. The dynamic manager needs to be defined this way :
 * <code>
 * <p>
 * public interface MyManager extends MyCustomManager {<br/>
 * 	void changeFoo(long id, String value);<br/>
 * }
 * </p>
 * <p>
 * public interface MyCustomManager {<br/>
 * 	void changeBar(long id, String value);<br/>
 * 	String doSomething(String value);<br/>
 * }
 * </p>
 * <p>
 * @Service("MyCustomManager")
 * public class MyCustomManagerImpl implements MyCustomManager {<br/>
 * 	void changeBar(long id, String value) { // overriding implementation of change method }
 * 	String doSomething(String value) { // custom method }
 * }
 * </p>
 * </code>
 *
 * <strong>Transaction demarcation and security constraints</strong>
 *
 * When needed, transaction demarcation has to be declared in the dynamic manager interface using @Transactional annotations.
 * Security constraints such as @PostAuthorize also have to be declared in the interface.
 * When using method parameters in security constraints, they must follow the "positional" naming convention : #arg0, #arg1, #arg2...
 * If one uses the actual parameter name (e.g. #id), Spring will not be able to resolve it.
 *
 * @author Gregory Fouquet
 *
 * @param <MANAGER>
 *            type of the dynamic manager service to be created.
 * @param <ENTITY>
 *            type of the entity which will be modified by the manager.
 */
public class DynamicManagerFactoryBean<MANAGER, ENTITY> extends AbstractDynamicComponentFactoryBean<MANAGER> {
	private static final int HANDLERS_COUNT = 7;

	/**
	 * Type of entities which are manipulated by the Dynamic manager. Should be initialized.
	 */
	private Class<ENTITY> entityType;

	/**
	 * @param entityType the entityType to set
	 */
	public void setEntityType(Class<ENTITY> entityType) {
		this.entityType = entityType;
	}

	@Override
	protected List<DynamicComponentInvocationHandler> createInvocationHandlers() {
		List<DynamicComponentInvocationHandler> handlers = new ArrayList<>(HANDLERS_COUNT);

		handlers.add(new PersistEntityHandler<>(entityType, entityManager));
		handlers.add(new EntityModifierHandler<>(entityManager, entityType));
		handlers.add(new FindAllByIdsHandler<>(entityType, entityManager));
		handlers.add(new FindByIdHandler(entityManager));
		handlers.add(new EntityFinderNamedQueryHandler<>(entityType, entityManager));
		handlers.add(new ListOfEntitiesFinderNamedQueryHandler<>(entityType, entityManager));
		handlers.add(new ArbitraryQueryHandler<>(entityType, entityManager));

		return handlers;
	}

	public void setEntityManager(EntityManager entityManager){
		this.entityManager = entityManager;
	}

}
