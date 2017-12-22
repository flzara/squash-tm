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
package org.squashtest.tm.service.internal.customfield;

import org.apache.commons.lang3.EnumUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.*;
import org.squashtest.tm.service.customfield.CustomFieldModelService;
import org.squashtest.tm.service.internal.dto.*;
import org.squashtest.tm.service.internal.workspace.StreamUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.squashtest.tm.jooq.domain.Tables.*;
import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_FIELD_RENDERING_LOCATION;

@Service
@Transactional(readOnly = true)
public class CustomFieldModelServiceImpl implements CustomFieldModelService {

	@Inject
	private DSLContext DSL;

	@Inject
	private MessageSource messageSource;

	@Override
	public Map<Long, Map<String, List<CustomFieldBindingModel>>> findCustomFieldsBindingsByProject(List<Long> projectIds) {
		Map<Long, CustomFieldModel> cufMap = findUsedCustomFields(projectIds);
		return findCustomFieldsBindingsByProject(projectIds, cufMap);
	}

	protected List<Long> findUsedCustomFieldIds(List<Long> readableProjectIds) {
		return DSL
			.selectDistinct(CUSTOM_FIELD_BINDING.CF_ID)
			.from(CUSTOM_FIELD_BINDING)
			.where(CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID.in(readableProjectIds))
			.fetch(CUSTOM_FIELD_BINDING.CF_ID, Long.class);
	}

	public Map<Long, CustomFieldModel> findAllUsedCustomFieldsByEntity(List<Long> projectIds,BindableEntity entity) {
//		List<Long> usedCufIds = findUsedCustomFieldIds(projectIds);
		return findCufMapByEntityType(projectIds,entity);
	}

	private Map<Long, CustomFieldModel> findUsedCustomFields(List<Long> projectIds) {
		List<Long> usedCufIds = findUsedCustomFieldIds(projectIds);
		return findCufMap(usedCufIds);
	}


	protected Map<Long, CustomFieldModel> findCufMapByEntityType(List<Long> usedCufIds,BindableEntity entity) {
		Map<Long, CustomFieldModel> cufMap = new HashMap<>();

		DSL.selectDistinct(CUSTOM_FIELD.CF_ID, CUSTOM_FIELD.INPUT_TYPE, CUSTOM_FIELD.NAME, CUSTOM_FIELD.LABEL, CUSTOM_FIELD.CODE, CUSTOM_FIELD.OPTIONAL, CUSTOM_FIELD.DEFAULT_VALUE, CUSTOM_FIELD.LARGE_DEFAULT_VALUE
			, CUSTOM_FIELD_OPTION.CODE, CUSTOM_FIELD_OPTION.LABEL, CUSTOM_FIELD_OPTION.POSITION)
			.from(CUSTOM_FIELD)
			.leftJoin(CUSTOM_FIELD_OPTION).using(CUSTOM_FIELD.CF_ID)
			.join(CUSTOM_FIELD_BINDING).on(CUSTOM_FIELD.CF_ID.eq(CUSTOM_FIELD_BINDING.CF_ID))
			.where(CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID.in(usedCufIds))
			.and(CUSTOM_FIELD_BINDING.BOUND_ENTITY.eq(entity.toString()))
			.fetch()
			.forEach(r -> {
				Long cufId = r.get(CUSTOM_FIELD.CF_ID);
				String type = r.get(CUSTOM_FIELD.INPUT_TYPE);
				InputType inputType = EnumUtils.getEnum(InputType.class, type);
				switch (inputType) {
					case RICH_TEXT:
						CustomFieldModel richTextCustomFieldModel = getRichTextCustomFieldModel(r);
						cufMap.put(richTextCustomFieldModel.getId(), richTextCustomFieldModel);
						break;
					//here is the not fun case
					//as we have made a left join, we can have the first tuple witch need to be treated as a cuf AND an option
					//or subsequent tuple witch must be treated only as option...
					case DROPDOWN_LIST:
						if (cufMap.containsKey(cufId)) {
							CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel = (CustomFieldModelFactory.SingleSelectFieldModel) cufMap.get(cufId);
							singleSelectFieldModel.addOption(getCufValueOptionModel(r));
						} else {
							CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel = getSingleSelectFieldModel(r);
							singleSelectFieldModel.addOption(getCufValueOptionModel(r));
							cufMap.put(singleSelectFieldModel.getId(), singleSelectFieldModel);
						}
						break;

					case DATE_PICKER:
						CustomFieldModel datePickerCustomFieldModel = getDatePickerCustomFieldModel(r);
						cufMap.put(datePickerCustomFieldModel.getId(), datePickerCustomFieldModel);
						break;

					case TAG:
						if (cufMap.containsKey(cufId)) {
							CustomFieldModelFactory.MultiSelectFieldModel multiSelectFieldModel = (CustomFieldModelFactory.MultiSelectFieldModel) cufMap.get(cufId);
							multiSelectFieldModel.addOption(getCufValueOptionModel(r));
						} else {
							CustomFieldModelFactory.MultiSelectFieldModel multiSelectFieldModel = getMultiSelectFieldModel(r);
							multiSelectFieldModel.addOption(getCufValueOptionModel(r));
							cufMap.put(multiSelectFieldModel.getId(), multiSelectFieldModel);
						}
						break;

					default:
						CustomFieldModel cufModel = getSingleValueCustomFieldModel(r);
						cufMap.put(cufId, cufModel);
				}
			});

		return cufMap;
	}







