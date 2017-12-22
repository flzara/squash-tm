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
package org.squashtest.tm.web.internal.model.datatable

import spock.lang.Specification

/**
  * @author Gregory Fouquet
 *
 */
class DataTableDrawParametersTest extends Specification {

	def "getiSortCol_0 should not blow up my face as per changeset 36984c995ed1"() {
		given: 
		DataTableDrawParameters params = new DataTableDrawParameters()
		params.setiSortCol_0(1)
		
		when:
		params.getiSortCol_0()
		
		then:
		notThrown ClassCastException 
	}
	def "getsSortedAttribute_0 should not blow up my face as per changeset 36984c995ed1"() {
		given: 
		DataTableDrawParameters params = new DataTableDrawParameters()
		params.setiSortCol_0(1)
		
		when:
		params.getsSortedAttribute_0()
		
		then:
		notThrown ClassCastException 
	}
}
