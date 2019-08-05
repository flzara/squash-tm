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
package org.squashtest.tm.service.internal.chart;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.chart.ChartSeries;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.query.QQueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.service.chart.ChartModificationService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.query.QueryProcessingServiceImpl;
import org.squashtest.tm.service.internal.repository.CustomChartDefinitionDao;
import org.squashtest.tm.service.query.ConfiguredQuery;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Transactional
@Service("squashtest.tm.service.ChartModificationService")
public class ChartModificationServiceImpl implements ChartModificationService {

	@PersistenceContext
	private EntityManager em;

	@Inject
	private Provider<ChartToConfiguredQueryConverter> converterProvider;

	@Inject
	private QueryProcessingServiceImpl dataFinder;

	@Inject
	private CustomChartDefinitionDao chartDefinitionDao;

	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;

	@Inject
	private Provider<TupleProcessor> tupleProcessorProvider;

	@Override
	public void persist(ChartDefinition newChartDefinition) {
		session().persist(newChartDefinition);
	}

	@Override
	public ChartDefinition findById(long id) {
		return (ChartDefinition) session().get(ChartDefinition.class, id);
	}


	@Override
	public boolean hasChart(List<Long> userIds) {
		return chartDefinitionDao.hasChart(userIds);
	}


	@Override
	public Map<EntityType, Set<QueryColumnPrototype>> getColumnPrototypes() {

		JPAQueryFactory factory = new JPAQueryFactory(em);
		QQueryColumnPrototype prototype = QQueryColumnPrototype.queryColumnPrototype;

		Map<EntityType, Set<QueryColumnPrototype>> prototypes;

		prototypes = factory.from(prototype).where(prototype.business.eq(true)).orderBy(prototype.id.asc())
			.transform(groupBy(prototype.specializedType.entityType).as(set(prototype)));

		return prototypes;
	}


	@Override
	public void update(ChartDefinition chartDef) {
		session().saveOrUpdate(chartDef);
	}


	@Override
	public ChartInstance generateChart(long chartDefId, List<EntityReference> dynamicScope, Long dashboardId) {
		ChartDefinition def = findById(chartDefId);
		return generateChart(def, dynamicScope, dashboardId);
	}

	@Override
	public ChartInstance generateChart(ChartDefinition chartDefinition, List<EntityReference> dynamicScope, Long dashboardId) {

		ChartToConfiguredQueryConverter converter = converterProvider.get();

		ConfiguredQuery configuredQuery = converter.withDefinition(chartDefinition)
			.forDynamicScope(dynamicScope)
			.forDashboard(dashboardId)
			.convert();

		return generateChart(chartDefinition, configuredQuery);
	}

	@Override
	public ChartInstance generateChart(ChartDefinition chartDef, Long projectId) {
		if (chartDef.getProject() == null) {
			Project project = em.find(Project.class, projectId);
			chartDef.setProject(project);
		}

		ChartToConfiguredQueryConverter converter = converterProvider.get();

		ConfiguredQuery configuredQuery = converter.withDefinition(chartDef)
			.convert();

		return generateChart(chartDef, configuredQuery);
	}


	private Session session() {
		return em.unwrap(Session.class);
	}

	@Override
	@PreAuthorize("hasPermission(#definition.id, 'org.squashtest.tm.domain.chart.ChartDefinition' ,'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public void updateDefinition(ChartDefinition definition, Long id) {
		ChartDefinition oldDef = customReportLibraryNodeService.findChartDefinitionByNodeId(id);
		definition.setProject(oldDef.getProject());
		((AuditableMixin) definition).setCreatedBy(((AuditableMixin) oldDef).getCreatedBy());
		((AuditableMixin) definition).setCreatedOn(((AuditableMixin) oldDef).getCreatedOn());
		//rename if needed without forgot to rename the node.
		if (!definition.getName().equals(oldDef.getName())) {
			CustomReportLibraryNode node = customReportLibraryNodeService.findNodeFromEntity(oldDef);
			node.renameNode(definition.getName());
		}
		/*session().flush();
		session().clear();*/
		session().merge(definition);
	}

	@Override
	public ChartInstance generateChartForMilestoneDashboard(ChartDefinition chart, Long milestoneId, Workspace workspace) {

		ChartToConfiguredQueryConverter converter = converterProvider.get();

		ConfiguredQuery configuredQuery = converter.withDefinition(chart)
			.forMilestone(milestoneId)
			.forWorkspace(workspace)
			.convert();

		return generateChart(chart, configuredQuery);
	}

	@Override
	public ChartInstance generateChartInMilestoneMode(ChartDefinition chart, List<EntityReference> scope, Workspace workspace) {
		ChartToConfiguredQueryConverter converter = converterProvider.get();

		ConfiguredQuery configuredQuery = converter.withDefinition(chart)
			.forCurrentActiveMilestone()
			.forWorkspace(workspace)
			.convert();

		return generateChart(chart, configuredQuery);
	}


	private ChartInstance generateChart(ChartDefinition definition, ConfiguredQuery configuredQuery) {

		// first, gather the tuples
		List<Tuple> tuples = dataFinder.executeQuery(configuredQuery);

		// now postprocess them
		TupleProcessor processor = tupleProcessorProvider.get();

		ChartSeries series = processor
			.setDefinition(definition)
			.initialize()
			.process(tuples)
			.createChartSeries();

		// create the chart instance and return it
		return new ChartInstance(definition, series);

	}

}
