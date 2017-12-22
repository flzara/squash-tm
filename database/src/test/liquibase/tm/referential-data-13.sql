
-- info lists and info list items

INSERT INTO INFO_LIST (INFO_LIST_ID, LABEL, DESCRIPTION, CODE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON) VALUES
(-1, 'infolist.category.default', '', 'DEF_REQ_CAT', 'system', '2010-01-01 00:00:00', 'system', '2010-01-01 00:00:00'),
(-2, 'infolist.nature.default', '', 'DEF_TC_NAT', 'system', '2010-01-01 00:00:00', 'system', '2010-01-01 00:00:00'),
(-3, 'infolist.type.default', '', 'DEF_TC_TYP', 'system', '2010-01-01 00:00:00', 'system', '2010-01-01 00:00:00');


INSERT INTO INFO_LIST_ITEM (ITEM_ID, ITEM_TYPE, LIST_ID, ITEM_INDEX, LABEL, CODE, ICON_NAME) VALUES
(-1, 'SYS', -1, 0, 'requirement.category.CAT_FUNCTIONAL', 'CAT_FUNCTIONAL', 'def_cat_functional'),
(-2, 'SYS', -1, 1, 'requirement.category.CAT_NON_FUNCTIONAL', 'CAT_NON_FUNCTIONAL', 'def_cat_non-functional'),
(-3, 'SYS', -1, 2, 'requirement.category.CAT_USE_CASE', 'CAT_USE_CASE', 'def_cat_use-case'),
(-4, 'SYS', -1, 3, 'requirement.category.CAT_BUSINESS', 'CAT_BUSINESS', 'def_cat_business'),
(-5, 'SYS', -1, 4, 'requirement.category.CAT_TEST_REQUIREMENT', 'CAT_TEST_REQUIREMENT', 'def_cat_test-requirement'),
(-6, 'SYS', -1, 5, 'requirement.category.CAT_UNDEFINED', 'CAT_UNDEFINED', 'def_cat_undefined'),
(-7, 'SYS', -1, 6, 'requirement.category.CAT_ERGONOMIC', 'CAT_ERGONOMIC', 'def_cat_ergonomic'),
(-8, 'SYS', -1, 7, 'requirement.category.CAT_PERFORMANCE', 'CAT_PERFORMANCE', 'def_cat_performance'),
(-9, 'SYS', -1, 8, 'requirement.category.CAT_TECHNICAL', 'CAT_TECHNICAL', 'def_cat_technical'),
(-10, 'SYS', -1, 9, 'requirement.category.CAT_USER_STORY', 'CAT_USER_STORY', 'def_cat_user-story'),
(-11, 'SYS', -1, 10, 'requirement.category.CAT_SECURITY', 'CAT_SECURITY', 'def_cat_security'),
(-12, 'SYS', -2, 0, 'test-case.nature.NAT_UNDEFINED', 'NAT_UNDEFINED', 'noicon'),
(-13, 'SYS', -2, 1, 'test-case.nature.NAT_FUNCTIONAL_TESTING', 'NAT_FUNCTIONAL_TESTING', 'noicon'),
(-14, 'SYS', -2, 2, 'test-case.nature.NAT_BUSINESS_TESTING', 'NAT_BUSINESS_TESTING', 'noicon'),
(-15, 'SYS', -2, 3, 'test-case.nature.NAT_USER_TESTING', 'NAT_USER_TESTING', 'noicon'),
(-16, 'SYS', -2, 4, 'test-case.nature.NAT_NON_FUNCTIONAL_TESTING', 'NAT_NON_FUNCTIONAL_TESTING', 'noicon'),
(-17, 'SYS', -2, 5, 'test-case.nature.NAT_PERFORMANCE_TESTING', 'NAT_PERFORMANCE_TESTING', 'noicon'),
(-18, 'SYS', -2, 6, 'test-case.nature.NAT_SECURITY_TESTING', 'NAT_SECURITY_TESTING', 'noicon'),
(-19, 'SYS', -2, 7, 'test-case.nature.NAT_ATDD', 'NAT_ATDD', 'noicon'),
(-20, 'SYS', -3, 0, 'test-case.type.TYP_UNDEFINED', 'TYP_UNDEFINED', 'noicon'),
(-21, 'SYS', -3, 1, 'test-case.type.TYP_COMPLIANCE_TESTING', 'TYP_COMPLIANCE_TESTING', 'noicon'),
(-22, 'SYS', -3, 2, 'test-case.type.TYP_CORRECTION_TESTING', 'TYP_CORRECTION_TESTING', 'noicon'),
(-23, 'SYS', -3, 3, 'test-case.type.TYP_EVOLUTION_TESTING', 'TYP_EVOLUTION_TESTING', 'noicon'),
(-24, 'SYS', -3, 4, 'test-case.type.TYP_REGRESSION_TESTING', 'TYP_REGRESSION_TESTING', 'noicon'),
(-25, 'SYS', -3, 5, 'test-case.type.TYP_END_TO_END_TESTING', 'TYP_END_TO_END_TESTING', 'noicon'),
(-26, 'SYS', -3, 6, 'test-case.type.TYP_PARTNER_TESTING', 'TYP_PARTNER_TESTING', 'noicon');

