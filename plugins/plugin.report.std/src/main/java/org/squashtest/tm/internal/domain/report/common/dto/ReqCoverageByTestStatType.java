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
package org.squashtest.tm.internal.domain.report.common.dto;

public enum ReqCoverageByTestStatType {
	TOTAL, TOTAL_VERIFIED, CRITICAL, CRITICAL_VERIFIED, MAJOR, MAJOR_VERIFIED, MINOR, MINOR_VERIFIED, UNDEFINED, UNDEFINED_VERIFIED
}
//TODO ajouter method convert(reqCriticaity) minor-> minor
//TODO ajouter methode convertVerified(name) name _> name_VERIFIED