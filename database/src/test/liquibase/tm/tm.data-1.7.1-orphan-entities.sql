/* now add the orphan entities into the  test database */


INSERT INTO ATTACHMENT_LIST (ATTACHMENT_LIST_ID) VALUES
(807),
(808),
(809),
(810),
(811),
(812),
(813),
(814),
(815),
(816),
(817),
(818),
(819),
(820),
(821),
(822),
(823),
(824),
(825),
(826),
(827),
(828),
(829),
(830),
(831),
(832),
(833),
(834),
(835),
(836),
(837),
(838),
(839),
(840),
(841),
(842),
(843),
(844),
(845),
(846),
(847),
(848),
(849),
(850),
(851),
(852),
(853),
(854),
(855),
(856),
(857),
(858),
(859),
(860),
(861),
(862),
(863),
(864),
(865);


INSERT INTO ATTACHMENT_CONTENT (ATTACHMENT_CONTENT_ID, STREAM_CONTENT) VALUES
(9, ''),
(10, ''),
(11, ''),
(12, ''),
(13, ''),
(14, ''),
(15, ''),
(16, ''),
(17, ''),
(18, ''),
(19, ''),
(20, ''),
(21, ''),
(22, ''),
(23, '');


INSERT INTO ATTACHMENT (ATTACHMENT_ID, NAME, TYPE, SIZE, ADDED_ON, CONTENT_ID, ATTACHMENT_LIST_ID) VALUES
(9, 'texte0.txt', 'txt', 0, '2013-11-26 10:51:19', 9, 818),
(10, 'texte0.txt', 'txt', 0, '2013-11-26 10:54:02', 10, 817),
(11, 'texte0.txt', 'txt', 0, '2013-11-26 10:54:50', 11, 828),
(12, 'texte0.txt', 'txt', 0, '2013-11-26 10:54:50', 12, 832),
(13, 'texte0.txt', 'txt', 0, '2013-11-26 10:56:14', 13, 835),
(14, 'texte0.txt', 'txt', 0, '2013-11-26 10:56:31', 14, 834),
(15, 'texte0.txt', 'txt', 0, '2013-11-26 10:57:16', 15, 836),
(16, 'texte0.txt', 'txt', 0, '2013-11-26 10:57:49', 16, 837),
(17, 'texte0.txt', 'txt', 0, '2013-11-26 10:58:05', 17, 838),
(18, 'texte0.txt', 'txt', 0, '2013-11-26 10:58:24', 18, 843),
(19, 'texte0.txt', 'txt', 0, '2013-11-26 11:00:12', 19, 852),
(20, 'texte0.txt', 'txt', 0, '2013-11-26 12:14:52', 20, 858),
(21, 'texte0.txt', 'txt', 0, '2013-11-26 12:14:52', 21, 859),
(22, 'texte0.txt', 'txt', 0, '2013-11-26 12:14:53', 22, 860),
(23, 'texte0.txt', 'txt', 0, '2013-11-26 12:15:08', 23, 863);

/* =================================================================
	REQUIREMENTS
	================================================================*/



INSERT INTO RESOURCE (RES_ID, NAME, DESCRIPTION, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID) VALUES
(185, 'dossier ', '', 'admin', '2013-11-26 09:48:59', NULL, NULL, 807),
(186, 'exigence', '', 'admin', '2013-11-26 09:49:09', NULL, NULL, 808),
(187, 'exigence', '', 'admin', '2013-11-26 09:49:19', NULL, NULL, 809),
(188, 'exigence', '', 'admin', '2013-11-26 10:48:51', NULL, NULL, 810),
(189, 'exigence', '', 'admin', '2013-11-26 10:48:57', NULL, NULL, 811),
(190, 'exigence', '', 'admin', '2013-11-26 10:49:06', NULL, NULL, 812),
(191, 'dossier ', '', 'admin', '2013-11-26 10:49:12', NULL, NULL, 813),
(192, 'exigence', '', 'admin', '2013-11-26 10:49:12', NULL, NULL, 814),
(193, 'exigence', '', 'admin', '2013-11-26 10:49:12', NULL, NULL, 815),
(194, 'exigence', '', 'admin', '2013-11-26 10:49:12', NULL, NULL, 816),
(195, 'dossier2', '', 'admin', '2013-11-26 10:50:15', NULL, NULL, 819),
(196, 'dossier ', '', 'admin', '2013-11-26 10:50:23', NULL, NULL, 820),
(197, 'dossier2', '', 'admin', '2013-11-26 10:50:23', NULL, NULL, 821),
(198, 'exigence', '', 'admin', '2013-11-26 10:50:23', NULL, NULL, 822),
(199, 'exigence', '', 'admin', '2013-11-26 10:50:23', NULL, NULL, 823),
(200, 'exigence', '', 'admin', '2013-11-26 10:50:23', NULL, NULL, 824);


INSERT INTO SIMPLE_RESOURCE (RES_ID) VALUES
(185),
(191),
(195),
(196),
(197);


