insert into CUSTOM_FIELD (CF_ID, FIELD_TYPE, NAME, LABEL, OPTIONAL, DEFAULT_VALUE, INPUT_TYPE, CODE) values
(1, 'CF', 'cuf-text1', 'cuf-Text1', false, 'defaultt1', 'PLAIN_TEXT', 't1'),
(2, 'CF', 'cuf-text2', 'cuf-text2', true, '', 'PLAIN_TEXT', 't2'),
(3, 'SSF', 'cuf-deroulante1', 'cuf-deroulante1', false, 'opt11', 'DROPDOWN_LIST', 'd1'),
(4, 'SSF', 'cuf-deroulante2', 'cuf-deroulante2', true, '', 'DROPDOWN_LIST', 'd2'),
(5, 'CF', 'cuf-check', 'cuf-check', true, 'false', 'CHECKBOX', 'cuf_check');


insert into CUSTOM_FIELD_BINDING (CFB_ID, CF_ID, BOUND_ENTITY, BOUND_PROJECT_ID, POSITION) values
(1, 1, 'REQUIREMENT_VERSION', 4, 1),
(2, 2, 'REQUIREMENT_VERSION', 4, 2),
(3, 3, 'REQUIREMENT_VERSION', 4, 3),
(4, 4, 'REQUIREMENT_VERSION', 4, 4),
(5, 5, 'REQUIREMENT_VERSION', 4, 5);


insert into CUSTOM_FIELD_OPTION (CF_ID, LABEL, POSITION, CODE) values
(3, 'opt11', 0, 'opt11'),
(3, 'opt12', 1, 'opt12'),
(4, 'opt21', 0, 'opt21'),
(4, 'opt22', 1, 'opt22');


insert into CUSTOM_FIELD_VALUE (CFV_ID, BOUND_ENTITY_ID, BOUND_ENTITY_TYPE, CFB_ID, VALUE) values
(1, 176, 'REQUIREMENT_VERSION', 1, ''),
(2, 177, 'REQUIREMENT_VERSION', 1, 'defaultt1'),
(3, 178, 'REQUIREMENT_VERSION', 1, 'defaultt1'),
(4, 179, 'REQUIREMENT_VERSION', 1, 'magical'),
(5, 176, 'REQUIREMENT_VERSION', 2, ''),
(6, 177, 'REQUIREMENT_VERSION', 2, ''),
(7, 178, 'REQUIREMENT_VERSION', 2, ''),
(8, 179, 'REQUIREMENT_VERSION', 2, 'exception'),
(9, 176, 'REQUIREMENT_VERSION', 3, 'opt12'),
(10, 177, 'REQUIREMENT_VERSION', 3, 'opt11'),
(11, 178, 'REQUIREMENT_VERSION', 3, 'opt11'),
(12, 179, 'REQUIREMENT_VERSION', 3, 'opt11'),
(13, 176, 'REQUIREMENT_VERSION', 4, 'opt21'),
(14, 177, 'REQUIREMENT_VERSION', 4, ''),
(15, 178, 'REQUIREMENT_VERSION', 4, ''),
(16, 179, 'REQUIREMENT_VERSION', 4, ''),
(17, 176, 'REQUIREMENT_VERSION', 5, 'true'),
(18, 177, 'REQUIREMENT_VERSION', 5, 'false'),
(19, 178, 'REQUIREMENT_VERSION', 5, 'false'),
(20, 179, 'REQUIREMENT_VERSION', 5, 'false');



--
-- CUF binding for test-cases on project 4
-- 

INSERT INTO CUSTOM_FIELD_BINDING (CFB_ID, CF_ID, BOUND_ENTITY, BOUND_PROJECT_ID, POSITION) VALUES
(6, 1, 'TEST_CASE', 4, 1);

INSERT INTO CUSTOM_FIELD_VALUE (CFV_ID, BOUND_ENTITY_ID, BOUND_ENTITY_TYPE, CFB_ID, VALUE) VALUES
(21, 189, 'TEST_CASE', 6, 'defaultt1'),
(22, 190, 'TEST_CASE', 6, 'defaultt1'),
(23, 199, 'TEST_CASE', 6, 'defaultt1'),
(24, 200, 'TEST_CASE', 6, 'defaultt1'),
(25, 202, 'TEST_CASE', 6, 'defaultt1'),
(26, 203, 'TEST_CASE', 6, 'defaultt1'),
(27, 207, 'TEST_CASE', 6, 'defaultt1'),
(28, 208, 'TEST_CASE', 6, 'defaultt1'),
(29, 209, 'TEST_CASE', 6, 'defaultt1');


--
-- Imported test cases without cuf values
--
INSERT INTO ATTACHMENT_LIST (ATTACHMENT_LIST_ID) VALUES
(779),
(780),
(781),
(782),
(783),
(784),
(785),
(786),
(787),
(788),
(789),
(790),
(791),
(792),
(793),
(794),
(795);

INSERT INTO TEST_CASE_LIBRARY_NODE (TCLN_ID, DELETED_ON, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, PROJECT_ID, ATTACHMENT_LIST_ID) VALUES
(215, NULL, NULL, 'JDD-import_testcases', 'admin', '2013-03-26 11:07:52', NULL, NULL, 4, 779),
(216, NULL, '<p>The Wheel of Time</p>', 'JDD-TC04', 'yoda', '2012-08-26 00:00:00', NULL, NULL, 4, 780),
(217, NULL, '<p>Lord of the Rings</p>', 'JDD-TC03', 'gandalf', '1965-08-18 00:00:00', NULL, NULL, 4, 784),
(218, NULL, '<p>The Silmarillion</p>', 'JDD-TC02', 'gandalf', '1985-08-17 00:00:00', NULL, NULL, 4, 788),
(219, NULL, '<p>LOTR</p>', 'JDD-TC01', 'gandalf', '1985-08-17 00:00:00', NULL, NULL, 4, 792);
 
