
-- Issue 3860 when copy past test step , the copied custom field values were added to the source step and not the copy one : 

-- steps with too much custom field values
INSERT INTO CUSTOM_FIELD_VALUE (CFV_ID, BOUND_ENTITY_ID, BOUND_ENTITY_TYPE, CFB_ID, VALUE)
VALUES (168,8,'TEST_STEP',7,'super'),
(169,8,'TEST_STEP',7,'super'),
(170,4,'TEST_STEP',7,'super');

-- steps missing custom field values
INSERT INTO ATTACHMENT_LIST (ATTACHMENT_LIST_ID)
VALUES(801),
(802);

INSERT INTO TEST_STEP (TEST_STEP_ID)
VALUES (204),
(205);

INSERT INTO ACTION_TEST_STEP (TEST_STEP_ID, ACTION, EXPECTED_RESULT, ATTACHMENT_LIST_ID)
VALUES ( 204, 'action', 'expected', 801),
 ( 205, 'action', 'expected', 802);
 
 INSERT INTO TEST_CASE_STEPS (TEST_CASE_ID, STEP_ID, STEP_ORDER)
 VALUES (183,204,5),
 (183,205,5);
 
 