-- charts


-- -------------------------------------------
-- section 1 :  basic attribute columns
-- -------------------------------------------


-- columns for entity : REQUIREMENT --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-1, 'ATTRIBUTE', TRUE, 'REQUIREMENT_ID', 'REQUIREMENT', null, 'NUMERIC', 'id', null),
        (-3, 'ATTRIBUTE', FALSE, 'REQUIREMENT_PROJECT', 'REQUIREMENT', null, 'NUMERIC', 'project.id', null),
        (-4, 'ATTRIBUTE', TRUE, 'REQUIREMENT_CRITICALITY', 'REQUIREMENT', null, 'LEVEL_ENUM', 'resource.criticality', null),
        (-5, 'ATTRIBUTE', TRUE, 'REQUIREMENT_STATUS', 'REQUIREMENT', null, 'LEVEL_ENUM', 'resource.status', null),
        (-6, 'ATTRIBUTE', TRUE, 'REQUIREMENT_CATEGORY', 'REQUIREMENT', null, 'INFO_LIST_ITEM', 'resource.category.code', null);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-1, 'MEASURE'), (-1, 'AXIS'), (-1, 'FILTER'),
         (-4, 'MEASURE'), (-4, 'AXIS'), (-4, 'FILTER'),
         (-5, 'MEASURE'), (-5, 'AXIS'), (-5, 'FILTER'),
         (-6, 'MEASURE'), (-6, 'AXIS'), (-6, 'FILTER');




