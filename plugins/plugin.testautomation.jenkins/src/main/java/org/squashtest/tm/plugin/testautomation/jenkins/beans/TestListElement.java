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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class TestListElement {

	private String name;
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


	public Collection<String> collectAllTestNames(){

		Collection<String> allNames = new LinkedList<>();

		//case : directory
		if (contents != null){

			// we don't want the pseudo root and discard its name if encountered.
			String thisName = ! amIPseudoRoot() ? name + "/" : "";

			for (TestListElement content : contents){

				Collection<String> subNames = content.collectAllTestNames();
				for (String sub : subNames){
					allNames.add(thisName + sub);
				}

			}
		}
		// case : test
		else{
			allNames.add(name);
		}

		return allNames;
	}
}
