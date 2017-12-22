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
package org.squashtest.tm.bugtracker.advanceddomain;

public class Field {

	private String id;
	
	private String label;
	
	private FieldValue[] possibleValues = new FieldValue[0];
	
	private Rendering rendering;

	public Field(){
		super();
	}
	
	public Field(String id, String label){
		this.id = id;
		this.label = label;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public FieldValue[] getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(FieldValue[] possibleValues) {
		this.possibleValues = possibleValues;
	}

	public Rendering getRendering() {
		return rendering;
	}

	public void setRendering(Rendering rendering) {
		this.rendering = rendering;
	}
	
}
