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
version = System.properties["newVersion"]

if (!(version ==~ /(\d+\.){2}\d+/)) {
	throw new RuntimeException("System property 'newVersion' not set or not matching 'major.minor.micro' : ${version}")
}

namespace = "../src/main/liquibase"

newFilesDefs = [
// new tm changelog
"/tm/tm.changelog-${version}.xml" :
"""<?xml version="1.0" encoding="UTF-8"?>
<!--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

-->
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd">

    <!-- DO NOT FORGET TO UPDATE VERSION IN CORE_CONFIG ! -->
    <changeSet id="tm-${version}.00" author="gfouquet">
        <comment>Adds a TM database version number</comment>
        <update tableName="CORE_CONFIG">
            <column name="STR_KEY" value="squashtest.tm.database.version" />
            <column name="VALUE" value="1.1.0" />
            <where>STR_KEY = 'squashtest.tm.database.version'</where>
        </update>
    </changeSet>
</databaseChangeLog>""",    
// new tm upgrade changelog
"/tm/tm.changelog-upgrade-to-${version}.xml" :
"""<?xml version="1.0" encoding="UTF-8"?>
<!--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

-->
<databaseChangeLog
     xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog 
     http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd">

     <include file="tm.changelog-${version}.xml" relativeToChangelogFile="true" />
</databaseChangeLog>
""", 
// new global upgrade changelog
"/upgrade/upgrade.changelog-up-to-${version}.xml" :
"""<?xml version="1.0" encoding="UTF-8"?>
<!--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

-->
<databaseChangeLog
     xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog 
     http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd">

    <include file="../tm/tm.changelog-upgrade-to-${version}.xml" relativeToChangelogFile="true" />
    
</databaseChangeLog>
"""
	]

newFilesDefs.each { fileDef ->
	def file = new File(namespace + fileDef.key)
	println "Creating ${file.absolutePath} ..."
	file.withWriter { it << fileDef.value }
}

println "You have to manually modify : "
println "* sample-db.changelog.xml"
println "* tm.changelog-master.xml"
println "* tm.changelog-master.xml"
println "* tm.changelog-master.xml"
println "* global.changelog-incremental-updates.xml"