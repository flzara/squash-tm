--
--     This file is part of the Squashtest platform.
--     Copyright (C) Henix, henix.fr
--
--     See the NOTICE file distributed with this work for additional
--     information regarding copyright ownership.
--
--     This is free software: you can redistribute it and/or modify
--     it under the terms of the GNU Lesser General Public License as published by
--     the Free Software Foundation, either version 3 of the License, or
--     (at your option) any later version.
--
--     this software is distributed in the hope that it will be useful,
--     but WITHOUT ANY WARRANTY; without even the implied warranty of
--     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--     GNU Lesser General Public License for more details.
--
--     You should have received a copy of the GNU Lesser General Public License
--     along with this software.  If not, see <http://www.gnu.org/licenses/>.
--

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