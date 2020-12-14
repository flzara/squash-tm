Squash TM integration tests
===========================

This module runs integration tests. It requires a database to run. The database schema will be populated during the phase pre-integration-test (unless explicitly skipped). By default the tests will run with an embeded H2 unless an alternate database profile is enabled (see Database profiles). 

In order to run with an alternate database you need to configure several elements in your environment :
* a database server (obviously), 
* the schema must exist (empty schema is fine),
* some maven properties must be set (see Environment configuration).


Database profiles
------------------

* `h2` : the default profile, will run the tests on H2. Also enabled by Ddatabase=h2
* `mysql` : run the tests on MySQL. Also enabled by -Ddatabase=mysql
* `postgresql` : guess. Also enabled by -Ddatabase=postgresql.


Other profiles
--------------

* `skip-database` : the database will not be generated. It's quite common that the database hasn't changed (structurally) a bit between two runs of integration tests, so you can save time by skipping the database creation. Also enabled by -Ddatabase.nocreate=true
* `verbose` : makes Hibernate log its SQL.
* `integration` : that profile is intended for the CI server and is of little interest to the developpers. Also enabled by -Dstage=integration



Environment configuration 
-------------------------

H2 doesn't need environment configuration : as an embedded database it 
doesn't depend on your environment. However for MySQL/MariaDB or Postgresql, you need to configure the following maven properties (preferably in your settings.xml) :

	* `liquibase.url` : the test database url
	* `liquibase.username` : a user with DDL grants
	* `liquibase.password` : the username's password

The best place for that is your `$HOME/.m2/settings.xml`, one set for each database profile. 



