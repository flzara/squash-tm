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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.SQLQuery;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.ProjectDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("rawtypes")
@Repository("squashtest.tm.repository.RequirementLibraryNodeDao")
public class HibernateRequirementLibraryNodeDao extends HibernateEntityDao<RequirementLibraryNode> implements
	LibraryNodeDao<RequirementLibraryNode> {

	@Inject
	private ProjectDao projectDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getParentsName(long entityId) {
		SQLQuery query = currentSession().createSQLQuery(NativeQueries.RLN_FIND_SORTED_PARENT_NAMES);
		query.setParameter(ParameterNames.NODE_ID, entityId, LongType.INSTANCE);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getParentsIds(long entityId) {
		SQLQuery query = currentSession().createSQLQuery(NativeQueries.RLN_FIND_SORTED_PARENT_IDS);
		query.setResultTransformer(new SqLIdResultTransformer());
		query.setParameter(ParameterNames.NODE_ID, entityId, LongType.INSTANCE);
		return query.list();
	}

	@Override
	public List<Long> findNodeIdsByPath(List<String> paths) {
		List<Long> result = new ArrayList<>();
		for (String path : paths.subList(1, paths.size())) {
			result.add(findNodeIdByPath(path));
		}
		return result;
	}

	// Naive and probably sub optimized implementation but request on closure table don't give expected results, so we have to do it by recursive algorithm.
	// Hibernate or the RDBS seems to not be able to do the proper group concat on polymorphic associations.
	@Override
	public Long findNodeIdByPath(String path) {
		// TODO this looks way too complex for a dao method, probably more like a service method
		String projectName = PathUtils.extractUnescapedProjectName(path);
		List<String> splits = Arrays.asList(PathUtils.splitPath(path));
		List<String> effectiveSplits = unescapeSlashes(splits);
		GenericProject project = projectDao.findByName(projectName);

		//checks
		if (effectiveSplits.size() < 2 || project == null) {
			return null;
		}

		//first round, we need to find the first node
		RequirementLibraryNode parent = null;
		List<RequirementLibraryNode> content = project.getRequirementLibrary().getContent();

		for (RequirementLibraryNode requirementLibraryNode : content) {
			if (requirementLibraryNode.getName().equals(effectiveSplits.get(1))) {
				parent = requirementLibraryNode;
			}
		}

		//if first node doesn't exists return null as the path cannot exists
		if (parent == null) {
			return null;
		}

		//if length == 2, we are looking for a root node, so we didn't need to dig, we just return idFirstNode
		if (effectiveSplits.size() == 2) {
			return parent.getId();
		}

		return findRecursive(parent, effectiveSplits.subList(2, effectiveSplits.size()));
	}

	private Long findRecursive(RequirementLibraryNode parent,
		List<String> splits) {
		if (parent.getClass().equals(Requirement.class)) {
			Requirement reqParent = (Requirement) parent;
			return findRecursiveRequirement(reqParent, splits);
		} else {
			RequirementFolder reqFolder = (RequirementFolder) parent;
			List<RequirementLibraryNode> folderContent = reqFolder.getContent();
			for (RequirementLibraryNode requirementLibraryNode : folderContent) {
				if (requirementLibraryNode.getName().equals(splits.get(0)) && splits.size() == 1) {
					return requirementLibraryNode.getId();
				} else if (requirementLibraryNode.getName().equals(splits.get(0))) {
					return findRecursive(requirementLibraryNode, splits.subList(1, splits.size()));
				}
			}
			return null;
		}
	}

	private Long findRecursiveRequirement(Requirement reqParent,
		List<String> splits) {
		List<Requirement> content = reqParent.getContent();
		for (Requirement requirement : content) {
			if (requirement.getName().equals(splits.get(0)) && splits.size() == 1) {
				return requirement.getId();
			} else if (requirement.getName().equals(splits.get(0))) {
				return findRecursiveRequirement(requirement, splits.subList(1, splits.size()));
			}
		}
		return null;
	}

	private List<String> unescapeSlashes(List<String> paths) {
		List<String> unescaped = new ArrayList<>(paths.size());
		for (String orig : paths) {
			unescaped.add(orig.replaceAll("\\\\/", "/"));
		}
		return unescaped;
	}

}
