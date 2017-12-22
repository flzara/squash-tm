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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Named;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Make a copy of the argument list. Decision was taken to copy the list, as passing directly {@link ProceedingJoinPoint} args can lead to
 * unpredictable results if the list is modified by a {@link IdsCoercerExtender} after.
 * @author Gregory Fouquet
 * @since 1.11.6
 */
@Named("passThroughIdsCoercer")
public class PassThroughIdsCoercer implements IdsCoercer {
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<? extends Serializable> coerce(Object object) {
		Collection<? extends Serializable> args = (Collection<? extends Serializable>) object;
		return new ArrayList<>(args);
	}

}
