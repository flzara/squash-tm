Insert into ACL_OBJECT_IDENTITY(ID, IDENTITY, CLASS_ID) values 
 (14, 14, 1);
Insert into CAMPAIGN_LIBRARY(CL_ID) values 
 (14);
Insert into TEST_STEP(TEST_STEP_ID) values 
 (165), 
 (166);
Insert into TEST_CASE_LIBRARY(TCL_ID) values 
 (14);
Insert into REQUIREMENT_LIBRARY(RL_ID) values 
 (14);
Insert into ATTACHMENT_LIST(ATTACHMENT_LIST_ID) values 
 (888), 
 (889), 
 (893), 
 (897), 
 (894), 
 (891), 
 (892), 
 (895), 
 (896);
Insert into CORE_USER(ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_ON, LAST_MODIFIED_BY) values 
 (32, 'User-1', 'Charles', 'Dupond', 'charlesdupond@aaaa@aa', true, 'admin', '2011-09-30 10:26:23.0', null, null);
Insert into AUTH_USER(LOGIN, PASSWORD, ACTIVE) values 
 ('User-1', 'dcb13237f01f1419f1da2e8d0e387a1f2ad056e6', true);
Insert into ISSUE_LIST(ISSUE_LIST_ID) values 
 (312), 
 (310), 
 (311);
Insert into ACTION_TEST_STEP(TEST_STEP_ID, ACTION, EXPECTED_RESULT, ATTACHMENT_LIST_ID) values 
 (165, '<p>Fusce nec risus augue, at lacinia enim.</p><ul><li>Mauris eget arcu sem, eget suscipit dui. Integer vulputate venenatis urna a commodo. Sed elementum tincidunt adipiscing. Nulla o</li><li>dio tortor, dapibus sit amet interdum eu,</li><li>suscipit quis diam. Phasellus convallis</li></ul><p>pellentesque lorem, ut volutpat mi tincidunt vitae. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Aliquam libero nibh, sollicitud</p>', '<ol><li>Quisque euismod mi in nunc soda</li><li>les ac porta augue viverra. Fusce n</li><li>on odio nec magna gravida vehicula</li><li>. Donec tincidunt erat justo, ut fringilla</li></ol><p>velit. Donec dapibus ullamcorper leo, non bibendum quam condi<strong>mentum sit amet. Du</strong>is cursus dui quis tortor accumsan in pulvinar lectus mattis.</p>', 891), 
 (166, '<ul><li><u>Praesent nec enim id arcu porta fringilla u</u></li><li><u>t id nisi. Aenean tempor gravida lectus id co</u></li><li><u>mmodo. In vestibulum dictum ligula, nec feug</u></li></ul><p>iat lorem lacinia ac. Donec interdum, risus sed hendrerit tempus, est urna commodo odio, quis varius nulla metus non dolor. Curabitur nec arcu enim, sagittis adipiscing enim. Nam vehicula sodales ipsum, sit amet posuere ante hendrerit nec. In consequat feugiat tempor. Suspendisse mass</p>', '<p>Sed eget rhoncus sapien. Nam et pulvinar nisi. In mattis quam eu risus aliquet vel condimentum dui mattis. Ut vitae mi orci.</p><p>hasellus suscipit luctus qua</p><p>non scelerisque. Aliquam orci diam, ultrices vel cursus eget, gravida sed tortor. Maecenas in metus dui, ut rhoncus sem. Do</p>', 892);
Insert into CORE_GROUP_MEMBER(USER_ID, GROUP_ID) values 
 (32, 2);
Insert into PROJECT(PROJECT_ID, NAME, DESCRIPTION, LABEL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, CL_ID, TCL_ID, RL_ID) values 
 (14, 'Test Project-1', '<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam felis ante,</p>', 'Lorem ipsum dolor sit amet, ', true, 'admin', '2011-09-30 10:24:47.0', null, null, 14, 14, 14);
