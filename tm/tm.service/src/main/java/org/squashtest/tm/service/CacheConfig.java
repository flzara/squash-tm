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

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * JSR 107 (JCache)-based cache configuration, the provider being ehcache 3 (org.ehache, not net.sf.ehcache).
 * The configuration is in resources/ehcache.xml.
 *
 * The cache manager configured here allows for creating caches if one day we need to use Spring @org.springframework.cache.Cacheable,
 * or configure a Hibernate second-level cache.
 *
 * Also used in SecurityConfig for the AclCache
 *
 */
@Configuration
// forcing highest precedence, because caches don't depend on any other things
// on the other hand some other beans might need it
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
public class CacheConfig {

}
