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
package org.squashtest.tm.domain.jpql;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BooleanType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


// TODO : move in there the group concat functions, and maybe share them with QueryDSL ?

/**
 * <p>
 * 	This class defines some extensions to JPQL language, that must be registered in the HibernateSessionFactory,
 * 	and can also be used in QueryDSL if needed.
 * </p>
 *
 * <h4>group_concat</h4>
 *
 * <p>
 * 	This section factory declares a custom function for the support of group_concat - which is not a JPQL standard function.
 * 	The implementation for this custom function depends on the target database. See GroupConcatFunction for h2 and mysql,
 * 	StringAggFunction for Postgresql
 * </p>
 *
 * <p>
 *     Also supported since 1.20 :
 *     <b>matches</b>(column, 'regular expression') : test by regular expressions. Examples :
 *     <ul>
 *         <li>select matches (column, 'toto.*') from table; => returns true or false</li>
 *     		<li>select name from TestCase where matches(name, '^TEST_.*') = true; ==> returns the names of all test cases that begins with 'TEST_'.
 *     			Flip the boolean to false to negate the result.
 *     		</li>
 *     </ul>
 *     <b>i_matches(column, 'regular exprssion) : same things case insensitive
 * </p>
 *
 * <p>
 * 	Syntax is as follow :
 *  <ul>
 *  	<li><pre>group_concat(<it>col identifier</it>)</pre>will concatenate as expected over the column identifier.</li>
 *  	<li><pre>group_concat(<it>col identifier 1</it>, 'order by', <it>col identifier 2</it>, ['asc|desc']) </pre>will send this to the target db as 'group_concat(id1 order by id2 [asc|desc])'
 *  </ul>
 * </p>
 *
 * 	<h4>Aggregate function wrappers</h4>
 *
 *  <p>
 * 	The standard aggregate functions : count, sum, avg, min and max, used in a JPA query, cannot apply on subqueries.
 * 	For instance one cannot sum((select id from entity)). The antlr parser used by Hibernate will just dismiss such nodes
 * 	although the would-be SQL would run just fine on the target database.
 * </p>
 *
 * <p>By chance custom sql function can do so. So, by registering the standard aggregate functions as custom functions we
 * can trick Hibernate parser into thinking that the jpa item it reads is a custom function while it will just really print
 * a regular function in the sql output, and so offer us free support for aggregate over subqueries.</p>
 *
 * <p>The function names are :
 *
 * <ul>
 * 	<li>'s_avg',</li>
 * 	<li>'s_sum',</li>
 * 	<li>'s_min',</li>
 * 	<li>'s_max',</li>
 * 	<li>'s_count',</li>
 * </ul>
 *
 * Those wrapper use distinct names so that the native aggregate functions are not overridden : depending on you needs
 * you may use the regular 'count', or the corresponding wrapper 's_count'.</p>
 * @author bsiri
 *
 */
public final class HibernateDialectExtensions {

	public static final String FN_NAME_GROUP_CONCAT = "group_concat";
	public static final String FN_NAME_MATCHES = "matches";
	public static final String FN_NAME_I_MATCHES = "i_matches";

	public static final String FN_NAME_FULLTEXT = "fulltext_search";

	public static final String FN_INSENSITIVE_LIKE = "i_like";

	public static final String FN_NAME_WEEK = "week";


	public static final String FN_NAME_SUM = "s_sum";
	public static final String FN_NAME_CNT = "s_count";
	public static final String FN_NAME_MIN = "s_min";
	public static final String FN_NAME_MAX = "s_max";
	public static final String FN_NAME_AVG = "s_avg";


    private HibernateDialectExtensions(){
        super();
    }




        public static Map<String, SQLFunction> getMysqlDialectExtensions(){
            Map<String, SQLFunction> extensions = getCommonExtensions();
            extensions.put(FN_NAME_GROUP_CONCAT, new GroupConcatFunction(FN_NAME_GROUP_CONCAT, StringType.INSTANCE));
            extensions.put(FN_NAME_MATCHES, new Regexp());
            extensions.put(FN_NAME_I_MATCHES, new InsensitiveRegexp());
			extensions.put(FN_NAME_FULLTEXT, new MatchFullTextSearch());
			extensions.put(FN_INSENSITIVE_LIKE, new LikeInsensitiveSearch());
            return extensions;
        }

