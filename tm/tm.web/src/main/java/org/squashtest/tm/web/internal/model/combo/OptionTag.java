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
package org.squashtest.tm.web.internal.model.combo;

/***
 * Used to compose select option tag with the association label / value 
 * especially for internationalization devices
 * 
 * @author xpetitrenaud
 * 
 */

public class OptionTag {
	/***
	 * select option label
	 */
	private String label;
	
	/***
	 * select option value
	 */
	private String value;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	/***
	 * Overloaded constructor 
	 * @param givenLabel the label 
	 * @param givenValue the value
	 */
	public OptionTag(String givenLabel, String givenValue )
	{
		this.label = givenLabel;
		this.value = givenValue;
	}
	
	public OptionTag(){
		
	}
}
