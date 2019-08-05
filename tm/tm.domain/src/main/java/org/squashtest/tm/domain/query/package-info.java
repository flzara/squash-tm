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

/**
 *
 * <h1>Domain "Query"</h1>
 *
 * <p>
 *     The documentation of the domain "Query" is centralized here, in order to provide with a better, global picture
 *     of the key concepts.
 * </p>
 *
 * <p>
 * The entities of this package are the basic units that modelize a query about the business. A dedicated engine engine
 * will turn them into a QueryDsl query before execution against the database. The custom charts and the search workspace
 * are powered by such query models. The basic construct for these are the {@link org.squashtest.tm.domain.query.QueryColumnPrototype}s
 * and the {@link org.squashtest.tm.domain.query.QueryModel}s, as described below.
 *</p>
 *
 * <h1>QueryModel</h1>
 *
 * A {@link org.squashtest.tm.domain.query.QueryModel} is, well, a model of the query. Its most important members are :
 *
 * <ul>
 * <li>What data you want to find (the {@link org.squashtest.tm.domain.query.QueryProjectionColumn}s)</li>
 * <li>If needed, how data should be grouped (the {@link org.squashtest.tm.domain.query.QueryAggregationColumn}s)</li>
 * <li>How specific you need the data to be (the {@link org.squashtest.tm.domain.query.QueryFilterColumn}s)</li>
 * <li>If needed, how they should be sorted (the {@link org.squashtest.tm.domain.query.QueryOrderingColumn}s</li>
 * </ul>
 *
 * Each of these query model members share the same set of {@link org.squashtest.tm.domain.query.QueryColumnPrototype},
 * which are then used differently depending on the context of that member, in other words which
 * role they assume in the query (projection, aggregation etc). Basically they can be translated in familiar SQL
 * concepts :
 *
 * <ul>
 *     <li>Projection - "select"</li>
 *     <li>Aggregation - "group by"</li>
 *     <li>Filter - "where / having"</li>
 *     <li>Order - "sort by"</li>
 * </ul>
 *
 * The query engine is not so straightforward and handles the corner cases behind the scene. However it is still the
 * responsibility of the caller to make sure that the query is well formed, ie :
 *
 * <ul>
 *     <li>If a column appears in the Order member, it must also appear in the Projection member</li>
 *     <li>If there is an Aggregation member, it must include all the columns that appear in the Projection member unless
 *     that column is subject to an Aggregate Operation (eg {@link org.squashtest.tm.domain.query.Operation#COUNT}).</li>
 * </ul>
 *
 * Within those constraints the query engine will derive the query plan, define what and how joins should be made, translate
 * the columns into expressions depending on their type and roles, assign aliases etc.
 *
 * <h1>Column prototypes</h1>
 *
 * <p>
 * The main java type that define a column is {@link org.squashtest.tm.domain.query.QueryColumnPrototype}. All available
 * columns are statically defined in the database. As explained above, each column prototype included in a query must
 * assume a role among Projection, Aggregation, Filter or Ordering as explained above. When used with a given role for
 * a given query, they conceptually become {@link org.squashtest.tm.domain.query.QueryColumnPrototypeInstance}).
 * </p>
 *
 * <h1>Column types</h1>
 *
 * <p>
 * A column represent a logical attribute of an entity. Attributes are said logical because they may or may not directly relate
 * to a database column : they represent a business information in a broader sense, which can be retrieved as-is when the information
 * is readily available, or will be reconstructed from other raw data if not.
 *
 * Please note that the column type thus refer to its natural or artificial nature, namely
 * {@link org.squashtest.tm.domain.query.ColumnType#ATTRIBUTE} (like 'label') or {@link org.squashtest.tm.domain.query.ColumnType#CALCULATED}
 * ('number of executions last month'), as opposed to the data type of the information (eg 'integer' or 'date').
 * </p>
 *
 * <p>
 * Most of the columns are either ATTRIBUTE or CALCULATED. In rare cases there are also two other types.
 * </p>
 * <p>
 *     The {@link org.squashtest.tm.domain.query.ColumnType#ENTITY} is a special column that represent the (JPA) entity
 *     itself, which means a whole group column in SQL terms (or would even require multiple SQL queries to build it).
 * </p>
 * <p>
 *	The {@link org.squashtest.tm.domain.query.ColumnType#CUF} designate the custom field columns : a custom field column
 *	here is "custom field of type X" of an entity.
 *
 * An example for instance is "a custom field of type date and bound to a TestCase". Here the column doesn't hold the name of the attribute,
 * unlike the other columns described above. This discrepancy of the model stems from the need of having a unmodifiable set of
 * {@link org.squashtest.tm.domain.query.QueryColumnPrototype}, statically defined in the database as referential data.
 * This requirement is incompatible with the custom fields, which are essentially dynamic. The alternative would have been to
 * manage (CRUD-like) a moving set of column prototypes that reflect the way custom fields are configured throughout the app.
 * </p>
 *
 * <p>
 * You can check a column type by looking at {@link org.squashtest.tm.domain.query.QueryColumnPrototype#getColumnType()} :
 * </p>
 *
 * <ul>
 * <li>{@link org.squashtest.tm.domain.query.ColumnType#ENTITY} : represents an entity itself</li>
 * <li>{@link org.squashtest.tm.domain.query.ColumnType#ATTRIBUTE} : represents a normal attribute - eg it maps directly to a database column</li>
 * <li>{@link org.squashtest.tm.domain.query.ColumnType#CALCULATED} : represents a derived attribute, that results from a subquery</li>
 * <li>{@link org.squashtest.tm.domain.query.ColumnType#CUF} : represents a custom field, here a special case of calculated column. See above for details.
 * </ul>
 *
 *
 * <p>Specialized Entity Type</p>
 *
 * <p>
 *     A column represent either an entity itself, either a (logical or actual) attribute of that entity. A natural candidate
 *     to model this would be the meta type {@link org.squashtest.tm.domain.EntityType}. However design
 *     shortcomings of the query engine impose to define more precisely the purpose of that entity in the context of a query,
 *     which called for a refinement of EntityType.
 * </p>
 * <p>
 *     What is a {@link org.squashtest.tm.domain.query.SpecializedEntityType} ?
 *     Consider for example the test cases, requirement and campaign belong to a Project (EntityType#PROJECT). Technically a
 *     a PROJECT is a PROJECT, but in the context of the query there is a distinction between a TEST_CASE_PROJECT
 *     ("the project of that test case") and a REQUIREMENT_PROJECT ("the project of that requirement) : the entity PROJECT
 *     can appear multiple times in the same query, but assumes different roles.
 * </p>
 *
 * <p>The association of an {@link org.squashtest.tm.domain.EntityType}
 *     and an {@link org.squashtest.tm.domain.query.SpecializedEntityType.EntityRole} taken together form a
 *     {@link org.squashtest.tm.domain.query.SpecializedEntityType}
 * </p>
 *
 *
 */
package org.squashtest.tm.domain.query;
