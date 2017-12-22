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
package org.squashtest.tm.service.internal.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.core.ParameterNameDiscoverer;

/**
 * ParameterNameDiscoverer which parameter names according to their position in the method's signature. In other words,
 * given a method's signature <code>void foo(int bar, String baz)</code>, this method's parameters will be resolved with
 * the names <code>[arg0, arg1]</code>
 * 
 * @author Gregory Fouquet
 * 
 */
public class ArgumentPositionParameterNameDiscoverer implements ParameterNameDiscoverer {

	@Override
	public String[] getParameterNames(Method method) {
		return getArgsNames(method.getParameterTypes().length);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String[] getParameterNames(Constructor ctor) {
		return getArgsNames(ctor.getParameterTypes().length);
	}

	private String[] getArgsNames(int length) {
		String[] argsNames = new String[length];

		for (int i = 0; i < argsNames.length; i++) {
			argsNames[i] = "arg" + i;
		}
		return argsNames;
	}
}
