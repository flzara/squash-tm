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
package org.squashtest.it.stub.security

import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.model.ObjectIdentityGenerator
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy


/**
 * That stub class is actually a working subclass of ObjectIdentityRetrievalStrategyImpl. It was redefined as a Stub
 * to make it clear that we just need a stub. If a working instance is required, please consider a test configuration context
 * that includes EnabledAclSpecConfig instead of DisabledAclSpecConfig.
 */
class StubObjectIdentityRetrievalGenerator implements ObjectIdentityGenerator, ObjectIdentityRetrievalStrategy{

	@Override
	ObjectIdentity createObjectIdentity(Serializable id, String type) {
		throw new RuntimeException("illegal invocation of StubObjectIdentityRetrievalGenerator#createObjectIdentity. " +
			"The context configuration for that test stated that no ACL checking was required (it uses DisabledAclSpecConfig). " +
			"If that need to change, please review carefully the context configuration, include EnabledAclSpecConfig, and add the " +
			"required ACL in your dbunit datasets.")
	}

	@Override
	ObjectIdentity getObjectIdentity(Object domainObject) {
		throw new RuntimeException("illegal invocation of StubObjectIdentityRetrievalGenerator#getObjectIdentity. " +
			"The context configuration for that test stated that no ACL checking was required (it uses DisabledAclSpecConfig). " +
			"If that need to change, please review carefully the context configuration, include EnabledAclSpecConfig, and add the " +
			"required ACL in your dbunit datasets.")
	}
}
