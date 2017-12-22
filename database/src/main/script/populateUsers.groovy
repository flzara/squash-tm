/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
@GrabConfig(systemClassLoader=true)
// replace below by the required driver (it's a maven artifact description)
@Grab(group='com.h2database', module='h2', version='1.3.154')
@Grab(group='mysql', module='mysql-connector-java', version='5.1.16')

// then go at the end of this script and modify it

import groovy.sql.Sql
import java.security.MessageDigest

Sql sql

def open(Map args) {
	sql = Sql.newInstance(args.database, args.user, args.password, args.driver)

	println "Opened connection to '$args.database'"
}

def create(Map args) {
	sql.execute "insert into CORE_USER (LOGIN, FIRST_NAME, LAST_NAME) values (?, ?, ?)", [
		args.user,
		args.firstName,
		args.lastName
	]

	println "User '$args.user' created"
}

def add(String login) {
	[to: {to login, it }]
}

def to(String login, Map args) {
	sql.execute "insert into CORE_GROUP_MEMBER (USER_ID, GROUP_ID) values ((select ID from CORE_USER where LOGIN=?), (select ID from CORE_GROUP where QUALIFIED_NAME=?))", [
		login,
		args.group
	]

	println "User '$login' added to group $args.group"
}

def close() {
	sql.close()

	println "Connection closed"
}

def withTransaction(Closure c) {
	sql.withTransaction c
}

// replace with db url and so on
//open database: "jdbc:h2:C:/workspaces/11-01-07/squashtest-csp/provision/target/dev-database/squashtest", user: "sa", password: "sa", driver: "org.h2.Driver"
open database: "jdbc:mysql://192.168.2.27:3306/squashtest_liq", user: "liquibase", password: "_liquibase", driver: "com.mysql.jdbc.Driver"

try {
	withTransaction {
		// add whichever users you need
		create user: "emma.frost", firstName: "Emma", lastName: "Frost"
		add("emma.frost").to group: "squashtest.authz.group.core.Admin"

        create user: "bruce.wayne", firstName: "Bruce", lastName: "The Goddamn Batman"
        create user: "peter.parker", firstName: "Peter", lastName: "Parker"
	}
} finally {
	close()
}
