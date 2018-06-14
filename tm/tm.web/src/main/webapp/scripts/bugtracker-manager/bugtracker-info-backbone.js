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

	// base class for the others
	var BaseModel = Backbone.Model.extend({
		
		readInput : function(input){
			var $input = $(input);
			var attr = $input.data('bind');
			var val = $input.val();
			this.set(attr,val);
		}
		
	});
	
	// -------------- conf model --------------
	
	var OAuthConfModel = BaseModel.extend({
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
	
	// -------------- credentials models --------------
	
	var BasicAuthCredsModel = BaseModel.extend({
		defaults: {
			username: "",
			password: ""
		}
	});
	
	var OAuthCredsModel = BaseModel.extend({
		defaults: {
			token: "",
			tokenSecret: ""
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
		},
		
		currentConf : function(){
			var proto = this.attributes.protocol;
			return this.attributes.authConfMap[proto];
		},
		
		currentCreds : function(){
			var proto = this.attributes.protocol;
			return this.attributes.credentialsMap[proto];
		}
	});



	// *************** Authentication conf views *****************************
	
	var BaseTemplatedView = Backbone.View.extend({
		
		events: {
			'change input' : 'updateModel',
			'change select' : 'updateModel',
			'change textarea' : 'updateModel',			
		},
		
		initialize : function(options){
			this.model = options.model;
			this.initModel();
			this.render();
		},
		
		
		updateModel : function(evt){
			this.model.readInput(evt.currentTarget);
		},
		
		render : function(){
			this.$el.empty();
			this.$el.html(this.template(this.model.attributes));
		},
		
		enable: function(){
			this.$el.find('input').prop('disabled', false);
		},

		disable: function(){
			this.$el.find('input').prop('disabled', true);
		},

		//*** may be overriden by subclasses ***

		initModel: function(){
			
		}
		
	});
	
	/*
	 * The model will have the same attributes than the java bean ServerOAuth1aConsumerConf  
	 */
	var OAuthConfView = BaseTemplatedView.extend({
		
		el: "#bt-auth-conf-form",
		
		template: loadTemplate("#oauth-conf-template"),

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
		}

		
	});
	
	// ************ Application-Level credentials *****************************


	var BasicAuthCredentialsView = BaseTemplatedView.extend({

		el: '#bt-auth-creds-form',
		
		template: loadTemplate("#basic-creds-template")

	});

	var OAuthCredentialsView = BaseTemplatedView.extend({
		
		el: '#bt-auth-creds-form',
		
		template: loadTemplate("#oauth-creds-template")
		
	});
	
	
	// ************************ Main views ****************************

	/*
	 * The main view merely handle interactions between ProtocolView and PolicyView
	 */
	var AuthenticationMasterView = Backbone.View.extend({

		el: "#bugtracker-authentication-masterpane",
		
		initialize : function(options){
			
			var model = this.initModel(options.conf);
			
			var conf = {
				model : model
			}
			
			new ProtocolView(conf);
			new PolicyView(conf);
			
		},
		
		initModel : function(mdl){
			var model = new AuthenticationModel({
				btUrl : mdl.btUrl,
				protocol: mdl.selectedProto,
				policy: mdl.authPolicy,
			});
			
			// set the attributes of the current conf and credentials
			model.currentConf().set(mdl.authConf);
			model.currentCreds().set(mdl.credentials);
			
			return model;
		}
		
	});

	// ************************ Panel views ************************
	
	/*
	 * Base behavior : handle panels that have a message pane, 
	 * a templated part, a save reminer and a button pane.
	 * 
	 * Subclasses must define 'prefix' and several other methods,
	 * and their dom must follow the same conventions 
	 * (read bugtracker-info.jsp)
	 */
	var BasePanelView = Backbone.View.extend({
		
		// must be defined by subclasses
		prefix : "",
		
		$main: null,
		$saveReminder: null,
		$btnpane: null,
		$currTpl: null,
		
		initialize : function(options){
			var prefix = this.prefix;
			if (StringUtil.isBlank(prefix)){
				throw "prefix undefined !";
			}
			
			this.model = options.model;
			
			this.$main = this.$("#"+prefix+"-main");
			this.$saveReminder = this.$(".needs-save-msg");
			this.$btnpane = this.$("#"+prefix+"-buttonpane");
			
			new MessageView({
				el: "#"+prefix+"-messagezone",
				evtPrefix : prefix
			});
			
			this._modelEvents();
			this.render();
			
		},
		
		render: function(){
			var protocol = this.model.get('protocol');
			var View = this.viewMap[protocol];			
			var tplModel = this.getConfiguredModel();
			
			if (View == null){
				this.$main.hide();
			}
			else{
				this.$main.show();
				this.$currTpl = new View({model: tplModel});
			}	
		},
		
		
		_modelEvents : function(){
			var self = this;
			this.listenTo(this.model, 'change:protocol', this.render);
			this.listenTo(this.model, 'change:protocol', this.showSaveReminder);
			
			// also listen to changes in the model map
			var confModels = this.getModelMap();
			_.values(confModels).forEach(function(model){
				self.listenTo(model, 'change', self.showSaveReminder)
			});	
			
			// additional model events
			this.specificModelEvents();
		},
		
		showSaveReminder : function(){
			this.$saveReminder.show();
		},
		
		hideSaveReminder : function(){
			this.$saveReminder.hide();
		},
		
		setAjaxMode : function(){
			this.$btnpane.children().css('visibility', 'hidden');
			this.$btnpane.addClass('waiting-loading');
		},

		unsetAjaxMode : function(){
			this.$btnpane.children().css('visibility', 'visible');
			this.$btnpane.removeClass('waiting-loading');
		},

		updateModel : function(evt){
			this.getConfiguredModel().readInput(evt.currentTarget);
		},
		
		getCurrentTemplate: function(){
			return this.$currTpl;
		},
		
		ajax: function(conf){
			
			this.setAjaxMode();
			
			var self = this;
			
			return $.ajax(conf)
			.done(function(){
				//rearm the unsaved data reminder and trigger success
				self.hideSaveReminder();
				self.$('.error-message').text('');
				radio.trigger('bt-auth-conf-save-success');
			})
			.fail(function(xhr){
				// let go validation errors : they are caught and handled by
				// the notification listener
				// for others, display them in the pane
				if (xhr.status !== 412){
					xhr.errorIsHandled = true;
					radio.trigger(self.prefix+'-warning', notification.getErrorMessage(xhr));
				}
			})
			.always(function(){
				self.unsetAjaxMode();
			});
		},
		
		// *********** subclasses may/must override theses ***********
		
		getModelMap: function(){
			throw "not implemeted !";
		},
		
		getConfiguredModel : function(){
			throw "not implemeted !";
		},
		
		specificEvents: function(){
			return {};
		}, 
		
		specificModelEvents: function(){
			//noop
		},
		
		// templated view constructor mapped by protocol
		// or null if none defined for that protocol
		viewMap : {
			'BASIC_AUTH': null,
			'OAUTH_1A': null
		}
		
	});
	
	
	var ProtocolView = BasePanelView.extend({
		
		el: "#bugtracker-auth-protocol",
		prefix: "bt-auth-conf",

		viewMap: {
			'BASIC_AUTH': null,
			'OAUTH_1A': OAuthConfView
		},
	
		
		getModelMap: function(){
			return this.model.get('authConfMap');
		},
		
		getConfiguredModel : function(){
			return this.model.currentConf();
		},
		
		events: {
			'change #bt-auth-proto-select' : 'updateProtocol',	
			'click 	.auth-save' : 'save'
		},
		

		// ************* ajax *******************
		
		updateProtocol : function(evt){
			var val = $(evt.target).val();
			var self = this;
			var url = this.model.get('btUrl') + '/authentication-protocol';
			
			$.post(url, {value: val}).done(function(){
				self.model.set('protocol', val);
			});
		}, 
		
		
		save : function(){
			var url = this.model.get('btUrl') + '/authentication-configuration';
			
			var authConfAttributes = this.getConfiguredModel().attributes;
			// add the type to hint Jackson at what to do
			authConfAttributes.type = this.model.get('protocol');
			var payload = JSON.stringify(authConfAttributes);
			
			var self = this;
			
			this.ajax({
				type: 'POST',
				url : url,
				data : payload,
				contentType: 'application/json'
			})
			.done(function(){
				radio.trigger('bt-auth-conf-save-success');
			});
		}
	
	
	});
	
	var PolicyView = BasePanelView.extend({

		el: "#bugtracker-auth-policy",
		prefix: "bt-auth-creds",
		
		viewMap: {
			'BASIC_AUTH': BasicAuthCredentialsView,
			'OAUTH_1A': OAuthCredentialsView
		},
	
		
		getModelMap: function(){
			return this.model.get('credentialsMap');
		},
		
		getConfiguredModel : function(){
			return this.model.currentCreds();
		},
		
		events: {
			'click .auth-test' : 'test',
			'click .auth-save' : 'save',
			'change input[name="bt-auth-policty"]' : 'updatePolicy'
		},
		
		// ******* ajax **************
		
		updatePolicy: function(){
			alert("TODOOO");
		}
		
	});
	
	
	// ****************** Message view *****************
	
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
	


	return AuthenticationMasterView;


});
