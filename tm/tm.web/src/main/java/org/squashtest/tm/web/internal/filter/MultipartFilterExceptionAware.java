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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.squashtest.tm.web.internal.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MultipartFilter;
import org.squashtest.tm.web.internal.exceptionresolver.MaxUploadSizeErrorModel;

/**
 * <p>
 * This filter will behave like the MultipartFilter and handle possible exceptions thrown by the MultipartResolvers. 
 * Because filters are executed in the outer rings of the application the regular exception handlers won't kick in.
 * That's why we must take care of problems here.
 * </p>
 * 
 * <p>
 * As of Squash TM 15 :
 * The following is patched together from the code of HandlerMaxUploadSizeExceptionResolver, 
 * that might not be useful at all now.
 * </p>
 * @author bsiri
 */
public class MultipartFilterExceptionAware extends MultipartFilter{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartFilterExceptionAware.class);

    @Override
    protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

        try{
            super.doFilterInternal(request, response, filterChain);
        }
        catch(MaxUploadSizeExceededException ex){
            handleException(ex, response);
        }
        
    }
    
    // we return json only, because that is the main use case here.
    protected void handleException(MaxUploadSizeExceededException ex, HttpServletResponse response) throws JsonProcessingException, IOException{
        LOGGER.error("exception while uploading file", ex);
        
        Map<String, Object> mainError = new HashMap<>();
        mainError.put("maxUploadError", new MaxUploadSizeErrorModel(ex));        
        String strErr = new ObjectMapper().writeValueAsString(mainError);
        
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setContentType("application/json");
        response.getWriter().write(strErr);
    }
    
}
