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
package org.squashtest.tm.service.internal.denormalizedField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customfield.*;
import org.squashtest.tm.domain.denormalizedfield.*;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.squashtest.tm.service.internal.repository.DenormalizedFieldValueDao;
import org.squashtest.tm.service.internal.repository.DenormalizedFieldValueDeletionDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;

/**
 *
 * @author mpagnon
 *
 */
@Service("squashtest.tm.service.DenormalizedFieldValueManager")
public class PrivateDenormalizedFieldValueServiceImpl implements PrivateDenormalizedFieldValueService {
	@Inject
	private CustomFieldValueDao customFieldValueDao;

	@Inject
	private DenormalizedFieldValueDao denormalizedFieldValueDao;

	@Inject
	private DenormalizedFieldValueDeletionDao denormalizedFieldValueDeletionDao;



	@Inject
	private ExecutionDao execDao;


	@Override
	public void createAllDenormalizedFieldValues(BoundEntity source, DenormalizedFieldHolder destination) {
		List<CustomFieldValue> customFieldValues = customFieldValueDao.findAllCustomValues(source.getBoundEntityId(), source.getBoundEntityType());
		createDenormalizedFieldValues(destination, customFieldValues);
	}



	@Override
	public void createAllDenormalizedFieldValuesForSteps(Execution execution) {

		// fetch execution steps
		List<ExecutionStep> executionSteps = execDao.findSteps(execution.getId());
		if (executionSteps.isEmpty()){
			return;
		}

		// fetch the custom fields of the original steps
		List<Long> originalIds = execDao.findOriginalStepIds(execution.getId());
		List<CustomFieldValue> originalValues = customFieldValueDao.batchedInitializedFindAllCustomValuesFor(originalIds, BindableEntity.TEST_STEP);

		// sort them by original step id
		MultiValueMap cufsPerStepId = new MultiValueMap();
		for (CustomFieldValue value : originalValues){
			cufsPerStepId.put(value.getBoundEntityId(), value);
		}

		// main loop
		for (ExecutionStep estep : executionSteps){
			Collection _values = cufsPerStepId.getCollection(estep.getReferencedTestStep().getId());
			if (_values != null){ // might be null when there are no custom fields
				List<CustomFieldValue> values = new ArrayList<>(_values);
				createDenormalizedFieldValues(estep, values);
			}
		}

	}

	private void createDenormalizedFieldValues(DenormalizedFieldHolder entity, List<CustomFieldValue> values){

		// now loop and create the custom fields;
		for (CustomFieldValue cfv : values ){
			DenormalizedFieldValue dfv;

			switch(cfv.getCustomField().getInputType()){
			case DROPDOWN_LIST :
				dfv = new DenormalizedSingleSelectField(cfv, entity.getDenormalizedFieldHolderId(), entity.getDenormalizedFieldHolderType());
				break;
			case RICH_TEXT :
				dfv = new DenormalizedRichValue(cfv, entity.getDenormalizedFieldHolderId(), entity.getDenormalizedFieldHolderType());
				break;
			case TAG :
				dfv = new DenormalizedMultiSelectField(cfv, entity.getDenormalizedFieldHolderId(), entity.getDenormalizedFieldHolderType());
				break;
			case NUMERIC:
				dfv = new DenormalizedNumericValue(cfv, entity.getDenormalizedFieldHolderId(), entity.getDenormalizedFieldHolderType());
				break;

			default :
				dfv = new DenormalizedFieldValue(cfv, entity.getDenormalizedFieldHolderId(), entity.getDenormalizedFieldHolderType());
			}

			denormalizedFieldValueDao.save(dfv);
		}

	}


	@Override
	public void deleteAllDenormalizedFieldValues(DenormalizedFieldHolder entity) {
		List<DenormalizedFieldValue> dfvs = denormalizedFieldValueDao.findDFVForEntity(entity.getDenormalizedFieldHolderId(), entity.getDenormalizedFieldHolderType());
		for(DenormalizedFieldValue dfv : dfvs){
			denormalizedFieldValueDeletionDao.removeDenormalizedFieldValue(dfv);
		}
	}



	@Override
	public List<DenormalizedFieldValue> findAllForEntity(DenormalizedFieldHolder denormalizedFieldHolder) {
		return denormalizedFieldValueDao.findDFVForEntity(denormalizedFieldHolder.getDenormalizedFieldHolderId(), denormalizedFieldHolder.getDenormalizedFieldHolderType());
	}

	@Override
	public List<DenormalizedFieldValue> findAllForEntityAndRenderingLocation(
			DenormalizedFieldHolder denormalizedFieldHolder, RenderingLocation renderingLocation) {
		return denormalizedFieldValueDao.findDFVForEntityAndRenderingLocation(denormalizedFieldHolder.getDenormalizedFieldHolderId(), denormalizedFieldHolder.getDenormalizedFieldHolderType(), renderingLocation);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<DenormalizedFieldValue> findAllForEntities(Collection<DenormalizedFieldHolder> entities,Collection<RenderingLocation> nullOrLocations) {
		if (entities.isEmpty()){
			return Collections.emptyList();
		}
		else {
			DenormalizedFieldHolderType type = entities.iterator().next().getDenormalizedFieldHolderType();

			Collection<Long> entityIds = CollectionUtils.collect(entities, new Transformer() {
				@Override
				public Object transform(Object input) {
					return ((DenormalizedFieldHolder) input).getDenormalizedFieldHolderId();
				}
			});

			if (nullOrLocations == null){
				return denormalizedFieldValueDao.findDFVForEntities(type, entityIds);
			}
			else{
				return denormalizedFieldValueDao.findDFVForEntitiesAndLocations(type, entityIds, nullOrLocations);
			}
		}
	}

	@Override
	public List<DenormalizedFieldValue> findAllForEntity(Long denormalizedFieldHolderId, DenormalizedFieldHolderType denormalizedFieldHolderType) {
		return denormalizedFieldValueDao.findDFVForEntity(denormalizedFieldHolderId, denormalizedFieldHolderType);
	}

	@Override
	public void changeValue(long denormalizedFieldValueId, RawValue newValue) {

		DenormalizedFieldValue changedValue = denormalizedFieldValueDao.findById(denormalizedFieldValueId);
		newValue.setValueFor(changedValue);

	}

	/**
	 * @see org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager#hasDenormalizedFields(org.squashtest.tm.domain.customfield.BoundEntity)
	 */
	@Override
	public boolean hasDenormalizedFields(DenormalizedFieldHolder entity) {
		return denormalizedFieldValueDao.countDenormalizedFields(entity.getDenormalizedFieldHolderId(), entity.getDenormalizedFieldHolderType()) > 0;
	}




}
