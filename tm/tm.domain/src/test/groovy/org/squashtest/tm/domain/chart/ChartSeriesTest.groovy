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
package org.squashtest.tm.domain.chart

import spock.lang.Specification;

class ChartSeriesTest extends Specification {

	def "should build a plottable serie by combining abscissa and one serie"(){

		given :
		ChartSeries barbecue = new ChartSeries(
				abscissa :
				[
					["Bob", "sausages"] as Object[],
					["Bob", "beers"] as Object[],
					["Bob", "steacks"] as Object[],
					["Mike", "sausages"] as Object[],
					["Mike", "steacks"] as Object[]
				],

				series : [
					"amount" : ["some", "a lot of", "a couple of", "truckloads of", "a fair share of"]
				] as LinkedHashMap

				)

		when :
		def serie = barbecue.getSerie("amount")

		then :
		string(serie[0]) == "Bob brought some sausages"
		string(serie[1]) == "Bob brought a lot of beers"
		string(serie[2]) == "Bob brought a couple of steacks"
		string(serie[3]) == "Mike brought truckloads of sausages"
		string(serie[4]) == "Mike brought a fair share of steacks"

	}


	def string(row){
		return "${row[0]} brought ${row[2]} ${row[1]}"
	}

}