-- columns for entity : REQUIREMENT_VERSION --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-7, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_ID', 'REQUIREMENT_VERSION', null, 'NUMERIC', 'id', null),
        (-8, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_REFERENCE', 'REQUIREMENT_VERSION', null, 'STRING', 'reference', null),
        (-9, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_CATEGORY', 'REQUIREMENT_VERSION', null, 'INFO_LIST_ITEM', 'category.code', null),
        (-10, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_CRITICALITY', 'REQUIREMENT_VERSION', null, 'LEVEL_ENUM', 'criticality', null),
        (-11, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_STATUS', 'REQUIREMENT_VERSION', null, 'LEVEL_ENUM', 'status', null),
        (-12, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_CREATED_BY', 'REQUIREMENT_VERSION', null, 'STRING', 'audit.createdBy', null),
        (-13, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_CREATED_ON', 'REQUIREMENT_VERSION', null, 'DATE', 'audit.createdOn', null),
        (-14, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_MODIFIED_BY', 'REQUIREMENT_VERSION', null, 'STRING', 'audit.lastModifiedBy', null),
        (-15, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_MODIFIED_ON', 'REQUIREMENT_VERSION', null, 'DATE', 'audit.lastModifiedOn', null),
        (-16, 'ATTRIBUTE', TRUE, 'REQUIREMENT_VERSION_VERS_NUM', 'REQUIREMENT_VERSION', null, 'NUMERIC', 'versionNumber', null);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-7, 'MEASURE'), (-7, 'AXIS'), (-7, 'FILTER'),
         (-8, 'FILTER'), (-8, 'AXIS'),
         (-9, 'MEASURE'), (-9, 'AXIS'), (-9, 'FILTER'),
         (-10, 'MEASURE'), (-10, 'AXIS'), (-10, 'FILTER'),
         (-11, 'MEASURE'), (-11, 'AXIS'), (-11, 'FILTER'),
         (-12, 'AXIS'), (-12, 'FILTER'),
         (-13, 'AXIS'), (-13, 'FILTER'),
         (-14, 'AXIS'), (-14, 'FILTER'),
         (-15, 'AXIS'), (-15, 'FILTER'),
         (-16, 'FILTER'), (-16, 'MEASURE');




-- columns for entity : TEST_CASE --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-19, 'ATTRIBUTE', TRUE, 'TEST_CASE_ID', 'TEST_CASE', null, 'NUMERIC', 'id', null),
        (-20, 'ATTRIBUTE', TRUE, 'TEST_CASE_REFERENCE', 'TEST_CASE', null, 'STRING', 'reference', null),
        (-21, 'ATTRIBUTE', TRUE, 'TEST_CASE_IMPORTANCE', 'TEST_CASE', null, 'LEVEL_ENUM', 'importance', null),
        (-22, 'ATTRIBUTE', TRUE, 'TEST_CASE_NATURE', 'TEST_CASE', null, 'INFO_LIST_ITEM', 'nature.code', null),
        (-23, 'ATTRIBUTE', TRUE, 'TEST_CASE_TYPE', 'TEST_CASE', null, 'INFO_LIST_ITEM', 'type.code', null),
        (-24, 'ATTRIBUTE', TRUE, 'TEST_CASE_STATUS', 'TEST_CASE', null, 'LEVEL_ENUM', 'status', null),
        (-25, 'ATTRIBUTE', TRUE, 'TEST_CASE_CREATED_BY', 'TEST_CASE', null, 'STRING', 'audit.createdBy', null),
        (-26, 'ATTRIBUTE', TRUE, 'TEST_CASE_CREATED_ON', 'TEST_CASE', null, 'DATE', 'audit.createdOn', null),
        (-27, 'ATTRIBUTE', TRUE, 'TEST_CASE_MODIFIED_BY', 'TEST_CASE', null, 'STRING', 'audit.lastModifiedBy', null),
        (-28, 'ATTRIBUTE', TRUE, 'TEST_CASE_MODIFIED_ON', 'TEST_CASE', null, 'DATE', 'audit.lastModifiedOn', null),
        (-29, 'ATTRIBUTE', FALSE, 'TEST_CASE_PROJECT', 'TEST_CASE', null, 'NUMERIC', 'project.id', null);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-19, 'MEASURE'), (-19, 'AXIS'), (-19, 'FILTER'),
         (-20, 'FILTER'), (-20, 'AXIS'),
         (-21, 'MEASURE'), (-21, 'AXIS'), (-21, 'FILTER'),
         (-22, 'MEASURE'), (-22, 'AXIS'), (-22, 'FILTER'),
         (-23, 'MEASURE'), (-23, 'AXIS'), (-23, 'FILTER'),
         (-24, 'MEASURE'), (-24, 'AXIS'), (-24, 'FILTER'),
         (-25, 'AXIS'), (-25, 'FILTER'),
         (-26, 'AXIS'), (-26, 'FILTER'),
         (-27, 'AXIS'), (-27, 'FILTER'),
         (-28, 'AXIS'), (-28, 'FILTER');




-- columns for entity : CAMPAIGN --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-37, 'ATTRIBUTE', TRUE, 'CAMPAIGN_ID', 'CAMPAIGN', null, 'NUMERIC', 'id', null),
        (-38, 'ATTRIBUTE', FALSE, 'CAMPAIGN_PROJECT', 'CAMPAIGN', null, 'NUMERIC', 'project.id', null),
        (-39, 'ATTRIBUTE', TRUE, 'CAMPAIGN_REFERENCE', 'CAMPAIGN', null, 'STRING', 'reference', null),
        (-40, 'ATTRIBUTE', TRUE, 'CAMPAIGN_SCHED_START', 'CAMPAIGN', null, 'DATE', 'scheduledPeriod.scheduledStartDate', null),
        (-41, 'ATTRIBUTE', TRUE, 'CAMPAIGN_SCHED_END', 'CAMPAIGN', null, 'DATE', 'scheduledPeriod.scheduledEndDate', null),
        (-42, 'ATTRIBUTE', TRUE, 'CAMPAIGN_ACTUAL_START', 'CAMPAIGN', null, 'DATE', 'actualPeriod.actualStartDate', null),
        (-43, 'ATTRIBUTE', TRUE, 'CAMPAIGN_ACTUAL_END', 'CAMPAIGN', null, 'DATE', 'actualPeriod.actualEndDate', null);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-37, 'MEASURE'), (-37, 'AXIS'), (-37, 'FILTER'),
         (-39, 'FILTER'), (-39, 'MEASURE'),
         (-40, 'AXIS'), (-40, 'FILTER'),
         (-41, 'AXIS'), (-41, 'FILTER'),
         (-42, 'AXIS'), (-42, 'FILTER'),
         (-43, 'AXIS'), (-43, 'FILTER');




-- columns for entity : ITERATION --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-46, 'ATTRIBUTE', TRUE, 'ITERATION_ID', 'ITERATION', null, 'NUMERIC', 'id', null),
        (-47, 'ATTRIBUTE', TRUE, 'ITERATION_REFERENCE', 'ITERATION', null, 'STRING', 'reference', null),
        (-48, 'ATTRIBUTE', TRUE, 'ITERATION_SCHED_START', 'ITERATION', null, 'DATE', 'scheduledPeriod.scheduledStartDate', null),
        (-49, 'ATTRIBUTE', TRUE, 'ITERATION_SCHED_END', 'ITERATION', null, 'DATE', 'scheduledPeriod.scheduledEndDate', null),
        (-50, 'ATTRIBUTE', TRUE, 'ITERATION_ACTUAL_START', 'ITERATION', null, 'DATE', 'actualPeriod.actualStartDate', null),
        (-51, 'ATTRIBUTE', TRUE, 'ITERATION_ACTUAL_END', 'ITERATION', null, 'DATE', 'actualPeriod.actualEndDate', null);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-46, 'MEASURE'), (-46, 'AXIS'), (-46, 'FILTER'),
         (-47, 'FILTER'), (-47, 'MEASURE'),
         (-48, 'AXIS'), (-48, 'FILTER'),
         (-49, 'AXIS'), (-49, 'FILTER'),
         (-50, 'AXIS'), (-50, 'FILTER'),
         (-51, 'AXIS'), (-51, 'FILTER');




-- columns for entity : ITEM_TEST_PLAN --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-54, 'ATTRIBUTE', TRUE, 'ITEM_TEST_PLAN_ID', 'ITEM_TEST_PLAN', null, 'NUMERIC', 'id', null),
        (-55, 'ATTRIBUTE', TRUE, 'ITEM_TEST_PLAN_LABEL', 'ITEM_TEST_PLAN', null, 'STRING', 'label', null),
        (-56, 'ATTRIBUTE', TRUE, 'ITEM_TEST_PLAN_STATUS', 'ITEM_TEST_PLAN', null, 'EXECUTION_STATUS', 'executionStatus', null),
        (-57, 'ATTRIBUTE', TRUE, 'ITEM_TEST_PLAN_LASTEXECON', 'ITEM_TEST_PLAN', null, 'DATE', 'lastExecutedOn', null),
        (-58, 'ATTRIBUTE', TRUE, 'ITEM_TEST_PLAN_DATASET_LABEL', 'ITEM_TEST_PLAN', null, 'STRING', 'referencedDataset.name', null),
        (-59, 'ATTRIBUTE', TRUE, 'ITEM_TEST_PLAN_TESTER', 'ITEM_TEST_PLAN', null, 'STRING', 'user.login', null),
        (-60, 'ATTRIBUTE', TRUE, 'ITEM_TEST_PLAN_TC_ID', 'ITEM_TEST_PLAN', null, 'NUMERIC', 'referencedTestCase.id', null);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-54, 'MEASURE'), (-54, 'AXIS'), (-54, 'FILTER'),
         (-55, 'MEASURE'), (-55, 'AXIS'), (-55, 'FILTER'),
         (-56, 'MEASURE'), (-56, 'AXIS'), (-56, 'FILTER'),
         (-57, 'MEASURE'), (-57, 'AXIS'), (-57, 'FILTER'),
         (-58, 'MEASURE'), (-58, 'AXIS'), (-58, 'FILTER'),
         (-59, 'MEASURE'), (-59, 'AXIS'), (-59, 'FILTER'),
         (-60, 'MEASURE'), (-60, 'AXIS'), (-60, 'FILTER');




