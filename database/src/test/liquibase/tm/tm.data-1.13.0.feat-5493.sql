Insert into ATTACHMENT_LIST(ATTACHMENT_LIST_ID) values (9001);

Insert into INFO_LIST(INFO_LIST_ID, CODE, CREATED_ON, CREATED_BY) values(9001, 'lollist', '2011-05-20 18:12:00.0', 'anyone');

Insert into PROJECT(PROJECT_ID, NAME, DESCRIPTION, LABEL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, CL_ID, TCL_ID, RL_ID, ATTACHMENT_LIST_ID, REQ_CATEGORIES_LIST, TC_NATURES_LIST, TC_TYPES_LIST) values 
 (4, 'Sandbox #1', '', null, true, 'henix_admin', '2011-05-20 18:12:00.0', null, null, null, null, null, 9001,9001 ,9001 ,9001);



insert into CUSTOM_FIELD (CF_ID, FIELD_TYPE, NAME, LABEL, OPTIONAL, DEFAULT_VALUE, INPUT_TYPE, CODE) values
(1, 'CF', 'cuf-text1', 'cuf-Text1', false, 'defaultt1', 'PLAIN_TEXT', 't1'),
(2, 'CF', 'cuf-text2', 'cuf-text2', true, '', 'PLAIN_TEXT', 't2'),
(3, 'SSF', 'cuf-deroulante1', 'cuf-deroulante1', false, 'opt11', 'DROPDOWN_LIST', 'd1'),
(4, 'SSF', 'cuf-deroulante2', 'cuf-deroulante2', true, '', 'DROPDOWN_LIST', 'd2'),
(5, 'CF', 'cuf-check', 'cuf-check', true, 'false', 'CHECKBOX', 'cuf_check');


insert into CUSTOM_FIELD_BINDING (CFB_ID, CF_ID, BOUND_ENTITY, BOUND_PROJECT_ID, POSITION) values
(1, 1, 'REQUIREMENT_VERSION', 4, 1),
(2, 2, 'REQUIREMENT_VERSION', 4, 2),
(3, 3, 'REQUIREMENT_VERSION', 4, 3),
(4, 4, 'REQUIREMENT_VERSION', 4, 4),
(5, 5, 'REQUIREMENT_VERSION', 4, 5);