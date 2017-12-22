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
package org.squashtest.tm;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * This SpringApplicationRunListener lists all the jars found in the plugins folder (as defined by
 * ${squash.path.plugins-path}) and adds them to the ApplicationContext's classpath to that they are scanned / started.
 *
 *
 * [JTH 2017-06-07] Known issue on this class :
 *
 * You can't use generics inheritance reliably in plugins because :
 *
 *  - During spring boot load, very early in boot process, Spring boot create a new class loader witch will be his main class loader
 *  - Spring AOP seems to keep a reference to this class loader via a Thread.currentThread().getContextClassLoader() during it's initialization
 *  - This class override the Spring class loader in Spring context, by defining a new class loader, son of the previous one
 *  - So the Class loader referenced in Spring AOP is the father of the actual class loader of Spring context and NOT the actual class loader of Spring context.
 *  - This seems to lead to a crash because Spring AOP don't classify correctly generic types.
 *
 *  Maybe i missed something, but it seems that we cannot solve that issue without writing our own SpringBoot launcher or waiting for a correction from Spring team
 *
 *  - This launcher should add the plugin directory to the list of directory to scan for the class loader.
 * 	- The classloader will be created with all the standard directories AND the plugin directory
 * 	- Spring AOP will pick this classloader
 * 	- We don't ever need a PluginsPathClasspathExtender class
 *
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
public class PluginsPathClasspathExtender implements SpringApplicationRunListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginsPathClasspathExtender.class);


	public PluginsPathClasspathExtender(SpringApplication app, String[] args) {
		super();
	}

	@Override
	public void started() {
		// NOOP
	}

	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {

		// NOOP
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
		LOGGER.debug("Context prepared, about to extend classpath");

		Environment environment = context.getEnvironment();

		if (environment == null) {
			LOGGER.warn("Environment was not prepared, I don't know where to look for plugins");
			return;
		}

		String pluginsPath = environment.getProperty("squash.path.plugins-path");

		if (pluginsPath == null) {
			LOGGER.warn("Plugin path was not defined, I don't know where to look for plugins");
			return;
		}

		if (!(context instanceof DefaultResourceLoader)) {
			LOGGER.warn("Context is not a DefaultResourceLoader, I don't know how to change the classpath. There will be no plugins");
			return;
		}

		File pluginsFolder = new File(pluginsPath);

		if (!pluginsFolder.exists() || !pluginsFolder.isDirectory()) {
			LOGGER.warn("Plugin path '{}' is not a readable folder. There will be no plugins", pluginsFolder.getAbsolutePath());
			return;
		}

		LOGGER.info("Enumerating plugins / jars in folder '{}'", pluginsFolder.getAbsolutePath());

		File[] plugins = pluginsFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});

		URL[] pluginsUrls = new URL[plugins.length];

		for (int i = 0; i < plugins.length; i++) {
			try {
				URL pluginUrl = plugins[i].toURI().toURL();
				pluginsUrls[i] = pluginUrl;
				LOGGER.info("Jar '{}' will be added to classpath", pluginUrl);

			} catch (MalformedURLException e) {
				// I guess this should not happen because URL is built from an existing file
				LOGGER.warn("Plugin file '{}' could not be converted into a URL", plugins[i], e);
			}
		}


		ClassLoader extendedClassloader = new URLClassLoader(pluginsUrls, context.getClassLoader());
		((DefaultResourceLoader) context).setClassLoader(extendedClassloader);
		LOGGER.info("Classpath was extended with the content of plugins folder");

	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
		LOGGER.info("loaded");

	}

	@Override
	public void finished(ConfigurableApplicationContext context, Throwable exception) {
		LOGGER.info("finished");

	}
}