-- columns for entity : EXECUTION --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-66, 'ATTRIBUTE', TRUE, 'EXECUTION_ID', 'EXECUTION', null, 'NUMERIC', 'id', null),
        (-67, 'ATTRIBUTE', TRUE, 'EXECUTION_LABEL', 'EXECUTION', null, 'STRING', 'name', null),
        (-68, 'ATTRIBUTE', TRUE, 'EXECUTION_DS_LABEL', 'EXECUTION', null, 'STRING', 'datasetLabel', null),
        (-69, 'ATTRIBUTE', TRUE, 'EXECUTION_LASTEXEC', 'EXECUTION', null, 'DATE', 'lastExecutedOn', null),
        (-70, 'ATTRIBUTE', TRUE, 'EXECUTION_TESTER_LOGIN', 'EXECUTION', null, 'STRING', 'lastExecutedBy', null),
        (-71, 'ATTRIBUTE', TRUE, 'EXECUTION_STATUS', 'EXECUTION', null, 'EXECUTION_STATUS', 'executionStatus', null);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-66, 'MEASURE'), (-66, 'AXIS'), (-66, 'FILTER'),
         (-67, 'AXIS'), (-67, 'FILTER'),
         (-68, 'AXIS'), (-68, 'FILTER'),
         (-69, 'AXIS'), (-69, 'FILTER'),
         (-70, 'AXIS'), (-70, 'FILTER'),
         (-71, 'MEASURE'), (-71, 'AXIS'), (-71, 'FILTER');




