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

import java.math.BigInteger;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.hibernate.Session;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.MetadataProvidingFieldBridge;
import org.hibernate.search.bridge.spi.FieldMetadataBuilder;
import org.hibernate.search.bridge.spi.FieldType;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQueryFactory;
import org.squashtest.tm.domain.search.SessionFieldBridge;

public class TestCaseIterationBridge extends SessionFieldBridge implements MetadataProvidingFieldBridge {

	private static final Integer EXPECTED_LENGTH = 7;
	
	private static final String COUNT_ITER = 
			"select count(distinct ITER.ITERATION_ID) from ITERATION iter " +
			"inner join ITEM_TEST_PLAN_LIST ilist on iter.ITERATION_ID = ilist.ITERATION_ID " +
			"inner join ITERATION_TEST_PLAN_ITEM item on ilist.ITEM_TEST_PLAN_ID = item.ITEM_TEST_PLAN_ID " +
			"where item.TCLN_ID = :tcId";
	
	private String padRawValue(Long rawValue){
		return StringUtils.leftPad(Long.toString(rawValue), EXPECTED_LENGTH, '0');
	}
	
	
	private Long findNumberOfIterations(Session session, Long id){
		
		BigInteger count = (BigInteger) session.createNativeQuery(COUNT_ITER)
				.setParameter("tcId", id)
				.getSingleResult();
		
		return count.longValue();
		
		/*
		//TODO 
		Issue 7977 :
		The following works correctly too but the generated SQL query is so ugly. Yuk.
		If the native query just above is cleared by the QA and no database-dependant problem
		arise (I doubt it), remove that commented code.
		
		QIteration iteration = QIteration.iteration;
		QIterationTestPlanItem item = QIterationTestPlanItem.iterationTestPlanItem;
		QTestCase testCase = QTestCase.testCase;
		
		return new ExtendedHibernateQueryFactory(session)
				.select(iteration.id).distinct()
				.from(iteration)
				.join(iteration.testPlans, item)
				.join(item.referencedTestCase, testCase)
				.where(testCase.id.eq(id))
				.fetchCount();
		*/
	}
	
	
	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document, LuceneOptions luceneOptions) {

		TestCase testcase = (TestCase) value;
		
		Long count = findNumberOfIterations(session, testcase.getId());
		Integer result = new Integer(count.toString());
		if ( result == null ) {
			if ( luceneOptions.indexNullAs() != null ) {
				luceneOptions.addSortedDocValuesFieldToDocument( name, new Long(0).toString(), document );
			}
		}
		else {
			applyToLuceneOptions( luceneOptions, name, count, document );
		}
	}


	protected void applyToLuceneOptions(LuceneOptions luceneOptions, String name, Number value, Document document) {
		luceneOptions.addSortedDocValuesFieldToDocument( name, value.toString(), document );
		document.add(new TextField(name,  padRawValue(value.longValue()), Field.Store.YES));
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
