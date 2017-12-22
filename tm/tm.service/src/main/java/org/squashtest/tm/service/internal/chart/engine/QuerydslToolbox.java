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
package org.squashtest.tm.service.internal.chart.engine;


import static org.squashtest.tm.domain.chart.DataType.BOOLEAN;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.ColumnPrototypeInstance;
import org.squashtest.tm.domain.chart.ColumnType;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.chart.QueryStrategy;
import org.squashtest.tm.domain.chart.SpecializedEntityType;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.CustomFieldValueOption;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.jpql.ExtOps;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Ops.DateTimeOps;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateOperation;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimpleExpression;

class QuerydslToolbox {

	public static final int BY_YEAR_SUBSTRING_SIZE = 4;
	public static final int BY_MONTH_SUBSTRING_SIZE = 7;
	public static final int BY_DAY_SUBSTRING_SIZE = 10;

	private String subContext;

	private EnumMap<InternalEntityType, String> nondefaultPath = new EnumMap<>(InternalEntityType.class);

	/**
	 * Default constructor with default context
	 */
	QuerydslToolbox() {
		super();
	}

	/**
	 * Constructor with explicit context name
	 *
	 */
	QuerydslToolbox(String subContext) {
		super();
		this.subContext = subContext;
	}

	/**
	 * Constructor with context name driven by the given column
	 *
	 */
	QuerydslToolbox(ColumnPrototypeInstance column) {
		super();
		this.subContext = "subcolumn_" + column.getColumn().getId();
	}

	void setSubContext(String subContext) {
		this.subContext = subContext;
	}

	String getSubContext() {
		return subContext;
	}

	/**
	 * his method will affect the behavior of {@link #getQName(InternalEntityType)} and {@link #getQBean(InternalEntityType)} :
	 * the returned path will use the supplied alias instead of the default ones
	 *
	 */
	void forceAlias(InternalEntityType type, String alias) {
		nondefaultPath.put(type, alias);
	}

	// ************** info retrievers ***************************

	/**
	 *	The following methods ensure that the entities are aliased appropriately
	 *	according to a context.
	 *
	 */
	String getQName(InternalEntityType type) {

		EntityPathBase<?> path = type.getQBean();

		String name;

		if (nondefaultPath.containsKey(type)) {
			name = nondefaultPath.get(type);
		} else if (subContext == null) {
			name = path.getMetadata().getName();
		} else {
			name = path.getMetadata().getName() + "_" + subContext;
		}

		return name;

	}

	EntityPathBase<?> getQBean(InternalEntityType type) {
		String name = getQName(type);
		return type.getAliasedQBean(name);
	}

	EntityPathBase<?> getQBean(SpecializedEntityType domainType) {
		InternalEntityType type = InternalEntityType.fromSpecializedType(domainType);
		return getQBean(type);
	}

	EntityPathBase<?> getQBean(ColumnPrototypeInstance column) {
		InternalEntityType type = InternalEntityType.fromSpecializedType(column.getSpecializedType());
		return getQBean(type);
	}

	String getAlias(EntityPathBase<?> path) {
		return path.getMetadata().getName();
	}

	String getCustomFieldValueTableAlias(ColumnPrototype columnPrototype, Long cufId) {
		if (columnPrototype.getDataType().equals(DataType.TAG)){
			return getCustomFieldValueOptionTableAlias(columnPrototype,cufId);
		}
		return getCustomFieldValueStandardTableAlias(columnPrototype,cufId);
	}

	String getCustomFieldValueStandardTableAlias(ColumnPrototype columnPrototype, Long cufId) {
		return columnPrototype.getLabel() + "_" + cufId;
	}

	String getCustomFieldValueOptionTableAlias(ColumnPrototype columnPrototype, Long cufId) {
		return columnPrototype.getLabel() + "_value_option_" + cufId;
	}


	/**
	 * Returns the aliases registered in the "from" clause of
	 * the given query
	 *
	 */
	Set<String> getJoinedAliases(ExtendedHibernateQuery<?> query) {
		AliasCollector collector = new AliasCollector();
		for (JoinExpression join : query.getMetadata().getJoins()) {
			join.getTarget().accept(collector, collector.getAliases());
		}
		return collector.getAliases();
	}


