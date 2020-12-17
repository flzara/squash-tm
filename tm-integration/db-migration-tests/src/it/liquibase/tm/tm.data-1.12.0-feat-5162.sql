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

--
--
-- Feat 5162
--
-- Iterations now have a reference. Iterations created prior version 1.13.0 will now have
-- as a default reference the iteration order.
--
--

Insert into ATTACHMENT_LIST(ATTACHMENT_LIST_ID) values (9000);

insert into CAMPAIGN_LIBRARY_NODE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID) values
('feat-5162-campaign', 'liquibase', '2015-08-21', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into CAMPAIGN(CLN_ID,ACTUAL_END_AUTO, ACTUAL_START_AUTO) values ((select max(cln_id) from CAMPAIGN_LIBRARY_NODE), true, true);



insert into ITERATION(NAME, CREATED_BY, CREATED_ON, ACTUAL_END_AUTO, ACTUAL_START_AUTO) values
('feat-5162-iteration-1', 'liquibase', '2015-08-21', true, true);
insert into CAMPAIGN_ITERATION(CAMPAIGN_ID, ITERATION_ID, ITERATION_ORDER) values
((select max(CLN_ID) from CAMPAIGN), (select max(ITERATION_ID) from ITERATION), 0);

insert into ITERATION(NAME, CREATED_BY, CREATED_ON, ACTUAL_END_AUTO, ACTUAL_START_AUTO) values
('feat-5162-iteration-2', 'liquibase', '2015-08-21', true, true);
insert into CAMPAIGN_ITERATION(CAMPAIGN_ID, ITERATION_ID, ITERATION_ORDER) values
((select max(CLN_ID) from CAMPAIGN), (select max(ITERATION_ID) from ITERATION), 1);

insert into ITERATION(NAME, CREATED_BY, CREATED_ON, ACTUAL_END_AUTO, ACTUAL_START_AUTO) values
('feat-5162-iteration-3', 'liquibase', '2015-08-21', true, true);
insert into CAMPAIGN_ITERATION(CAMPAIGN_ID, ITERATION_ID, ITERATION_ORDER) values
((select max(CLN_ID) from CAMPAIGN), (select max(ITERATION_ID) from ITERATION), 2);
