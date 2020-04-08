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
package org.squashtest.tm.api.wizard;

import org.squashtest.tm.core.foundation.i18n.ContextBasedInternationalized;

/**
 * Implementation of {@linkplain WorkspacePluginIcon}
 * providing internationalized properties using the context's message source.
 */
public class InternationalizedWorkspaceIcon extends ContextBasedInternationalized implements WorkspacePluginIcon {

	private String cssClass;
	private String styleSheetPath;
	private String tooltipI18nKey;
	private String url;

	@Override
	public String getCssClass() {
		return cssClass;
	}
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public String getStyleSheetPath() {
		return styleSheetPath;
	}
	public void setStyleSheetPath(String styleSheetPath) {
		this.styleSheetPath = styleSheetPath;
	}

	@Override
	public String getTooltip() {
		return getMessage(tooltipI18nKey);
	}
	public void setTooltipI18nKey(String tooltipI18nKey) {
		this.tooltipI18nKey = tooltipI18nKey;
	}

	@Override
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
