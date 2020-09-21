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

import org.squashtest.tm.domain.milestone.MilestoneMember;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is similar to {@linkplain CheckLockedMilestone}
 * but is used on methods which modify <b>multiple</b> {@linkplain MilestoneMember}s or their components.
 * <br/>
 * See {@linkplain CheckLockedMilestone} annotation for main documentation.
 * <p/>
 * The argument which conveys the entities' ids has to be annotated with @{@linkplain Ids}.
 * <p/>
 * This annotation is managed by the aspect {@linkplain CheckLockedMilestoneAspect}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckLockedMilestones {
	/**
	 * Class of the {@linkplain MilestoneMember} which is modified by the annotated method.
	 */
	Class<? extends MilestoneMember> entityType();
}
