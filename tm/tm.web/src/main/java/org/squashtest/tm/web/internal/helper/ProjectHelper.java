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
package org.squashtest.tm.web.internal.helper;

import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.project.ProjectVisitor;

/**
 * Helper methods for Project view generation.
 * 
 * @author Gregory Fouquet
 * 
 */
public final class ProjectHelper {
	private ProjectHelper() {
		super();
	}

	/**
	 * We cannot use instanceof in el, hence this helper method.
	 * Also, we need to visit the object (potential hibernate proxy) to get its actual type.
	 * 
	 * @param project
	 * @return <code>true</code> if project is an instance of {@link ProjectTemplate}
	 */
	public static boolean isTemplate(GenericProject project) {
		final boolean[] res = { false };

		project.accept(new ProjectVisitor() {

			@Override
			public void visit(ProjectTemplate projectTemplate) {
				res[0] = true;

			}

			@Override
			public void visit(Project project) {
				res[0] = false;

			}
		});

		return res[0];
	}
}
