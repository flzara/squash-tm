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
package org.squashtest.tm.web.internal.context;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.squashtest.tm.api.config.SquashPathProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This specialization of {@link ReloadableResourceBundleMessageSource} registers <strong>message.properties</strong>
 * files from fragments looking up into standardized folders "/WEB-INF/messages/<wizard-name>/"
 *
 * @author Gregory Fouquet
 */
public class ReloadableSquashTmMessageSource extends ReloadableResourceBundleMessageSource implements
	ResourceLoaderAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReloadableSquashTmMessageSource.class);
	/**
	 * Resource pattern to scan for messages embedded into plugins / fragments
	 */
	private static final String PLUGIN_MESSAGES_SCAN_PATTERN = "WEB-INF/messages/**";
	/**
	 * Base path for looked up message.properties files
	 */
	private static final String MESSAGES_BASE_PATH = "/WEB-INF/messages/";
	private ResourcePatternResolver resourcePatternResolver;
	private String[] basenames;
	private SquashPathProperties squashPathProperties;

	/**
	 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource#setResourceLoader(org.springframework.core.io.ResourceLoader)
	 */
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		super.setResourceLoader(resourceLoader);
		if (resourceLoader instanceof ResourcePatternResolver) {
			this.resourcePatternResolver = (ResourcePatternResolver) resourceLoader;
		} else {
			this.resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
		}
	}

	/**
	 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource#setBasenames(java.lang.String[])
	 */
	@Override
	public void setBasenames(String... basenames) {
		this.basenames = basenames;
		super.setBasenames(basenames);
	}

	@PostConstruct
	public void registerFragmentMessageProperties() {
		try {
			Set<String> consolidatedBasenames = new LinkedHashSet<>();

			LOGGER.debug("About to scan for external language pack basenames to build MessageSource");
			addExternalBasenames(consolidatedBasenames);

			LOGGER.debug("About to register configured basenames to build MessageSource");
			addConfiguredBasenames(consolidatedBasenames);

			// in runtime environment, directories are not resolved using path "WEB-INF/messages/*", hence the catch-all
			// pattern and then filter on directories
			LOGGER.debug("About to scan {} for additional fragment / plugin basenames", PLUGIN_MESSAGES_SCAN_PATTERN);
			addLookedUpBasenames(consolidatedBasenames);

			LOGGER.debug("About to scan classpath for plugin language packs to build MessageSource");
			addPluginBasenames(consolidatedBasenames);

			super.setBasenames(consolidatedBasenames.toArray(new String[consolidatedBasenames.size()]));
		} catch (IOException e) {
			LOGGER.warn("Error during message source initialization, some messages may not be properly translated.", e);
		}
	}

	private void addPluginBasenames(Set<String> consolidatedBasenames) throws IOException {
		Resource[] resources = resourcePatternResolver.getResources("classpath*:org/squashtest/tm/plugin/**/messages.properties");

		for (Resource resource : resources) {
			try {
				if (resource.exists()) {
					// resource path is external-path/jar-name.jar!/internal-path/messages.properties
					String path = resource.getURL().getPath();
					int bang = path.lastIndexOf('!');
					String basename = "classpath:" + StringUtils.removeEnd(path.substring(bang + 2), ".properties");
					consolidatedBasenames.add(basename);

					LOGGER.info("Registering *discovered* plugin classpath path {} as a basename for application MessageSource", basename);
				}

			} catch (IOException e) {
				LOGGER.info("An IO error occurred while looking up plugin language resources '{}': {}", resource, e.getMessage());
				LOGGER.debug("Plugin language resources lookup error for resource {}", resource, e);
			}
		}
	}

	private void addExternalBasenames(Set<String> consolidatedBasenames) {
		String locationPattern = squashPathProperties.getLanguagesPath() + "/**/messages.properties";
		if (!locationPattern.startsWith("file:")) {
			locationPattern = "file:" + locationPattern;
		}

		try {
			Resource[] resources = resourcePatternResolver.getResources(locationPattern);

			for (Resource resource : resources) {
				if (resource.exists()) {
					String basename = StringUtils.removeEnd(resource.getURL().getPath(), ".properties");
					consolidatedBasenames.add(basename);

					LOGGER.info("Registering *discovered* external path {} as a basename for application MessageSource", basename);
				}

			}
		} catch (IOException e) {
			LOGGER.info("An IO error occurred while looking up external language resources '{}' : {}", locationPattern, e.getMessage());
			LOGGER.debug("External language lookup error. Current path : {}", new File(".").toString(), e);
		}
	}

	private void addLookedUpBasenames(Set<String> consolidatedBasenames) {
		try {
			Resource[] resources = resourcePatternResolver.getResources(PLUGIN_MESSAGES_SCAN_PATTERN);

			for (Resource resource : resources) {
				if (isFirstLevelDirectory(resource)) {
					String basename = MESSAGES_BASE_PATH + resource.getFilename() + "/messages";
					consolidatedBasenames.add(basename);

					LOGGER.info("Registering *discovered* path {} as a basename for application MessageSource", basename);

				}

			}
		} catch (IOException e) {
			LOGGER.info("Error during bean initialization, no fragment messages will be registered. Maybe there are no fragments.", e);
		}
	}

	private void addConfiguredBasenames(Set<String> consolidatedBasenames) {
		if (basenames != null) {
			for (String basename : basenames) {
				consolidatedBasenames.add(basename);
				LOGGER.info("Registering *configured* path {} as a basename for application MessageSource", basename);
			}
		}
	}

	private boolean isFirstLevelDirectory(Resource resource) throws IOException {
		if (!resource.exists()) {
			return false;
		}

		// in runtime env we do not work with file (exception) so we have to use URLs
		URL url = resource.getURL();
		return url.getPath().endsWith(MESSAGES_BASE_PATH + resource.getFilename() + '/');
	}

	public void setSquashPathProperties(SquashPathProperties squashPathProperties) {
		this.squashPathProperties = squashPathProperties;
	}
}
