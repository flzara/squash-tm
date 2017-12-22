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
package org.squashtest.tm.plugin.testautomation.jenkins.beans;

import java.util.Arrays;

public class ItemList {
	
	private Item[] items;
	
	
	
	public Item[] getItems() {
		return items;
	}

	public void setItems(Item[] items) {	//NOSONAR that array is definitely not stored directly
		this.items = Arrays.copyOf(items, items.length);
	}

	
	public Item findQueuedBuildByExtId(String projectName, String extId){
		
		Item result = null;
		
		// defensive check, probably useless if the json deserializer does the job correctly
		if (items != null){
			for (Item item : items){
				
				if (item == null) continue;
				
				if (item.representsProjectWithExtId(projectName, extId)){
					result = item;
					break;
				}
			}
		}
		
		return result;
	}
	
	public Item findQueuedBuildById(String projectName, int id){
		
		if (items == null) return null;
		
		for (Item item : items){
			
			if (item == null) continue;
			
			if (item.representsProjectWithId(projectName, id)){
				return item;
			}
		}
		return null;
	}
}
