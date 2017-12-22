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
package org.squashtest.tm.service.internal.workspace;

import org.jooq.Record;
import org.squashtest.tm.domain.Identified;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static org.squashtest.tm.core.foundation.lang.NullFilterListCollector.toNullFilteredList;

public class StreamUtils {

	public static <I extends Record, K, V> List<K>  performJoinAggregate(Function<I,K> leftTupleTransformer,
																			   Function<I,V> rightTupleTransformer,
																			   Function<Map.Entry<K,List<V>>, K> injector,
																			   Collection<I> records) {

		return records.stream().collect(
			Collectors.groupingBy(
				leftTupleTransformer,
				mapping(
					rightTupleTransformer,
					toNullFilteredList()
				)))
			.entrySet().stream()
			.map(injector)
			.collect(Collectors.toList());
	}

	public static <I extends Record, K, V> List<K>  performJoinAggregate(Function<I,K> leftTupleTransformer,
																		 Function<I,V> rightTupleTransformer,
																		 BiConsumer<K,List<V>> injector,
																		 Collection<I> records) {

		Function<Map.Entry<K, List<V>>, K> function = entry -> {
			K key = entry.getKey();
			List<V> value = entry.getValue();
			injector.accept(key,value);
			return key;
		};

		return transformTupleIntoMap(leftTupleTransformer, rightTupleTransformer, records)
			.entrySet().stream()
			.map(function)
			.collect(Collectors.toList());
	}

	private static <I extends Record, K, V> Map<K, List<V>> transformTupleIntoMap(Function<I, K> leftTupleTransformer, Function<I, V> rightTupleTransformer, Collection<I> records) {
		return records.stream().collect(
			Collectors.groupingBy(
				leftTupleTransformer,
				mapping(
					rightTupleTransformer,
					toNullFilteredList()
				)));
	}

	public static <I extends Record, K extends Identified, V>  Map<Long,K>  performJoinAggregateIntoMap(Function<I,K> leftTupleTransformer,
																										Function<I,V> rightTupleTransformer,
																										BiConsumer<K,List<V>> injector,
																										Collection<I> records) {

		Function<Map.Entry<K, List<V>>, K> function = entry -> {
			K key = entry.getKey();
			List<V> value = entry.getValue();
			injector.accept(key,value);
			return key;
		};

		return records.stream().collect(
			Collectors.groupingBy(
				leftTupleTransformer,
				mapping(
					rightTupleTransformer,
					toNullFilteredList()
				)))
			.entrySet().stream()
			.map(function)
			.collect(Collectors.toMap(Identified::getId, Function.identity()));
	}
}