INSERT INTO TEST_CASE_FOLDER (TCLN_ID) VALUES
(215);

INSERT INTO TEST_CASE (TCLN_ID, VERSION, EXECUTION_MODE, IMPORTANCE, IMPORTANCE_AUTO, PREREQUISITE, REFERENCE, TA_TEST, TC_NATURE, TC_TYPE, TC_STATUS) VALUES
(216, 1, 'MANUAL', 'MEDIUM', false, '<ol><li>Being an Aes Sedai or an Asha''man</li></ol>', '', NULL, 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS'),
(217, 1, 'MANUAL', 'LOW', false, '<ol><li>Knowledge of Middle-Earth</li></ol>', '', NULL, 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS'),
(218, 1, 'MANUAL', 'VERY_HIGH', false, '<ol><li>Knowledge of Sindarin</li></ol>', '', NULL, 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS'),
(219, 1, 'MANUAL', 'HIGH', false, '<ol><li>Knowledge of test case creation</li></ol>', '', NULL, 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS');


INSERT INTO TCLN_RELATIONSHIP (ANCESTOR_ID, DESCENDANT_ID) VALUES
(215, 216),
(215, 217),
(215, 218),
(215, 219);

INSERT INTO TCLN_RELATIONSHIP_CLOSURE (ANCESTOR_ID, DESCENDANT_ID, DEPTH) VALUES
(215, 215, 0),
(216, 216, 0),
(217, 217, 0),
(218, 218, 0),
(219, 219, 0),
(215, 216, 1),
(215, 217, 1),
(215, 218, 1),
(215, 219, 1);



INSERT INTO TEST_STEP (TEST_STEP_ID) VALUES
(169),
(170),
(171),
(172),
(173),
(174),
(175),
(176),
(177),
(178),
(179),
(180);


INSERT INTO ACTION_TEST_STEP (TEST_STEP_ID, ACTION, EXPECTED_RESULT, ATTACHMENT_LIST_ID) VALUES
(169, '<p>STEP 1</p>', '<p>RS1</p>', 781),
(170, '<p>STEP 2</p>', '<p>RS2</p>', 782),
(171, '<p>STEP 3</p>', '<p>RS3</p>', 783),
(172, '<p>STEP 1</p>', '<p>RS1</p>', 785),
(173, '<p>STEP 2</p>', '<p>RS2</p>', 786),
(174, '<p>STEP 3</p>', '<p>RS3</p>', 787),
(175, '<p>STEP 1</p>', '<p>RS1</p>', 789),
(176, '<p>STEP 2</p>', '<p>RS2</p>', 790),
(177, '<p>STEP 3</p>', '<p>RS3</p>', 791),
(178, '<p>STEP 1</p>', '<p>RS1</p>', 793),
(179, '<p>STEP 2</p>', '<p>RS2</p>', 794),
(180, '<p>STEP 3</p>', '<p>RS3</p>', 795);


INSERT INTO TEST_CASE_STEPS (TEST_CASE_ID, STEP_ID, STEP_ORDER) VALUES
(216, 169, 0),
(216, 170, 1),
(216, 171, 2),
(217, 172, 0),
(217, 173, 1),
(217, 174, 2),
(218, 175, 0),
(218, 176, 1),
(218, 177, 2),
(219, 178, 0),
(219, 179, 1),
(219, 180, 2);

--
-- Added some test suites
--


INSERT INTO ATTACHMENT_LIST (ATTACHMENT_LIST_ID) VALUES
(796),
(797),
(798),
(799),
(800);


INSERT INTO TEST_SUITE (ID, NAME, DESCRIPTION, ATTACHMENT_LIST_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON) VALUES
(1, 'suite 1', '', 796, 'admin', '2013-04-16 11:48:23', 'admin', '2013-04-16 11:51:32'),
(2, 'suite 2', '', 797, 'admin', '2013-04-16 11:48:37', 'admin', '2013-04-16 11:51:40'),
(3, 'suite 3', '', 798, 'admin', '2013-04-16 11:48:59', 'admin', '2013-04-16 11:51:51'),
(4, 'suite 4', '', 799, 'admin', '2013-04-16 11:49:13', 'admin', '2013-04-16 11:51:56'),
(5, 'suite 5', '', 800, 'admin', '2013-04-16 11:49:49', 'admin', '2013-04-16 11:51:45');


INSERT INTO ITERATION_TEST_SUITE (ITERATION_ID, TEST_SUITE_ID) VALUES
(2, 1),
(2, 2),
(3, 5),
(4, 3),
(11, 4);


UPDATE ITERATION_TEST_PLAN_ITEM 
SET TEST_SUITE = 1
WHERE ITEM_TEST_PLAN_ID in (2,4,8,9,11);

UPDATE ITERATION_TEST_PLAN_ITEM 
SET TEST_SUITE = 2
WHERE ITEM_TEST_PLAN_ID in (3,5,6,7,10);

UPDATE ITERATION_TEST_PLAN_ITEM 
SET TEST_SUITE = 5
WHERE ITEM_TEST_PLAN_ID in (13,14,20);

UPDATE ITERATION_TEST_PLAN_ITEM 
SET TEST_SUITE = 3
WHERE ITEM_TEST_PLAN_ID in (22,24,28);

UPDATE ITERATION_TEST_PLAN_ITEM 
SET TEST_SUITE = 4
WHERE ITEM_TEST_PLAN_ID = 39;


