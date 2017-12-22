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
package org.squashtest.tm.web.internal.report;

import java.util.Map;

import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.api.report.DocxTemplaterReport;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.ReportView;
import org.squashtest.tm.api.report.StandardReportCategory;
import org.squashtest.tm.api.report.StandardReportType;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.Input;

/**
 * @author Gregory Fouquet
 *
 */
public class IdentifiedReportDecorator implements Report {
	private final Report report;
	private final ReportIdentifier identifier;


	/**
	 * @param report
	 */
	/* package */IdentifiedReportDecorator(Report report, String namespace) {
		super();
		this.report = report;
		identifier = new ReportIdentifier(namespace);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.api.report.Report#getCategory()
	 */
	@Override
	public StandardReportCategory getCategory() {
		return report.getCategory();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.api.report.Report#getType()
	 */
	@Override
	public StandardReportType getType() {
		return report.getType();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.api.report.Report#getDescriptionKey()
	 */
	@Override
	public String getDescriptionKey() {
		return report.getDescriptionKey();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.api.report.Report#getViews()
	 */
	@Override
	public ReportView[] getViews() {
		return report.getViews();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.api.report.Report#getDescription()
	 */
	@Override
	public String getDescription() {
		return report.getDescription();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.api.report.Report#getForm()
	 */
	@Override
	public Input[] getForm() {
		return report.getForm();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.api.report.Report#getLabelKey()
	 */
	@Override
	public String getLabelKey() {
		return report.getLabelKey();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.api.report.Report#getLabel()
	 */
	@Override
	public String getLabel() {
		return report.getLabel();
	}

	/**
	 * @return the identifier
	 */
	public ReportIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * @return
	 * @see org.squashtest.tm.web.internal.report.ReportIdentifier#getNamespace()
	 */
	public String getNamespace() {
		return identifier.getNamespace();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (identifier == null ? 0 : identifier.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj){return true;}
		if (obj == null){return false;}
		if (getClass() != obj.getClass()){return false;}
		IdentifiedReportDecorator other = (IdentifiedReportDecorator) obj;
		if (identifier == null) {
			if (other.identifier != null){return false;}
		} else{ if (!identifier.equals(other.identifier)){return false;}
		}return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IdentifiedReportDecorator [labelKey=" + getLabelKey() + ", namespace=" + getNamespace() + "]";
	}

	public boolean isDocxTemplate(){
		 return report instanceof DocxTemplaterReport;
	}

	/**
	 * @return
	 * @see org.squashtest.tm.api.report.Report#buildModelAndView(int, java.lang.String, java.util.Map)
	 */
	@Override
	public ModelAndView buildModelAndView(int viewIndex, String format, Map<String, Criteria> criteria) {
		return report.buildModelAndView(viewIndex, format, criteria);

	}
}
