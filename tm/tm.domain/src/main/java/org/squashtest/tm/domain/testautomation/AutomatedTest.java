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
package org.squashtest.tm.domain.testautomation;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.Sizes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;

@NamedQueries({
	@NamedQuery(name="automatedTest.countReferencesByTestCases", query="select count(*) from TestCase tc join tc.automatedTest autoTest where autoTest.id = :autoTestId"),
	@NamedQuery(name="automatedTest.countReferencesByExecutions", query="select count(*) from AutomatedExecutionExtender extender join extender.automatedTest autoTest where autoTest.id = :autoTestId"),
	@NamedQuery(name="automatedTest.findByTestCase", query="select auto from TestCase tc join tc.automatedTest auto where tc.id in (:testCaseIds)"),
	@NamedQuery(name="automatedTest.bulkDelete", query="delete from AutomatedTest auto where auto in (:tests)"),
	@NamedQuery(name="automatedTest.findOrphans",
	query="from AutomatedTest auto where not exists (from TestCase where automatedTest = auto) and not exists (from AutomatedExecutionExtender where automatedTest = auto)")
})
@Entity
public class AutomatedTest implements Identified{

	@Id
	@Column(name="TEST_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "automated_test_test_id_seq")
	@SequenceGenerator(name = "automated_test_test_id_seq", sequenceName = "automated_test_test_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name="PROJECT_ID")
	private TestAutomationProject project;

	@Size(max = Sizes.NAME_MAX)
	private String name;

	protected AutomatedTest() {
		super();
	}

	public AutomatedTest(String name, TestAutomationProject project) {
		super();
		this.name = name;
		this.project = project;
	}

	@Override
	public Long getId() {
		return id;
	}

	public TestAutomationProject getProject() {
		return project;
	}

	public String getName(){
		return name;
	}

	/**
	 *
	 * @return project.name + name
	 */
	public String getFullName(){
		return "/" + project.getJobName() + "/"+name;
	}

	/**
	 *
	 * @return project.label + name
	 */
	public String getFullLabel(){
		return "/" + project.getLabel() + "/" + name;
	}

	/**
	 *
	 * @return name - shortName
	 */
	public String getPath(){
		return name.replaceAll("[^\\/]*$","");
	}

	/**
	 *
	 * @return returns name - path
	 */
	public String getShortName(){
		return name.replaceAll(".*\\/", "");
	}

	/**
	 *
	 * @return name - rootfolder
	 */
	public String getNameWithoutRoot(){
		return name.replaceFirst("^[^\\/]*\\/", "");
	}

	public String getRootFolderName(){
		return name.replaceFirst("\\/.*$","/");
	}

	/**
	 *
	 * @return if the test is a direct child of the root folder
	 */
	public boolean isAtTheRoot(){
		return getPath().equals(getRootFolderName());
	}

	public AutomatedTest newWithProject(TestAutomationProject newP){
		return new AutomatedTest(name, newP);
	}


}
