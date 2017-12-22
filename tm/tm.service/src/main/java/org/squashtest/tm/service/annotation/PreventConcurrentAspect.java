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
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.concurrent.EntityLockManager;
import org.squashtest.tm.service.concurrent.EntityLockManager.EntityRef;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This aspect manages @PreventConcurrent annotations.
 *
 * @author Gregory Fouquet
 * @since 1.11.6
 */
@Component
@Aspect
public class PreventConcurrentAspect implements Ordered {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreventConcurrentAspect.class);

	@Override
	/**
	 * This aspect must be executed around the spring transaction manager to avoid commits after the lock release.
	 * So it's precedence must be higher, and after some testing, the value of transaction manager precedence seems to be very high...
	 */
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

	@Around(value = "execution(@org.squashtest.tm.service.annotation.PreventConcurrent * *(..)) && @annotation(pc)", argNames = "pc")
	public Object lockEntity(ProceedingJoinPoint pjp, PreventConcurrent pc) throws Throwable { // NOSONAR propagated exception
		Serializable id = findEntityId(pjp);
		IdCoercer coercer =  pc.coercer().newInstance();
		Serializable coercedId = coercer.coerce(id);
		EntityRef ref = new EntityRef(pc.entityType(), coercedId);
		ReentrantLock lock = EntityLockManager.getLock(ref);
		lock.lock();
		LOGGER.warn("Acquired lock on {}", lock);

		try {
			return pjp.proceed();

		} finally {
			LOGGER.warn("Releasing lock on {}", lock);
			lock.unlock();

		}
	}

	@Around(value = "execution(@org.squashtest.tm.service.annotation.PreventConcurrents * *(..)) && @annotation(pc)", argNames = "pc")
	public Object lockEntities(ProceedingJoinPoint pjp, PreventConcurrents pc) throws Throwable { // NOSONAR propagated exception
		Set<EntityRef> refs = findEntityRefs(pjp,pc);
		Collection<Lock> locks = EntityLockManager.lock(refs);

		try {
			return pjp.proceed();

		} finally {
			EntityLockManager.release(locks);

		}
	}

	@Around(value = "execution(@org.squashtest.tm.service.annotation.BatchPreventConcurrent * *(..)) && @annotation(pc)", argNames = "pc")
	public Object lockEntities(ProceedingJoinPoint pjp, BatchPreventConcurrent pc) throws Throwable { // NOSONAR propagated exception
		Collection<? extends Serializable> sourceIds = findEntityIds(pjp);
		IdsCoercer coercer = pc.coercer().newInstance();
		Collection<? extends Serializable> ids = coercer.coerce(sourceIds);
		Set<EntityRef> refs = new HashSet<>();
		for (Serializable id : ids) {
			refs.add(new EntityRef(pc.entityType(), id));
		}
		Collection<Lock> locks = EntityLockManager.lock(refs);

		try {
			return pjp.proceed();

		} finally {
			EntityLockManager.release(locks);

		}
	}


	private Serializable findEntityId(ProceedingJoinPoint pjp) {
		return findAnnotatedParam(pjp, Id.class);
	}



	private Collection<? extends Serializable> findEntityIds(ProceedingJoinPoint pjp) {
		return findAnnotatedParam(pjp, Ids.class);
	}

	private <T> T findAnnotatedParam(ProceedingJoinPoint pjp, Class<? extends Annotation> expected) {
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Method meth = sig.getMethod();
		Annotation[][] annotations = meth.getParameterAnnotations();
		LOGGER.trace("Advising method {}{}.", pjp.getSignature().getDeclaringTypeName(), meth.getName());

		T annotatedParam = null;

		argsLoop:
		for (int iArg = 0; iArg < annotations.length; iArg++) {
			Annotation[] curArg = annotations[iArg];

			annLoop:
			for (int jAnn = 0; jAnn < curArg.length; jAnn++) {
				if (curArg[jAnn].annotationType().equals(expected)) {
					LOGGER.trace("Found required @{} on arg #{} of method {}", new Object[]{expected.getSimpleName(), iArg, meth.getName()});
					annotatedParam = (T) pjp.getArgs()[iArg];
					break argsLoop;
				}
			}
		}

		if (annotatedParam == null) {
			throw new IllegalArgumentException("I coult not find any arg annotated @" + expected.getSimpleName() + " in @PreventConcurrent method '" +
				pjp.getSignature().getDeclaringTypeName() + '.' + meth.getName() + "' This must be a structural programming error");

		}
		return annotatedParam;
	}

	//PRIVATE CODE FOR @PreventConcurents

	private Set<EntityRef> findEntityRefs( ProceedingJoinPoint pjp,PreventConcurrents pc) throws Throwable {
		Set<EntityRef> refs = new HashSet<>();
		refs.addAll(findEntityIdsForSimpleLocks(pjp,pc.simplesLocks()));
		refs.addAll(findEntityIdsForBashLocks(pjp,pc.batchsLocks()));
		return refs;
	}

	private Collection<EntityRef> findEntityIdsForBashLocks(ProceedingJoinPoint pjp, BatchPreventConcurrent[] batchsLocks) throws Throwable {
		Set<EntityRef> refs = new HashSet<>();
		for (BatchPreventConcurrent batchPreventConcurrent : batchsLocks) {
			refs.addAll(findEntityRefForNamedParam(pjp, batchPreventConcurrent));
		}
		return refs;
	}

	private Collection<EntityRef> findEntityIdsForSimpleLocks(ProceedingJoinPoint pjp, PreventConcurrent[] simplesLocks) throws Throwable {
		Set<EntityRef> refs = new HashSet<>();
		for (PreventConcurrent preventConcurrent : simplesLocks) {
			refs.add(findEntityRefForNamedParam(pjp, preventConcurrent));
		}
		return refs;
	}

	private EntityRef findEntityRefForNamedParam(ProceedingJoinPoint pjp, PreventConcurrent preventConcurrent) throws Throwable {
		Class<?> entityType = preventConcurrent.entityType();
		Serializable id = findIdForNamedParam(pjp, preventConcurrent.paramName(), Id.class);
		IdCoercer coercer = preventConcurrent.coercer().newInstance();
		EntityRef entityRef = new EntityRef(entityType, coercer.coerce(id));
		LOGGER.debug("Prevent Concurency - Finded an entity to lock {}.", entityRef.toString());
		return entityRef;
	}

	private Set<EntityRef> findEntityRefForNamedParam(ProceedingJoinPoint pjp, BatchPreventConcurrent batchPreventConcurrent) throws Throwable {
		Class<?> entityType = batchPreventConcurrent.entityType();
		Object sourceIds = findIdForNamedParam(pjp, batchPreventConcurrent.paramName(), Ids.class);
		IdsCoercer coercer = batchPreventConcurrent.coercer().newInstance();
		Collection<? extends Serializable> ids = coercer.coerce(sourceIds);
		Set<EntityRef> refs = new HashSet<>();
		for (Serializable id : ids) {
			refs.add(new EntityRef(entityType, id));
		}
		LOGGER.debug("Prevent Concurency - Finded several entities to lock {}.", refs.toString());
		return refs;
	}

	private <T> T findIdForNamedParam(ProceedingJoinPoint pjp, String paramName, Class<? extends Annotation> expected) {
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Method meth = sig.getMethod();
		Annotation[][] annotations = meth.getParameterAnnotations();
		LOGGER.debug("Prevent Concurency - Advising method {}{}.", pjp.getSignature().getDeclaringTypeName(), meth.getName());

		T annotatedParam = null;

		argsLoop:
		for (int iArg = 0; iArg < annotations.length; iArg++) {
			Annotation[] curArg = annotations[iArg];

			annLoop:
			for (int jAnn = 0; jAnn < curArg.length; jAnn++) {
				if (curArg[jAnn].annotationType().equals(expected)) {
					String annoValue = findAnnotationParamName(curArg[jAnn]);
					if (annoValue.equals(paramName)) {
						LOGGER.trace("Found required @{} on arg #{} of method {}", new Object[]{expected.getSimpleName(), iArg, meth.getName()});
						annotatedParam = (T) pjp.getArgs()[iArg];
					}
					else {
						throw new IllegalArgumentException("I coult not find any arg annotated @" + expected.getSimpleName() + " with a value of "+ paramName + " in @PreventConcurrent method '" +
								pjp.getSignature().getDeclaringTypeName() + '.' + meth.getName() + ". Instead an @Id was found with a value of "+ annoValue + "' This must be a structural programming error");
					}
					break argsLoop;
				}
			}
		}

		if (annotatedParam == null) {
			throw new IllegalArgumentException("I coult not find any arg annotated @" + expected.getSimpleName() + " in @PreventConcurrent method '" +
				pjp.getSignature().getDeclaringTypeName() + '.' + meth.getName() + "' This must be a structural programming error");
		}
		return annotatedParam;
	}

	//It's safe, type have been checked before.
	private String findAnnotationParamName(Annotation annotation) {
		if (annotation.annotationType().equals(Id.class)) {
			return ((Id)annotation).value();
		}
		if (annotation.annotationType().equals(Ids.class)) {
			return ((Ids)annotation).value();
		}
		return null;
	}

}
