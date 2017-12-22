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
/**
 * This file is part of the Squash TM management services for SaaS / Squash On Demand (saas.management.fragment) project.
 * Copyright (C) 2015 - 2016 Henix, henix.fr - All Rights Reserved
 * <p>
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * (C)Henix. Tous droits réservés.
 * <p>
 * Avertissement : ce programme est protégé par la loi relative au droit d'auteur et par les conventions internationales. Toute reproduction ou distribution partielle ou totale du logiciel, par quelque moyen que ce soit, est strictement interdite.
 */
package org.squashtest.tm.service.importer;

/**
 * Interface to be implemented by a {@link Target} which has a path.
 *
 * This interface was factored out of Target at a period of time where Target had become a pure abstract class, by this
 * I mean a class which has no implementation whatsoever, only abstract method declarations. Moreover, an implementation,
 * well, a specialization of Target for which path had no meaning used to throw exception when such meaningless methods were
 * called. This was error prone and indeed it led to an actual issue. As a followup of this issue, Target was refactored
 * to avoid this sort of bugs
 *
 * One could argue that I should have chosen to extend Target. But I chose to use separate interfaces and arcane generics
 * boundary syntax instead.
 *
 * @author bsiri, Gregory Fouquet
 * @since 1.14.0.RC2  07/06/16
 */
public interface WithPath {
	String getPath();
	String getProject();
}
