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
package org.squashtest.tm.domain.customfield;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * Enumerates the entities which can be bounded to custom fields.
 * 
 * @author Gregory Fouquet
 * 
 */
public enum BindableEntity implements Internationalizable {

	TEST_CASE() {
		@Override
		public Class<?> getReferencedClass() {
			return TestCase.class;
		};

		@Override
		public RenderingLocation[] getValidRenderingLocations() {
			return new RenderingLocation[0];
		}

	},

	TEST_STEP() {

		@Override
		public Class<?> getReferencedClass() {
			return ActionTestStep.class;
		};

		@Override
		public RenderingLocation[] getValidRenderingLocations() {
			return new RenderingLocation[] { RenderingLocation.STEP_TABLE };
		}
	},

	CAMPAIGN() {
		@Override
		public Class<?> getReferencedClass() {
			return Campaign.class;
		};

		@Override
		public RenderingLocation[] getValidRenderingLocations() {
			return new RenderingLocation[0];
		}

	},
	ITERATION() {
		@Override
		public Class<?> getReferencedClass() {
			return Iteration.class;
		};

		@Override
		public RenderingLocation[] getValidRenderingLocations() {
			return new RenderingLocation[0];
		}
	},
	TEST_SUITE() {
		@Override
		public Class<?> getReferencedClass() {
			return TestSuite.class;
		};

		@Override
		public RenderingLocation[] getValidRenderingLocations() {
			return new RenderingLocation[0];
		}

	},
	REQUIREMENT_VERSION() {
		@Override
		public Class<?> getReferencedClass() {
			return RequirementVersion.class;
		};

		@Override
		public RenderingLocation[] getValidRenderingLocations() {
			return new RenderingLocation[0];
		}

	},
	EXECUTION(){
		@Override
		public Class<?> getReferencedClass() {
			return Execution.class;
		};
		
		@Override
		public RenderingLocation[] getValidRenderingLocations() {
			return new RenderingLocation[0];
		}
	},
	EXECUTION_STEP(){
		@Override
		public Class<?> getReferencedClass() {
			return ExecutionStep.class;
		};
		
		@Override
		public RenderingLocation[] getValidRenderingLocations() {
			return new RenderingLocation[] { RenderingLocation.STEP_TABLE };
		}
	};

	private static final String I18N_NAMESPACE = "label.customField.bindableEntity.";

	/**
	 * @see org.squashtest.tm.core.foundation.i18n.Internationalizable#getI18nKey()
	 */
	@Override
	public String getI18nKey() {
		return I18N_NAMESPACE + name();
	}

	public abstract Class<?> getReferencedClass();

	public abstract RenderingLocation[] getValidRenderingLocations();

	public static BindableEntity coerceToBindableEntity(@NotNull Class<?> entityType) {
		for (BindableEntity be : values()) {
			if (be.getReferencedClass().isAssignableFrom(entityType)) {
				return be;
			}
		}

		throw new IllegalArgumentException("Type cannot be coerced to a BindableEntity : " + entityType.getName());
	}
}