-- columns for entity : ISSUE --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-74, 'ATTRIBUTE', FALSE, 'ISSUE_ID', 'ISSUE', null, 'NUMERIC', 'id', null),
        (-75, 'ATTRIBUTE', FALSE, 'ISSUE_REMOTE_ID', 'ISSUE', null, 'STRING', 'remoteIssueId', null),
        (-76, 'ATTRIBUTE', FALSE, 'ISSUE_STATUS', 'ISSUE', null, 'STRING', 'status', null),
        (-77, 'ATTRIBUTE', FALSE, 'ISSUE_SEVERITY', 'ISSUE', null, 'STRING', 'severity', null),
        (-78, 'ATTRIBUTE', FALSE, 'ISSUE_BUGTRACKER', 'ISSUE', null, 'STRING', 'bugtracker', null);




-- columns for entity : TEST_CASE_STEP --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-79, 'ATTRIBUTE', FALSE, 'TEST_CASE_STEP_ID', 'TEST_CASE_STEP', null, 'NUMERIC', 'id', null),
        (-80, 'ATTRIBUTE', FALSE, 'TEST_CASE_STEP_CLASS', 'TEST_CASE_STEP', null, 'NUMERIC', 'class', null);




-- columns for entity : TEST_CASE_NATURE --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-81, 'ATTRIBUTE', FALSE, 'TEST_CASE_NATURE_ID', 'INFO_LIST_ITEM', 'TEST_CASE_NATURE', 'NUMERIC', 'id', null),
        (-82, 'ATTRIBUTE', FALSE, 'TEST_CASE_NATURE_LABEL', 'INFO_LIST_ITEM', 'TEST_CASE_NATURE', 'STRING', 'label', null);




-- columns for entity : TEST_CASE_TYPE --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-83, 'ATTRIBUTE', FALSE, 'TEST_CASE_TYPE_ID', 'INFO_LIST_ITEM', 'TEST_CASE_TYPE', 'NUMERIC', 'id', null),
        (-84, 'ATTRIBUTE', FALSE, 'TEST_CASE_TYPE_LABEL', 'INFO_LIST_ITEM', 'TEST_CASE_TYPE', 'STRING', 'label', null);




-- columns for entity : REQUIREMENT_VERSION_CATEGORY --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-85, 'ATTRIBUTE', FALSE, 'REQUIREMENT_VERSION_CATEGORY_ID', 'INFO_LIST_ITEM', 'REQUIREMENT_VERSION_CATEGORY', 'NUMERIC', 'id', null),
        (-86, 'ATTRIBUTE', FALSE, 'REQUIREMENT_VERSION_CATEGORY_LABEL', 'INFO_LIST_ITEM', 'REQUIREMENT_VERSION_CATEGORY', 'STRING', 'label', null);




-- columns for entity : TEST_CASE_MILESTONE --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-87, 'ATTRIBUTE', FALSE, 'TEST_CASE_MILESTONE_ID', 'MILESTONE', 'TEST_CASE_MILESTONE', 'NUMERIC', 'id', null),
        (-88, 'ATTRIBUTE', FALSE, 'TEST_CASE_MILESTONE_LABEL', 'MILESTONE', 'TEST_CASE_MILESTONE', 'STRING', 'label', null);




-- columns for entity : REQUIREMENT_VERSION_MILESTONE --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-89, 'ATTRIBUTE', FALSE, 'REQUIREMENT_VERSION_MILESTONE_ID', 'MILESTONE', 'REQUIREMENT_VERSION_MILESTONE', 'NUMERIC', 'id', null),
        (-90, 'ATTRIBUTE', FALSE, 'REQUIREMENT_VERSION_MILESTONE_LABEL', 'MILESTONE', 'REQUIREMENT_VERSION_MILESTONE', 'STRING', 'label', null);




