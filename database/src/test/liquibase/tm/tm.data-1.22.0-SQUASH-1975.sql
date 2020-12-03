-- Clean data because of negative id which mess with data migration which implies ids addition --
DELETE FROM TEST_CASE
WHERE TA_TEST = -1;

DELETE FROM AUTOMATED_TEST;
DELETE FROM TEST_AUTOMATION_PROJECT;
DELETE FROM TEST_AUTOMATION_SERVER;

-- Insert data for test --

-- create an attachment list. All will refer to it.

INSERT INTO ATTACHMENT_LIST (ATTACHMENT_LIST_ID) VALUES
(912);

-- create data

INSERT INTO TEST_CASE_LIBRARY (TCL_ID, ATTACHMENT_LIST_ID) VALUES (3, 908);
INSERT INTO REQUIREMENT_LIBRARY (RL_ID, ATTACHMENT_LIST_ID) VALUES (3, 908);
INSERT INTO CAMPAIGN_LIBRARY (CL_ID, ATTACHMENT_LIST_ID) VALUES (3, 908);
INSERT INTO CUSTOM_REPORT_LIBRARY (CRL_ID, ATTACHMENT_LIST_ID) VALUES (3, 908);

INSERT INTO TEST_AUTOMATION_SERVER (SERVER_ID, BASE_URL, LOGIN, PASSWORD, KIND, NAME, CREATED_ON, MANUAL_SLAVE_SELECTION)
VALUES (1, 'http://localhost:8081/jenkins', 'admin', 'admin', 'jenkins', 'server1', '2019-05-16', false),
(14, 'http://localhost:8082/jenkins', 'admin', 'admin', 'jenkins', 'server2', '2019-05-16', false);

INSERT INTO TEST_CASE_LIBRARY (TCL_ID, ATTACHMENT_LIST_ID) VALUES (4, 908);
INSERT INTO REQUIREMENT_LIBRARY (RL_ID, ATTACHMENT_LIST_ID) VALUES (4, 908);
INSERT INTO CAMPAIGN_LIBRARY (CL_ID, ATTACHMENT_LIST_ID) VALUES (4, 908);
INSERT INTO CUSTOM_REPORT_LIBRARY (CRL_ID, ATTACHMENT_LIST_ID) VALUES (4, 908);

INSERT INTO PROJECT (PROJECT_ID, NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID, REQ_CATEGORIES_LIST, TC_NATURES_LIST,
TC_TYPES_LIST, TCL_ID, CL_ID, RL_ID, CRL_ID, TA_SERVER_ID) VALUES
(3, 'project-test-squash-1975', 'admin', '2020-11-24', 908, 1, 2, 3, 3, 3, 3, 3, 1),
(4, 'project-test-squash-1975-2', 'admin', '2020-11-24', 908, 1, 2, 3, 4, 4, 4, 4, 14);

INSERT INTO TEST_AUTOMATION_PROJECT (TA_PROJECT_ID, TM_PROJECT_ID, LABEL, REMOTE_NAME, SERVER_ID, EXECUTION_ENVIRONMENTS, CAN_RUN_GHERKIN)
VALUES (1, 3, 'project_auto1', 'project_auto_1', 1, '', false),
(2, 4, 'project_auto1', 'project_auto_1', 14, '', false),
(3, 4, 'project_auto2', 'project_auto_2', 14, '', false);

