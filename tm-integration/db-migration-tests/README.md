Squash TM Migration Tests
=========================

That module will run the database migration tests; ie tests that check 
that data that were affected by changes of their underlying table structure are still consistent and error-free. 

By default the tests will run with an embedded `h2` database but can also run with MySQL or Postgresql if their respective profiles are enabled.


Database profiles
-----------------

* `h2` : the default profile, will run the tests on H2. Also enabled by Ddatabase=h2
* `mysql` : run the tests on MySQL. Also enabled by -Ddatabase=mysql
* `postgresql` : guess. Also enabled by -Ddatabase=postgresql.



Environment configuration 
-------------------------

H2 doesn't need configuration configuration : as an embedded database it 
doesn't depend on your environment. However for MySQL/MariaDB or Postgresql, you need to configure the following maven properties (preferably in your settings.xml) :

	* `liquibase.url` : the test database url
	* `liquibase.username` : a user with DDL grants
	* `liquibase.password` : the username's password

The best place for that is your `$HOME/.m2/settings.xml`, one set for each database profile. 
