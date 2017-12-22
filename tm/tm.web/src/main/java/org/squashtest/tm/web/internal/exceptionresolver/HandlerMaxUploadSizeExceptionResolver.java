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
package org.squashtest.tm.web.internal.exceptionresolver;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;

/*
Deprecation notice : Because multipart requests are handled in the filter chain this 
exception resolver will probably not kick in anymore. I'm deprecating this 
and put a few loggers to watch if its ever invoked at all. 

If by Squash TM 16 it has shown no sign of activity feel free to decommission this class.
*/
@Component
@Deprecated 
public class HandlerMaxUploadSizeExceptionResolver extends AbstractHandlerExceptionResolver {

	private static final int NB_BYTES_PER_MBYTES = 1048576;
        
        // SONAR made me rename the logger in order to avoid nameclash with the logger in the supperclass
        private static final Logger THIS_LOGGER = LoggerFactory.getLogger(HandlerMaxUploadSizeExceptionResolver.class);

	@Inject
	private InternationalizationHelper messageSource;

	public HandlerMaxUploadSizeExceptionResolver() {
		super();
	}

	@Override
        @ExceptionHandler(value = {MaxUploadSizeExceededException.class})
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
	                                          Exception ex) {
                THIS_LOGGER.trace("received exception, testing whether it should be handled");
            
		if (exceptionIsHandled(ex)) {

                        THIS_LOGGER.trace("exception is being handled");
                    
			response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);

			MaxUploadSizeExceededException mex = (MaxUploadSizeExceededException) ex; // NOSONAR Type was checked
			// earlier

			if (ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.APPLICATION_JSON)) {
                                THIS_LOGGER.trace("MIME type is application/json, returning response as json");
				return handleAsJson(mex);
			} else if (ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.TEXT_PLAIN)) {
                            THIS_LOGGER.trace("MIME type is text/plain, returning response as plain text");
				return handleAsText(mex);
			} else if (ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.TEXT_HTML)) {
                            THIS_LOGGER.trace("MIME type is text/html, returning response as html");
				return handleAsHtml(mex);
			}
			// special delivery for IE
			else if (ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.ANYTHING)) {
                            THIS_LOGGER.trace("MIME type is */*, returning response as plain text");
				return handleAsText(mex);
			}
		}

                THIS_LOGGER.trace("the exception was not processed because it was not a valid target");
		return null;
	}

	private ModelAndView handleAsJson(MaxUploadSizeExceededException mex) {
		MaxUploadSizeErrorModel error = new MaxUploadSizeErrorModel(mex);
		return new ModelAndView(new MappingJackson2JsonView(), "maxUploadError", error);
	}

	private ModelAndView handleAsText(MaxUploadSizeExceededException mex) {

		String error = "{ \"maxSize\" : " + mex.getMaxUploadSize() + "}";

		AbstractView view = new MaxSizeView();

		return new ModelAndView(view, "actionValidationError", error);

	}

	private ModelAndView handleAsHtml(MaxUploadSizeExceededException mex) {

		Long size = mex.getMaxUploadSize() / NB_BYTES_PER_MBYTES;

		String msg = messageSource.getMessage("message.AttachmentUploadSizeExceeded", null, "message.AttachmentUploadSizeExceeded", LocaleContextHolder.getLocale()).replaceAll("#size#", size.toString());

		String error = "<div>" + msg + "</div>";

		AbstractView view = new MaxSizeView();

		return new ModelAndView(view, "actionValidationError", error);

	}

	private static class MaxSizeView extends AbstractView {
		@Override
		protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
		                                       HttpServletResponse response) throws Exception {
			ServletOutputStream out = response.getOutputStream();
			out.write(((String) model.get("actionValidationError")).getBytes());
			out.flush();
		}
	}

	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof MaxUploadSizeExceededException;
	}

}
