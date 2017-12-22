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

import java.util.ArrayList;
import java.util.List;

public class SearchInputPanelModel {

	private List<SearchInputFieldModel> fields = new ArrayList<>();
	private String title;
	private boolean open;
	private String id;
	private String location;
	private List<String> cssClasses = new ArrayList<>();
	
	public SearchInputPanelModel(){
		
	}
	
	public SearchInputPanelModel(String title){
		this.title = title;
	}
	
	public SearchInputPanelModel(String title, List<SearchInputFieldModel> fields){
		this(title);
		this.fields = fields;
	}

	public void addField(SearchInputFieldModel field){
		this.fields.add(field);
	}
	
	public List<SearchInputFieldModel> getFields() {
		return fields;
	}

	public void setFields(List<SearchInputFieldModel> fields) {
		this.fields = fields;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<String> getCssClasses() {
		return cssClasses;
	}

	public void setCssClasses(List<String> cssClasses) {
		this.cssClasses = cssClasses;
	}
	
	public void addCssClass(String cssClass){
		this.cssClasses.add(cssClass);
	}
}