	boolean isAggregate(Operation operation) {
		boolean res;
		switch (operation) {
			case COUNT:
			case SUM:
				res = true;
				break;
			default:
				res = false;
				break;
		}
		return res;
	}

	/**
	 * Tells whether the given filter is part of a where clause - or a having component
	 *
	 */
	/*
	 * technically a filter is a 'having' component only if :
	 *
	 * 1 - this this filter applies to a column of of type CALCULATED,
	 * 2 - that happens to have a subquery of strategy INLINED,
	 * 3 - and the measure of that subquery has an aggregate operation
	 *
	 * Indeed :
	 *
	 * - if the column is an ATTRIBUTE, per construction the data is scalar (therefore no aggregate)
	 * - if the column is a calculated of subquery, the filter will be handled from within the subquery, then
	 * 	the whole subquery will be converted to a where clause for the outerquery. see #createAsPredicate()
	 * to see how it's done.
	 * - if the column is a custom field, the said custom field is likely a scalar too (unless one day we
	 * want to count how many tags a given cuf taglist contains).
	 *
	 */
	boolean isWhereClauseComponent(Filter filter) {
		ColumnPrototypeInstance column = filter;

		while (column.getColumn().getColumnType() == ColumnType.CALCULATED &&
			subQueryStrategy(column) == QueryStrategy.INLINED
			) {
			column = column.getColumn().getSubQuery().getMeasures().get(0);
		}

		return !isAggregate(column.getOperation());
	}

	boolean isHavingClauseComponent(Filter filter) {
		return !isWhereClauseComponent(filter);
	}

	boolean isSubquery(ColumnPrototypeInstance proto) {
		return proto.getColumn().getColumnType() == ColumnType.CALCULATED;
	}

	// ***************************** high level API ***********************


	Expression<?> createAsSelect(ColumnPrototypeInstance col) {

		Expression<?> selectElement;

		ColumnPrototype proto = col.getColumn();

		switch (proto.getColumnType()) {
			case ATTRIBUTE:
				selectElement = createAttributeSelect(col);
				break;

			case CALCULATED:
				selectElement = createSubquerySelect(col);
				break;

			case CUF:
				selectElement = createCustomFieldSelect(col);
				break;

			default:
				throw new IllegalArgumentException("columns of column type '" + proto.getColumnType() + "' are not yet supported");
		}

		return selectElement;
	}


	/**
	 * Creates an Expression like 'baseExp' 'operation' 'operand1', 'operand2' ... suitable for a 'where' or 'having' clause.
	 * Note that  the caller is responsible of the usage of this expression - 'where' or 'having'.
	 *
	 */
	BooleanExpression createAsPredicate(Filter filter) {
		BooleanExpression predicate;

		ColumnPrototype proto = filter.getColumn();

		switch (proto.getColumnType()) {
			case ATTRIBUTE:
				predicate = createAttributePredicate(filter);
				break;

			case CALCULATED:
				predicate = createSubqueryPredicate(filter);
				break;

			case CUF:
				predicate = createCufPredicate(filter);
				break;

			default:
				throw new IllegalArgumentException("columns of column type '" + proto.getColumnType() + "' are not yet supported");
		}

		return predicate;
	}




	// ********************* low level API *********************


	@SuppressWarnings("rawtypes")
	PathBuilder makePath(InternalEntityType src, InternalEntityType dest, String attribute) {

		Class<?> srcClass = src.getEntityClass();
		Class<?> destClass = dest.getEntityClass();
		String srcAlias = getQName(src);

		return new PathBuilder<>(srcClass, srcAlias).get(attribute, destClass);
	}

	@SuppressWarnings("rawtypes")
	PathBuilder makePath(EntityPathBase<?> src, EntityPathBase<?> dest, String attribute) {
		Class<?> srcClass = src.getType();
		Class<?> destClass = dest.getType();
		String srcAlias = src.getMetadata().getName();

		return new PathBuilder<>(srcClass, srcAlias).get(attribute, destClass);
	}