-- columns for entity : ITERATION_TEST_PLAN_ASSIGNED_USER --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-91, 'ATTRIBUTE', FALSE, 'ITERATION_TEST_PLAN_ASSIGNED_USER_ID', 'USER', 'ITERATION_TEST_PLAN_ASSIGNED_USER', 'NUMERIC', 'id', null),
        (-92, 'ATTRIBUTE', FALSE, 'ITERATION_TEST_PLAN_ASSIGNED_USER_LOGIN', 'USER', 'ITERATION_TEST_PLAN_ASSIGNED_USER', 'STRING', 'login', null);




-- columns for entity : AUTOMATED_TEST --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-93, 'ATTRIBUTE', FALSE, 'AUTOMATED_TEST_ID', 'AUTOMATED_TEST', null, 'NUMERIC', 'id', null);




-- columns for entity : AUTOMATED_EXECUTION_EXTENDER --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-94, 'ATTRIBUTE', FALSE, 'AUTOMATED_EXECUTION_EXTENDER_ID', 'AUTOMATED_EXECUTION_EXTENDER', null, 'NUMERIC', 'id', null);




-- -------------------------------------------
-- section 2 :  subqueries
-- -------------------------------------------

-- subqueries for entity REQUIREMENT --

-- subquery reqVCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-1, 'REQUIREMENT_NB_VERSIONS_SUBQUERY', 'SUBQUERY', 'INNER_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-7, -1, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-1, -1, 'NONE', 0);



-- subqueries for entity REQUIREMENT_VERSION --

-- subquery rvVerifTCCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-2, 'REQUIREMENT_VERSION_TCCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-19, -2, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-7, -2, 'NONE', 0);



-- subquery rvMilesCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-3, 'REQUIREMENT_VERSION_MILCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-89, -3, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-7, -3, 'NONE', 0);



-- subqueries for entity TEST_CASE --

-- subquery tcVerifVersionCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-4, 'TEST_CASE_VERSCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-7, -4, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-19, -4, 'NONE', 0);



-- subquery tcCallStepsCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-5, 'TEST_CASE_CALLSTEPCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-79, -5, 'COUNT', 0);
insert into CHART_FILTER(FILTER_ID, CHART_COLUMN_ID, QUERY_ID, FILTER_OPERATION) values (-1, -80, -5, 'EQUALS');
insert into CHART_FILTER_VALUES(FILTER_ID, FILTER_VALUE)
values 
        (-1,'2');

insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-19, -5, 'NONE', 0);



-- subquery tcStepsCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-6, 'TEST_CASE_STEPCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-79, -6, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-19, -6, 'NONE', 0);



-- subquery tcMilesCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-7, 'TEST_CASE_VERSION_MILCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-87, -7, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-19, -7, 'NONE', 0);



-- subquery tcIterCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-8, 'TEST_CASE_ITERCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-46, -8, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-19, -8, 'NONE', 0);



-- subquery tcExeCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-9, 'TEST_CASE_EXECOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-66, -9, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-19, -9, 'NONE', 0);



-- subquery tcHasAutoSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-10, 'TEST_CASE_HASAUTOSCRIPT_SUBQUERY', 'INLINED', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-93, -10, 'NOT_NULL', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-19, -10, 'NONE', 0);



-- subqueries for entity CAMPAIGN --

-- subquery cIterCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-11, 'CAMPAIGN_ITERCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-46, -11, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-37, -11, 'NONE', 0);



-- subquery cIssueCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-12, 'CAMPAIGN_ISSUECOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-75, -12, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-37, -12, 'NONE', 0);



-- subqueries for entity ITERATION --

-- subquery itItemCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-13, 'ITERATION_ITEMCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-54, -13, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-46, -13, 'NONE', 0);



-- subquery itIssueCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-14, 'ITERATION_ISSUECOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-75, -14, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-46, -14, 'NONE', 0);



-- subqueries for entity ITEM_TEST_PLAN --

-- subquery itpTcDeletedSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-15, 'ITEM_TEST_PLAN_TCDELETED_SUBQUERY', 'INLINED', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-19, -15, 'IS_NULL', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-54, -15, 'NONE', 0);



-- subquery itpIsExecutedSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-16, 'ITEM_TEST_PLAN_ISEXECUTED_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-66, -16, 'NOT_NULL', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-54, -16, 'NONE', 0);



