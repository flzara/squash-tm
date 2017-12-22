Insert into CAMPAIGN_LIBRARY(CL_ID) values 
(6);
Insert into REQUIREMENT_LIBRARY(RL_ID) values 
 (6);
Insert into TEST_CASE_LIBRARY(TCL_ID) values 
 (6);
insert into PROJECT (NAME, ACTIVE, CREATED_BY, CREATED_ON, CL_ID, TCL_ID, RL_ID)
values ('squashtest', true, 'squash_it', '2011-09-21', 6,6,6);

insert into ACL_OBJECT_IDENTITY (IDENTITY, CLASS_ID)
values (1, 1);

insert into CORE_USER (LOGIN, FIRST_NAME, LAST_NAME, EMAIL, CREATED_BY, CREATED_ON)
values 
('Project.Manager','Project.Manager','Project.Manager','Project.Manager','squash_it', '2011-09-21'),
('Test.Editor','Test.Editor','Test.Editor','Test.Editor','squash_it', '2011-09-21'),
('Test.Runner','Test.Runner','Test.Runner','Test.Runner','squash_it', '2011-09-21'),
('Project.Viewer','Project.Viewer','Project.Viewer','Project.Viewer','squash_it', '2011-09-21');


insert into ACL_RESPONSIBILITY_SCOPE_ENTRY(USER_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID)
values
( (select cu.ID from CORE_USER cu where cu.LOGIN='Project.Manager'),
	(select ag.ID from ACL_GROUP ag where ag.QUALIFIED_NAME='squashtest.acl.group.tm.ProjectManager'),
	(select max(aoi.ID) from ACL_OBJECT_IDENTITY aoi) ),
( (select cu.ID from CORE_USER cu where cu.LOGIN='Test.Editor'),
	(select ag.ID from ACL_GROUP ag where ag.QUALIFIED_NAME='squashtest.acl.group.tm.TestEditor'),
	(select max(aoi.ID) from ACL_OBJECT_IDENTITY aoi) ),
( (select cu.ID from CORE_USER cu where cu.LOGIN='Test.Runner'),
	(select ag.ID from ACL_GROUP ag where ag.QUALIFIED_NAME='squashtest.acl.group.tm.TestRunner'),
	(select max(aoi.ID) from ACL_OBJECT_IDENTITY aoi) ),
( (select cu.ID from CORE_USER cu where cu.LOGIN='Project.Viewer'),
	(select ag.ID from ACL_GROUP ag where ag.QUALIFIED_NAME='squashtest.acl.group.tm.ProjectViewer'),
	(select max(aoi.ID) from ACL_OBJECT_IDENTITY aoi) );