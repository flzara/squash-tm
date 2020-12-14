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


-- Feature 7183 when ordered lists have duplicate indices for order column


-- create an attachment list. All will refer to it.

INSERT INTO ATTACHMENT_LIST (ATTACHMENT_LIST_ID) VALUES
(907);

-- create a project

INSERT INTO TEST_CASE_LIBRARY (TCL_ID, ATTACHMENT_LIST_ID) VALUES (2, 907);
INSERT INTO REQUIREMENT_LIBRARY (RL_ID, ATTACHMENT_LIST_ID) VALUES (2, 907);
INSERT INTO CAMPAIGN_LIBRARY (CL_ID, ATTACHMENT_LIST_ID) VALUES (2, 907);
INSERT INTO CUSTOM_REPORT_LIBRARY (CRL_ID, ATTACHMENT_LIST_ID) VALUES (2, 907);

INSERT INTO PROJECT (PROJECT_ID, NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID, REQ_CATEGORIES_LIST, TC_NATURES_LIST,
TC_TYPES_LIST, TCL_ID, CL_ID, RL_ID, CRL_ID) VALUES
(2, 'project-test', 'admin', '2016-06-29', 907, 1, 2, 3, 2, 2, 2, 2);

-- create test cases

INSERT INTO TEST_CASE_LIBRARY_NODE (TCLN_ID, NAME, CREATED_BY, CREATED_ON, PROJECT_ID, ATTACHMENT_LIST_ID) VALUES
(-121, 'tc-test', 'admin', '2018-03-18', 2, 907),
(-122, 'tc-test-2', 'admin', '2018-03-18', 2, 907),
(-123, 'tc-test-3', 'admin', '2018-03-18', 2, 907);

INSERT INTO TEST_CASE (TCLN_ID, VERSION, TC_NATURE, TC_TYPE, PREREQUISITE) VALUES
(-121, 1, 12, 20, ''),
(-122, 1, 12, 20, ''),
(-123, 1, 12, 20, '');

-- and the steps

insert into TEST_STEP (TEST_STEP_ID) VALUES
(-15), (-16), (-17), (-18), (-19);

INSERT INTO TEST_CASE_STEPS (TEST_CASE_ID, STEP_ID, STEP_ORDER) VALUES
(-121, -15, 0),
(-121, -16, 1),
(-121, -17, 1),
(-122, -18, 0),
(-121, -19, 2);

-- create campaigns, iterations and test suites

INSERT INTO CAMPAIGN_LIBRARY_NODE (CLN_ID, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_ON, LAST_MODIFIED_BY,
PROJECT_ID, ATTACHMENT_LIST_ID) VALUES
(-17, '', 'dossier', 'admin', '2013-11-26 10:55:03', NULL, NULL, 2, 907),
(-20, '', 'dossier 2', 'admin', '2013-11-26 10:55:24', NULL, NULL, 2, 907);

INSERT INTO CAMPAIGN (CLN_ID, ACTUAL_END_AUTO, ACTUAL_START_AUTO, CAMPAIGN_STATUS) VALUES
(-17, false, false, 'UNDEFINED'),
(-20, false, false, 'UNDEFINED');

INSERT INTO DATASET (DATASET_ID, NAME, TEST_CASE_ID) VALUES
(-1, 'dataset-1', -121),
(-2, 'dataset-2', -122);

INSERT INTO CAMPAIGN_TEST_PLAN_ITEM (CTPI_ID, CAMPAIGN_ID, TEST_CASE_ID, USER_ID, TEST_PLAN_ORDER, DATASET_ID) VALUES
(-23, -17, -121, NULL, 0, NULL),
(-24, -17, -121, NULL, 1, -1),
(2, -20, -121, NULL, 4, NULL),
(-26, -17, -122, NULL, 1, -2),
(-27, -17, -123, NULL, 0, NULL),
(-28, -20, -122, NULL, 1, NULL);

INSERT INTO ITERATION (ITERATION_ID, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON,
ACTUAL_END_AUTO, ACTUAL_START_AUTO, ATTACHMENT_LIST_ID, ITERATION_STATUS) VALUES
(-15, '', 'iteration-1', 'admin', '2013-11-26 10:55:27', 'admin', '2013-11-26 12:14:53', false, false, 907, 'UNDEFINED'),
(-16, '', 'iteration-2', 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:55', false, false, 907, 'UNDEFINED');

INSERT INTO CAMPAIGN_ITERATION (CAMPAIGN_ID, ITERATION_ID, ITERATION_ORDER) VALUES
(-17, -15, 0),
(-20, -16, 0);

INSERT INTO ITERATION_TEST_PLAN_ITEM (ITEM_TEST_PLAN_ID, EXECUTION_STATUS, LAST_EXECUTED_BY, LAST_EXECUTED_ON, TCLN_ID,
LABEL, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, USER_ID, DATASET_ID) VALUES
(-47, 'READY', NULL, NULL, -121, 'cas de test', 'admin', '2013-11-26 10:56:44', 'admin', '2013-11-26 10:56:44', NULL, -1),
(-48, 'READY', NULL, NULL, -123, '01_Banker connects', 'admin', '2013-11-26 10:56:59', 'admin', '2013-11-26 10:58:32', NULL, NULL),
(-49, 'READY', NULL, NULL, -122, 'cas de test', 'admin', '2013-11-26 10:59:56', 'admin', '2013-11-26 10:59:56', NULL, -2),
(-50, 'READY', NULL, NULL, -122, 'cas de test', 'admin', '2013-11-26 11:01:27', 'admin', '2013-11-26 11:01:27', NULL, -2),
(-51, 'READY', NULL, NULL, -121, 'cas de test', 'admin', '2013-11-26 11:01:27', 'admin', '2013-11-26 11:01:27', NULL, -1),
(-52, 'READY', NULL, NULL, -121, 'cas de test', 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53', NULL, -1),
(-53, 'READY', NULL, NULL, -123, '01_Banker connects', 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53', NULL, NULL),
(-54, 'READY', NULL, NULL, -122, 'cas de test', 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53', NULL, -2);

INSERT INTO ITEM_TEST_PLAN_LIST (ITERATION_ID, ITEM_TEST_PLAN_ID, ITEM_TEST_PLAN_ORDER) VALUES
(-15, -47, 0),
(-15, -48, 1),
(-15, -49, 7),
(-15, -52, 0),
(-16, -53, 0),
(-16, -54, 1);

INSERT INTO TEST_SUITE (ID, NAME, DESCRIPTION, ATTACHMENT_LIST_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON) VALUES
(-6, 'suite', NULL, 907, 'admin', '2013-11-26 11:02:20', 'admin', '2013-11-26 11:02:20'),
(-7, 'suite 2', NULL, 907, 'admin', '2013-11-26 11:02:37', 'admin', '2013-11-26 11:02:37'),
(-8, 'suite 3', NULL, 907, 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53'),
(-9, 'suite 4', NULL, 907, 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53');

INSERT INTO ITERATION_TEST_SUITE (ITERATION_ID, TEST_SUITE_ID) VALUES
(-15, -6),
(-15, -7),
(-15, -8),
(-16, -9);

INSERT INTO TEST_SUITE_TEST_PLAN_ITEM (TPI_ID, SUITE_ID, TEST_PLAN_ORDER) VALUES
(-48, -6, 0),
(-49, -6, 5),
(-47, -6, 0);
