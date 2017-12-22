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
package org.squashtest.tm.service.internal.customreport

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.chart.*
import org.squashtest.tm.domain.customreport.*
import org.squashtest.tm.domain.report.ReportDefinition
import org.squashtest.tm.domain.tree.TreeLibraryNode
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService
import org.squashtest.tm.service.internal.repository.CustomReportLibraryDao
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport
import org.springframework.orm.jpa.JpaTransactionManager

import javax.inject.Inject


@UnitilsSupport
@DataSet("CustomReportLibraryNodeServiceIT.sandboxCopyNode.xml")
@Transactional
class CustomReportLibraryNodeServiceCopyNodeIT extends DbunitServiceSpecification {

	@Inject
	CustomReportLibraryNodeService service;

	@Inject
	CustomReportLibraryNodeDao crlnDao;

	@Inject
	CustomReportLibraryDao crlDao;

	@Autowired
	private ApplicationContext applicationContext;



	def "should copy a folder and it's content"(){

		when:

		def result = service.copyNodes([-10L], -2L);
		em.flush()
		em.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		List<CustomReportLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 2

		//verify content order and ids
		childrens.get(0).getId() == -20L
		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		CustomReportFolder baseFolder = baseFolderNode.getEntity()
		baseFolderNode.getId() != -20L && -10L
		baseFolder.getId() != -1L

		//checking root baseFolder attributes
		baseFolderNode.getName().equals("folder1")
		baseFolderNode.getEntityType().equals(CustomReportTreeDefinition.FOLDER)
		baseFolderNode.getEntityId()==baseFolder.getId()
		baseFolderNode.getLibrary().getId() == -2L
		baseFolderNode.getParent().equals(targetFolderNode)
		baseFolder.getName().equals("folder1")
		baseFolder.getDescription().equals("un joli folder")
		baseFolder.getProject().getId() == -2L

		//checking childs of baseFolder, wich should have been copied with their parent
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()
		copiedChildrens.size().equals(4)


	}

	def "should copy a folder and check first folder child "(){
		when:
		def result = service.copyNodes([-10L], -2L);
		session.flush()
		session.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		List<TreeLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 2

		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()


		//checking first child, it should be a folder
		CustomReportLibraryNode childFolderNode = copiedChildrens.get(0)
		childFolderNode.getId() != -100L
		childFolderNode.getName().equals("folder1-1")
		childFolderNode.getParent().equals(baseFolderNode)
		CustomReportFolder childFolder = childFolderNode.getEntity()
		childFolder.getId() != -2L
		childFolder.getCustomReportLibrary().getId().equals(-2L)

	}

	def "should copy a folder and check dashboard child"(){
		when:
		def result = service.copyNodes([-10L], -2L);
		session.flush()
		session.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		List<CustomReportLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 2

		//verify content order and ids
		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()


		//checking second child, it should be a dashboard
		CustomReportLibraryNode childDashboardNode = copiedChildrens.get(1)
		childDashboardNode.getId() != -101L
		childDashboardNode.getName().equals("dashboard1")
		childDashboardNode.getParent().equals(baseFolderNode)
		CustomReportDashboard childDashboard = childDashboardNode.getEntity()
		childDashboard.getId() != -1L
		childDashboard.getCustomReportLibrary().getId().equals(-2L)
		childDashboard.getChartBindings().size() == 1
		CustomReportChartBinding binding = childDashboard.getChartBindings().getAt(0)
		binding.getId()!=-1L
		binding.getChart().getId() == -1L


	}

