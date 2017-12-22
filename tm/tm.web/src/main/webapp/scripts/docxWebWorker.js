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
importScripts('require.js');

var eventRecived;
self.onmessage = function(e) {
	//sometime the worker get the message before init so need to catch the event so it's not lost
	eventRecived = e;
};
	
require(["lib/docxgen/docxgen.min"], function( DocxGen) {

	doAction = function(e) {
	
		doc = new DocxGen(e.data[0]);
		doc.setData(e.data[1]); // set the templateVariables
		doc.render(); // apply them (replace all occurences of {first_name} by Hipp, ...)
		output = doc.getZip().generate({type : "base64"}); // Output the document using Data-URI

		postMessage(output);
		
	};
	
	if (!!eventRecived) {
		doAction(eventRecived);		
	}
	self.onmessage = function(e) {
		doAction(e);
	};
	
});


