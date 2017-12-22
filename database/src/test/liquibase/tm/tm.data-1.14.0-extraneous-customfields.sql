

-- create an attachment list. All will refer to it.

 insert into ATTACHMENT_LIST(attachment_list_id) values (-1);

-- create a project

insert into TEST_CASE_LIBRARY(tcl_id, attachment_list_id) values (-1, -1);
insert into REQUIREMENT_LIBRARY(rl_id, attachment_list_id) values (-1, -1);
insert into CAMPAIGN_LIBRARY(cl_id, attachment_list_id) values (-1, -1);
insert into CUSTOM_REPORT_LIBRARY(crl_id, attachment_list_id) values (-1, -1);

 insert into PROJECT(project_id, name, created_by, created_on, attachment_list_id, req_categories_list, tc_natures_list, tc_types_list,
tcl_id, cl_id, rl_id, crl_id)
values (-1, 'project', 'admin', '2016-06-29', -1, -1, -2, -3, -1, -1, -1, -1);

-- create a couple of custom fields
-- first a combobox, second a tag list

insert into CUSTOM_FIELD(cf_id, field_type, name, label, default_value, input_type, code)
values (-1, 'SSF', 'single select list 1', 'mylist#1', 'bob', 'DROPDOWN_LIST', 'SSF1'),
        (-2, 'MSF', 'tag list 1', 'mytags#1', null, 'TAG', 'MSF1');

-- the combo box and tag list must have options

insert into CUSTOM_FIELD_OPTION(cf_id, label, position, code)
values (-1, 'bob', 0, 'bob'),
        (-1, 'mike', 1, 'mike'),
        (-2, 'tag1', 0, 'tag1'),
        (-2, 'tag2', 1, 'tag2');

-- bind that to the test cases of the project

insert into CUSTOM_FIELD_BINDING(cfb_id, cf_id, bound_entity, bound_project_id, position)
values (-1,-1, 'TEST_CASE', -1, 0),
        (-2, -2, 'TEST_CASE', -1, 1),
        (-3, -1, 'TEST_STEP', -1, 0);

-- create a couple of test cases : one is ok, the other one suffers from 6340

insert into TEST_CASE_LIBRARY_NODE(tcln_id, name, created_by, created_on, project_id, attachment_list_id)
values (-1, 'tc-6340-fine', 'admin', '2016-06-29', -1, -1),
        (-2, 'tc-6340-wrong', 'admin', '2016-06-29', -1, -1);

insert into TEST_CASE(tcln_id, version, tc_nature, tc_type, prerequisite)
values (-1, 1, -12, -20, ''),
        (-2, 1, -12, -20, '');

-- and the steps

insert into TEST_STEP(test_step_id)
values (-1), (-2), (-3), (-4);

insert into ACTION_TEST_STEP(test_step_id)
values (-1), (-2), (-3), (-4);

insert into TEST_CASE_STEPS(test_case_id, step_id, step_order)
values (-1, -1, 0),
        (-1, -2, 1),
        (-2, -3, 0),
        (-2, -4, 1);


-- create their custom field values

insert into CUSTOM_FIELD_VALUE(cfv_id, bound_entity_id, bound_entity_type, cfb_id, field_type, value)
values (-1, -1, 'TEST_CASE', -1, 'SSF', 'bob'),
        (-2, -1, 'TEST_CASE', -2, 'MSF', null),
        (-3, -2, 'TEST_CASE', -1, 'SSF', 'bob'),
        (-4, -2, 'TEST_CASE', -1, 'SSF', 'bob'),
        (-5, -2, 'TEST_CASE', -2, 'MSF', null),
        (-6, -2, 'TEST_CASE', -2, 'MSF', null),
        (-7, -1, 'TEST_STEP', -3, 'SSF', 'mike'),
        (-8, -2, 'TEST_STEP', -3, 'SSF', 'mike'),
        (-9, -3, 'TEST_STEP', -3, 'SSF', 'mike'),
        (-10, -4, 'TEST_STEP', -3, 'SSF', 'mike');

