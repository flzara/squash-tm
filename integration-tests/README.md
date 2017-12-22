Squash TM integration tests
===========================

This module runs integration tests

The command `mvn clean integration-tests` provisions a test database and runs the ITs.
One can skip db provisioning using the `mvn integration-tests -Ddatabase.nocreate` CLI.
By default, the tests run against a _h2_ db. To run against a _mysql_ or _postgresql_ db, use the CLI : 

    mvn clean integration-test -Pmysql,!h2.windows,!h2.unix
    mvn clean integration-test -Ppostgresql,!h2.windows,!h2.unix

**Note** : one requires **two** databases to run integration tests. both DB should be accessible through the same user with _DDL and DML grants_


Maven profiles
--------------

* default : `h2.windows` and `h2.unix`

* `verbose` : use this when you want Hibernate to vomit SQL. Most of the time it's useless and slows up the process 

* `h2.windows` : performs integration tests against _h2_ database configured for a windows environment. 
This profile is automatically activated on windows environments 

* `h2.unix` : performs integration tests against _h2_ database configured for a unix (linux + macos environment. 
This profile is automatically activated on windows environments 
	
* `mysql` : performs integration tests against _mysql_ It requires these properties : 
	* `it.database.url` : the test database url
	* `it.legacy.database.url` : the database url for "legacy" (non db-unit) tests
	* `it.database.schemaName` : the test database name
	* `it.database.username` : a user with DDL grants
	* `it.database.password` : the username's password

* `postgresql` : performs integration tests against _postgres_ It requires these properties : 
	* `it.database.url` : the test database url
	* `it.legacy.database.url` : the database url for "legavt" (non db-unit) tests
	* `it.database.username` : a user with DDL grants
	* `it.database.password` : the username's password


