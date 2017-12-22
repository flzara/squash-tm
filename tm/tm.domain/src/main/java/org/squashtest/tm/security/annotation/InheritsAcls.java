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
package org.squashtest.tm.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an objects inherits acl from another constrained object. One should be able to navigate from the ACL
 * constrained object to the target object. In the assotiation is single valued, the inheriting object should be
 * accessible using a property named {@link#propertyName()}. If the association is multi-valued, it should be accessible
 * using a property named {@link #collectionName()}
 *
 * @author Gregory Fouquet
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InheritsAcls {
	/**
	 * Class from which the target objects inherits ACLs.
	 */
	Class<?> constrainedClass();

	/**
	 * Name of the multi-valued property in {@link #constrainedClass()} which contains the target object. When
	 * {@link #collectionName()} is used, {@link #propertyName()} should be <code>null</code>.
	 *
	 * @return
	 */
	String collectionName() default "";

	/**
	 * Name of the single-valued property in {@link #constrainedClass()} which references the target object. When
	 * {@link #propertyName()} is used, {@link #collectionName()} should be <code>null</code>.
	 *
	 * @return
	 */
	String propertyName() default "";
}