INSERT INTO REQUIREMENT_LIBRARY_NODE (RLN_ID, DELETED_ON, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, PROJECT_ID) VALUES
(190, NULL, 'admin', '2013-11-26 09:48:59', NULL, NULL, 6),
(191, NULL, 'admin', '2013-11-26 09:49:09', 'admin', '2013-11-26 10:48:57', 6),
(192, NULL, 'admin', '2013-11-26 10:48:51', 'admin', '2013-11-26 10:48:51', 6),
(193, NULL, 'admin', '2013-11-26 10:49:06', 'admin', '2013-11-26 10:49:06', 6),
(194, NULL, 'admin', '2013-11-26 10:49:12', NULL, NULL, 6),
(195, NULL, 'admin', '2013-11-26 10:49:12', 'admin', '2013-11-26 10:49:12', 6),
(196, NULL, 'admin', '2013-11-26 10:50:15', NULL, NULL, 6),
(197, NULL, 'admin', '2013-11-26 10:50:23', NULL, NULL, 6),
(198, NULL, 'admin', '2013-11-26 10:50:23', NULL, NULL, 6),
(199, NULL, 'admin', '2013-11-26 10:50:23', 'admin', '2013-11-26 10:50:23', 6);

INSERT INTO REQUIREMENT (RLN_ID, CURRENT_VERSION_ID) VALUES
(192, null),
(191, null),
(193, null),
(195, null),
(199, null);

