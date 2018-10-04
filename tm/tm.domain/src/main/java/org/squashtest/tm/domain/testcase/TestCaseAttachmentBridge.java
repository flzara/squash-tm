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
package org.squashtest.tm.domain.testcase;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.hibernate.Session;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.MetadataProvidingFieldBridge;
import org.hibernate.search.bridge.spi.FieldMetadataBuilder;
import org.hibernate.search.bridge.spi.FieldType;
import org.squashtest.tm.domain.search.SessionFieldBridge;

public class TestCaseAttachmentBridge extends SessionFieldBridge implements MetadataProvidingFieldBridge {

	private static final Integer EXPECTED_LENGTH = 7;

	private String padRawValue(long rawValue) {
		return StringUtils.leftPad(Long.toString(rawValue), EXPECTED_LENGTH, '0');
	}

	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document,
			LuceneOptions luceneOptions) {

		TestCase tc = (TestCase) value;
		Long attCount = (Long) session.getNamedQuery("testCase.countAttachments")
				.setParameter("id", tc.getId())
				.setReadOnly(true)
				.uniqueResult();

		Integer result = new Integer(attCount.toString());
		if ( result == null ) {
			if ( luceneOptions.indexNullAs() != null ) {
				luceneOptions.addSortedDocValuesFieldToDocument( name, luceneOptions.indexNullAs(), document );
			}
		}
		else {
			applyToLuceneOptions( luceneOptions, name, attCount, document );
		}
	}

	protected void applyToLuceneOptions(LuceneOptions luceneOptions, String name, Number value, Document document) {
		luceneOptions.addSortedDocValuesFieldToDocument( name, value.toString(), document );
		document.add(new TextField(name,padRawValue(value.longValue()), Field.Store.YES));

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
		fieldMetadataBuilder.field(name , FieldType.STRING).sortable( true );
	}
}
