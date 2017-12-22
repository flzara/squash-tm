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
package org.squashtest.tm.service.security.acls.jdbc;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.UnloadedSidException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.security.acls.CustomPermission;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;

/**
 * Specialization of {@link JdbcAclService} with management methods. Rem : as we tweaked Spring's ACL database model,
 * Spring's JdbcMutableAclService does not fit.
 *
 * This class is inspired by Spring Security's JdbcMutableAclService, Copyright 2004, 2005, 2006 Acegi Technology Pty
 * Limited
 *
 * @author Gregory Fouquet
 *
 */

/**
 *
 * When one update the Acl of an object (ie the permissions of a user), one want to refresh the aclCache if there is
 * one. The right way to do this would have been to delegate such task to the LookupStrategy when it's relevant to do
 * so. However we cannot subclass BasicLookupStrategy because it's final and duplicating its code for a class of ours
 * would be illegal.
 *
 * So we're bypassing the cache encapsulation and expose it right here.
 *
 *
 * @author bsiri
 */
@Service("squashtest.core.security.AclService")
@Transactional
public class JdbcManageableAclService extends JdbcAclService implements ManageableAclService, ObjectAclService {

	private static final String WITH_ARGS = "' with args [";

	private static final String WILL_ATTEMPT_TO_PERFORM = "Will attempt to perform '";

	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcManageableAclService.class);

	private final AclCache aclCache;

	private final DerivedPermissionsManager derivedManager;


	private final RowMapper<PermissionGroup> permissionGroupMapper = new RowMapper<PermissionGroup>() {
		@Override
		public PermissionGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new PermissionGroup(rs.getLong(1), rs.getString(2));
		}
	};

	private final RowMapper<Object[]> aclgroupMapper = new RowMapper<Object[]>() {
		@Override
		public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
			Object objTab[] = new Object[2];
			objTab[0] = rs.getLong(1);
			objTab[1] = new PermissionGroup(rs.getLong(2), rs.getString(3));
			return objTab;
		}
	};

	private static final String INSERT_OBJECT_IDENTITY = "insert into ACL_OBJECT_IDENTITY (IDENTITY, CLASS_ID) values (?, ?)";
	private static final String SELECT_OBJECT_IDENTITY_PRIMARY_KEY = "select oid.ID from ACL_OBJECT_IDENTITY oid inner join ACL_CLASS c on c.ID = oid.CLASS_ID where c.CLASSNAME = ? and oid.IDENTITY = ?";
	private static final String SELECT_CLASS_PRIMARY_KEY = "select ID from ACL_CLASS where CLASSNAME = ?";
	private static final String FIND_ALL_ACL_GROUPS_BY_NAMESPACE = "select ID, QUALIFIED_NAME from ACL_GROUP where QUALIFIED_NAME like ?";

	//IGNOREVIOLATIONS:START
	private static final String INSERT_PARTY_ACL_RESPONSABILITY_SCOPE = "insert into ACL_RESPONSIBILITY_SCOPE_ENTRY (PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID) "
			+ "values (?, "
			+ "(select ID from ACL_GROUP where QUALIFIED_NAME = ?), "
			+ "(select oid.ID from ACL_OBJECT_IDENTITY oid "
			+ "inner join ACL_CLASS c on c.ID = oid.CLASS_ID "
			+ "where CLASSNAME = ?  and oid.IDENTITY = ? )) ";

	private static final String FIND_ACL_FOR_CLASS_FROM_PARTY_FILTERED = "select oid.IDENTITY, ag.ID, ag.QUALIFIED_NAME as sorting_key, COALESCE(pro.NAME,'') as project_name from "
			+ "ACL_GROUP ag  inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "left outer join PROJECT pro on pro.PROJECT_ID = oid.IDENTITY "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID  where arse.PARTY_ID = ? and ac.CLASSNAME in ( ? , ? )"
			+ "and pro.NAME like ?";

	private static final String FIND_ACL_FOR_CLASS_FROM_USER = "select oid.IDENTITY, ag.ID, ag.QUALIFIED_NAME from "
			+ "ACL_GROUP ag  inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join CORE_PARTY cu on arse.PARTY_ID = cu.PARTY_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "left join CORE_TEAM team on team.PARTY_ID = cu.PARTY_ID "
			+ "left join CORE_TEAM_MEMBER tmemb on tmemb.TEAM_ID = team.PARTY_ID "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID, "
			+ "CORE_USER u "
			+ "where((u.PARTY_ID = tmemb.USER_ID) or (u.PARTY_ID = cu.PARTY_ID)) and (u.LOGIN = ? ) and (ac.CLASSNAME in ( ? , ? ) ) ";


	private static final String FIND_ACL_FOR_CLASS_FROM_PARTY = "select oid.IDENTITY, ag.ID, ag.QUALIFIED_NAME as sorting_key, COALESCE(pro.NAME,'') as project_name from "
			+ "ACL_GROUP ag  inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "left outer join PROJECT pro on pro.PROJECT_ID = oid.IDENTITY "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID  where arse.PARTY_ID = ? and ac.CLASSNAME in ( ? , ? )";

	//11-02-13 : this query is ready for task 1865
	private static final String USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS = "select arse.PARTY_ID, ag.ID, ag.QUALIFIED_NAME, CONCAT(COALESCE(cu.LOGIN, ''), COALESCE(ct.NAME, '')) as sorting_key, CONCAT(case when cu.LOGIN is NULL then 'TEAM' else 'USER' end, COALESCE(cu.LOGIN, ct.NAME)) as party_type from "
			+ "ACL_GROUP ag inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID "
			+ "left outer join CORE_USER cu on arse.PARTY_ID = cu.PARTY_ID "
			+ "left outer join CORE_TEAM ct on arse.PARTY_ID = ct.PARTY_ID "
			+ "where oid.IDENTITY = ? and ac.CLASSNAME = ? ";

	// 2015
	private static final String USER_NAME_FROM_IDENTITY_AND_CLASS = "select cu.LOGIN from "
			+ "ACL_GROUP ag inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID "
			+ "left outer join CORE_USER cu on arse.PARTY_ID = cu.PARTY_ID "
			+ "left outer join CORE_TEAM ct on arse.PARTY_ID = ct.PARTY_ID "
			+ "where oid.IDENTITY = ? ";

	// 11-02-13 : this query is ready for task 1865
	private static final String USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS_FILTERED = "select arse.PARTY_ID, ag.ID, ag.QUALIFIED_NAME, CONCAT(COALESCE(cu.LOGIN, ''), COALESCE(ct.NAME, '')) as sorting_key, CONCAT(case when cu.LOGIN is NULL then 'TEAM' else 'USER' end, COALESCE(cu.LOGIN, ct.NAME)) as party_type from "
			+ "ACL_GROUP ag inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on ag.ID = arse.ACL_GROUP_ID "
			+ "inner join ACL_OBJECT_IDENTITY oid on oid.ID = arse.OBJECT_IDENTITY_ID "
			+ "inner join ACL_CLASS ac on ac.ID = oid.CLASS_ID "
			+ "left outer join CORE_USER cu on arse.PARTY_ID = cu.PARTY_ID "
			+ "left outer join CORE_TEAM ct on arse.PARTY_ID = ct.PARTY_ID "
			+ "where oid.IDENTITY = ? and ac.CLASSNAME = ? "
			+ "and (cu.LOGIN like ? or ct.name like ?)";

	private static final String DELETE_PARTY_RESPONSABILITY_ENTRY = "delete from ACL_RESPONSIBILITY_SCOPE_ENTRY "
			+ "where PARTY_ID = ?  and OBJECT_IDENTITY_ID = "
			+ "(select oid.ID from ACL_OBJECT_IDENTITY oid  inner join ACL_CLASS c on c.ID = oid.CLASS_ID "
			+ "where oid.IDENTITY = ? and c.CLASSNAME = ?)";

	private static final String FIND_OBJECT_WITHOUT_PERMISSION_BY_PARTY = "select nro.IDENTITY from ACL_OBJECT_IDENTITY nro "
			+ "inner join ACL_CLASS nrc on nro.CLASS_ID = nrc.ID "
			+ "where nrc.CLASSNAME in ( ? , ? ) "
			+ "and not exists (select 1 "
			+ "from ACL_OBJECT_IDENTITY ro "
			+ "inner join ACL_CLASS rc on rc.ID = ro.CLASS_ID "
			+ "inner join ACL_RESPONSIBILITY_SCOPE_ENTRY r on r.OBJECT_IDENTITY_ID = ro.ID "
			+ "where ro.ID = nro.ID and rc.ID = nrc.ID and r.PARTY_ID = ?) ";

	private static final String FIND_PARTIES_WITHOUT_PERMISSION_BY_OBJECT = "select p.PARTY_ID from CORE_PARTY p "
			+ "left outer join CORE_USER cu on p.PARTY_ID = cu.PARTY_ID "
			+ "left outer join CORE_TEAM ct on p.PARTY_ID = ct.PARTY_ID "
			+ "where not exists (select 1  from ACL_OBJECT_IDENTITY aoi "
			+ "inner join ACL_CLASS ac on ac.ID = aoi.CLASS_ID "
			+ "inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on arse.OBJECT_IDENTITY_ID = aoi.ID "
			+ "where p.PARTY_ID = arse.PARTY_ID  and ac.CLASSNAME in ( ? , ? )  and aoi.IDENTITY = ? )";
	//+ "and (cu.ACTIVE = true or ct.PARTY_ID is not NULL)"; uncomment if only active user should be granted new clearances

	private static final String DELETE_OBJECT_IDENTITY = "delete from ACL_OBJECT_IDENTITY where IDENTITY = ? and CLASS_ID = ?";

	private static final String DELETE_ALL_RESPONSABILITY_ENTRIES = "delete from ACL_RESPONSIBILITY_SCOPE_ENTRY "
			+ "where OBJECT_IDENTITY_ID = (select oid.ID from ACL_OBJECT_IDENTITY oid "
			+ "inner join ACL_CLASS c on c.ID = oid.CLASS_ID "
			+ "where oid.IDENTITY = ? and c.CLASSNAME = ?)";

	private static final String DELETE_ALL_RESPONSABILITY_ENTRIES_FOR_PARTY = "delete from ACL_RESPONSIBILITY_SCOPE_ENTRY "
			+ "where PARTY_ID = ?";
	//IGNOREVIOLATIONS:START

    @Inject
	public JdbcManageableAclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache, DerivedPermissionsManager derivedManager) {
		super(dataSource, lookupStrategy);
        this.aclCache = aclCache;
        this.derivedManager = derivedManager;
        setFindChildrenQuery("select null as obj_id, null as class from ACL_OBJECT_IDENTITY where 0 = 1");
	}




	// ******************** ACL Modifications ***********************************


	@Override
	public void addNewResponsibility(@NotNull long partyId, @NotNull ObjectIdentity entityRef,
			@NotNull String qualifiedName) {
		jdbcTemplate.update(DELETE_PARTY_RESPONSABILITY_ENTRY,
            partyId, entityRef.getIdentifier(), entityRef.getType());

		jdbcTemplate.update(INSERT_PARTY_ACL_RESPONSABILITY_SCOPE,
            partyId, qualifiedName, entityRef.getType(), entityRef.getIdentifier());

		derivedManager.updateDerivedPermissions(partyId);

		evictFromCache(entityRef);
	}



	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#removeObjectIdentity(org.springframework.security.acls.model.ObjectIdentity)
	 */
	@Override
	public void removeObjectIdentity(ObjectIdentity objectIdentity) {
		LOGGER.info("Attempting to delete the Object Identity " + objectIdentity);

		long classId = retrieveClassPrimaryKey(objectIdentity.getType());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(WILL_ATTEMPT_TO_PERFORM + DELETE_OBJECT_IDENTITY + WITH_ARGS
					+ objectIdentity.getIdentifier() + ',' + classId + ']');
		}
		jdbcTemplate.update(DELETE_OBJECT_IDENTITY, objectIdentity.getIdentifier(), classId);

		derivedManager.updateDerivedPermissions(objectIdentity);

		evictFromCache(objectIdentity);
	}


	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#removeAllResponsibilities(org.springframework.security.acls.model.ObjectIdentity)
	 */
	@Override
	public void removeAllResponsibilities(ObjectIdentity entityRef) {
		jdbcTemplate.update(DELETE_ALL_RESPONSABILITY_ENTRIES, entityRef.getIdentifier(), entityRef.getType());

		derivedManager.updateDerivedPermissions(entityRef);

		evictFromCache(entityRef);
	}

	@Override
	public void removeAllResponsibilities(long partyId) {
		jdbcTemplate.update(DELETE_ALL_RESPONSABILITY_ENTRIES_FOR_PARTY, partyId);

		derivedManager.updateDerivedPermissions(partyId);

	}

	/**
	 * Removes all responsibilities a user might have on a entity. In other words, the given user will no longer have
	 * any permission on the entity.
	 *
	 * @param partyId
	 * @param entityRef
	 */
	@Override
	public void removeAllResponsibilities(@NotNull long partyId, @NotNull ObjectIdentity entityRef) {
		jdbcTemplate.update(DELETE_PARTY_RESPONSABILITY_ENTRY, partyId, entityRef.getIdentifier(), entityRef.getType());


		derivedManager.updateDerivedPermissions(partyId, entityRef);

		evictFromCache(entityRef);
	}

	@Override
	public void updateDerivedPermissions(long partyId){
		derivedManager.updateDerivedPermissions(partyId);
		aclCache.clearCache();
	}


	// ******************** /ACL Modifications ***********************************


	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#createObjectIdentity(org.springframework.security.acls.model.ObjectIdentity)
	 */
	@Override
	public void createObjectIdentity(@NotNull ObjectIdentity objectIdentity) throws AlreadyExistsException {
		LOGGER.info("Attempting to create the Object Identity " + objectIdentity);

		checkObjectIdentityDoesNotExist(objectIdentity);

		long classId = retrieveClassPrimaryKey(objectIdentity.getType());

		createObjectIdentity(objectIdentity.getIdentifier(), classId);

		derivedManager.updateDerivedPermissions(objectIdentity);

	}

	private void createObjectIdentity(Serializable objectIdentifier, long classId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(WILL_ATTEMPT_TO_PERFORM + INSERT_OBJECT_IDENTITY + WITH_ARGS + objectIdentifier
					+ ',' + classId + ']');
		}
		jdbcTemplate.update(INSERT_OBJECT_IDENTITY, objectIdentifier, classId);
	}

	private void checkObjectIdentityDoesNotExist(ObjectIdentity objectIdentity) {
		if (retrieveObjectIdentityPrimaryKey(objectIdentity) != null) {
			throw new AlreadyExistsException("Object identity '" + objectIdentity + "' already exists");
		}
	}

	private Long retrieveClassPrimaryKey(String type) throws UnknownAclClassException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(WILL_ATTEMPT_TO_PERFORM + SELECT_CLASS_PRIMARY_KEY + WITH_ARGS + type + ']');
		}
		List<Long> classIds = jdbcTemplate.queryForList(SELECT_CLASS_PRIMARY_KEY, new Object[] { type }, Long.class);

		if (!classIds.isEmpty()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Found classId " + classIds.get(0));
			}
			return classIds.get(0);
		}

		throw new UnknownAclClassException(type);
	}

	private Long retrieveObjectIdentityPrimaryKey(ObjectIdentity objectIdentity) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(WILL_ATTEMPT_TO_PERFORM + SELECT_OBJECT_IDENTITY_PRIMARY_KEY + WITH_ARGS
					+ objectIdentity.getType() + ',' + objectIdentity.getIdentifier() + ']');
		}

		try {
			//return jdbcTemplate.queryForLong(SELECT_OBJECT_IDENTITY_PRIMARY_KEY, objectIdentity.getType(), objectIdentity.getIdentifier());
			return jdbcTemplate.queryForObject(SELECT_OBJECT_IDENTITY_PRIMARY_KEY, new Object[] {objectIdentity.getType(), objectIdentity.getIdentifier()},Long.class);
		} catch (DataAccessException notFound) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#findAllPermissionGroupsByNamespace(java.lang.String)
	 */
	@Override
	public List<PermissionGroup> findAllPermissionGroupsByNamespace(@NotNull String namespace) {
		return jdbcTemplate.query(FIND_ALL_ACL_GROUPS_BY_NAMESPACE, new Object[] { namespace + '%' },
				permissionGroupMapper);
	}


	@Override
	public List<Object[]> retrieveClassAclGroupFromPartyId(@NotNull long partyId, String qualifiedClassName) {
		List<String> qualifiedClassNames = new ArrayList<>();
		qualifiedClassNames.add(qualifiedClassName);
		return retrieveClassAclGroupFromPartyId(partyId, qualifiedClassNames);
	}

	/**
	 * Only a size of 1 or 2 is supported for now for the second parameter.
	 */
	@Override
	public List<Object[]> retrieveClassAclGroupFromPartyId(long partyId, List<String> qualifiedClassNames) {

		List<String> adaptedQualifiedClassNames = adaptQualifiedClassNameList(qualifiedClassNames);

		return jdbcTemplate.query(FIND_ACL_FOR_CLASS_FROM_PARTY, new Object[] { partyId, adaptedQualifiedClassNames.get(0),adaptedQualifiedClassNames.get(1) },
				aclgroupMapper);
	}

	@Override
	public List<Object[]> retrieveClassAclGroupFromUserLogin(String userLogin,
			String qualifiedClassName) {
		List<String> qualifiedClassNames = new ArrayList<>();
		qualifiedClassNames.add(qualifiedClassName);
		return retrieveClassAclGroupFromUserLogin(userLogin,qualifiedClassNames);
	}

	@Override
	public List<Object[]> retrieveClassAclGroupFromUserLogin(String userLogin, List<String> qualifiedClassNames) {

		List<String> adaptedQualifiedClassNames = adaptQualifiedClassNameList(qualifiedClassNames);

		return jdbcTemplate.query(FIND_ACL_FOR_CLASS_FROM_USER, new Object[] { userLogin, adaptedQualifiedClassNames.get(0), adaptedQualifiedClassNames.get(1)},
				aclgroupMapper);
	}

	//TODO
	@Override
	public List<Object[]> retrieveClassAclGroupFromPartyId(@NotNull long partyId, String qualifiedClassName, Sorting sorting, Filtering filtering) {
		List<String> qualifiedClassNames = new ArrayList<>();
		qualifiedClassNames.add(qualifiedClassName);
		return retrieveClassAclGroupFromPartyId(partyId,qualifiedClassNames);
	}

	/**
	 * Only a size of 1 or 2 is supported for now for the second parameter.
	 */
	@Override
	public List<Object[]> retrieveClassAclGroupFromPartyId(long partyId,
			List<String> qualifiedClassNames, Sorting sorting, Filtering filtering) {

		String baseQuery;
		String orderByClause;
		Object[] arguments;

		List<String> adaptedQualifiedClassNames = adaptQualifiedClassNameList(qualifiedClassNames);

		if (filtering.isDefined()){
			baseQuery = FIND_ACL_FOR_CLASS_FROM_PARTY_FILTERED;
			String filter = "%"+filtering.getFilter()+"%";
			arguments = new Object[]{partyId, adaptedQualifiedClassNames.get(0),adaptedQualifiedClassNames.get(1), filter};
		}
		else{
			baseQuery = FIND_ACL_FOR_CLASS_FROM_PARTY;
			arguments = new Object[]{partyId,adaptedQualifiedClassNames.get(0),adaptedQualifiedClassNames.get(1)};
		}

		if (sorting.getSortedAttribute() != null && sorting.getSortedAttribute().contains("project.name")){
			orderByClause=" order by project_name ";
		}
		else{
			orderByClause=" order by sorting_key ";
		}

		orderByClause+= sorting.getSortOrder().getCode();

		String finalQuery = baseQuery + orderByClause;

		return jdbcTemplate.query(finalQuery, arguments, aclgroupMapper);
	}

	@Override
	public List<Long> findObjectWithoutPermissionByPartyId(long partyId, String qualifiedClass) {
		List<String> qualifiedClassNames = new ArrayList<>();
		qualifiedClassNames.add(qualifiedClass);
		return findObjectWithoutPermissionByPartyId(partyId,qualifiedClassNames);
	}


	/**
	 * Only a size of 1 or 2 is supported for now for the second parameter.
	 */
	@Override
	public List<Long> findObjectWithoutPermissionByPartyId(long partyId, List<String> qualifiedClasses) {

		List<String> adaptedQualifiedClasses = adaptQualifiedClassNameList(qualifiedClasses);

		List<BigInteger> reslult = jdbcTemplate.queryForList(FIND_OBJECT_WITHOUT_PERMISSION_BY_PARTY, new Object[] {
				adaptedQualifiedClasses.get(0),adaptedQualifiedClasses.get(1), partyId }, BigInteger.class);
		List<Long> finalResult = new ArrayList<>();
		for (BigInteger bigInteger : reslult) {
			finalResult.add(bigInteger.longValue());
		}
		return finalResult;
	}
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#findUsersWithExecutePermission(java.util.List)
	 */
	@Override
	public List<String> findUsersWithExecutePermission(List<ObjectIdentity> entityRefs) {
		List<Permission> permissions = new ArrayList<>();
		permissions.add(CustomPermission.EXECUTE);
		return findUsersWithPermissions(entityRefs, permissions);
	}

	private List<String> findUsersWithPermissions(List<ObjectIdentity> entityRefs, List<Permission> permissionsList) {
		List<String> resultSidList = new ArrayList<>();
		Collection<Acl> aclList;
		try {
			aclList = readAclsById(entityRefs).values();
		} catch (NotFoundException nfe) {
			LOGGER.debug("Acl not found for entities.", nfe);
			aclList = Collections.emptyList();
		}

		for (Acl acl : aclList) {
			List<AccessControlEntry> aces = acl.getEntries();

			for (AccessControlEntry ctrlEntry : aces) {

				List<Sid> sids = new ArrayList<>();
				List<Permission> permissions = new ArrayList<>();
				for (Permission permission : permissionsList) {
					permissions.add(permission);
				}
				sids.add(ctrlEntry.getSid());
				try {
					if (acl.isGranted(permissions, sids, false)) {
						PrincipalSid principalSid = (PrincipalSid) ctrlEntry.getSid();
						if (!resultSidList.contains(principalSid.getPrincipal())) {
							resultSidList.add(principalSid.getPrincipal());
						}
					}
				} catch (NotFoundException ex) {
					// this may happen quite often and is not an error case so we fine-grain log and then ignore
					LOGGER.debug("Error while processing acl list ", ex);

				} catch (UnloadedSidException | ClassCastException | NullPointerException ex) {
                    // not too sure about what should be done with other exceptions so we warn and then ignore
					LOGGER.warn("Error while processing acl list ", ex);

                }
			}
		}

		return resultSidList;
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#findUsersWithWritePermission(java.util.List)
	 */
	@Override
	public List<String> findUsersWithWritePermission(@NotNull List<ObjectIdentity> entityRefs) {
		List<Permission> permissions = new ArrayList<>();
		permissions.add(BasePermission.WRITE);
		return findUsersWithPermissions(entityRefs, permissions);
	}

	protected void evictFromCache(ObjectIdentity oIdentity) {
		if (aclCache != null) {
			aclCache.evictFromCache(oIdentity);
		}
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#retriveUserAndAclGroupNameFromIdentityAndClass(long, java.lang.Class)
	 */
	@Override
	public List<Object[]> retrievePartyAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass) {
		return jdbcTemplate.query(USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS, new Object[] { entityId, entityClass.getCanonicalName() },
				aclgroupMapper);

	}

	/* (non-Javadoc)
	 *
	 */
	@Override
	public List<Object[]> retrieveUsersFromIdentityAndClass(long entityId) {
		return jdbcTemplate.query(USER_NAME_FROM_IDENTITY_AND_CLASS,
 new Object[] { entityId },
				aclgroupMapper);

	}


	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.jdbc.ManageableAclService#findUsersWithoutPermissionByObject(long, java.lang.String)
	 */
	@Override
	public List<Object[]> retrievePartyAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass, Sorting sorting, Filtering filtering) {

		String baseQuery;
		String orderByClause;
		Object[] arguments;

		if (filtering.isDefined()){
			baseQuery = USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS_FILTERED;
			String filter = "%"+filtering.getFilter()+"%";
			arguments = new Object[]{entityId, entityClass.getCanonicalName(), filter, filter};
		}
		else{
			baseQuery = USER_AND_ACL_GROUP_NAME_FROM_IDENTITY_AND_CLASS;
			arguments = new Object[]{entityId, entityClass.getCanonicalName()};
		}

		if ("name".equals(sorting.getSortedAttribute())){
			orderByClause=" order by sorting_key ";
		}
		else if("qualifiedName".equals(sorting.getSortedAttribute())){
			orderByClause=" order by ag.QUALIFIED_NAME ";
		} else {
			orderByClause=" order by party_type ";
		}
		orderByClause+= sorting.getSortOrder().getCode();


		String finalQuery = baseQuery + orderByClause;

		return jdbcTemplate.query(finalQuery, arguments , aclgroupMapper);

	}

	@Override
	public List<Long> findPartiesWithoutPermissionByObject(long objectId, String qualifiedClassName) {
		List<String> qualifiedClassNames = new ArrayList<>();
		qualifiedClassNames.add(qualifiedClassName);
		qualifiedClassNames.add(qualifiedClassName);
		return findPartiesWithoutPermissionByObject(objectId,qualifiedClassNames);
	}

	/**
	 * Only a size of 1 or 2 is supported for now for the second parameter.
	 */
	@Override
	public List<Long> findPartiesWithoutPermissionByObject(long objectId, List<String> qualifiedClassNames) {

		qualifiedClassNames = adaptQualifiedClassNameList(qualifiedClassNames);

		List<BigInteger> result = jdbcTemplate.queryForList(FIND_PARTIES_WITHOUT_PERMISSION_BY_OBJECT,
				new Object[] {
				qualifiedClassNames.get(0),
				qualifiedClassNames.get(1),
				objectId
		},
		BigInteger.class);
		List<Long> finalResult = new ArrayList<>();
		for (BigInteger bigInteger : result) {
			finalResult.add(bigInteger.longValue());
		}
		return finalResult;
	}


	@Override
	public void refreshAcls() {
		aclCache.clearCache();
	}

	private List<String> adaptQualifiedClassNameList(List<String> qualifiedClassNameList){

		if( qualifiedClassNameList == null ||  qualifiedClassNameList.isEmpty() ||
				qualifiedClassNameList.size() > 2){
			throw new UnsupportedQualifiedNameListSizeException();
		}

		if(qualifiedClassNameList.size() == 1){
			qualifiedClassNameList.add(qualifiedClassNameList.get(0));
		}

		return qualifiedClassNameList;
	}

}
