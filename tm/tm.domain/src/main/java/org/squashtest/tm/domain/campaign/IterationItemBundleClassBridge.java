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
package org.squashtest.tm.domain.campaign;

import org.apache.lucene.document.*;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.MetadataProvidingFieldBridge;
import org.hibernate.search.bridge.spi.FieldMetadataBuilder;
import org.hibernate.search.bridge.spi.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.testcase.TestCase;



/**
 * <p>This "bridge" actually serve several fields, most notably those of the referenced test case. Indeed when @IndexEmbedding
 * an entity its classbridges are executed even when we don't want to. Here we only embedded what we need from them.</p>
 *
 * <p>Incidentally, the parameter 'name' in 'writeFieldToDocument' is not used since the many fields written here are defined
 * internally (see the public static Strings)</p>
 *
 * <p>I wouldn't be surprised if more fields joined the party, due to how expensive to index some other fields might be.</p>
 *
 */
// TODO : as it stands the test case will be loaded anyway. I'm not sure whether checking if the test case is loaded or not
// would be efficient considering how expensive that operation is (ie, getting a handle on the right session - unlike what is done in SessionFieldBridge -
// invoke Persistence.getPersistenceUtil() etc)
// So I keep the code simple and let the test case load. It's still faster than unrolling the natural way including testcase classbridges
public class IterationItemBundleClassBridge implements FieldBridge, MetadataProvidingFieldBridge {

	public static final String FIELD_TC_ID 			= "referencedTestCase.id";
	public static final String FIELD_TC_NAME 			= "referencedTestCase.name";
	public static final String FIELD_TC_REFERENCE 		= "referencedTestCase.reference";
	public static final String FIELD_TC_IMPORTANCE		= "referencedTestCase.importance";
	private static final Logger LOGGER = LoggerFactory.getLogger(IterationItemBundleClassBridge.class);

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

		IterationTestPlanItem item = (IterationTestPlanItem) value;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("preparing indexation bundle for iteration test plan item "+item.getId());
		}

		indexTestCase(item, document,luceneOptions);

	}

	// ************************** test case indexation ***************************************

	private void indexTestCase(IterationTestPlanItem item, Document document, LuceneOptions luceneOptions){

		LOGGER.debug("indexing referencedTestCase :");

		// abort if no test case to index
		if (item.isTestCaseDeleted()){
			return;
		}

		TestCase tc = item.getReferencedTestCase();
		String importance = tc.getImportance().getLevel()+"-"+tc.getImportance().toString();
			// note : not indexing testcase id as a LongField because result is weird

		applyToLuceneStringOptions(luceneOptions, FIELD_TC_NAME, tc.getName().toLowerCase(),  document);
		applyToLuceneStringOptions(luceneOptions, FIELD_TC_REFERENCE, tc.getReference().toLowerCase(),  document);
		applyToLuceneStringOptions(luceneOptions, FIELD_TC_IMPORTANCE, importance, document);

		Integer result = new Integer(tc.getId().toString());
		if ( result == null ) {
			if ( luceneOptions.indexNullAs() != null ) {
				luceneOptions.addFieldToDocument( FIELD_TC_ID, luceneOptions.indexNullAs(), document );
			}
		}
		else {
			applyToLuceneStringOptions( luceneOptions, FIELD_TC_ID, result.toString(), document );
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
		fieldMetadataBuilder.field(FIELD_TC_NAME,FieldType.STRING).sortable(true);
		fieldMetadataBuilder.field(FIELD_TC_REFERENCE,FieldType.STRING).sortable(true);
		fieldMetadataBuilder.field(FIELD_TC_IMPORTANCE,FieldType.STRING).sortable(true);
		fieldMetadataBuilder.field(FIELD_TC_ID,FieldType.STRING).sortable(true);
		fieldMetadataBuilder.field("datasets",FieldType.STRING).sortable(true);
		fieldMetadataBuilder.field("label",FieldType.STRING

		).sortable(true);

	}

}
