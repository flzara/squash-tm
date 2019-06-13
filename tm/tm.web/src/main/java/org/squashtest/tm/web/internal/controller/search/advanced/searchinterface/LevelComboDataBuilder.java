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
package org.squashtest.tm.web.internal.controller.search.advanced.searchinterface;

import org.squashtest.tm.domain.Level;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

/**
 * Specialization of EnumJeditableComboDataBuilder to produce a key suitable for the search panel. Sort of a lazy
 * hack, for we should build SearchInputPossibleValueModel instead.
 * 
 * @author Gregory Fouquet
 * 
 * @param <T>
 * @see OptionListBuilder
 */
class LevelComboDataBuilder<T extends Enum<?> & Level, B extends LevelComboDataBuilder<T, B>> extends EnumJeditableComboDataBuilder<T, B> {
	
	/*
	 * TM-282: as it stands now, prefixing the enum value with the rank is undesirable nor needed. I need to ascertain first that it is indeed
	 * not needed anymore before deleting this class before it is used in quite a number of places, and in very smelly places, and I don't want to 
	 * undergo the job yet.
	 */

}