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
package org.squashtest.tm.internal.domain.report.query.hibernate;

import java.util.Arrays;

import org.hibernate.criterion.Criterion;
import org.squashtest.tm.internal.domain.report.query.QueryOperator;

/**
 * This class explicitly designs a criterion for a HibernateReportQuery.
 *
 * The primary goal of this class is to hold all the informations and metainformations describing a particular Criterion
 * for the parent Query.
 *
 * Implementing classes must also implement a method that return a Hibernate Criterion based on those informations.
 *
 *
 * Implementing the ReportCriterion : ==================================
 *
 * - makeCriterion() will generate the corresponding Hibernate Criterion, based on the informations you fed the instance
 * with. You might not need all of its properties (see the setters below) and those properties exist for convenience in
 * case you need them.
 *
 * @author bsiri
 *
 */

public abstract class ReportCriterion {
	private String criterionName = "";
	private QueryOperator operator = null;

	private Class<?> entityClass = null;
	private String entityAlias = "";

	private Class<?> paramClass = null;
	private String attributePath = "";

	private Object[] parameters = null;

	public ReportCriterion() {

	}

	/**
	 * Rich constructor for a ReportCriterion.
	 *
	 * @param criterionName
	 *            the name for that Criterion.
	 * @param attributePath
	 *            the path to the property of the target Hibernate entity.
	 */
	public ReportCriterion(String criterionName, String attributePath) {
		this.criterionName = criterionName;
		this.attributePath = attributePath;
	}

	public String getCriterionName() {
		return criterionName;
	}

	public void setCriterionName(String criterionName) {
		this.criterionName = criterionName;
	}

	public void setAttributePath(String attributePath) {
		this.attributePath = attributePath;
	}

	public void setParameter(Object... params) {
		this.parameters = params;
	}

	public String getAttributePath() {
		return attributePath;
	}

	/**
	 *
	 * @return the raw parameters, non typed.
	 */
	public Object[] getParameters() {
		return parameters;
	}

	/**
	 *
	 * @return the class of the target entity.
	 */
	protected Class<?> getEntityClass() {
		return entityClass;
	}

	protected void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	protected String getEntityAlias() {
		return entityAlias;
	}

	/**
	 * Sets an alternate alias for the target entity.
	 *
	 * @param entityAlias
	 */
	protected void setEntityAlias(String entityAlias) {
		this.entityAlias = entityAlias;
	}

	protected Class<?> getParamClass() {
		return paramClass;
	}

	/**
	 * Specify the class of the parameters.
	 *
	 * @param paramClass
	 *            the java class of the parameters.
	 */
	protected void setParamClass(Class<?> paramClass) {
		this.paramClass = paramClass;
	}

	protected QueryOperator getOperator() {
		return operator;
	}

	/**
	 * Sets a semantic hint about what will the criterion do. Implementations might use that hint or not.
	 *
	 * @param operator
	 */
	protected void setOperator(QueryOperator operator) {
		this.operator = operator;
	}

	protected void setParameters(Object[] parameters) {	//NOSONAR no, this array is not stored directly
		this.parameters = Arrays.copyOf(parameters, parameters.length);
	}

	/**
	 * This method will convert, using some of its properties, a Hibernate Criterion corresponding to that
	 * ReportCriterion.
	 *
	 * @return the converted Criterion.
	 */
	public abstract Criterion makeCriterion();

}
