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
package org.squashtest.tm.web.internal.controller.campaign;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.service.internal.dto.RawValueModel;
import org.squashtest.tm.service.internal.dto.RawValueModel.RawValueModelMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

//XSS OK
public class CampaignFormModel {
	/**
	 * Note : the following validation annotations are never called, a custom validator will be invoked for this.
	 *
	 */

	/*@NotBlank
	@NotNull*/
	private String name;

	private String reference;

	private String description;

	private static final String MESSAGE_NOT_BLANK ="message.notBlank";

	/*@NotNull
	@NotEmpty*/
	private RawValueModelMap customFields = new RawValueModelMap();


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getReference() {
		return reference;
	}


	public void setReference(String reference) {
		this.reference = reference;
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public RawValueModelMap getCustomFields() {
		return customFields;
	}


	public void setCustomFields(RawValueModelMap customFields) {
		this.customFields = customFields;
	}



	public Campaign getCampaign(){
		Campaign newCampaign = new Campaign();
		newCampaign.setName(name);
		newCampaign.setDescription(description);
		newCampaign.setReference(reference);
		return newCampaign;
	}

	@JsonIgnore
	public Map<Long, RawValue> getCufs(){
		Map<Long, RawValue> cufs = new HashMap<>(customFields.size());
		for (Entry<Long, RawValueModel> entry : customFields.entrySet()){
			cufs.put(entry.getKey(), entry.getValue().toRawValue());
		}
		return cufs;
	}


	public static class CampaignFormModelValidator implements Validator {

		private MessageSource messageSource;

		public void setMessageSource(MessageSource messageSource){
			this.messageSource = messageSource;
		}



		public CampaignFormModelValidator(MessageSource messageSource) {
			super();
			this.messageSource = messageSource;
		}



		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(CampaignFormModel.class);
		}

		@Override
		public void validate(Object target, Errors errors) {

			String notBlank = messageSource.getMessage(MESSAGE_NOT_BLANK, null, LocaleContextHolder.getLocale());

			CampaignFormModel model = (CampaignFormModel) target;

			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", MESSAGE_NOT_BLANK, notBlank);


			for (Entry<Long, RawValueModel> entry : model.getCustomFields().entrySet()){
				RawValueModel value = entry.getValue();
				if (value.isEmpty()){
					errors.rejectValue("customFields["+entry.getKey()+"]", MESSAGE_NOT_BLANK, notBlank);
				}
			}


		}

	}


}
