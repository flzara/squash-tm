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
package org.squashtest.tm.service.deletion

import org.springframework.context.MessageSource
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.internal.deletion.AbstractNodeDeletionHandler
import org.squashtest.tm.service.internal.repository.EntityDao
import org.squashtest.tm.service.internal.repository.FolderDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao
import spock.lang.Specification

class AbstractNodeDeletionHandlerImplTest extends Specification {

	final TestCaseDao nodeDao = Mock();
	final TestCaseFolderDao folderDao = Mock();

	class TestNodeDeletionHandler extends AbstractNodeDeletionHandler<TestCaseLibraryNode,  TestCaseFolder>{

		TestCaseDao tcDao;
		TestCaseFolderDao tcFolderDao;


		protected EntityDao<TestCase> getEntityDao(){
			return tcDao;
		}
		protected FolderDao<TestCaseFolder, TestCaseLibraryNode> getFolderDao(){
			return tcFolderDao;
		}

		protected  List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds){
			List<SuppressionPreviewReport> preview = new LinkedList<SuppressionPreviewReport>();


			NotDeletablePreviewReport notDeletableReport = new NotDeletablePreviewReport();
			List<Long> calleds = tcDao.findTestCasesHavingCaller (nodeIds);

			for (Long called : calleds ){
				notDeletableReport.addName(called.toString());
			}
			notDeletableReport.addWhy("because it's a test");

			preview.add(notDeletableReport);

			return preview;
		}

		protected  List<Long> detectLockedNodes(List<Long> nodeIds){
			return tcDao.findAllTestCasesIdsCalledByTestCases (nodeIds);
		}

		protected  OperationReport batchDeleteNodes(List<Long> ids){
			OperationReport report = new  OperationReport()
			report.addRemoved(ids, "who-cares");
			return report
		}

		@Override
		protected OperationReport batchUnbindFromMilestone(List<Long> ids) {
			return new OperationReport();
		}

		protected boolean isMilestoneMode(){
			return false;
		}

	}

	TestNodeDeletionHandler handler = new TestNodeDeletionHandler();

	def setup(){
		handler.tcDao=nodeDao;
		handler.tcFolderDao=folderDao;
	}

	//default groovy collections for the [] notation is an implementation of List. But I have specific needs for Long[], hence the
	//scaffolding code here
	private List<Long[]> toListOfArrays(List<List<Long>> l){
		List<Long[]> res = new LinkedList<Long[]>();
		for (List<Long> longs : l){
			Long[] array = new Long[2]
			array[0] = longs[0]
			array[1] = longs[1]
			res.add(array)
		}
		return res;
	}


	private List<Long[]> defaultTreeHierarchy(){
		return  toListOfArrays([ [null, 1], [null, 2],
			[1, 11], [1, 12],[1, 13],[2, 14] ,
			[11, 21], [11, 22], [12, 23], [12, 24],
			[24, 31], [24, 32]
		])
	}


	def "should fetch the node hierarchy"(){

		given :
		def layer0 = [11, 12, 13, 14]
		def layer1 = [21, 22, 23, 24]
		def layer2 = [31, 32]
		def layer3 = []

		and :

		folderDao.findContentForList([1, 2]) >> layer0
		folderDao.findContentForList([11, 12, 13, 14]) >> layer1
		folderDao.findContentForList([21, 22, 23, 24]) >>  layer2
		folderDao.findContentForList([31, 32]) >> layer3

		and :
		def expectedList = [1, 2, 11, 12, 13, 14, 21, 22, 23, 24, 31, 32]


		when :
		def result = handler.findNodeHierarchy([1l, 2l])

		then :
		result == expectedList



	}



	def "should fetch the paired node hierarchy"(){

		given :
		def layer0 = toListOfArrays ([ [1, 11], [1, 12],[1, 13],[2, 14]]);
		def layer1 = toListOfArrays ([ [11, 21], [11, 22], [12, 23], [12, 24]] )
		def layer2 = toListOfArrays ([ [24, 31], [24, 32]])
		def layer3 = toListOfArrays ([])

		and :

		folderDao.findPairedContentForList([1, 2]) >> layer0
		folderDao.findPairedContentForList([11, 12, 13, 14]) >> layer1
		folderDao.findPairedContentForList([21, 22, 23, 24]) >>  layer2
		folderDao.findPairedContentForList([31, 32]) >> layer3

		and :
		def expectedList = [ [null, 1], [null, 2], layer0, layer1 , layer2 , layer3].flatten()

		when :
		def hierarchy = handler.findPairedNodeHierarchy([1l, 2l])

		then :
		hierarchy.flatten() == expectedList


	}



	//not much to test here since we'll mostly test the test class defined above
	def "should return a preview of : affected : all nodes, not deletable : several"(){

		given :
		def layer0 = [11,12,13,14]
		def layer1 = [21, 22, 23, 24]
		def layer2 = [ 31 , 32]
		def layer3 = []

		and :

		folderDao.findContentForList([1, 2]) >> layer0
		folderDao.findContentForList([11, 12, 13, 14]) >> layer1
		folderDao.findContentForList([21, 22, 23, 24]) >>  layer2
		folderDao.findContentForList([31, 32]) >> layer3

		and :

		def callers = [31, 32]
		nodeDao.findTestCasesHavingCaller(_) >> callers

		and :
		MessageSource source = Mock()
		source.getMessage("squashtm.deletion.preview.notdeletable.whichnodes",_,_) >> "cannot be deleted"
		source.getMessage("squashtm.deletion.preview.notdeletable.why",_,_) >> "reason"

		when :

		def preview = handler.simulateDeletion([1l, 2l])

		then :

		preview.size == 1
		String nondeletablemessage = preview[0].toString(source, null)

		nondeletablemessage.contains("31");
		nondeletablemessage.contains("32");
		nondeletablemessage.contains("<br/>reason : because it's a test<br/>");
	}





	def "should delete nodes"(){

		given :
		def layer0 = toListOfArrays ([ [1, 11], [1, 12],[1, 13],[2, 14]]);
		def layer1 = toListOfArrays ([ [11, 21], [11, 22], [12, 23], [12, 24]] )
		def layer2 = toListOfArrays ([ [24, 31], [24, 32]])
		def layer3 = toListOfArrays ([])

		and :

		folderDao.findPairedContentForList([1, 2]) >> layer0
		folderDao.findPairedContentForList([11, 12, 13, 14]) >> layer1
		folderDao.findPairedContentForList([21, 22, 23, 24]) >>  layer2
		folderDao.findPairedContentForList([31, 32]) >> layer3

		and :
		nodeDao.findAllTestCasesIdsCalledByTestCases(_) >> [32l, 22l]

		and :
		def expected = [2, 14, 13, 23, 21, 31]


		when :
		def deleted = handler.deleteNodes([1l, 2l])

		then :
		deleted.removed.collect{it.resid} as Set == expected as Set



	}






}
