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

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This class receive configuration properties from the "squash.management" namespace, which are used to configure the
 * "management" channel / url namespace.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@ConfigurationProperties(prefix = "squash.management")
public class SquashManagementProperties {
	private int port = 9443;
	private String channel = "https";

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Do not use this getter to configure spring security. Use SquashManagementProperties#getRequiredChannel instead.
	 *
	 * @return
	 */
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getRequiredChannel() {
		return StringUtils.equals(channel, "http") ? "REQUIRES_INSECURE_CHANNEL" : "REQUIRES_SECURE_CHANNEL";
	}
}