	protected Map<Long, CustomFieldModel> findCufMap(List<Long> usedCufIds) {
		Map<Long, CustomFieldModel> cufMap = new HashMap<>();

		DSL.selectDistinct(CUSTOM_FIELD.CF_ID, CUSTOM_FIELD.INPUT_TYPE, CUSTOM_FIELD.NAME, CUSTOM_FIELD.LABEL, CUSTOM_FIELD.CODE, CUSTOM_FIELD.OPTIONAL, CUSTOM_FIELD.DEFAULT_VALUE, CUSTOM_FIELD.LARGE_DEFAULT_VALUE
			, CUSTOM_FIELD_OPTION.CODE, CUSTOM_FIELD_OPTION.LABEL, CUSTOM_FIELD_OPTION.POSITION)
			.from(CUSTOM_FIELD)
			.leftJoin(CUSTOM_FIELD_OPTION).using(CUSTOM_FIELD.CF_ID)
			.where(CUSTOM_FIELD.CF_ID.in(usedCufIds))
			.fetch()
			.forEach(r -> {
				Long cufId = r.get(CUSTOM_FIELD.CF_ID);
				String type = r.get(CUSTOM_FIELD.INPUT_TYPE);
				InputType inputType = EnumUtils.getEnum(InputType.class, type);
				switch (inputType) {
					case RICH_TEXT:
						CustomFieldModel richTextCustomFieldModel = getRichTextCustomFieldModel(r);
						cufMap.put(richTextCustomFieldModel.getId(), richTextCustomFieldModel);
						break;
					//here is the not fun case
					//as we have made a left join, we can have the first tuple witch need to be treated as a cuf AND an option
					//or subsequent tuple witch must be treated only as option...
					case DROPDOWN_LIST:
						if (cufMap.containsKey(cufId)) {
							CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel = (CustomFieldModelFactory.SingleSelectFieldModel) cufMap.get(cufId);
							singleSelectFieldModel.addOption(getCufValueOptionModel(r));
						} else {
							CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel = getSingleSelectFieldModel(r);
							singleSelectFieldModel.addOption(getCufValueOptionModel(r));
							cufMap.put(singleSelectFieldModel.getId(), singleSelectFieldModel);
						}
						break;

					case DATE_PICKER:
						CustomFieldModel datePickerCustomFieldModel = getDatePickerCustomFieldModel(r);
						cufMap.put(datePickerCustomFieldModel.getId(), datePickerCustomFieldModel);
						break;

					case TAG:
						if (cufMap.containsKey(cufId)) {
							CustomFieldModelFactory.MultiSelectFieldModel multiSelectFieldModel = (CustomFieldModelFactory.MultiSelectFieldModel) cufMap.get(cufId);
							multiSelectFieldModel.addOption(getCufValueOptionModel(r));
						} else {
							CustomFieldModelFactory.MultiSelectFieldModel multiSelectFieldModel = getMultiSelectFieldModel(r);
							multiSelectFieldModel.addOption(getCufValueOptionModel(r));
							cufMap.put(multiSelectFieldModel.getId(), multiSelectFieldModel);
						}
						break;

					default:
						CustomFieldModel cufModel = getSingleValueCustomFieldModel(r);
						cufMap.put(cufId, cufModel);
				}
			});

		return cufMap;
	}

		private CustomFieldModelFactory.MultiSelectFieldModel getMultiSelectFieldModel(Record r) {
		CustomFieldModelFactory.MultiSelectFieldModel multiSelectFieldModel = new CustomFieldModelFactory.MultiSelectFieldModel();
		initCufModel(r, multiSelectFieldModel);
		for (String value : r.get(CUSTOM_FIELD.DEFAULT_VALUE).split(MultiSelectField.SEPARATOR_EXPR)) {
			multiSelectFieldModel.addDefaultValue(value);
		}
		return multiSelectFieldModel;
	}

