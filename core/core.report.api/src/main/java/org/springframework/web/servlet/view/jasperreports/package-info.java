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
/**
 * <p>NOT PART OF THE API This package contains the Spring MVC 4.3.9.RELEASE support classes for JasperReports.</p>
 *
 * <p>
 *     After JasperReports introduced substantial changes in JR API, Spring finally decided to drop support due to the massive
 *     amount of rewriting necessary and divergent design philosophy (see https://jira.spring.io/browse/SPR-13294).
 *
 *     Among the alternatives the Spring crew weight in favor of direct interaction with JasperReports API.
 *     Here we choose to preserve the now deprecated Spring support classes, for the following reasons :
 * </p>
 *
 * <ul>
 *     <li>We don't plan to upgrade JasperReports and support newer features (at least not yet),</li>
 *     <li>Dropping Spring would break the third party plugins which explicitly depend on it : this is not encouraged but
 *     nonetheless permitted by our API (see the dependency between ReportView) (the expected casualties would be low though),</li>
 *     <li>And of course this is cheap, easy and legal.</li>
 * </ul>
 *
 *
 * <p>
 *     However this merely kicks the can further. It might break in the future because Spring MVC might change the signature
 *     required at compile time, or change Spring MVC behavior, or users really needing support for the new JasperReports API.
 *     Eventually we will have to drop Spring-JR anyway; therefore Plugin developpers are advised not to depend on this
 *     package directly.
 * </p>
 *
 * <p>
 *     <b>Possible directions from here :</b>
 *     When time is come, future migration for direct JasperReport calls should include support for ExporterInput,
 *     ExporterConfiguration and ExporterOutput. It would be nice to enhance and use
 *     tm.service#org.squashtest.tm.web.internal.report.service.JasperReportService for that purpose.
 * </p>
 */
package org.springframework.web.servlet.view.jasperreports;
