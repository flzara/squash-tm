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
package org.squashtest.tm.web.internal.controller.generic;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

/*
 *https://gist.github.com/jonikarppinen/662c38fb57a23de61c8b
 */

@Controller
public class SquashErrorController implements ErrorController {

    private static final String PATH = "/error";
    
    @Inject
    private ErrorAttributes errorAttributes;

    /*
     * This method will be called when any non handled exception occurs. But we cannot just rethrow that exception : 
     * quis custodiet ipsos custodes? There is no other error handler after this one. 
     * 
     *   So we must manually handle the job of printing the exception.
     * 
     */
    @RequestMapping(value = PATH)
    public String error(HttpServletRequest request, HttpServletResponse response, Model model) throws Throwable{
        
    	Map<String ,Object> errors = getErrorAttributes(request);
    	
    	model.addAllAttributes(errors);
    	model.addAttribute("code", response.getStatus());
    	
    	return "page/error";
    	
    	
    }
    
    @RequestMapping(value = PATH, produces={"application/json", "application/*+json"})
    @ResponseBody
    public Map<String,Object> errorJson(HttpServletRequest request, HttpServletResponse response, Model model) throws Throwable{
        
    	return getErrorAttributes(request);
    	
    }
    

    @Override
    public String getErrorPath() {
        return PATH;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, true);
    }
    
    /*
     * for reminder here is how you can fetch the exception
    private Throwable getError(HttpServletRequest request){
    	RequestAttributes requestAttributes = new ServletRequestAttributes(request);
    	return (Throwable)requestAttributes.getAttribute("javax.servlet.error.exception", RequestAttributes.SCOPE_REQUEST);
    }*/
}
