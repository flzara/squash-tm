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
package org.squashtest.tm.service.internal.workspace

import org.jooq.Record
import org.squashtest.tm.internal.test.StubJooqRecord
import spock.lang.Specification

class StreamUtilsTest extends Specification {

	List<Record> getRecords (){
		def line1 = ["LEFT_ID":1L, "LEFT_NAME":"name1", "LEFT_ATTR":12, "RIGHT_ID":1L, "RIGHT_NAME":"right_name_1"]
		Record record1 = new StubJooqRecord(line1)

		def line2 = ["LEFT_ID":1L, "LEFT_NAME":"name1", "LEFT_ATTR":12, "RIGHT_ID":2L, "RIGHT_NAME":"right_name_2"]
		Record record2 = new StubJooqRecord(line2)

		def line3 = ["LEFT_ID":1L, "LEFT_NAME":"name1", "LEFT_ATTR":12, "RIGHT_ID":3L, "RIGHT_NAME":"right_name_3"]
		Record record3 = new StubJooqRecord(line3)

		def line4 = ["LEFT_ID":2L, "LEFT_NAME":"name2", "LEFT_ATTR":32, "RIGHT_ID":null, "RIGHT_NAME":null]
		Record record4 = new StubJooqRecord(line4)

		def line5 = ["LEFT_ID":3L, "LEFT_NAME":"name3", "LEFT_ATTR":8, "RIGHT_ID":4L, "RIGHT_NAME":"right_name_4"]
		Record record5 = new StubJooqRecord(line5)

		[record1, record2, record3, record4, record5]
	}


	def "should transform results into map"(){

		when:
		def tupleIntoMap = StreamUtils.transformTupleIntoMap(StubFunctions.leftTupleTransformer, StubFunctions.rightTupleTransformer, getRecords());

		then:
		tupleIntoMap.size() == 3
		tupleIntoMap.find {it.key.get("LEFT_ID") == 1L}.value.collect{it.get("RIGHT_ID")}.sort() == [1L,2L,3L]
		tupleIntoMap.find {it.key.get("LEFT_ID") == 1L}.value.collect{it.get("RIGHT_NAME")}.sort() == ["right_name_1","right_name_2","right_name_3"]
		tupleIntoMap.find {it.key.get("LEFT_ID") == 1L}.key == ["LEFT_ID":1L, "LEFT_NAME":"name1", "LEFT_ATTR":12]

		tupleIntoMap.find {it.key.get("LEFT_ID") == 2L}.value == []
		tupleIntoMap.find {it.key.get("LEFT_ID") == 2L}.key == ["LEFT_ID":2L, "LEFT_NAME":"name2", "LEFT_ATTR":32]

		tupleIntoMap.find {it.key.get("LEFT_ID") == 3L}.value.collect{it.get("RIGHT_ID")}.sort() == [4L]
		tupleIntoMap.find {it.key.get("LEFT_ID") == 3L}.key == ["LEFT_ID":3L, "LEFT_NAME":"name3", "LEFT_ATTR":8]

	}

	def "should perform join aggregate"(){
		when:
		def result = StreamUtils.performJoinAggregate(StubFunctions.leftTupleTransformer, StubFunctions.rightTupleTransformer, StubFunctions.injector(), getRecords());

		then:
		result.size() == 3
		def firstLeftElement = result.sort { it.get("LEFT_ID") }.get(0)
		firstLeftElement.get("LEFT_ID") == 1
		List<Map<String,Object>> firstRightElements = firstLeftElement.get("RIGHT_ELEMENTS") as List<Map<String, Object>>;
		firstRightElements.size() == 3
		firstRightElements.collect{it.get("RIGHT_ID")}.sort() == [1L,2L,3L]
		firstRightElements.collect{it.get("RIGHT_NAME")}.sort() == ["right_name_1","right_name_2","right_name_3"]

		def secondLeftElement = result.sort { it.get("LEFT_ID") }.get(1)
		secondLeftElement.get("LEFT_ID") == 2
		List<Map<String,Object>> secondRightElements = secondLeftElement.get("RIGHT_ELEMENTS") as List<Map<String, Object>>;
		secondRightElements.size() == 0

		def thirdLeftElement = result.sort { it.get("LEFT_ID") }.get(2)
		thirdLeftElement.get("LEFT_ID") == 3
		List<Map<String,Object>> thirdRightElements = thirdLeftElement.get("RIGHT_ELEMENTS") as List<Map<String, Object>>;
		thirdRightElements.size() == 1
		thirdRightElements.collect{it.get("RIGHT_ID")}.sort() == [4L]
		thirdRightElements.collect{it.get("RIGHT_NAME")}.sort() == ["right_name_4"]
	}
}
