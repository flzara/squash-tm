/*
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
/*
 * This object will retrieve the permissions rules associated to this workspace. It will be an instance of 'tc-workspace/permissions-rules', 
 * 'req-workspace/permissions-rules' or 'camp-workspace/permissions-rules', provided that one has been instanciated already. 
 * This object doesn't know which one, it just passes it when asked to. Will throw an exception if no instance of permissions-rules could be found.
 * 
 */
define(function() {
	return {
		errmsg : "error : no permission-rules could be found. Please ensure that either 'tc-workspace/permissions-rules', "	+ 
				 "'req-workspace/permissions-rules' or 'camp-workspace/permissions-rules' have been invoked first.",

		get : function() {
			try {
				var instance = squashtm.workspace.permissions_rules;
				if (instance === undefined) {
					throw this.errmsg;
				}
				return instance;
			} catch (severly_undefined) {
				throw this.errmsg;
			}
		}
	};
});