	private CustomFieldModelFactory.SingleSelectFieldModel getSingleSelectFieldModel(Record r) {
		CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel = new CustomFieldModelFactory.SingleSelectFieldModel();
		initCufModel(r, singleSelectFieldModel);
		singleSelectFieldModel.setDefaultValue(r.get(CUSTOM_FIELD.DEFAULT_VALUE));
		return singleSelectFieldModel;
	}

	private CustomFieldModelFactory.CustomFieldOptionModel getCufValueOptionModel(Record r) {
		CustomFieldModelFactory.CustomFieldOptionModel optionModel = new CustomFieldModelFactory.CustomFieldOptionModel();
		optionModel.setCode(r.get(CUSTOM_FIELD_OPTION.CODE));
		optionModel.setLabel(r.get(CUSTOM_FIELD_OPTION.LABEL));
		return optionModel;
	}

	private CustomFieldModel getDatePickerCustomFieldModel(Record r) {
		CustomFieldModelFactory.DatePickerFieldModel cufModel = new CustomFieldModelFactory.DatePickerFieldModel();
		initCufModel(r, cufModel);
		Locale locale = LocaleContextHolder.getLocale();
		cufModel.setFormat(getMessage("squashtm.dateformatShort.datepicker"));
		cufModel.setLocale(locale.toString());
		cufModel.setDefaultValue(r.get(CUSTOM_FIELD.DEFAULT_VALUE));
		return cufModel;
	}

	private CustomFieldModel getRichTextCustomFieldModel(Record r) {
		CustomFieldModelFactory.SingleValuedCustomFieldModel cufModel = new CustomFieldModelFactory.SingleValuedCustomFieldModel();
		initCufModel(r, cufModel);
		cufModel.setDefaultValue(r.get(CUSTOM_FIELD.LARGE_DEFAULT_VALUE));
		return cufModel;
	}

	//Take care if you change the JOOQ request, the result can become incompatible.
	private CustomFieldModelFactory.SingleValuedCustomFieldModel getSingleValueCustomFieldModel(Record r) {
		CustomFieldModelFactory.SingleValuedCustomFieldModel cufModel = new CustomFieldModelFactory.SingleValuedCustomFieldModel();
		initCufModel(r, cufModel);
		cufModel.setDefaultValue(r.get(CUSTOM_FIELD.DEFAULT_VALUE));
		return cufModel;
	}

	private void initCufModel(Record r, CustomFieldModel cufModel) {
		cufModel.setId(r.get(CUSTOM_FIELD.CF_ID));
		cufModel.setCode(r.get(CUSTOM_FIELD.CODE));
		cufModel.setName(r.get(CUSTOM_FIELD.NAME));
		cufModel.setLabel(r.get(CUSTOM_FIELD.LABEL));
		cufModel.setOptional(r.get(CUSTOM_FIELD.OPTIONAL));

		cufModel.setDenormalized(false);

		InputTypeModel inputTypeModel = new InputTypeModel();
		String inputTypeKey = r.get(CUSTOM_FIELD.INPUT_TYPE);
		InputType inputType = EnumUtils.getEnum(InputType.class, inputTypeKey);
		inputTypeModel.setEnumName(inputTypeKey);
		inputTypeModel.setFriendlyName(getMessage(inputType.getI18nKey()));

		cufModel.setInputType(inputTypeModel);
	}

	private Map<Long, Map<String, List<CustomFieldBindingModel>>> findCustomFieldsBindingsByProject(List<Long> readableProjectIds, Map<Long, CustomFieldModel> cufMap) {
		Result result = DSL
			.selectDistinct(CUSTOM_FIELD_BINDING.CFB_ID, CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID, CUSTOM_FIELD_BINDING.POSITION, CUSTOM_FIELD_BINDING.BOUND_ENTITY, CUSTOM_FIELD_BINDING.CF_ID
				, CUSTOM_FIELD_RENDERING_LOCATION.RENDERING_LOCATION)
			.from(CUSTOM_FIELD_BINDING)
			.leftJoin(CUSTOM_FIELD_RENDERING_LOCATION).on(CUSTOM_FIELD_BINDING.CFB_ID.eq(CUSTOM_FIELD_RENDERING_LOCATION.CFB_ID))
			.where(CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID.in(readableProjectIds))
			.fetch();

		Function<Record,CustomFieldBindingModel> customFieldBindingModelTransformer = getCustomFieldModelTransformer(cufMap);

		Function<Record, RenderingLocationModel> renderingLocationModelTransformer = getRenderingLocationModelTransformer();

		//we inject the rendering location directly inside the binding model
		Function<Map.Entry<CustomFieldBindingModel,List<RenderingLocationModel>>, CustomFieldBindingModel> injector = entry -> {
			CustomFieldBindingModel bindingModel = entry.getKey();
			List<RenderingLocationModel> renderingLocationModels = entry.getValue();
			bindingModel.setRenderingLocations(renderingLocationModels.toArray(new RenderingLocationModel[]{}));
			return bindingModel;
		};

		List<CustomFieldBindingModel> list = StreamUtils.performJoinAggregate(customFieldBindingModelTransformer, renderingLocationModelTransformer, injector, result);

		Map<Long, Map<String, List<CustomFieldBindingModel>>> cufBindingsByProject = groupByProjectAndType(list);

		for (Long id : readableProjectIds) {
			if(!cufBindingsByProject.containsKey(id)){
				cufBindingsByProject.put(id, createEmptyCufMap());
			}
		}

		return cufBindingsByProject;
	}

