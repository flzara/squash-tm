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
package org.squashtest.tm.domain.chart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/*
 * TECH INFORMATIONS
 *
 * <p>
 * 	A result set for a {@link ChartInstance}, which contains the axes and one or several series. A few words on its structure :
 *
 * </p>
 *
 *   <b>the abscissa</b>
 *
 *   <p>The abscissa are common to all series, and is given as a list of Object[]. An Object[] contains the values for the {@link AxisColumn}
 *   defined in a {@link ChartDefinition}, in the same order. </p>
 *
 * 	<b>the series</b>
 *
 * 	<p> Each serie represent the values for one of the {@link MeasureColumn} in the {@link ChartDefinition}. Each value in these list match by index
 *	 a value in the axes list.</p>
 *
 *	<p>Note that this structure requires perfect alignment of the series and abscissa, which is enough for now. In the future it could be a problem, for instance
 *	if a serie is not defined for all values of the abscissa - which would lead to abscissa and serie of different lenght, thus making the match-by-index less convenient.</p>
 *
 * @author bsiri
 *
 */
public class ChartSeries {

	private List<Object[]> abscissa;

	// I insist, this must a LinkedHashMap (or any Map which iterator returns elements in the order they were inserted)
	private LinkedHashMap<String, List<Object>> series = new LinkedHashMap<>();


	public void setAbscissa(List<Object[]> abscissa) {
		this.abscissa = abscissa;
	}

	public void addSerie(String name, List<Object> serie) {
		series.put(name, serie);
	}

	/**
	 * returns the merge (aka zip, interleave etc) operation between the abscissa and the required serie
	 *
	 * @param name
	 * @return
	 */
	public List<Object[]> getSerie(String name) {
		return makeSerie(series.get(name));
	}

	public List<Object[]> getSerie(int serieIndex) {

		List<Object> serie = null;
		int i = 0;
		for (List<Object> s : series.values()) {
			if (i == serieIndex) {
				serie = s;
				break;
			}
			i++;
		}
		if (serie != null) {
			return makeSerie(serie);
		} else {
			throw new IllegalArgumentException("no serie at index '" + serieIndex + "'");
		}

	}

	private List<Object[]> makeSerie(List<Object> serie) {

		List<Object[]> result = new ArrayList<>(abscissa.size());

		Iterator<Object[]> absIter = abscissa.iterator();
		Iterator<Object> serIter = serie.iterator();

		while (absIter.hasNext()) {
			Object[] abs = absIter.next();
			Object ser = serIter.next();
			Object[] merge = new Object[abs.length + 1];

			System.arraycopy(abs, 0, merge, 0, abs.length);
			merge[abs.length] = ser;
			result.add(merge);
		}

		return result;
	}


	public Map<String, List<Object>> getSeries() {
		return series;
	}

	public void setSeries(LinkedHashMap<String, List<Object>> series) {
		this.series = series;
	}

	public List<Object[]> getAbscissa() {
		return abscissa;
	}


}
