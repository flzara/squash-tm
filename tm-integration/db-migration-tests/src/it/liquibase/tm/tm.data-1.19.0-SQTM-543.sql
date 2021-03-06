

insert into BUGTRACKER (BUGTRACKER_ID, NAME, KIND, URL, AUTH_POLICY, AUTH_PROTOCOL)
values 	(10, 'mantis server', 'mantis', 'http://mantisbt.server.org', 'USER', 'BASIC_AUTH'),
		(20, 'redmine 3 server', 'redmine3', 'http://redmine.server.org', 'APP_LEVEL', 'BASIC_AUTH'),
		(30, 'jira server', 'jira.rest', 'http://jira.server.org', 'USER', 'OAUTH_1A');

insert into STORED_CREDENTIALS(CREDENTIAL_ID, ENC_VERSION, ENC_CREDENTIALS, AUTHENTICATED_SERVER, CONTENT_TYPE, AUTHENTICATED_USER)
values
	(-20, 1, '(encrypted content for redmine server)', 20, 'CRED', null),
	(-31, 1, '(encrypted content for jira serer (oauth tokens))', 30, 'CRED', null),
	(-32, 1, '(encrypted content for jira serer (oauth conf))', 30, 'CONF', null);
