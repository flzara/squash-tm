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
package org.squashtest.tm.service.internal.batchimport;

import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.CoverageInstruction;

import java.util.Map;

/**
 * Interface for batch import instructions methods.
 */
public interface Facility {
	LogTrain deleteTestCase(TestCaseTarget target);

	LogTrain addActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues);

	LogTrain addCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase, CallStepParamsInfo paramInfo, ActionTestStep actionStepBackup);

	LogTrain updateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues);

	LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase, CallStepParamsInfo paramInfo, ActionTestStep actionStepBackup);

	LogTrain deleteTestStep(TestStepTarget target);

	LogTrain createParameter(ParameterTarget target, Parameter param);

	LogTrain updateParameter(ParameterTarget target, Parameter param);

	LogTrain deleteParameter(ParameterTarget target);


	/**
	 * The creation of a dataset is idempotent (if such dataset exists it wont be created twice)
	 */
	LogTrain createDataset(DatasetTarget target);

	LogTrain deleteDataset(DatasetTarget dataset);

	/**
	 * Will update the value for the given parameter in the given dataset. If the dataset doesn't exist for this dataset, it will be created.
	 * If the parameter doesn't exist or is not available to this dataset the method fails. In all cases the methods returns a log.
	 */
	LogTrain failsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value, boolean isUpdate);

	/**
	 * Does exactly the same as the method above but with other arguments.
	 */
	LogTrain createTestCase(TestCaseInstruction instr);

	/**
	 * Does exactly the same as the method above but with other arguments.
	 */
	LogTrain updateTestCase(TestCaseInstruction instr);


	/**
	 * Will create a RequirementVersion. If the Requirement it depends on doesn't exist
	 * it will be created on the fly.
	 */
	LogTrain createRequirementVersion(RequirementVersionInstruction instr);

	/**
	 * Updates a RequiremenVersion with a new content.
	 */
	LogTrain updateRequirementVersion(RequirementVersionInstruction instr);

	/**
	 * Will delete a RequirementVersion when implemented some day, today it
	 * is not and will log a Failure if invoked.
	 */
	LogTrain deleteRequirementVersion(RequirementVersionInstruction instr);

	LogTrain createCoverage(CoverageInstruction instr);
	
	/**
	 * Will bind two requirement versions with a RequirementLink
	 * 
	 * @param instr
	 * @return
	 */
	LogTrain createRequirementLink(RequirementLinkInstruction instr);
	
	/**
	 * Will update the link between two requirements
	 * 
	 * @param instr
	 * @return
	 */
	LogTrain updateRequirementLink(RequirementLinkInstruction instr);
	
	/**
	 * Will remove a link between two requirements
	 * 
	 * @param instr
	 * @return
	 */
	LogTrain deleteRequirementLink(RequirementLinkInstruction instr);

}

