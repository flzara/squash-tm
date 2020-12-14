-- test set for audit data creation while migrating pre-SQUASH-794 automated suites --
INSERT INTO AUTOMATED_SUITE (SUITE_ID) VALUES
('1234'),
('5678'),
('2468'),
('1357'),
('1111'),
('2222');

Insert into EXECUTION(EXECUTION_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, NAME, PREREQUISITE, EXECUTION_MODE, EXECUTION_STATUS, LAST_EXECUTED_BY, LAST_EXECUTED_ON) values
 (84, 'User-1', '2011-09-29 10:37:26.0', 'User-1', '2011-09-29 10:37:37.0', 'Test-Case 1', '', 'AUTOMATED', 'SUCCESS', 'User-1', '2011-09-29 10:37:37.0'),
 (85, 'User-1', '2011-09-29 10:37:26.0', 'User-1', '2011-09-29 10:37:37.0', 'Test-Case 1', '', 'AUTOMATED', 'READY', 'User-1', '2011-09-29 10:37:37.0'),
 (86, 'User-1', '2011-09-29 10:37:26.0', 'User-1', '2011-09-29 10:37:37.0', 'Test-Case 1', '', 'AUTOMATED', 'SUCCESS', 'User-1', '2011-09-29 10:37:37.0'),
 (87, 'User-2', '2011-09-30 10:37:26.0', 'User-2', '2011-09-30 10:37:37.0', 'Test-Case 1', '', 'AUTOMATED', 'FAILURE', 'User-2', '2011-09-30 10:37:37.0'),
 (88, 'User-2', '2011-09-30 10:37:26.0', 'User-2', '2011-09-30 10:37:37.0', 'Test-Case 1', '', 'AUTOMATED', 'BLOCKED', 'User-2', '2011-09-30 10:37:37.0'),
 (89, 'User-2', '2011-09-30 10:37:26.0', 'User-2', '2011-09-30 10:37:37.0', 'Test-Case 1', '', 'AUTOMATED', 'NOT_FOUND', 'User-2', '2011-09-30 10:37:37.0'),
 (90, 'User-2', '2011-09-30 10:37:26.0', 'User-2', '2011-09-30 10:37:37.0', 'Test-Case 1', '', 'AUTOMATED', 'UNTESTABLE', 'User-2', '2011-09-30 10:37:37.0'),
 (91, 'User-2', '2011-09-30 10:37:26.0', 'User-2', '2011-09-30 10:37:37.0', 'Test-Case 1', '', 'AUTOMATED', 'SUCCESS', 'User-2', '2011-09-30 10:37:37.0');

INSERT INTO AUTOMATED_EXECUTION_EXTENDER (EXTENDER_ID, MASTER_EXECUTION_ID, SUITE_ID) VALUES
(-1, 84, '1234'),
(-2, 85, '1234'),
(-3, 86, '1234'),
(-4, 87, '5678'),
(-5, 88, '2468'),
(-6, 89, '1357'),
(-7, 90, '1357'),
(-8, 91, '1111');