	private Map<Long, Map<String, List<CustomFieldBindingModel>>> groupByProjectAndType(List<CustomFieldBindingModel> list) {
		return list.stream().collect(
			groupingBy(CustomFieldBindingModel::getProjectId, //we groupBy project id
				//and we groupBy bindable entity, with an initial map already initialized with empty lists as required per model.
				groupingBy((CustomFieldBindingModel customFieldBindingModel) -> customFieldBindingModel.getBoundEntity().getEnumName(),
					() -> {
						//here we create the empty list, initial step of the reducing operation
						HashMap<String, List<CustomFieldBindingModel>> map = createEmptyCufMap();
						return map;
					},
					mapping(
						Function.identity(),
						toList()
					))
			));
	}

	private HashMap<String, List<CustomFieldBindingModel>> createEmptyCufMap() {
		HashMap<String, List<CustomFieldBindingModel>> map = new HashMap<>();
		EnumSet<BindableEntity> bindableEntities = EnumSet.allOf(BindableEntity.class);
		bindableEntities.forEach(bindableEntity -> {
            map.put(bindableEntity.name(), new ArrayList<>());
        });
		return map;
	}

	private Function<Record, RenderingLocationModel> getRenderingLocationModelTransformer() {
		return r -> {
			String renderingLocationKey = r.get(CUSTOM_FIELD_RENDERING_LOCATION.RENDERING_LOCATION);
			if (renderingLocationKey == null) {
				return null;//it's ok, we collect with a null filtering collector
			}
			RenderingLocationModel renderingLocationModel = new RenderingLocationModel();
			RenderingLocation renderingLocation = EnumUtils.getEnum(RenderingLocation.class, renderingLocationKey);
			renderingLocationModel.setEnumName(renderingLocationKey);
			renderingLocationModel.setFriendlyName(getMessage(renderingLocation.getI18nKey()));
			return renderingLocationModel;
		};
	}

	/**
	 * Return a function that will be responsible to transform a tuple to CustomFieldBidingModel
	 * @param cufMap a pre fetched map
	 * @return the function that will be called to transform a Tuple to a CustomFieldBindingModel
	 */
	private Function<Record, CustomFieldBindingModel> getCustomFieldModelTransformer(Map<Long, CustomFieldModel> cufMap) {
		return r -> {//creating a map <CustomFieldBindingModel, List<RenderingLocationModel>>
			//here we create custom field binding model
			//double created by joins are filtered by the grouping by as we have implemented equals on id attribute
			CustomFieldBindingModel customFieldBindingModel = new CustomFieldBindingModel();
			customFieldBindingModel.setId(r.get(CUSTOM_FIELD_BINDING.CFB_ID));
			customFieldBindingModel.setProjectId(r.get(CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID));
			customFieldBindingModel.setPosition(r.get(CUSTOM_FIELD_BINDING.POSITION));
			String boundEntityKey = r.get(CUSTOM_FIELD_BINDING.BOUND_ENTITY);
			BindableEntity bindableEntity = EnumUtils.getEnum(BindableEntity.class, boundEntityKey);
			BindableEntityModel bindableEntityModel = new BindableEntityModel();
			bindableEntityModel.setEnumName(boundEntityKey);
			bindableEntityModel.setFriendlyName(getMessage(bindableEntity.getI18nKey()));
			customFieldBindingModel.setBoundEntity(bindableEntityModel);
			customFieldBindingModel.setCustomField(cufMap.get(r.get(CUSTOM_FIELD_BINDING.CF_ID)));
			return customFieldBindingModel;
		};
	}

	private String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}


}
