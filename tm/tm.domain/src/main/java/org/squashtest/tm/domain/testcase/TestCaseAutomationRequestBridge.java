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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.TextField;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.MetadataProvidingFieldBridge;
import org.hibernate.search.bridge.spi.FieldMetadataBuilder;
import org.hibernate.search.bridge.spi.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;

public class TestCaseAutomationRequestBridge implements FieldBridge, MetadataProvidingFieldBridge {

	public static final String FIELD_AUTOMATION_REQUEST_STATUS = "automationRequest.requestStatus";
	public static final String FIELD_AUTOMATION_REQUEST_ID = "automationRequest.id";

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseAutomationRequestBridge.class);

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		TestCase item = (TestCase) value;

		indexAutomationRequest(item, document, luceneOptions);
	}

	private void indexAutomationRequest(TestCase item, Document document, LuceneOptions luceneOptions){

		LOGGER.debug("indexing automationRequest :");

		AutomationRequest automationRequest = item.getAutomationRequest();
		//abort if no automation request for tc
		if(automationRequest == null) {
			return;
		}
		String requestStatus = automationRequest.getRequestStatus().getLevel()+"-"+automationRequest.getRequestStatus().toString();

		applyToLuceneStringOptions(luceneOptions, FIELD_AUTOMATION_REQUEST_STATUS, requestStatus, document);

		Integer result = new Integer(automationRequest.getId().toString());
		if ( result == null ) {
			if ( luceneOptions.indexNullAs() != null ) {
				luceneOptions.addFieldToDocument( FIELD_AUTOMATION_REQUEST_ID, luceneOptions.indexNullAs(), document );
			}
		}
		else {
			applyToLuceneStringOptions( luceneOptions, FIELD_AUTOMATION_REQUEST_ID, result.toString(), document );
		}
	}



	protected void applyToLuceneOptions(LuceneOptions luceneOptions, String name, Number value, Document document) {
		luceneOptions.addNumericFieldToDocument( name, value, document );
		document.add(new NumericDocValuesField(name,  new Long(value.longValue())));
	}

	protected void applyToLuceneStringOptions(LuceneOptions luceneOptions, String name, String value, Document document) {
		luceneOptions.addSortedDocValuesFieldToDocument( name, value, document );
		document.add(new TextField(name, value, Field.Store.YES));
	}

	@Override
	public void configureFieldMetadata(String name, FieldMetadataBuilder fieldMetadataBuilder) {
		fieldMetadataBuilder.field(FIELD_AUTOMATION_REQUEST_STATUS,FieldType.STRING).sortable(true);
		fieldMetadataBuilder.field(FIELD_AUTOMATION_REQUEST_ID,FieldType.STRING).sortable(true);
	}
}
