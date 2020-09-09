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
package org.squashtest.tm.service.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.milestone.MilestoneMember;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.requirement.MilestoneForbidModificationException;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.testcase.TestCaseFinder;

import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This aspect manages {@linkplain CheckLockedMilestone} annotation.
 */
@Aspect
@Component
public class CheckLockedMilestoneAspect {

	private static final String ADVISING_METHOD = "Advising method {}{}.";
	private static final String FOUND_ID_IN_METHOD = "Found required @Id on arg #{} of method {}.";
	private static final String ILLEGAL_ID_PARAMETER = "The found parameter annotated @Id cannot be cast to a long.";
	private static final String LOCKED_MILESTONE_MESSAGE = "This test case is bound to a locked milestone. It can't be modified";
	private static final String MISSING_ID_PARAMETER = "Could not find any argument annotated @Id in @CheckLockedMilestone method %s%s. This must be a structural programming error.";

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckLockedMilestone.class);

	@Inject
	private MilestoneDao milestoneDao;

	@Around(
		value = "execution(@org.squashtest.tm.service.annotation.CheckLockedMilestone * * (..)) && @annotation(args)",
		argNames = "args")
	public void checkLockedMilestone(ProceedingJoinPoint pjp, CheckLockedMilestone args) throws Throwable {
		long id = findEntityId(pjp);
		Class<? extends MilestoneMember> entityType = args.entityType();

		boolean isEntityBoundToLockedMilestone;
		switch (entityType.getSimpleName()) {
			case "TestCase":
				isEntityBoundToLockedMilestone = milestoneDao.isTestCaseMilestoneModifiable(id);
				break;
			default:
				throw new UnsupportedOperationException();
		}
		if (isEntityBoundToLockedMilestone) {
			throw new MilestoneForbidModificationException(LOCKED_MILESTONE_MESSAGE);
		}
		pjp.proceed();
	}

	private long findEntityId(ProceedingJoinPoint pjp) {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method method = signature.getMethod();
		LOGGER.trace(ADVISING_METHOD, signature.getDeclaringTypeName(), method.getName());

		Object annotatedParameter = null;
		Annotation[][] annotations = method.getParameterAnnotations();

		argsLoop:
		for (int iArg = 0; iArg < annotations.length; iArg++) {
			Annotation[] currentArgAnnotations = annotations[iArg];
			for (int jAnn = 0; jAnn < currentArgAnnotations.length; jAnn++) {
				if (Id.class.equals(currentArgAnnotations[jAnn].annotationType())) {
					LOGGER.trace(FOUND_ID_IN_METHOD, iArg, method.getName());
					annotatedParameter = pjp.getArgs()[iArg];
					break argsLoop;
				}
			}
		}

		if (annotatedParameter == null) {
			throw new IllegalArgumentException(
				String.format(MISSING_ID_PARAMETER, signature.getDeclaringTypeName(), method.getName()));
		}

		long result;
		try {
			result = (long) annotatedParameter;
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException(ILLEGAL_ID_PARAMETER);
		}
		return result;
	}

}
