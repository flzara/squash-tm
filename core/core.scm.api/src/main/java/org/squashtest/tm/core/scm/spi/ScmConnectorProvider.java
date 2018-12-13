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
package org.squashtest.tm.core.scm.spi;

import org.squashtest.tm.domain.scm.ScmRepository;

public interface ScmConnectorProvider {

	/**
	 * Get the kind of ScmConnector this provider creates.
	 * @return The kind of ScmConnector this provider creates as a String.
	 */
	String getScmKind();

	/**
	 * Create a ScmConnector suitable for the given ScmRepository.
	 * @return The ScmConnector suitable for the given ScmRepository.
	 */
	ScmConnector createScmConnector(ScmRepository scmRepository);
}
