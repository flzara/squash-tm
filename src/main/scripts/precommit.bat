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

echo pre-commit checks

@REM we only run licence check on commit, not qnew, rebase and so on
echo %HG_ARGS% | findstr /I commit

if %ERRORLEVEL% == 0 (
	echo ...license check
	mvn -o -q -DskipTests license:check
)
