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
 * Exception thrown if the administrator forgot to configure the secret for credentials encryption
 *
 */
public class MissingEncryptionKeyException extends ActionException {

	private static final String I18N_KEY = "storedcredentials.missingencryptionkey";

	private static final String STD_ERR_MSG= "Stored credentials : no encryption key was set. Squash TM won't store credentials until a key is supplied. "
											+ "Please contact your administrator and make sure that property squash.crypto.secret is configured either in the configuration file "
											+ "or at the command line using -Dsquash.crypto.secret=*******.";


	public MissingEncryptionKeyException() {
		super(STD_ERR_MSG);
	}

	@Override
	public String getI18nKey(){
		return I18N_KEY;
	}


}
