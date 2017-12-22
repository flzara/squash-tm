Squash TM provsion
==================

This module is used by developper to "provision eclipse container", so that they can start the app in eclipse
It both creates a database and collects config and jars required to run the app

Maven profiles
--------------

* default : provisions a h2 database

* `mysql` : provisions a mysql database. It requires these properties :
	* `dev.database.url` : the test database url
	* `dev.database.username` : a user with DDL grants
	* `dev.database.password` : the username's password
	
* `postgresql` : provisions a postgresql database. It requires these properties :
	* `dev.database.url` : the test database url
	* `dev.database.username` : a user with DDL grants
	* `dev.database.password` : the username's password
	

Maven properties
----------------

* `-Ddb.emptycontent=true` : will create a database with no mock user data