        public static Map<String, SQLFunction> getH2DialectExtensions(){
            Map<String, SQLFunction> extensions = getCommonExtensions();
            extensions.put(FN_NAME_GROUP_CONCAT, new GroupConcatFunction(FN_NAME_GROUP_CONCAT, StringType.INSTANCE));
			extensions.put(FN_NAME_MATCHES, new Regexp());
			extensions.put(FN_NAME_I_MATCHES, new InsensitiveRegexp());
			extensions.put(FN_INSENSITIVE_LIKE, new LikeInsensitiveSearch());
            return extensions;
        }

        public static Map<String, SQLFunction> getPostgresDialectExtensions(){
            Map<String, SQLFunction> extensions = getCommonExtensions();
            extensions.put(FN_NAME_GROUP_CONCAT, new StringAggFunction(FN_NAME_GROUP_CONCAT, StringType.INSTANCE));
            extensions.put(FN_NAME_WEEK, new ExtractWeek(FN_NAME_WEEK, IntegerType.INSTANCE));
            extensions.put(FN_NAME_MATCHES, new TildeStar());
			extensions.put(FN_NAME_I_MATCHES, new InsensitiveTildeStar());
			extensions.put(FN_NAME_FULLTEXT, new TsQueryFullTextSearch());
			extensions.put(FN_INSENSITIVE_LIKE, new ILikeSearch());
            return extensions;
        }


        private static Map<String, SQLFunction> getCommonExtensions(){
            Map<String, SQLFunction> extensions = new HashMap<>();
            extensions.put(FN_NAME_SUM, new StandardSQLFunction("sum"));
            extensions.put(FN_NAME_MIN, new StandardSQLFunction("min"));
            extensions.put(FN_NAME_MAX, new StandardSQLFunction("max"));
            extensions.put(FN_NAME_AVG, new StandardSQLFunction("avg", DoubleType.INSTANCE));
            extensions.put(FN_NAME_CNT, new SCountDistinctFunction());
            return extensions;
        }




	/* **************************************************
	 *  Inner classes
	 * *************************************************/

	private static class SCountDistinctFunction extends StandardSQLFunction {

		SCountDistinctFunction() {
			super("count", LongType.INSTANCE);
		}

		@Override
		public final String render(Type firstArgumentType, List arguments, SessionFactoryImplementor sessionFactory) {
			return "count(distinct " + arguments.get(0) + " )";
		}


	}

	/**
	 * <p>This custom implementation of group_concat. Because it can contain an embedded expression, hibernate will try to parse it just as if
	 * it was within the scope of the main query - thus causing parsing exception.</p>
	 *
	 *  <p>To prevent this we had to make the awkward syntax as follow:</p>
	 *
	 *  <ul>
	 *  	<li>group_concat(<it>col identifier</it>) : will concatenate as expected over the column identifier.</li>
	 *  	<li>group_concat(<it>col identifier 1</it>, 'order by', <it>col identifier 2</it> , ['asc|desc'], [sep]) : will send this to the target db as 'group_concat(id1 order by id2 [asc|desc] separator sep)'
	 *  </ul>
	 *
	 *  <p>
	 *      : group_concat(tc.label, 'order by', tc.id, 'desc', '|') -> concatenate
	 *  </p>
	 *
	 *
	 *
	 * @author bsiri
	 *
	 */
	private static class GroupConcatFunction extends StandardSQLFunction {

		GroupConcatFunction(String name, Type registeredType) {
			super(name, registeredType);
		}

		GroupConcatFunction(String name) {
			super(name);
		}

