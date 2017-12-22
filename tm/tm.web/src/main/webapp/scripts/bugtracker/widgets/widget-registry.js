/*
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
/*
 * This is roughly a delegate module loader 
 */
define(["require", 
        "./widget", 
        "./date_picker", 
        "./date_time", 
        "./dropdown_list", 
        "./tag_list", 
        "./text_area",
        "./text_field",
        "./timetracker",
        "./free_tag_list",
        "./file_upload",
        "./checkbox"],
        function(require, baseWidget, date_picker, date_time, dropdown_list, tag_list, text_area, text_field, timetracker, free_tag_list, file_upload, checkbox){

	var registry = {
		
		cache : {},
		

		smartlog : function(widgetName, error){
			if (!window.console || !window.console.log){
				return;
			}

			var baseMessage = "bugtracker widget registry : error while processing widget '"+widgetName+"' : ";
			if (!!error.stack){
				console.log(baseMessage + error.stack);				
			}
			else if (!!error.message){
				console.log(baseMessage+error.message);
			}
			
			else{
				console.log(baseMessage+error);				
			}
			
		},
		
		handleErrors : function(failedWidgetName, error, handler){
			this.smartlog(failedWidgetName, error);
			this.cache[failedWidgetName]=false;	
			handler();
		},
		
		loadWidget : function(widgetName, success, fallback){

			var self = this;
			
			if (this.cache[widgetName]===undefined){
				
							
				require(["./"+widgetName], function(widg){
					try{
						self.registerWidget(widg, widgetName);
						success();
					}catch(error){
						self.handleErrors(widgetName, error, fallback);
					}
					
				}, function(error){
					self.handleErrors(widgetName, error, fallback);
				});
				
			}
			
			else if (this.cache[widgetName]){
				try{
					success();
				}catch(error){
					self.handleErrors(widgetName, error, fallback);
				}
			}
			else{
				fallback();	//supposed not to fail when invoked here, or in any case we don't need no special error managment
			}
			
		},
		
		registerWidget : function(widgetDef, widgetName){
			$.widget('squashbt.'+widgetName, $.squashbt.basewidget ,widgetDef);
			$.squashbt[widgetName].createDom = widgetDef.createDom;
			this.cache[widgetName]=true;		
		},
		
		defaultWidget : "text_field"
	};	
	

	
	$.widget('squashbt.basewidget', baseWidget);
	$.squashbt.basewidget.createDom = baseWidget.createDom;
	
	
	registry.registerWidget(text_field , "text_field");
	registry.registerWidget(date_picker , "date_picker");
	registry.registerWidget(date_time , "date_time");
	registry.registerWidget(dropdown_list , "dropdown_list");
	registry.registerWidget(tag_list , "tag_list");
	registry.registerWidget(timetracker, "timetracker");
	registry.registerWidget(free_tag_list , "free_tag_list");
	registry.registerWidget(text_area , "text_area");
	registry.registerWidget(file_upload , "file_upload");
	registry.registerWidget(checkbox , "checkbox");

	return registry;
});