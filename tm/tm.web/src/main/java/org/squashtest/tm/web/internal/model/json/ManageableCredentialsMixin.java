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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.squashtest.tm.service.internal.servers.ManageableBasicAuthCredentials;
import org.squashtest.tm.service.internal.servers.ManageableTokenAuthCredentials;
import org.squashtest.tm.service.internal.servers.UserOAuth1aToken;

@JsonTypeInfo(include=JsonTypeInfo.As.PROPERTY, use=Id.NAME, property="type")
@JsonSubTypes({
	@Type(name="BASIC_AUTH", value=ManageableBasicAuthCredentials.class),
	@Type(name="OAUTH_1A", value=UserOAuth1aToken.class),
	@Type(name = "TOKEN_AUTH", value = ManageableTokenAuthCredentials.class)
})
public interface ManageableCredentialsMixin {

}
