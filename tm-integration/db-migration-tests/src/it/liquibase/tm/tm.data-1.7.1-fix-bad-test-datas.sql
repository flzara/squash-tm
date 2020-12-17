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

/* let's fix the sample database */

/*fix test-steps attached to different test-cases. */
create table TEST_CASE_STEPS2 (
ID bigint NOT NULL
);

insert into TEST_CASE_STEPS2
(ID)
select max(tcs1.TEST_CASE_ID)
from  TEST_CASE_STEPS tcs1, TEST_CASE_STEPS tcs2
where tcs1.STEP_ID = tcs2.STEP_ID
and tcs1.TEST_CASE_ID != tcs2.TEST_CASE_ID
group by tcs1.STEP_ID
;

delete from TEST_CASE_STEPS
where TEST_CASE_ID in (select ID from TEST_CASE_STEPS2)
;

delete from TEST_CASE_STEPS
where TEST_CASE_ID = 203
;

drop table TEST_CASE_STEPS2
;
/* delete orphan test steps*/
update EXECUTION_STEP
set TEST_STEP_ID = null
where TEST_STEP_ID in (1, 139, 141, 131, 140);

delete from ACTION_TEST_STEP
where TEST_STEP_ID in (1, 139, 141, 131, 140);

delete from TEST_STEP
where TEST_STEP_ID in (1, 139, 141, 131, 140);


/* delete orphan test plan item*/

delete from EXECUTION_EXECUTION_STEPS
where EXECUTION_ID = 1;
delete from EXECUTION_STEP
where EXECUTION_STEP_ID = 1;
delete from ITEM_TEST_PLAN_EXECUTION
where ITEM_TEST_PLAN_ID = 1;
delete from EXECUTION
where EXECUTION_ID=1;
delete from ITERATION_TEST_PLAN_ITEM
where ITEM_TEST_PLAN_ID = 1;

delete from ISSUE_LIST
where ISSUE_LIST_ID in (1,79)
;

/* delete unwanted orphan attachment lists*/
delete from ATTACHMENT_LIST
where ATTACHMENT_LIST_ID in (3,352,6,7)
;
