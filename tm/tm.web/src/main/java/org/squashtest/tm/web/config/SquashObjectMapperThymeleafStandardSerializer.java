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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.serializer.IStandardJavaScriptSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 * Replacement for the regular Jackson-based Thymeleaf serializer, that will use our own configured ObjectMapper instead.
 * 
 * @author bsiri
 *
 */
class SquashObjectMapperThymeleafStandardSerializer implements IStandardJavaScriptSerializer{

	private ObjectMapper mapper;
	
	/*
	 * The object mapper we use here need a few features that Thymeleaf would set for its own serializer, especially 
	 * regarding whether it should close the stream (here it must not).
	 * (see StandardJavaScriptSerializer#JacksonStandardJavaScriptSerializer)
	 * 
	 */
	public SquashObjectMapperThymeleafStandardSerializer(ObjectMapper mapper) {
		super();
		this.mapper = mapper.copy();
		this.mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
	}
	
	
	@Override
	public void serializeValue(Object object, Writer writer) {
        try {
            this.mapper.writeValue(writer, object);
        } catch (final IOException e) {
            throw new TemplateProcessingException(
                    "An exception was raised while trying to serialize object to JavaScript using Jackson", e);
        }
	}

	
	
}
