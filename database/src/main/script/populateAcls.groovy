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
import org.apache.ivy.ant.AddPathTask;

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

def close() {
    sql.close()

    println "Connection closed"
}

def withTransaction(Closure c) {
    sql.withTransaction c
}

def create() {
    return [
        classReference: { classReference it }, 
        objectIdentity: { objectIdentity it }, 
        aclGroup: { aclGroup it }
    ]
}

def classReference(String type) {
    sql.execute "insert into ACL_CLASS (CLASSNAME) values (?)", [
        type
    ]
    
    println "Inserted class type"
}

def objectIdentity(Map args) {
    sql.execute "insert into ACL_OBJECT_IDENTITY (IDENTITY, CLASS_ID) values (?, (select ID from ACL_CLASS where CLASSNAME = ?))", [
        args.identity,
        args.classname
    ]

    println "Inserted Object Identity $args.identity of class $args.classname" 
}

def user(String login) {
    [ has: { permissions(login, it.groupOfPermissions) } ]
}

def permissions(String login, String groupCode) {
    [ on: { definePermissionsOnObject(login, groupCode, it.object) } ]            
}

def definePermissionsOnObject(def login, def groupCode, def objectIdentity) {
    sql.execute "insert into ACL_RESPONSIBILITY_SCOPE_ENTRY (USER_ID, ACL_GROUP_ID, OBJECT_IDENTITY_ID) values ((select ID from CORE_USER where login = ?), (select ID from ACL_GROUP where QUALIFIED_NAME = ?), (select ID from ACL_OBJECT_IDENTITY where IDENTITY = ?))", [
        login,
        groupCode,
        objectIdentity
    ]

    println "Inserted Responsibility Scope Entry for user $login on object $objectIdentity, resp $groupCode " 
}

def aclGroup(def name) {
    def res = sql.executeInsert("insert into ACL_GROUP (QUALIFIED_NAME) values (?)", [
        name
    ])
    
    def groupId  = res[0][0]

    println "Inserted Acl Group $name with id $groupId" 

    [ add: { addIt-> [ to: { toIt -> addPermissionToGroup(groupId, addIt.permission , toIt.classname) } ] } ]    
}

def addPermissionToGroup(groupId, permission, classname) {
    def res = sql.executeInsert("insert into ACL_GROUP_PERMISSION (ACL_GROUP_ID, PERMISSION_MASK, PERMISSION_ORDER, CLASS_ID) values (?, ?, 0, (select ID from ACL_CLASS where CLASSNAME = ?))", [
        groupId,
        permission,
        classname
    ])

    println "Inserted Permission $permission on type $classname for group $groupId" 

    [ and: { andIt-> [to: { toIt -> addPermissionToGroup(groupId, andIt.permission , toIt.classname) }] } ]    
}
// replace with db url and so on
open database: "jdbc:mysql://192.168.2.27:3306/squashtest_liq", user: "liquibase", password: "_liquibase", driver: "com.mysql.jdbc.Driver"
//open database: "jdbc:h2:C:/workspaces/11-01-07/squashtest-csp/provision/target/dev-database/squashtest", user: "sa", password: "sa", driver: "org.h2.Driver"

try {
    withTransaction {
        create() classReference "squashtest.Project"
        create() classReference "squashtest.Bidule"
        
        create() objectIdentity identity: 10, classname: "squashtest.Project"
        create() objectIdentity identity: 20, classname: "squashtest.Project"
        create() objectIdentity identity: 30, classname: "squashtest.Project"
        create() objectIdentity identity: 40, classname: "squashtest.Bidule"
        
        create() aclGroup "TEST_CREATOR" add permission: 3 to classname: "squashtest.Project" and permission: 4 to classname: "squashtest.Bidule"
         
        user "peter.parker" has groupOfPermissions: "TEST_CREATOR" on object:10
        user "peter.parker" has groupOfPermissions: "TEST_CREATOR" on object:20
        user "bruce.wayne" has groupOfPermissions: "TEST_CREATOR" on object:10
        user "bruce.wayne" has groupOfPermissions: "TEST_CREATOR" on object:30
    }
} finally {
    close()
}
