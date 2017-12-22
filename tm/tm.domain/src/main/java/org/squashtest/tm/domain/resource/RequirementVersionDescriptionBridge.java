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
	/**
	 *     This file is part of the Squashtest platform.
	 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.domain.resource;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

public class RequirementVersionDescriptionBridge implements FieldBridge{

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

		String description = (String) value;
		Integer val = 1;
		if(description == null || description.trim().isEmpty()){
			val = 0;
		}

		Field field = new Field(name, String.valueOf(val), luceneOptions.getStore(),
				   luceneOptions.getIndex(), luceneOptions.getTermVector() );
				   field.setBoost( luceneOptions.getBoost());

		document.add(field);
	}
}
