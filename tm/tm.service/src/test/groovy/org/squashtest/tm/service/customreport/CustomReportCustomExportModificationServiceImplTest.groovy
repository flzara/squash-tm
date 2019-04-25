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
package org.squashtest.tm.service.customreport

import org.squashtest.tm.domain.customreport.CustomReportCustomExport
import org.squashtest.tm.service.internal.customreport.CustomReportCustomExportModificationServiceImpl
import spock.lang.Specification

class CustomReportCustomExportModificationServiceImplTest extends Specification {

	private CustomReportCustomExportModificationServiceImpl sut = new CustomReportCustomExportModificationServiceImpl()
	private CustomReportLibraryNodeService customReportLibraryNodeService = Mock()

	def setup() {
		sut.reportLibraryNodeService = customReportLibraryNodeService
	}

	def "Should update an existing CustomReportCustomExport with a different name"() {
		given:
			long customExportId = 4L
		and:
			CustomReportCustomExport formerExport = new CustomReportCustomExport()
			formerExport.name = "my_custom_export"
		and:
			CustomReportCustomExport updatedExport = Mock()
			updatedExport.getName() >> "my_updated_custom_export"
			updatedExport.getScope() >> Mock(List)
			updatedExport.getColumns() >> Mock(List)
		and:
			customReportLibraryNodeService.findCustomExportByNodeId(customExportId) >> formerExport
			customReportLibraryNodeService.renameNode(customExportId, updatedExport.getName()) >> {
				formerExport.name = updatedExport.getName()
			}
		when:
			sut.updateCustomExport(customExportId, updatedExport)
		then:
			formerExport.getName() == "my_updated_custom_export"
			formerExport.getScope() >> updatedExport.getScope()
			formerExport.getColumns() >> updatedExport.getColumns()
	}

	def "Should update an existing CustomReportCustomExport with the same name"() {
		given:
			long customExportId = 4L
		and:
			CustomReportCustomExport formerExport = new CustomReportCustomExport()
			formerExport.name = "my_custom_export"
		and:
			CustomReportCustomExport updatedExport = Mock()
			updatedExport.getName() >> "my_custom_export"
			updatedExport.getScope() >> Mock(List)
			updatedExport.getColumns() >> Mock(List)
		and:
			customReportLibraryNodeService.findCustomExportByNodeId(customExportId) >> formerExport
		when:
			sut.updateCustomExport(customExportId, updatedExport)
		then:
			formerExport.getName() == "my_custom_export"
			formerExport.getScope() >> updatedExport.getScope()
			formerExport.getColumns() >> updatedExport.getColumns()
	}
}
