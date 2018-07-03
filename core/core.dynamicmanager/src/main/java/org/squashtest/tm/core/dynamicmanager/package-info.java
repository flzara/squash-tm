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


/*
 *  <p>
 *      Deprecation Note:  The dynamic dao were all replaced by Spring Data repositories but the dynamic services are still in use.
 *      However the nature of their operations make them essentially very close to Spring Data repositories too.
 *      I suggest we finish the job and ditch the dynamic services too. We could then definitely remove
 *      this module and tools/tools.annotation.processor.
 *  </p>
 */
package org.squashtest.tm.core.dynamicmanager;
