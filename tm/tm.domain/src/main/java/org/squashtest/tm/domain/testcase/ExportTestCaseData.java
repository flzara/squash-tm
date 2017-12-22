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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.library.ExportData;
/**
 * Data support for jasper Test Case Export
 * @author mpagnon
 *
 */
public class ExportTestCaseData extends ExportData implements TestStepVisitor{
	private String prerequisite = "";
	private TestCaseImportance weight;
	private String reference = "";
	private String nature ;
	private String type;
	private TestCaseStatus status;
	private String firstAction = "";
	private String firstExpectedResult = "";
	private List<ExportTestStepData> steps = new ArrayList<>();
	private ExportTestStepData lastBuildStepData;
	private String lastModifiedBy = "";
	private Date lastModifiedOn ;


	public String getPrerequisite() {
		return prerequisite;
	}

	public void setPrerequisite(String prerequisite) {
		doSetPrerequisite(prerequisite);
	}
	private void doSetPrerequisite(String prerequisite){
		if(prerequisite != null) {
			this.prerequisite = prerequisite;
		}
	}

	public TestCaseImportance getWeight() {
		return weight;
	}

	public void setWeight(TestCaseImportance weight) {
		this.weight = weight;
	}

	public ExportTestCaseData() {
		super();
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		doSetReference(reference);
	}

	private void doSetReference(String reference){
		if(reference != null){
			this.reference = reference;
		}
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public TestCaseStatus getStatus() {
		return status;
	}

	public void setStatus(TestCaseStatus status) {
		this.status = status;
	}

	public String getFirstAction() {
		return firstAction;
	}

	public void setFirstAction(String firstAction) {
		doSetFirstAction(firstAction);
	}
	public void doSetFirstAction(String firstAction){
		if(firstAction != null){
			this.firstAction = firstAction;
		}
	}
	public String getFirstExpectedResult() {
		return firstExpectedResult;
	}

	public void setFirstExpectedResult(String firstExpectedResult) {
		doSetFirstExpectedResult(firstExpectedResult);
	}
	private void doSetFirstExpectedResult(String firstExpectedResult){
		if(firstExpectedResult != null){
			this.firstExpectedResult = firstExpectedResult;
		}
	}
	public List<ExportTestStepData> getSteps() {
		return steps;
	}

	public void setSteps(List<ExportTestStepData> steps) {
		this.steps = steps;
	}

	public ExportTestCaseData(TestCase testCase, TestCaseFolder folder) {
		super(testCase, folder);
		doSetReference(testCase.getReference());
		doSetPrerequisite(testCase.getPrerequisite());
		this.weight = testCase.getImportance() ;
		this.nature = testCase.getNature().getCode();
		this.type = testCase.getType().getCode();
		this.status = testCase.getStatus();
		AuditableMixin audit = (AuditableMixin) testCase;
		this.lastModifiedBy = audit.getLastModifiedBy();
		this.lastModifiedOn = audit.getLastModifiedOn();
		formatSteps(testCase);
	}

	private void formatSteps(TestCase testCase) {
		List<TestStep> testSteps = testCase.getSteps();
		if(!testSteps.isEmpty()){
			formatFirstStepsInfos(testSteps);
			formatOtherStepsInfos(testSteps);
		}
	}

	private void formatOtherStepsInfos(List<TestStep> testSteps) {
		for(int i=1; i<testSteps.size(); i ++){
			ExportTestStepData otherStep = buildExportTestStepData(testSteps.get(i));
			otherStep.setTestCase(this);
			this.steps.add(otherStep);
		}
	}

	private void formatFirstStepsInfos(List<TestStep> testSteps) {
		ExportTestStepData firstStep = buildExportTestStepData(testSteps.get(0));
		firstStep.setTestCase(this);
		doSetFirstAction(firstStep.getAction());
		doSetFirstExpectedResult(firstStep.getExpectedResult());
	}

	private ExportTestStepData buildExportTestStepData(TestStep item) {
		item.accept(this);
		return lastBuildStepData;
	}

	@Override
	public void visit(ActionTestStep visited) {
		String action = visited.getAction();
		String result = visited.getExpectedResult();
		lastBuildStepData = new ExportTestStepData(action, result);

	}

	@Override
	public void visit(CallTestStep visited) {
		String action = "Calls : "+visited.getCalledTestCase().getName();
		String result = "";
		lastBuildStepData = new ExportTestStepData(action, result);

	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(Date lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}
}