Insert into ACL_RESPONSIBILITY_SCOPE_ENTRY(ID, USER_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID) values 
(56, 32, 2, 14);
Insert into EXECUTION_STEP(EXECUTION_STEP_ID, EXPECTED_RESULT, ACTION, EXECUTION_STATUS, LAST_EXECUTED_BY, LAST_EXECUTED_ON, COMMENT, TEST_STEP_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, ISSUE_LIST_ID) values 
 (228, '<ol><li>Quisque euismod mi in nunc soda</li><li>les ac porta augue viverra. Fusce n</li><li>on odio nec magna gravida vehicula</li><li>. Donec tincidunt erat justo, ut fringilla</li></ol><p>velit. Donec dapibus ullamcorper leo, non bibendum quam condi<strong>mentum sit amet. Du</strong>is cursus dui quis tortor accumsan in pulvinar lectus mattis.</p>', '<p>Fusce nec risus augue, at lacinia enim.</p><ul><li>Mauris eget arcu sem, eget suscipit dui. Integer vulputate venenatis urna a commodo. Sed elementum tincidunt adipiscing. Nulla o</li><li>dio tortor, dapibus sit amet interdum eu,</li><li>suscipit quis diam. Phasellus convallis</li></ul><p>pellentesque lorem, ut volutpat mi tincidunt vitae. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Aliquam libero nibh, sollicitud</p>', 'SUCCESS', 'User-1', '2011-09-30 10:37:35.0', null, 165, 'User-1', '2011-09-30 10:37:26.0', 'User-1', '2011-09-30 10:37:35.0', 895, 310), 
 (229, '<p>Sed eget rhoncus sapien. Nam et pulvinar nisi. In mattis quam eu risus aliquet vel condimentum dui mattis. Ut vitae mi orci.</p><p>hasellus suscipit luctus qua</p><p>non scelerisque. Aliquam orci diam, ultrices vel cursus eget, gravida sed tortor. Maecenas in metus dui, ut rhoncus sem. Do</p>', '<ul><li><u>Praesent nec enim id arcu porta fringilla u</u></li><li><u>t id nisi. Aenean tempor gravida lectus id co</u></li><li><u>mmodo. In vestibulum dictum ligula, nec feug</u></li></ul><p>iat lorem lacinia ac. Donec interdum, risus sed hendrerit tempus, est urna commodo odio, quis varius nulla metus non dolor. Curabitur nec arcu enim, sagittis adipiscing enim. Nam vehicula sodales ipsum, sit amet posuere ante hendrerit nec. In consequat feugiat tempor. Suspendisse mass</p>', 'SUCCESS', 'User-1', '2011-09-30 10:37:37.0', null, 166, 'User-1', '2011-09-30 10:37:26.0', 'User-1', '2011-09-30 10:37:37.0', 896, 311);
Insert into ITERATION(ITERATION_ID, DELETED_ON, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, ACTUAL_END_AUTO, ACTUAL_END_DATE, ACTUAL_START_AUTO, ACTUAL_START_DATE, SCHEDULED_END_DATE, SCHEDULED_START_DATE, ATTACHMENT_LIST_ID) values 
 (83, null, '<p>Sed eget rhoncus sapien. Nam et pulvinar nisi. In mattis quam eu risus aliquet vel condimentum dui mattis. Ut vitae mi orci. Phasellus suscipit luctus quam non scelerisque. Aliquam orci diam, ultrices vel cursus eget, gravida sed tortor. Maecenas in metus dui, ut rhoncus sem. Do</p>', 'Iteration - 1', 'User-1', '2011-09-30 10:34:38.0', 'User-1', '2011-09-30 10:37:17.0', false, null, false, null, '2011-09-30 00:00:00.0', '2011-09-01 00:00:00.0', 894);
Insert into CAMPAIGN_LIBRARY_NODE(CLN_ID, DELETED_ON, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_ON, LAST_MODIFIED_BY, PROJECT_ID) values 
 (104, null, '<p>Sed eget rhoncus sapien. Nam et pulvinar nisi. In mattis quam eu risus aliquet vel condimentum dui mattis. Ut vitae mi orci. Phasellus suscipit luctus quam non scelerisque. Aliquam orci diam, ultrices vel cursus eget, gravida sed tortor. Maecenas in metus dui, ut rhoncus sem. Do</p>', 'Folder Test I', 'User-1', '2011-09-30 10:33:45.0', '2011-09-30 10:35:05.0', 'User-1', 14), 
 (105, null, '<p>Sed eget rhoncus sapien. Nam et pulvinar nisi. In mattis quam eu risus aliquet vel condimentum dui mattis. Ut vitae mi orci. Phasellus suscipit luctus quam non scelerisque. Aliquam orci diam, ultrices vel cursus eget, gravida sed tortor. Maecenas in metus dui, ut rhoncus sem. Do</p>', 'Campaign Test 1', 'User-1', '2011-09-30 10:34:02.0', '2011-09-30 10:35:15.0', 'User-1', 14);
