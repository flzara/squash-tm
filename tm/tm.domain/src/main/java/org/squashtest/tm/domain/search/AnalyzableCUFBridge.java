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
package org.squashtest.tm.domain.search;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.squashtest.tm.domain.customfield.InputType;

/**
 * "You see, in this world there's two kinds of cuf bridges, my friend: those that analyze and those that don't. You do."
 */
public class AnalyzableCUFBridge extends AbstractCUFBridge {

	/**
	 * The analyze variant of AbstractCUFBridge accept any custom fields bare the dropdown list.
	 *
	 * @param criteria
	 */
	@Override
	protected void filterOnCufType(Criteria criteria) {
		criteria.add(Restrictions.not(Restrictions.eq("cuf.inputType", InputType.DROPDOWN_LIST)));
	}
}
