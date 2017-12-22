<?xml version="1.0" encoding="ISO-8859-1"?>
<!--

        This file is part of the Squashtest platform.
        Copyright (C) Henix, henix.fr

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
<xsl:stylesheet version="2.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:lb="http://www.liquibase.org/xml/ns/dbchangelog">

	<xsl:output method="xml" indent="yes"/>
	

	<xsl:template match="lb:comment">
		<!-- for some reason if you don't specify how to handle <comment> or else tags, saxon will print them
			by default. So we need to explain it that it should catch and ignore them unless we told him to.
		-->
	</xsl:template>



	
	
	<!-- recursively check for renamed table, then prints the final name when it's over-->
	<xsl:template name="print-final-table-name">
		<xsl:param name="renamedTables"/>
		<xsl:param name="finalName"/>
		<xsl:choose>
			<xsl:when test="$renamedTables[@oldTableName=$finalName]">
				<xsl:call-template name="print-final-table-name">
					<xsl:with-param name="renamedTables" select="$renamedTables"/>
					<xsl:with-param name="finalName" select="$renamedTables[@oldTableName=$finalName]/@newTableName"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<h2><xsl:value-of select="$finalName"/></h2>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
	one create the column only if it hasn't been deleted subsequently
	hence the variable 'droppedColumns'
	
	-->	
	<xsl:template match="*/lb:column">
		<xsl:param name="droppedColumns"/>
		<xsl:if test="not($droppedColumns[@columnName=current()/@name])">
		<li>
			<label class="column-descr"><xsl:value-of select="@name" /> : </label>
			<p class="column-descr"><xsl:value-of select="@remarks" /></p>
		</li> 
		</xsl:if>
	</xsl:template>
	
	
	<!-- 
		items (tables) displayed here will exist according to :
		
			- whether the table has been deleted afterward or not,
			- or renamed, 
			- same goes for columns.
	-->
	<xsl:template match="lb:createTable">
	
		<xsl:param name="droppedColumns"/>
		<xsl:param name="addedColumns"/>
		<xsl:param name="droppedTables"/>
		<xsl:param name="renamedTables"/>
	
	<xsl:if test="not($droppedTables[@tableName=current()/@tableName])">
	
		<xsl:call-template name="print-final-table-name">
			<xsl:with-param name="renamedTables" select="$renamedTables"/>
			<xsl:with-param name="finalName" select="@tableName"/>
		</xsl:call-template>
		
		<xsl:apply-templates select="lb:comment" />

		<label class="table-descr">description : </label>
		<p class="table-descr">
			<xsl:value-of select="preceding-sibling::lb:comment[1]"/>
		</p>
		
		<h3 class="table-column">Colonnes</h3>
		
		<ul>
			<xsl:apply-templates select="./lb:column | $addedColumns[@tableName=current()/@tableName]/lb:column">
				<xsl:with-param name="droppedColumns" select="$droppedColumns[@tableName=current()/@tableName]"/>
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
		</ul>

		<hr/>
	</xsl:if>
		
	</xsl:template>
	


	<xsl:template match="/">

		<html>

		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		
		  <TITLE>dictionaire donnees Squash TM version ${project.version}</TITLE>
		  
		  <style type="text/css">
			label.table-descr {
				font-weight:bold;
			}
			
			p.table-descr {
				text-align:justify;
			}
			
			h3.table-column{
			}
			
			label.column-descr{
				font-weight:bold;
			}
			
			p.column-descr{
				text-align:justify;
			}
			
			
		  </style>

		</head>

		<body BGCOLOR="#FFFFFF">
		<h1>Squash Tm version ${project.version} : Dictionnaire de donnees</h1>		
		<!--<xsl:variable name="liquiFiles" select="collection('file:/D:/bsiri/helios_workspace/squashtest-csp/database/src/main/liquibase/tm/?select=*.xml')">			
			</xsl:variable>
		-->
 			<xsl:variable name="liquiFiles" select="collection('../../../../../database/src/main/liquibase/tm/?select=*.xml')"></xsl:variable>
			
			
			
			<xsl:variable name="addedColumns" />
			
			<xsl:for-each select="$liquiFiles//lb:createTable">
				<xsl:sort select="@tableName" />
				<xsl:apply-templates select=".">
					<xsl:with-param name="droppedColumns" select="$liquiFiles//lb:dropColumn"/>
					<xsl:with-param name="addedColumns" select="$liquiFiles//lb:addColumn"/>
					<xsl:with-param name="droppedTables" select="$liquiFiles//lb:dropTable"/>
					<xsl:with-param name="renamedTables" select="$liquiFiles//lb:renameTable"/>
				</xsl:apply-templates>
			</xsl:for-each>
			


		</body>

		</html>

	</xsl:template >



</xsl:stylesheet>