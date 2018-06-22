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
package org.squashtest.tm.service.importer;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;

public class LogEntry implements Comparable<LogEntry> {
	private Integer line;
	private Target target;
	private ImportMode mode;
	private ImportStatus status;
	private String i18nError;
	private String i18nImpact;

	private Object[] errorArgs;
	private Object[] impactArgs;

	public LogEntry(Target target, ImportStatus status, String i18nError) {
		super();
		this.target = target;
		this.status = status;
		this.i18nError = i18nError;
	}

	/**
	 * @deprecated use builder api
	 */
	@Deprecated
	public LogEntry(Target target, ImportStatus status, String i18nError, Object[] errorArgs) {
		this(target, status, i18nError);
		setErrorArgsPrivately(errorArgs);
	}

	public static final class Builder {
		private final LogEntry product;

		private Builder(ImportStatus status) {
			product = new LogEntry(null, status, null);
		}

		public Builder forTarget(Target tgt) {
			product.target = tgt;
			return this;
		}

		public Builder atLine(int line) {
			product.line = line;
			return this;
		}

		public Builder withMessage(String key, Object... args) {
			product.i18nError = key;
			product.errorArgs = args;
			return this;
		}

		public Builder withImpact(String key, Object... args) {
			product.i18nImpact = key;
			product.impactArgs = args;
			return this;
		}

		public LogEntry build() {
			return product;
		}
	}

	public static Builder failure() {
		return new Builder(ImportStatus.FAILURE);
	}

	public static Builder warning() {
		return new Builder(ImportStatus.WARNING);
	}

	public static Builder ok() {
		return new Builder(ImportStatus.OK);
	}

	/**
	 * Use this when you don't statically know what the status is. Otherwise, consider LogEntry#failure() or LogEntry#warning()
	 *
	 * @param status
	 * @return
	 */
	public static Builder status(@NotNull ImportStatus status) {
		return new Builder(status);
	}



	public Object[] getErrorArgs() {
		return errorArgs;
	}

	private void setErrorArgsPrivately(Object[] errorArgsParam) {
		if (errorArgsParam == null) {
			this.errorArgs = null;
		} else {
			this.errorArgs = Arrays.copyOf(errorArgsParam, errorArgsParam.length);
		}
	}

	public void setErrorArgs(Object... errorArgs) {
		this.errorArgs = errorArgs;
	}

	public Object[] getImpactArgs() {
		return impactArgs;
	}

	public void setImpactArgs(Object... impactArgs) {
		this.impactArgs = impactArgs;
	}

	@Override
	public int compareTo(LogEntry o) {
		if (!line.equals(o.line)) {
			return line - o.line;
		} else if (status != o.getStatus()) {
			return status.getLevel() - o.getStatus().getLevel();
		} else {
			// even when two instances have strictly same content we don't want to consider them equal.
			// note that returning -1 is not an ideal solution because it violates the Comparable contract
			// x.compareTo(y) == - y.compareTo(x) but it's good enough here
			// rem : what does good enough means ? it randomly breaks the "should compare nicely with each others" test, FFS
			/*
			 * Re : meaning of "good enough" :
			 *
			 * According to specs as long as two log entries report things related to a same entity we don't bother in which order they appear.
			 * Thus, formally they compare to each other according to a partial order : for {x,y} € LogEntry the comparison is defined if
			 * they reference different lines or have different status, otherwise the comparison is undecided.
			 * However the constraint x.compareTo(y) == - y.compareTo(x) suggests that Comparable requires an implementation of a total order.
			 * I don't need that so I return an arbitrary value instead, which is good enough.
			 *
			 * Consequently this implementation doesn't break the aforementioned test because it is not required to sort every pair of LogEntry
			 * in a deterministic way. Rather, the test itself is ill-designed.
			 *
			 */
			return -1;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LogEntry logEntry = (LogEntry) o;

		if (!line.equals(logEntry.line)) return false;
		if (status != logEntry.status) return false;
		if (!i18nError.equals(logEntry.i18nError)) return false;
		return Arrays.deepEquals(errorArgs, logEntry.errorArgs);
	}

	// if the LogEntry status is OK, the i18nError and errorArgs are null so the hash return a NPE
	@Override
	public int hashCode() {
		int result = line.hashCode();
		result = 31 * result + status.hashCode();
		result = 31 * result + (i18nError != null ? i18nError.hashCode() : 0);
		result = 31 * result + (errorArgs != null ? Arrays.hashCode(errorArgs) : 0);
		return result;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public ImportMode getMode() {
		return mode;
	}

	public void setMode(ImportMode mode) {
		this.mode = mode;
	}

	public Target getTarget() {
		return target;
	}

	public ImportStatus getStatus() {
		return status;
	}

	public String getI18nError() {
		return i18nError;
	}

	public String getI18nImpact() {
		return i18nImpact;
	}

	public void setTarget(Target target) {
		this.target = target;
	}
}
