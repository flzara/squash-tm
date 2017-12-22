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
package org.squashtest.tm.service.deletion;

import java.util.Locale;

import org.springframework.context.MessageSource;


/**
 * <p>A SuppressionPreviewReport is an object that can deliver a localized information (given a MessageSource and a Locale), used in the context of entity removal (hence the name). It is 
 * filled by the system in response of simulating the deletion <p>   
 * 
 * @author bsiri
 *
 */
public interface SuppressionPreviewReport  {
	
	String toString(MessageSource source, Locale locale);
	
}
