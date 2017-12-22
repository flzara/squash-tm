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
package org.squashtest.tm.launcher.ihm;

import java.awt.Desktop;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.squashtest.tm.launcher.ihm.window.SimpleWindow;

// IGNOREVIOLATIONS:FILE Don't bother rule-checking this app, it is pretty useless anyway
public class Starter {

	//Squash url
	private static final String SQUASH_URL = "http://127.0.0.1:8080/squash/";
	//Number of milliseconds until new http request
	static int intervals = 5000;

	public static void main(String[] args) {


		SimpleWindow sw = new SimpleWindow();
		sw.setVisible( true );

		boolean stop = false;

		while(!stop){
			stop = myTask();
			try {
				Thread.sleep(intervals); //5 secs
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
            Desktop.getDesktop().browse(new URI(Starter.SQUASH_URL));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
		//close window
		System.exit(0);
	}

	public static boolean myTask(){
		HttpURLConnection http;
		String response = "";
		try {
			http = (HttpURLConnection) new URL(Starter.SQUASH_URL).openConnection();
			http.setRequestMethod("GET");
			http.connect();
			response = http.getResponseMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "ok".equalsIgnoreCase(response);
	}
}