	/**
	 * Creates an expression fit for a "select" clause,  for columns of ColumnType = ATTRIBUTE
	 *
	 */
	Expression<?> createAttributeSelect(ColumnPrototypeInstance column) {
		Expression attribute = attributePath(column);
		Operation operation = column.getOperation();

		if (operation != Operation.NONE) {
			attribute = applyOperation(operation, attribute);
		}

		return attribute;

	}


	/**
	 * Creates an expression fit for a "select" clause,  for columns of ColumnType = CALCULATED
	 *
	 */
	Expression<?> createSubquerySelect(ColumnPrototypeInstance col) {
		Expression<?> expression = null;

		switch (subQueryStrategy(col)) {

			// create a subselect statement
			// NOSONAR because this is definitely not too long
			case SUBQUERY:
				EntityPathBase<?> colBean = getQBean(col);
				SubQueryBuilder qbuilder = createSubquery(col).asSubselectQuery().joinAxesOn(colBean);
				expression = qbuilder.createQuery();
				break;

			// fetches the measure from the subquery
			// NOSONAR because this is definitely not too long
			case INLINED:
				QuerydslToolbox subtoolbox = new QuerydslToolbox(col);
				MeasureColumn submeasure = col.getColumn().getSubQuery().getMeasures().get(0);    // take that Demeter !
				expression = subtoolbox.createAsSelect(submeasure);
				break;


			case MAIN:
			default :
				throw new IllegalArgumentException(
					"Attempted to create a subquery for column '" + col.getColumn().getLabel() +
						"' from what appears to be a main query. " +
						"This is probably due to an ill-inserted entry in the database, please report this to the suppport.");
		}


		// apply operation if any
		Operation operation = col.getOperation();
		if (operation != Operation.NONE) {
			expression = applyOperation(operation, expression);
		}

		return expression;
	}

	private Expression<?> createCustomFieldSelect(ColumnPrototypeInstance col) {
		Expression<?> expression;

		ColumnPrototype columnPrototype = col.getColumn();
		DataType dataType = columnPrototype.getDataType();
		Long cufId = col.getCufId();
		String alias = getCustomFieldValueTableAlias(columnPrototype, cufId);
		Operation operation = col.getOperation();

		expression = makePathForCFV(dataType,alias);
		if (operation != Operation.NONE) {
			if(dataType == DataType.DATE_AS_STRING){
				expression = applyOperationForDateCustomFields(operation, expression);
			}
			else {
				expression = applyOperation(operation, expression);
			}
		}

		return expression;
	}



	/**
	 * Creates an expression fit for a "where" clause,  for columns of ColumnType = ATTRIBUTE
	 *
	 */
	@SuppressWarnings("unchecked")
	BooleanExpression createAttributePredicate(Filter filter) {
		DataType datatype = filter.getDataType();
		Operation operation = filter.getOperation();

		// make the expression on which the filter is applied
		Expression<?> attrExpr = attributePath(filter);

		// convert the operands
		List<Expression<?>> valExpr = makeOperands(operation, datatype, filter.getValues());
		Expression<?>[] operands = valExpr.toArray(new Expression[valExpr.size()]);


		return createPredicate(operation, attrExpr, datatype, operands);
	}

	/**
	 * Creates an expression fit for a "where" or "having" clause. It's up to the caller to
	 * know what to do with that.
	 *
	 */
	BooleanExpression createSubqueryPredicate(Filter filter) {
		BooleanExpression predicate = null;

		switch (subQueryStrategy(filter)) {

			// create "where exists (subquery)" expression
			case SUBQUERY:
				EntityPathBase<?> colBean = getQBean(filter);
				//create the subquery
				QueryBuilder qbuilder = createSubquery(filter)
					.asSubwhereQuery()
					.joinAxesOn(colBean)
					.filterMeasureOn(filter);
				Expression<?> subquery = qbuilder.createQuery();

				// now integrate the subquery
				predicate = Expressions.predicate(Ops.EXISTS, subquery);

				break;

			case INLINED:
				MeasureColumn submeasure = filter.getColumn().getSubQuery().getMeasures().get(0);    // and take that again !
				QuerydslToolbox subtoolbox = new QuerydslToolbox(filter);    // create a new toolbox configured with a proper subcontext

				//ok, it is semantically sloppy. But for now the produced element is what we need :-S
				Expression<?> subexpr = subtoolbox.createAsSelect(submeasure);

				List<Expression<?>> valExpr = makeOperands(filter.getOperation(), filter.getDataType(), filter.getValues());
				Expression<?>[] operands = valExpr.toArray(new Expression[valExpr.size()]);

				predicate = createPredicate(filter.getOperation(), subexpr, submeasure.getDataType(), operands);

				break;

			case MAIN:
			default :
				throw new IllegalArgumentException(
					"Attempted to create a subquery for column '" + filter.getColumn().getLabel() +
						"' from what appears to be a main query. " +
						"This is probably due to an ill-inserted entry in the database, please report this to the support.");
		}


		return predicate;
	}

