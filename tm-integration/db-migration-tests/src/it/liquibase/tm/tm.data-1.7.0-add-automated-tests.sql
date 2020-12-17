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


INSERT INTO TEST_AUTOMATION_SERVER (SERVER_ID, BASE_URL, LOGIN, PASSWORD, KIND) VALUES
(1, 'http://localhost:9080/jenkins', 'bob', 'bob', 'jenkins');

INSERT INTO TEST_AUTOMATION_PROJECT (PROJECT_ID, NAME, SERVER_ID) VALUES
(1, 'automated_project', 1);

INSERT INTO TM_TA_PROJECTS (TM_PROJECT_ID, TA_PROJECT_ID) VALUES
(5, 1);

INSERT INTO AUTOMATED_TEST (TEST_ID, NAME, PROJECT_ID) VALUES
(1, 'test', 1),
(2, 'test2', 1);

UPDATE TEST_CASE
SET TA_TEST = 1
WHERE TCLN_ID = 93;

UPDATE TEST_CASE
SET TA_TEST = 2
WHERE TCLN_ID = 194;

INSERT INTO AUTOMATED_SUITE (SUITE_ID) VALUES
(1),
(2);

INSERT INTO AUTOMATED_EXECUTION_EXTENDER (EXTENDER_ID, MASTER_EXECUTION_ID, TEST_ID, RESULT_URL, SUITE_ID, RESULT_SUMMARY) VALUES
(1, 42, 1, 'http://localhost:9080/jenins/result', 1, 'summary'),
(2, 41, 2, 'http://localhost:9080/jenins/result', 2, 'summary');

INSERT INTO ISSUE (ISSUE_ID, REMOTE_ISSUE_ID, ISSUE_LIST_ID, BUGTRACKER_ID) VALUES
(11, '6', 175, 2),
(12, '7', 175, 2),
(13, '8', 176, 2);

DELETE FROM ITEM_TEST_PLAN_EXECUTION
where execution_id in (43, 42, 44);


