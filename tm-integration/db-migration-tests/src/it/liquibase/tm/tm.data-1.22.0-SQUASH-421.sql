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

-- test set for UUID creation while migrating pre-SQUASH-167 iterations --
-- (column UUID will have no value in old iterations, but we want to add a NOT NULL constraint) --
INSERT INTO ATTACHMENT_LIST (ATTACHMENT_LIST_ID) VALUES
(911);

INSERT INTO TEST_SUITE (ID, NAME, DESCRIPTION, ATTACHMENT_LIST_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON) VALUES
(-19, 'suite-testset-SQUASH-241', '', 911, 'admin', '2013-04-16 11:48:23', 'admin', '2013-04-16 11:51:32'),
(-20, 'suite-testset-SQUASH-241', '', 911, 'admin', '2013-04-16 11:48:23', 'admin', '2013-04-16 11:51:32');