insert into CUSTOM_FIELD_VALUE_OPTION(cfv_id, label, position)
values (-2, 'changed', 0),
        (-2, 'changed too', 1),
        (-5, 'changed', 0),
        (-5, 'changed too', 1),
        (-6, 'tag 1', 0),
        (-6, 'tag 2', 1);

-- creating executions for those test cases that present the same defects (ie extraneous denormalized fields)

insert into EXECUTION(execution_id, tcln_id, created_by, created_on, name, prerequisite)
values (-1, -1, 'admin', '2016-06-29', 'tc-6340-fine', ''),
        (-2, -2, 'admin', '2016-06-29', 'tc-6340-wrong', '');

insert into EXECUTION_STEP(execution_step_id, created_by, created_on, action)
values (-1, 'admin', '2016-06-29', ''),
        (-2, 'admin', '2016-06-29', ''),
        (-3, 'admin', '2016-06-29', ''),
        (-4, 'admin', '2016-06-29', '');

insert into EXECUTION_EXECUTION_STEPS(execution_id, execution_step_id, execution_step_order)
values (-1, -1, 0),
        (-1, -2, 1),
        (-2, -3, 0),
        (-2, -4, 1);


-- creating their denormalized values (for the ok execution and the not ok execution)

insert into DENORMALIZED_FIELD_VALUE(dfv_id, code, denormalized_field_holder_id, denormalized_field_holder_type, label, value, input_type, field_type)
values (-1, 'SSF1', -1, 'EXECUTION', 'mylist#1', 'bob', 'DROPDOWN_LIST', 'SSF'), 
        (-2, 'MSF1', -1, 'EXECUTION', 'mytags#1', null, 'TAG', 'MSF'),
        (-3, 'SSF1', -2, 'EXECUTION', 'mylist#1', 'bob', 'DROPDOWN_LIST', 'SSF'), 
        (-4, 'SSF1', -2, 'EXECUTION', 'mylist#1', 'bob', 'DROPDOWN_LIST', 'SSF'), 
        (-5, 'MSF1', -2, 'EXECUTION', 'mytags#1', null, 'TAG', 'MSF'),
        (-6, 'MSF1', -2, 'EXECUTION', 'mytags#1', null, 'TAG', 'MSF'),
        (-7, 'SSF1', -1, 'EXECUTION_STEP', 'mylist#1', 'mike', 'DROPDOWN_LIST', 'SSF'),
        (-8, 'SSF1', -2, 'EXECUTION_STEP', 'mylist#1', 'mike', 'DROPDOWN_LIST', 'SSF'),
        (-9, 'SSF1', -3, 'EXECUTION_STEP', 'mylist#1', 'mike', 'DROPDOWN_LIST', 'SSF'),
        (-10, 'SSF1', -4, 'EXECUTION_STEP', 'mylist#1', 'mike', 'DROPDOWN_LIST', 'SSF');

insert into DENORMALIZED_FIELD_OPTION(dfv_id, label, position, code)
values (-1, 'bob', 0, 'bob'),
        (-1, 'mike', 1, 'mike'),
        (-2, 'tag1', 0, 'tag1'),
        (-2, 'tag2', 1, 'tag2'),
        (-2, 'changed', 1, 'changed'),
        (-2, 'changed too', 1, 'changed too'),
        (-3, 'bob', 0, 'bob'),
        (-3, 'mike', 1, 'mike'),
        (-4, 'bob', 0, 'bob'),
        (-4, 'mike', 1, 'mike'),
        (-5, 'tag1', 0, 'tag1'),
        (-5, 'tag2', 1, 'tag2'),
        (-5, 'changed', 2, 'changed'),
        (-5, 'changed too', 3, 'changed too'),
        (-6, 'tag1', 0, 'tag1'),
        (-6, 'tag2', 1, 'tag2'),
        (-6, 'changed', 2, 'changed'),
        (-6, 'changed too', 3, 'changed too');
        

insert into DENORMALIZED_FIELD_VALUE_OPTION(dfv_id, label, position)
values (-2, 'changed', 0),
        (-2, 'changed too', 1),
        (-5, 'changed ', 0),
        (-5, 'changed too', 1),
        (-6, 'tag 1', 0),
        (-6, 'tag 2', 1);