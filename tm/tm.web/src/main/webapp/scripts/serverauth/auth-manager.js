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
 * 
 * This module helps managing the user credentials for the third parties with which Squash TM interacts, for 
 * instance bugtrackers that require login/password authentication.  
 * 
 * API : 
 * ---------------------
 * 
 * get({
 * 	serverId : mandatory. the id of the server we want to check authentication.
 * 	status : optional, default to 'NON_AUTHENTICATED'. A string value of java enum AuthenticationStatus.
 * 			If set, will override the current status of the manager with that new value.
 * }) 
 * 
 * => returns an instance of an AuthenticationManager for the given server (see below)
 * 
 * get(serverId) => same than above, but only specifies the server id 
 * 
 * 
 * AuthenticationManager API
 * -----------------------
 * 
 * authenticate() => checks whether the used is authenticated to the server it manages, and challenges the 
 * user if he is not. 
 * 
 * The method returns a JQuery promise. If the used is authenticated, or becomes authenticated after challenge, 
 * the promise will be resolved, otherwise it will be rejected.
 * 
 * AuthenticationManager events 
 * ----------------------
 * 
 * authmanager.authentication => thrown when a used authenticate successfully for the first time. The manager itself
 * 								is passed as argument.
 * 
 */
define(['jquery', 'handlebars', 'workspace.routing',
        "text!./auth-dialog.html!strip", 'squash.translator' , 
        'app/ws/squashtm.notification',
        'workspace.event-bus',
        'jquery.squash.formdialog'], 
        function($, Handlebars, routing, template, translator, notification, eventBus){
	
	// classic global variables initialisation
	window.squashtm = window.squashtm || {};
	window.squashtm.workspace = window.squashtm.workspace || {};
	window.squashtm.workspace.authmanagers = window.squashtm.workspace.authmanagers || {};
	
	// *********** utility code ***************
	
	
	translator.load([
	    "label.LogIn",
	    "label.Login",
	    "label.password",
	    "label.Ok",
	    "label.Cancel"
	]);
	
	function cleanup(dialog){
		if (dialog.data()['formDialog'] !== undefined){
			dialog.formDialog('destroy');
		}
		dialog.remove();	
	}
	
	
	function invokeLoginDialog(manager, deferred){
		
		
		var lang = translator.get({
			title : 'label.LogIn',
			username : 'label.Login',
			password : 'label.password',
			confirm : 'label.Ok',
			cancel : 'label.Cancel'
		});
		
		// safety check : check that no other login dialog has been left by mistake
		var d = $("#login-dialog");
		if (d.length > 0){
			cleanup(d);
		}
		
		// now we can build a fresh one
		var dHtml= (Handlebars.compile(template))(lang);
		$('body').append(dHtml);
		var dialog = $("#login-dialog");
		
		dialog.formDialog({
			width : 300
		});
		
		var hasResolved = false;
		
		dialog.on('formdialogconfirm', function(){
	        var login = $("#login-dialog-login").val();
	        var password = $("#login-dialog-password").val();
	        var url = routing.buildURL('servers.authentication', manager.serverId);
	        
	        var credentials = {
	        	type : 'BASIC_AUTH',
	        	username : login,
	        	password : password
	        };
	        
	        $.ajax({
	          url : url,
	          type : 'POST',
	          data : JSON.stringify(credentials),
	          contentType : 'application/json'
	        })
	        .success(function(){
		      hasResolved = true;
	          dialog.formDialog('close');
	          manager.status = "AUTHENTICATED";
	          eventBus.trigger('authmanager.authentication', manager);
	          deferred.resolve();
	        })
	        .error(function(xhr){
	        	manager.status = "NON_AUTHENTICATED";
	        	xhr.errorIsHandled = true;
	        	var error = notification.getErrorMessage(xhr);
	        	dialog.formDialog('showError', error);
	        });
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
		dialog.on('formdialogclose', function(){
			if (! hasResolved){
				deferred.reject();
			}
			cleanup(dialog);
		});
		
		dialog.formDialog('open');
				
	}
	
	// *************** AuthenticationManager ***************
	
	function AuthenticationManager(conf){
		this.serverId = conf.serverId;
		this.status = conf.status || "NON_AUTHENTICATED";
	}
	
	AuthenticationManager.prototype.authenticate = function(){
		
		var manager = this;
		
		var defer = $.Deferred();
		
		// no server -> nothing to do
		if (this.status === "UNDEFINED"){
			console.log("authentication manager : server '"+this.serverId+"' is undefined");
			defer.reject();
		}
		
		// user is authenticated -> proceed 
		else if (this.status === "AUTHENTICATED"){
			defer.resolve();
		}
		
		// it seems that the user needs to authenticate. 
		else{
			// let's check again that the user really do need authentication
			var chkUrl = routing.buildURL('servers.authentication', this.serverId);
			$.getJSON(chkUrl)
			.success(function(jsonStatus){
				
				switch(jsonStatus){
				case "UNDEFINED" :
					defer.reject();
					break;
				case "AUTHENTICATED" : 
					defer.resolve();
					break;
					
				case "NON_AUTHENTICATED" :
					invokeLoginDialog(manager, defer);
					break;
				}
				
			});
			
		}
		
		return defer.promise();
	}
	
	// *************** main **********************
	
	return	{
		get : function(arg){
			
			// normalize the configuration
			var conf = null;
			if (isNaN(arg)){
				conf = arg;	// arg is an object
			}
			else{
				// arg is an int
				conf = {
					serverId : arg
				}
			}
			
			// find or create the instance of authmanager for this serverid
			var serverId = conf.serverId;
			var manag = window.squashtm.workspace.authmanagers[serverId];
			if (manag === undefined){
				manag = window.squashtm.workspace.authmanagers[serverId] = new AuthenticationManager(conf);
			}
			
			// update the status if requested
			manag.status = conf.status || manag.status;
			
			return manag;
		}
		
	};
	
});