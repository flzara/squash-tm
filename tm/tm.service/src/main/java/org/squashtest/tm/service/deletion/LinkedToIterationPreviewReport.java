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



public class LinkedToIterationPreviewReport implements SuppressionPreviewReport {

	private static final String NODES_NAMES_MESSAGE_KEY = "message.deletionWarning.testCase.nodeNames";
	private static final String NODE_NAME_MESSAGE_KEY = "message.deletionWarning.testCase.nodeNames.single";
	private static final String WHY_MESSAGE_KEY = "message.deletionWarning.testCase.why.linkedToIteration";
	private static final String WHY_SINGLE_MESSAGE_KEY = "message.deletionWarning.testCase.why.linkedToIteration.single";
	private final Set<String> nodeNames = new HashSet<>();
	@Override
	public String toString(MessageSource source, Locale locale) {
		StringBuilder builder = new StringBuilder();

		if (! nodeNames.isEmpty()){
			String firstMessageKey;
			String secondMessageKey;
			if( nodeNames.size() > 1){
				firstMessageKey = NODES_NAMES_MESSAGE_KEY;
				secondMessageKey = WHY_MESSAGE_KEY;
			}else{
				firstMessageKey = NODE_NAME_MESSAGE_KEY;
				secondMessageKey = WHY_SINGLE_MESSAGE_KEY;
			}
			builder.append(source.getMessage(firstMessageKey, null, locale));
			builder.append(setToString(nodeNames));
			builder.append("<br/>");

			builder.append(source.getMessage(secondMessageKey, null, locale));
			builder.append("<br/>");

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
}