-- subquery itpManExCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-17, 'ITEM_TEST_PLAN_MANEXCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-66, -17, 'COUNT', 0);
insert into CHART_FILTER(FILTER_ID, CHART_COLUMN_ID, QUERY_ID, FILTER_OPERATION) values (-2, -94, -17, 'NOT_NULL');
insert into CHART_FILTER_VALUES(FILTER_ID, FILTER_VALUE)
values 
        (-2,'FALSE');

insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-54, -17, 'NONE', 0);



-- subquery itpAutoExCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-18, 'ITEM_TEST_PLAN_AUTOEXCOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-66, -18, 'COUNT', 0);
insert into CHART_FILTER(FILTER_ID, CHART_COLUMN_ID, QUERY_ID, FILTER_OPERATION) values (-3, -94, -18, 'NOT_NULL');
insert into CHART_FILTER_VALUES(FILTER_ID, FILTER_VALUE)
values 
        (-3,'TRUE');

insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-54, -18, 'NONE', 0);



-- subquery itpIssueCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-19, 'ITEM_TEST_PLAN_ISSUECOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-75, -19, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-54, -19, 'NONE', 0);



-- subqueries for entity EXECUTION --

-- subquery exIsAutoSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-20, 'EXECUTION_ISAUTO_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-94, -20, 'NOT_NULL', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-66, -20, 'NONE', 0);



-- subquery exIssueCountSub --

insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values (-21, 'EXECUTION_ISSUECOUNT_SUBQUERY', 'SUBQUERY', 'LEFT_JOIN');
insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) values (-75, -21, 'COUNT', 0);
insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  values (-66, -21, 'NONE', 0);



-- subqueries for entity ISSUE --

-- subqueries for entity TEST_CASE_STEP --

-- subqueries for entity TEST_CASE_NATURE --

-- subqueries for entity TEST_CASE_TYPE --

-- subqueries for entity REQUIREMENT_VERSION_CATEGORY --

-- subqueries for entity TEST_CASE_MILESTONE --

-- subqueries for entity REQUIREMENT_VERSION_MILESTONE --

-- subqueries for entity ITERATION_TEST_PLAN_ASSIGNED_USER --

-- subqueries for entity AUTOMATED_TEST --

-- subqueries for entity AUTOMATED_EXECUTION_EXTENDER --


-- -------------------------------------------
-- section 3 :  calculated columns
-- -------------------------------------------


-- columns for entity : REQUIREMENT --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-2, 'CALCULATED', TRUE, 'REQUIREMENT_NB_VERSIONS', 'REQUIREMENT', null, 'NUMERIC', 'count(requirementVersionCoverages)', -1);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-2, 'MEASURE'), (-2, 'AXIS'), (-2, 'FILTER');




-- columns for entity : REQUIREMENT_VERSION --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-17, 'CALCULATED', TRUE, 'REQUIREMENT_VERSION_TCCOUNT', 'REQUIREMENT_VERSION', null, 'NUMERIC', 'count(requirementVersionCoverages)', -2),
        (-18, 'CALCULATED', TRUE, 'REQUIREMENT_VERSION_MILCOUNT', 'REQUIREMENT_VERSION', null, 'NUMERIC', 'count(milestones)', -3);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-17, 'MEASURE'), (-17, 'AXIS'), (-17, 'FILTER'),
         (-18, 'MEASURE'), (-18, 'AXIS'), (-18, 'FILTER');




