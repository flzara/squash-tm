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
package org.squashtest.tm.service.internal.logging;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.stereotype.Component;

/**
 * @author http://stackoverflow.com/questions/17186195/how-to-set-loglevel-from-info-to-error-using-log4j2-api-at-runtime
 * 
 */
@Component
public class Log4jLoggerLevelModifier {
	public void setLogLevel(String loggerName, String level) {
            
                LoggerContext ctxt = (LoggerContext)LogManager.getContext(false);
                Configuration conf = ctxt.getConfiguration();
                LoggerConfig logConf = conf.getLoggerConfig(loggerName);
            
		if ("trace".equalsIgnoreCase(level)) {
			logConf.setLevel(Level.TRACE);
		} else if ("debug".equalsIgnoreCase(level)) {
			logConf.setLevel(Level.DEBUG);
		} else if ("info".equalsIgnoreCase(level)) {
			logConf.setLevel(Level.INFO);
		} else if ("error".equalsIgnoreCase(level)) {
			logConf.setLevel(Level.ERROR);
		} else if ("fatal".equalsIgnoreCase(level)) {
			logConf.setLevel(Level.FATAL);
		} else if ("warn".equalsIgnoreCase(level)) {
			logConf.setLevel(Level.WARN);
		}
                
                ctxt.updateLoggers(conf);
	}
        
  

	public void setTraceLevel(String logger) {
		setLogLevel(logger, "trace");
	}

	public void setDebugLevel(String logger) {
		setLogLevel(logger, "debug");
	}

	public void setInfoLevel(String logger) {
		setLogLevel(logger, "info");
	}

	public void setWarnLevel(String logger) {
		setLogLevel(logger, "warn");
	}

	public void setErrorLevel(String logger) {
		setLogLevel(logger, "error");
	}
}
