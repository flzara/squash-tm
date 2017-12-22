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

import java.util.Locale;

import org.springframework.context.MessageSource;


/**
 * 
 * This notifies the user that he tried to remove a folder in milestone mode, yet that folder has a content he cannot see
 * because they won't show indeed in the interface.
 * 
 * @author bsiri
 *
 */
public class MilestoneModeNoFolderDeletion extends MilestoneRuleReport implements SuppressionPreviewReport{

	private static final String MESSAGE_KEY = "message.deletionWarning.milestonesmode.nofoldersallowed";

	public MilestoneModeNoFolderDeletion(String type){
		super(type);
	}

	@Override
	protected String getKey() {
		return MESSAGE_KEY;
	}

	// we don't want to use the super implementation for that one
	@Override
	public String toString(MessageSource source, Locale locale) {
		return source.getMessage(MESSAGE_KEY, null, locale);
	}
}
