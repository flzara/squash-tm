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