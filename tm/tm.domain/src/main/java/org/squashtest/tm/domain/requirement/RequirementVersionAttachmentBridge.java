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
package org.squashtest.tm.domain.requirement;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.hibernate.Session;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.MetadataProvidingFieldBridge;
import org.hibernate.search.bridge.spi.FieldMetadataBuilder;
import org.hibernate.search.bridge.spi.FieldType;
import org.squashtest.tm.domain.search.SessionFieldBridge;

public class RequirementVersionAttachmentBridge  extends  SessionFieldBridge implements MetadataProvidingFieldBridge {

	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document, LuceneOptions luceneOptions){

		RequirementVersion reqVer =  (RequirementVersion) value;

		Long count = (Long) session.getNamedQuery("requirementVersion.countAttachments")
				.setReadOnly(true)
				.setParameter("id", reqVer.getId())
				.uniqueResult();

		Integer result = new Integer(count.toString());
		if ( result == null ) {
			if ( luceneOptions.indexNullAs() != null ) {
				luceneOptions.addFieldToDocument( name, luceneOptions.indexNullAs(), document );
			}
		}
		else {
			applyToLuceneOptions( luceneOptions, name, count, document );
		}
	}

	protected void applyToLuceneOptions(LuceneOptions luceneOptions, String name, Number value, Document document) {
		luceneOptions.addNumericFieldToDocument( name, value, document );
		document.add(new NumericDocValuesField(name,  new Long(value.longValue())));
	}

	public Object get(final String name, final Document document) {
		final IndexableField field = document.getField( name );
		if ( field != null ) {
			return field.numericValue();
		}
		else {
			return null;
		}
	}

	@Override
	public void configureFieldMetadata(String name, FieldMetadataBuilder fieldMetadataBuilder) {
		fieldMetadataBuilder.field(name , FieldType.LONG).sortable( true );
	}
}
