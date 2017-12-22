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
package org.squashtest.tm.service.internal.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.squashtest.tm.service.security.acls.ExtraPermissionEvaluator;


/**
 * <p>That class is itself a {@link PermissionEvaluator} and can register extra evaluators to check against. Those extra evaluators must be available as OSGI services.
 * Whenever requested this class will in turn test each extra evaluators, then itself, as long as the answer is false. If at least one of the evaluator grants the access,
 * granted the access will be (hence the prefix AffirmativeBased).</p>
 *
 *
 * @author bsiri
 *
 */
public class AffirmativeBasedCompositePermissionEvaluator extends AclPermissionEvaluator implements PermissionEvaluator{

	private static final Logger LOGGER = LoggerFactory.getLogger(AffirmativeBasedCompositePermissionEvaluator.class);

	// Note : I choose not to synchronize this collection because chances of things going haywire
	// are very slim, and could happen only at boot time.
	// NOTE while nosgi, ExtraPermissionEvaluator not implemented anywhere, could be removable over-engineered stuff
	private Collection<ExtraPermissionEvaluator> evaluators = Collections.emptyList();

	public AffirmativeBasedCompositePermissionEvaluator(AclService aclService, Collection<ExtraPermissionEvaluator> delegates) {
		super(aclService);

		if (!CollectionUtils.isEmpty(delegates)) {
			evaluators = delegates;

			for (ExtraPermissionEvaluator evaluator : delegates) {
					LOGGER.info("Registering permission evaluator of class {}", evaluator.getClass());
			}
		}
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission){

		boolean granted = false;

		if (! evaluators.isEmpty()){
			Iterator<ExtraPermissionEvaluator> evalIter = evaluators.iterator();

			while(evalIter.hasNext() && ! granted){
				ExtraPermissionEvaluator evaluator = evalIter.next();
				granted = evaluator.hasPermission(authentication, targetDomainObject, permission);
			}
		}

		return granted || super.hasPermission(authentication, targetDomainObject, permission);

	}



	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission){

		boolean granted = false;

		if (! evaluators.isEmpty()){
			Iterator<ExtraPermissionEvaluator> evalIter = evaluators.iterator();

			while(evalIter.hasNext() && ! granted){
				ExtraPermissionEvaluator evaluator = evalIter.next();
				granted = evaluator.hasPermission(authentication, targetId, targetType, permission);
			}
		}

		return granted || super.hasPermission(authentication, targetId, targetType, permission);

	}


}
