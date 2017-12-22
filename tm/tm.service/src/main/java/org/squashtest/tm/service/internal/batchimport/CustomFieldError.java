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
package org.squashtest.tm.service.internal.batchimport;

import java.util.Map;

import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;

public enum CustomFieldError {
	MAX_SIZE {
		@Override
		public String getErrorMessage() {
			return Messages.ERROR_MAX_SIZE;
		}

		@Override
		public String getCreateImpact() {
			return Messages.IMPACT_MAX_SIZE;
		}

		@Override
		public String getUpdateImpact() {
			return Messages.IMPACT_MAX_SIZE;
		}
	},
	MANDATORY_CUF {
		@Override
		public String getErrorMessage() {
			return Messages.ERROR_MANDATORY_CUF;
		}

		@Override
		public String getCreateImpact() {
			return Messages.IMPACT_DEFAULT_VALUE;
		}

		@Override
		public String getUpdateImpact() {
			return Messages.IMPACT_NO_CHANGE;
		}
	},
	UNPARSABLE_CHECKBOX {
		@Override
		public String getErrorMessage() {
			return Messages.ERROR_UNPARSABLE_CHECKBOX;
		}

		@Override
		public String getCreateImpact() {
			return Messages.IMPACT_DEFAULT_VALUE;
		}

		@Override
		public String getUpdateImpact() {
			return Messages.IMPACT_NO_CHANGE;
		}
	},
	UNPARSABLE_DATE {
		@Override
		public String getErrorMessage() {
			return Messages.ERROR_UNPARSABLE_DATE;
		}

		@Override
		public String getCreateImpact() {
			return Messages.IMPACT_DEFAULT_VALUE;
		}

		@Override
		public String getUpdateImpact() {
			return Messages.IMPACT_NO_CHANGE;
		}
	},
	UNPARSABLE_OPTION {
		@Override
		public String getErrorMessage() {
			return Messages.ERROR_UNPARSABLE_OPTION;
		}

		@Override
		public String getCreateImpact() {
			return Messages.IMPACT_DEFAULT_VALUE;
		}

		@Override
		public String getUpdateImpact() {
			return Messages.IMPACT_NO_CHANGE;
		}
	},
	UNKNOWN_CUF_TYPE {
		@Override
		public String getErrorMessage() {
			return Messages.ERROR_UNKNOWN_CUF_TYPE;
		}

		@Override
		public String getCreateImpact() {
			return Messages.IMPACT_NO_CHANGE;
		}

		@Override
		public String getUpdateImpact() {
			return Messages.IMPACT_NO_CHANGE;
		}
	},
	UNPARSABLE_NUMBER {
		@Override
		public String getErrorMessage() {
			return Messages.ERROR_UNPARSABLE_NUMBER;
		}

		@Override
		public String getCreateImpact() {
			return Messages.IMPACT_DEFAULT_VALUE;
		}

		@Override
		public String getUpdateImpact() {
			return Messages.IMPACT_NO_CHANGE;
		}
	}
	;

	public abstract String getErrorMessage();

	public abstract String getCreateImpact();

	public abstract String getUpdateImpact();

	public static void updateValue(Map<String, String> cufs, CustomField cuf, String value, String impact) {

		switch (impact) {
			case Messages.IMPACT_MAX_SIZE:
				cufs.put(cuf.getCode(), value.substring(0, Math.min(value.length(), CustomFieldValue.MAX_SIZE)));

				break;
			case Messages.IMPACT_DEFAULT_VALUE:
				cufs.put(cuf.getCode(), cuf.getDefaultValue());

				break;
			case Messages.IMPACT_NO_CHANGE:
				cufs.remove(cuf.getCode());

				break;
			default:
				throw new IllegalArgumentException("Unknown CUF validation impact : " + impact);
		}

	}

}
