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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;
import org.hibernate.transform.AliasToBeanResultTransformer;

/**
 * <p>
 * 	The purpose is simple : provide a simple way to use an AliasToBeanConstructorResultTransformer. 
 * It is useful when you just want to use a tuple as parameters of a constructor. For HQL queries 
 * it's cheaper than using the {@link AliasToBeanResultTransformer} because one don't have to look up 
 * for every setters. </p>
 * 
 * <p>The constructor that will be used is the first one found, so you'd better design your DTO to have 
 * exactly one constructor if you don't like surprises.</p>
 * 
 * <p>Some will argue that HQL natively support this (using the "select new org.MyPojo(...)" idiom). However 
 * it doesn't always hold here because we run in an OSGI environment, which leads sometimes to classloading 
 * issues. The most common problematic case is when a "select new" is used with a parameter list : 
 * in this case the Hibernate engine has to expand the "in (:paramlist)" the hql in a new query then parse it again 
 * using the classloader of the thread instead of the one we want (the one of the session factory). 
 * The dev team promised it will be solved by Hibernate 5 (see {@link ReflectHelper} and {@link ClassLoaderHelper}).</p>
 * 
 * @author bsiri
 *
 */
public class EasyConstructorResultTransformer extends AliasToBeanConstructorResultTransformer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EasyConstructorResultTransformer(Class<?> clazz) {		
		super(clazz.getDeclaredConstructors()[0]);
	}
	

}
