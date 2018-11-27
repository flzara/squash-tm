insert into ACL_OBJECT_IDENTITY (ID, IDENTITY, CLASS_ID)
values
(-30, 2, 1);

insert into CORE_PARTY(PARTY_ID)
values
(-40),
(-41),
(-42),
(-43);

insert into CORE_USER (PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, CREATED_BY, CREATED_ON)
values
(-40, 'user-assigned', 'user-assigned', 'user-assigned', 'user-assigned', 'squash-it', '2018-11-19'),
(-41, 'user-assigned2', 'user-assigned2', 'user-assigned2', 'user-assigned2', 'squash-it', '2018-11-19'),
(-42, 'user-no-assigned', 'user-no-assigned', 'user-no-assigned', 'user-no-assigned3', 'squash-it', '2018-11-19'),
(-43, 'user-no-assigned2', 'user-no-assigned2', 'user-no-assigned2', 'user-no-assigned4', 'squash-it', '2018-11-19');

insert into ACL_RESPONSIBILITY_SCOPE_ENTRY(PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID)
values
( -40, (select ag.ID from ACL_GROUP ag where ag.QUALIFIED_NAME='squashtest.acl.group.tm.ProjectManager'), -30),
( -41,(select ag.ID from ACL_GROUP ag where ag.QUALIFIED_NAME='squashtest.acl.group.tm.TestEditor'), -30);
