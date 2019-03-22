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
package org.squashtest.tm.core.foundation.lang;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * Start/suspend/resume/stop StopWatch, which can do so for multiple tasks. You can do so for multiple tasks. A task is referenced by name. It must
 * be declared before use, or expect NPE when using it (it's meant to fail early, and this class is not for production use)(yet). Time unit is milliseconds.
 * <ul>
 *     <li>start("task") : starts a stopwatch for task "task"</li>
 *     <li>suspend("task"): suspends the stopwatch of task "task"</li>
 *     <li>same for resume and stop</li>
 * </ul>
 *
 *</p>
 *
 * <p>
 *     Also, not thread safe.
 * </p>
 */
public class StopWatch {

	private String name = "";

	private Map<String, org.apache.commons.lang3.time.StopWatch> watchmap = new LinkedHashMap<>();


	public StopWatch(){

	}

	public StopWatch(String name){
		this.name = name;
	}

	public void addTask(String taskName){
		watchmap.put(taskName, new org.apache.commons.lang3.time.StopWatch());
	}


	public void start(String taskName){
		watchmap.get(taskName).start();
	}

	public void suspend(String taskName){
		watchmap.get(taskName).suspend();
	}

	public void resume(String taskName){
		watchmap.get(taskName).resume();
	}

	public void stop(String taskName){
		watchmap.get(taskName).stop();
	}



	public void stopAll(){
		for (org.apache.commons.lang3.time.StopWatch sw : watchmap.values()){
			if (sw.isStarted() || sw.isSuspended()) {
				sw.stop();
			}
		}
	}


	/**
	 * Returns the watches formatted as the Spring stopwatches pretty print
	 *
	 * @return
	 */
	public String toString(){
		stopAll();

		long allTime = computeAllTimes();

		StringBuilder builder = new StringBuilder();
		builder.append("StopWatch '"+name+"': running time (millis) = "+allTime+"\n");
		builder.append("-----------------------------------------\n");
		builder.append("ms     %     Task name\n");
		builder.append("-----------------------------------------\n");

		if (allTime > 0){
			watchmap.entrySet().forEach(entry  -> {
				org.apache.commons.lang3.time.StopWatch sw = entry.getValue();
				long swtime = sw.getTime();
				long percent = Math.round(((double)swtime / (double)allTime)*100);
				builder.append(String.format("%05d  %03d%%  %s\n", swtime, percent, entry.getKey()));
			});

		}

		return builder.toString();

	}

	private long computeAllTimes(){
		Optional<Long> times =  watchmap.values().stream().map(sw -> sw.getTime()).reduce((t1, t2) -> t1 + t2);
		if (times.isPresent()){
			return times.get();
		}
		else{
			return 0;
		}
	}

}