	def "should copy a folder and check chart def child"(){
		when:
		def result = service.copyNodes([-10L], -2L);
		session.flush()
		session.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		List<CustomReportLibraryNode> childrens = targetFolderNode.getChildren()

		//verify content order and ids
		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()

		//checking chart copy
		ChartDefinition originalchart = findEntity(ChartDefinition.class,-1L)
		CustomReportLibraryNode childChartNode = copiedChildrens.get(2)
		childChartNode.getId() != -102L
		childChartNode.getName().equals("chart1")
		childChartNode.getParent().equals(baseFolderNode)
		ChartDefinition childChart = childChartNode.getEntity()
		childChart.getId() != -1L
		childChart.getCustomReportLibrary().getId().equals(-2L)
		ChartQuery chartQuery = childChart.getQuery()
		chartQuery.id != originalchart.getQuery().id


		//checking that all linked entities are properly copied and not the original one relinked
		!childChart.getAxis().containsAll(originalchart.getAxis())
		!childChart.getFilters().containsAll(originalchart.getFilters())
		!childChart.getMeasures().containsAll(originalchart.getMeasures())

		AxisColumn copiedColumn = childChart.getAxis().get(0)
		AxisColumn originalColumn = originalchart.getAxis().get(0)
		copiedColumn.getColumn().getId().equals(originalColumn.getColumn().getId())

		MeasureColumn copiedMeasure = childChart.getMeasures().get(0)
		MeasureColumn originalMeasure = originalchart.getMeasures().get(0)
		copiedMeasure.getColumn().getId().equals(originalMeasure.getColumn().getId())

		Filter copiedFilter = childChart.getFilters().get(0)
		Filter originalFilter = originalchart.getFilters().get(0)
		copiedFilter.getColumn().equals(originalFilter.getColumn())
		copiedFilter.getValues().containsAll(originalFilter.getValues())
	}

	def "should copy a folder and check report def child"(){
		when:
		def result = service.copyNodes([-10L], -2L);
		em.flush()
		em.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		List<CustomReportLibraryNode> childrens = targetFolderNode.getChildren()

		//verify content order and ids
		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()

		//checking chart copy
		ReportDefinition originalReport = findEntity(ReportDefinition.class,-1L)
		CustomReportLibraryNode childReportNode = copiedChildrens.get(3)
		childReportNode.getId() != -103L
		childReportNode.getName().equals("report1")
		childReportNode.getParent().equals(baseFolderNode)
		ReportDefinition childReport = childReportNode.getEntity()
		childReport.getId() != -1L
		childReport.getPluginNamespace().equals("my plugin")
		childReport.getCustomReportLibrary().getId().equals(-2L)

	}

	def "should copy a folder and sub tree recursively"(){
		when:
		def result = service.copyNodes([-11L], -2L);
		session.flush()
		session.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		List<CustomReportLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 2

		//verify content order and ids
		childrens.get(0).getId() == -20L
		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		CustomReportFolder baseFolder = baseFolderNode.getEntity()

		//checking root baseFolder attributes
		baseFolderNode.getName().equals("folder2-Copie1")
		baseFolder.getName().equals("folder2-Copie1")

		//checking hierarchy
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()
		copiedChildrens.size() == 1
		CustomReportLibraryNode node = copiedChildrens.get(0).getChildren().get(0).getChildren().get(0)
		node.getName().equals("dashboard1")
		node.getEntityType().equals(CustomReportTreeDefinition.DASHBOARD)
		node.getEntity().getId() != -2L
		node.getParent().getParent().getParent().getId().equals(baseFolderNode.getId())

	}

	def "should move a folder and it's content"(){
		when:
		service.moveNodes([-10L], -2L);
		session.flush()
		session.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		CustomReportLibraryNode originalParentNode = findEntity(CustomReportLibraryNode.class,-1L)
		List<CustomReportLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 2

		//verify content order and ids
		childrens.get(0).getId() == -20L
		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		CustomReportFolder baseFolder = baseFolderNode.getEntity()
		baseFolderNode.getId() == -10L
		baseFolder.getId() == -1L
		baseFolderNode.getParent().equals(targetFolderNode)
		originalParentNode.getChildren().size()==1
		!originalParentNode.getChildren().contains(baseFolderNode)

		//checking root baseFolder attributes
		baseFolderNode.getName().equals("folder1")
		baseFolderNode.getEntityType().equals(CustomReportTreeDefinition.FOLDER)
		baseFolderNode.getEntityId()==baseFolder.getId()
		baseFolderNode.getLibrary().getId() == -2L
		baseFolderNode.getParent().equals(targetFolderNode)
		baseFolder.getName().equals("folder1")
		baseFolder.getDescription().equals("un joli folder")
		baseFolder.getProject().getId() == -2L

		//checking children of baseFolder, witch should have been copied with their parent
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()
		copiedChildrens.size().equals(4)
	}

