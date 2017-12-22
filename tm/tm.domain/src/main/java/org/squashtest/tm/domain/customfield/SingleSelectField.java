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
package org.squashtest.tm.domain.customfield;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.exception.WrongStringSizeException;
import org.squashtest.tm.exception.customfield.CannotDeleteDefaultOptionException;
import org.squashtest.tm.exception.customfield.CodeAlreadyExistsException;
import org.squashtest.tm.exception.customfield.CodeDoesNotMatchesPatternException;
import org.squashtest.tm.exception.customfield.OptionAlreadyExistException;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A CustomField which stores a single option selected from a list.
 *
 * @author Gregory Fouquet
 */

@Entity
@DiscriminatorValue("SSF")
public class SingleSelectField extends CustomField {

	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_OPTION", joinColumns = @JoinColumn(name = "CF_ID"))
	@OrderColumn(name = "POSITION")
	@Valid
	private List<CustomFieldOption> options = new ArrayList<>();

	/**
	 * Created a SingleSelectField with a
	 */
	public SingleSelectField() {
		super(InputType.DROPDOWN_LIST);
	}

	/**
	 * Will check if label and the code are available among the existing options. If so, will add the new option at the
	 * end of the list. Else will throw a NameAlreadyInUseException or CodeAlreadyExistsException.
	 *
	 * @param option : the new option
	 */
	public void addOption(CustomFieldOption option) {
		checkLabelAvailable(option.getLabel());
		checkCodeAvailable(option.getCode());
		// TODO fix [Task 1682] and remove this line
		checkCodeMatchesPattern(option.getCode());
		options.add(option);
	}

	// TODO fix [Task 1682] and remove this method
	// Also, aded a way to check if code is not only spaces
	private void checkCodeMatchesPattern(String code) {
		if (!code.matches(CODE_REGEXP)  && code.trim().isEmpty()) {
			throw new CodeDoesNotMatchesPatternException(code, CODE_REGEXP, "optionCode");
		}
		if (code.length() > MAX_CODE_SIZE || code.length() < MIN_CODE_SIZE) {
			throw new WrongStringSizeException("code", MIN_CODE_SIZE, MAX_CODE_SIZE);
		}
	}

	private void checkCodeAvailable(String code) {
		if (!isCodeAvailable(code)) {
			throw new CodeAlreadyExistsException(null, code, CustomFieldOption.class);
		}
	}

	private void checkLabelAvailable(String label) {
		// TODO fix [Task 1682] and remove the first check
		if (label.length() > 255 || label.length() < 1) {
			throw new WrongStringSizeException("label", 1, 255);
		}

		if (!isLabelAvailable(label)) {
			throw new OptionAlreadyExistException(label);
		}
	}

	/**
	 * Checks first if the option is the default one. If so: throw a CannotDeleteDefaultOptionException
	 *
	 * @throws CannotDeleteDefaultOptionException
	 */
	public void removeOption(@NotBlank String label) {
		if (defaultValue != null && defaultValue.equals(label)) {
			throw new CannotDeleteDefaultOptionException(label);
		}
		removeOptionWithoutCheck(label);
	}

	private void removeOptionWithoutCheck(@NotBlank String label) {
		Iterator<CustomFieldOption> it = options.iterator();
		while (it.hasNext()) {
			if (label.equals(it.next().getLabel())) {
				it.remove();
				return;
			}
		}
	}

	/**
	 * Checks if the newlabel is available among all options. <br>
	 * If so, will change the defaultValue if needed, remove the option and add a new one at the vacant position. Else
	 * throws OptionAlreadyExistException.
	 *
	 * @throws OptionAlreadyExistException
	 */
	public void changeOptionLabel(String previousLabel, String newlabel) {
		checkLabelAvailable(newlabel);
		int index = findIndexOfLabel(previousLabel);
		String code = findCodeOf(previousLabel);
		if (defaultValue.equals(previousLabel)) {
			defaultValue = newlabel;
		}
		removeOption(previousLabel);
		addOption(newlabel, code, index);

	}

	/**
	 * Checks if the newCode is available among all options. <br>
	 * If so, will remove the option and add a new one at the vacant position. Else throws CodeAlreadyExistException.
	 *
	 * @param optionLabel : the label to identify the concerned option.
	 * @param newCode     : the new code for the concerned option.
	 */
	public void changeOptionCode(String optionLabel, String newCode) {
		checkCodeAvailable(newCode);
		// TODO fix [Task 1682] and remove this line
		checkCodeMatchesPattern(newCode);
		int index = findIndexOfLabel(optionLabel);
		//We can remove the option without checking if it is the default value because an option
		//with the same label will be created right after that.
		removeOptionWithoutCheck(optionLabel);
		addOption(optionLabel, newCode, index);
	}

	private String findCodeOf(String previousLabel) {

		for (CustomFieldOption option : options) {
			if (previousLabel.equals(option.getLabel())) {
				return option.getCode();
			}
		}
		return null;
	}

	private boolean isLabelAvailable(String newlabel) {
		return findIndexOfLabel(newlabel) == -1;
	}

	private boolean isCodeAvailable(String newCode) {
		return findIndexOfCode(newCode) == -1;
	}

	private int findIndexOfCode(String newCode) {

		for (CustomFieldOption option : options) {
			if (newCode.equals(option.getCode())) {
				return options.indexOf(option);
			}
		}
		return -1;
	}

	private void addOption(String newlabel, String code, int index) {
		options.add(index, new CustomFieldOption(newlabel, code));
	}

	private int findIndexOfLabel(String previousLabel) {

		for (CustomFieldOption option : options) {
			if (previousLabel.equals(option.getLabel())) {
				return options.indexOf(option);
			}
		}
		return -1;
	}

	public List<CustomFieldOption> getOptions() {
		return Collections.unmodifiableList(options);
	}

	/**
	 * Will remove all options and recreate them at their right-full positions.
	 *
	 * @param newIndex      : the lowest index for the moved selection
	 * @param optionsLabels : the labels of the moved options
	 */
	public void moveOptions(int newIndex, List<String> optionsLabels) {
		List<CustomFieldOption> newOptions = copyOptionList(optionsLabels);
		removeOptions(optionsLabels);
		options.addAll(newIndex, newOptions);
	}

	private List<CustomFieldOption> copyOptionList(List<String> optionsLabels) {
		List<CustomFieldOption> newOptions = new ArrayList<>(optionsLabels.size());
		for (String optionLabel : optionsLabels) {
			String code = findCodeOf(optionLabel);
			newOptions.add(new CustomFieldOption(optionLabel, code));
		}
		return newOptions;
	}

	private void removeOptions(List<String> optionsLabels) {
		for (String option : optionsLabels) {
			removeOption(option);
		}
	}

	@Override
	public void accept(CustomFieldVisitor visitor) {
		visitor.visit(this);
	}

}
