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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.internal.batchimport.excel.CellValueCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.ImportModeCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.InfoListItemCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.InfoListItemCoercer.ListRole;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalBooleanCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalDateCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalEnumCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalIntegerCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalOneBasedIndexCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalStringArrayCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.ParamAssignationModeCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.StringCellCoercer;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementLinksSheetColumn;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn;

/**
 * Repository of {@link CellValueCoercer} for a given {@link TemplateColumn}s
 *
 * @author Gregory Fouquet
 *
 */
@Component
final class CellValueCoercerRepository<COL extends Enum<COL> & TemplateColumn> {
	private static final Map<TemplateWorksheet, CellValueCoercerRepository<?>> COERCER_REPO_BY_WORKSHEET = new HashMap<>(
		TemplateWorksheet.values().length);

	static {
		COERCER_REPO_BY_WORKSHEET.put(TemplateWorksheet.TEST_CASES_SHEET, createTestCasesSheetRepo());
		COERCER_REPO_BY_WORKSHEET.put(TemplateWorksheet.STEPS_SHEET, createStepsSheetRepo());
		COERCER_REPO_BY_WORKSHEET.put(TemplateWorksheet.PARAMETERS_SHEET, createParamsSheetRepo());
		COERCER_REPO_BY_WORKSHEET.put(TemplateWorksheet.DATASETS_SHEET, createDatasetsSheetRepo());
		COERCER_REPO_BY_WORKSHEET.put(TemplateWorksheet.DATASET_PARAM_VALUES_SHEET, createDatasetParamValuesSheetRepo());
		COERCER_REPO_BY_WORKSHEET.put(TemplateWorksheet.REQUIREMENT_SHEET, createRequirementSheetRepo());
		COERCER_REPO_BY_WORKSHEET.put(TemplateWorksheet.REQUIREMENT_LINKS_SHEET, createRequirementLinkSheetRepo());
		COERCER_REPO_BY_WORKSHEET.put(TemplateWorksheet.COVERAGE_SHEET, createCoverageSheetRepo());
	}

	/**
	 * Returns the repository suitable for the given worksheet.
	 *
	 * @param worksheet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <C extends Enum<C> & TemplateColumn> CellValueCoercerRepository<C> forWorksheet(
			@NotNull TemplateWorksheet worksheet) {
		return (CellValueCoercerRepository<C>) COERCER_REPO_BY_WORKSHEET.get(worksheet);
	}

	private static CellValueCoercerRepository<?> createCoverageSheetRepo() {
		CellValueCoercerRepository<CoverageSheetColumn> repo = new CellValueCoercerRepository<>();
		repo.coercerByColumn.put(CoverageSheetColumn.REQ_VERSION_NUM, OptionalIntegerCellCoercer.INSTANCE);
		return repo;
	}

	private static CellValueCoercerRepository<?> createRequirementSheetRepo() {
		CellValueCoercerRepository<RequirementSheetColumn> repo = new CellValueCoercerRepository<>();
		repo.coercerByColumn.put(RequirementSheetColumn.ACTION, ImportModeCellCoercer.INSTANCE);

		repo.coercerByColumn.put(RequirementSheetColumn.REQ_NUM, OptionalOneBasedIndexCellCoercer.INSTANCE);

		repo.coercerByColumn.put(RequirementSheetColumn.REQ_VERSION_CATEGORY, new InfoListItemCoercer<ListItemReference>(ListRole.ROLE_CATEGORY));
		repo.coercerByColumn.put(RequirementSheetColumn.REQ_VERSION_CREATED_ON, OptionalDateCellCoercer.INSTANCE);
		repo.coercerByColumn.put(RequirementSheetColumn.REQ_VERSION_CRITICALITY, OptionalEnumCellCoercer.forEnum(RequirementCriticality.class));
		repo.coercerByColumn.put(RequirementSheetColumn.REQ_VERSION_MILESTONE, OptionalStringArrayCellCoercer.INSTANCE);
		repo.coercerByColumn.put(RequirementSheetColumn.REQ_VERSION_NUM, OptionalIntegerCellCoercer.INSTANCE);

		repo.coercerByColumn.put(RequirementSheetColumn.REQ_VERSION_STATUS, OptionalEnumCellCoercer.forEnum(RequirementStatus.class));
		return repo;
	}
	
	private static CellValueCoercerRepository<?> createRequirementLinkSheetRepo(){
		CellValueCoercerRepository<RequirementLinksSheetColumn> repo = new CellValueCoercerRepository<>();		
		
		repo.coercerByColumn.put(RequirementLinksSheetColumn.ACTION, ImportModeCellCoercer.INSTANCE);
		repo.coercerByColumn.put(RequirementLinksSheetColumn.REQ_VERSION_NUM, OptionalIntegerCellCoercer.INSTANCE);
		repo.coercerByColumn.put(RequirementLinksSheetColumn.RELATED_REQ_VERSION_NUM, OptionalIntegerCellCoercer.INSTANCE);
		
		// for other properties, the default StringCellCoercer will kick in
		return repo;
	}

	/**
	 * @return
	 */
	private static CellValueCoercerRepository<?> createDatasetsSheetRepo() {
		CellValueCoercerRepository<DatasetSheetColumn> repo = new CellValueCoercerRepository<>();

		repo.coercerByColumn.put(DatasetSheetColumn.ACTION, ImportModeCellCoercer.INSTANCE);

		return repo;
	}

