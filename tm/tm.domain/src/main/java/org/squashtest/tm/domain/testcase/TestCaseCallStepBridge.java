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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.bridge.LuceneOptions;
import org.squashtest.tm.domain.search.SessionFieldBridge;

public class TestCaseCallStepBridge extends SessionFieldBridge{

	private static final Integer EXPECTED_LENGTH = 7;
	
	private String padRawValue(Long rawValue){
		return StringUtils.leftPad(Long.toString(rawValue), EXPECTED_LENGTH, '0');
	}
	
	private Long findNumberOfCalledTestCases(Session session, Long id){
		return (Long) session
				.createCriteria(TestCase.class)
				.add(Restrictions.eq("id", id)).createCriteria("steps")
				.createCriteria("calledTestCase")
				.setProjection(Projections.rowCount()).uniqueResult(); 
	}

	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document, LuceneOptions luceneOptions) {		
	
		TestCase testcase = (TestCase) value;

		Long numberOfCalledTestCases = findNumberOfCalledTestCases(session, testcase.getId());

		Field field = new Field(name, padRawValue(numberOfCalledTestCases),
				luceneOptions.getStore(), luceneOptions.getIndex(),
				luceneOptions.getTermVector());
		field.setBoost(luceneOptions.getBoost());
		
		document.add(field);
	}

}
