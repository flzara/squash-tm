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

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.bugtracker.definition.RemoteCategory;
import org.squashtest.tm.bugtracker.definition.RemotePriority;
import org.squashtest.tm.bugtracker.definition.RemoteStatus;
import org.squashtest.tm.bugtracker.definition.RemoteUser;
import org.squashtest.tm.bugtracker.definition.RemoteVersion;


/**
 * A FieldValue represents, well, a value. This class is kind of stuff-what-you-can-in-there because the following may happen :
 *
 * <ul>
 * 	<li>the value may be a simple scalar (eg, a string),</li>
 * 	<li>the value may be an collection of scalar (eg, a collection of string)</li>
 * 	<li>the value may be identified (eg, a version)</li>
 * 	<li>and well, the value may be collection of identified or unidentified scalar of values</li>
 * 	<li>...</li>
 * </ul>
 *
 * <p>
 * 	you get the idea. This class flattens the fact that the content can have or not have an idea, and can be a simple type of aggregated type.
 * It is so because json serializers will handle it more easily, since the mechanics doesn't rely on the class of the data (there is only one class) but solely on its content.
 * </p>
 *
 * <p>
 * 	fields are :
 * <ul>
 * 	<li>id : if the fieldvalue is identified by something, let it be the id</li>
 * 	<li>typename : a metadata which states what type of data it is. Content is free, can be used by widget extensions deployed in Squash, or otherwise may help to convert a FieldValue to a desired specific domain entity</li>
 * 	<li>scalar : if the fieldvalue is a simpletype, let scalar be its value</li>
 * 	<li>composite : if there are multiple value for a value, let composite be this value</li>
 *      <li>custom : optional. For anything that doesn't fit in the above. 
 *          It exists merely for non-basic widgets deployed via a widget extension and which would need 
 *          to convey some extra data. Most of the case you can just ignore it.
 *      </li>
 * </ul>
 *
 * </p>
 *
 * @author bsiri
 *
 */
public class FieldValue implements RemotePriority, RemoteVersion, RemoteCategory, RemoteUser, RemoteStatus{

	private String id;
	private String typename;
	private String scalar;
	private FieldValue[] composite = new FieldValue[0];
	private Object custom;


	public FieldValue(){
		super();
	}

	public FieldValue(String id, String scalar){
		super();
		this.id=id;
		this.scalar = scalar;
	}

	public FieldValue(String id, String typeName, String scalar){
		super();
		this.id = id;
		this.scalar = scalar;
		this.typename = typeName;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getScalar() {
		return scalar;
	}

	public void setScalar(String scalar) {
		this.scalar = scalar;
	}

	public FieldValue[] getComposite() {
		return composite;
	}

	public void setComposite(FieldValue[] composite) {
		this.composite = composite;
	}


	public String getTypename() {
		return typename;
	}

	public void setTypename(String typename) {
		this.typename = typename;
	}


	private String doGetName(){
		if (scalar!=null){
			return scalar+", ";
		}
		else{
			StringBuilder builder = new StringBuilder();
			for (FieldValue aComposite : composite) {
				builder.append(aComposite.doGetName()).append(", ");
			}
			return builder.toString();
		}
	}

	@Override
	public String getName() {
		return doGetName().replaceFirst(",\\s*$", "");
	}

	public void setName(String name){
		//nothing. This exists just because Jackson would complain otherwise
	}

	public Object getCustom() {
		return custom;
	}

	public void setCustom(Object custom) {
		this.custom = custom;
	}

	public boolean hasScalarValue(){
		return ! StringUtils.isBlank(scalar);
	}

	public boolean hasCompositeValue(){
		return composite.length != 0;
	}

}