-- columns for entity : TEST_CASE --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-30, 'CALCULATED', TRUE, 'TEST_CASE_VERSCOUNT', 'TEST_CASE', null, 'NUMERIC', 'count(requirementVersionCoverages)', -4),
        (-31, 'CALCULATED', TRUE, 'TEST_CASE_CALLSTEPCOUNT', 'TEST_CASE', null, 'NUMERIC', 'count(steps[class="CallTestStep"])', -5),
        (-32, 'CALCULATED', TRUE, 'TEST_CASE_STEPCOUNT', 'TEST_CASE', null, 'NUMERIC', 'count(steps)', -6),
        (-33, 'CALCULATED', TRUE, 'TEST_CASE_MILCOUNT', 'TEST_CASE', null, 'NUMERIC', 'count(milestones)', -7),
        (-34, 'CALCULATED', TRUE, 'TEST_CASE_ITERCOUNT', 'TEST_CASE', null, 'NUMERIC', 'count(iterations)', -8),
        (-35, 'CALCULATED', TRUE, 'TEST_CASE_EXECOUNT', 'TEST_CASE', null, 'NUMERIC', 'count(executions)', -9),
        (-36, 'CALCULATED', TRUE, 'TEST_CASE_HASAUTOSCRIPT', 'TEST_CASE', null, 'BOOLEAN', 'notnull(automatedTest)', -10);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-30, 'MEASURE'), (-30, 'AXIS'), (-30, 'FILTER'),
         (-31, 'MEASURE'), (-31, 'AXIS'), (-31, 'FILTER'),
         (-32, 'MEASURE'), (-32, 'AXIS'), (-32, 'FILTER'),
         (-33, 'MEASURE'), (-33, 'AXIS'), (-33, 'FILTER'),
         (-34, 'MEASURE'), (-34, 'AXIS'), (-34, 'FILTER'),
         (-35, 'MEASURE'), (-35, 'AXIS'), (-35, 'FILTER'),
         (-36, 'MEASURE'), (-36, 'AXIS'), (-36, 'FILTER');




-- columns for entity : CAMPAIGN --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-44, 'CALCULATED', TRUE, 'CAMPAIGN_ITERCOUNT', 'CAMPAIGN', null, 'NUMERIC', 'count(iterations)', -11),
        (-45, 'CALCULATED', TRUE, 'CAMPAIGN_ISSUECOUNT', 'CAMPAIGN', null, 'NUMERIC', 'count(issues)', -12);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-44, 'MEASURE'), (-44, 'AXIS'), (-44, 'FILTER'),
         (-45, 'MEASURE'), (-45, 'AXIS'), (-45, 'FILTER');




-- columns for entity : ITERATION --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-52, 'CALCULATED', TRUE, 'ITERATION_ITEMCOUNT', 'ITERATION', null, 'NUMERIC', 'count(testPlans)', -13),
        (-53, 'CALCULATED', TRUE, 'ITERATION_ISSUECOUNT', 'ITERATION', null, 'NUMERIC', 'count(issues)', -14);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-52, 'MEASURE'), (-52, 'AXIS'), (-52, 'FILTER'),
         (-53, 'MEASURE'), (-53, 'AXIS'), (-53, 'FILTER');




-- columns for entity : ITEM_TEST_PLAN --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-61, 'CALCULATED', TRUE, 'ITEM_TEST_PLAN_TC_DELETED', 'ITEM_TEST_PLAN', null, 'BOOLEAN', 'isnull(referencedTestCase)', -15),
        (-62, 'CALCULATED', TRUE, 'ITEM_TEST_PLAN_IS_EXECUTED', 'ITEM_TEST_PLAN', null, 'BOOLEAN', 'notnull(executions)', -16),
        (-63, 'CALCULATED', TRUE, 'ITEM_TEST_PLAN_MANEXCOUNT', 'ITEM_TEST_PLAN', null, 'NUMERIC', 'count(executions[auto="false"])', -17),
        (-64, 'CALCULATED', TRUE, 'ITEM_TEST_PLAN_AUTOEXCOUNT', 'ITEM_TEST_PLAN', null, 'NUMERIC', 'count(executions[auto="true"])', -18),
        (-65, 'CALCULATED', TRUE, 'ITEM_TEST_PLAN_ISSUECOUNT', 'ITEM_TEST_PLAN', null, 'NUMERIC', 'count(issues)', -19);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-61, 'MEASURE'), (-61, 'AXIS'), (-61, 'FILTER'),
         (-62, 'MEASURE'), (-62, 'AXIS'), (-62, 'FILTER'),
         (-63, 'MEASURE'), (-63, 'AXIS'), (-63, 'FILTER'),
         (-64, 'MEASURE'), (-64, 'AXIS'), (-64, 'FILTER'),
         (-65, 'MEASURE'), (-65, 'AXIS'), (-65, 'FILTER');




-- columns for entity : EXECUTION --

insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values
        (-72, 'CALCULATED', TRUE, 'EXECUTION_ISAUTO', 'EXECUTION', null, 'BOOLEAN', 'notnull(automatedExecutionExtender)', -20),
        (-73, 'CALCULATED', TRUE, 'EXECUTION_ISSUECOUNT', 'EXECUTION', null, 'NUMERIC', 'count(issues)', -21);

insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values
         (-72, 'FILTER'),
         (-73, 'MEASURE'), (-73, 'AXIS'), (-73, 'FILTER');