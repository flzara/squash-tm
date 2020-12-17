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


insert into BUGTRACKER (BUGTRACKER_ID, NAME, KIND, URL, AUTH_POLICY, AUTH_PROTOCOL)
values 	(10, 'mantis server', 'mantis', 'http://mantisbt.server.org', 'USER', 'BASIC_AUTH'),
		(20, 'redmine 3 server', 'redmine3', 'http://redmine.server.org', 'APP_LEVEL', 'BASIC_AUTH'),
		(30, 'jira server', 'jira.rest', 'http://jira.server.org', 'USER', 'OAUTH_1A');

insert into STORED_CREDENTIALS(CREDENTIAL_ID, ENC_VERSION, ENC_CREDENTIALS, AUTHENTICATED_SERVER, CONTENT_TYPE, AUTHENTICATED_USER)
values
	(-20, 1, '(encrypted content for redmine server)', 20, 'CRED', null),
	(-31, 1, '(encrypted content for jira serer (oauth tokens))', 30, 'CRED', null),
	(-32, 1, '(encrypted content for jira serer (oauth conf))', 30, 'CONF', null);
