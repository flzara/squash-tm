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
package org.squashtest.tm.web.internal.controller.audittrail;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.event.RequirementAuditEventVisitor;
import org.squashtest.tm.domain.event.RequirementCreation;
import org.squashtest.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.tm.domain.event.RequirementPropertyChange;
import org.squashtest.tm.domain.event.RequirementVersionModification;
import org.squashtest.tm.domain.event.SyncRequirementCreation;
import org.squashtest.tm.domain.event.SyncRequirementUpdate;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

/**
 * Builder for datatable model showing {@link RequirementAuditEvent} objects. Not threadsafe, should be discarded after
 * use.
 *
 * @author Gregory Fouquet
 *
 */
public class RequirementAuditEventTableModelBuilder extends DataTableModelBuilder<RequirementAuditEvent> implements
		RequirementAuditEventVisitor {
	/**
	 * The locale to use to format the labels.
	 */
	private final Locale locale;
	/**
	 * The source for localized label messages.
	 */
	private final InternationalizationHelper i18nHelper;

	/**
	 * Data for the item currently build.
	 */
	private Map<String, String> currentItemData;

	/**
	 * @param locale
	 * @param  messageSource
	 */
	public RequirementAuditEventTableModelBuilder(@NotNull Locale locale, @NotNull InternationalizationHelper messageSource) {
		super();
		this.i18nHelper = messageSource;
		this.locale = locale;
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder#buildItemData(java.lang.Object)
	 */
	@Override
	protected Map<String, String> buildItemData(RequirementAuditEvent item) {
		item.accept(this);

		return currentItemData;
	}

	/**
	 * @see org.squashtest.tm.domain.event.RequirementAuditEventVisitor#visit(org.squashtest.tm.domain.event.RequirementCreation)
	 */
	@Override
	public void visit(RequirementCreation event) {
		String message = i18nHelper.internationalize("label.Creation", locale);
		populateCurrentItemData(message, "creation", event);
	}
	
	
	@Override
	public void visit(SyncRequirementCreation event) {
		String message = i18nHelper.internationalize("label.CreationBySynchronization", locale);
		populateCurrentItemData(message, "sync-creation", event);
		currentItemData.put("event-meta", event.getSource());
		
	}
	
	@Override
	public void visit(SyncRequirementUpdate event) {
		String message = i18nHelper.internationalize("label.UpdateBySynchronization", locale);
		populateCurrentItemData(message, "sync-update", event);		
		currentItemData.put("event-meta", event.getSource());
	}
	

	/**
	 * @see org.squashtest.tm.domain.event.RequirementAuditEventVisitor#visit(org.squashtest.tm.domain.event.RequirementPropertyChange)
	 */
	@Override
	public void visit(RequirementPropertyChange event) {
		Object[] args = buildMessageArgs(event);

		@SuppressWarnings("deprecation")
		String message = i18nHelper.getMessage(buildPropertyChangeMessageKey(event), args, locale);
		populateCurrentItemData(message, "simple-prop", event);
	}

	private String buildPropertyChangeMessageKey(RequirementVersionModification event) {
		return "message.property-change." + event.getPropertyName() + ".label";
	}

	private Object[] buildMessageArgs(RequirementPropertyChange event) {
		Object[] args;
		
		if (propertyIsEnumeratedAndInternationalizable(event)) {
			args = buildMessageArgsForI18nableEnumProperty(event);
		}
		else if (propertyIsInfolist(event)){
			args = buildMessageArgsForI18ableInfoListProperty(event);
		}
		else{
			args = buildMessageArgsForStringProperty(event); 
		}
		
		return args;
	}

	private boolean propertyIsEnumeratedAndInternationalizable(RequirementPropertyChange event) {
		Field field = ReflectionUtils.findField(RequirementVersion.class, event.getPropertyName());
		Class<?> fieldType = field.getType();

		return Enum.class.isAssignableFrom(fieldType) && Internationalizable.class.isAssignableFrom(fieldType);
	}
	
	private boolean propertyIsInfolist(RequirementPropertyChange event){
		Field field = ReflectionUtils.findField(RequirementVersion.class, event.getPropertyName());
		Class<?> fieldType = field.getType();

		return InfoListItem.class.isAssignableFrom(fieldType);
		
	}

	private Object[] buildMessageArgsForStringProperty(RequirementPropertyChange event) {
		return new Object[] { event.getOldValue(), event.getNewValue() };
	}

	private Object[] buildMessageArgsForI18nableEnumProperty(RequirementPropertyChange event) {
		Field enumField = ReflectionUtils.findField(RequirementVersion.class, event.getPropertyName());
		Class<?> enumType = enumField.getType();

		String oldValueLabel = retrieveEnumI18ndLabel(enumType, event.getOldValue());
		String newValueLabel = retrieveEnumI18ndLabel(enumType, event.getNewValue());

		return new Object[] { oldValueLabel, newValueLabel };
	}
	
	
	/*
	 * Unfortunately there is no way to tell which subtype of InfoListItem is being processed there : 
	 * the SystemListItem is i18nable while the UserListItem is not. So let's hope for the best : 
	 * the i18nhelper should return the key itself if not found in the messagesource
	 */
	private Object[]  buildMessageArgsForI18ableInfoListProperty(RequirementPropertyChange event){

		return new Object[]{
			i18nHelper.getMessage(event.getOldValue(), null, event.getOldValue(), locale),
			i18nHelper.getMessage(event.getNewValue(), null, event.getNewValue(), locale)
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String retrieveEnumI18ndLabel(Class enumType, String stringValue) {
		Internationalizable enumValue = (Internationalizable) Enum.valueOf(enumType, stringValue);
		return i18nHelper.internationalize(enumValue, locale);
	}

	/**
	 * @see org.squashtest.tm.domain.event.RequirementAuditEventVisitor#visit(org.squashtest.tm.domain.event.RequirementLargePropertyChange)
	 */
	@Override
	public void visit(RequirementLargePropertyChange event) {
		String message = i18nHelper.internationalize(buildPropertyChangeMessageKey(event), locale);
		populateCurrentItemData(message, "fat-prop", event);

	}


	private void populateCurrentItemData(String message, String eventType, RequirementAuditEvent event) {

		String formattedDate = DateFormatUtils.format(event.getDate(), "dd/MM/yyyy HH'h'mm");
		String escapedAuthor = HtmlUtils.htmlEscape(event.getAuthor());
		String escapedMessage = HtmlUtils.htmlEscape(message);

		Map<String, String> row = new HashMap<>(5);

		row.put("event-date", formattedDate);
		row.put("event-author", escapedAuthor);
		row.put("event-message", escapedMessage);
		row.put("event-type", eventType);
		row.put("event-id", String.valueOf(event.getId()));

		currentItemData = row;

	}
}
