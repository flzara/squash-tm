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
package org.squashtest.tm.service.servers;

import org.squashtest.tm.core.foundation.exception.ActionException;

/**
 * Exception thrown when attempting to retrieve credentials that were encrypted with a different key than the current one,
 * and this is completely undecipherable.
 *
 */
public class EncryptionKeyChangedException extends ActionException {

	private static final String I18N_KEY = "storedcredentials.encryptionkeychanged";

	private static final String STD_ERR_MSG= "Stored credentials : it seems the encryption key changed since the credentials were encrypted. " +
												 "The credentials cannot be read again without that key. The solutions are either 1/ restore the key " +
												 "if the change was unintentional or 2/ reconfigure the stored credentials.";


	public EncryptionKeyChangedException() {
		super(STD_ERR_MSG);
	}

	@Override
	public String getI18nKey(){
		return I18N_KEY;
	}


}
