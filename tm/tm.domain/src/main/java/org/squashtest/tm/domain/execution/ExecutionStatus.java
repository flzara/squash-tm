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
package org.squashtest.tm.domain.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.requirement.RequirementStatus;

/**
 *
 * <p>
 * This class declare the 7 execution statuses, 5 of them being canonical.
 *
 * Also, it declares and additional methods to update the new execution status of an execution, based on the former
 * states of the execution, of the step, and the new status of the step. See their documentation for details.
 * </p>
 *
 * <b>Definitions</b> :
 * <ul>
 * <li><u>former execution status</u> : former ExecutionStatus of the considered Execution</li>
 * <li><u>former step status</u> : former ExecutionStatus of the ExecutionStep (that belongs to the considered
 * Execution) that just had been updated</li>
 * <li><u>new step status</u> : new ExecutionStatus of the ExecutionStep that just had been updated. From the object
 * point of view, it's "this" : the public methods here will consider "this" as the new step status.</li>
 * <li><u>new execution status</u> : the new ExecutionStatus of the considered Execution. It is what is being computed
 * there.</li>
 * </ul>
 *
 * Rem : {@link Level} seems to be used for sorting purposes (which might not be so good a thing)
 *
 * @author bsiri
 *
 */

/*
 * Tech documentation :
 *
 * When asking for the new status of an Execution given its former state, and those of the modified step, the procedure
 * goes through multiple stages. It's a rather awkward procedure because the implementation first concern was
 * optimization rather than code clarity. Indeed we need to reduce calls to the database to the minimum because this
 * code may be called numerous times in a short period of time, especially since a recomputation requires to load the
 * complete collection of steps for the considered execution.
 *
 * To achieve the lowest number of db calls, we apply successive tests, from the less information-consuming to the most
 * information-consuming.
 *
 * Computation here is based on the table of truth of : former execution status, former step status, new step status.
 * From all the possible combinations obtained in that table we can deduce the tests mentioned above, and which
 * informations are required to deduce them. When no test can be deduced it usually means that a db call is needed.
 *
 * The tests are grouped in the following stages (in that order) :
 *
 * 1/ trivial deductions : we filter the table of truth with trivial computations wrt the former states of the execution
 * only. If the new status could be deducted it is returned immediately, otherwise we proceed to the next stage.
 *
 * 2/ check if computation is mandatory : another row of quick tests that will tell if a db call is unavoidable. If
 * that's the case the method returns null immediately, otherwise we proceed to the next stage. The calling method is
 * now in charge to call the db.
 *
 * 3/ Status-specific computations : we perform specific tests, like in the first one (trivial computation), this time
 * adding all the informations we have at disposal. If a result was found the method returns it immediately, otherwise
 * we proceed.
 *
 * 4/ Failure : none of the step above succeeded. The result is null, and the calling method must call the DB.
 *
 * See below for more comments about the various tests .
 *
 * Note 1 : impossible states like formerStepStatus = BLOCKED and formerExecStatus = SUCCESS are filtered out (they
 * aren't supposed to happen).
 *
 * Note 2 : see the method computeNewStatus for the simplest statement about what does this thing compute.
 */

/*
 * Feat 1181, 03/08/12 : the list of execution statuses is extended by two statuses designed for automated executions :
 * TA_WARNING and TA_ERROR. There are now 8 status, yet manual or automated statuses only uses a subset of them.
 *
 * The set of valid statuses for manual executions is : BLOCKED, FAILURE, SUCCESS, RUNNING, READY The set of valid
 * statuses for automated executions : ERROR, FAILURE, WARNING, SUCCESS, RUNNING, READY, NOT_RUN, NOT_FOUND
 *
 *
 * The status sets for manual and automated can still be compared to each other, but one have to normalize them first :
 * BLOCKED, FAILURE, SUCCESS, RUNNING, READY is considered as the Canonical status set. To this end, TA_ERROR and
 * TA_NOT_RUN is considered equal to BLOCKED, TA_NOT_FOUND is considered as UNTESTABLE, and TA_WARNING is considered equal to SUCCESS.
 */

/*
 * Feat 3481, 3482, 3483, 30/06/14 :
 *
 *  Two more TA statuses : NOT_RUN(BLOCKED) and NOT_FOUND(UNTESTABLE)
 *
 */