	/**
	 * Creates an expression fit for a "where" or "having" clause. Dedicated to CUF column prototype.
	 * @param filter
	 * @return
     */
	//TODO make predicate for different data types
	BooleanExpression createCufPredicate(Filter filter) {
		ColumnPrototype columnPrototype = filter.getColumn();
		DataType dataType = columnPrototype.getDataType();
		Long cufId = filter.getCufId();
		String alias = getCustomFieldValueStandardTableAlias(columnPrototype, cufId);
		Operation operation = filter.getOperation();

		// convert the operands
		List<Expression<?>> valExpr = makeOperands(operation, dataType, filter.getValues());
		Expression<?>[] operands = valExpr.toArray(new Expression[valExpr.size()]);
		Expression<?> attrExpr;


		switch (dataType){
			case STRING:
			case BOOLEAN_AS_STRING:
			case DATE_AS_STRING:
			case LIST:
				//make a path for the cuf value
				attrExpr = makePathForValueCFV(alias);
				break;
			case NUMERIC:
				attrExpr = makePathForNumericValueCFV(alias);
				break;
			case TAG:
				alias = getCustomFieldValueOptionTableAlias(columnPrototype,cufId);
				attrExpr = makePathForTagValueCFV(alias);
				break;
			default:
				throw new IllegalArgumentException("The datatype " + dataType.name() + " is not handled by custom report engine");
		}
		return createPredicate(operation, attrExpr, dataType, operands);
	}

	Expression<?> makePathForCFV(DataType dataType, String alias) {
		switch(dataType){
			case STRING:
			case LIST:
			case BOOLEAN_AS_STRING:
			case DATE_AS_STRING:
				return makePathForValueCFV(alias);
			case NUMERIC:
				return makePathForNumericValueCFV(alias);
			case TAG:
				return makePathForTagValueCFV(alias);
			default:
				throw new IllegalArgumentException("Unknown datatype for cuf : " + dataType);
		}
	}

	Expression<?> makePathForTagValueCFV(String alias) {
		return makePath(CustomFieldValueOption.class, alias, String.class, "label");
	}

	Expression<?> makePathForNumericValueCFV(String alias) {
		return makePath(CustomFieldValue.class, alias, String.class, "numericValue");
	}


	/**
	 * Make the entity path for a standard {@link CustomFieldValue}. The returned path will be :
	 * alias.value
	 * @param alias
	 * @return
     */
	PathBuilder makePathForValueCFV(String alias) {
		return makePath(CustomFieldValue.class, alias, String.class, "value");
	}

	/*
	 * There is a special treatment when operation = IS_NULL / NOT_NULL. Indeed one cannot
	 * write  for instance 'select (attribute is not null)' : although legal in most SQL database,
	 * HQL will just not have it.
	 *
	 * So we must use a case construct instead.
	 *
	 * Also, the case construct is a custom BOOLEAN_CASE and correctly generate 'case when (predicate) then true else false',
	 * because the standard querysdl case builder would generate 'case when (predicate) then ?1 else false',
	 * and then Hibernate complains because it can't determine the type of the overall expression.
	 */
	SimpleExpression<?> applyOperation(Operation operation, Expression<?> baseExp, Expression... operands) {

		SimpleExpression result;

		// the IS_NULL / NOT_NULL case
		if (operation == Operation.NOT_NULL || operation == Operation.IS_NULL) {
			Operator ops = getOperator(operation);
			Predicate nullness = Expressions.predicate(ops, baseExp);
			result = Expressions.operation(Boolean.class, ExtOps.TRUE_IF, nullness);
		}

		// the normal case
		else {
			Operator operator = getOperator(operation);
			Expression[] expressions = prepend(baseExp, operands);
			result = Expressions.operation(operator.getType(), operator, expressions);

		}
		return result;
	}

