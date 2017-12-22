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
package org.squashtest.tm.service.internal.library


import javax.inject.Provider;

import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.testcase.TestCaseLibraryNavigationServiceImpl;

import spock.lang.Specification



class AbstractLibraryNavigationServiceTest extends Specification {

	private AbstractLibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode>	service = new TestCaseLibraryNavigationServiceImpl()
	private PasteStrategy<TestCaseFolder, TestCaseLibraryNode> pasteToFolderStrategy = Mock()

	def setup(){
		def provider = Mock(Provider)
		provider.get() >> pasteToFolderStrategy
		service.pasteToTestCaseFolderStrategyProvider = provider
	}


	def "should use paste strategy"(){
		given :
			Long destinationId = 2l
			Long[] sourceNodeIds = [1L]

		when :
		def result = service.copyNodesToFolder(2l, sourceNodeIds)

		then :
		1*pasteToFolderStrategy.pasteNodes(destinationId, Arrays.asList(sourceNodeIds))

	}

}
