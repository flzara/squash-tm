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
package org.squashtest.tm.domain.event;

import org.apache.commons.lang3.text.WordUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.squashtest.tm.domain.requirement.RequirementVersion;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * This aspect advises a RequirementVersion to raise an event when a RequirementVersion's
 * intrinsic property is modified.
 * <p>
 * Is it a build-time weaved aspect and should not be handled at runtime by Spring.
 *
 * @author Gregory Fouquet
 * @since 1.4.0  11/04/16 (port from .aj file)
 */
@Aspect
public class RequirementModificationEventPublisherAspect extends AbstractRequirementEventPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementModificationEventPublisherAspect.class);

	@Pointcut("execution(public void org.squashtest.tm.domain.requirement.RequirementVersion.setDescription(*))")
	private void executeLargePropertySetter() {
		// NOOP
	}

	@Pointcut("execution(public void org.squashtest.tm.domain.requirement.RequirementVersion.set*(*)) && !executeLargePropertySetter()")
	private void executeSimplePropertySetter() {
		// NOOP
	}

	/**
	 * Advises setters of a Requirement and raises a modification event after
	 * the setter is used to change the requirement's state. If the aspect is
	 * disabled, does nothing.
	 */
	@Around("executeSimplePropertySetter() && target(req) && args(newValue)")
	public void listenRequirementModification(ProceedingJoinPoint joinPoint, RequirementVersion req, Object newValue) throws Throwable { // NOSONAR propagated exception
		if (eventsAreEnabled(req)) {
			String propertyName = extractModifiedPropertyName(joinPoint);
			Object oldValue = readOldValue(req, propertyName);

			// this statement cannot be factored out
			joinPoint.proceed(new Object[]{req, newValue});

			if (requirementWasModified(oldValue, newValue)) {
				raiseSimplePropertyEvent(req, propertyName, oldValue, newValue);
			}
		} else {
			// this statement cannot be factored out
			joinPoint.proceed(new Object[]{req, newValue});
		}
	}

	/**
	 * Advises setters of a Requirement and raises an modification event after
	 * the setter is used to change the requirement's state. If the aspect is
	 * disabled, does nothing.
	 */
	@Around("executeLargePropertySetter() && target(req) && args(newValue)")
	public void listenLargeRequirementModification(ProceedingJoinPoint joinPoint, RequirementVersion req, Object newValue) throws Throwable { // NOSONAR propagated exception
		if (eventsAreEnabled(req)) {
			String propertyName = extractModifiedPropertyName(joinPoint);
			Object oldValue = readOldValue(req, propertyName);

			// this statement cannot be factored out
			joinPoint.proceed(new Object[]{req, newValue});

			if (requirementWasModified(oldValue, newValue)) {
				raiseLargePropertyEvent(req, propertyName, oldValue, newValue);
			}
		} else {
			// this statement cannot be factored out
			joinPoint.proceed(new Object[]{req, newValue});
		}
	}

	private boolean requirementWasModified(Object oldValue, Object newValue) {
		return !Objects.equals(Objects.toString(oldValue),
			Objects.toString(newValue));
	}

	private void raiseSimplePropertyEvent(RequirementVersion req, String propertyName, Object oldValue, Object newValue) {
		RequirementPropertyChange event = RequirementPropertyChange.builder()
			.setSource(req)
			.setModifiedProperty(propertyName)
			.setOldValue(oldValue)
			.setNewValue(newValue)
			.setAuthor(currentUser())
			.build();

		publish(event);

		LOGGER.trace("Simple property change event raised");
	}

	private Object readOldValue(RequirementVersion req, String propertyName) {
		try {
			Method propertyGetter = RequirementVersion.class.getMethod("get" + WordUtils.capitalize(propertyName));
			return ReflectionUtils.invokeMethod(propertyGetter, req);

		} catch (NoSuchMethodException e) {
			ReflectionUtils.handleReflectionException(e);
		}

		// this should never happen - the catch block rethows an exception
		return null;
	}

	private String extractModifiedPropertyName(JoinPoint setterJoinPoint) {
		String methodName = setterJoinPoint.getSignature().getName();
		String propertyName = methodName.substring(3); // method is assumed to be "setXxx"

		return WordUtils.uncapitalize(propertyName);
	}

	private void raiseLargePropertyEvent(RequirementVersion req, String propertyName, Object oldValue, Object newValue) {
		RequirementLargePropertyChange event = RequirementLargePropertyChange.builder()
			.setSource(req)
			.setModifiedProperty(propertyName)
			.setOldValue(oldValue)
			.setNewValue(newValue)
			.setAuthor(currentUser())
			.build();

		publish(event);

		LOGGER.trace("Large property change event raised");
	}

	private boolean eventsAreEnabled(RequirementVersion req) {
		return aspectIsEnabled() && requirementIsPersistent(req);
	}

	private boolean requirementIsPersistent(RequirementVersion req) {
		return req.getId() != null;
	}
}
