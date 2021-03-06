

-- Feature 7183 when ordered lists have duplicate indices for order column

INSERT INTO ITERATION (ITERATION_ID, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON,
ACTUAL_END_AUTO, ACTUAL_START_AUTO, ATTACHMENT_LIST_ID, ITERATION_STATUS) VALUES
(-17, '', 'iteration-3', 'admin', '2013-11-26 10:55:27', 'admin', '2013-11-26 12:14:53', false, false, 907, 'UNDEFINED');

INSERT INTO CAMPAIGN_ITERATION (CAMPAIGN_ID, ITERATION_ID, ITERATION_ORDER) VALUES
(-20, -17, 1);

INSERT INTO TEST_SUITE (ID, NAME, DESCRIPTION, ATTACHMENT_LIST_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON) VALUES
(-10, 'suite 5', NULL, 907, 'admin', '2013-11-26 11:02:20', 'admin', '2013-11-26 11:02:20'),
(-11, 'suite 6', NULL, 907, 'admin', '2013-11-26 11:02:37', 'admin', '2013-11-26 11:02:37'),
(-12, 'suite 7', NULL, 907, 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53'),
(-13, 'suite 8', NULL, 907, 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53'),
(-14, 'suite 9', NULL, 907, 'admin', '2013-11-26 11:02:20', 'admin', '2013-11-26 11:02:20'),
(-15, 'suite 10', NULL, 907, 'admin', '2013-11-26 11:02:37', 'admin', '2013-11-26 11:02:37'),
(-16, 'suite 11', NULL, 907, 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53'),
(-17, 'suite 12', NULL, 907, 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53'),
(-18, 'suite 13', NULL, 907, 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:53');

INSERT INTO ITERATION_TEST_SUITE (ITERATION_ID, TEST_SUITE_ID) VALUES
(-16, -10),
(-16, -11),
(-16, -12),
(-16, -13),
(-17, -14),
(-17, -15),
(-17, -16),
(-17, -17),
(-17, -18);

