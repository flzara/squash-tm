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

import static org.squashtest.tm.service.importer.EntityType.COVERAGE;
import static org.squashtest.tm.service.importer.EntityType.DATASET;
import static org.squashtest.tm.service.importer.EntityType.PARAMETER;
import static org.squashtest.tm.service.importer.EntityType.REQUIREMENT_VERSION;
import static org.squashtest.tm.service.importer.EntityType.REQUIREMENT_LINK;
import static org.squashtest.tm.service.importer.EntityType.TEST_CASE;
import static org.squashtest.tm.service.importer.EntityType.TEST_STEP;
import static org.squashtest.tm.service.importer.ImportStatus.FAILURE;
import static org.squashtest.tm.service.importer.ImportStatus.OK;
import static org.squashtest.tm.service.importer.ImportStatus.WARNING;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeSet;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.service.internal.batchimport.LogTrain;

public class ImportLog{

	private static final Logger LOGGER = LoggerFactory.getLogger(ImportLog.class);

	// key : EntityType, values : LogEntry
	@SuppressWarnings("rawtypes")
	private MultiValueMap logEntriesPerType = MultiValueMap.decorate(new HashMap(), TreeSet.class); // NOSONAR actual collection type required

	private int testCaseSuccesses = 0;
	private int testCaseWarnings = 0;
	private int testCaseFailures = 0;

	private int testStepSuccesses = 0;
	private int testStepWarnings = 0;
	private int testStepFailures = 0;

	private int parameterSuccesses = 0;
	private int parameterWarnings = 0;
	private int parameterFailures = 0;

	private int datasetSuccesses = 0;
	private int datasetWarnings = 0;
	private int datasetFailures = 0;

	private int requirementVersionSuccesses = 0;
	private int requirementVersionWarnings = 0;
	private int requirementVersionFailures = 0;

	private int coverageSuccesses = 0;
	private int coverageWarnings = 0;
	private int coverageFailures = 0;
	
	private int reqlinksSuccesses = 0;
	private int reqlinksWarnings = 0;
	private int reqlinksFailures = 0;


	private String reportUrl;

	public void addLogEntry(LogEntry logEntry) {
		logEntriesPerType.put(logEntry.getTarget().getType(), logEntry);
	}