	/**
	 * @return
	 */
	private static CellValueCoercerRepository<?> createDatasetParamValuesSheetRepo() {
		CellValueCoercerRepository<DatasetParamValuesSheetColumn> repo = new CellValueCoercerRepository<>();

		repo.coercerByColumn.put(DatasetParamValuesSheetColumn.ACTION, ImportModeCellCoercer.INSTANCE);

		return repo;
	}

	/**
	 * @return
	 */
	private static CellValueCoercerRepository<ParameterSheetColumn> createParamsSheetRepo() {
		CellValueCoercerRepository<ParameterSheetColumn> repo = new CellValueCoercerRepository<>();

		repo.coercerByColumn.put(ParameterSheetColumn.ACTION, ImportModeCellCoercer.INSTANCE);

		return repo;
	}

	/**
	 * @return a {@link CellValueCoercerRepository} suitable for the steps worksheet.
	 */
	private static CellValueCoercerRepository<StepSheetColumn> createStepsSheetRepo() {
		CellValueCoercerRepository<StepSheetColumn> repo = new CellValueCoercerRepository<>();

		repo.coercerByColumn.put(StepSheetColumn.ACTION, ImportModeCellCoercer.INSTANCE);
		repo.coercerByColumn.put(StepSheetColumn.TC_STEP_NUM, OptionalOneBasedIndexCellCoercer.INSTANCE);
		repo.coercerByColumn.put(StepSheetColumn.TC_STEP_IS_CALL_STEP, OptionalBooleanCellCoercer.INSTANCE);
		repo.coercerByColumn.put(StepSheetColumn.TC_STEP_CALL_DATASET, ParamAssignationModeCellCoercer.INSTANCE);

		return repo;
	}

	/**
	 * @return a {@link CellValueCoercerRepository} suitable for the test cases worksheet.
	 */
	private static CellValueCoercerRepository<TestCaseSheetColumn> createTestCasesSheetRepo() {
		CellValueCoercerRepository<TestCaseSheetColumn> repo = new CellValueCoercerRepository<>();

		repo.coercerByColumn.put(TestCaseSheetColumn.TC_NUM, OptionalOneBasedIndexCellCoercer.INSTANCE);
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_WEIGHT_AUTO, OptionalBooleanCellCoercer.INSTANCE);
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_WEIGHT,
				OptionalEnumCellCoercer.forEnum(TestCaseImportance.class));
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_NATURE, new InfoListItemCoercer<ListItemReference>(ListRole.ROLE_NATURE));
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_TYPE, new InfoListItemCoercer<ListItemReference>(ListRole.ROLE_TYPE));
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_STATUS, OptionalEnumCellCoercer.forEnum(TestCaseStatus.class));
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_CREATED_ON, OptionalDateCellCoercer.INSTANCE);
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_MILESTONE, OptionalStringArrayCellCoercer.INSTANCE);

		repo.coercerByColumn.put(TestCaseSheetColumn.ACTION, ImportModeCellCoercer.INSTANCE);

		return repo;
	}

	/**
	 * The default coercer that shall be given when no other is defined.
	 */
	private static final CellValueCoercer<String> DEFAULT_COERCER = StringCellCoercer.INSTANCE;

	private Map<COL, CellValueCoercer<?>> coercerByColumn = new HashMap<>();

	private CellValueCoercerRepository() {
		super();
	}

	/**
	 * Finds a coercer for the given column. When no coercer is available, returns the default coercer
	 *
	 * @param col
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <VAL> CellValueCoercer<VAL> findCoercer(COL col) {
		CellValueCoercer<?> coercer = coercerByColumn.get(col);
		return (CellValueCoercer<VAL>) (coercer == null ? DEFAULT_COERCER : coercer);
	}

	/**
	 * @return the coercer suitable for custom field cells.
	 */
	public CellValueCoercer<String> findCustomFieldCoercer() {
		return StringCellCoercer.INSTANCE;
	}
}
