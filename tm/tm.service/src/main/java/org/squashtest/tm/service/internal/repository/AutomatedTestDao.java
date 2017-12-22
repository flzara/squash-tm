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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.service.internal.repository.hibernate.NonUniqueEntityException;

public interface AutomatedTestDao {

	/**
	 * Will persist this test if really new, or return the persisted instance
	 * if this test already exists. Due to this the calling code should always
	 * rely on the returned instance of AutomatedTest.
	 * 
	 * @param newTest
	 */
	AutomatedTest persistOrAttach(AutomatedTest newTest);


	/**
	 * Will remove the test from the database, if and only if no TestCase nor AutomatedExecutionExtender
	 * still refer to it.
	 * 
	 * @param test
	 */
	void removeIfUnused(AutomatedTest test);


	/**
	 * returns how many test cases and/or executions reference an AutomatedTest (given its id)
	 * 
	 * @param testId
	 * @return
	 */
	long countReferences(long testId);


	/**
	 * Will look for AutomatedTests that aren't referenced by anything and will remove them from the repository
	 * 
	 */
	void pruneOrphans();


	AutomatedTest findById(Long testId);

	List<AutomatedTest> findByTestCases(Collection<Long> testCaseIds);


	/**
	 *	<p>Given a detached (or even attached) {@link AutomatedTest} example, will fetch a {@link AutomatedTest}
	 *	having the same characteristics. Null attributes will be discarded before the comparison. </p>
	 *
	 * @return a TestAutomation test if one was found, null if none was found.
	 * @throws NonUniqueEntityException if more than one match. Causes are either an example not restrictive enough ... or a bug.
	 */
	AutomatedTest findByExample(AutomatedTest example);


	/**
	 * warning : return unique automated tests ( ie result.size() &lt;= argument.size() )
	 * 
	 * @param extenderIds
	 * @return
	 */
	List<AutomatedTest> findAllByExtenderIds(List<Long> extenderIds);

	/**
	 * Same than {@link #findAllByExtenderIds(List)}, but with the extenders themselves instead of their ids.
	 * 
	 * @param extenders
	 * @return
	 */
	List<AutomatedTest> findAllByExtender(Collection<AutomatedExecutionExtender> extenders);

}
