@REM
@REM     This file is part of the Squashtest platform.
@REM     Copyright (C) Henix, henix.fr
@REM
@REM     See the NOTICE file distributed with this work for additional
@REM     information regarding copyright ownership.
@REM
@REM     This is free software: you can redistribute it and/or modify
@REM     it under the terms of the GNU Lesser General Public License as published by
@REM     the Free Software Foundation, either version 3 of the License, or
@REM     (at your option) any later version.
@REM
@REM     this software is distributed in the hope that it will be useful,
@REM     but WITHOUT ANY WARRANTY; without even the implied warranty of
@REM     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
@REM     GNU Lesser General Public License for more details.
@REM
@REM     You should have received a copy of the GNU Lesser General Public License
@REM     along with this software.  If not, see <http://www.gnu.org/licenses/>.
@REM


REM usage : <h2|mysql|postgresql> [-empty]

@rem Debugging : just remove/comment the next line.
@rem echo off

@rem Default variables.
set CHANGELOG_FILE=src/main/liquibase/sample/sample-db.changelog.xml
set DB_URL_PARAM=-Dliquibase.url=${dev.liquibase.url}

@rem Read the database profile. Add profile 'gen-dev' as a marker profile.
@rem If the profile is H2, force the url to the usual h2 url (in ../tm/data)

set PROFILE=%1,gen-dev
if "%1"=="h2" set DB_URL_PARAM=-Dliquibase.url=jdbc:h2:${project.basedir}/../tm/data/squash-tm
shift

@rem Check if the next param is '-empty'. If so change the changelog file and shift, otherwise do nothing.
set IS_EMPTY=%1
if "%IS_EMPTY%"=="-empty" (
    set CHANGELOG_FILE=src/main/liquibase/global.changelog-master.xml
    shift
)


@rem Now prepare the final argline. It includes the (possibly empty) db url, the database profile to use,
@rem and the other parameters of this script.
set ALL_PARAMS=-Dliquibase.dropFirst=true ^
            -Dliquibase.changeLogFile=%CHANGELOG_FILE% ^
            %DB_URL_PARAM% ^
            -P%PROFILE%



mvn org.liquibase:liquibase-maven-plugin:update %ALL_PARAMS%
