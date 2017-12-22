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
package org.squashtest.tm.web.internal.model.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JsonProjectBuilder;

/**
 * @author Gregory Fouquet
 *
 */
@Component
@Scope("prototype")
public class JsonTestCaseBuilder {
	public interface ListBuilder {
		List<JsonTestCase> toJson();
	}

	@Inject
	private InternationalizationHelper internationalizationHelper;

	@Inject
	private JsonProjectBuilder projectBuilder;


	private Locale locale;

	private List<TestCase> entities;

	private boolean extended = false;

	public JsonTestCaseBuilder locale(@NotNull Locale locale) {
		this.locale = locale;
		return this;
	}

	public ListBuilder entities(@NotNull List<TestCase> entities) {
		this.entities = entities;
		return new ListBuilder() {

			@Override
			public List<JsonTestCase> toJson() {
				return buildList();
			}

		};
	}


	public JsonTestCaseBuilder extended(){
		extended = true;
		return this;
	}


	/**
	 * Simplistic implementation - we could cache projets and other referencial data.
	 *
	 * @return
	 */
	private List<JsonTestCase> buildList() {
		if (entities.isEmpty()) {
			return Collections.emptyList();
		}

		List<JsonTestCase> res = new ArrayList<>(entities.size());

		for (TestCase tc : entities) {
			res.add(build(tc));
		}

		return res;
	}

	private JsonTestCase build(TestCase tc) {
		JsonTestCase res = new JsonTestCase();
		res.setId(tc.getId());
		res.setName(tc.getName());
		res.setRef(tc.getReference());
		res.setProject(projectBuilder.toSimpleJson(tc.getProject()));
		res.setType(buildType(tc.getType()));

		if (extended){
			res.setDescription(tc.getDescription());
		}

		return res;
	}

	/**
	 * @param type
	 * @return
	 */
	private JsonInternationalizableItem buildType(InfoListItem type) {
		JsonInternationalizableItem res = new JsonInternationalizableItem();
		res.setValue(type.getCode());
		res.setLabel(internationalizationHelper.getMessage(type.getLabel(),null, type.getLabel(), locale));
		return res;
	}


}
