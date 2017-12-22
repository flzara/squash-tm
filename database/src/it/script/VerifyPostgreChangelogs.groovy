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
List.metaClass.collectWithIndex = { body ->
	def i = 0
	delegate.collect { body(it, i++) }
}

File.metaClass.collectLines

def root = new File("${pom.basedir}")
println root

def src = new File("src/main/liquibase/tm", root)

def changelogPattern = /tm\.changelog-(\d+)\.(\d+)\.(\d+)(-.+)?\.xml/

def forbiddenPattern = /"CHAR\(/

log.info("Applying syntax checks to database changelogs");

def errlog = src.listFiles()
	.findAll { // finds all changelogs
		it.name ==~ changelogPattern
		
	}.findAll { // filters out <= 1.11.0 which do not support postgresql
		def m = it.name =~ changelogPattern
		m.find()
		(Integer.valueOf(m.group(1)) * 10000 + Integer.valueOf(m.group(2)) * 100 + Integer.valueOf(m.group(3))) > 11100
		
	}.collect {
		// finds and filters out contets which contain forbidden regexp
		def log = it.readLines()
			.collectWithIndex { l, dx ->
				[dx: dx, err: (l =~ forbiddenPattern).find(), l: l]
			}.findAll { t -> t.err }
		
		[n: it.name, err: log.size() > 0, log: log]
	}.findAll {
		it.err
	}.inject([]) { memo, err -> // reduce to array of all faulty lines
		memo.addAll(err.log.collect{ [n: err.n, dx: it.dx, l: it.l] })
		memo
	}
	
errlog.each {
	log.error("PG syntax : Line ${it.dx}, file '${it.n}' : ${it.l}")
}
	
if (errlog.size() > 0) {
	fail("ERROR : Some changesets contain CHAR columns. Use VARCHAR instead for PostgreSQL compatibility")
}
