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
package org.squashtest.tm.internal.test;

import org.jooq.*;
import org.jooq.exception.DataTypeException;
import org.jooq.exception.MappingException;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StubJooqRecord implements Record {

	private Map<String, Object> fields = new HashMap<>();

	public StubJooqRecord(Map<String, Object> fields) {
		this.fields = fields;
	}

	@Override
	public Row fieldsRow() {
		return null;
	}

	@Override
	public <T> Field<T> field(Field<T> field) {
		return null;
	}

	@Override
	public Field<?> field(String name) {
		return null;
	}

	@Override
	public Field<?> field(Name name) {
		return null;
	}

	@Override
	public Field<?> field(int index) {
		return null;
	}

	@Override
	public Field<?>[] fields() {
		return new Field[0];
	}

	@Override
	public Field<?>[] fields(Field<?>... fields) {
		return new Field[0];
	}

	@Override
	public Field<?>[] fields(String... fieldNames) {
		return new Field[0];
	}

	@Override
	public Field<?>[] fields(Name... fieldNames) {
		return new Field[0];
	}

	@Override
	public Field<?>[] fields(int... fieldIndexes) {
		return new Field[0];
	}

	@Override
	public Row valuesRow() {
		return null;
	}

	@Override
	public <T> T get(Field<T> field) throws IllegalArgumentException {
		return null;
	}

	@Override
	public <T> T get(Field<?> field, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <T, U> U get(Field<T> field, Converter<? super T, ? extends U> converter) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public Object get(String fieldName) throws IllegalArgumentException {
		return fields.get(fieldName);
	}

	@Override
	public <T> T get(String fieldName, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <U> U get(String fieldName, Converter<?, ? extends U> converter) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public Object get(Name fieldName) throws IllegalArgumentException {
		return null;
	}

	@Override
	public <T> T get(Name fieldName, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <U> U get(Name fieldName, Converter<?, ? extends U> converter) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public Object get(int index) throws IllegalArgumentException {
		return null;
	}

	@Override
	public <T> T get(int index, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <U> U get(int index, Converter<?, ? extends U> converter) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <T> void set(Field<T> field, T value) {

	}

	@Override
	public <T, U> void set(Field<T> field, U value, Converter<? extends T, ? super U> converter) {

	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Record original() {
		return null;
	}

	@Override
	public <T> T original(Field<T> field) {
		return null;
	}

	@Override
	public Object original(int fieldIndex) {
		return null;
	}

	@Override
	public Object original(String fieldName) {
		return null;
	}

	@Override
	public Object original(Name fieldName) {
		return null;
	}

	@Override
	public boolean changed() {
		return false;
	}

	@Override
	public boolean changed(Field<?> field) {
		return false;
	}

	@Override
	public boolean changed(int fieldIndex) {
		return false;
	}

	@Override
	public boolean changed(String fieldName) {
		return false;
	}

	@Override
	public boolean changed(Name fieldName) {
		return false;
	}

	@Override
	public void changed(boolean changed) {

	}

	@Override
	public void changed(Field<?> field, boolean changed) {

	}

	@Override
	public void changed(int fieldIndex, boolean changed) {

	}

	@Override
	public void changed(String fieldName, boolean changed) {

	}

	@Override
	public void changed(Name fieldName, boolean changed) {

	}

	@Override
	public void reset() {

	}

	@Override
	public void reset(Field<?> field) {

	}

	@Override
	public void reset(int fieldIndex) {

	}

	@Override
	public void reset(String fieldName) {

	}

	@Override
	public void reset(Name fieldName) {

	}

	@Override
	public Object[] intoArray() {
		return new Object[0];
	}

	@Override
	public List<Object> intoList() {
		return null;
	}

	@Override
	public Map<String, Object> intoMap() {
		return null;
	}

	@Override
	public Record into(Field<?>... fields) {
		return null;
	}

	@Override
	public <T1> Record1<T1> into(Field<T1> field1) {
		return null;
	}

	@Override
	public <T1, T2> Record2<T1, T2> into(Field<T1> field1, Field<T2> field2) {
		return null;
	}

	@Override
	public <T1, T2, T3> Record3<T1, T2, T3> into(Field<T1> field1, Field<T2> field2, Field<T3> field3) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4> Record4<T1, T2, T3, T4> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5> Record5<T1, T2, T3, T4, T5> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6> Record6<T1, T2, T3, T4, T5, T6> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7> Record7<T1, T2, T3, T4, T5, T6, T7> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8> Record8<T1, T2, T3, T4, T5, T6, T7, T8> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9> Record9<T1, T2, T3, T4, T5, T6, T7, T8, T9> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Record10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Record11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Record12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Record13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Record14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13, Field<T14> field14) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Record15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Record16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Record17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16, Field<T17> field17) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Record18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16, Field<T17> field17, Field<T18> field18) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Record19<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16, Field<T17> field17, Field<T18> field18, Field<T19> field19) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> Record20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16, Field<T17> field17, Field<T18> field18, Field<T19> field19, Field<T20> field20) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21> Record21<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16, Field<T17> field17, Field<T18> field18, Field<T19> field19, Field<T20> field20, Field<T21> field21) {
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22> Record22<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22> into(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11, Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16, Field<T17> field17, Field<T18> field18, Field<T19> field19, Field<T20> field20, Field<T21> field21, Field<T22> field22) {
		return null;
	}

	@Override
	public <E> E into(Class<? extends E> type) throws MappingException {
		return null;
	}

	@Override
	public <E> E into(E object) throws MappingException {
		return null;
	}

	@Override
	public <R extends Record> R into(Table<R> table) {
		return null;
	}

	@Override
	public ResultSet intoResultSet() {
		return null;
	}

	@Override
	public <E> E map(RecordMapper<Record, E> mapper) {
		return null;
	}

	@Override
	public void from(Object source) throws MappingException {
		this.fields = (Map<String, Object>) source;
	}

	@Override
	public void from(Object source, Field<?>... fields) throws MappingException {

	}

	@Override
	public void from(Object source, String... fieldNames) throws MappingException {

	}

	@Override
	public void from(Object source, Name... fieldNames) throws MappingException {

	}

	@Override
	public void from(Object source, int... fieldIndexes) throws MappingException {

	}

	@Override
	public void fromMap(Map<String, ?> map) {

	}

	@Override
	public void fromMap(Map<String, ?> map, Field<?>... fields) {

	}

	@Override
	public void fromMap(Map<String, ?> map, String... fieldNames) {

	}

	@Override
	public void fromMap(Map<String, ?> map, Name... fieldNames) {

	}

	@Override
	public void fromMap(Map<String, ?> map, int... fieldIndexes) {

	}

	@Override
	public void fromArray(Object... array) {

	}

	@Override
	public void fromArray(Object[] array, Field<?>... fields) {

	}

	@Override
	public void fromArray(Object[] array, String... fieldNames) {

	}

	@Override
	public void fromArray(Object[] array, Name... fieldNames) {

	}

	@Override
	public void fromArray(Object[] array, int... fieldIndexes) {

	}

	@Override
	public int compareTo(Record record) {
		return 0;
	}

	@Override
	public <T> T getValue(Field<T> field) throws IllegalArgumentException {
		return null;
	}

	@Override
	public <T> T getValue(Field<T> field, T defaultValue) throws IllegalArgumentException {
		return null;
	}

	@Override
	public <T> T getValue(Field<?> field, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <T> T getValue(Field<?> field, Class<? extends T> type, T defaultValue) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <T, U> U getValue(Field<T> field, Converter<? super T, U> converter) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <T, U> U getValue(Field<T> field, Converter<? super T, U> converter, U defaultValue) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public Object getValue(String fieldName) throws IllegalArgumentException {
		return null;
	}

	@Override
	public Object getValue(String fieldName, Object defaultValue) throws IllegalArgumentException {
		return null;
	}

	@Override
	public <T> T getValue(String fieldName, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <T> T getValue(String fieldName, Class<? extends T> type, T defaultValue) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <U> U getValue(String fieldName, Converter<?, U> converter) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <U> U getValue(String fieldName, Converter<?, U> converter, U defaultValue) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public Object getValue(Name fieldName) throws IllegalArgumentException {
		return null;
	}

	@Override
	public <T> T getValue(Name fieldName, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <U> U getValue(Name fieldName, Converter<?, U> converter) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public Object getValue(int index) throws IllegalArgumentException {
		return null;
	}

	@Override
	public Object getValue(int index, Object defaultValue) throws IllegalArgumentException {
		return null;
	}

	@Override
	public <T> T getValue(int index, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <T> T getValue(int index, Class<? extends T> type, T defaultValue) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <U> U getValue(int index, Converter<?, U> converter) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <U> U getValue(int index, Converter<?, U> converter, U defaultValue) throws IllegalArgumentException, DataTypeException {
		return null;
	}

	@Override
	public <T> void setValue(Field<T> field, T value) {

	}

	@Override
	public <T, U> void setValue(Field<T> field, U value, Converter<T, ? super U> converter) {

	}

	@Override
	public void attach(Configuration configuration) {

	}

	@Override
	public void detach() {

	}
}