	def "should move a folder and check folder child"(){
		when:
		service.moveNodes([-10L], -2L);
		session.flush()
		session.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		CustomReportLibraryNode originalParentNode = findEntity(CustomReportLibraryNode.class,-1L)
		List<CustomReportLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 2
		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		CustomReportFolder baseFolder = baseFolderNode.getEntity()

		//checking children of baseFolder, witch should have been copied with their parent
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()
		copiedChildrens.size().equals(4)

		CustomReportLibraryNode childFolderNode = copiedChildrens.get(0)
		childFolderNode.getId() == -100L
		childFolderNode.getName().equals("folder1-1")
		childFolderNode.getParent().equals(baseFolderNode)
		childFolderNode.getLibrary().getId()== -2L
		CustomReportFolder childFolder = childFolderNode.getEntity()
		childFolder.getId() == -2L
		childFolder.getCustomReportLibrary().getId().equals(-2L)
	}

	def "should move a folder and check dashboard child"(){
		when:
		service.moveNodes([-10L], -2L);
		session.flush()
		session.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		List<CustomReportLibraryNode> childrens = targetFolderNode.getChildren()
		childrens.size() == 2

		//verify content order and ids
		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()


		//checking second child, it should be a dashboard
		CustomReportLibraryNode childDashboardNode = copiedChildrens.get(1)
		childDashboardNode.getId() == -101L
		childDashboardNode.getName().equals("dashboard1")
		childDashboardNode.getParent().equals(baseFolderNode)
		CustomReportDashboard childDashboard = childDashboardNode.getEntity()
		childDashboard.getId() == -1L
		childDashboard.getCustomReportLibrary().getId().equals(-2L)
		childDashboard.getChartBindings().size() == 1
		CustomReportChartBinding binding = childDashboard.getChartBindings().getAt(0)
		binding.getId()==-1L
		binding.getChart().getId() == -1L
	}

	def "should move a folder and check chart def child"(){
		when:
		service.moveNodes([-10L], -2L);
		session.flush()
		session.clear()

		then:
		CustomReportLibraryNode targetFolderNode = findEntity(CustomReportLibraryNode.class,-2L)
		List<CustomReportLibraryNode> childrens = targetFolderNode.getChildren()

		//verify content order and ids
		CustomReportLibraryNode baseFolderNode = childrens.get(1)
		List<CustomReportLibraryNode> copiedChildrens = baseFolderNode.getChildren()

		//checking chart copy
		ChartDefinition originalchart = findEntity(ChartDefinition.class,-1L)
		CustomReportLibraryNode childChartNode = copiedChildrens.get(2)
		childChartNode.getId() == -102L
		childChartNode.getName().equals("chart1")
		childChartNode.getParent().equals(baseFolderNode)
		ChartDefinition childChart = childChartNode.getEntity()
		childChart.getId() == -1L
		childChart.getCustomReportLibrary().getId().equals(-2L)
		ChartQuery chartQuery = childChart.getQuery()
		chartQuery.id == originalchart.getQuery().id


		//checking that all linked entities are properly copied and not the original one relinked
		childChart.getAxis().containsAll(originalchart.getAxis())
		childChart.getFilters().containsAll(originalchart.getFilters())
		childChart.getMeasures().containsAll(originalchart.getMeasures())

		AxisColumn copiedColumn = childChart.getAxis().get(0)
		AxisColumn originalColumn = originalchart.getAxis().get(0)
		copiedColumn.getColumn().getId().equals(originalColumn.getColumn().getId())

		MeasureColumn copiedMeasure = childChart.getMeasures().get(0)
		MeasureColumn originalMeasure = originalchart.getMeasures().get(0)
		copiedMeasure.getColumn().getId().equals(originalMeasure.getColumn().getId())

		Filter copiedFilter = childChart.getFilters().get(0)
		Filter originalFilter = originalchart.getFilters().get(0)
		copiedFilter.getColumn().equals(originalFilter.getColumn())
		copiedFilter.getValues().containsAll(originalFilter.getValues())
	}

}
