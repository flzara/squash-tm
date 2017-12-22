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
package org.squashtest.tm.domain.testcase;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Persister;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.infrastructure.hibernate.TestStepPersister;
import org.squashtest.tm.security.annotation.AclConstrainedObject;
import org.squashtest.tm.security.annotation.InheritsAcls;




/*
 * IF YOU CHANGE ANYTHING REGARDING THE HIBERNATE MAPPING IN THAT CLASS, LIKE THE UNDERLYING TABLES OR THE MAPPING,
 * PLEASE MIND THE CUSTOM PERSISTER BELOW (@Persister(impl=TestStepPersister.class)).
 * 
 *  see org.squashtest.csp.tm.internal.infrastructure.hibernate.TestStepPersister
 */



@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@InheritsAcls(constrainedClass = TestCase.class, collectionName = "steps")
@Persister(impl=TestStepPersister.class)
public abstract class TestStep implements Identified {

	@Id
	@Column(name = "TEST_STEP_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "test_step_test_step_id_seq")
	@SequenceGenerator(name = "test_step_test_step_id_seq", sequenceName = "test_step_test_step_id_seq", allocationSize = 1)
	private Long id;


	@ManyToOne(fetch=FetchType.LAZY)
	@JoinTable(name = "TEST_CASE_STEPS", joinColumns = @JoinColumn(name = "STEP_ID", updatable = false, insertable = false), inverseJoinColumns = @JoinColumn(name = "TEST_CASE_ID", updatable = false, insertable = false))
	private TestCase testCase;

	@Override
	public Long getId() {
		return id;
	}


	public void setTestCase(@NotNull TestCase testCase){
		this.testCase = testCase;
	}

	public TestCase getTestCase(){
		return testCase;
	}

	@AclConstrainedObject
	public TestCaseLibrary getLibrary(){
		if(testCase != null){
			return (TestCaseLibrary) testCase.getLibrary();
		}
		return null;
	}

	/**
	 * @see {@link TestCase#getPositionOfStep(long)}
	 * @return {@link TestCase#getPositionOfStep(long)} or -1 if testCase is null
	 * @throws {@link UnknownElementException}
	 */
	public int getIndex(){
		int result = -1;
		if(testCase != null){
			result =  testCase.getPositionOfStep(this.id);
		}
		return result;
	}

	/**
	 * Should create a transient copy of this {@link TestStep} according to business rules. Business rules should be
	 * described by implementor in a unit test case.
	 *
	 * @return copy, should never return <code>null</code>.
	 */
	public abstract TestStep createCopy();

	public abstract void accept(TestStepVisitor visitor);

	public abstract List<ExecutionStep> createExecutionSteps(Dataset dataset);

}
