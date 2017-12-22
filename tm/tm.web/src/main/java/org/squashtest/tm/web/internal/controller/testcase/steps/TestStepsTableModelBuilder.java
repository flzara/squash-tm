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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.MultiValuedCustomFieldValue;
import org.squashtest.tm.domain.customfield.NumericCustomFieldValue;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.service.internal.dto.NumericCufHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

/**
 * Builds a DataTable model for TestSteps table.
 *
 * @author Gregory Fouquet
 *
 */
public class TestStepsTableModelBuilder extends DataTableModelBuilder<TestStep> implements TestStepVisitor {
	/**
	 *
	 */
	private static final int DEFAULT_MAP_CAPACITY = 16;

	private Map<Long, Map<String, CustomFieldValueTableModel>> customFieldValuesById;

	private Map<?, ?> lastBuiltItem;

	public TestStepsTableModelBuilder() {
		super();
	}

	/**
	 *
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder#buildItemData(java.lang.Object)
	 */
	@Override
	protected Map<?, ?> buildItemData(TestStep item) {
		item.accept(this);
		return lastBuiltItem;
	}

	/**
	 * Creates a model row from the visited item and stores it as {@link #lastBuiltItem}
	 */
	@Override
	public void visit(ActionTestStep visited) {

		Map<Object, Object> item = new HashMap<>(11);

		item.put("step-id", visited.getId());
		item.put("step-index", getCurrentIndex());
		item.put("attach-list-id", visited.getAttachmentList().getId());
		item.put("step-action", visited.getAction());
		item.put("step-result", visited.getExpectedResult());
		item.put("nb-attachments", visited.getAttachmentList().size());
		item.put("step-type", "action");
		item.put("call-step-info", null);
		item.put("empty-requirements-holder", null);
		item.put("empty-browse-holder", null);
		item.put("empty-delete-holder", null);
		item.put("has-requirements", !visited.getRequirementVersionCoverages().isEmpty());
		item.put("nb-requirements", visited.getRequirementVersionCoverages().size());

		appendCustomFields(item);

		lastBuiltItem = item;

	}

	@Override
	public void visit(CallTestStep visited) {
		Map<Object, Object> item = new HashMap<>(11);

		item.put("step-id", visited.getId());
		item.put("step-index", getCurrentIndex());
		item.put("attach-list-id", null);
		item.put("step-action", null);
		item.put("step-result", null);
		item.put("nb-attachments", null);
		item.put("step-type", "call");
		item.put("call-step-info", new CallStepInfo(visited));
		item.put("empty-requirements-holder", null);
		item.put("empty-browse-holder", null);
		item.put("empty-delete-holder", null);
		item.put("has-requirements", false);
		item.put("nb-requirements", null);

		appendCustomFields(item);

		lastBuiltItem = item;

	}

	private void appendCustomFields(Map<Object, Object> item) {
		Map<String, CustomFieldValueTableModel> cufValues = getCustomFieldsFor((Long) item.get("step-id"));
		item.put("customFields", cufValues);

	}

	public void usingCustomFields(Collection<CustomFieldValue> cufValues, int nbFieldsPerEntity) {
		customFieldValuesById = new HashMap<>();

		for (CustomFieldValue value : cufValues) {
			Long entityId = value.getBoundEntityId();
			Map<String, CustomFieldValueTableModel> values = customFieldValuesById.get(entityId);

			if (values == null) {
				values = new HashMap<>(nbFieldsPerEntity);
				customFieldValuesById.put(entityId, values);
			}

			values.put(value.getCustomField().getCode(), new CustomFieldValueTableModel(value));

		}
	}

	protected static class CustomFieldValueTableModel {
		private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldValueTableModel.class);


		private String value;
		private List<String> values;

		private Long id;


		public Object getValue() {
			return value != null ? value : values;
		}

		public void setValue(Object value) {
			if (List.class.isAssignableFrom(value.getClass())){
				this.values = (List<String>) value;
			}
			else if (String.class.isAssignableFrom(value.getClass())){
				this.value = (String)value;
			}
			else{
				throw new IllegalArgumentException("type '"+value.getClass()+"' not supported");
			}
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public CustomFieldValueTableModel() {
			super();
		}

		public Date getValueAsDate() {
			try {
				return DateUtils.parseIso8601Date(value);
			} catch (ParseException | ClassCastException e) {
				LOGGER.debug("Unable to parse date {} of custom field #{}", value, id);
			}

			return null;
		}

		private CustomFieldValueTableModel(CustomFieldValue value) {
			this.id = value.getId();

			if (MultiValuedCustomFieldValue.class.isAssignableFrom(value.getClass())) {
				this.values = ((MultiValuedCustomFieldValue)value).getValues();
			}
			else if(NumericCustomFieldValue.class.isAssignableFrom(value.getClass())){
				this.value = NumericCufHelper.formatOutputNumericCufValue(value.getValue());
			}
			else{
				this.value = value.getValue();
			}
		}

	}

	private Map<String, CustomFieldValueTableModel> getCustomFieldsFor(Long id) {
		if (customFieldValuesById == null) {
			return new HashMap<>();
		}

		Map<String, CustomFieldValueTableModel> values = customFieldValuesById.get(id);

		if (values == null) {
			values = new HashMap<>();
		}
		return values;

	}

	public void usingCustomFields(Collection<CustomFieldValue> cufValues) {
		usingCustomFields(cufValues, DEFAULT_MAP_CAPACITY);
	}


	public static final class CallStepInfo{

		private Long calledTcId;
		private String calledTcName;

		private Long calledDatasetId;
		private String calledDatasetName;

		private String paramMode;

		CallStepInfo(CallTestStep step){
			this.calledTcId = step.getCalledTestCase().getId();
			this.calledTcName = step.getCalledTestCase().getName();

			this.paramMode = step.getParameterAssignationMode().toString();

			if (step.getParameterAssignationMode() == ParameterAssignationMode.CALLED_DATASET){
				this.calledDatasetId = step.getCalledDataset().getId();
				this.calledDatasetName = step.getCalledDataset().getName();
			}

		}

		public Long getCalledTcId() {
			return calledTcId;
		}

		public String getCalledTcName() {
			return calledTcName;
		}

		public Long getCalledDatasetId() {
			return calledDatasetId;
		}

		public String getCalledDatasetName() {
			return calledDatasetName;
		}

		public String getParamMode() {
			return paramMode;
		}

	}

}
