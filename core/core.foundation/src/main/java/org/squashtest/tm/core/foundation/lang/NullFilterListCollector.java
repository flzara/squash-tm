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
package org.squashtest.tm.core.foundation.lang;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class NullFilterListCollector<T>
	implements Collector<T, ArrayList<T>, List<T>> {

	@Override
	public Supplier<ArrayList<T>> supplier() {
		return ArrayList::new;
	}

	@Override
	public BiConsumer<ArrayList<T>, T> accumulator() {
		return (list, element) -> {
			if(Objects.nonNull(element)){
				list.add(element);
			}
		};
	}

	@Override
	public BinaryOperator<ArrayList<T>> combiner() {
		return (ts, ts2) -> {
			ts.addAll(ts2);
			return ts;
		};
	}

	@Override
	public Function<ArrayList<T>, List<T>> finisher() {
		return null;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return EnumSet.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH);
	}

	public static <T> Collector<T, ?, List<T>> toNullFilteredList(){
		return new NullFilterListCollector<>();
	}
}