Insert into TEST_CASE_LIBRARY_NODE(TCLN_ID, DELETED_ON, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, PROJECT_ID) values 
 (237, null, '<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam felis ante, accumsan quis tincidunt vel, iaculis in purus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi at nisl et est egestas venenatis. Nullam venenatis, justo ac hendrerit auctor, dolor velit adipiscing lacus, sed gravida enim elit dictum lorem. Suspendisse eu lectus ac metus lobortis vulputate at id purus. Proin dapibus commodo velit sit amet aliquet. Fusce cursus arcu vitae diam auctor vulputate. Integer et nunc et ipsum scelerisque dignissim eu ultricies sem. Pellentesque id commodo dui. Duis in vestibulum magna. Sed porta ante id magna semper ac gravida risus dapibus. Fusce eu ante sapien.</p>', 'Test Folder  a', 'User-1', '2011-09-30 10:30:47.0', 'User-1', '2011-09-30 10:35:38.0', 14), 
 (238, null, '<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam felis ante, accumsan quis tincidunt vel, iaculis in purus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi at nisl et est egestas venenatis. Nullam venenatis, justo ac hendrerit auctor, dolor velit adipiscing lacus, sed gravida enim elit dictum lorem. Suspendisse eu lectus ac metus lobortis vulputate at id purus. Proin dapibus commodo velit sit amet aliquet. Fusce cursus arcu vitae diam auctor vulputate. Integer et nunc et ipsum scelerisque dignissim eu ultricies sem. Pellentesque id commodo dui. Duis in vestibulum magna. Sed porta ante id magna semper ac gravida risus dapibus. Fusce eu ante sapien.</p>', 'Test-Case 1', 'User-1', '2011-09-30 10:30:57.0', 'User-1', '2011-09-30 10:35:26.0', 14);
Insert into REQUIREMENT_LIBRARY_NODE(RLN_ID, DELETED_ON, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, PROJECT_ID) values 
 (254, null, '<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam felis ante, accumsan quis tincidunt vel, iaculis in purus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi at nisl et est egestas venenatis. Nullam venenatis, justo ac hendrerit auctor, dolor velit adipiscing lacus, sed gravida enim elit dictum lorem. Suspendisse eu lectus ac metus lobortis vulputate at id purus. Proin dapibus commodo velit sit amet aliquet. Fusce cursus arcu vitae diam auctor vulputate. Integer et nunc et ipsum scelerisque dignissim eu ultricies sem. Pellentesque id commodo dui. Duis in vestibulum magna. Sed porta ante id magna semper ac gravida risus dapibus. Fusce eu ante sapien.</p>', 'Test Folder 1', 'User-1', '2011-09-30 10:29:33.0', 'User-1', '2011-09-30 10:35:53.0', 14), 
 (255, null, '<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam felis ante, accumsan quis tincidunt vel, iaculis in purus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi at nisl et est egestas venenatis. Nullam venenatis, justo ac hendrerit auctor, dolor velit adipiscing lacus, sed gravida enim elit dictum lorem. Suspendisse eu lectus ac metus lobortis vulputate at id purus. Proin dapibus commodo velit sit amet aliquet. Fusce cursus arcu vitae diam auctor vulputate. Integer et nunc et ipsum scelerisque dignissim eu ultricies sem. Pellentesque id commodo dui. Duis in vestibulum magna. Sed porta ante id magna semper ac gravida risus dapibus. Fusce eu ante sapien.</p>', 'Test Requirement 1', 'User-1', '2011-09-30 10:30:12.0', 'User-1', '2011-09-30 10:36:18.0', 14);
Insert into REQUIREMENT_LIBRARY_CONTENT(LIBRARY_ID, CONTENT_ID) values 
 (14, 254);
