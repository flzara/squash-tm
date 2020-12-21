
INSERT INTO TEST_AUTOMATION_SERVER (SERVER_ID, BASE_URL, LOGIN, PASSWORD, KIND, NAME, CREATED_ON, MANUAL_SLAVE_SELECTION)
VALUES (-1, 'http://localhost:8081/jenkins', 'admin', 'admin', 'jenkins', 'server1', '2019-05-16', false);

INSERT INTO TEST_AUTOMATION_PROJECT (TA_PROJECT_ID, TM_PROJECT_ID, LABEL, REMOTE_NAME, SERVER_ID, EXECUTION_ENVIRONMENTS, CAN_RUN_GHERKIN)
VALUES (-1, 2, 'project_auto1', 'project_auto_1', -1, '', false);

INSERT INTO AUTOMATED_TEST (TEST_ID, NAME, PROJECT_ID) VALUES (-1, 'test1.ta', -1);

INSERT INTO TEST_CASE_LIBRARY_NODE (TCLN_ID, NAME, CREATED_BY, CREATED_ON, PROJECT_ID, ATTACHMENT_LIST_ID) VALUES
(-100, 'tc-test', 'admin', '2018-03-18', 2, 907),
(-200, 'tc-test-2', 'admin', '2018-03-18', 2, 907),
(-300, 'tc-test-3', 'admin', '2018-03-18', 2, 907);

INSERT INTO TEST_CASE (TCLN_ID, VERSION, IMPORTANCE, IMPORTANCE_AUTO, PREREQUISITE, REFERENCE, TA_TEST, TC_STATUS, TC_NATURE, TC_TYPE, TC_KIND)
VALUES 	(-100, 1, 'LOW', false, '', '', NULL, 'WORK_IN_PROGRESS', 12, 20, 'STANDARD'),
		(-200, 1, 'LOW', false, '', '', NULL, 'WORK_IN_PROGRESS', 12, 20, 'STANDARD'),
		(-300, 1, 'LOW', false, '', '', -1, 'WORK_IN_PROGRESS', 12, 20, 'STANDARD');

INSERT INTO AUTOMATION_REQUEST(AUTOMATION_REQUEST_ID, REQUEST_STATUS, TEST_CASE_ID, PROJECT_ID)
VALUES (-20, 'AUTOMATED', -100, 2),
	   (-30, 'AUTOMATED', -300, 2);