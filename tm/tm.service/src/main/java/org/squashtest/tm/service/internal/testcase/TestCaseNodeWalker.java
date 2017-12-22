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
package org.squashtest.tm.service.internal.testcase;

import java.util.LinkedList;
import java.util.List;

import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor;


/**
 * 
 *  That class will descend a TestCaseLibraryNode hierarchy and add them in a particular order.
 * 
 *  Currently : will walk depth-first and wont add duplicate entries.
 *  
 *  NOT THREAD SAFE. Get a new instance everytime you need a walk.
 */

/*
 * Todo : 
 * 		1) define Folder and LibraryNode as visitable so that we can turn that class into generic.
 * 		2) let the user choose the walking and adding strategy if need be some day.
 */
public class TestCaseNodeWalker implements TestCaseLibraryNodeVisitor{


	private List<TestCase> outputList ;
			
	public TestCaseNodeWalker(){
		outputList = new LinkedList<>();
	}
	
	public List<TestCase> walk(List<TestCaseLibraryNode> inputList){
		
		for (TestCaseLibraryNode node : inputList){
			node.accept(this);
		}
		
		return outputList;
	}

	
	@Override
	public void visit(TestCase testCase){
		if (! outputList.contains(testCase)){
			outputList.add(testCase);
		}
	}
	
	@Override
	public void visit(TestCaseFolder testCaseFolder){
		for (TestCaseLibraryNode node : testCaseFolder.getContent()){
			node.accept(this);
		}
	}

	
}
