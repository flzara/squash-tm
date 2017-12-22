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
package org.squashtest.tm.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods with this annotation should not be run concurrently when they access the same entity.
 * <p/>
 * The method arg which conveys the entity's id has to be annotated with @Id
 * <p/>
 * This annotation is processed at runtime using Spring AOP so it can only be used on spring beans. It means it should
 * be put on the concrete class and @Id should be put on the interface (yeah, that sucks) => best thing is to put it on
 * both the interface and the concrete class.
 * 
 * For generic method, as the type of the entity is required, we have to override the generic method, and call the generic method with
 * super.methodName...
 *
 * @author Gregory Fouquet
 * @since 1.11.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreventConcurrent {
	/**
	 * Type of the entity which should be locked
	 */
	Class<?> entityType();
	
	/**
	 * The name of the id parameter, used only if method needs severals locks. 
	 * We have to put the name here, as Spring proxy are JDK proxy if the bean implement an interface.
	 * So inside the aspect, we cannot retrieve the name of the parameters of the method. The SAME name value should be put inside the {@link Id}
	 * to allow the link.
	 * @return
	 */
	String paramName() default "";
	
	/**
	 * Class of a coercer if needed. Default is a direct transmission of the argument to {@link PreventConcurrentAspect}
	 */
	Class<? extends IdCoercer> coercer() default PassThroughIdCoercer.class;
}
