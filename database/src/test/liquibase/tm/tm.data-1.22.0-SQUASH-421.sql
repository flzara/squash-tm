-- test set for UUID creation while migrating pre-SQUASH-167 iterations --
-- (column UUID will have no value in old iterations, but we want to add a NOT NULL constraint) --
INSERT INTO ATTACHMENT_LIST (ATTACHMENT_LIST_ID) VALUES
(911);

INSERT INTO TEST_SUITE (ID, NAME, DESCRIPTION, ATTACHMENT_LIST_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON) VALUES
(-19, 'suite-testset-SQUASH-241', '', 911, 'admin', '2013-04-16 11:48:23', 'admin', '2013-04-16 11:51:32'),
(-20, 'suite-testset-SQUASH-241', '', 911, 'admin', '2013-04-16 11:48:23', 'admin', '2013-04-16 11:51:32'),;
