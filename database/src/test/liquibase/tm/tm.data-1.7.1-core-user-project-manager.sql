/*
Create a user then add it to the core_group project manager
*/

insert into CORE_PARTY values(DEFAULT);
insert into CORE_USER(PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)
values ((select max(PARTY_ID) from CORE_PARTY), 'Bob', 'Bob', 'Bobovitch', 'bob@bob.com', true, 'admin', '2013-10-21', NULL, NULL);

insert into CORE_GROUP_MEMBER(PARTY_ID, GROUP_ID) values((select max(PARTY_ID) from CORE_PARTY), 3);



/*
 * create one user that will be project manager on multiple projects 3 and 6. 
 */
insert into CORE_PARTY values(DEFAULT);
insert into CORE_USER(PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)
values ((select max(PARTY_ID) from CORE_PARTY), 'Myiku', 'Myiku', 'Myiku', 'Myiku@Myiku.com', true, 'admin', '2013-10-21', NULL, NULL);

insert into ACL_RESPONSIBILITY_SCOPE_ENTRY(PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID)
values ((select max(PARTY_ID) from CORE_PARTY), 5, (select ID from ACL_OBJECT_IDENTITY where IDENTITY=3 and CLASS_ID=1 )),
		((select max(PARTY_ID) from CORE_PARTY), 5, (select ID from ACL_OBJECT_IDENTITY where IDENTITY=6 and CLASS_ID=1 ));
		
		
/*
 *create a team, that will be project manager on project 5 and add one user to it * 
 */
		
insert into CORE_PARTY values(DEFAULT);
insert into CORE_TEAM(PARTY_ID, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)	
values ((select max(PARTY_ID) from CORE_PARTY), 'The A Team', 'admin', '2013-10-21', NULL, NULL);
		
insert into ACL_RESPONSIBILITY_SCOPE_ENTRY(PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID)
values  ((select max(PARTY_ID) from CORE_PARTY), 5,  (select ID from ACL_OBJECT_IDENTITY where IDENTITY=5 and CLASS_ID=1 ));

		
insert into CORE_PARTY values(DEFAULT);
insert into CORE_USER(PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)
values ((select max(PARTY_ID) from CORE_PARTY), 'Robertu', 'Robertu', 'Robertu', 'Robertu@Robertu.com', true, 'admin', '2013-10-21', NULL, NULL);		

insert into CORE_TEAM_MEMBER(TEAM_ID, USER_ID)
values ((select max(PARTY_ID) from CORE_TEAM), (select max(PARTY_ID) from CORE_USER));


/*
 * Create a user that will be member of team 3 AND ALSO direct project manager of project 1
 * 
 */
insert into CORE_PARTY values(DEFAULT);
insert into CORE_USER(PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)
values ((select max(PARTY_ID) from CORE_PARTY), 'Garyu', 'Garyu', 'Garyu', 'Garyu@Garyu.com', true, 'admin', '2013-10-21', NULL, NULL);	

insert into CORE_TEAM_MEMBER(TEAM_ID, USER_ID)
values ((select max(PARTY_ID) from CORE_TEAM), (select max(PARTY_ID) from CORE_USER));

insert into ACL_RESPONSIBILITY_SCOPE_ENTRY(PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID)
values ((select max(PARTY_ID) from CORE_PARTY), 5,  (select ID from ACL_OBJECT_IDENTITY where IDENTITY=1 and CLASS_ID=1 ));
		

/*
 * Create some more users whose purpose is to be deactivated any, we'll test that they're wiped out
 */
insert into CORE_PARTY values(DEFAULT);
insert into CORE_USER(PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)
values ((select max(PARTY_ID) from CORE_PARTY), 'deactivated joe', 'deactivated joe', 'deactivated joe', 'deactivated joe@deactivated.com', true, 'admin', '2013-10-21', NULL, NULL);	

insert into CORE_GROUP_MEMBER(PARTY_ID, GROUP_ID) values((select max(PARTY_ID) from CORE_PARTY), 2);
insert into AUTH_USER(LOGIN, PASSWORD, ACTIVE) values('deactivated joe', 'aaa', false);


insert into CORE_PARTY values(DEFAULT);
insert into CORE_USER(PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)
values ((select max(PARTY_ID) from CORE_PARTY), 'deactivated jane', 'deactivated jane', 'deactivated jane', 'deactivated jane@deactivated.com', false, 'admin', '2013-10-21', NULL, NULL);	

insert into CORE_GROUP_MEMBER(PARTY_ID, GROUP_ID) values((select max(PARTY_ID) from CORE_PARTY), 2);
insert into AUTH_USER(LOGIN,PASSWORD, ACTIVE) values('deactivated jane', 'aaa', false);
insert into CORE_TEAM_MEMBER(TEAM_ID, USER_ID) values((select max(PARTY_ID) from CORE_TEAM), (select max(PARTY_ID) from CORE_USER));