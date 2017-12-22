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
package org.squashtest.tm.web.internal.model.json;

import java.util.List;

import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.UserListItem;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Jackson mixin to deserialize InfoList. It deserializes InfoListItems into UserListItems.
 * 
 * TODO maybe there should be a discriminator in the item's json, then we would default to USerListItem when
 * discriminator is absent.
 * 
 * 
 * @author Gregory Fouquet
 * 
 */
@JsonAutoDetect
public abstract class InfoListMixin {
	// FIXME : I strongly suggest ListItemReference.class instead as
	// it was meant for similar purposes
	@JsonDeserialize(contentAs = UserListItem.class)
	private List<InfoListItem> items;

}
