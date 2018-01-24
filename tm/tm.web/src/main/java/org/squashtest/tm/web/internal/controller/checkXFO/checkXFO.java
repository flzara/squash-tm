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
package org.squashtest.tm.web.internal.controller.checkXFO;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class checkXFO {

    @ResponseBody
	@RequestMapping(value = "/checkXFO", method = RequestMethod.POST, params = "URL")
	public boolean XFOAllowForAll(@RequestParam("URL") String url) {

        boolean result = false;
        URL obj;
        try {
            obj = new URL(url);
            URLConnection conn = obj.openConnection();

           // get header by 'key'
            String XFrameOptions = conn.getHeaderField("X-Frame-Options");

            if (!"DENY".equals(XFrameOptions) && !"SAMEORIGIN".equals(XFrameOptions)) {
                result = true;
            }

        } catch (IOException e) {
            // nothing to do
		}

        return result;

    }
}
