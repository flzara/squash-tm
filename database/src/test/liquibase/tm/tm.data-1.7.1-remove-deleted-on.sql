/*
Create table to store ids of entities to delete
*/

create table ENTITIES_TO_DELETE
(
ENTITY_NAME VARCHAR(50),
ENTITY_ID BIGINT
)
;

/* --------------------------------------------*/
/* Save ids of entities with delete on not null */
/* ----------------------------------------------*/
insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'CLN' , CLN_ID
from CAMPAIGN_LIBRARY_NODE
where DELETED_ON is not null
;

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'TCLN' , TCLN_ID
from TEST_CASE_LIBRARY_NODE
where DELETED_ON is not null
;

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'RLN' , RLN_ID
from REQUIREMENT_LIBRARY_NODE
where DELETED_ON is not null
;

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ITERATION' , ITERATION_ID
from ITERATION
where DELETED_ON is not null
;

/* Add iterations from deleted campaign */
insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ITERATION', CI.ITERATION_ID
from CAMPAIGN_ITERATION CI 
where CI.CAMPAIGN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'CLN')
and CI.ITERATION_ID not in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ITERATION')
;

/* -----------------------------------*/
/* Store ids of sub entities to delete */
/* -----------------------------------*/

/* --------------- */
/* Iteration block */
/* --------------- */

/* ITERATIONS ATTACHMENT_LIST*/
insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ATTACHMENT_LIST', ITER.ATTACHMENT_LIST_ID
from ITERATION ITER, ENTITIES_TO_DELETE ETD
where ETD.ENTITY_NAME = 'ITERATION'
and ETD.ENTITY_ID = ITER.ITERATION_ID
;

/* ITERATION_TEST_PLAN_ITEM */
insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ITPI', ITPL.ITEM_TEST_PLAN_ID
from ITEM_TEST_PLAN_LIST ITPL, ENTITIES_TO_DELETE ETD
where ETD.ENTITY_NAME = 'ITERATION'
and ETD.ENTITY_ID = ITPL.ITERATION_ID
;

/* EXECUTION */
insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'EXECUTION', ITPE.EXECUTION_ID
from ITEM_TEST_PLAN_EXECUTION ITPE , ENTITIES_TO_DELETE ETD
where ITPE.ITEM_TEST_PLAN_ID = ETD.ENTITY_ID
and ETD.ENTITY_NAME = 'ITPI'
;


insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ISSUE_LIST', E.ISSUE_LIST_ID
from EXECUTION E,  ENTITIES_TO_DELETE ETD
where E.EXECUTION_ID = ETD.ENTITY_ID
and ETD.ENTITY_NAME = 'EXECUTION'
;			

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ATTACHMENT_LIST', E.ATTACHMENT_LIST_ID
from EXECUTION E,  ENTITIES_TO_DELETE ETD
where E.EXECUTION_ID = ETD.ENTITY_ID
and ETD.ENTITY_NAME = 'EXECUTION'
;

/* EXECUTION_STEP */

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'EXECUTION_STEP', EES.EXECUTION_STEP_ID
from  EXECUTION_EXECUTION_STEPS EES, ENTITIES_TO_DELETE ETD
where EES.EXECUTION_ID =  ETD.ENTITY_ID 
and  ETD.ENTITY_NAME = 'EXECUTION'
;

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ISSUE_LIST', ES.ISSUE_LIST_ID
from  EXECUTION_STEP ES,  ENTITIES_TO_DELETE ETD
where ES.EXECUTION_STEP_ID = ETD.ENTITY_ID
and	ETD.ENTITY_NAME = 'EXECUTION_STEP'
;

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ATTACHMENT_LIST', ES.ATTACHMENT_LIST_ID
from  EXECUTION_STEP ES,  ENTITIES_TO_DELETE ETD
where ES.EXECUTION_STEP_ID = ETD.ENTITY_ID
and	ETD.ENTITY_NAME = 'EXECUTION_STEP'
;

/* --------------- */
/* Campaign block */
/* --------------- */


insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ATTACHMENT_LIST', CLN.ATTACHMENT_LIST_ID
from CAMPAIGN_LIBRARY_NODE CLN, ENTITIES_TO_DELETE ETD
where CLN.CLN_ID = ETD.ENTITY_ID
and ETD.ENTITY_NAME = 'CLN'
;


/* ----------------- */
/* Test Cases block */
/* ----------------- */

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ATTACHMENT_LIST', TCLN.ATTACHMENT_LIST_ID
from TEST_CASE_LIBRARY_NODE TCLN , ENTITIES_TO_DELETE ETD
where TCLN.TCLN_ID = ETD.ENTITY_ID
and  ETD.ENTITY_NAME = 'TCLN'
;

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'TEST_STEP' , TCS.STEP_ID
from TEST_CASE_STEPS TCS, ENTITIES_TO_DELETE ETD
where TCS.TEST_CASE_ID = ETD.ENTITY_ID
and ETD.ENTITY_NAME = 'TCLN'
;

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ATTACHMENT_LIST', ATS.ATTACHMENT_LIST_ID
from ACTION_TEST_STEP ATS, ENTITIES_TO_DELETE ETD
where ATS.TEST_STEP_ID = ETD.ENTITY_ID
and ETD.ENTITY_NAME = 'TEST_STEP'
;

/* --------------- */
/* Attachments     */
/* --------------- */

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ATTACHMENT', ATTACH.ATTACHMENT_ID
from ATTACHMENT ATTACH, ENTITIES_TO_DELETE ETD
where ATTACH.ATTACHMENT_LIST_ID = ETD.ENTITY_ID
and ETD.ENTITY_NAME = 'ATTACHMENT_LIST'
;

