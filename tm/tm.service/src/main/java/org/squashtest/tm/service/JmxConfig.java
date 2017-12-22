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
package org.squashtest.tm.service;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.jmx.export.MBeanExporter;
import org.squashtest.tm.service.internal.logging.Log4jLoggerLevelModifier;

/**
 * @author Gregory Fouquet
 */
@Configurable
public class JmxConfig {
    @Inject
    private Log4jLoggerLevelModifier log4jLoggerLevelModifier;

    @Bean
    public MBeanExporter mBeanExporter() {
        Map<String, Object> beans = new HashMap<>();
        beans.put("squash.core:name=squash.core.logging.LoggerLevelModifier", log4jLoggerLevelModifier);

        MBeanExporter exporter = new MBeanExporter();
        exporter.setBeans(beans);

        return exporter;
    }
}
