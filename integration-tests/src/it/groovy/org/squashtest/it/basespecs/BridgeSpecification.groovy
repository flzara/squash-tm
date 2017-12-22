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
package org.squashtest.it.basespecs

import javax.persistence.EntityManager

import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.Field.Index
import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.Field.TermVector
import org.hibernate.Session
import org.hibernate.search.bridge.LuceneOptions

/**
 * Superclass for a hibernate search bridge specification.
 * 
 * 
 * @author Gregory Fouquet
 *
 */
abstract class BridgeSpecification extends DbunitDaoSpecification {

	LuceneOptions lucene = Mock()
	Document doc = new Document()

	Session getSession() {
		em.unwrap(Session.class)
	}	
	
	def setup() {
		lucene.getStore() >> Field.Store.YES
		lucene.getIndex() >> Field.Index.ANALYZED
		lucene.getTermVector() >> Field.TermVector.NO

	}
	
}
