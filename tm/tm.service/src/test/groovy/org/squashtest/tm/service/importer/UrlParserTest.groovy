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
package org.squashtest.tm.service.importer


import org.squashtest.tm.service.internal.importer.UrlParser;

import spock.lang.Specification;


class UrlParserTest extends Specification{
	def "should parse simple path"(){
		
		given :
			String path = "name1/name2/name3"
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
		
		then :
			folderNames.get(0) == "name1"
			folderNames.get(1) == "name2"
			folderNames.get(2) == "name3"		
		
	}
	def "should parse simple path2"(){
		
		given :
			String path = "/name1/name2/name3"
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
			
		
		then :
			folderNames.get(0) == "name1"
			folderNames.get(1) == "name2"
			folderNames.get(2) == "name3"
		
	}
	def "should parse path and trim"(){
		
		given :
			String path = "/   name1  /  name2 / name3   "
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
			
		
		then :
			folderNames.get(0) == "name1"
			folderNames.get(1) == "name2"
			folderNames.get(2) == "name3"
		
	}
	
	def "should parse path and echap double slash"(){
		
		given :
			String path = "/name1//endName1/name2/name3"
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
			
		
		then :
			folderNames.get(0) == "name1/endName1"
			folderNames.get(1) == "name2"
			folderNames.get(2) == "name3"
		
	}
	def "should parse path and echap triple slash"(){
		
		given :
			String path = "/name1///name2///name3///"
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
			
		
		then :
			folderNames.get(0) == "name1/"
			folderNames.get(1) == "name2/"
			folderNames.get(2) == "name3/"
		
	}
	def "should parse path and echap quadruple slash"(){
		
		given :
			String path = "/name1////enName1/name2/name3/"
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
			
		
		then :
			folderNames.get(0) == "name1//enName1"
			folderNames.get(1) == "name2"
			folderNames.get(2) == "name3"
		
	}
	def "should parse path and echap double slash taking into account space characters"(){
		
		given :
			String path = "/name1/ //name2/name3/"
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
			
		
		then :
			folderNames.get(0) == "name1"
			folderNames.get(1) == "/name2"
			folderNames.get(2) == "name3"
		
	}
	def "should parse and add name with spaces"(){
		
		given :
			String path = "/name 1/name2/name3/"
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
			
		
		then :
			folderNames.get(0) == "name 1"
			folderNames.get(1) == "name2"
			folderNames.get(2) == "name3"
		
	}
	def "should parse and not add empty string because of spaces at begining and end of path"(){
		
		given :
			String path = "    /name1/name2/name3/   "
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
			
		
		then :
			folderNames.get(0) == "name1"
			folderNames.get(1) == "name2"
			folderNames.get(2) == "name3"
		
	}
	def "should parse and add empty string"(){
		
		given :
			String path = "/name1/     /name2/  name3/"
		
		when :
		
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path)
			
		
		then :
			folderNames.get(0) == "name1"
			folderNames.get(1) == ""
			folderNames.get(2) == "name2"
			folderNames.get(3) == "name3"
			
		}

}