	public void appendLogTrain(LogTrain train) {
		for (LogEntry entry : train.getEntries()) {
			logEntriesPerType.put(entry.getTarget().getType(), entry);
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<LogEntry> findAllFor(EntityType type) {
		Collection<LogEntry> entries = logEntriesPerType.getCollection(type);
		if (entries != null) {
			return entries;
		} else {
			return Collections.emptyList();
		}
	}

	public boolean isEmpty(){
		return logEntriesPerType.isEmpty();
	}


	/**
	 *
	 * <p>The logs for the datasets also contain the logs for the dataset parameter values.
	 * Since they were inserted separately we need to purge them from redundant informations.</p>
	 *
	 * <p>To ensure consistency we need to check that, for each imported line, there can be
	 *   a log entry with status OK if this is the unique log entry for that line.
	 *   From a procedural point of view we need, for each imported lines, to remove a log entry
	 *   if it has a status OK and :
	 * <ul>
	 * 	<li>there was already a status OK for that line, or</li>
	 * 	<li>there is at least 1 warning or error</li>
	 * </ul>
	 * </p>
	 *
	 */

	/*
	 * NB : This code relies on the fact that the log entries are sorted by import line number then by status,
	 * and that the status OK comes first.
	 *
	 * Basically the job boils down to the following rules :
	 *
	 * for each line, for each entry, if there was a previous element with status OK on this line -> remove it.
	 *
	 */
	public void packLogs(){

		LinkedList<LogEntry> listiterableLogs = new LinkedList<>(findAllFor(DATASET));

		Integer precLine = null;
		boolean okFoundOnPrecEntry = false;

		ListIterator<LogEntry> iter =listiterableLogs.listIterator();

		while (iter.hasNext()){

			LogEntry entry = iter.next();
			Integer curLine = entry.getLine();
			ImportStatus curStatus = entry.getStatus();

			/*
			 * if we found an occurence on the previous entry
			 * and the current entry concerns the same line and
			 * remove it.
			 */
			if (okFoundOnPrecEntry && curLine.equals(precLine)){

				// finding the previous element actually means
				// to backtrack twice (because the cursor points
				// to the next element already)

				iter.previous();
				iter.previous();

				iter.remove();

				// now we replace the cursor where it was before
				// the 'if'.
				iter.next();

			}

			// now we set our flag according to the status of the
			// current entry and update precLine
			okFoundOnPrecEntry = curStatus == OK;
			precLine = curLine;
		}

		// once complete we replace the original list with the filtered one
		findAllFor(DATASET).clear();
		logEntriesPerType.putAll(DATASET, listiterableLogs);


	}


	public void recompute() {
		LOGGER.debug("ReqImport - Compute requirement import results");
		recomputeFor(TEST_CASE);
		recomputeFor(TEST_STEP);
		recomputeFor(PARAMETER);
		recomputeFor(DATASET);
		recomputeFor(REQUIREMENT_VERSION);
		recomputeFor(COVERAGE);
		recomputeFor(REQUIREMENT_LINK); 
	}


	/*
	 * This method will compute, for one type of data, how
	 * many lines in the imported excel workbook were treated successfully,
	 * partially or not at all.
	 *
	 * Each line can be the object of one or many log entry, for each of those
	 * lines we need to know whether the entries that reference them have errors,
	 * warning or just report a success.
	 *
	 * The entries are returned sorted by line number (thanks to the choice of a
	 * TreeSet as the collection). All we have to do is to iterate over the
	 * elements, record whenever a status 'warning' or 'failure' is encountered, then
	 * when a new line is being treated we just report what statuses were found
	 * and reset the counters.
	 *
	 */
	private void recomputeFor(EntityType type){

		Collection<LogEntry> entries = findAllFor(type);

		if (! entries.isEmpty()){

			boolean errors;
			boolean warnings;

			Iterator<LogEntry> iter = entries.iterator();
			Integer curLine;

			LogEntry entry = iter.next();
			Integer precLine = entry.getLine();	// we move the iterator forward purposedly

			errors = entry.getStatus() == FAILURE;
			warnings = entry.getStatus() == WARNING;

			while(iter.hasNext()){
				entry = iter.next();
				curLine = entry.getLine();

				if (! curLine.equals(precLine)){

					countForEntity(type, errors, warnings);

					// reset
					errors = entry.getStatus() == FAILURE;
					warnings = entry.getStatus() == WARNING;
				}
				else{
					errors = entry.getStatus() == FAILURE || errors;
					warnings = entry.getStatus() == WARNING || warnings;
				}

				precLine = curLine;
			}

			countForEntity(type, errors, warnings);

		}

	}

	private void countForEntity(EntityType type, boolean errors, boolean warnings) {
		switch(type){
		case TEST_CASE :
			countTestcase(errors, warnings);
			break;
		case TEST_STEP :
			countStep(errors, warnings);
			break;
		case PARAMETER :
			countParameter(errors, warnings);
			break;
		case DATASET :
			countDataset(errors, warnings);
			break;
		case REQUIREMENT_VERSION :
			countRequirementVersion(errors, warnings);
			break;

		case COVERAGE:
			countCoverage(errors, warnings);
			break;
			
		case REQUIREMENT_LINK :
			countLinks(errors, warnings);
			break;
			
		case NONE :
			break;

		default:
			throw new IllegalStateException(String.format("Entity type %s not yet implemented", type));
		}
	}
	
	private void countLinks(boolean errors, boolean warnings){
		if (errors) {
			reqlinksFailures++;
		} else if (warnings) {
			reqlinksWarnings++;
		} else {
			reqlinksSuccesses++;
		}
	}

	private void countCoverage(boolean errors, boolean warnings) {
		if (errors) {
			coverageFailures++;
		} else if (warnings) {
			coverageWarnings++;
		} else {
			coverageSuccesses++;
		}
	}

	private void countTestcase(boolean errors, boolean warnings){
		if (errors){
			testCaseFailures++;
		}
		else if (warnings){
			testCaseWarnings ++;
		}
		else{
			testCaseSuccesses++;
		}
	}


	private void countStep(boolean errors, boolean warnings){
		if (errors){
			testStepFailures++;
		}
		else if (warnings){
			testStepWarnings ++;
		}
		else{
			testStepSuccesses++;
		}
	}

	private void countParameter(boolean errors, boolean warnings){
		if (errors){
			parameterFailures++;
		}
		else if (warnings){
			parameterWarnings ++;
		}
		else{
			parameterSuccesses++;
		}
	}


	private void countDataset(boolean errors, boolean warnings){
		if (errors){
			datasetFailures++;
		}
		else if (warnings){
			datasetWarnings ++;
		}
		else{
			datasetSuccesses++;
		}
	}

	private void countRequirementVersion(boolean errors, boolean warnings){
		LOGGER.debug("ReqImport Compute requirements");
		if (errors){
			requirementVersionFailures++;
		}
		else if (warnings){
			requirementVersionWarnings ++;
		}
		else{
			requirementVersionSuccesses++;
		}
	}

	public int getTestCaseSuccesses() {
		return testCaseSuccesses;
	}

	public int getTestCaseWarnings() {
		return testCaseWarnings;
	}

	public int getTestCaseFailures() {
		return testCaseFailures;
	}

	public int getTestStepSuccesses() {
		return testStepSuccesses;
	}

	public int getTestStepWarnings() {
		return testStepWarnings;
	}

	public int getTestStepFailures() {
		return testStepFailures;
	}

	public int getParameterSuccesses() {
		return parameterSuccesses;
	}

	public int getParameterWarnings() {
		return parameterWarnings;
	}

	public int getParameterFailures() {
		return parameterFailures;
	}

	public int getDatasetSuccesses() {
		return datasetSuccesses;
	}

	public int getDatasetWarnings() {
		return datasetWarnings;
	}

	public int getDatasetFailures() {
		return datasetFailures;
	}

	public int getRequirementVersionSuccesses() {
		return requirementVersionSuccesses;
	}

	public int getRequirementVersionWarnings() {
		return requirementVersionWarnings;
	}

	public int getRequirementVersionFailures() {
		return requirementVersionFailures;
	}

	public String getReportUrl() {
		return reportUrl;
	}

	public void setReportUrl(String reportURL) {
		this.reportUrl = reportURL;
	}

	public String getStatus(){
		return "ok";
	}

	public int getCoverageSuccesses() {
		return coverageSuccesses;
	}

	public int getCoverageWarnings() {
		return coverageWarnings;
	}

	public int getCoverageFailures() {
		return coverageFailures;
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	public int getReqlinksSuccesses() {
		return reqlinksSuccesses;
	}

	public int getReqlinksWarnings() {
		return reqlinksWarnings;
	}

	public int getReqlinksFailures() {
		return reqlinksFailures;
	}

	
	
}
