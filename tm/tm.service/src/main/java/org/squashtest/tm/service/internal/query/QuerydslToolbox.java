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
package org.squashtest.tm.service.internal.query;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.CustomFieldValueOption;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.jpql.ExtOps;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.ColumnType;
import org.squashtest.tm.domain.query.DataType;
import org.squashtest.tm.domain.query.Operation;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.query.QueryFilterColumn;
import org.squashtest.tm.domain.query.QueryModel;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.query.QueryStrategy;
import org.squashtest.tm.domain.query.SpecializedEntityType;
import org.squashtest.tm.domain.requirement.RequirementStatus;

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
import org.squashtest.tm.service.internal.batchimport.MilestoneImportHelper;

class QuerydslToolbox {

	public static final int BY_YEAR_SUBSTRING_SIZE = 4;
	public static final int BY_MONTH_SUBSTRING_SIZE = 7;
	public static final int BY_DAY_SUBSTRING_SIZE = 10;
	private static final String NOT_YET_SUPPORTED = "' not yet supported";
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
	QuerydslToolbox(QueryColumnPrototypeInstance column) {
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

	EntityPathBase<?> getQBean(QueryColumnPrototypeInstance column) {
		InternalEntityType type = InternalEntityType.fromSpecializedType(column.getSpecializedType());
		return getQBean(type);
	}

	String getAlias(EntityPathBase<?> path) {
		return path.getMetadata().getName();
	}

	String getCustomFieldValueTableAlias(QueryColumnPrototype columnPrototype, Long cufId) {
		if (columnPrototype.getDataType().equals(DataType.TAG)){
			return getCustomFieldValueOptionTableAlias(columnPrototype,cufId);
		}
		return getCustomFieldValueStandardTableAlias(columnPrototype,cufId);
	}

	String getCustomFieldValueStandardTableAlias(QueryColumnPrototype columnPrototype, Long cufId) {
		return columnPrototype.getLabel() + "_" + cufId;
	}

	String getCustomFieldValueOptionTableAlias(QueryColumnPrototype columnPrototype, Long cufId) {
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
	 * - if the column is an ATTRIBUTE | ENTITY, per construction the data is scalar (therefore no aggregate)
	 * - if the column is a calculated of subquery, the filter will be handled from within the subquery, then
	 * 	the whole subquery will be converted to a where clause for the outerquery. see #createAsPredicate()
	 * to see how it's done.
	 * - if the column is a custom field, the said custom field is likely a scalar too (unless one day we
	 * want to count how many tags a given cuf taglist contains).
	 *
	 */
	boolean isWhereClauseComponent(QueryFilterColumn filter) {
		QueryColumnPrototypeInstance column = filter;

		while (column.getColumn().getColumnType() == ColumnType.CALCULATED &&
			subQueryStrategy(column) == QueryStrategy.INLINED
			) {
			column = column.getColumn().getSubQuery().getProjectionColumns().get(0);
		}

		return !isAggregate(column.getOperation());
	}

	boolean isHavingClauseComponent(QueryFilterColumn filter) {
		return !isWhereClauseComponent(filter);
	}

	boolean isSubquery(QueryColumnPrototypeInstance proto) {
		return proto.getColumn().getColumnType() == ColumnType.CALCULATED;
	}

	// ***************************** high level API ***********************


	/**
	 * Turns the given column into an expression suitable for the 'select' clause.
	 *
	 * @param col
	 * @return
	 */
	Expression<?> createAsSelect(QueryColumnPrototypeInstance col) {

		Expression<?> selectElement;

		QueryColumnPrototype proto = col.getColumn();

		switch (proto.getColumnType()) {
			case ENTITY:
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
	BooleanExpression createAsPredicate(QueryFilterColumn filter) {
		BooleanExpression predicate;

		QueryColumnPrototype proto = filter.getColumn();

		switch (proto.getColumnType()) {
			case ENTITY:
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


	/**
	 * Turns the column into an expression suitable for a groupBy clause.
	 *
	 * @param col
	 * @return
	 */
	Expression<?> createAsGroupBy(QueryColumnPrototypeInstance col){
		// for now it's totally equivalent to #createAsSelect
		return createAsSelect(col);
	}


	/**
	 * Turns the column into an expression suitable for a sortBy clause. Note that raw column expression alone is created; the consumer must appends the sort direction.
	 * Note that when the column datatype is LEVEL_ENUM, the result will be equivalent to
	 * #createAsCaseWhen
	 *
	 * @param col
	 * @return
	 */
	Expression<?> createAsSortBy(QueryColumnPrototypeInstance col){
		// level enum require a 'case when' construct
		// that will help to sort them by rank instead of
		// lexicographically.
		if (col.getDataType().isAssignableToLevelEnum()){
			return createAsCaseWhen(col);
		}
		// for the other columns the sortBy column expression is the same than for the select expression
		else{
			return createAsSelect(col);
		}
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
	 * or ColumnType = ENTITY
	 *
	 */
	Expression<?> createAttributeSelect(QueryColumnPrototypeInstance column) {
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
	Expression<?> createSubquerySelect(QueryColumnPrototypeInstance col) {
		Expression<?> expression = null;

		switch (subQueryStrategy(col)) {

			// create a subselect statement
			// NOSONAR because this is definitely not too long
			case SUBQUERY:

				SpecializedEntityType specType = col.getSpecializedType();
				InternalEntityType internalType = InternalEntityType.fromSpecializedType(specType);
				EntityPathBase<?> colBean = getQBean(col);

				SubQueryBuilder qbuilder = new SubQueryBuilder(col)
											   .asSubselectQuery()
											   .withRootEntity(internalType)
											   .joinRootEntityOn(colBean);

				expression = qbuilder.createQuery();
				break;

			// fetches the measure from the subquery
			// NOSONAR because this is definitely not too long
			case INLINED:
				QuerydslToolbox subtoolbox = new QuerydslToolbox(col);
				QueryModel subqueryModel = col.getColumn().getSubQuery();
				QueryProjectionColumn projectedColumn = subqueryModel.getProjectionColumns().get(0);

				expression = subtoolbox.createAsSelect(projectedColumn);
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

	private Expression<?> createCustomFieldSelect(QueryColumnPrototypeInstance col) {
		Expression<?> expression;

		QueryColumnPrototype columnPrototype = col.getColumn();
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
	 * Creates an expression fit for a "where" clause,  for columns of ColumnType = ATTRIBUTE | ENTITY
	 *
	 */
	@SuppressWarnings("unchecked")
	BooleanExpression createAttributePredicate(QueryFilterColumn filter) {

		Operation operation = filter.getOperation();

		QueryColumnPrototype column = filter.getColumn();
		DataType datatype = column.getDataType();


		// make the expression on which the filter is applied
		Expression<?> attrExpr = attributePath(filter);

		// convert the operands
		List<Expression<?>> valExpr = makeOperands(column, operation, filter.getValues());
		Expression<?>[] operands = valExpr.toArray(new Expression[valExpr.size()]);


		return createPredicate(operation, attrExpr, datatype, operands);
	}

	/**
	 * Creates an expression fit for a "where" or "having" clause. It's up to the caller to
	 * know what to do with that.
	 *
	 */
	BooleanExpression createSubqueryPredicate(QueryFilterColumn filter) {
		BooleanExpression predicate = null;

		switch (subQueryStrategy(filter)) {

			// create "where exists (subquery)" expression
			case SUBQUERY:
				EntityPathBase<?> colBean = getQBean(filter);
				//create the subquery
				QueryBuilder qbuilder = new SubQueryBuilder(filter)
					.asSubwhereQuery()
					.withRootEntity(filter.getSpecializedType())
					.joinRootEntityOn(colBean)
					.filterOn(filter);

				Expression<?> subquery = qbuilder.createQuery();

				// now integrate the subquery
				predicate = Expressions.predicate(Ops.EXISTS, subquery);

				break;

			case INLINED:
				QueryModel subqueryModel = filter.getColumn().getSubQuery();
				QueryProjectionColumn subProjection = subqueryModel.getProjectionColumns().get(0);
				QuerydslToolbox subtoolbox = new QuerydslToolbox(filter);    // create a new toolbox configured with a proper subcontext

				//ok, it is semantically sloppy. But for now the produced element is what we need :-S
				Expression<?> subexpr = subtoolbox.createAsSelect(subProjection);

				List<Expression<?>> valExpr = makeOperands(filter.getColumn(), filter.getOperation(), filter.getValues());
				Expression<?>[] operands = valExpr.toArray(new Expression[valExpr.size()]);

				predicate = createPredicate(filter.getOperation(), subexpr, subProjection.getDataType(), operands);

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
	BooleanExpression createCufPredicate(QueryFilterColumn filter) {

		QueryColumnPrototype columnPrototype = filter.getColumn();
		DataType dataType = columnPrototype.getDataType();

		Long cufId = filter.getCufId();
		String alias = getCustomFieldValueStandardTableAlias(columnPrototype, cufId);
		Operation operation = filter.getOperation();

		// convert the operands
		List<Expression<?>> valExpr = makeOperands(columnPrototype, operation, filter.getValues());
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

	private SimpleExpression<?> applyOperationForDateCustomFields(Operation operation, Expression<?> baseExp) {
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
		else if(datatype == DataType.ENTITY) {
			predicate = createEntityPredicate(operation, baseExp, operands);
		}
		// another special case, for regex
		else if (operation == Operation.MATCHES){
			predicate = createMatchPredicate(operation, baseExp, operands);
		}
		else if (operation == Operation.FULLTEXT) {
			predicate = createFullTextPredicate(operation, baseExp, operands);
		}
		else if (operation == Operation.LIKE) {
			predicate = createLikePredicate(operation, baseExp, operands);
		}
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
				throw new IllegalArgumentException("Operation '" + operation + NOT_YET_SUPPORTED);
		}

		return result;

	}


	private BooleanExpression createMatchPredicate(Operation operation, Expression<?> baseExp, Expression... operands) {
		BooleanExpression matchExpr = Expressions.booleanOperation(ExtOps.S_MATCHES, baseExp, operands[0]);
		// the isTrue() is necessary, because the result of the match (positive or negative) still needs to 
		// be compared to something.
		return matchExpr.isTrue();
	}

	private BooleanExpression createFullTextPredicate(Operation operation, Expression<?> baseExp, Expression... operands) {
		BooleanExpression matchExpr = Expressions.booleanOperation(ExtOps.FULLTEXT, baseExp, operands[0]);

		return matchExpr.isTrue();
	}

	private BooleanExpression createLikePredicate(Operation operation, Expression<?> baseExp, Expression... operands) {
		BooleanExpression matchExpr = Expressions.booleanOperation(ExtOps.LIKE_INSENSITIVE, baseExp, operands[0]);

		return matchExpr.isTrue();
	}

	private BooleanExpression createEntityPredicate(Operation operation, Expression<?> baseExp, Expression... operands) {
		if(operation != Operation.IS_CLASS) {
			throw new IllegalArgumentException("Operation other than IS_CLASS is not allowed with DataType ENTITY");
		}
		return Expressions.booleanOperation(ExtOps.IS_CLASS, baseExp, operands[0]);
	}


	List<Expression<?>> createOperands(QueryFilterColumn filter, Operation operation) {
		QueryColumnPrototype column = filter.getColumn();
		List<String> values = filter.getValues();
		return makeOperands(column, operation, values);
	}


	/**
	 * Creates a Case-When construct with the given column. The column must have
	 * a DataType of LEVEL_ENUM or an IllegalArgumentException will be thrown.
	 *
	 * @param col
	 * @return
	 */
	Expression<?> createAsCaseWhen(QueryColumnPrototypeInstance col){

		// guard
		if (! col.getDataType().isAssignableToLevelEnum()){
			throw new IllegalArgumentException("Attempted to create a CaseWhen construct on a non LEVEL_ENUM column");
		}

		CaseBuilder.Cases cases = null;

		Expression<?> colExpr = createAsSelect(col);

		EnumHelper helper = new EnumHelper(col.getColumn());
		Map<Level, Integer> levels = helper.getLevelMap();

		for (Map.Entry<Level, Integer> entry : levels.entrySet()){
			Level enumValue = entry.getKey();
			Integer level = entry.getValue();
			Predicate predicate = Expressions.predicate(Ops.EQ, colExpr, Expressions.constant(enumValue));

			if (cases == null){
				cases = new CaseBuilder().when(predicate).then(level);
			}
			else{
				cases = cases.when(predicate).then(level);
			}

		}

		return cases.otherwise(-1000);

	}

	// ******************************* private stuffs *********************


	@SuppressWarnings("rawtypes")
	private PathBuilder makePath(Class<?> srcClass, String srcAlias, Class<?> attributeClass, String attributeAlias) {
		return new PathBuilder<>(srcClass, srcAlias).get(attributeAlias, attributeClass);
	}

	@SuppressWarnings("rawtypes")
	private PathBuilder makePath(Class<?> srcClass, String srcAlias) {
		return new PathBuilder<>(srcClass, srcAlias);
	}

	/*
	 * should be invoked only on columns of ColumnType = ATTRIBUTE | ENTITY
	 *
	 */
	private PathBuilder attributePath(QueryColumnPrototypeInstance column) {

		QueryColumnPrototype prototype = column.getColumn();

		InternalEntityType type = InternalEntityType.fromSpecializedType(column.getSpecializedType());

		Class<?> clazz = type.getEntityClass();
		String alias = getQName(type);

		// if the column represents the entity itself
		if (prototype.representsEntityItself()){
			return makePath(clazz, alias);
		}
		// if the column is an attribute
		else{
			String attribute = prototype.getAttributeName();
			Class<?> attributeType = classFromDatatype(prototype.getDataType());

			return makePath(clazz, alias, attributeType, attribute);
		}

	}

	/**
	 * From the supplied string values, creates the operands of the correct types based on which column
	 * and which operation.
	 *
	 * @param prototype
	 * @param operation
	 * @param values
	 * @return
	 */
	List<Expression<?>> makeOperands(QueryColumnPrototype prototype, Operation operation, List<String> values) {

		DataType type = prototype.getDataType();

		try {

			List<Expression<?>> expressions = new ArrayList<>(values.size());

			/*
			 * Usually binary operations have operands of the same type of
			 * the column they apply to, except for NOT_NULL which accepts a
			 * boolean instead. Hence the line below.
			 */
			DataType actualType = operation == Operation.NOT_NULL ? DataType.BOOLEAN : type;

			for (String val : values) {// NOSONAR that's a <no swearing please> it's not complex !

				Object operand;

				switch (actualType) {
					case INFO_LIST_ITEM:
					case LIST:
					case STRING:
					case TAG:
					case DATE_AS_STRING:
					case TEXT:
						operand = val;
						break;
					case NUMERIC:
						operand = val.contains(".") ? Double.valueOf(val) : Long.valueOf(val);
						break;
					case DATE:
						operand = DateUtils.parseIso8601Date(val);
						break;
					case EXECUTION_STATUS:
					case REQUIREMENT_STATUS:
					case LEVEL_ENUM:
						EnumHelper helper = new EnumHelper(prototype);
						operand = helper.valueOf(val);
						break;
					case BOOLEAN_AS_STRING:
						operand = val.toLowerCase();
						break;
					case BOOLEAN:
					case EXISTENCE:
						operand = Boolean.valueOf(val.toLowerCase());
						break;
					case ENTITY:
						try {
							operand = Class.forName(val);
						} catch (ClassNotFoundException ex) {
							throw new IllegalArgumentException(ex);
						}
						break;
					default:
						throw new IllegalArgumentException("type '" + type + NOT_YET_SUPPORTED);
				}

				if (Operation.LIKE == operation && !operand.toString().contains("%")) {
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
			case FULLTEXT:
				operator = ExtOps.FULLTEXT;
				break;

			default:
				throw new IllegalArgumentException("Operation '" + operation + NOT_YET_SUPPORTED);
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
			case TEXT:
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
			case REQUIREMENT_STATUS:
				result = RequirementStatus.class;
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
	private QueryStrategy subQueryStrategy(QueryColumnPrototypeInstance col) {
		QueryColumnPrototype proto = col.getColumn();
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