public enum ExecutionStatus implements Internationalizable, Level {

	SETTLED(10) {
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			return needsComputation();
		}

		@Override
		public boolean isCanonical() {
			return true;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return SETTLED;
		}

		@Override
		public boolean canBeDisabled() {
			return true;
		}

		@Override
		public boolean defaultEnabled() {
			return false;
		}
	},

	UNTESTABLE(9) {
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			return needsComputation();
		}

		@Override
		public boolean isCanonical() {
			return true;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return UNTESTABLE;
		}

		@Override
		public boolean canBeDisabled() {
			return true;
		}

		@Override
		public boolean defaultEnabled() {
			return true;
		}
	},

	BLOCKED(6) {
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			return ExecutionStatus.BLOCKED;
		}

		@Override
		public boolean isCanonical() {
			return true;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return BLOCKED;
		}
	},

	FAILURE(5) {
		@Override
		// the case 'former exec status blocked' is already ruled out in the trivialDeductions
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			return ExecutionStatus.FAILURE;
		}

		@Override
		public boolean isCanonical() {
			return true;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return FAILURE;
		}
	},

	SUCCESS(3) {
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			ExecutionStatus newStatus;

			if (formerExecutionStatus == ExecutionStatus.FAILURE) {
				newStatus = ExecutionStatus.FAILURE;
			} else if (formerStepStatus == ExecutionStatus.RUNNING && formerExecutionStatus == ExecutionStatus.READY) {
				newStatus = ExecutionStatus.RUNNING;
			} else {
				newStatus = needsComputation();
			}

			return newStatus;
		}

		@Override
		public boolean isCanonical() {
			return true;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return SUCCESS;
		}
	},

	RUNNING(2) {

		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			ExecutionStatus newStatus;

			if (formerExecutionStatus == ExecutionStatus.FAILURE) {
				newStatus = ExecutionStatus.FAILURE;
			} else if (formerExecutionStatus == ExecutionStatus.READY) {
				newStatus = ExecutionStatus.READY;
			} else if (formerExecutionStatus == ExecutionStatus.RUNNING) {
				newStatus = needsComputation();
			} else {
				newStatus = ExecutionStatus.RUNNING;
			}
			return newStatus;
		}

		@Override
		public boolean isCanonical() {
			return true;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return RUNNING;
		}
	},

	READY(1) {

		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			ExecutionStatus newStatus;

			if (formerExecutionStatus == ExecutionStatus.FAILURE) {
				newStatus = ExecutionStatus.FAILURE;
			} else {
				newStatus = needsComputation();
			}

			return newStatus;
		}

		@Override
		public boolean isCanonical() {
			return true;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return READY;
		}
	},

	WARNING(4) {
		// supposed to never happen because this operation requires canonical statuses and TA_WARNING is not one of
		// them.
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			throw new UnsupportedOperationException(
					"ExecutionStatus.TA_WARNING#resolveStatus(...) should never have been invoked. That exception cleary results from faulty logic. If you read this message please "
							+ "report the issue at https://ci.squashtest.org/mantis/ Please put [ExecutionStatus - unsupported operation] as title for your report and explain what you did. Also please check that it hadn't been reported "
							+ "already. Thanks for your help and happy Squash !");
		}

		@Override
		public boolean isCanonical() {
			return false;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return SUCCESS;
		}

		@Override
		public boolean defaultEnabled() {
			return false;
		}
	},

	ERROR(7) {
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			throw new UnsupportedOperationException(
					"ExecutionStatus.TA_ERROR#resolveStatus(...) " + LOG_MSG_RESOLVESTATUS_ERROR);
		}

		@Override
		public boolean isCanonical() {
			return false;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return BLOCKED;
		}

		@Override
		public boolean defaultEnabled() {
			return false;
		}
	},

	NOT_RUN(8) {
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			throw new UnsupportedOperationException(
					"ExecutionStatus.TA_NOT_RUN#resolveStatus(...) " + LOG_MSG_RESOLVESTATUS_ERROR);
		}

		@Override
		public boolean isCanonical() {
			return false;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return BLOCKED;
		}

		@Override
		public boolean defaultEnabled() {
			return false;
		}
	},
	NOT_FOUND(9) {
		@Override
		protected ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
			throw new UnsupportedOperationException(
					"ExecutionStatus.TA_NOT_FOUND#resolveStatus(...) " + LOG_MSG_RESOLVESTATUS_ERROR);
		}

		@Override
		public boolean isCanonical() {
			return false;
		}

		@Override
		public ExecutionStatus getCanonicalStatus() {
			return UNTESTABLE;
		}

		@Override
		public boolean defaultEnabled() {
			return false;
		}
	};

	/* ************************* attributes ********************************** */

	private static final String LOG_MSG_RESOLVESTATUS_ERROR = "should never have been invoked. That exception cleary results from faulty logic. If you read this message please "
								+ "report the issue at https://ci.squashtest.org/mantis/ Please put [ExecutionStatus - unsupported operation] as title for your report and explain what you did. Also please check that it hadn't been reported "
								+ "already. Thanks for your help and happy Squash !";

	private static final String I18N_KEY_ROOT = "execution.execution-status.";

	private static final Set<ExecutionStatus> CANONICAL_STATUSES;
	// tests with terminal statuses are considered executed (used for setting iteration/campaign end dates
	// automatically)
	public static final Set<ExecutionStatus> TERMINAL_STATUSES;
	// tests with non terminal statuses are considered not executed (used for setting iteration/campaign end dates
	// automatically)
	private static final Set<ExecutionStatus> NON_TERMINAL_STATUSES;

	// those status are available to Squash TA Only, they will never be settable by a human user.
	public static final Set<ExecutionStatus> TA_STATUSES_ONLY;

	// status that aren't enabled by default for a project, but could be if the manager decided so.
	public static final Set<ExecutionStatus> DEFAULT_DISABLED_STATUSES;

	// status that are assimilated to a success. as 1.17 only SUCCESS and SETTLED
	public static final Set<ExecutionStatus> SUCCESS_STATUSES;

	private final int level;

	static {

		Set<ExecutionStatus> set = new HashSet<>(6);
		set.add(BLOCKED);
		set.add(FAILURE);
		set.add(SUCCESS);
		set.add(RUNNING);
		set.add(READY);
		set.add(UNTESTABLE);

		CANONICAL_STATUSES = Collections.unmodifiableSet(set);

		Set<ExecutionStatus> terms = new HashSet<>(9);
		terms.add(BLOCKED);
		terms.add(FAILURE);
		terms.add(SUCCESS);
		terms.add(WARNING);
		terms.add(NOT_RUN);
		terms.add(NOT_FOUND);
		terms.add(ERROR);
		terms.add(UNTESTABLE);
		terms.add(SETTLED);

		TERMINAL_STATUSES = Collections.unmodifiableSet(terms);

		Set<ExecutionStatus> nonTerms = new HashSet<>(2);
		nonTerms.add(RUNNING);
		nonTerms.add(READY);

		NON_TERMINAL_STATUSES = Collections.unmodifiableSet(nonTerms);


		Set<ExecutionStatus> disabled = new HashSet<>(1);
		disabled.add(SETTLED);

		DEFAULT_DISABLED_STATUSES = Collections.unmodifiableSet(disabled);

		Set<ExecutionStatus> TAStatuses = new HashSet<>(4);
		TAStatuses.add(ERROR);
		TAStatuses.add(WARNING);
		TAStatuses.add(NOT_FOUND);
		TAStatuses.add(NOT_RUN);

		TA_STATUSES_ONLY = Collections.unmodifiableSet(TAStatuses);

		Set<ExecutionStatus> success = new HashSet<>(1);
		success.add(SETTLED);
		success.add(SUCCESS);

		SUCCESS_STATUSES = success;
	}

	private ExecutionStatus(int level) {
		this.level = level;
	}

	public static Set<ExecutionStatus> getSuccessStatusSet() {
		return SUCCESS_STATUSES;
	}

	@Deprecated // use DEFAULT_DISABLED_STATUSES instead
	public boolean defaultEnabled() {
		return true;
	}

	public boolean canBeDisabled() {
		return false;
	}

	protected ExecutionStatus isAmbiguous() {
		return null;
	}

	protected boolean isAmbiguous(ExecutionStatus status) {
		return status == null;
	}

	protected ExecutionStatus needsComputation() {
		return null;
	}

	protected boolean needsComputation(ExecutionStatus status) {
		return status == null;
	}

	/* *************************** abstract methods ********************************* */

	protected abstract ExecutionStatus resolveStatus(ExecutionStatus formerExecutionStatus,
			ExecutionStatus formerStepStatus);

	public abstract boolean isCanonical();

	public abstract ExecutionStatus getCanonicalStatus();

	/* **************************** static methods ***************************** */

	public static List<ExecutionStatus> toCanonicalStatusList(List<ExecutionStatus> nonCanonical) {
		List<ExecutionStatus> canonical = new ArrayList<>();
		for (ExecutionStatus nStatus : nonCanonical) {
			canonical.add(nStatus.getCanonicalStatus());
		}
		return canonical;
	}

	public static Set<ExecutionStatus> getCanonicalStatusSet() {
		return CANONICAL_STATUSES;
	}

	public static Set<ExecutionStatus> getTerminatedStatusSet() {
		return TERMINAL_STATUSES;
	}

	public static Set<ExecutionStatus> getNonTerminatedStatusSet() {
		return NON_TERMINAL_STATUSES;
	}

	/* **************************** public instance methods ***************************** */

	@Override
	public int getLevel() {
		return level;
	}

	/***
	 * This methods checks if the status is RUNNING or READY
	 *
	 * @return true if the status is neither RUNNING nor READY
	 */
	public boolean isTerminatedStatus() {
		return TERMINAL_STATUSES.contains(this.getCanonicalStatus());
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}

	/**
	 * will deduce the new status of an execution based on the former execution status and former step status. "this" is
	 * here the new step status. In some case the deduction is impossible and a further computation will be necessary.
	 *
	 * The method will first convert the argument to their canonical form before performing the comparison.
	 *
	 * @param formerExecutionStatus
	 *            : the former execution status
	 * @param formerStepStatus
	 *            : the former step status
	 * @return : the new execution status when possible, or null if it wasn't. The later usually means that a call to
	 *         the database is needed.
	 */
	public ExecutionStatus deduceNewStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
		return this.getCanonicalStatus().doDeduceNewStatus(formerExecutionStatus.getCanonicalStatus(),
				formerStepStatus.getCanonicalStatus());
	}

	/* *************************** class-private instance method (assumes canonical form) ******************** */

	protected boolean isNoneOf(ExecutionStatus... status) {
		for (ExecutionStatus state : status) {
			if (this == state) {
				return false;
			}
		}
		return true;
	}

	protected boolean isOneOf(ExecutionStatus... status) {
		return !isNoneOf(status);
	}

	protected ExecutionStatus doDeduceNewStatus(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {

		ExecutionStatus newStatus;

		// first pass : trivial deductions
		ExecutionStatus deductedStatus = trivialDeductions(formerExecutionStatus, formerStepStatus);

		if (!isAmbiguous(deductedStatus)) {
			newStatus = deductedStatus;
		}

		else {
			// second pass : trivial computations
			boolean needComputation = trivialNeedComputation(formerExecutionStatus, formerStepStatus);
			if (needComputation) {
				newStatus = null;
			}

			// third pass : we need further resolution
			else {
				newStatus = resolveStatus(formerExecutionStatus, formerStepStatus);
			}

		}

		return newStatus;
	}

	/**
	 * will compute from scratch a status using a complete report.
	 *
	 * @param report
	 *            : ExecutionStatusReport.
	 * @return : ExecutionStatus.
	 */
	public static ExecutionStatus computeNewStatus(ExecutionStatusReport report) {

		ExecutionStatus newStatus = ExecutionStatus.READY;

		if (report.has(BLOCKED)) {
			newStatus = BLOCKED;

		} else if (report.allOf(UNTESTABLE)) {
			newStatus = UNTESTABLE;

		} else if (report.allOf(SETTLED, UNTESTABLE)) {
			newStatus = SETTLED;
		} else if (report.has(FAILURE)) {
			newStatus = FAILURE;

		} else if (report.allOf(SUCCESS,  UNTESTABLE, SETTLED)) {
			newStatus = SUCCESS;

		} else if (report.anyOf(SUCCESS,  SETTLED)) {
			newStatus = RUNNING;

		}

		return newStatus;
	}

	/* ************************************ tests ******************************************* */

	// trivial tests who do not depend on the actual value of "this" (namely, the new Step status)
	protected ExecutionStatus trivialDeductions(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
		ExecutionStatus newStatus;

		// step status unchanged : no change
		if (!hasChanged(formerStepStatus)) {
			newStatus = formerExecutionStatus;
		}
		// new step status = former step status : no change. Running is an exception : the Execution might be set to
		// ready.
		else if (isSetToExecutionStatus(formerExecutionStatus)) {
			newStatus = formerExecutionStatus;
		}

		// if the former execution status was blocked and the former step status wasn't blocked, the execution will stay
		// blocked
		else if (wontUnlockBloquedExecution(formerExecutionStatus, formerStepStatus)) {
			newStatus = ExecutionStatus.BLOCKED;
		} else {
			newStatus = isAmbiguous();
		}

		return newStatus;

	}

	// in certain cases the computation is mandatory no matter what. This method will check that.
	protected boolean trivialNeedComputation(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
		boolean isMandatory = false;

		// if the former Step status was blocked and is now changing then computation is mandatory
		if (mayUnlockBloquedExecution(formerStepStatus)) {
			isMandatory = true;
		}

		// here we test if the former step status is the former execution status : this step was maybe the only one
		// responsible for the former exec status (eg, the only one blocked).
		// we then now the new exec status must then be recomputed.
		else if (couldHaveSetExecStatusAlone(formerExecutionStatus, formerStepStatus)) {
			isMandatory = true;
		}

		// UNTESTABLE is a neutral status, the new execution status depends on every others status but not this one.
		else if (this == UNTESTABLE) {
			isMandatory = true;
		}

		return isMandatory;
	}

	/* *************************** Micro tests ************************************** */

	protected boolean hasChanged(ExecutionStatus formerStepStatus) {
		return this != formerStepStatus;
	}

	// new step status = former step status : no change. new Step Status Running is an exception : the Execution might
	// be set to ready later.
	protected boolean isSetToExecutionStatus(ExecutionStatus formerExecutionStatus) {
		if (this == ExecutionStatus.RUNNING) {
			return false;
		}
		return this == formerExecutionStatus;
	}

	protected boolean wontUnlockBloquedExecution(ExecutionStatus formerExecutionStatus, ExecutionStatus formerStepStatus) {
		return formerExecutionStatus == ExecutionStatus.BLOCKED && formerStepStatus != ExecutionStatus.BLOCKED;
	}

	protected boolean mayUnlockBloquedExecution(ExecutionStatus formerStepStatus) {
		return this != ExecutionStatus.BLOCKED && formerStepStatus == ExecutionStatus.BLOCKED;
	}

	// here we test if the former step status is the former execution status : this step was maybe the only one
	// responsible for the
	// former exec status (eg, the only one blocked).
	// we then now the new exec status must then be recomputed.
	protected boolean couldHaveSetExecStatusAlone(ExecutionStatus formerExecutionStatus,
			ExecutionStatus formerStepStatus) {
		return formerExecutionStatus == formerStepStatus;
	}

	// ---------------------------------- Comparator -------------------------------------

	public static StringComparator stringComparator() {
		return new StringComparator();
	}

	/**
	 * inner class used to sort RequirementStatus over their string representation. In case we have to sort stringified
	 * statuses with other arbitrary strings, stringified statuses will have a lower rank than other strings.
	 */
	private static class StringComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			RequirementStatus status1, status2;

			try {
				String comparableString1 = removeDisableString(o1);
				status1 = RequirementStatus.valueOf(comparableString1);
			} catch (IllegalArgumentException iae) {
				return 1;
			}

			try {
				String comparableString2 = removeDisableString(o2);
				status2 = RequirementStatus.valueOf(comparableString2);
			} catch (IllegalArgumentException iae) {
				return -1;
			}

			return status1.compareTo(status2);
		}

		private String removeDisableString(String o) {
			String newString = o;
			String disabled = "disabled.";
			if (o.startsWith(disabled)) {
				newString = o.substring(disabled.length());
			}
			return newString;
		}
	}


}