	private SimpleExpression<?> applyOperationForDateCustomFields(Operation operation, Expression<?> baseExp, Expression... operands) {
		SimpleExpression result;

		Operator operator = Ops.SUBSTR_2ARGS;
		Expression<Integer> subStringBegin = Expressions.constant(0);
		Expression<Integer> subStringEnd;

		switch (operation){//NOSONAR a switch...
			case BY_YEAR:
				subStringEnd = Expressions.constant(BY_YEAR_SUBSTRING_SIZE);
				break;
			case BY_MONTH:
				subStringEnd = Expressions.constant(BY_MONTH_SUBSTRING_SIZE);
				break;
			case BY_DAY:
				subStringEnd = Expressions.constant(BY_DAY_SUBSTRING_SIZE);
				break;
			case COUNT://If it's a count we don't need substring we must return with a correct count expression
				operator = ExtOps.S_COUNT;
				return Expressions.operation(operator.getType(), operator, baseExp);
			default:
				throw new IllegalArgumentException("Unknown operation for date custom field");
		}

		result = Expressions.operation(operator.getType(), operator, baseExp, subStringBegin, subStringEnd);

		return result;
	}




	/**
	 * creates an Expression like 'baseExp' 'operation' 'operand1', 'operand2' ...
	 * @return
	 */
	BooleanExpression createPredicate(Operation operation, Expression<?> baseExp, DataType datatype,
									  Expression... operands) {

		BooleanExpression predicate;

		// special case
		if (operation == Operation.NOT_NULL || operation == Operation.IS_NULL) {
			predicate = createExistencePredicate(operation, baseExp, operands);
		}

		// special case for date
		else if (datatype == DataType.DATE) {
			predicate = createDatePredicate(operation, baseExp, operands);
		}
		// normal case
		else {
			Operator operator = getOperator(operation);

			Expression[] expressions = prepend(baseExp, operands);

			predicate = Expressions.predicate(operator, expressions);

		}

		return predicate;

	}

	/*
	 * There is a special treatment when operation = IS_NULL / NOT_NULL. Indeed one cannot
	 * write 'where attribute is not null = true|false' : although legal in most SQL database,
	 * HQL will just not have it.
	 *
	 * So we must infer if we need operator IS_NULL or IS_NOT_NULL :
	 * 1/ from the operand (true or false),
	 * 2/ also from the operation stated in the column (IS_NULL or NOT_NULL)
	 *
	 * Indeed (IS_NULL == true) == (IS_NOT_NULL == false) and vice-versa
	 *
	 */
	private BooleanExpression createExistencePredicate(Operation operation, Expression<?> baseExp, Expression... operands) {
		String arg = operands[0].toString();
		boolean argIsTrue = "true".equals(arg) || "1".equals(arg);
		boolean operIsIS_NULL = operation == Operation.IS_NULL;

		// when both boolean have the same value then the actual operation is IS_NULL, else it's the other one
		Ops actualOperator = argIsTrue == operIsIS_NULL ? Ops.IS_NULL : Ops.IS_NOT_NULL;
		return Expressions.predicate(actualOperator, baseExp);
	}


