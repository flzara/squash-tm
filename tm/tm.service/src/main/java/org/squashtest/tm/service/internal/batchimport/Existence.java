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
package org.squashtest.tm.service.internal.batchimport;

/**
 * That enum sort of represents the level of existence of a test case. It
 * can be either physically present, or virtually present, or virtually non
 * existent, or default to physically non existant.<br/>
 * It helps us keeping track of the fate of a test case during the import
 * process (which is, remember, essentially a batch processing).
 *
 * @author bsiri
 */
enum Existence {
    /**
     * exists now in the database
     */
    EXISTS,
    /**
     * will be created later on in the process
     */
    TO_BE_CREATED,
    /**
     * will be deleted later on in the process
     */
    TO_BE_DELETED,
    /**
     * at this point, doesn't exists either in DB nor in anything planned
     * later in the process
     */
    NOT_EXISTS
}
