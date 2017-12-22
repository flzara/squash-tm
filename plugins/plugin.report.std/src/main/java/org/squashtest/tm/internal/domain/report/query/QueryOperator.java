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
package org.squashtest.tm.internal.domain.report.query;


/*
 * FIXME : add more type someday, before the class is released.
 * 
 */
/**
 * This Enumeration is optional and may be used when producing an implementation of the ReportQuery interface, or not.
 * 
 * It is only meant to bring semantics to a Query if some implementation of ReportQuery needs to. If you don't know what
 * to do with it, you probably don't need it.
 * 
 * @author bsiri
 *
 */

public enum QueryOperator {
	COMPARATOR_GT,
	COMPARATOR_LT,
	COMPARATOR_EQ,
	COMPARATOR_IN,
	COMPARATOR_SPECIAL
}
