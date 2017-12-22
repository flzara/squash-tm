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
package org.squashtest.tm.service.internal.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.exception.CompositeDomainException;
import org.squashtest.tm.exception.DomainException;
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.testcase.CustomTestStepModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;

/**
 * Implementations for (non dynamically generated) testStep modification services.
 *
 * @author mpagnon
 *
 */
@Service("CustomTestStepModificationService")
@Transactional
public class CustomTestStepModificationServiceImpl implements CustomTestStepModificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomTestStepModificationServiceImpl.class);
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private TestStepDao testStepDao;
	@Inject
	private CustomFieldValueManagerService cufValueService;
	@Inject
	private ParameterModificationService parameterModificationService;
	/**
	 * @see CustomTestStepModificationService#updateTestStep(Long, String, String, Map)
	 */
	@Override
	public void updateTestStep(Long testStepId, String action, String expectedResult, Map<Long, RawValue> cufValues) {
		List<DomainException> exceptions = new ArrayList<>();
		TestStep step = testStepDao.findById(testStepId);
		parameterModificationService.createParamsForStep(testStepId);
		updateCufValues(step, cufValues, exceptions);
		updateNonCustomFields(action, expectedResult, exceptions, step);
		if (!exceptions.isEmpty()) {
			throw new CompositeDomainException(exceptions);
		}

	}

	private void updateNonCustomFields(String action, String expectedResult, List<DomainException> exceptions,
			TestStep step) {
		step.accept(new TestStepUpdater(action, expectedResult));
		try {
			testStepDao.flush();
		} catch (ConstraintViolationException e) {
			for (ConstraintViolation<?> cv : e.getConstraintViolations()) {
				String property = cv.getPropertyPath().toString();
				String message = cv.getMessage();
				exceptions.add(new DomainException(message, property) {
					@Override
					public String getI18nKey() {
						return "";
					}
				});
			}
		}
	}

	private TestStep updateCufValues(TestStep step, Map<Long, RawValue> cufValues, List<DomainException> exceptions) {

		if (cufValues != null) {
			PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(step, "WRITE"));
			for (Entry<Long, RawValue> cufValue : cufValues.entrySet()) {
				try {
					if (cufValue.getValue() != null){
						RawValue rValue = cufValue.getValue();
						cufValueService.changeValue(cufValue.getKey(), rValue);
					}
				} catch (DomainException e) {
					LOGGER.error(e.getMessage());
					exceptions.add(e);
				}
			}
		}
		return step;
	}

	private static final class TestStepUpdater implements TestStepVisitor {

		private String expectedResult;
		private String action;

		private TestStepUpdater(String action, String expectedResult) {
			this.action = action;
			this.expectedResult = expectedResult;

		}

		@Override
		public void visit(ActionTestStep visited) {
			if (action != null){
				visited.setAction(action);
			}
			if (expectedResult != null){
				visited.setExpectedResult(expectedResult);
			}
		}

		@Override
		public void visit(CallTestStep visited) {
			// NOPE

		}

	}


}
