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
package org.squashtest.tm.web.internal.report.services

import org.squashtest.tm.domain.infolist.InfoListItem
import org.squashtest.tm.domain.infolist.ListItemReference
import org.squashtest.tm.domain.requirement.ExportRequirementData
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.web.internal.report.service.JasperReportsService
import spock.lang.Specification

public class JasperReportsServiceImplTest extends Specification {

	private JasperReportsService jrService = new JasperReportsService();


	def "should export some requirements"(){

		given :


		URL fileURL = getClass().getClassLoader().getResource("requirement-export.jasper");
		File file = new File(fileURL.getFile());
		InputStream reportStream = new FileInputStream(file);

		def data1 = generateExportData("/project/folder",
				"/project/folder/uprate",
				1l,
				2,
				"REQS",
				"uprate",
				"<p>the rate in the up stance is 100</p>",
				RequirementCriticality.MAJOR,
				new ListItemReference("HITECH"),
				RequirementStatus.APPROVED)

		def data2 = generateExportData("/project/folder",
				"/project/folder/downrate",
				2l,
				1,
				"REQS",
				"downrate",
				"<p>the rate in the down stance is 30</p>",
				RequirementCriticality.UNDEFINED,
				new ListItemReference("LOTECH"),
				RequirementStatus.UNDER_REVIEW)

		def data3 = generateExportData("/project/folder",
				"/project/folder/stdrate",
				3l,
				4,
				"REQS",
				"stdrate",
				"<p>the rate in the normal stance is 55</p>",
				RequirementCriticality.CRITICAL,
				new ListItemReference("CURTECH"),
				RequirementStatus.APPROVED)



		def dataSource = [data1, data2, data3];

		when :

		InputStream resStream = jrService.getReportAsStream(reportStream, "csv", dataSource, new HashMap(), new HashMap());

		BufferedReader reader = new BufferedReader(new InputStreamReader(resStream));

		def header = reader.readLine();		// we don't care much of testing the header
		def strData1 = reader.readLine();
		def strData2 = reader.readLine();
		def strData3 = reader.readLine();


		then :

		strData1 == "/project/folder,/project/folder/uprate,1,2,REQS,uprate,<p>the rate in the up stance is 100</p>,MAJOR,HITECH,APPROVED,null,null"
		strData2 == "/project/folder,/project/folder/downrate,2,1,REQS,downrate,<p>the rate in the down stance is 30</p>,UNDEFINED,LOTECH,UNDER_REVIEW,null,null"
		strData3 == "/project/folder,/project/folder/stdrate,3,4,REQS,stdrate,<p>the rate in the normal stance is 55</p>,CRITICAL,CURTECH,APPROVED,null,null"

	}

	private ExportRequirementData generateExportData(
			String folderPath,
			String requirementParentPath,
			Long id,
			int version,
			String reference,
			String name,
			String description,
			RequirementCriticality crit,
			InfoListItem cat,
			RequirementStatus status){

		ExportRequirementData data = new ExportRequirementData();

		data.setFolderName folderPath
		data.setRequirementParentPath requirementParentPath
		data.setId id
		data.setCurrentVersion version
		data.setReference reference
		data.setName name
		data.setDescription description
		data.setCriticality crit
		data.setCategory (cat.getCode())
		data.setStatus status

		data
	}

}
