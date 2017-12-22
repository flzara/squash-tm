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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

import java.util.*;

/**
 * <p>This aspect will handle the following meta annotations :
 * 	<ul>
 * 		<li>EmptyCollectionGuard</li>
 * 		<li>Maybe more to come ?</li>
 * 	</ul>
 *  which are all intended to refine the default behaviour dynamically-generated Spring JPA DAO.
 * </p>
 * <p>
 * See below for details.
 * </p>
 *
 * <h3>EmptyCollectionGuard</h3
 *
 * <p>
 * A method of a Spring JPA repository will not fail when passed
 * empty {@link Iterable}, if that method has the annotation {@link EmptyCollectionGuard}.</p>
 *
 * <p>When this aspect is triggered, any argument of type/subtype of {@link Iterable} will be checked against emptyness.</p>
 *
 * <p>
 *  If the test passes the call will be forwared to the target method. Otherwise it will be aborted and a value semantically meaning
 *  "no results" will be returned. The actual result depend on the expected returned type : </p>
 * <ul>
 * 		<li>void : returns null</li>
 * 		<li>Collection (or subclass) : returns an empty List/Set, or throw {@link UnsupportedReturnTypeException} for other subtypes of Collection</li>
 * 		<li>Object : returns null </li>
 * 		<li> primitive : 0/false etc</li>
 * </ul>
 * </p>
 *
 * <p>
 *  History note : part of the code is scrapped from @link ArbitraryQueryHandler (core.dynamicmanagers)
 * </p>
 *
 */
@Aspect
public class SpringDaoMetaAnnotationAspect implements Ordered {

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}


	@Pointcut(value = "call(@org.squashtest.tm.service.annotation.EmptyCollectionGuard * org.springframework.data.repository.Repository+.*(..))")
	public void callEmptyCollectionGuard() {

	}

	// NOSONAR yes I know I throw a Throwable but that's usual stuff with reflection <insert more bitching here>
	@Around(value = "callEmptyCollectionGuard()")
	public Object guardAgainstEmptyness(ProceedingJoinPoint pjp) throws Throwable {
		Object[] args = pjp.getArgs();

		// abort if one argument is an empty collection
		for (Object arg : args) {
			if (isEmptyIterable(arg)) {
				return abortQuery(pjp);
			}
		}

		// else proceed
		return pjp.proceed();

	}


	private Object abortQuery(ProceedingJoinPoint pjp) {

		Object result;

		Class<?> returnType = findReturnType(pjp);


		if (returnType == null) {
			// damn, could not find what it is ! trying dumb luck now
			result = null;
		} else if (isCollectionType(returnType)) {
			// for collections
			result = newEmptyCollection(returnType);
		} else if (returnType.isPrimitive()) {
			// for primitive types
			result = newPrimitiveZero(returnType);
		} else {
			// for other entities
			result = null;
		}

		return result;
	}

	private Class<?> findReturnType(ProceedingJoinPoint pjp) {
		Signature sig = pjp.getSignature();
		if (MethodSignature.class.isAssignableFrom(sig.getClass())) {
			return ((MethodSignature) pjp.getSignature()).getReturnType();
		} else {
			return null;
		}
	}

	private Object newEmptyCollection(Class<?> returnType) {
		Object res;

		if (isList(returnType)) {
			res = new ArrayList<>();
		} else if (isSet(returnType)) {
			res = new HashSet<>();
		} else if (isQueue(returnType)) {
			res = new LinkedList<>();
		} else {
			throw new UnsupportedReturnTypeException(returnType);
		}

		return res;
	}

	private Object newPrimitiveZero(Class<?> returnType) {
		Object res;

		if (returnType.equals(Short.TYPE)) {
			res = 0;
		} else if (returnType.equals(Integer.TYPE)) {
			res = 0;
		} else if (returnType.equals(Float.TYPE)) {
			res = 0f;
		} else if (returnType.equals(Double.TYPE)) {
			res = 0d;
		} else if (returnType.equals(Long.TYPE)) {
			res = 0L;
		} else if (returnType.equals(Byte.TYPE)) {
			res = (byte) 0;
		} else {
			res = (char) 0;
		}

		return res;
	}

	private boolean isEmptyIterable(Object arg) {
		return arg != null &&
			Iterable.class.isAssignableFrom(arg.getClass()) &&
			!((Iterable) arg).iterator().hasNext();
	}

	private boolean isCollectionType(Class<?> paramType) {
		return Collection.class.isAssignableFrom(paramType);
	}

	private boolean isList(Class<?> paramType) {
		return List.class.isAssignableFrom(paramType);
	}

	private boolean isSet(Class<?> paramType) {
		return Set.class.isAssignableFrom(paramType);
	}


	private boolean isQueue(Class<?> paramType) {
		return Queue.class.isAssignableFrom(paramType);
	}


	public static final class UnsupportedReturnTypeException extends RuntimeException {
		public UnsupportedReturnTypeException(Class<?> returnType) {
			super("Class '" + returnType + "' is not supported by the dao empty arguments guard. "
				+ "Details : attempted to execute a Spring dynamic Dao with an empty "
				+ "collection as parameter. Tried then to return an empty result, "
				+ "but cannot find a suitable value for the expected return type '" + returnType + "'");
		}
	}


}
