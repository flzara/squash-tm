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
import java.util.List;

import groovy.io.FileType
import groovy.text.GStringTemplateEngine;



def filetemplate = """
/**
* smartsprites directive :
*/

/** sprite: sprites-icons; sprite-image: url('../images/sprites-icons.png'); sprite-layout: vertical */

/**
* defining the raw classes 
*
*/

<% filenames.each { name -> %>
.sq-icon-<%=name%> {
	background-image : url(../images/icon-lib/<%=name%>.png); /** sprite-ref: sprites-icons; */
	background-repeat : no-repeat;
}

<% } %>

/**
 * defining the tree classes
 *
 */

<% filenames.each { name -> %>
li[rel="requirement"][category-icon="<%=name%>"] > a > .jstree-icon{
	background-image: url(../images/icon-lib/<%=name%>.png); /** sprite-ref: sprites-icons; */
}

<% } %>
 
"""


// get path of the script
def scriptPath = getClass().protectionDomain.codeSource.location.path
// get parent path
def scriptDir = scriptPath.substring(0, scriptPath.lastIndexOf("/"))
// get parent path again
def webappDir = scriptDir.substring(0, scriptDir.lastIndexOf("/"))
// add to the path the icon path of icon-lib
def iconDir= webappDir + "/images/icon-lib"


def list = []
def dir = new File(iconDir)
// get all .png file in the directory
dir.eachFileRecurse (FileType.FILES) { file ->
	if(file.name.endsWith('.png')) {
		list << file
	}
}

//name of the generated css
def fileName = scriptDir + '/icon-lib-item-icon.css'

//CSS file
File f = new File(fileName)
boolean isNew = f.createNewFile()
if(!isNew){
	f.delete()
	println("delete existing cssfile")
}
println("create new css file : "+fileName)
f = new File(fileName)


/*
 * generating the file
 */
def engine = new GStringTemplateEngine()
def template = engine.createTemplate(filetemplate)
def writer = new StringWriter()

def model = [
	filenames : list.collect { 
		zefile -> 
		def name = zefile.getName()
		name
		.lastIndexOf('.')
		.with {
			it != -1 ? 
				name[0..<it] : 
				name
		} 
	}	
]

template.make(model).writeTo (writer)
writer.flush()

f.append (writer.toString())



// get main dir
def mainDir =  webappDir.substring(0,  webappDir.lastIndexOf("/"))
def javaDir =  mainDir + "/java/org/squashtest/tm/web/internal/util/"
def javaFileName = javaDir + "IconLibrary.java"

File javaFile = new File(javaFileName)
boolean isJavaFileNew = javaFile.createNewFile()
if(!isJavaFileNew){
	javaFile.delete()
	println("delete existing java file")
}
println("create new java file : "+ javaFileName)
javaFile = new File(javaFileName)

javaFile.append('package org.squashtest.tm.web.internal.util;\n import java.util.Arrays;\n import java.util.List;\n\n public class IconLibrary {\n');
javaFile.append('private static List<String> ICON_NAMES = Arrays.asList(');
list.each {
	
	def name = it.getName()
	def nameWithOutExt = name.lastIndexOf('.').with {it != -1 ? name[0..<it] : name}
	if ( nameWithOutExt != "noicon"){
	javaFile.append('"')
	javaFile.append('sq-icon-')
	javaFile.append(nameWithOutExt)
	javaFile.append('"')
	if(it != list.last()) {
	javaFile.append(',')
	
 }
	}
	
}	
javaFile.append(');\n')
javaFile.append('public static List<String> getIconNames() {\n')	
javaFile.append('return ICON_NAMES;\n')
javaFile.append('}\n')
javaFile.append('}')




	
	


