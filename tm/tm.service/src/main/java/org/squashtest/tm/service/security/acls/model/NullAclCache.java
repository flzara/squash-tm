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
package org.squashtest.tm.service.security.acls.model;

import java.io.Serializable;

import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;

public class NullAclCache implements AclCache {

	@Override
	public void evictFromCache(Serializable pk) {
		// NOOP
	}

	@Override
	public void evictFromCache(ObjectIdentity objectIdentity) {
		// NOOP
	}

	@Override
	public MutableAcl getFromCache(ObjectIdentity objectIdentity) {
		return null;
	}

	@Override
	public MutableAcl getFromCache(Serializable pk) {
		return null;
	}

	@Override
	public void putInCache(MutableAcl acl) {
		// NOOP
	}

	@Override
	public void clearCache() {
		// NOOP
	}

}
