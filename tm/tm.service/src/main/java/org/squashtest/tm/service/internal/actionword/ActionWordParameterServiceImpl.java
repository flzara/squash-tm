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
package org.squashtest.tm.service.internal.actionword;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.util.ActionWordUtil;
import org.squashtest.tm.exception.actionword.InvalidActionWordInputException;
import org.squashtest.tm.exception.actionword.InvalidActionWordParameterValueException;
import org.squashtest.tm.service.actionword.ActionWordLibraryNodeService;
import org.squashtest.tm.service.actionword.ActionWordParameterService;
import org.squashtest.tm.service.internal.repository.ActionWordParameterDao;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

@Service
@Transactional
public class ActionWordParameterServiceImpl implements ActionWordParameterService {

	@Inject
	private ActionWordParameterDao actionWordParameterDao;

	@Inject
	private ActionWordLibraryNodeService actionWordLibraryNodeService;

	@Override
	public String renameParameter(long parameterId, String newName) {
		ActionWordParameter parameter = actionWordParameterDao.getOne(parameterId);
		parameter.setName(newName);
		ActionWord actionWord = parameter.getActionWord();

		checkNewActionWordLength(actionWord);

		// update ActionWordLibraryNode name
		actionWordLibraryNodeService.renameNodeFromActionWord(actionWord);
		return newName;
	}

	private void checkNewActionWordLength(ActionWord actionWord) {
		String newWord = actionWord.createWord();
		String newWordWithDefaultValues = actionWord.createWordWithDefaultValues();
		if (newWord.length() > 255 || newWordWithDefaultValues.length() > 255 ) {
			throw new InvalidActionWordInputException("Invalid action word input");
		}
	}

	@Override
	public String updateParameterDefaultValue(long parameterId, @NotNull String newDefaultValue) {
		ActionWordParameter parameter = actionWordParameterDao.getOne(parameterId);

		String updatedNewDefaultValue = ActionWordUtil.replaceExtraSpacesInText(newDefaultValue.trim());

		if (updatedNewDefaultValue.contains("<")
			|| updatedNewDefaultValue.contains(">")
			|| updatedNewDefaultValue.contains("\"")){
			throw new InvalidActionWordParameterValueException("The default value cannot contain <, > or \" character");
		}
		parameter.setDefaultValue(updatedNewDefaultValue);
		ActionWord actionWord = parameter.getActionWord();

		checkNewActionWordLength(actionWord);

		return updatedNewDefaultValue;
	}
}
