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


Insert into ATTACHMENT_LIST(ATTACHMENT_LIST_ID) values
(762);
Insert into ISSUE_LIST(ISSUE_LIST_ID) values
(188);

INSERT INTO EXECUTION (EXECUTION_ID, TCLN_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, DESCRIPTION, NAME, EXECUTION_MODE, EXECUTION_STATUS, LAST_EXECUTED_BY, LAST_EXECUTED_ON, ATTACHMENT_LIST_ID, ISSUE_LIST_ID, PREREQUISITE, TC_NATURE, TC_TYPE, TC_STATUS, IMPORTANCE, REFERENCE, TC_DESCRIPTION) VALUES
(47, NULL, 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00', NULL, '111_Connection with the default user', 'MANUAL', 'FAILURE', 'guest_tpl', '2011-06-21 08:41:00', 762, 188, '', '', '', '', '', NULL, NULL);