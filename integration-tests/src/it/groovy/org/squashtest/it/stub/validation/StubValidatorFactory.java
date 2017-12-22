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
package org.squashtest.it.stub.validation;

import org.springframework.stereotype.Component;

import javax.validation.*;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import java.util.Collections;
import java.util.Set;

@Component
public class StubValidatorFactory implements ValidatorFactory {
	private final Validator  validator = new Validator() {
		@Override
		public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
			return Collections.emptySet();
		}

		@Override
		public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
			return Collections.emptySet();
		}

		@Override
		public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
			return Collections.emptySet();
		}

		@Override
		public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
			return null;
		}

		@Override
		public <T> T unwrap(Class<T> type) {
			return null;
		}

		@Override
		public ExecutableValidator forExecutables() {
			return null;
		}
	};

	@Override
	public Validator getValidator() {
		return validator;
	}

	@Override
	public ValidatorContext usingContext() {
		return null;
	}

	@Override
	public MessageInterpolator getMessageInterpolator() {
		return null;
	}

	@Override
	public TraversableResolver getTraversableResolver() {
		return null;
	}

	@Override
	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return null;
	}

	@Override
	public ParameterNameProvider getParameterNameProvider() {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		return null;
	}

	@Override
	public void close() {
		// noop
	}
}
