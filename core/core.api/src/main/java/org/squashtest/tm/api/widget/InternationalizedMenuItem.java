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
package org.squashtest.tm.api.widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.squashtest.tm.api.security.acls.AccessRule;
import org.squashtest.tm.api.widget.access.AccessRuleBuilder;
import org.squashtest.tm.core.foundation.i18n.Labelled;
import org.squashtest.tm.core.foundation.lang.Assert;

/**
 * Implementation of {@link MenuItem} which provides internationalized properties using the context's message source.
 * Has to be configured using Spring.
 *
 * @author Gregory Fouquet
 *
 */
public class InternationalizedMenuItem extends Labelled implements MenuItem, InitializingBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(InternationalizedMenuItem.class);

	private String tooltipKey;
	private String url;
	private AccessRule accessRule;

	public InternationalizedMenuItem() {
		super();
	}

	/**
	 * Tooltip is internationalized.
	 *
	 * @see org.squashtest.tm.api.widget.MenuItem#getTooltip()
	 */
	@Override
	public String getTooltip() {
		return getMessage(tooltipKey);
	}

	/**
	 * @see org.squashtest.tm.api.widget.MenuItem#getUrl()
	 */
	@Override
	public String getUrl() {
		return url;
	}

	/**
	 * @param tooltipKey
	 *            the tooltipKey to set
	 */
	public void setTooltipKey(String tooltipKey) {
		this.tooltipKey = tooltipKey;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	private void checkBeanState() {
		/*
		 * URL can now be null
		 *
		 */
		Assert.propertyNotBlank(tooltipKey, "tooltipKey property should not be null");
		initializeAccessRule();
		Assert.propertyNotNull(accessRule, "accessRule property should not be null");
	}

	/**
	 * Anybody can access this widget. Override to customize.
	 *
	 * @return
	 */
	private void initializeAccessRule() {
		if (accessRule == null) {
			accessRule = AccessRuleBuilder.anybody();
			LOGGER.debug("Access rule set to anybody");
		}
	}

	/**
	 * @see org.squashtest.tm.api.widget.MenuItem#getAccessRule()
	 */
	@Override
	public AccessRule getAccessRule() {
		return accessRule;
	}

	/**
	 * @param accessRule
	 *            the accessRule to set
	 */
	public void setAccessRule(AccessRule accessRule) {
		this.accessRule = accessRule;
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public final void afterPropertiesSet() {
		doInitialize();
		checkBeanState();

	}

	/**
	 * Hook whichcan be overriden by subclasses do perform further initialization.
	 */
	protected void doInitialize() {
		// NOOP

	}

}
