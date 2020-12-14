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
(20,3),
(21,2);

INSERT INTO ACL_RESPONSIBILITY_SCOPE_ENTRY (ID, PARTY_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID) VALUES
(60,20,2,1),
(64,20,2,2),
(62,20,2,3),
(63,20,2,4),
(61,21,2,1),
(65,21,2,2),
(66,21,2,3),
(67,21,2,4);


-- 
-- test for issue 2474
-- add one custom field bindings for test steps in project 3. 
-- the custom field values are not created : the changeset 'tm-1.6.0.issue-2474-01' is supposed to create them
-- the only exception is test step 8 for which it will be created. We will also check that it was not created a second time.
--

insert into CUSTOM_FIELD_BINDING (cf_id, bound_entity, bound_project_id, position) values 
(1, 'TEST_STEP', 3, 1);


insert into CUSTOM_FIELD_VALUE (bound_entity_id, bound_entity_type, cfb_id, value)
select 8, 'TEST_STEP', max(cfb_id), 'success !'
from CUSTOM_FIELD_BINDING;
