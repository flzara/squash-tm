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
 * Handles the authentication options for this bugtracker. The overall layout is as follow : 
 * 
 * AuthenticationMasterView
 * 	+ ProtocolView
 * 	|	+ (own content)
 * 	|	+ ProtocolConfView
 * 	|	- ProtocolMessageView
 * 	+ PolicyView
 * 		+ (own content)
 * 		+ AppLevelCredsView
 * 		- PolicyMessageView
 * 
 * ProtocolConfView and AppLevelCredsView partially rely on Handlebars, but most of the html here is server-generated 
 * and pre-rendered whenever possible. 
 * 
 * 1/ Configuration
 * 
 *	{
 *		btUrl : the bugtracker url
 *		authPolicy :  either 'USER' and 'APP_LEVEL'
 *		availableProtos : the list of available authentication protocols
 *		selectedProto : the selected protocol chosen among the above
 *		failureMessage : error message if an unrecoverable error was encountered
 *		warningMessage: error message if a recoverable error was encountered
 *		conf : the conf object, which is variable. See the various ConfView implementations. 
 *		credentials : the (app-level) credential object, which is variable. See the various CredentialsView implementations.
 *	}
 *
 *
 * 2/ Flow
 * 
 * The main model is common to AuthenticationMasterView, ProtocolView and PolicyView. 
 * 
 * The views that manage the templated parts (the server conf and server credentials) will receive their own model instead.
 * 
 * UI event -> xhr update -> model update -> redraw() 
 * 
 * 
 * 3/ Managing the Authentication Protocol
 * 
 * Updating the protocol has very fundamental effects : it triggers a redraw on both views, and on the server every 
 * information (conf and credentials) related to the formerly selected protocol are wiped. The subview must account 
 * for that and notify the user when the data needs to be resaved. 
 * 
 * 
 */
