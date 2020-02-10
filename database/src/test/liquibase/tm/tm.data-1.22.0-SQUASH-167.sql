-- test set for UUID creation while migrating pre-SQUASH-167 iterations --
-- (column UUID will have no value in old iterations, but we want to add a NOT NULL constraint) --
INSERT INTO ATTACHMENT_LIST (ATTACHMENT_LIST_ID) VALUES
(910);

INSERT INTO ITERATION (ITERATION_ID, DESCRIPTION, NAME, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON,
ACTUAL_END_AUTO, ACTUAL_START_AUTO, ATTACHMENT_LIST_ID, ITERATION_STATUS) VALUES
(-18, 'iteration-testset-SQUASH-167', 'iteration-3', 'admin', '2013-11-26 10:55:27', 'admin', '2013-11-26 12:14:53', false, false, 910, 'UNDEFINED'),
(-19, 'iteration-testset-SQUASH-167', 'iteration-4', 'admin', '2013-11-26 12:14:53', 'admin', '2013-11-26 12:14:55', false, false, 910, 'UNDEFINED');