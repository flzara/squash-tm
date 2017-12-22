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

import java.io.Serializable;
import java.util.Date;

/**
 * This class encapsulates test suite execution status updates.
 * 
 * @author edegenetais
 * 
 */
public class TestSuiteExecutionStatus implements Serializable {
        /**
         * for serialization.
         */
        private static final long serialVersionUID = -7060255926451384204L;

        /**
         * Name of the test suite.
         */
        private String suiteName;
        /**
         * Start time. Mandatory.
         */
        private Date startTime;
        /**
         * End time. May be null (ex: updates to status RUNNING don't define end
         * time).
         */
        private Date endTime;

        /**
         * The new status of the test suite.
         */
        private ExecutionStatus status;

        /**
         * @return Name of the test suite.
         */
        public String getSuiteName() {
                return suiteName;
        }

        /**
         * @param suiteName
         *            Name of the test suite.
         */
        public void setSuiteName(String suiteName) {
                if(suiteName==null){
                        throw new IllegalArgumentException("test suite name cannot be null.");
                }
                this.suiteName = suiteName;
        }

        /**
         * @return Start time. Should always be defined.
         */
        public Date getStartTime() {
                return startTime;
        }

        /**
         * @param startTime
         *            Start time. Never null.
         */
        public void setStartTime(Date startTime) {
                if (startTime == null) {
                        throw new IllegalArgumentException("Start time cannot be null.");
                }
                this.startTime = startTime;
        }

        /**
         * @return End time. May be null (ex: updates to status RUNNING don't define
         *         end time).
         */
        public Date getEndTime() {
                return endTime;
        }

        /**
         * @param endTime
         *            End time. May be null (ex: updates to status RUNNING don't
         *            define end time).
         */
        public void setEndTime(Date endTime) {
                this.endTime = endTime;
        }

        /**
         * @return The new status of the test suite.
         */
        public ExecutionStatus getStatus() {
                return status;
        }

        /**
         * @param status The new status of the test suite. Cannot be null.
         */
        public void setStatus(ExecutionStatus status) {
                if(status==null){
                        throw new IllegalArgumentException("Status cannot be null.");
                }
                this.status = status;
        }

}