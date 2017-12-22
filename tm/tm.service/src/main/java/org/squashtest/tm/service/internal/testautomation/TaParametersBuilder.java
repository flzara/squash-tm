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
package org.squashtest.tm.service.internal.testautomation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * Builds parameters hash which shall be passed when executing an automated test.
 * 
 * Builder sould be discarded after the {@link TaParametersBuilder#build()} method is invoked.
 * 
 * @author Gregory Fouquet
 * 
 */
@Component
@Scope("prototype")
public class TaParametersBuilder implements ParametersBuilder {
	private abstract class ChildScopedParametersBuilder<E> implements ScopedParametersBuilder<E> {
		/**
		 * @see org.squashtest.tm.service.internal.testautomation.ParametersBuilder#testCase()
		 */
		@Override
		public ScopedParametersBuilder<TestCase> testCase() {
			return TaParametersBuilder.this.testCase();
		}

		/**
		 * @see org.squashtest.tm.service.internal.testautomation.ParametersBuilder#iteration()
		 */
		@Override
		public ScopedParametersBuilder<Iteration> iteration() {
			return TaParametersBuilder.this.iteration();
		}

		/**
		 * @see org.squashtest.tm.service.internal.testautomation.ParametersBuilder#campaign()
		 */
		@Override
		public ScopedParametersBuilder<Campaign> campaign() {
			return TaParametersBuilder.this.campaign();
		}

		/**
		 * @see org.squashtest.tm.service.internal.testautomation.ParametersBuilder#build()
		 */
		@Override
		public Map<String, Object> build() {
			return TaParametersBuilder.this.build();
		}

		/**
		 * @see org.squashtest.tm.service.internal.testautomation.ScopedParametersBuilder#addEntity(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public ScopedParametersBuilder<E> addEntity(E entity) {
			doAddEntity(entity);
			return this;
		}

		protected abstract void doAddEntity(E entity);

		/**
		 * @see org.squashtest.tm.service.internal.testautomation.ScopedParametersBuilder#addCustomFields(java.util.Collection)
		 */
		@Override
		public ScopedParametersBuilder<E> addCustomFields(Collection<CustomFieldValue> fields) {
			String codePrefix = getCustomFieldPrefix() + "_CUF_";
			TaParametersBuilder.this.addCustomFields(codePrefix, fields);

			return this;
		}

		/**
		 * @return
		 */
		protected abstract String getCustomFieldPrefix();
	};

	private ScopedParametersBuilder<TestCase> testCaseScopeBuilder = new ChildScopedParametersBuilder<TestCase>() {

		@Override
		protected void doAddEntity(TestCase entity) {
			addTestCase(entity);
		}

		@Override
		protected String getCustomFieldPrefix() {
			return "TC";
		}
	};
	private ScopedParametersBuilder<Campaign> campaignScopeBuilder = new ChildScopedParametersBuilder<Campaign>() {

		@Override
		protected void doAddEntity(Campaign entity) {
			// NOOP
		}

		@Override
		protected String getCustomFieldPrefix() {
			return "CPG";
		}
	};
	private ScopedParametersBuilder<Iteration> iterationScopeBuilder = new ChildScopedParametersBuilder<Iteration>() {

		@Override
		protected void doAddEntity(Iteration entity) {
			// NOOP
		}

		@Override
		protected String getCustomFieldPrefix() {
			return "IT";
		}
	};

	private Map<String, Object> params = new HashMap<>();

	/**
	 * @see org.squashtest.tm.service.internal.testautomation.ParametersBuilder#testCase()
	 */
	@Override
	public ScopedParametersBuilder<TestCase> testCase() {
		return testCaseScopeBuilder;
	}

	/**
	 * @see org.squashtest.tm.service.internal.testautomation.ParametersBuilder#iteration()
	 */
	@Override
	public ScopedParametersBuilder<Iteration> iteration() {
		return iterationScopeBuilder;
	}

	/**
	 * @see org.squashtest.tm.service.internal.testautomation.ParametersBuilder#campaign()
	 */
	@Override
	public ScopedParametersBuilder<Campaign> campaign() {
		return campaignScopeBuilder;
	}

	/**
	 * @see org.squashtest.tm.service.internal.testautomation.ParametersBuilder#build()
	 */
	@Override
	public Map<String, Object> build() {
		Map<String, Object> res = params;
		params = Collections.unmodifiableMap(res);
		return res;
	}

	private void addTestCase(TestCase testCase) {
		String value = testCase.getReference();
		nullSafePut("TC_REFERENCE", value);
	}

	private void nullSafePut(String key, String value) {
		if (value != null) {
			params.put(key, value);
		}
	}

	/**
	 * Adds a custom field to build parms. Param key is "${codePrefix}${field.code}" If a field's value is
	 * <code>null</code>, it is not added.
	 * 
	 * @param codePrefix
	 * @param fields
	 */
	private void addCustomFields(String codePrefix, Collection<CustomFieldValue> fields) {
		for (CustomFieldValue field : fields) {
			String val = field.getValue();

			if (val != null) {
				String code = codePrefix + field.getBinding().getCustomField().getCode();
				params.put(code, val);
			}
		}
	}
}