INSERT INTO REQUIREMENT_VERSION (RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CRITICALITY, REQUIREMENT_STATUS, CATEGORY) VALUES
(186, 191, '', 1, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(187, 191, '', 2, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(188, 192, '', 1, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(189, 191, '', 3, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(190, 193, '', 1, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(192, 195, '', 3, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(193, 195, '', 2, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(194, 195, '', 1, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(198, 199, '', 3, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(199, 199, '', 2, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE'),
(200, 199, '', 1, 'MINOR', 'WORK_IN_PROGRESS', 'USE_CASE');

update REQUIREMENT
set CURRENT_VERSION_ID = 188
where RLN_ID = 192
;
update REQUIREMENT
set CURRENT_VERSION_ID = 189
where RLN_ID = 191
;
update REQUIREMENT
set CURRENT_VERSION_ID = 190
where RLN_ID = 193
;
update REQUIREMENT
set CURRENT_VERSION_ID = 192
where RLN_ID = 195
;
update REQUIREMENT
set CURRENT_VERSION_ID = 198
where RLN_ID = 199
;

INSERT INTO REQUIREMENT_FOLDER (RLN_ID, RES_ID) VALUES
(190, 185),
(194, 191),
(196, 195),
(197, 196),
(198, 197);







INSERT INTO RLN_RELATIONSHIP (ANCESTOR_ID, DESCENDANT_ID) VALUES
(190, 191),
(190, 196),
(194, 195),
(197, 198),
(197, 199);


INSERT INTO RLN_RELATIONSHIP_CLOSURE (ANCESTOR_ID, DESCENDANT_ID, DEPTH) VALUES
(190, 190, 0),
(191, 191, 0),
(190, 191, 1),
(192, 192, 0),
(193, 193, 0),
(194, 194, 0),
(195, 195, 0),
(194, 195, 1),
(196, 196, 0),
(190, 196, 1),
(197, 197, 0),
(198, 198, 0),
(199, 199, 0),
(197, 199, 1),
(197, 198, 1);



INSERT INTO REQUIREMENT_LIBRARY_CONTENT (LIBRARY_ID, CONTENT_ID) VALUES
(6, 190),
(6, 192);


INSERT INTO REQUIREMENT_AUDIT_EVENT (EVENT_ID, REQ_VERSION_ID, EVENT_DATE, AUTHOR) VALUES
(49, 186, '2013-11-26 09:49:09', 'admin'),
(50, 187, '2013-11-26 09:49:19', 'admin'),
(51, 188, '2013-11-26 10:48:51', 'admin'),
(52, 189, '2013-11-26 10:48:57', 'admin'),
(53, 190, '2013-11-26 10:49:06', 'admin'),
(54, 192, '2013-11-26 10:49:12', 'admin'),
(55, 198, '2013-11-26 10:50:23', 'admin');

INSERT INTO REQUIREMENT_CREATION (EVENT_ID) VALUES
(49),
(50),
(51),
(52),
(53),
(54),
(55);

/* =================================================================
	TEST CASES
	================================================================*/

INSERT INTO TEST_CASE_LIBRARY_NODE (TCLN_ID, DELETED_ON, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, PROJECT_ID, ATTACHMENT_LIST_ID) VALUES
(220, NULL, '<p>\n	def</p>\n', 'cas de test', 'admin', '2013-11-26 10:49:36', NULL, NULL, 6, 817),
(221, NULL, '', 'dossier', 'admin', '2013-11-26 10:49:49', NULL, NULL, 6, 818),
(222, NULL, '', 'dossier 2', 'admin', '2013-11-26 10:50:42', NULL, NULL, 6, 825),
(223, NULL, '', 'dossier', 'admin', '2013-11-26 10:54:50', NULL, NULL, 6, 828),
(224, NULL, '', 'dossier 2', 'admin', '2013-11-26 10:54:50', NULL, NULL, 6, 829),
(225, NULL, '<p>\n	def</p>\n', 'cas de test', 'admin', '2013-11-26 10:54:50', NULL, NULL, 6, 832);

INSERT INTO TEST_CASE (TCLN_ID, VERSION, EXECUTION_MODE, IMPORTANCE, IMPORTANCE_AUTO, PREREQUISITE, REFERENCE, TA_TEST, TC_NATURE, TC_TYPE, TC_STATUS) VALUES
(220, 1, 'MANUAL', 'LOW', false, '', 'ref', NULL, 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS'),
(225, 1, 'MANUAL', 'LOW', false, '', 'ref', NULL, 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS');

INSERT INTO TEST_CASE_FOLDER (TCLN_ID) VALUES
(221),
(222),
(223),
(224);

INSERT INTO TEST_CASE_LIBRARY_CONTENT (LIBRARY_ID, CONTENT_ID) VALUES
(6, 221);


INSERT INTO TEST_STEP (TEST_STEP_ID) VALUES
(181),
(182),
(183),
(184),
(185),
(186),
(187),
(188);
INSERT INTO ACTION_TEST_STEP (TEST_STEP_ID, ACTION, EXPECTED_RESULT, ATTACHMENT_LIST_ID) VALUES

(181, '<p>\n	test</p>\n', '', 826),
(182, '<p>\n	test</p>\n', '', 827),
(185, '<p>\n	test</p>\n', '', 830),
(186, '<p>\n	test</p>\n', '', 831);


INSERT INTO CALL_TEST_STEP (TEST_STEP_ID, CALLED_TEST_CASE_ID) VALUES
(183, 197),
(187, 197),
(184, 220),
(188, 217);

INSERT INTO TEST_CASE_STEPS (TEST_CASE_ID, STEP_ID, STEP_ORDER) VALUES
(220, 181, 0),
(220, 182, 1),
(220, 183, 2),
(145, 184, 5),
(225, 185, 0),
(225, 186, 1),
(225, 187, 2),
(217, 188, 3);

INSERT INTO TCLN_RELATIONSHIP (ANCESTOR_ID, DESCENDANT_ID) VALUES
(221, 222),
(222, 220),
(223, 224),
(224, 225);

INSERT INTO TCLN_RELATIONSHIP_CLOSURE (ANCESTOR_ID, DESCENDANT_ID, DEPTH) VALUES
(220, 220, 0),
(221, 221, 0),
(222, 222, 0),
(221, 222, 1),
(222, 220, 1),
(221, 220, 2),
(223, 223, 0),
(224, 224, 0),
(225, 225, 0),
(223, 224, 1),
(224, 225, 1),
(223, 225, 2);


INSERT INTO DATASET (DATASET_ID, NAME, TEST_CASE_ID) VALUES
(1, 'dataset', 220),
(3, 'dataset', 225),
(2, 'dataset 2', 145);


INSERT INTO PARAMETER (PARAM_ID, NAME, TEST_CASE_ID, DESCRIPTION) VALUES
(1, 'param', 220, ''),
(2, 'param', 225, '');


INSERT INTO DATASET_PARAM_VALUE (DATASET_PARAM_VALUE_ID, DATASET_ID, PARAM_ID, PARAM_VALUE) VALUES
(1, 1, 1, 'param'),
(2, 2, 1, 'param'),
(3, 3, 2, 'param');

INSERT INTO REQUIREMENT_VERSION_COVERAGE (REQUIREMENT_VERSION_COVERAGE_ID, VERIFIED_REQ_VERSION_ID, VERIFYING_TEST_CASE_ID) VALUES
(58, 187, 132),
(57, 187, 183),
(55, 187, 197),
(56, 187, 204),
(67, 189, 220),
(68, 189, 225),
(62, 193, 132),
(60, 193, 183),
(59, 193, 197),
(61, 193, 204),
(66, 199, 132),
(63, 199, 183),
(64, 199, 197),
(65, 199, 204);

/* =================================================================
	CAMPAIGNS
	================================================================*/

update CAMPAIGN_TEST_PLAN_ITEM
set TEST_PLAN_ORDER = 6 where CTPI_ID = 6
;
update CAMPAIGN_TEST_PLAN_ITEM
set TEST_PLAN_ORDER = 7 where CTPI_ID = 8
;

update ITEM_TEST_PLAN_LIST
set ITEM_TEST_PLAN_ORDER =  6
where ITEM_TEST_PLAN_ID = 33
;

update ITEM_TEST_PLAN_LIST
set ITEM_TEST_PLAN_ORDER = 7
where ITEM_TEST_PLAN_ID = 34
;


INSERT INTO CAMPAIGN_LIBRARY_NODE (CLN_ID, DELETED_ON, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_ON, LAST_MODIFIED_BY, PROJECT_ID, ATTACHMENT_LIST_ID) VALUES
(15, NULL, '', 'dossier', 'admin', '2013-11-26 10:55:03', NULL, NULL, 6, 833),
(16, NULL, '', 'dossier 2', 'admin', '2013-11-26 10:55:12', NULL, NULL, 6, 834),
(17, NULL, '', 'campagne', 'admin', '2013-11-26 10:55:19', '2013-11-26 12:14:53', 'admin', 6, 835),
(18, NULL, '', 'dossier', 'admin', '2013-11-26 12:14:52', NULL, NULL, 6, 857),
(19, NULL, '', 'dossier 2', 'admin', '2013-11-26 12:14:52', NULL, NULL, 6, 858),
(20, NULL, '', 'campagne', 'admin', '2013-11-26 12:14:53', NULL, NULL, 6, 859);


INSERT INTO CAMPAIGN (CLN_ID, ACTUAL_END_AUTO, ACTUAL_END_DATE, ACTUAL_START_AUTO, ACTUAL_START_DATE, SCHEDULED_END_DATE, SCHEDULED_START_DATE) VALUES
(17, false, NULL, false, NULL, NULL, NULL),
(20, false, NULL, false, NULL, NULL, NULL);

INSERT INTO CAMPAIGN_FOLDER (CLN_ID) VALUES
(15),
(16),
(18),
(19);



INSERT INTO CAMPAIGN_LIBRARY_CONTENT (LIBRARY_ID, CONTENT_ID) VALUES
(6, 15);


INSERT INTO CLN_RELATIONSHIP (ANCESTOR_ID, DESCENDANT_ID) VALUES
(15, 16),
(16, 17),
(18, 19),
(19, 20);

INSERT INTO CLN_RELATIONSHIP_CLOSURE (ANCESTOR_ID, DESCENDANT_ID, DEPTH) VALUES
(15, 15, 0),
(16, 16, 0),
(15, 16, 1),
(17, 17, 0),
(16, 17, 1),
(15, 17, 2),
(18, 18, 0),
(19, 19, 0),
(20, 20, 0),
(18, 19, 1),
(19, 20, 1),
(18, 20, 2);


INSERT INTO CAMPAIGN_TEST_PLAN_ITEM (CTPI_ID, CAMPAIGN_ID, TEST_CASE_ID, USER_ID, TEST_PLAN_ORDER) VALUES
(23, 17, 197, NULL, 0),
(24, 17, 220, NULL, 1),
(25, 7, 225, NULL, 4),
(26, 7, 220, NULL, 5),
(27, 20, 197, NULL, 0),
(28, 20, 220, NULL, 1);

INSERT INTO ITERATION (ITERATION_ID, DELETED_ON, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, ACTUAL_END_AUTO, ACTUAL_END_DATE, ACTUAL_START_AUTO, ACTUAL_START_DATE, SCHEDULED_END_DATE, SCHEDULED_START_DATE, ATTACHMENT_LIST_ID) VALUES
(15, NULL, '', 'iteration', 'admin', '2013-11-26 10:55:27', 'admin', '2013-11-26 12:14:53', false, NULL, false, NULL, NULL, NULL, 836),
(16, NULL, '', 'iteration', 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53', false, NULL, false, NULL, NULL, NULL, 860);


INSERT INTO CAMPAIGN_ITERATION (CAMPAIGN_ID, ITERATION_ID, ITERATION_ORDER) VALUES
(17, 15, 0),
(20, 16, 0);


INSERT INTO ITERATION_TEST_PLAN_ITEM (ITEM_TEST_PLAN_ID, EXECUTION_STATUS, LAST_EXECUTED_BY, LAST_EXECUTED_ON, TCLN_ID, LABEL, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, USER_ID, DATASET_ID) VALUES
(47, 'READY', NULL, NULL, 220, 'cas de test', 'admin', '2013-11-26 10:56:44', 'admin', '2013-11-26 10:56:44', NULL, 1),
(48, 'READY', NULL, NULL, 132, '01_Banker connects', 'admin', '2013-11-26 10:56:59', 'admin', '2013-11-26 10:58:32', 1, NULL),
(49, 'READY', NULL, NULL, 225, 'cas de test', 'admin', '2013-11-26 10:59:56', 'admin', '2013-11-26 10:59:56', NULL, 3),
(50, 'READY', NULL, NULL, 225, 'cas de test', 'admin', '2013-11-26 11:01:27', 'admin', '2013-11-26 11:01:27', NULL, 3),
(51, 'READY', NULL, NULL, 220, 'cas de test', 'admin', '2013-11-26 11:01:27', 'admin', '2013-11-26 11:01:27', NULL, 1),
(52, 'READY', NULL, NULL, 220, 'cas de test', 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53', NULL, 1),
(53, 'READY', NULL, NULL, 132, '01_Banker connects', 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53', 1, NULL),
(54, 'READY', NULL, NULL, 225, 'cas de test', 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53', NULL, 3);


INSERT INTO ITEM_TEST_PLAN_LIST (ITERATION_ID, ITEM_TEST_PLAN_ID, ITEM_TEST_PLAN_ORDER) VALUES
(15, 47, 0),
(15, 48, 1),
(15, 49, 2),
(5, 50, 4),
(5, 51, 5),
(16, 52, 0),
(16, 53, 1),
(16, 54, 2);

INSERT INTO TEST_SUITE (ID, NAME, DESCRIPTION, ATTACHMENT_LIST_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON) VALUES
(6, 'suite', NULL, 855, 'admin', '2013-11-26 11:02:20', 'admin', '2013-11-26 11:02:20'),
(7, 'suite 2', NULL, 856, 'admin', '2013-11-26 11:02:37', 'admin', '2013-11-26 11:02:37'),
(8, 'suite', NULL, 861, 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53'),
(9, 'suite 2', NULL, 862, 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53');

INSERT INTO ITERATION_TEST_SUITE (ITERATION_ID, TEST_SUITE_ID) VALUES
(15, 6),
(15, 7),
(16, 8),
(16, 9);


INSERT INTO TEST_SUITE_TEST_PLAN_ITEM (TPI_ID, SUITE_ID, TEST_PLAN_ORDER) VALUES
(48, 6, 0),
(49, 6, 1),
(47, 7, 0),
(48, 7, 1),
(53, 8, 0),
(54, 8, 1),
(52, 9, 0),
(53, 9, 1);


INSERT INTO ISSUE_LIST (ISSUE_LIST_ID) VALUES
(188),
(189),
(190),
(191),
(192),
(193),
(194),
(195),
(196),
(197),
(198),
(199),
(200),
(201),
(202),
(203),
(204),
(205),
(206),
(207),
(208);

INSERT INTO EXECUTION (EXECUTION_ID, TCLN_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, DESCRIPTION, NAME, EXECUTION_MODE, EXECUTION_STATUS, LAST_EXECUTED_BY, LAST_EXECUTED_ON, ATTACHMENT_LIST_ID, ISSUE_LIST_ID, PREREQUISITE, TC_NATURE, TC_TYPE, TC_STATUS, IMPORTANCE, REFERENCE, TC_DESCRIPTION) VALUES
(46, 132, 'admin', '2013-11-26 10:57:20', 'admin', '2013-11-26 10:58:07', NULL, '01_Banker connects', 'MANUAL', 'FAILURE', 'admin', '2013-11-26 10:58:07', 837, 188, '', 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS', 'LOW', '', '<p>\n	This is the nominal case (<span>without errors</span>) where the <strong>Banker</strong> connects to <span><strong><u>NX Bank</u></strong></span>.</p>\n<p>\n	</p>\n<p>\n	In this nomminal case, we also check :</p>\n<ul>\n	<li>\n		the interface.</li>\n	<li>\n		the navigation from this screen.</li>\n</ul>\n'),
(47, 220, 'admin', '2013-11-26 10:58:24', 'admin', '2013-11-26 10:58:24', NULL, 'ref - cas de test', 'MANUAL', 'READY', NULL, NULL, 843, 194, '', 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS', 'LOW', 'ref', '<p>\n	def</p>\n'),
(48, 132, 'admin', '2013-11-26 10:58:32', 'admin', '2013-11-26 10:58:32', NULL, '01_Banker connects', 'MANUAL', 'READY', NULL, NULL, 846, 197, '', 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS', 'LOW', '', '<p>\n	This is the nominal case (<span>without errors</span>) where the <strong>Banker</strong> connects to <span><strong><u>NX Bank</u></strong></span>.</p>\n<p>\n	</p>\n<p>\n	In this nomminal case, we also check :</p>\n<ul>\n	<li>\n		the interface.</li>\n	<li>\n		the navigation from this screen.</li>\n</ul>\n'),
(49, 220, 'admin', '2013-11-26 11:00:12', 'admin', '2013-11-26 11:00:12', NULL, 'ref - cas de test', 'MANUAL', 'READY', NULL, NULL, 852, 203, '', 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS', 'LOW', 'ref', '<p>\n	def</p>\n'),
(50, 220, 'admin', '2013-11-26 12:15:08', 'admin', '2013-11-26 12:15:08', NULL, 'ref - cas de test', 'MANUAL', 'READY', NULL, NULL, 863, 206, '', 'UNDEFINED', 'UNDEFINED', 'WORK_IN_PROGRESS', 'LOW', 'ref', '<p>\n	def</p>\n');

INSERT INTO AUTOMATED_TEST(TEST_ID,NAME,PROJECT_ID) VALUES
(3, 'test3', 1)
;

update TEST_CASE
set TA_TEST = 3
where TCLN_ID = 220
;

INSERT INTO AUTOMATED_SUITE (SUITE_ID) VALUES
(3);

INSERT INTO AUTOMATED_EXECUTION_EXTENDER (EXTENDER_ID, MASTER_EXECUTION_ID, TEST_ID, RESULT_URL,SUITE_ID,RESULT_SUMMARY ) VALUES
(3, 50, 3, 'http://localhost:9080/jenkins/result2', 3, 'summary');

INSERT INTO ITEM_TEST_PLAN_EXECUTION (ITEM_TEST_PLAN_ID, EXECUTION_ID, EXECUTION_ORDER) VALUES
(48, 46, 0),
(47, 47, 0),
(48, 48, 1),
(47, 49, 1),
(52, 50, 0);

INSERT INTO EXECUTION_STEP (EXECUTION_STEP_ID, EXPECTED_RESULT, ACTION, EXECUTION_STATUS, LAST_EXECUTED_BY, LAST_EXECUTED_ON, COMMENT, TEST_STEP_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, ISSUE_LIST_ID) VALUES
(143, '<p>\n	-</p>\n', '<p>\n	<em><u>Pr requisite :</u></em></p>\n<ul>\n	<li>\n		Access to a browser : FF 4 or IE 7.</li>\n	<li>\n		A <span>valid login and password</span> for a <strong>Admin</strong> profile.</li>\n</ul>\n', 'FAILURE', 'admin', '2013-11-26 10:58:07', NULL, 6, 'admin', '2013-11-26 10:57:20', 'admin', '2013-11-26 10:58:07', 838, 189),
(144, '<p>\n	The user is connected to <span><strong><u>NX Pet Shop Back Office</u></strong></span>.</p>\n<p>\n	</p>\n<p>\n	The screen Authorisation is displayed.</p>\n', '<p>\n	<em><u>Connection :</u></em></p>\n<p>\n	The user can connect to the web application at the following adress :</p>\n<p>\n	<span><u>http://localhost:8080/nxpetshopback</u></span></p>\n', 'READY', NULL, NULL, NULL, 7, 'admin', '2013-11-26 10:57:20', NULL, NULL, 839, 190),
(145, '<p>\n	All the items are correctly displayed.</p>\n', '<p>\n	<u><em>Interface Authorisation :</em></u></p>\n<p>\n	On the Authorisation screen, look for the following items :</p>\n<ol>\n	<li>\n		A banner with label <span>JPetStore Back Office</span></li>\n	<li>\n		A label <span>Please enter your login and password.</span></li>\n	<li>\n		A text box with label <span>Login :</span></li>\n	<li>\n		A text box with label <span>Password :</span></li>\n	<li>\n		A button <span>VALIDATE</span></li>\n</ol>\n', 'READY', NULL, NULL, NULL, 8, 'admin', '2013-11-26 10:57:20', NULL, NULL, 840, 191),
(146, '<p>\n	Both login and password are displayed.</p>\n', '<p>\n	<u><em>Login :</em></u></p>\n<ol>\n	<li>\n		Write into the Login text box : <span>Admin</span></li>\n	<li>\n		Write into the Password text box : <span>Admin</span></li>\n</ol>\n<p>\n	<u>Note :</u><em> this is login password is <span>valid</span>.</em></p>\n', 'READY', NULL, NULL, NULL, 10, 'admin', '2013-11-26 10:57:20', NULL, NULL, 841, 192),
(147, '<p>\n	The login and password are right<span>.</span></p>\n<p>\n	</p>\n<p>\n	The user enter the <span>Create user</span> screen.</p>\n', '<p>\n	<em><u>Validate (and navigation) :</u></em></p>\n<p>\n	Click on the button VALIDATE.</p>\n', 'READY', NULL, NULL, NULL, 11, 'admin', '2013-11-26 10:57:20', NULL, NULL, 842, 193),
(148, '', '<p>\n	test</p>\n', 'READY', NULL, NULL, NULL, 181, 'admin', '2013-11-26 10:58:24', NULL, NULL, 844, 195),
(149, '', '<p>\n	test</p>\n', 'READY', NULL, NULL, NULL, 182, 'admin', '2013-11-26 10:58:24', NULL, NULL, 845, 196),
(150, '<p>\n	-</p>\n', '<p>\n	<em><u>Pr requisite :</u></em></p>\n<ul>\n	<li>\n		Access to a browser : FF 4 or IE 7.</li>\n	<li>\n		A <span>valid login and password</span> for a <strong>Admin</strong> profile.</li>\n</ul>\n', 'READY', NULL, NULL, NULL, 6, 'admin', '2013-11-26 10:58:32', NULL, NULL, 847, 198),
(151, '<p>\n	The user is connected to <span><strong><u>NX Pet Shop Back Office</u></strong></span>.</p>\n<p>\n	</p>\n<p>\n	The screen Authorisation is displayed.</p>\n', '<p>\n	<em><u>Connection :</u></em></p>\n<p>\n	The user can connect to the web application at the following adress :</p>\n<p>\n	<span><u>http://localhost:8080/nxpetshopback</u></span></p>\n', 'READY', NULL, NULL, NULL, 7, 'admin', '2013-11-26 10:58:32', NULL, NULL, 848, 199),
(152, '<p>\n	All the items are correctly displayed.</p>\n', '<p>\n	<u><em>Interface Authorisation :</em></u></p>\n<p>\n	On the Authorisation screen, look for the following items :</p>\n<ol>\n	<li>\n		A banner with label <span>JPetStore Back Office</span></li>\n	<li>\n		A label <span>Please enter your login and password.</span></li>\n	<li>\n		A text box with label <span>Login :</span></li>\n	<li>\n		A text box with label <span>Password :</span></li>\n	<li>\n		A button <span>VALIDATE</span></li>\n</ol>\n', 'READY', NULL, NULL, NULL, 8, 'admin', '2013-11-26 10:58:32', NULL, NULL, 849, 200),
(153, '<p>\n	Both login and password are displayed.</p>\n', '<p>\n	<u><em>Login :</em></u></p>\n<ol>\n	<li>\n		Write into the Login text box : <span>Admin</span></li>\n	<li>\n		Write into the Password text box : <span>Admin</span></li>\n</ol>\n<p>\n	<u>Note :</u><em> this is login password is <span>valid</span>.</em></p>\n', 'READY', NULL, NULL, NULL, 10, 'admin', '2013-11-26 10:58:32', NULL, NULL, 850, 201),
(154, '<p>\n	The login and password are right<span>.</span></p>\n<p>\n	</p>\n<p>\n	The user enter the <span>Create user</span> screen.</p>\n', '<p>\n	<em><u>Validate (and navigation) :</u></em></p>\n<p>\n	Click on the button VALIDATE.</p>\n', 'READY', NULL, NULL, NULL, 11, 'admin', '2013-11-26 10:58:32', NULL, NULL, 851, 202),
(155, '', '<p>\n	test</p>\n', 'READY', NULL, NULL, NULL, 181, 'admin', '2013-11-26 11:00:12', NULL, NULL, 853, 204),
(156, '', '<p>\n	test</p>\n', 'READY', NULL, NULL, NULL, 182, 'admin', '2013-11-26 11:00:12', NULL, NULL, 854, 205),
(157, '', '<p>\n	test</p>\n', 'READY', NULL, NULL, NULL, 181, 'admin', '2013-11-26 12:15:08', NULL, NULL, 864, 207),
(158, '', '<p>\n	test</p>\n', 'READY', NULL, NULL, NULL, 182, 'admin', '2013-11-26 12:15:08', NULL, NULL, 865, 208);


INSERT INTO EXECUTION_EXECUTION_STEPS (EXECUTION_ID, EXECUTION_STEP_ID, EXECUTION_STEP_ORDER) VALUES
(46, 143, 0),
(46, 144, 1),
(46, 145, 2),
(46, 146, 3),
(46, 147, 4),
(47, 148, 0),
(47, 149, 1),
(48, 150, 0),
(48, 151, 1),
(48, 152, 2),
(48, 153, 3),
(48, 154, 4),
(49, 155, 0),
(49, 156, 1),
(50, 157, 0),
(50, 158, 1);





INSERT INTO CUSTOM_FIELD_BINDING (CFB_ID, CF_ID, BOUND_ENTITY, BOUND_PROJECT_ID, POSITION) VALUES
(8, 1, 'REQUIREMENT_VERSION', 6, 1),
(9, 2, 'REQUIREMENT_VERSION', 6, 2),
(10, 3, 'REQUIREMENT_VERSION', 6, 3),
(11, 4, 'REQUIREMENT_VERSION', 6, 4),
(12, 5, 'REQUIREMENT_VERSION', 6, 5),
(13, 1, 'TEST_CASE', 6, 1),
(14, 2, 'TEST_CASE', 6, 2),
(15, 3, 'TEST_CASE', 6, 3),
(16, 4, 'TEST_CASE', 6, 4),
(17, 5, 'TEST_CASE', 6, 5),
(18, 1, 'TEST_STEP', 6, 1),
(19, 2, 'TEST_STEP', 6, 2),
(20, 3, 'TEST_STEP', 6, 3),
(21, 4, 'TEST_STEP', 6, 4),
(22, 5, 'TEST_STEP', 6, 5),
(23, 3, 'CAMPAIGN', 6, 1),
(24, 4, 'CAMPAIGN', 6, 2),
(25, 5, 'CAMPAIGN', 6, 3),
(26, 2, 'TEST_SUITE', 6, 1),
(27, 3, 'TEST_SUITE', 6, 2),
(28, 2, 'ITERATION', 6, 1),
(29, 4, 'ITERATION', 6, 2);

INSERT INTO CUSTOM_FIELD_RENDERING_LOCATION (CFB_ID, RENDERING_LOCATION) VALUES
(22, 'STEP_TABLE');

INSERT INTO CUSTOM_FIELD_VALUE (CFV_ID, BOUND_ENTITY_ID, BOUND_ENTITY_TYPE, CFB_ID, VALUE) VALUES
(15700, 8, 'TEST_STEP', 7, 'success !'),
(15800, 50, 'TEST_STEP', 7, 'defaultt1'),
(15900, 49, 'TEST_STEP', 7, 'defaultt1'),
(16000, 48, 'TEST_STEP', 7, 'defaultt1'),
(16100, 39, 'TEST_STEP', 7, 'defaultt1'),
(16200, 34, 'TEST_STEP', 7, 'defaultt1'),
(16300, 4, 'TEST_STEP', 7, 'defaultt1'),
(16400, 11, 'TEST_STEP', 7, 'defaultt1'),
(16500, 10, 'TEST_STEP', 7, 'defaultt1'),
(16600, 7, 'TEST_STEP', 7, 'defaultt1'),
(16700, 6, 'TEST_STEP', 7, 'defaultt1'),
(16800, 186, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(16900, 187, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17000, 189, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17100, 188, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17200, 190, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17300, 194, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17400, 193, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17500, 192, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17600, 200, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17700, 199, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17800, 198, 'REQUIREMENT_VERSION', 8, 'defaultt1'),
(17900, 186, 'REQUIREMENT_VERSION', 9, ''),
(18000, 187, 'REQUIREMENT_VERSION', 9, ''),
(18100, 189, 'REQUIREMENT_VERSION', 9, ''),
(18200, 188, 'REQUIREMENT_VERSION', 9, ''),
(18300, 190, 'REQUIREMENT_VERSION', 9, ''),
(18400, 194, 'REQUIREMENT_VERSION', 9, ''),
(18500, 193, 'REQUIREMENT_VERSION', 9, ''),
(18600, 192, 'REQUIREMENT_VERSION', 9, ''),
(18700, 200, 'REQUIREMENT_VERSION', 9, ''),
(18800, 199, 'REQUIREMENT_VERSION', 9, ''),
(18900, 198, 'REQUIREMENT_VERSION', 9, ''),
(19000, 186, 'REQUIREMENT_VERSION', 10, 'opt11'),
(19100, 187, 'REQUIREMENT_VERSION', 10, 'opt11'),
(19200, 189, 'REQUIREMENT_VERSION', 10, 'opt11'),
(19300, 188, 'REQUIREMENT_VERSION', 10, 'opt11'),
(19400, 190, 'REQUIREMENT_VERSION', 10, 'opt11'),
(19500, 194, 'REQUIREMENT_VERSION', 10, 'opt11'),
(19600, 193, 'REQUIREMENT_VERSION', 10, 'opt11'),
(19700, 192, 'REQUIREMENT_VERSION', 10, 'opt11'),
(19800, 200, 'REQUIREMENT_VERSION', 10, 'opt11'),
(19900, 199, 'REQUIREMENT_VERSION', 10, 'opt11'),
(20000, 198, 'REQUIREMENT_VERSION', 10, 'opt11'),
(20100, 186, 'REQUIREMENT_VERSION', 11, ''),
(20200, 187, 'REQUIREMENT_VERSION', 11, ''),
(20300, 189, 'REQUIREMENT_VERSION', 11, ''),
(20400, 188, 'REQUIREMENT_VERSION', 11, ''),
(20500, 190, 'REQUIREMENT_VERSION', 11, ''),
(20600, 194, 'REQUIREMENT_VERSION', 11, ''),
(20700, 193, 'REQUIREMENT_VERSION', 11, ''),
(20800, 192, 'REQUIREMENT_VERSION', 11, ''),
(20900, 200, 'REQUIREMENT_VERSION', 11, ''),
(21000, 199, 'REQUIREMENT_VERSION', 11, ''),
(21100, 198, 'REQUIREMENT_VERSION', 11, ''),
(21200, 186, 'REQUIREMENT_VERSION', 12, 'false'),
(21300, 187, 'REQUIREMENT_VERSION', 12, 'false'),
(21400, 189, 'REQUIREMENT_VERSION', 12, 'false'),
(21500, 188, 'REQUIREMENT_VERSION', 12, 'false'),
(21600, 190, 'REQUIREMENT_VERSION', 12, 'false'),
(21700, 194, 'REQUIREMENT_VERSION', 12, 'false'),
(21800, 193, 'REQUIREMENT_VERSION', 12, 'false'),
(21900, 192, 'REQUIREMENT_VERSION', 12, 'false'),
(22000, 200, 'REQUIREMENT_VERSION', 12, 'false'),
(22100, 199, 'REQUIREMENT_VERSION', 12, 'false'),
(22200, 198, 'REQUIREMENT_VERSION', 12, 'false'),
(22300, 220, 'TEST_CASE', 13, 'defaultt1'),
(22400, 225, 'TEST_CASE', 13, 'defaultt1'),
(22500, 220, 'TEST_CASE', 14, ''),
(22600, 225, 'TEST_CASE', 14, ''),
(22700, 220, 'TEST_CASE', 15, 'opt11'),
(22800, 225, 'TEST_CASE', 15, 'opt11'),
(22900, 220, 'TEST_CASE', 16, ''),
(23000, 225, 'TEST_CASE', 16, ''),
(23100, 220, 'TEST_CASE', 17, 'false'),
(23200, 225, 'TEST_CASE', 17, 'false'),
(23300, 181, 'TEST_STEP', 18, 'defaultt1'),
(23400, 182, 'TEST_STEP', 18, 'defaultt1'),
(23500, 185, 'TEST_STEP', 18, 'defaultt1'),
(23600, 186, 'TEST_STEP', 18, 'defaultt1'),
(23700, 181, 'TEST_STEP', 19, ''),
(23800, 182, 'TEST_STEP', 19, ''),
(23900, 185, 'TEST_STEP', 19, ''),
(24000, 186, 'TEST_STEP', 19, ''),
(24100, 181, 'TEST_STEP', 20, 'opt11'),
(24200, 182, 'TEST_STEP', 20, 'opt11'),
(24300, 185, 'TEST_STEP', 20, 'opt11'),
(24400, 186, 'TEST_STEP', 20, 'opt11'),
(24500, 181, 'TEST_STEP', 21, ''),
(24600, 182, 'TEST_STEP', 21, ''),
(24700, 185, 'TEST_STEP', 21, ''),
(24800, 186, 'TEST_STEP', 21, ''),
(24900, 181, 'TEST_STEP', 22, 'false'),
(25000, 182, 'TEST_STEP', 22, 'false'),
(25100, 185, 'TEST_STEP', 22, 'false'),
(25200, 186, 'TEST_STEP', 22, 'false'),
(25300, 17, 'CAMPAIGN', 23, 'opt11'),
(25400, 20, 'CAMPAIGN', 23, 'opt11'),
(25500, 17, 'CAMPAIGN', 24, ''),
(25600, 20, 'CAMPAIGN', 24, ''),
(25700, 17, 'CAMPAIGN', 25, 'false'),
(25800, 20, 'CAMPAIGN', 25, 'false'),
(25900, 6, 'TEST_SUITE', 26, ''),
(26000, 7, 'TEST_SUITE', 26, ''),
(26100, 8, 'TEST_SUITE', 26, ''),
(26200, 9, 'TEST_SUITE', 26, ''),
(26300, 6, 'TEST_SUITE', 27, 'opt11'),
(26400, 7, 'TEST_SUITE', 27, 'opt11'),
(26500, 8, 'TEST_SUITE', 27, 'opt11'),
(26600, 9, 'TEST_SUITE', 27, 'opt11'),
(26700, 15, 'ITERATION', 28, ''),
(26800, 16, 'ITERATION', 28, ''),
(26900, 15, 'ITERATION', 29, ''),
(27000, 16, 'ITERATION', 29, '');


INSERT INTO DENORMALIZED_FIELD_VALUE (DFV_ID, CFV_ID, CODE, DENORMALIZED_FIELD_HOLDER_ID, DENORMALIZED_FIELD_HOLDER_TYPE, INPUT_TYPE, LABEL, POSITION, VALUE) VALUES
(1, 16700, 't1', 143, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'defaultt1'),
(2, 16600, 't1', 144, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'defaultt1'),
(3, 15700, 't1', 145, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'success !'),
(4, 16500, 't1', 146, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'defaultt1'),
(5, 16400, 't1', 147, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'defaultt1'),
(6, 16700, 't1', 150, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'defaultt1'),
(7, 16600, 't1', 151, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'defaultt1'),
(8, 15700, 't1', 152, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'success !'),
(9, 16500, 't1', 153, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'defaultt1'),
(10, 16400, 't1', 154, 'EXECUTION_STEP', 'PLAIN_TEXT', 'cuf-Text1', 1, 'defaultt1');
