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
package org.squashtest.tm.plugin.testautomation.jenkins.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestListElement {

	private String name;
	private MetadataElement metadata;
	private Date timestamp;
	private TestListElement[] contents;

	public TestListElement(){
		super();
	}


	// this is a trick required because Jenkins would return a (pseudo) root of the test tree
	// this root stands for the project, we need to detect if this is the case for the current node.
	private boolean amIPseudoRoot(){
		return timestamp != null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public TestListElement[] getContents() {
		return contents;
	}

	public void setContents(TestListElement[] contents) {
		if (contents == null){
			this.contents = null;
		}
		else{
			this.contents = Arrays.copyOf(contents, contents.length);
		}
	}

	public MetadataElement getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataElement metadata) {
		this.metadata = metadata;
	}

	//[TM-13] Instead of a list of test names, we now return a mapof the test names with the associated SquashTM TestCases'UUID.
	public Map<String, List<String>> collectAllTestNamesWithLinkedTestCases(){

		Map<String, List<String>> testNamesWithLinkedTCMap = new LinkedHashMap<>();

		//case : directory
		if (contents != null){

			// we don't want the pseudo root and discard its name if encountered.
			String thisName = ! amIPseudoRoot() ? name + "/" : "";

			for (TestListElement content : contents){
				Map<String, List<String>> subMap = content.collectAllTestNamesWithLinkedTestCases();
				subMap.forEach((testName, linkedTestCaseList) -> testNamesWithLinkedTCMap.put(thisName + testName, linkedTestCaseList));
			}
		}
		// case : test
		else{
			if(metadata == null || metadata.getLinkedTC() == null){
				testNamesWithLinkedTCMap.put(name, Collections.emptyList());
			} else {
				testNamesWithLinkedTCMap.put(name, metadata.getLinkedTC());
			}
		}

		return testNamesWithLinkedTCMap;
	}

	// [TM-13] New sub-element of TestListElement containing metadatas. For now, only the list of Squash-TM TestCases'UUID have interest for us.
	private class MetadataElement {

		@JsonProperty("linked-TC")
		private List<String> linkedTC;

		public List<String> getLinkedTC() {
			return linkedTC;
		}

		public void setLinkedTC(List<String> linkedTC) {
			this.linkedTC = linkedTC;
		}

		public MetadataElement(){
			super();
		}


	}
}
