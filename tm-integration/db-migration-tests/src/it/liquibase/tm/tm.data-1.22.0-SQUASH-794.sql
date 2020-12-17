--
--     This file is part of the Squashtest platform.
--     Copyright (C) Henix, henix.fr
--
--     See the NOTICE file distributed with this work for additional
--     information regarding copyright ownership.
--
--     This is free software: you can redistribute it and/or modify
--     it under the terms of the GNU Lesser General Public License as published by
--     the Free Software Foundation, either version 3 of the License, or
--     (at your option) any later version.
--
--     this software is distributed in the hope that it will be useful,
--     but WITHOUT ANY WARRANTY; without even the implied warranty of
--     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--     GNU Lesser General Public License for more details.
--
--     You should have received a copy of the GNU Lesser General Public License
--     along with this software.  If not, see <http://www.gnu.org/licenses/>.
--

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