		@Override
		public final String render(Type firstArgumentType, List arguments, SessionFactoryImplementor sessionFactory) {

			if (arguments.size() == 1) {
				return super.render(firstArgumentType, arguments, sessionFactory);
			} else {
				try {
					// validation
					String direction = arguments.size() >= 4 ? ((String) arguments.get(3)).replaceAll("'", "") : "asc";
					String separator = arguments.size() >= 5 ? ((String) arguments.get(4)).replaceAll("'", "") : ",";
					if (!("asc".equalsIgnoreCase(direction) || "desc".equalsIgnoreCase(direction))) {
						throw new IllegalArgumentException();
					}
					if (!"'order by'".equalsIgnoreCase((String) arguments.get(1))) {
						throw new IllegalArgumentException();
					}

					// expression
					return createSqlQuery(arguments, direction, separator);
				} catch (IllegalArgumentException ex) {
					throw new IllegalArgumentException("usage of custom hql group_concat : group_concat(col id, [ 'order by', col id2, ['asc|desc']]", ex);
				}
			}

		}

		protected String createSqlQuery(List<?> arguments, String direction, String separator) {
			return "group_concat(" + arguments.get(0) + " order by " + arguments.get(2) + " " + direction + " separator '" + separator + "')";
		}

	}

	/**
	 * Equivalent of GroupConcatFunction for Postgresql
	 *
	 */
	private static final class StringAggFunction extends GroupConcatFunction {

		StringAggFunction(String name) {
			super(name);
		}

		StringAggFunction(String name, Type registeredType) {
			super(name, registeredType);
		}

		@Override
		public String createSqlQuery(List<?> arguments, String direction, String separator) {
			return "string_agg(cast(" + arguments.get(0) + " as text),'" + separator + "' order by " + arguments.get(2)
				+ " " + direction + ")";
		}
	}

	/**
	 * Support for week(timestamp) function
	 *
	 */
	private static final class ExtractWeek extends  StandardSQLFunction{

		ExtractWeek(String name) {
			super(name);
		}

		public ExtractWeek(String name, Type registeredType) {
			super(name, registeredType);
			// TODO Auto-generated constructor stub
		}

		@Override
		public final String render(Type firstArgumentType, List arguments, SessionFactoryImplementor sessionFactory) {
			return "extract(week from "+arguments.get(0) +")";
		}


	}


	/**
	 * Support for operator REGEXP (mysql, h2).
	 */
	private static final class Regexp extends SQLFunctionTemplate {
		public Regexp(){
			super(BooleanType.INSTANCE, " (?1 regexp ?2) ");
		}
	}

	private static final class InsensitiveRegexp extends SQLFunctionTemplate {
		public InsensitiveRegexp(){
			super(BooleanType.INSTANCE, " ( LOWER(?1) regexp LOWER(?2)) ");
		}
	}

	/**
	 * Support for tilde-star "~*" operator for postgresql
	 *
	 */
	private static final class TildeStar extends SQLFunctionTemplate{
		public TildeStar(){
			super(BooleanType.INSTANCE, " (?1 ~ ?2) ");
		}
	}

	private static final class InsensitiveTildeStar extends SQLFunctionTemplate{
		public InsensitiveTildeStar(){
			super(BooleanType.INSTANCE, " (?1 ~* ?2) ");
		}
	}

	private static final class MatchFullTextSearch extends  SQLFunctionTemplate{
		public MatchFullTextSearch() {
			super(BooleanType.INSTANCE, " (MATCH(?1) AGAINST(?2) > 0) ");
		}
	}

	private static final class TsQueryFullTextSearch extends SQLFunctionTemplate{
		public TsQueryFullTextSearch() {super(BooleanType.INSTANCE, " (?1 @@ to_tsquery(?2))");}
	}

	private static  final class LikeInsensitiveSearch extends  SQLFunctionTemplate{
		public LikeInsensitiveSearch() {
			super(BooleanType.INSTANCE, "( LOWER(?1) like (?2) )");
		}
	}

	private static final class ILikeSearch extends SQLFunctionTemplate {
		public ILikeSearch() {
			super(BooleanType.INSTANCE, "( (?1) ILIKE (?2) )");
		}
	}

}
