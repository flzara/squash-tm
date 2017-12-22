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
 * Holds the {@link #id} and the {@link #status} of an entity concerned by
 * the import.
 */
class TargetStatus {// NOSONAR this class is not final so that it can
    // be tested in ValidationFacilityTest

    // convenient alias
    static final TargetStatus NOT_EXISTS = new TargetStatus(
        Existence.NOT_EXISTS);

    /**
     * The {@link Existence} status of the concerned entity.
     */
    Existence status = null; // NOSONAR this attribute is local to the
    // package and the implementor knows what
    // he's
    // doing
    /**
     * The id of the concerned entity.
     */
    Long id = null; // NOSONAR this attribute is local to the package and

    // the implementor knows what he's doing

    TargetStatus(Existence status) {
        if (status == Existence.EXISTS) {
            throw new IllegalArgumentException(
                "internal error : a TargetStatus representing an actually existent target should specify an id");
        }
        this.status = status;
    }

    TargetStatus(Existence status, Long id) {
        this.status = status;
        this.id = id;
    }

    public Existence getStatus() {
        return status;
    }

    public Long getId() {
        return id;
    }

}
