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
package org.squashtest.tm.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.squashtest.tm.api.config.SquashPathProperties;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.web.internal.context.ReloadableSquashTmMessageSource;
import org.squashtest.tm.web.internal.fileupload.MultipartResolverDispatcher;
import org.squashtest.tm.web.internal.fileupload.SquashMultipartResolver;
import org.squashtest.tm.web.internal.filter.AjaxEmptyResponseFilter;
import org.squashtest.tm.web.internal.filter.MultipartFilterExceptionAware;
import org.squashtest.tm.web.internal.filter.UserConcurrentRequestLockFilter;
import org.squashtest.tm.web.internal.filter.UserCredentialsCachePersistenceFilter;
import org.squashtest.tm.web.internal.listener.HttpSessionLifecycleLogger;
import org.squashtest.tm.web.internal.listener.OpenedEntitiesLifecycleListener;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import java.util.Collection;
import java.util.HashMap;

import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;
import static org.springframework.util.StringUtils.trimAllWhitespace;

/**
 * Servlet context config (mostly). Not in SquashServletInitializer because it delays the servlet context initialization for
 * too long.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@EnableConfigurationProperties({MessagesProperties.class})
@Configuration
public class SquashServletConfig {

    private static final String[] CREDENTIALS_CACHE_EXCLUDE_PATTERNS = new String[] {"/isSquashAlive/**","/scripts/**", "/static/**", "/images/**", "/styles/**" };

    private static final String IMPORTER_REGEX = ".*/importer/.*";
    private static final String UPLOAD_REGEX = ".*/attachments/upload.*";


	@Inject
	private MessagesProperties messagesProperties;
	@Inject
	private ThymeleafProperties thymeleafProperties;
	@Inject
	private SquashPathProperties squashPathProperties;
	@Inject
	private CredentialsProvider credentialsProvider;


	/**
	 * Message source which takes into account messages from "fragments"
	 * Overrides spring-boot default
	 * @return the message-source
	 */
	@Bean
	public MessageSource messageSource() {
		ReloadableSquashTmMessageSource bean = new ReloadableSquashTmMessageSource();
		bean.setSquashPathProperties(squashPathProperties);
		bean.setBasenames(commaDelimitedListToStringArray(trimAllWhitespace(messagesProperties.getBasename())));
		bean.setDefaultEncoding(messagesProperties.getEncoding());
		bean.setCacheSeconds(messagesProperties.getCacheSeconds());
		return bean;
	}


	/* ************************************************************************
									THYMELEAF
	************************************************************************* */

	/**
	 * A SpringTemplateEngine for the thymeleaf view resolvers
	 * which is for a large part declared as the one found
	 * in {@link ThymeleafAutoConfiguration} (and thus has access
	 * to the rest of the autoconfigured beans).
	 *
	 * It also accepts extra configuration for SpringEL compilation,
	 * and more importantly it will use our shiny-customize instance
	 * of Jackson Object mapper instead of the default Thymeleaf
	 * JacksonStandardJavaScriptSerializer
	 *
	 */
	@Bean
	public SpringTemplateEngine springTemplateEngine(
			Collection<ITemplateResolver> templateResolvers,
			ObjectProvider<Collection<IDialect>> dialectsProvider,
			ObjectMapper squashMapper
			){

		SpringTemplateEngine engine = new SpringTemplateEngine();
		for (ITemplateResolver templateResolver : templateResolvers) {
			engine.addTemplateResolver(templateResolver);
		}

		Collection<IDialect> dialects = dialectsProvider.getIfAvailable();
		if (!CollectionUtils.isEmpty(dialects)) {
			for (IDialect dialect : dialects) {
				engine.addDialect(dialect);
			}
		}

		injectObjectMapper(engine, squashMapper);

		return engine;

	}

	private void injectObjectMapper(SpringTemplateEngine engine, ObjectMapper mapper){
		for (IDialect dialect : engine.getDialects()){
			if (StandardDialect.class.isAssignableFrom(dialect.getClass())){
				StandardDialect stdDialect = (StandardDialect) dialect;
				stdDialect.setJavaScriptSerializer(new SquashObjectMapperThymeleafStandardSerializer(mapper));
			}
		}
	}


	/**
	 * The main Thymeleaf template resolver. It resolves pages of the core application and as such is the first
	 * template resolver in the chain (see order = 1). If a template cannot be resolved the secondary template resolver should
	 * kick in (see checkExistence).
	 *
	 * @param context
	 * @return
	 */
	@Bean(name = "thymeleaf.templateResolver.fragment")
	public ITemplateResolver fragmentTemplateResolver(ApplicationContext context) {
		SpringResourceTemplateResolver res = new OptionalSuffixThymeleafTemplateResolver(/*TODO : harmonize our template invocation convention, see javadoc*/);
		res.setApplicationContext(context);
		res.setPrefix(thymeleafProperties.getPrefix());
		res.setSuffix(".html");
		res.setTemplateMode(thymeleafProperties.getMode());
		res.setCharacterEncoding(thymeleafProperties.getEncoding().name());
		res.setCacheable(thymeleafProperties.isCache());
		res.setCheckExistence(true);
		res.setOrder(1);
		return res;
	}


	@Bean(name = "thymeleaf.templateResolver.plugins")
	public ITemplateResolver thymeleafClasspathTemplateResolver(ApplicationContext context) {
		SpringResourceTemplateResolver res = new SpringResourceTemplateResolver();
		res.setApplicationContext(context);
		res.setPrefix("classpath:/templates/");
		res.setSuffix("");
		res.setTemplateMode(thymeleafProperties.getMode());
		res.setCharacterEncoding(thymeleafProperties.getEncoding().name());
		res.setCacheable(thymeleafProperties.isCache());
		res.setOrder(2);
		return res;
	}



	/* ************************************************************************
									/THYMELEAF
	************************************************************************* */



	/* ************************************************************************
									MULTIPART
	************************************************************************* */

	@Bean
	@Role(BeanDefinition.ROLE_SUPPORT)
	public CommonsMultipartResolver filterMultipartResolver(){
            MultipartResolverDispatcher bean = new MultipartResolverDispatcher();
            bean.setDefaultResolver(defaultMultipartResolver());
            HashMap<String, SquashMultipartResolver> resolverMap = new HashMap<>();
            resolverMap.put(UPLOAD_REGEX, importMultipartResolver(ConfigurationService.Properties.UPLOAD_SIZE_LIMIT));
            resolverMap.put(IMPORTER_REGEX, importMultipartResolver(ConfigurationService.Properties.IMPORT_SIZE_LIMIT));
            bean.setResolverMap(resolverMap);
            return bean;
    }



	@Role(BeanDefinition.ROLE_SUPPORT)
	public SquashMultipartResolver defaultMultipartResolver() {
		SquashMultipartResolver bean = new SquashMultipartResolver();
                return bean;
	}


	@Role(BeanDefinition.ROLE_SUPPORT)
	public SquashMultipartResolver importMultipartResolver(String configKey) {
		SquashMultipartResolver bean = new SquashMultipartResolver();
		bean.setMaxUploadSizeKey(configKey);
		return bean;
	}

    @Bean
    public FilterRegistrationBean multipartFilterRegistrationBean() {
        final MultipartFilterExceptionAware multipartFilter = new MultipartFilterExceptionAware();
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(multipartFilter);
        return filterRegistrationBean;
    }


	/* ************************************************************************
									/MULTIPART
	************************************************************************* */


	@Bean
	@Order(1)
	public FilterRegistrationBean bugTrackerContextPersister() {

		UserCredentialsCachePersistenceFilter filter = new UserCredentialsCachePersistenceFilter();
		filter.setCredentialsProvider(credentialsProvider);
		filter.addExcludePatterns(CREDENTIALS_CACHE_EXCLUDE_PATTERNS);

		FilterRegistrationBean bean = new FilterRegistrationBean(filter);
		bean.setDispatcherTypes(DispatcherType.REQUEST);
		return bean;
	}

	@Bean
	@Order(1000)
	public FilterRegistrationBean ajaxEmptyResponseFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean(new AjaxEmptyResponseFilter());
		bean.setDispatcherTypes(DispatcherType.REQUEST);
		return bean;
	}

	@Bean @Order(500)
	public FilterRegistrationBean userConcurrentRequestLockFilter() {
		// This filter prevents a user from creating race conditions with himself. It prevents most concurrency-related
		// bugs (see #5748) but probably slows up the app.
		FilterRegistrationBean bean  = new FilterRegistrationBean(new UserConcurrentRequestLockFilter());
		bean.addInitParameter("excludePatterns", "(/|/isSquashAlive|/opened-entity)");
		bean.setDispatcherTypes(DispatcherType.REQUEST);

		return bean;
	}

	@Bean
	public HttpSessionLifecycleLogger httpSessionLifecycleLogger() {
		return new HttpSessionLifecycleLogger();
	}

	@Bean
	public OpenedEntitiesLifecycleListener openedEntitiesLifecycleListener() {
		return new OpenedEntitiesLifecycleListener();
	}

	@Bean
	public Hibernate5Module hibernate5JacksonModule() {
		Hibernate5Module bean = new Hibernate5Module();
		//Setting jackson tu eager on hibernate proxy... take care to your Mixins to avoid massive request ^^
		bean.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return bean;
	}
}
