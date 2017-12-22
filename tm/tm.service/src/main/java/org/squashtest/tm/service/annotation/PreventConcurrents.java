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

import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;

/**
 * See {@link PreventConcurrent} for base doc.
 * <code>
 * <p>
 * This annotation allow to lock different types of entities, mixing simple locks and batch locks.
 * <ul>
 * 	<li>Each simple lock will be a nested {@link PreventConcurrent} in simple lock array {@link PreventConcurrents#simplesLocks()}.</li>
 * 	<li>Each batch lock will be a nested {@link BatchPreventConcurrent} in batch lock array {@link PreventConcurrents#batchsLocks().</li>
 * 	<li>You have to define the {@link PreventConcurrent#paramName()} and {@link BatchPreventConcurrent#paramName()}. 
 * 	The same string should be put inside {@link Id} and {@link Ids} values. We have to do that as workaround to Spring Proxys wich are in fact JDK proxy.
 * 	This kind of proxy are bound to the interface of the Spring bean, and do not provide the method parameter name's to the aspect processing {@link PreventConcurrents}.
 * 	So you have to tag the parameter with {@link Id#value()} and {@link Ids#value()} so we can retrieve the good paramater and associate with the corresponding entity class</li>
 * 	<li>It also ensure that no dead lock can occurs if the same entity is present in severals arguments to be locked.</li>
 * </ul>
 * </p>
 * <p>
 * Example of usage : {@link TestCaseLibraryNavigationService#copyNodesToFolder(long, Long[])}
 * </p>
 *</code>
 * @author Julien Thebault
 * @since 1.13.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreventConcurrents {
	PreventConcurrent[] simplesLocks() default {};
	BatchPreventConcurrent[] batchsLocks() default {};
}
