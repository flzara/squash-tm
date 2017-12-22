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

import java.util.List;

import org.springframework.security.acls.model.ObjectIdentity;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.security.acls.PermissionGroup;

public interface ObjectAclService {
	


	void addNewResponsibility(long partyId, ObjectIdentity entityRef, String qualifiedName);
	
	/**
	 * Removes ALL RESPONSIBILITIES from the given object.
	 * 
	 * @param entityRef
	 */
	void removeAllResponsibilities(ObjectIdentity entityRef);
	
	/**
	 * Remove all responsibilities for the Party of the given Id
	 * @param partyId
	 */
	void removeAllResponsibilities(long partyId);
	

	/**
	 * Returns all permission groups for a namespace. A namespace is the start of a group's qualified name.
	 * 
	 * @param namespace
	 * @return
	 */
	void removeAllResponsibilities(long partyId, ObjectIdentity entityRef);
	

	List<PermissionGroup> findAllPermissionGroupsByNamespace(String namespace);

	List<Object[]> retrieveClassAclGroupFromPartyId(long partyId, String qualifiedClassName);

	List<Object[]> retrieveClassAclGroupFromPartyId(long partyId, List<String> qualifiedClassNames);
	
	List<Object[]> retrieveClassAclGroupFromUserLogin(String userLogin, String qualifiedClassName);

	List<Object[]> retrieveClassAclGroupFromUserLogin(String userLogin, List<String> qualifiedClassNames);
	
	List<Object[]> retrieveClassAclGroupFromPartyId(long partyId, String qualifiedClassName,Sorting sorting, Filtering filtering);

	List<Object[]> retrieveClassAclGroupFromPartyId(long partyId, List<String> qualifiedClassNames,Sorting sorting, Filtering filtering);
	
	List<Long> findObjectWithoutPermissionByPartyId(long partyId, String qualifiedClass);

	List<Long> findObjectWithoutPermissionByPartyId(long partyId, List<String> qualifiedClasses);
	

	List<String> findUsersWithWritePermission(List<ObjectIdentity> entityRefs);
	/**
	 * Will find squash User ids and theirs permission names for the given acl_object_identity.identity and acl_object_identity.className
	 * @param objectId : the acl_object_identity.identity
	 * @param objectClassName : the acl_object_identity.acl_class.className
	 * @return a list of Object[] containing at index 0 the user id and , at index 1, the user's acl_group.qualified_name for the given acl_object.
	 */
	List<Object[]> retrievePartyAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass);
	
	/**
	 * Same as {@link #retriveUserAndAclGroupNameFromIdentityAndClass(long, Class)}, sorted and filtered version
	 * @param objectId
	 * @param qualifiedClassName
	 * @return
	 */
	List<Object[]> retrievePartyAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass, Sorting sorting, Filtering filtering);

	List<Long> findPartiesWithoutPermissionByObject(long objectId, String qualifiedClassName);

	List<Long> findPartiesWithoutPermissionByObject(long objectId, List<String> qualifiedClassNames);
	
	
	List<String> findUsersWithExecutePermission(List<ObjectIdentity> entityRefs);
	
	
	void updateDerivedPermissions(long partyId);
	
	/**
	 * basically ensures that recent modification affecting the ACL will be taken into account immediately.
	 */
	void refreshAcls();

	List<Object[]> retrieveUsersFromIdentityAndClass(long entityId);
}
