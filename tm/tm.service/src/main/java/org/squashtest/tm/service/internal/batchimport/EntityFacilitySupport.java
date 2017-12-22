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

import org.squashtest.tm.domain.customfield.*;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;

import javax.inject.Inject;
import java.util.*;

/**
 * Implementors of this class usually are collaborators of "prototype" scope. They should share the same.
 * #customFieldTranslator attribute, which should be manually initialized.
 * 
 * Same remark for the #validator, because they need to share the same {@link Model}
 * 
 * @author Gregory Fouquet
 * @since x.y.z  04/05/16
 */
abstract class EntityFacilitySupport {
	static final String UNEXPECTED_ERROR_WHILE_IMPORTING = "unexpected error while importing ";

	static final String EXCEL_ERR_PREFIX = "Excel import : ";

	@Inject
	protected MilestoneImportHelper milestoneHelper;
	@Inject
	private PrivateCustomFieldValueService cufvalueService;

        
    //manually-injected attributes        
	private CustomFieldTransator customFieldTransator;
        
	protected ValidationFacility validator;

	/**
	 * Returnd the ids of the milestones to be bound as per test case instruction
	 *
	 * @param instr the instruction holding the names of candidate milestones
	 * @return
	 */
	protected final List<Long> boundMilestonesIds(Milestoned instr) {
		Collection<String> milestones = instr.getMilestones();
		if (milestones.isEmpty()) {
			return Collections.emptyList();
		}

		List<Milestone> ms = milestoneHelper.findBindable(milestones);
		List<Long> msids = new ArrayList<>(ms.size());
		for (Milestone m : ms) {
			msids.add(m.getId());
		}
		return msids;
	}

	/**
	 * because the service identifies cufs by their id, not their code<br/>
	 * also populates the cache (cufIdByCode), and transform the input data in a
	 * single string or a collection of string depending on the type of the
	 * custom field (Tags on non-tags).
	 */
	protected final Map<Long, RawValue> toAcceptableCufs(Map<String, String> origCufs) {
		if (customFieldTransator == null) {
			// programmer screwed up manual init of this class
			throw new IllegalStateException("'customFieldTransator' is null, it should have been initialized by FacilityImpl in a @PostConstruct method");
		}
		return customFieldTransator.toAcceptableCufs(origCufs);
	}
	
	/**
	 * Returns the input type of a customfield given its code. Returns null 
	 * if no such customfield exists.
	 * 
	 * @param cufCode
	 * @return
	 */
	protected final InputType getInputTypeFor(String cufCode){
		return customFieldTransator.getInputTypeFor(cufCode);
	}

	protected final void doUpdateCustomFields(Map<String, String> cufValues, BoundEntity bindableEntity) {

		List<CustomFieldValue> cufs = cufvalueService.findAllCustomFieldValues(bindableEntity);
		Set<String> codeSet = cufValues.keySet();
		for (CustomFieldValue v : cufs) {
			String code = v.getCustomField().getCode();
			String newValue = cufValues.get(code);
			if (codeSet.contains(code)) {
				v.setValue(newValue);
			}
		}

	}

	public void initializeCustomFieldTransator(CustomFieldTransator customFieldTransator) {
		this.customFieldTransator = customFieldTransator;
	}
        
    public void initializeValidator(ValidationFacility validator){
        this.validator = validator;
    }
}
