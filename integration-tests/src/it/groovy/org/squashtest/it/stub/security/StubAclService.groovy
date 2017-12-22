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

import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.ObjectIdentity
import org.squashtest.tm.core.foundation.collection.Filtering
import org.squashtest.tm.core.foundation.collection.Sorting
import org.squashtest.tm.security.acls.PermissionGroup
import org.squashtest.tm.service.security.acls.jdbc.ManageableAclService;
import org.squashtest.tm.service.security.acls.model.ObjectAclService


class StubAclService implements ObjectAclService, ManageableAclService {

	@Override
	public List<PermissionGroup> findAllPermissionGroupsByNamespace(
			String namespace) {

		return Collections.emptyList();
	}

	@Override
	public void removeAllResponsibilities(long partyId, ObjectIdentity entityRef) {
	}

	@Override
	public void removeAllResponsibilities(ObjectIdentity entityRef) {
	}

	@Override
	public List<Object[]> retrieveClassAclGroupFromPartyId(long partyId,
			String qualifiedClassName) {

		return Collections.emptyList();
	}

	@Override
	public List<Long> findObjectWithoutPermissionByPartyId(long partyId,
			String qualifiedClass) {

		return Collections.emptyList();
	}

	@Override
	public void addNewResponsibility(long partyId, ObjectIdentity entityRef,
			String qualifiedName) {
	}

	@Override
	public List<String> findUsersWithWritePermission(
			List<ObjectIdentity> entityRefs) {

		return Collections.emptyList();
	}

	@Override
	public List<Object[]> retrievePartyAndAclGroupNameFromIdentityAndClass(
			long entityId, Class<?> entityClass) {

		return Collections.emptyList();
	}

	@Override
	public List<Object[]> retrievePartyAndAclGroupNameFromIdentityAndClass(
			long entityId, Class<?> entityClass, Sorting sorting,
			Filtering filtering) {

		return Collections.emptyList();
	}

	@Override
	public List<Long> findPartiesWithoutPermissionByObject(long objectId,
			String qualifiedClassName) {

		return Collections.emptyList();
	}

	@Override
	public List<String> findUsersWithExecutePermission(
			List<ObjectIdentity> entityRefs) {

		return Collections.emptyList();
	}


	@Override
	public List<Object[]> retrieveClassAclGroupFromUserLogin(String userLogin,
			String qualifiedClassName) {

		return Collections.emptyList();
	}

	@Override
	public List<Object[]> retrieveClassAclGroupFromPartyId(long partyId,
			String qualifiedClassName, Sorting sorting, Filtering filtering) {

		return Collections.emptyList();
	}

	@Override
	public List<Object[]> retrieveClassAclGroupFromPartyId(long partyId,
			List<String> qualifiedClassNames) {

		return Collections.emptyList();
	}

	@Override
	public List<Object[]> retrieveClassAclGroupFromUserLogin(String userLogin,
			List<String> qualifiedClassNames) {

		return Collections.emptyList();
	}

	@Override
	public List<Object[]> retrieveClassAclGroupFromPartyId(long partyId,
			List<String> qualifiedClassNames, Sorting sorting,
			Filtering filtering) {

		return Collections.emptyList();
	}

	@Override
	public List<Long> findObjectWithoutPermissionByPartyId(long partyId,
			List<String> qualifiedClasses) {

		return Collections.emptyList();
	}

	@Override
	public List<Long> findPartiesWithoutPermissionByObject(long objectId,
			List<String> qualifiedClassNames) {

		return Collections.emptyList();
	}

	@Override
	public void refreshAcls() {
	}

	@Override
	public void removeAllResponsibilities(long partyId) {
	}

	@Override
	public void updateDerivedPermissions(long partyId) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Object[]> retrieveUsersFromIdentityAndClass(long entityId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void createObjectIdentity(ObjectIdentity objectIdentity) throws AlreadyExistsException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeObjectIdentity(ObjectIdentity objectIdentity) {
		// TODO Auto-generated method stub
		
	}
}