insert into ENTITIES_TO_DELETE (ENTITY_NAME, ENTITY_ID)
select 'ATTACHMENT_CONTENT', ATTACH.CONTENT_ID
from ATTACHMENT ATTACH, ENTITIES_TO_DELETE ETD
where ATTACH.ATTACHMENT_ID = ETD.ENTITY_ID
and ETD.ENTITY_NAME = 'ATTACHMENT'
;

/* -----------------------------------*/
/* Delete entities in order           */
/* -----------------------------------*/

/* --------------- */
/* Iteration block */
/* --------------- */

delete from EXECUTION_EXECUTION_STEPS
where EXECUTION_STEP_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'EXECUTION_STEP')
;

delete from EXECUTION_STEP
where EXECUTION_STEP_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'EXECUTION_STEP')
;

delete from ITEM_TEST_PLAN_EXECUTION
where ITEM_TEST_PLAN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ITPI')
;

delete from EXECUTION
where EXECUTION_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'EXECUTION')
;

delete from ISSUE_LIST
where ISSUE_LIST_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME ='ISSUE_LIST')
;

delete from ITEM_TEST_PLAN_LIST
where ITERATION_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ITERATION')
;

delete from ITERATION_TEST_PLAN_ITEM
where ITEM_TEST_PLAN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ITPI')
;

delete from CAMPAIGN_ITERATION
where ITERATION_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ITERATION')
;

delete from ITERATION
where ITERATION_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ITERATION')
;

/* --------------- */
/* Campaign block */
/* --------------- */

delete from CAMPAIGN_TEST_PLAN_ITEM 
where CAMPAIGN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'CLN')
;

delete from CAMPAIGN_LIBRARY_CONTENT
where CONTENT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'CLN')
;

delete from CLN_RELATIONSHIP_CLOSURE
where DESCENDANT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'CLN')
;

delete from CLN_RELATIONSHIP
where DESCENDANT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'CLN')
;

delete from CAMPAIGN_FOLDER
where CLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'CLN')
;

delete from CAMPAIGN 
where CLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'CLN')
;

delete from CAMPAIGN_LIBRARY_NODE
where CLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'CLN')
;
/* ----------------- */
/* Requirement Block */
/* ----------------- */


update REQUIREMENT_FOLDER
set RES_ID = null
where RLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'RLN')
;

delete from REQUIREMENT
where RLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'RLN')
;

delete from REQUIREMENT_LIBRARY_CONTENT
where CONTENT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'RLN')
;

delete from RLN_RELATIONSHIP_CLOSURE
where DESCENDANT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'RLN')
;

delete from RLN_RELATIONSHIP
where DESCENDANT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'RLN')
;

delete from REQUIREMENT_FOLDER
where RLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'RLN')
;

delete from REQUIREMENT_LIBRARY_NODE
where RLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'RLN')
;

/* ------------------ */
/* Test Cases block   */
/* ------------------ */

delete from ACTION_TEST_STEP
where TEST_STEP_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TEST_STEP')
;

delete from CALL_TEST_STEP
where TEST_STEP_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TEST_STEP')
;

delete from CALL_TEST_STEP
where CALLED_TEST_CASE_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

update EXECUTION_STEP
set EXECUTION_STEP_ID = null
where EXECUTION_STEP_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TEST_STEP')
;

delete from TEST_CASE_STEPS
where TEST_CASE_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

delete from TEST_STEP
where TEST_STEP_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TEST_STEP')
;

update EXECUTION
set TCLN_ID = null
where TCLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

update CAMPAIGN_TEST_PLAN_ITEM
set TEST_CASE_ID = null
where TEST_CASE_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

update ITERATION_TEST_PLAN_ITEM
set TCLN_ID = null
where TCLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

delete from TEST_CASE
where TCLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

delete from TCLN_RELATIONSHIP
where DESCENDANT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

delete from TEST_CASE_FOLDER
where TCLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

delete from TEST_CASE_LIBRARY_CONTENT
where CONTENT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

delete from TCLN_RELATIONSHIP_CLOSURE
where DESCENDANT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;

delete from TEST_CASE_LIBRARY_NODE
where TCLN_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
;


/* --------------- */
/* Custom Fields   */
/* --------------- */
delete from CUSTOM_FIELD_VALUE
where BOUND_ENTITY_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'CLN')
and BOUND_ENTITY_TYPE = 'CAMPAIGN'
;

delete from CUSTOM_FIELD_VALUE
where BOUND_ENTITY_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'REQUIREMENT_VERSION')
and BOUND_ENTITY_TYPE = 'REQUIREMENT_VERSION'
;

delete from CUSTOM_FIELD_VALUE
where BOUND_ENTITY_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ITERATION')
and BOUND_ENTITY_TYPE = 'ITERATION'
;

delete from CUSTOM_FIELD_VALUE
where BOUND_ENTITY_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TCLN')
and BOUND_ENTITY_TYPE = 'TEST_CASE'
;

delete from CUSTOM_FIELD_VALUE
where BOUND_ENTITY_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'TEST_STEP')
and BOUND_ENTITY_TYPE = 'TEST_STEP'
;


/* --------------- */
/* Attachments     */
/* --------------- */

delete from ATTACHMENT
where ATTACHMENT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ATTACHMENT' )
;
		
delete from ATTACHMENT_CONTENT
where ATTACHMENT_CONTENT_ID in (select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ATTACHMENT_CONTENT' )
;

delete from ATTACHMENT_LIST
where ATTACHMENT_LIST_ID in ( select ENTITY_ID from ENTITIES_TO_DELETE where ENTITY_NAME = 'ATTACHMENT_LIST')
;


/*
Drop table that stored the ids of entities to delete
*/
DROP TABLE ENTITIES_TO_DELETE
;