	@SuppressWarnings("unchecked")
	private BooleanExpression createDatePredicate(Operation operation, Expression<?> baseExp, Expression... operands) {

		Expression<Date> exp = operands[0];
		DateOperation<Date> dateOp = Expressions.dateOperation(Date.class, DateTimeOps.DATE, baseExp);
		BooleanExpression result;
		switch (operation) {
			case EQUALS:
				result = dateOp.eq(exp);
				break;
			case BETWEEN:
				result = dateOp.between(exp, operands[1]);
				break;
			case GREATER:
				result = dateOp.gt(exp);
				break;
			case GREATER_EQUAL:
				result = dateOp.goe(exp);
				break;
			case LOWER:
				result = dateOp.lt(exp);
				break;
			case LOWER_EQUAL:
				result = dateOp.loe(exp);
				break;
			case NOT_EQUALS:
				result = dateOp.ne(exp);
				break;
			default:
				throw new IllegalArgumentException("Operation '" + operation + "' not yet supported");
		}

		return result;

	}

	List<Expression<?>> createOperands(Filter filter, Operation operation) {
		DataType type = filter.getDataType();
		List<String> values = filter.getValues();
		return makeOperands(operation, type, values);
	}


	// ******************************* private stuffs *********************


	@SuppressWarnings("rawtypes")
	private PathBuilder makePath(Class<?> srcClass, String srcAlias, Class<?> attributeClass, String attributeAlias) {
		return new PathBuilder<>(srcClass, srcAlias).get(attributeAlias, attributeClass);
	}


	/*
	 * should be invoked only on columns of AttributeType = ATTRIBUTE
	 *
	 */
	private PathBuilder attributePath(ColumnPrototypeInstance column) {

		ColumnPrototype prototype = column.getColumn();

		InternalEntityType type = InternalEntityType.fromSpecializedType(column.getSpecializedType());

		String alias = getQName(type);
		Class<?> clazz = type.getClass();
		String attribute = prototype.getAttributeName();
		Class<?> attributeType = classFromDatatype(prototype.getDataType());

		return makePath(clazz, alias, attributeType, attribute);

	}

	// returns the path to the ID of the entity
	private PathBuilder idPath(ColumnPrototypeInstance column) {

		InternalEntityType type = InternalEntityType.fromSpecializedType(column.getSpecializedType());

		String alias = getQName(type);
		Class<?> clazz = type.getClass();

		return makePath(clazz, alias, Long.class, "id");
	}


