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


-- This will insert a Chart, before migration --

INSERT INTO CHART_QUERY (CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) VALUES
(-22, NULL, 'MAIN', 'INNER_JOIN');

INSERT INTO CHART_DEFINITION (CHART_ID, USER_ID, QUERY_ID, NAME, VISIBILITY, CHART_TYPE, DESCRIPTION, PROJECT_ID, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON, SCOPE_TYPE) VALUES
(-1, -40, -22, 'Graphe Bob', NULL, 'BAR', NULL, 2, 'admin', '2019-05-07 15:46:31', NULL, NULL, 'DEFAULT');


INSERT INTO CHART_AXIS_COLUMN (CHART_COLUMN_ID, QUERY_ID, LABEL, AXIS_OPERATION, AXIS_RANK, CUF_ID) VALUES
(21, -22, '', 'NONE', 0, NULL);


INSERT INTO CHART_FILTER (FILTER_ID, CHART_COLUMN_ID, QUERY_ID, FILTER_OPERATION, CUF_ID) VALUES
(-4, 21, -22, 'EQUALS', NULL);

INSERT INTO CHART_FILTER_VALUES (FILTER_ID, FILTER_VALUE) VALUES
(-4, 'HIGH');


INSERT INTO CHART_MEASURE_COLUMN (CHART_COLUMN_ID, QUERY_ID, LABEL, MEASURE_OPERATION, MEASURE_RANK, CUF_ID) VALUES
(1, -22, '', 'COUNT', 0, NULL);

