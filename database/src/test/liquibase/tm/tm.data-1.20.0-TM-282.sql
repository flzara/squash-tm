
-- This will insert a Chart, before migration --

INSERT INTO CHART_QUERY (CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) VALUES
(22, NULL, 'MAIN', 'INNER_JOIN');

INSERT INTO CHART_DEFINITION (CHART_ID, USER_ID, QUERY_ID, NAME, VISIBILITY, CHART_TYPE, DESCRIPTION, PROJECT_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, SCOPE_TYPE) VALUES
(1, -40, 22, 'Graphe Bob', NULL, 'BAR', NULL, 2, 'admin', '2019-05-07 15:46:31', NULL, NULL, 'DEFAULT');


INSERT INTO CHART_AXIS_COLUMN (CHART_COLUMN_ID, QUERY_ID, LABEL, AXIS_OPERATION, AXIS_RANK, CUF_ID) VALUES
(-21, 22, '', 'NONE', 0, NULL);


INSERT INTO CHART_FILTER (FILTER_ID, CHART_COLUMN_ID, QUERY_ID, FILTER_OPERATION, CUF_ID) VALUES
(4, -21, 22, 'EQUALS', NULL);

INSERT INTO CHART_FILTER_VALUES (FILTER_ID, FILTER_VALUE) VALUES
(4, 'HIGH');


INSERT INTO CHART_MEASURE_COLUMN (CHART_COLUMN_ID, QUERY_ID, LABEL, MEASURE_OPERATION, MEASURE_RANK, CUF_ID) VALUES
(-1, 22, '', 'COUNT', 0, NULL);

