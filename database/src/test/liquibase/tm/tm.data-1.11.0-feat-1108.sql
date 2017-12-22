--
--
-- Feat 1108
--
-- The natures and types of test cases, as well as the categories for exigences 
-- are now stored in separate tables. Until now the value for those attributes 
-- were inlined as string in the tables.
-- 
-- When updating the database schema from 1.11 to 1.12 the data will be migrated 
-- accordingly. This dataset helps testing that the migration went well. It 
-- simply consists of inserting testcases and categories of all natures/types/
-- categories.
--
--


-- phase 1 : creating the test cases

insert into TEST_CASE_LIBRARY_NODE(DESCRIPTION, CREATED_BY, CREATED_ON, 
LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, NAME) values 
('', 'liquibase', '2014-12-03', null, null, (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST),'feat-1108-undefined-undefined');

insert into TEST_CASE (TCLN_ID, VERSION, PREREQUISITE, TC_NATURE, TC_TYPE)
values((select max(TCLN_ID) from TEST_CASE_LIBRARY_NODE), 1, '', 'UNDEFINED', 'UNDEFINED');

 
insert into TEST_CASE_LIBRARY_NODE(DESCRIPTION, CREATED_BY, CREATED_ON, 
LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, NAME) values 
('', 'liquibase', '2014-12-03', null, null, (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST), 'feat-1108-functionaltesting-compliancetesting');

insert into TEST_CASE (TCLN_ID, VERSION, PREREQUISITE, TC_NATURE, TC_TYPE)
values((select max(TCLN_ID) from TEST_CASE_LIBRARY_NODE), 1, '', 'FUNCTIONAL_TESTING', 'COMPLIANCE_TESTING');

insert into TEST_CASE_LIBRARY_NODE(DESCRIPTION, CREATED_BY, CREATED_ON, 
LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, NAME) values 
('', 'liquibase', '2014-12-03', null, null, (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST), 'feat-1108-businesstesting-correctiontesting');

insert into TEST_CASE (TCLN_ID, VERSION, PREREQUISITE, TC_NATURE, TC_TYPE)
values((select max(TCLN_ID) from TEST_CASE_LIBRARY_NODE), 1, '', 'BUSINESS_TESTING', 'CORRECTION_TESTING');

insert into TEST_CASE_LIBRARY_NODE(DESCRIPTION, CREATED_BY, CREATED_ON, 
LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, NAME) values 
('', 'liquibase', '2014-12-03', null, null, (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST), 'feat-1108-usertesting-evolutiontesting');

insert into TEST_CASE (TCLN_ID, VERSION, PREREQUISITE, TC_NATURE, TC_TYPE)
values((select max(TCLN_ID) from TEST_CASE_LIBRARY_NODE), 1, '', 'USER_TESTING', 'EVOLUTION_TESTING');

insert into TEST_CASE_LIBRARY_NODE(DESCRIPTION, CREATED_BY, CREATED_ON, 
LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, NAME) values 
('', 'liquibase', '2014-12-03', null, null, (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST), 'feat-1108-nonfunctionaltesting-regressiontesting');

insert into TEST_CASE (TCLN_ID, VERSION, PREREQUISITE, TC_NATURE, TC_TYPE)
values((select max(TCLN_ID) from TEST_CASE_LIBRARY_NODE), 1, '', 'NON_FUNCTIONAL_TESTING', 'REGRESSION_TESTING');

insert into TEST_CASE_LIBRARY_NODE(DESCRIPTION, CREATED_BY, CREATED_ON, 
LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, NAME) values 
('', 'liquibase', '2014-12-03', null, null, (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST), 'feat-1108-performancetesting-endtoendtesting');

insert into TEST_CASE (TCLN_ID, VERSION, PREREQUISITE, TC_NATURE, TC_TYPE)
values((select max(TCLN_ID) from TEST_CASE_LIBRARY_NODE), 1, '', 'PERFORMANCE_TESTING', 'END_TO_END_TESTING');

insert into TEST_CASE_LIBRARY_NODE(DESCRIPTION, CREATED_BY, CREATED_ON, 
LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, NAME) values 
('', 'liquibase', '2014-12-03', null, null, (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST), 'feat-1108-securitytesting-partnertesting');

insert into TEST_CASE (TCLN_ID, VERSION, PREREQUISITE, TC_NATURE, TC_TYPE)
values((select max(TCLN_ID) from TEST_CASE_LIBRARY_NODE), 1, '', 'SECURITY_TESTING', 'PARTNER_TESTING');


-- the last insert uses a type already tested because the possibilities are exhausted.

insert into TEST_CASE_LIBRARY_NODE(DESCRIPTION, CREATED_BY, CREATED_ON, 
LAST_MODIFIED_BY, LAST_MODIFIED_ON, ATTACHMENT_LIST_ID, NAME) values 
('', 'liquibase', '2014-12-03', null, null, (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST), 'feat-1108-atdd-undefined');

insert into TEST_CASE (TCLN_ID, VERSION, PREREQUISITE, TC_NATURE, TC_TYPE)
values((select max(TCLN_ID) from TEST_CASE_LIBRARY_NODE), 1, '', 'ATDD', 'UNDEFINED');




-- phase 2 : creating the requirements
-- instead of creating multiple requirements we'll just create multiple versions of that requirement

-- the requirement
insert into REQUIREMENT_LIBRARY_NODE(CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, PROJECT_ID)
values ('liquibase', '2014-12-03', null, null, 1);

insert into REQUIREMENT(RLN_ID) values ((select max(RLN_ID) from REQUIREMENT_LIBRARY_NODE));


-- the versions
insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-functional', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 1, 'FUNCTIONAL');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-nonfunctional', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 2, 'NON_FUNCTIONAL');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-usecase', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 3, 'USE_CASE');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-business', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 4, 'BUSINESS');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-testrequirement', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 5, 'TEST_REQUIREMENT');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-undefined', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 6, 'UNDEFINED');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-ergonomic', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 7, 'ERGONOMIC');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-performance', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 8, 'PERFORMANCE');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-technical', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 9, 'TECHNICAL');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-userstory', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 10, 'USER_STORY');



insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID)
values ('feat-1108-security', 'liquibase', '2014-12-03', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into REQUIREMENT_VERSION(RES_ID, REQUIREMENT_ID, REFERENCE, VERSION_NUMBER, CATEGORY)
values ((select max(RES_ID) from RESOURCE), (select max(RLN_ID) from REQUIREMENT), '', 11, 'SECURITY');


update REQUIREMENT
set CURRENT_VERSION_ID = (select max(RES_ID) from RESOURCE)
where RLN_ID = (select max(RLN_ID) from (select * from REQUIREMENT) as re);

