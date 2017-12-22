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
package org.squashtest.tm.web.internal.model.datatable

import net.sf.cglib.reflect.FastClassEmitter.GetIndexCallback;

import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class DataTableModelBuilderTest extends Specification {

	def requirement() {
		Requirement req = Mock()
		req.name >> "foo"
		req.id >> 15

		Project project = Mock()
		project.getName() >> "bar"

		req.getProject() >> project
		
		return req
	}

	def pagedCollectionHolder(def paged) {
		PagedCollectionHolder holder = Mock()
		holder.pagedItems >> paged
		holder.totalNumberOfItems >> 5

		return holder
	}
	
	class TestDataTableModelBuilder extends DataTableModelBuilder<Requirement> {
					protected Object buildItemData(Requirement item) {
						Project p = item.getProject()
						
						
						[
							item.getId(),
							getCurrentIndex(),
							// p.getName() // GRF there's a funky error (NPE) when reading p.name, I think it's caused by the fact that the Project class is enhanced by CGlib 
							"bar",
							item.getName(),
							"" ] 
					}
				}


	def "should build verified requirements model from 1 row of 5 from paged collection holder"() {
		given:
		Requirement req = requirement()

		and:
		def holder = pagedCollectionHolder([req])

		when:
		def res = new TestDataTableModelBuilder().buildDataModel(holder,"echo");

		then:
		res.sEcho == "echo"
		res.iTotalDisplayRecords == 5
		res.iTotalRecords == 5
		res.aaData == [
			[
				15,
				1,
				"bar",
				"foo",
				""
			]
		]
	}

	def "should build raw model from 1 row of 5 from paged collection holder"() {
		given:
		Requirement req = requirement()

		and:
		def holder = pagedCollectionHolder([req])

		when:
		def res = new TestDataTableModelBuilder().buildRawModel(holder)

		then:
		res == [
			[
				15,
				1,
				"bar",
				"foo",
				""
			]
		]
	}

	def "should build raw model from 1 row of 5 from collection"() {
		given:
		Requirement req = requirement()


		when:
		def res = new TestDataTableModelBuilder().buildRawModel([req])

		then:
		res == [
			[
				15,
				0,
				"bar",
				"foo",
				""
			]
		]
	}

	def "should build raw model from 1 row of 5 from collection with start index "() {
		given:
		Requirement req = requirement()


		when:
		def res = new TestDataTableModelBuilder().buildRawModel([req], 20)

		then:
		res == [
			[
				15,
				20,
				"bar",
				"foo",
				""
			]
		]
	}
}