define(['jquery', 'backbone', 'underscore', 'handlebars', 'app/ws/squashtm.notification', 
	'app/util/StringUtil', 'app/squash.handlebars.helpers'],
		function($, Backbone, _, Handlebars, notification, StringUtil){

	// ***************** Handlebars helpers ******************************
	
	function loadTemplate(id){
		return Handlebars.compile($(id).html());
	}

	// ***************** communication channel ***************************

	var radio = $.extend({}, Backbone.Events);

	// ************************** The models ******************************


	
	// -------------- conf model --------------
	
	var OAuthConfModel = Backbone.Model.extend({

	
		defaults : {
			consumerKey: "",
			requestTokenHttpMethod: 'GET',
			accessTokenHttpMethod: 'GET',
			clientSecret: "",
			signatureMethod: 'HMAC_SHA1', 
			requestTokenUrl: "", 
			accessTokenUrl: "",
			userAuthorizationUrl: ""
		}
	});

	// -------------- the main model ------------------
	
	var AuthenticationModel = Backbone.Model.extend({
		
		defaults : {
			btUrl : "",
			protocol: null,		// selected protocol
			// conf mapped by protocol
			authConfMap : {
				'BASIC_AUTH': new Backbone.Model(),
				'OAUTH_1A' : new OAuthConfModel()
			},	
			policy : null,		// selected policy
			// app-level credentials mapped by protocol,
			credentialsMap : {
				'BASIC_AUTH' : new Backbone.Model(),
				'OAUTH_1A': new Backbone.Model()
			}
		}
	});
	
	
	// ************************ Main views ****************************

	/*
	 * The main view merely handle interactions between ProtocolView and PolicyView
	 */
	var AuthenticationMasterView = Backbone.View.extend({

		el: "#bugtracker-authentication-masterpane",
		
		// set by initialize()
		$protoPane: null,
		$policyPane: null,
		model: null,
		btUrl: null,
		
		initialize : function(options){
			
			this.initModel(options.conf);
			this.btUrl = options.btUrl;
			
			var conf = {
				model : this.model
			}
			
			this.$protoPane = new ProtocolView(conf);
			this.$policyPane = new PolicyView(conf);
			
		},
		
		initModel : function(mdl){
			var proto = mdl.selectedProto;
			this.model = new AuthenticationModel({
				btUrl : mdl.btUrl,
				protocol: mdl.selectedProto,
				policy: mdl.authPolicy,
			});
			
			this.model.get('authConfMap')[proto].set(mdl.authConf);
			this.model.get('credentialsMap')[proto].set(mdl.credentials);
		}
		
	});


	
	var ProtocolView = Backbone.View.extend({
		el: "#bugtracker-auth-protocol",
		
		// flag regarding the display of the 'data unsaved' warning
		saved: true,
		
		$main: null,
		$form: null,
		
		initialize : function(options){
			
			this.model = options.model;
			this.btUrl = options.btUrl;
			
			this.$main = this.$("#bt-auth-conf-main");
			this.$form = this.$("#bt-auth-conf-form");
			
			new MessageView({
				el: "#bt-auth-conf-messagezone",
				evtPrefix : "bt-auth-conf"
			});
			
			this.bindModelEvents();
			
			this.render();
		},
		
		render : function(){
			var proto = this.model.get('protocol'),
				conf = this.model.get('authConfMap')[proto];
			
			switch(proto){
			case 'BASIC_AUTH':
				// basic auth has no conf
				this.$form.empty();
				this.$main.hide();
				break;
				
			case 'OAUTH_1A':
				// TODO: render the form
				this.$form.empty();
				this.$main.show();
				var confModel = this.getCurrentConf();
				new OAuthConfView({model: confModel});
				break;
			}
			
		},
		
		events : {
			'change #bt-auth-proto-select' : 'updateProtocol',
			'change input' : 'updateModel',
			'change select' : 'updateModel',
			'change textarea' : 'updateModel',
			'click #bt-auth-conf-save' : 'save'
		},
		
		bindModelEvents : function(){
			this.listenTo(this.model, 'change:protocol', this.render);
		},

		updateProtocol : function(evt){
			var val = $(evt.target).val();
			var self = this;
			var url = this.model.get('btUrl') + '/authentication-protocol';
			
			$.post(url, {value: val}).done(function(){
				self.model.set('protocol', val);
			});
		}, 
		
		getCurrentConf : function(){
			var proto = this.model.get('protocol');
			var confMap = this.model.get('authConfMap');
			return confMap[proto];
		},
		
		updateModel : function(evt){
			var inp = $(evt.currentTarget);
			var attr = inp.data('bind');
			var val = inp.val();
			var confModel = this.getCurrentConf();
			confModel.set(attr,val);
		},
		
		save : function(){
			var url = this.model.get('btUrl') + '/authentication-configuration';
			var authConfAttributes = this.getCurrentConf().attributes;
			// add the type to hint Jackson at what to do
			authConfAttributes.type = this.model.get('protocol');
			var payload = JSON.stringify(authConfAttributes);
			$.ajax({
				type: 'POST',
				url : url,
				data : payload,
				contentType: 'application/json'
			});
		}
	
	
	});
	
	var PolicyView = Backbone.View.extend({
		el: "#bugtracker-auth-policy",
		
		initialize : function(options){
			this.model = options.model;
			new MessageView({
				el: "#bt-auth-creds-messagezone",
				evtPrefix : "bt-auth-creds"
			})
		}
		
	});
	

	var MessageView = Backbone.View.extend({

		$failPane : null,
		$warnPane : null,
		$succPane : null,
		$saveSuccPane : null,


		initialize : function(options){
			this.$failPane = $("#bt-auth-failure");
			this.$warnPane = $("#bt-auth-warning");
			this.$succPane = $("#bt-auth-info");
			this.$saveSuccPane = $("#bt-auth-save-info");

			var prefix = options.evtPrefix; 
			
			this.listenTo(radio, prefix+'-success', this.showSuccess);
			this.listenTo(radio, prefix+'-save-success', this.showSaveSuccess);
			this.listenTo(radio, prefix+'-warning', this.showWarning);
			this.listenTo(radio, prefix+'-failure', this.showFailure);
		},

		disable : function(){
			this.$el.addClass('disabled-transparent');
		},

		enable : function(){
			this.$el.removeClass('disabled-transparent');
		},

		allPanes : function(){
			return [this.$failPane, this.$warnPane, this.$succPane, this.$saveSuccPane];
		},

		showPane : function(paneName){
			_.without(this.allPanes(), this[paneName])
			.forEach(function(pane){
				pane.addClass('not-displayed');
			});
			this[paneName].removeClass('not-displayed');
		},

		showSuccess : function(){
			this.showPane('$succPane');
		},

		showSaveSuccess : function(){
			this.showPane('$saveSuccPane');
		},

		showWarning : function(msg){
			this.$warnPane.find('.generic-warning-main').text(msg);
			this.showPane('$warnPane');
		},

		showFailure : function(msg){
			this.$failPane.find('.generic-warning-main').text(msg);
			this.showPane('$failPane');
		}
	});
	

	// *************** Authentication conf views *****************************
	
	/*
	 * The model will have the same attributes than the java bean ServerOAuth1aConsumerConf  
	 */
	var OAuthConfView = Backbone.View.extend({
		
		el: "#bt-auth-conf-form",
		
		template: loadTemplate("#oauth-conf-template"),
		
		initialize : function(options){
			this.model = options.model;
			this.initModel();
			this.render();
		},
		
		// by 'init', we mean initialization of empty fields
		initModel : function(){
			// MAAAAGIIIC REAAACH OF REMOTE UNRELATED PROPERTYYYYY ELSEWHERE IN THE DOOOOM !			
			var baseUrl = $("#bugtracker-url").text();
			var model = this.model;
			var self = this;
			['requestTokenUrl', 'userAuthorizationUrl', 'accessTokenUrl'].forEach(function(url){
				if ( self.isBlank(url) ){
					model.set(url, baseUrl);
				}
			});
			
		},
		
		
		isBlank : function(ppt){
			return StringUtil.isBlank(this.model.get(ppt));
		},
		
		render : function(){
			this.$el.empty();
			this.$el.html(this.template(this.model.attributes));
		}, 
		
		events : {
			
		}
		
		
		
	});
	
	// ************ Application-Level credentials *****************************

	// BasicAuthCredentialsView
	var BasicAuthCredentialsView = Backbone.View.extend({

		el: '#bt-auth-creds-form',

		events: {
			'change #bt-auth-basic-login' : 'setUsername',
			'change #bt-auth-basic-pwd' : 'setPassword'
		},

		initialize : function(options){
			// deal with the case of undefined model
			if (_.isEmpty(options.model.attributes)){
				options.model.set('username', '');
				options.model.set('password', '');
			}
		},

		template: Handlebars.compile(
			'<div class="display-table">' +
				'<div class="display-table-row" style="line-height:3.5">' +
					'<label class="display-table-cell">{{i18n "label.Login"}}</label>' +
					'<input id="bt-auth-basic-login" type="text" class="display-table-cell" value="{{this.username}}">' +
				'</div>' +
				'<div class="display-table-row" style="line-height:3.5">' +
					'<label class="display-table-cell">{{i18n "label.Password"}}</label> ' +
					'<input id="bt-auth-basic-pwd" class="display-table-cell" type="password" value="{{this.password}}"> ' +
				'</div>' +
			'</div>'
		),

		render: function(){
			this.$el.html(this.template(this.model.attributes));
			return this;
		},

		enable: function(){
			this.$el.find('input').prop('disabled', false);
		},

		disable: function(){
			this.$el.find('input').prop('disabled', true);
		},

		setUsername: function(evt){
			this.model.set('username', evt.target.value);
		},

		setPassword: function(evt){
			this.model.set('password', evt.target.value);
		}


	});

	

	// ************ message pane *************************



	return AuthenticationMasterView;


});
