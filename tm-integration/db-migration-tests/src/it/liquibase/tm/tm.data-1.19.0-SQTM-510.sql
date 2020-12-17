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

insert into ACL_OBJECT_IDENTITY (ID, IDENTITY, CLASS_ID)
values
(30, 2, 1);

insert into CORE_PARTY(PARTY_ID)
values
(-40),
(-41),
(-42),
(-43),
(-44);

insert into CORE_GROUP (ID, QUALIFIED_NAME)
values
(1, 'squashtest.authz.group.core.Admin'),
(2, 'squashtest.authz.group.tm.User'),
(4,'squashtest.authz.group.tm.TestAutomationServer');

insert into CORE_USER (PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, CREATED_BY, CREATED_ON)
values
(-40, 'user-assigned', 'user-assigned', 'user-assigned', 'user-assigned', 'squash-it', '2018-11-19'),
(-41, 'user-assigned2', 'user-assigned2', 'user-assigned2', 'user-assigned2', 'squash-it', '2018-11-19'),
(-42, 'user-no-assigned', 'user-no-assigned', 'user-no-assigned', 'user-no-assigned3', 'squash-it', '2018-11-19'),
(-43, 'user-no-assigned2', 'user-no-assigned2', 'user-no-assigned2', 'user-no-assigned4', 'squash-it', '2018-11-19'),
(-44, 'admin2', 'admin2', 'admin2', 'admin2', 'squash-it', '2018-11-19');

insert into CORE_GROUP_MEMBER(PARTY_ID, GROUP_ID) values
(-40,2),
(-41,2),
(-42,2),
(-43,2),
(-44,1);

insert into ACL_RESPONSIBILITY_SCOPE_ENTRY(PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID)
values
(-40, (select ag.ID from ACL_GROUP ag where ag.QUALIFIED_NAME='squashtest.acl.group.tm.ProjectManager'), 30),
(-41, (select ag.ID from ACL_GROUP ag where ag.QUALIFIED_NAME='squashtest.acl.group.tm.TestEditor'), 30);
