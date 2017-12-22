insert into PROJECT(PROJECT_ID, NAME, DESCRIPTION, LABEL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, CL_ID, TCL_ID, RL_ID, ATTACHMENT_LIST_ID, REQ_CATEGORIES_LIST, TC_NATURES_LIST, TC_TYPES_LIST) values 
 (5, 'Sandbox #2', '', null, true, 'henix_admin', '2011-05-20 18:12:00.0', null, null, null, null, null, 9001,9001 ,9001 ,9001);

insert into CUSTOM_REPORT_LIBRARY (CRL_ID, ATTACHMENT_LIST_ID) values (1,9001);
insert into CUSTOM_REPORT_LIBRARY (CRL_ID, ATTACHMENT_LIST_ID) values (2,9001);

update PROJECT set CRL_ID = 1 WHERE PROJECT_ID=4;
update PROJECT set CRL_ID = 2 WHERE PROJECT_ID=5;

insert into ACL_OBJECT_IDENTITY (ID,IDENTITY,CLASS_ID) values (1,1,6);
insert into ACL_OBJECT_IDENTITY (ID,IDENTITY,CLASS_ID) values (2,4,1);

insert into ACL_OBJECT_IDENTITY (ID,IDENTITY,CLASS_ID) values (3,2,6);
insert into ACL_OBJECT_IDENTITY (ID,IDENTITY,CLASS_ID) values (4,5,1);

INSERT INTO CORE_GROUP (ID,QUALIFIED_NAME) VALUES
(1,'group');

INSERT INTO CORE_PARTY (PARTY_ID) VALUES
(10),
(11),
(12),
(20),
(21);

INSERT INTO CORE_TEAM (PARTY_ID, NAME, DESCRIPTION, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON) VALUES
(10, 'team1', 'this is team1', 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00'),
(11, 'team2', 'this is team2', 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00'),
(12, 'team3', 'this is team3', 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00');

INSERT INTO CORE_USER (PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON) VALUES
(20, 'user20', 'this is user20', 'this is user20', 'u@u', false, 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00'),
(21, 'user21', 'this is user21', 'this is user21', 'u@u', false, 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00');

INSERT INTO CORE_TEAM_MEMBER (TEAM_ID, USER_ID) VALUES
(10, 20),
(10, 21),
(11, 21);

INSERT INTO CORE_GROUP_MEMBER (PARTY_ID, GROUP_ID) VALUES
(20,1),
(21,1);

insert into ACL_RESPONSIBILITY_SCOPE_ENTRY(PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID) values
(20,4,2),
(20,4,4),
(20,4,3),
(21,5,2),
(21,5,3),
(21,5,4);