	List<Expression<?>> makeOperands(Operation operation, DataType type, List<String> values) {
		try {

			List<Expression<?>> expressions = new ArrayList<>(values.size());

			/*
			 * Usually binary operations have operands of the same type of
			 * the column they apply to, except for NOT_NULL which accepts a
			 * boolean instead. Hence the line below.
			 */
			DataType actualType = operation == Operation.NOT_NULL ? BOOLEAN : type;

			for (String val : values) {// NOSONAR that's a <no swearing please> it's not complex !

				Object operand;

				switch (actualType) {
					case INFO_LIST_ITEM:
					case LIST:
					case STRING:
					case TAG:
					case DATE_AS_STRING:
						operand = val;
						break;
					case NUMERIC:
						operand = val.contains(".") ? Double.valueOf(val) : Long.valueOf(val);
						break;
					case DATE:
						operand = DateUtils.parseIso8601Date(val);
						break;
					case EXECUTION_STATUS:
						operand = ExecutionStatus.valueOf(val);
						break;
					case LEVEL_ENUM:
						operand = LevelEnumHelper.valueOf(val);
						break;
					case BOOLEAN_AS_STRING:
						operand = val.toLowerCase();
						break;
					case BOOLEAN:
					case EXISTENCE:
						operand = Boolean.valueOf(val.toLowerCase());
						break;
					default:
						throw new IllegalArgumentException("type '" + type + "' not yet supported");
				}

				if (Operation.LIKE == operation) {
					operand = '%' + operand.toString() + '%';
				}

				expressions.add(Expressions.constant(operand));
			}


			if (operation == Operation.IN) {
				List<Expression<?>> listeExpression = new ArrayList<>(1);
				listeExpression.add(ExpressionUtils.list(Object.class, expressions.toArray(new Expression[expressions.size()])));
				return listeExpression;
			}
			return expressions;

		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}


	private SubQueryBuilder createSubquery(ColumnPrototypeInstance col) {
		ColumnPrototype prototype = col.getColumn();
		ChartQuery queryDef = prototype.getSubQuery();
		DetailedChartQuery detailedDef = new DetailedChartQuery(queryDef);

		return new SubQueryBuilder(detailedDef);
	}


	private Operator getOperator(Operation operation) {
		Operator operator;

		switch (operation) {// NOSONAR that's a <no swearing please> switch it's not complex !
			case EQUALS:
				operator = Ops.EQ;
				break;
			case LIKE:
				operator = Ops.LIKE;
				break;
			case BY_DAY:
				operator = ExtOps.YEAR_MONTH_DAY;
				break;
			case BY_WEEK:
				operator = DateTimeOps.YEAR_WEEK;
				break;
			case BY_MONTH:
				operator = DateTimeOps.YEAR_MONTH;
				break;
			case BY_YEAR:
				operator = DateTimeOps.YEAR;
				break;
			case COUNT:
				operator = ExtOps.S_COUNT;
				break;
			case SUM:
				operator = ExtOps.S_SUM;
				break;
			case GREATER:
				operator = Ops.GT;
				break;
			case IN:
				operator = Ops.IN;
				break;
			case BETWEEN:
				operator = Ops.BETWEEN;
				break;
			case AVG:
				operator = ExtOps.S_AVG;
				break;
			case GREATER_EQUAL:
				operator = Ops.GOE;
				break;
			case LOWER:
				operator = Ops.LT;
				break;
			case LOWER_EQUAL:
				operator = Ops.LOE;
				break;
			case MAX:
				operator = ExtOps.S_MAX;
				break;
			case MIN:
				operator = ExtOps.S_MIN;
				break;
			case IS_NULL:
				operator = Ops.IS_NULL;
				break;
			case NOT_NULL:
				operator = Ops.IS_NOT_NULL;
				break;
			case NOT_EQUALS:
				operator = Ops.NE;
				break;
			default:
				throw new IllegalArgumentException("Operation '" + operation + "' not yet supported");
		}

		return operator;
	}

	private Expression[] prepend(Expression head, Expression... tail) {
		Expression[] res = new Expression[tail.length + 1];
		res[0] = head;
		System.arraycopy(tail, 0, res, 1, tail.length);
		return res;
	}


	private Class<?> classFromDatatype(DataType type) {
		Class<?> result;

		switch (type) {
			case DATE:
				result = Date.class;
				break;
			case STRING:
				result = String.class;
				break;
			case NUMERIC:
				result = Long.class;
				break;
			case EXECUTION_STATUS:
				result = ExecutionStatus.class;
				break;
			case INFO_LIST_ITEM:
				result = InfoListItem.class;
				break;
			case LEVEL_ENUM:
				result = Level.class;
				break;

			default:
				throw new IllegalArgumentException("datatype '" + type + "' is not yet supported");
		}

		return result;
	}


	// warning : should be called on columns that have a ColumnType = CALCULATED only
	private QueryStrategy subQueryStrategy(ColumnPrototypeInstance col) {
		ColumnPrototype proto = col.getColumn();
		if (proto.getColumnType() != ColumnType.CALCULATED) {
			throw new IllegalArgumentException("column '" + proto.getLabel() + "' has a column type of '" + proto.getColumnType() + "', therefore it has no subquery");
		}
		return proto.getSubQuery().getStrategy();
	}


	private static final class AliasCollector implements Visitor<Void, Set<String>> {

		private Set<String> aliases = new HashSet<>();


		@Override
		public Void visit(Constant<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(FactoryExpression<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(com.querydsl.core.types.Operation<?> expr, Set<String> context) {
			for (Expression<?> subexpr : expr.getArgs()) {
				subexpr.accept(this, context);
			}
			return null;
		}

		@Override
		public Void visit(ParamExpression<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(Path<?> expr, Set<String> context) {
			PathMetadata metadata = expr.getMetadata();
			if (metadata.isRoot()) {
				context.add(expr.getMetadata().getName());
			} else {
				metadata.getParent().accept(this, context);
			}

			return null;
		}

		@Override
		public Void visit(SubQueryExpression<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(TemplateExpression<?> expr, Set<String> context) {
			return null;
		}

		Set<String> getAliases() {
			return aliases;
		}

	}

}
