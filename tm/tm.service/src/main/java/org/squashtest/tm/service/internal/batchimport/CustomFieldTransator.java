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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  04/05/16
 */
@Component
@Scope("prototype")
class CustomFieldTransator {
	private final Map<String, CustomFieldInfos> cufInfosCache = new HashMap<>();
	
	@Inject
	private CustomFieldDao customFieldDao;

	/**
	 * because the service identifies cufs by their id, not their code<br/>
	 * also populates the cache (cufIdByCode), and transform the input data in a
	 * single string or a collection of string depending on the type of the
	 * custom field (Tags on non-tags).
	 *
	 * @param valueByCode string representation of the custom field values mapped by the custom field code
	 * @return RawValue representation of the CF values mapped by the CF id
	 */
	protected final Map<Long, RawValue> toAcceptableCufs(Map<String, String> valueByCode) {

		Map<Long, RawValue> result = new HashMap<>(valueByCode.size());

		for (Map.Entry<String, String> entry : valueByCode.entrySet()) {
			String requestedCode = entry.getKey();

			if (!cufInfosCache.containsKey(requestedCode)) {
				loadCustomFieldByCode(requestedCode);
			}

			// now add to our map the id of the custom field, except if null :
			// the custom field
			// does not exist and therefore wont be included.
			CustomFieldInfos infos = cufInfosCache.get(requestedCode);
			if (infos != null) {
				String requestedValue = entry.getValue();
				switch (infos.getType()) {
					case TAG:
						List<String> values = requestedValue == null ? Collections.<String>emptyList() : Arrays.asList(requestedValue.split("\\|"));
						result.put(infos.getId(), new RawValue(values));
						break;
					default:
						result.put(infos.getId(), new RawValue(requestedValue));
						break;
				}
			}
		}

		return result;

	}
	
	/**
	 * Returns the input type of a customfield given its code. Returns null 
	 * if no such customfield exists.
	 * 
	 * @param cufCode
	 * @return
	 */
	protected final InputType getInputTypeFor(String cufCode){
		InputType response = null;
		
		if (!cufInfosCache.containsKey(cufCode)) {
			loadCustomFieldByCode(cufCode);
		}
		
		CustomFieldInfos infos = cufInfosCache.get(cufCode);
		if (infos != null) {
			response = infos.getType();
		}

		return response;
	}
	
	private void loadCustomFieldByCode(String code){
		CustomField customField = customFieldDao.findByCode(code);

		// that bit of code checks that if the custom field doesn't
		// exist, the hashmap entry contains
		// a dummy value for this code.
		CustomFieldInfos infos = null;
		if (customField != null) {
			Long id = customField.getId();
			InputType type = customField.getInputType();
			infos = new CustomFieldInfos(id, type);
		}

		cufInfosCache.put(code, infos);
	}
}
