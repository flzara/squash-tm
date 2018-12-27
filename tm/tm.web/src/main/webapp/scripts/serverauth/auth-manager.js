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
 * 	serverId : mandatory. The id of the server we want to check authentication.
 *  protocol : mandatory. The authentication protocol that must be used for that server.
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
 * authenticate() => checks whether the user is authenticated to the server it manages, and challenges the
 * user if he is not.
 *
 * The method returns a JQuery promise. If the user is authenticated, or becomes authenticated after challenge,
 * the promise will be resolved, otherwise it will be rejected.
 *
 * AuthenticationManager events
 * ----------------------
 *
 * authmanager.authentication => triggered when a used authenticate successfully for the first time. The manager itself
 * 								is passed as argument.
 *
 */
define(['jquery', 'handlebars', 'workspace.routing',
        "text!./basic-auth-dialog.html!strip", 'squash.translator' ,
        'app/ws/squashtm.notification',
        'workspace.event-bus',
        'jquery.squash.formdialog'],
        function($, Handlebars, routing, basauthTemplate, translator, notification, eventBus){

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


	function doAuthenticate(manager, deferred){
		switch(manager.protocol){
		case "BASIC_AUTH":
			invokeLoginDialog(manager, deferred);
			break;
		case "OAUTH_1A":
			commenceOauthTokensExchange(manager, deferred);
			break;
		}
	}


	/*
	 * Basic Auth form dialog.
	 *
	 * Will open basic authentication form dialog. The dialog will let the user try a username/password that will be submitted
	 * to the server for validation. In case of failure the user is allowed to try again with different credentials
	 * (typo in passwords etc), until he has authenticated successfully or aborted by closing the dialog.
	 *
	 * Each authentication attempts will test the credentials on the server, which will then reply with HTTP 2XX (success) or
	 * a HTTP 4XX/5XX (failure). A XHR success will automatically close the dialog and resolve the deferred. XHR failure
	 * will display the error message and let the user try again. If the dialog closes and the user is still not authenticated
	 * the deferred will be rejected.
	 */
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
		var dHtml= (Handlebars.compile(basauthTemplate))(lang);
		$('body').append(dHtml);
		var dialog = $("#login-dialog");

		dialog.formDialog({
			width : 300
		});

		// Issue 7520 - CKEDITOR with SCAYT autoStartup makes these pop-up inputs too big
		$("#login-dialog-login").css({'line-height':'0','margin-bottom':'0.3em'});
		$("#login-dialog-password").css('line-height', 0);

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


	/*
	 * OAuth1A authentication
	 *
	 * The oauth 1a authentication will happen in a separate window. On user authentication success the window will call the main
	 * window back for deferred resolution.
	 *
	 * The deferred will be stored in the main window context under the property 'squashtm.workspace.authmanagers.oauthdeferred'.
	 * The pages served by Squash TM in that separate window can interact with it, see pages in WEB-INF/templates/servers/oauth1a-*.html
	 *
	 */
	function commenceOauthTokensExchange(manager, deferred){

		// store the deferred
		squashtm.workspace.authmanagers.oauthdeferred = deferred;

		// begin token exchanges
		var oauthUrl = routing.buildURL('servers.authentication.oauth1a', manager.serverId);
		window.open(oauthUrl, 'OAuth authorizations', 'height=690, width=810, resizable, scrollbars, dialog, alwaysRaised');

	}


	// *************** AuthenticationManager ***************

	function AuthenticationManager(conf){
		this.serverId = conf.serverId;
		this.protocol = conf.protocol;
		this.status = conf.status || "NON_AUTHENTICATED";
	}

	AuthenticationManager.prototype.authenticate = function(){

		var manager = this;

		var defer = $.Deferred();

		// first, we want to make sure that the deferred fire the event 'authmanager.authentication'
		// when resolved
		defer.done(function(){
	          eventBus.trigger('authmanager.authentication', manager);
		});

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
					doAuthenticate(manager, defer);
					break;
				}

			});

		}

		return defer.promise();
	};

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
				};
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
