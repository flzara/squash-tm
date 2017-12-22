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
package org.squashtest.tm.domain.customreport

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.hibernate.Query
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
@DataSet("CustomReportLibraryNodeIT.xml")
class CustomReportLibraryNodeIT extends DbunitServiceSpecification {

	def "should find parent for not library nodes"(){
		given :

		when :
		CustomReportLibraryNode crln = findEntity(CustomReportLibraryNode.class, nodeID)
		TreeLibraryNode parent = crln.getParent();
		def parentId = parent.getId();

		then :
		parentId == expectedParentID

		where:

		nodeID		 	|| 	 expectedParentID
		-2L				||	 -1L
		-3L				||	 -2L
		-4L				||	 -2L
		-5L				||	 -2L
		-6L				||	 -2L
	}

	def "should find null parent for library nodes"(){
		given :

		when :
		CustomReportLibraryNode crln = findEntity(CustomReportLibraryNode.class, -1L)
		CustomReportLibraryNode parent = crln.getParent();

		then :
		parent==null;

	}

	def "should find childrens"(){
		given :

		when :
		CustomReportLibraryNode crln = findEntity(CustomReportLibraryNode.class, nodeID)
		def childrens = crln.getChildren();

		then :
		childrens.size() == expectedSize

		def ids = childrens.collect{
			it.getId()
		}

		ids as Set == expectedChildrensIDs as Set

		where:

		nodeID		 	|| 	 expectedChildrensIDs 	|	expectedSize
		-1L				||	 [-2L,-30L]				|	2
		-2L				||	 [-3L,-4L,-5L,-6L]			|	4

	}

	def "should add childrens"(){
		given :
		CustomReportLibrary crl = findEntity(CustomReportLibrary.class, -1L)

		when :
		CustomReportLibraryNode parentNode = findEntity(CustomReportLibraryNode.class, nodeID)
		def childrens = parentNode.getChildren();
		CustomReportLibraryNode newChild = new CustomReportLibraryNode()
		newChild.id = -7L
		newChild.library = crl
		newChild.parent = parentNode
		childrens.add(newChild)

		then :
		childrens.size() == expectedSize

		where:

		nodeID		 	|| 	 	expectedSize
		-1L				||	 	3
		-2L				||	 	5
		-3L				||		1

	}

	def "should find bound entity"(){
		given :


		when :
		CustomReportLibraryNode node = findEntity(CustomReportLibraryNode.class, nodeID)
		TreeEntity nodeEntity = node.getEntity();

		then :
		nodeEntity != null
		//checking polymorphic mapping with @any in CustomReportLibraryNode
		nodeEntity.getId()==expectedEntityID
		nodeEntity.getName()==expectedEntityName

		where:

		nodeID		 	|| 	 	expectedEntityID	|	expectedEntityName
		-1L				||	 			-1L			|		"project-1"
		-2L				||	 			-1L			|		"Folder1"
		-3L				||	 			-1L			|		"Chart1"
		-4L				||				-2L			|		"Chart2"
		-5L				||				-3L			|		"Chart3"
	}

	def "should find bound charts in dashboard"(){
		given :
		CustomReportDashboard crd = findEntity(CustomReportDashboard.class, -1L)

		when :
		def chartBindings = crd.getChartBindings()

		then :
		chartBindings.size()==3

	}


}
