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
package org.squashtest.tm.service.internal.customfield;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.ActionStepCollector;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;
import org.squashtest.tm.service.customfield.DenormalizedFieldHelper;
import org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager;

/**
 * Read the definition of Helper instead
 * 
 * 
 * @author bsiri
 * 
 */

@Service("squashtest.tm.service.CustomFieldHelperService")
public class CustomFieldHelperServiceImpl implements CustomFieldHelperService {

	@Inject
	private CustomFieldBindingFinderService cufBindingService;

	@Inject
	private CustomFieldValueManagerService cufValuesService;

	@Inject
	private DenormalizedFieldValueManager denormalizedFinder;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.squashtest.tm.web.internal.service.CustomFieldHelperService#hasCustomFields(org.squashtest.tm.domain.customfield
	 * .BoundEntity)
	 */
	@Override
	public boolean hasCustomFields(BoundEntity entity) {
		return cufValuesService.hasCustomFields(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.web.internal.service.CustomFieldHelperService#newHelper(X)
	 */
	@Override
	public <X extends BoundEntity> CustomFieldHelper<X> newHelper(X entity) {
		CustomFieldHelperImpl<X> helper = new CustomFieldHelperImpl<>(entity);
		helper.setCufBindingService(cufBindingService);
		helper.setCufValuesService(cufValuesService);
		return helper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.web.internal.service.CustomFieldHelperService#newHelper(java.util.List)
	 */
	@Override
	public <X extends BoundEntity> CustomFieldHelper<X> newHelper(List<X> entities) {
		CustomFieldHelperImpl<X> helper = new CustomFieldHelperImpl<>(entities);
		helper.setCufBindingService(cufBindingService);
		helper.setCufValuesService(cufValuesService);
		return helper;
	}

	@Override
	public <X extends DenormalizedFieldHolder> DenormalizedFieldHelper<X> newDenormalizedHelper(X entity) {
		DenormalizedFieldHelper<X> helper = new DenormalizedFieldHelper<>(entity);
		helper.setDenormalizedFieldValueFinder(denormalizedFinder);
		return helper;
	}

	@Override
	public <X extends DenormalizedFieldHolder> DenormalizedFieldHelper<X> newDenormalizedHelper(List<X> entities) {
		DenormalizedFieldHelper<X> helper = new DenormalizedFieldHelper<>(entities);
		helper.setDenormalizedFieldValueFinder(denormalizedFinder);
		return helper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.web.internal.service.CustomFieldHelperService#newStepsHelper(java.util.List)
	 */
	@Override
	public CustomFieldHelper<ActionTestStep> newStepsHelper(List<TestStep> steps, Project container) {
		AbstractCustomFieldHelper<ActionTestStep> helper;
		List<ActionTestStep> actionSteps = new ActionStepCollector().collect(steps);

		if (actionSteps.isEmpty()) {
			helper = new NoValuesCustomFieldHelper<>(container, BindableEntity.TEST_STEP);
		} else {
			helper = new CustomFieldHelperImpl<>(actionSteps);
		}

		helper.setCufBindingService(cufBindingService);
		helper.setCufValuesService(cufValuesService);

		return helper;
	}


}
