#! /bin/bash
#
#     This file is part of the Squashtest platform.
#     Copyright (C) Henix, henix.fr
#
#     See the NOTICE file distributed with this work for additional
#     information regarding copyright ownership.
#
#     This is free software: you can redistribute it and/or modify
#     it under the terms of the GNU Lesser General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
#
#     this software is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU Lesser General Public License for more details.
#
#     You should have received a copy of the GNU Lesser General Public License
#     along with this software.  If not, see <http://www.gnu.org/licenses/>.
#


#
# usage : gen-db <h2|mysql|postgresql> [-empty]
#

# Initialize the default variables

if [ $# -lt 1 ]
then
	echo "usage : gen-db <h2|mysql|postgresql> [-empty]"
	echo "Also, you need to configure the property 'dev.liquibase.url' in your $HOME/.m2/settings.xml"
	exit
fi

CHANGELOG_FILE="src/main/liquibase/sample/sample-db.changelog.xml"
DB_URL_PARAM="-Dliquibase.url=\${dev.liquibase.url}"


# Read the database profile. Add the profile 'gen-dev' as a marker profile.
# If the profile is H2, force the url to the usual h2 url (in ../tm/data)

PROFILE=$1,gen-dev
if [ "$1" ==  "h2" ]
then
	DB_URL_PARAM=-Dliquibase.url="jdbc:h2:\${project.basedir}/../tm/data/squash-tm"
fi
shift


# Check if the next param is '-empty'. If so change the changelog file and shift, otherwise do nothing.
IS_EMPTY=$1
if [ "$IS_EMPTY" == "-empty" ]
then
	CHANGELOG_FILE="src/main/liquibase/global.changelog-master.xml"
fi


# Now prepare the final argline. It inclues the (possibly empty) db url, the database profile to use, and the 
# other parameters of this script.

ALL_PARAMS="-Dliquibase.dropFirst=true -Dliquibase.changeLogFile=$CHANGELOG_FILE $DB_URL_PARAM -P$PROFILE"

echo "$ALL_PARAMS"
echo
echo
echo

mvn  org.liquibase:liquibase-maven-plugin:update $ALL_PARAMS



