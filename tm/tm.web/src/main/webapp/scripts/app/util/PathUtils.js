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
define([], function() {
  /**
   * Check if a path is conform to the following pattern :
   * name1/name2/name3...
   * no "/" are allowed as first or last character
   * "//" are rejecteds
   * empty string is correct as this method is used for partial path.
   * @param  String path [description]
   * @return Boolean
   */
	function validatePartialPath(path){
    if (path.length===0) {
      return true;
    }
    var regEx = new RegExp ("^(?!/)(.+?[^\\]/)+.*?(\/|[^\/])$");
    if (!regEx.test(path)) {
      return false;
    }
    var parts  = path.split("/");
    for (var i = 0; i < parts.length; i++) {
      var part = parts[i];
      if (part.length === 0) {
        return false;
      }
    }
    return true;
	}

	return {
		validatePartialPath : validatePartialPath
	};
});
