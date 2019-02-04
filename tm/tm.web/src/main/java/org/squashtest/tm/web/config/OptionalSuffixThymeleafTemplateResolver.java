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
package org.squashtest.tm.web.config;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

import java.util.Map;


/**
 * <p>
 * Legacy compatibility. When a controller returns a view name it usually includes '.html' at the end, however when resolving fragment the html is omitted.
 * </p>
 *
 * <p>
 * Thymeleaf 3 template resolver cannot cope with this inconsistency anymore. In order to avoid breaking things we now make  the suffix optional, so that
 * a view name can include .html or not in its name, as long as it resides in the /WEB-INF/templates folder it'll be fine.
 * </p>
 *
 * <p>
 * The big TODO is of course to user always use .html (ie track all directives th:include and make sure they specify '.html' in the fragment name) or
 * never use the suffix (ie track all views returned by a controller and remove the .html in its name).
 * </p>
 *
 * @author bsiri
 *
 */
public class OptionalSuffixThymeleafTemplateResolver extends SpringResourceTemplateResolver {

	@Override
	protected String computeResourceName(IEngineConfiguration configuration, String ownerTemplate, String template,
			String prefix, String suffix, Map<String, String> templateAliases,
			Map<String, Object> templateResolutionAttributes) {

		String effectiveSuffix = (template.endsWith(suffix)) ? "" : suffix;

		return super.computeResourceName(configuration, ownerTemplate, template, prefix, effectiveSuffix, templateAliases,
				templateResolutionAttributes);
	}



}
