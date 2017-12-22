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

import java.util.HashMap;
import java.util.Map;


/**
 * <p>An input type defines basically what widget should be used when rendered in a UI. Squash proposes several native widgets, but is open to extension
 * (ask on the forum on that topic if you're interested). An InputType maps the original name of a widget proposed on the remote bugtracker to the name
 * of a native Squash Widget. It is the role of the bugtracker connector to define such mapping and provide InputType for each fields that needs rendering on
 * the Squash UI.</p>
 *
 * <p>A widget represents a {@link Field} and produces a {@link FieldValue}. The original field name must contain only printable characters,
 * digits, dots, underscore and dash, any non supported character will be replaced by '_'.
 * See the static fields for the list of known fields.</p>
 *
 *  <p>special fields :
 *  	<ul>
*           <li>If a remote widget cannot be coerced to a Squash widget, it must use {@link #UNKNOWN} as name, and specify the original name anyway.</li>
*           <li>One or several widgets may set the flag {@link #isFieldSchemeSelector()}. If set, when their value change a new field scheme
* 		will be selected using {@link AdvancedProject#getFieldScheme(String)}, using "&lt;id&gt;>:&lt;scalar&gt;" as argument, where id is the id of the field
*               and scalar the value of the field value.
*               They also behave like a normal field.
*           </li>
 *  	</ul>
 *  </p>
 *
 *
 * <p>
 *   an InputType also accepts metadata that will be transmitted to the Squash UI, as a map. These metadata should 
 * be stuffed in the attribute {@link #configuration}. Supported metadata are :
 *
 *   <ul>
 *   	<li>
 *          {@link #TIME_FORMAT} : since 1.5.1. Used to format the time in a DATE_TIME input.
 *   	</li>
 *   	<li>{@link #FORMAT} : since 1.8.0. A format string that the widget can use to format its input or output. Widgets using this option are :
 *   		<ul>
 *   			<li>{@link #DATE_PICKER} (use the standard java date format)</li>
 *   			<li>{@link #DATE_TIME} (use the standard java date format)</li>
 *   		</ul>
 *   	<li>{@link #ONCHANGE} : since 1.5.1. 
 *      If set, when the widget on the Squash UI changes its value, it will emit a {@link DelegateCommand} to the bugtracker connector. Not all widgets
 *      supports this, as of 1.5.1 and until further notice only text_field can do so.
 * 
 *   	Native squash widgets will emit a DelegateCommand, using the value you supplied for 'onchange' as command name and its {@link FieldValue#getName()} as argument. 
 *      Customized widgets shipped with an extension can of course specify something else, it will be up to your connector to know how to interpret them.
 *   	This mechanism is used for instance by the text_fields for autocompletion.
 *   	</li>
 *   	<li>
 *   		{@link #MAX_LENGTH} : if set (to a positive numeric value), will cap the size of the input to that specified value. For now only plaijn TEXT_FIELD supports it.
 *   	</li>
 *   </ul>
 *
 * </p>
 *
 * @author bsiri
 *
 */
public class InputType {

	public static final String UNKNOWN		= "unknown";

	public static final String TEXT_FIELD 		= "text_field";
	public static final String TEXT_AREA 		= "text_area";
	public static final String DATE_PICKER		= "date_picker";
	public static final String DATE_TIME		= "date_time";
	public static final String TAG_LIST		= "tag_list";
	public static final String FREE_TAG_LIST	= "free_tag_list";
	public static final String DROPDOWN_LIST	= "dropdown_list";
	public static final String CHECKBOX		= "checkbox";
	public static final String CHECKBOX_LIST	= "checkbox_list";
	public static final String RADIO_BUTTON		= "radio_button";
	public static final String FILE_UPLOAD		= "file_upload";
	public static final String TIMETRACKER      = "timetracker";
	public static final String CASCADING_SELECT = "cascading_select";
	public static final String MULTI_SELECT     = "multi_select";

	public static final String EXCLUDED_CHARACTERS = "[^\\w-_.0-9]";


	//********************* common configuration keys ******************

	public static final String FORMAT		= "format";
        public static final String TIME_FORMAT          = "time-format";
	public static final String ONCHANGE 		= "onchange";
	public static final String MAX_LENGTH		= "max-length";


	// ***** attributes ******

	private String name = UNKNOWN;

	private String original = UNKNOWN;

	private String dataType;

	private boolean fieldSchemeSelector = false;


	private Map<String, String> configuration = new HashMap<>();


	public InputType(){
		super();
	}

	public InputType(String name, String original){
		super();
		this.name = name;
		this.original = original;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * returns the original name, escaped using {@link #formatName(String)}
	 * @return
	 */
	public String getOriginal() {
		return InputType.formatName(original);
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public boolean isFieldSchemeSelector() {
		return fieldSchemeSelector;
	}

	public void setFieldSchemeSelector(boolean fieldSchemeSelector) {
		this.fieldSchemeSelector = fieldSchemeSelector;
	}

	public Map<String, String> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<String, String> conf) {
		this.configuration = conf;
	}

	public void addConfiguration(String key, String value){
		this.configuration.put(key, value);
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String inputType) {
		this.dataType = inputType;
	}


	/**
	 * Escapes illegal characters and make that string comply to the rules specified
	 * at the documentation at the class level.
	 *
	 * @param original
	 * @return
	 */
	public static String formatName(String original){
		return original.replaceAll("[^\\w-_.0-9]", "_");
	}

}
