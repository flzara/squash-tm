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
package org.squashtest.tm.api.testautomation.execution.dto;

/**
 * Defines execution status values.
 * @author edegenetais
 *
 */
public enum ExecutionStatus {
        /**
         * The execution is currently running.
         */
        RUNNING,
        /**
         * Execution successfully completed.
         */
        SUCCESS,
        /**
         * Execution successful, but teardown failed, which means SUT maintenance
         * might be necessary (and subsequent tests may fail).
         */
        WARNING,
        /**
         * The test completed but the results did not meet functional expectations.
         */
        FAILURE,
        /**
         * The test failed due to a technical error.
         */
        ERROR,
        /**
         * The test is not run because prior test failed.
         */
        NOT_RUN,
        /**
         * The script to run is not found
         */
        NOT_FOUND
}