Insert into REQUIREMENT(RLN_ID, REFERENCE, CRITICALITY, ATTACHMENT_LIST_ID) values 
 (255, 'TF1-R1', 'MAJOR', 888);
Insert into REQUIREMENT_FOLDER(RLN_ID) values 
 (254);
Insert into TEST_CASE_FOLDER(TCLN_ID) values 
 (237);
Insert into CAMPAIGN_FOLDER(CLN_ID) values 
 (104);
Insert into TEST_CASE_LIBRARY_CONTENT(LIBRARY_ID, CONTENT_ID) values 
 (14, 237);
Insert into CAMPAIGN_LIBRARY_CONTENT(LIBRARY_ID, CONTENT_ID) values 
 (14, 104);
Insert into CAMPAIGN(CLN_ID, ACTUAL_END_AUTO, ACTUAL_END_DATE, ACTUAL_START_AUTO, ACTUAL_START_DATE, SCHEDULED_END_DATE, SCHEDULED_START_DATE, ATTACHMENT_LIST_ID) values 
 (105, true, null, true, null, '2011-09-30 00:00:00.0', '2011-09-01 00:00:00.0', 893);
Insert into TEST_CASE(TCLN_ID, VERSION, EXECUTION_MODE, ATTACHMENT_LIST_ID) values 
 (238, 1, 'MANUAL', 889);
Insert into CLN_RELATIONSHIP(ANCESTOR_ID, DESCENDANT_ID) values 
 (104, 105);
Insert into TEST_CASE_REQUIREMENT_LINK(TEST_CASE_ID, REQUIREMENT_ID) values 
 (238, 255);
Insert into CAMPAIGN_ITERATION(CAMPAIGN_ID, ITERATION_ID, ITERATION_ORDER) values 
 (105, 83, 0);
Insert into RLN_RELATIONSHIP(ANCESTOR_ID, DESCENDANT_ID) values 
 (254, 255);
Insert into CAMPAIGN_TEST_PLAN_ITEM(CTPI_ID, CAMPAIGN_ID, TEST_CASE_ID, USER_ID, TEST_PLAN_ORDER) values 
 (177, 105, 238, 32, 0);
Insert into TCLN_RELATIONSHIP(ANCESTOR_ID, DESCENDANT_ID) values 
 (237, 238);
Insert into TEST_CASE_STEPS(TEST_CASE_ID, STEP_ID, STEP_ORDER) values 
 (238, 165, 0), 
 (238, 166, 1);
Insert into ITERATION_TEST_PLAN_ITEM(ITEM_TEST_PLAN_ID, EXECUTION_STATUS, LAST_EXECUTED_BY, LAST_EXECUTED_ON, TCLN_ID, LABEL, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, USER_ID) values 
 (265, 'SUCCESS', 'User-1', '2011-09-30 10:37:37.0', 238, 'Test-Case 1', 'User-1', '2011-09-30 10:37:01.0', 'User-1', '2011-09-30 10:37:37.0', 32);
Insert into EXECUTION(EXECUTION_ID, TCLN_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, DESCRIPTION, NAME, EXECUTION_MODE, EXECUTION_STATUS, LAST_EXECUTED_BY, LAST_EXECUTED_ON, ATTACHMENT_LIST_ID, ISSUE_LIST_ID) values 
 (83, 238, 'User-1', '2011-09-30 10:37:26.0', 'User-1', '2011-09-30 10:37:37.0', null, 'Test-Case 1', 'MANUAL', 'SUCCESS', 'User-1', '2011-09-30 10:37:37.0', 897, 312);
Insert into ITEM_TEST_PLAN_EXECUTION(ITEM_TEST_PLAN_ID, EXECUTION_ID, EXECUTION_ORDER) values 
 (265, 83, 0);
Insert into ITEM_TEST_PLAN_LIST(ITERATION_ID, ITEM_TEST_PLAN_ID, ITEM_TEST_PLAN_ORDER) values 
 (83, 265, 0);
Insert into EXECUTION_EXECUTION_STEPS(EXECUTION_ID, EXECUTION_STEP_ID, EXECUTION_STEP_ORDER) values 
 (83, 228, 0), 
 (83, 229, 1);
