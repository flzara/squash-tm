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
define(['module',"jquery", "workerWithoutFake!docxWebWorker.js", "docxgen", "workspace.routing", "openxml","jszip-utils",  "FileSaver"], function(module, $, worker, DocxGen, routing,openXml){

	var init =  function (){


	startLoading();
	var config = module.config();

	var data = config.data;
	var html = config.html;
	var fileName = config.fileName;


    loadFile=function(url,callback){
        this.JSZipUtils.getBinaryContent(url,callback);
    };

    // need for IE9
    var inlineJszipUtilIE = function(){

		this.JSZipUtils._getBinaryFromXHR = function (xhr) {

		   	 return new Uint8Array(new VBArray(xhr.responseBody).toArray());
		};
    };

    // need for IE9
	var loadForBrowserWithoutWorker = function(){

		  loadFile(routing.buildURL("docxtemplate", config.namespace, config.viewIndx),function(err,content){


		    	doc=new DocxGen(content);

		    	doc.setData(data); //set the templateVariables
		    	doc.render(); //apply them (replace all occurences of {first_name} by Hipp, ...)


		    	output=doc.getZip().generate({type:"base64"}); //Output the document using Data-URI


		    	var docx = new openXml.OpenXmlPackage(output);

		    	for (var i = 0; i < html.length; i++){

		    	    var alt_chunk_id = "toto" + i;
		    		var alt_chunk_uri = "/word/" + i + ".html";
		    		// Add Alternative Format Import Part to document
		    		docx.addPart(alt_chunk_uri, "text/html", "base64", Base64.encode(html[i]));
		    		// Add Alternative Format Import Relationship to the document
		    		docx.mainDocumentPart().addRelationship(alt_chunk_id, openXml.relationshipTypes.alternativeFormatImport, alt_chunk_uri, "Internal");
	    	    }
	    		var theContent = docx.saveToBase64();

	    	    var url = routing.buildURL("ie9sucks");
	    	    var params = {"fileName":fileName, "b64":theContent};

                var form = $('<form method="POST" action="' + url + '">');
                $.each(params, function(k, v) {
                    form.append($('<input type="hidden" name="' + k +
                            '" value="' + v + '">'));
                });
                $('body').append(form);
                form.submit();

		});
	};

	function startLoading(){
		$("body").addClass("waiting-loading");
	}

	function stopLoading(){
		$("body").removeClass("waiting-loading");
	}


 	if (window.Worker){

	worker.onmessage = function(event) {
		console.log("getmsg from worker");
		var docx = new openXml.OpenXmlPackage(event.data);

	    for (var i = 0; i < html.length; i++){

	    var alt_chunk_id = "toto" + i;
		var alt_chunk_uri = "/word/" + i + ".html";
		// Add Alternative Format Import Part to document
		docx.addPart(alt_chunk_uri, "text/html", "base64", Base64.encode(html[i]));
		// Add Alternative Format Import Relationship to the document
		docx.mainDocumentPart().addRelationship(alt_chunk_id, openXml.relationshipTypes.alternativeFormatImport, alt_chunk_uri, "Internal");
	    }


	var theContent = docx.saveToBlob();
	saveAs(theContent,fileName + ".docx");


	stopLoading();
    };


        loadFile(routing.buildURL("docxtemplate", config.namespace, config.viewIndx),function(err,content){
        	console.log("send msg to worker");
            worker.postMessage([content, data, html]);
        });

   	} else {
		//damn IE9 specific code
   	    inlineJszipUtilIE();
   		loadForBrowserWithoutWorker();
		stopLoading();
	}
	}


    var Base64 = {

		// private property
		_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

		// public method for encoding
		encode : function (input) {
		    var output = "";
		    var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
		    var i = 0;

		    input = Base64._utf8_encode(input);

		    while (i < input.length) {

		        chr1 = input.charCodeAt(i++);
		        chr2 = input.charCodeAt(i++);
		        chr3 = input.charCodeAt(i++);

		        enc1 = chr1 >> 2;
		        enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
		        enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
		        enc4 = chr3 & 63;

		        if (isNaN(chr2)) {
		            enc3 = enc4 = 64;
		        } else if (isNaN(chr3)) {
		            enc4 = 64;
		        }

		        output = output +
		        this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
		        this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);

		    }

		    return output;
		},

		// public method for decoding
		decode : function (input) {
		    var output = "";
		    var chr1, chr2, chr3;
		    var enc1, enc2, enc3, enc4;
		    var i = 0;

		    input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

		    while (i < input.length) {

		        enc1 = this._keyStr.indexOf(input.charAt(i++));
		        enc2 = this._keyStr.indexOf(input.charAt(i++));
		        enc3 = this._keyStr.indexOf(input.charAt(i++));
		        enc4 = this._keyStr.indexOf(input.charAt(i++));

		        chr1 = (enc1 << 2) | (enc2 >> 4);
		        chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
		        chr3 = ((enc3 & 3) << 6) | enc4;

		        output = output + String.fromCharCode(chr1);

		        if (enc3 != 64) {
		            output = output + String.fromCharCode(chr2);
		        }
		        if (enc4 != 64) {
		            output = output + String.fromCharCode(chr3);
		        }

		    }

		    output = Base64._utf8_decode(output);

		    return output;

		},

		// private method for UTF-8 encoding
		_utf8_encode : function (string) {
		    string = string.replace(/\r\n/g,"\n");
		    var utftext = "";

		    for (var n = 0; n < string.length; n++) {

		        var c = string.charCodeAt(n);

		        if (c < 128) {
		            utftext += String.fromCharCode(c);
		        }
		        else if((c > 127) && (c < 2048)) {
		            utftext += String.fromCharCode((c >> 6) | 192);
		            utftext += String.fromCharCode((c & 63) | 128);
		        }
		        else {
		            utftext += String.fromCharCode((c >> 12) | 224);
		            utftext += String.fromCharCode(((c >> 6) & 63) | 128);
		            utftext += String.fromCharCode((c & 63) | 128);
		        }

		    }

		    return utftext;
		},

		// private method for UTF-8 decoding
		_utf8_decode : function (utftext) {
		    var string = "";
		    var i = 0;
		    var c = c1 = c2 = 0;

		    while ( i < utftext.length ) {

		        c = utftext.charCodeAt(i);

		        if (c < 128) {
		            string += String.fromCharCode(c);
		            i++;
		        }
		        else if((c > 191) && (c < 224)) {
		            c2 = utftext.charCodeAt(i+1);
		            string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
		            i += 2;
		        }
		        else {
		            c2 = utftext.charCodeAt(i+1);
		            c3 = utftext.charCodeAt(i+2);
		            string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
		            i += 3;
		        }

		    }

		    return string;
		}

    };



    return {init : init};

});

