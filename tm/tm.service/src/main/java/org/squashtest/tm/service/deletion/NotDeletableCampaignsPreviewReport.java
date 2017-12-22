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
package org.squashtest.tm.service.deletion;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.MessageSource;

public class NotDeletableCampaignsPreviewReport implements SuppressionPreviewReport {

	private static final String NORIGHT_MESSAGE_KEY_1 = "dialog.label.delete-node.label.specialcase.noright.first";
	private static final String NORIGHT_MESSAGE_KEY_2 = "dialog.label.delete-node.label.specialcase.noright.second";

	private static final String ADMIN_MESSAGE_KEY_1 = "dialog.label.delete-node.label.specialcase.right.first";
	private static final String ADMIN_MESSAGE_KEY_2 = "dialog.label.delete-node.label.specialcase.right.second";
	private static final String ADMIN_MESSAGE_KEY_3 = "dialog.label.delete-node.label.specialcase.right.third";
	private static final String ADMIN_MESSAGE_KEY_4 = "dialog.label.delete-node.label.specialcase.right.fourth";
	private static final String ADMIN_MESSAGE_KEY_5 = "dialog.label.delete-node.label.specialcase.right.last";

	private boolean hasRights;
	private final Set<String> nodeNames = new HashSet<>();

	@Override
	public String toString(MessageSource source, Locale locale) {
		StringBuilder builder = new StringBuilder();

		if(hasRights){
			builder.append(source.getMessage(ADMIN_MESSAGE_KEY_1, null, locale));
			builder.append("<span class='red-warning-message'>");
			builder.append(source.getMessage(ADMIN_MESSAGE_KEY_2, null, locale));
			builder.append(" </span>");
			builder.append(setToString(nodeNames));
			builder.append(source.getMessage(ADMIN_MESSAGE_KEY_3, null, locale));
			builder.append("<span class='red-warning-message'> ");
			builder.append(source.getMessage(ADMIN_MESSAGE_KEY_4, null, locale));
			builder.append("</span>");
			builder.append(source.getMessage(ADMIN_MESSAGE_KEY_5, null, locale));
		} else {
			builder.append(source.getMessage(NORIGHT_MESSAGE_KEY_1, null, locale));
			builder.append("<span> </span>");
			builder.append(setToString(nodeNames));
			builder.append(source.getMessage(NORIGHT_MESSAGE_KEY_2, null, locale));
		}

		return builder.toString();

	}


	public void addName(String name){
		nodeNames.add(name);
	}

	private String setToString(Set<String> set){
		StringBuilder builder = new StringBuilder();
		Iterator<String> iterator = set.iterator();

		if(iterator.hasNext()){
			builder.append(iterator.next());
		}

		while(iterator.hasNext()){
			builder.append(", ").append(iterator.next());
		}

		return builder.toString();

	}

	public boolean isHasRights() {
		return hasRights;
	}


	public void setHasRights(boolean hasRights) {
		this.hasRights = hasRights;
	}
}
