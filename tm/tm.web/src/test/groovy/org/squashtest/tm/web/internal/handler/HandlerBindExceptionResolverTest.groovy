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
package org.squashtest.tm.web.internal.handler

import org.apache.commons.collections.iterators.IteratorEnumeration
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.servlet.view.json.MappingJackson2JsonView
import org.squashtest.tm.web.internal.exceptionresolver.HandlerBindExceptionResolver
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HandlerBindExceptionResolverTest extends Specification {
	HandlerBindExceptionResolver resolver = new HandlerBindExceptionResolver()
	HttpServletRequest jsonRequest = Mock()
	BindException bindException = Mock()
	
	def setup() {
		jsonRequest.getHeaders("Accept") >> new IteratorEnumeration(['application/json'].iterator())
		bindException.getFieldErrors() >> []
	}
	
	def "should not handle exception other than BindException"() {
		when:
		def res = resolver.doResolveException(jsonRequest, Mock(HttpServletResponse), Mock(Object), new RuntimeException())
		
		then: 
		res == null
	}	
	
	def "should return a MappingJackson2JsonView"() {
		when:
		def res = resolver.doResolveException(jsonRequest, Mock(HttpServletResponse), Mock(Object), bindException)
		
		then: 
		res.view instanceof MappingJackson2JsonView
	}	
	
	def "returned model should contain a fieldValidationErrors list"() {
		when:
		def res = resolver.doResolveException(jsonRequest, Mock(HttpServletResponse), Mock(Object), bindException)
		
		then: 
		res.model['fieldValidationErrors'] == []
	}
	
	def "should not process requests not accepting json"() {
		given:
		HttpServletRequest req = Mock()
		req.getHeaders("Accept") >> new IteratorEnumeration(['text/xhtml'].iterator())
		
		when:
		def res = resolver.doResolveException(req, Mock(HttpServletResponse), Mock(Object), bindException)
		
		then: 
		res == null
	}
	
	
		def "returned model should contain a fieldValidationErrors"() {
		given:
		FieldError err = Mock()
		err.getObjectName() >> "baz"
		err.getField() >> "foo"
		err.getDefaultMessage() >> "bar"
		
		and: 
		BindException bex = Mock()
		bex.getFieldErrors() >> [err]
		
		when:
		def res = resolver.doResolveException(jsonRequest, Mock(HttpServletResponse), Mock(Object), bex)
		
		then:
		res.model['fieldValidationErrors'].size() == 1
		res.model['fieldValidationErrors'][0].objectName == "baz"
		res.model['fieldValidationErrors'][0].fieldName == "foo"
		res.model['fieldValidationErrors'][0].errorMessage == "bar"
	}
	
	def "response status should be set to 412"() {
		given:
		HttpServletResponse response = Mock()
		
		when:
		resolver.doResolveException(jsonRequest, response, Mock(Object), bindException)
		
		then:
		1 * response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED)
	}
}
