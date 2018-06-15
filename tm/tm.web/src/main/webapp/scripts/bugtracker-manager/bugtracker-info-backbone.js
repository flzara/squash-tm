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
	'app/util/StringUtil', 'squash.translator', 'app/squash.handlebars.helpers'],
		function($, Backbone, _, Handlebars, notification, StringUtil, translator){

	// async load of the messages we need here
	translator.load(['bugtracker.admin.messages.testcreds.success', 
		'bugtracker.admin.messages.save.success', 
		'error.generic.label']);
	
	// ***************** Handlebars helpers ******************************
	
	function loadTemplate(id){
		return Handlebars.compile($(id).html());
	}

	// ***************** communication channel ***************************

	// todo : the radio has not much point after the refactor
	// ditch it at the next refactor
	var radio = $.extend({}, Backbone.Events);

	
	
	// ************************** Superclasses definition ******************************

	// --- model base classe -----
	var BaseModel = Backbone.Model.extend({
		
		// reads the value of a simple html input
		// and checks the value of data-bind html attribute
		// then store the attr: value in this model
		readInput : function(input){
			var $input = $(input);
			var attr = $input.data('bind');
			var val = $input.val();
			this.set(attr,val);
		}
		
	});
	
	
	// ----- base protocol-specific templated view -----
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

		//*** may be overridden by subclasses ***

		// allow for further customization of the model 
		// when the view initialize
		initModel: function(){
			
		}
		
	});
	
	
	// -------- base panel view ------------
	/*
	 * Base behavior : handle panels that have a message pane, 
	 * a templated part, a save reminer and a button pane.
	 * 
	 * Subclasses must define 'name' and several other methods,
	 * and their dom must follow the same conventions 
	 * (read bugtracker-info.jsp)
	 */
	var BasePanelView = Backbone.View.extend({
		
		// must be defined by subclasses
		name : "",
		
		$main: null,
		$saveReminder: null,
		$btnpane: null,
		$currTpl: null,
		
		initialize : function(options){			
			var name = this.name;
			if (StringUtil.isBlank(name)){
				throw "name undefined !";
			}
			
			// special rendering if there is a critical error :
			if (this.hasCriticalError(options)){
				this.$('.adm-srv-auth').children().not('.srv-auth-messagepane').remove();
			}
			
			// else normal rendering
			else{
				this.model = options.model;
				
				this.$main = this.$(".srv-auth-form-main");
				this.$saveReminder = this.$(".needs-save-msg");
				this.$btnpane = this.$(".srv-auth-buttonpane");				
				
				this._modelEvents();
				this.render();
			}
			
			// always initialize the message view and display messages in it if any
			new MessageView({
				el: this.$('.srv-auth-messagepane').get(),
				event : name
			});
			

			this.initialMessage(options);
			
			
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
			this.listenTo(this.model, 'change:protocol', this.resetMessages);
			this.listenTo(this.model, 'change:protocol', this.showSaveReminder);
			
			// also listen to changes in the model map
			var confModels = this.getModelMap();
			_.values(confModels).forEach(function(model){
				self.listenTo(model, 'change', self.showSaveReminder)
			});	
			
			// additional model events
			this.specificModelEvents();
		},
		
		hasCriticalError: function(options){
			return !! options.failureMessage;
		},
		
		initialMessage: function(options){
			if (!! options.failureMessage){
				radio.trigger(this.name, 'error', options.failureMessage);
			}
			else if (!! options.warningMessage){
				radio.trigger(this.name, 'failure', options.warningMessage);
			}
		},
		
		showSaveReminder : function(){
			this.$saveReminder.show();
		},
		
		hideSaveReminder : function(){
			this.$saveReminder.hide();
		},
		
		resetMessages : function(){
			radio.trigger(this.name, 'reset', '');
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
		
		preparePayload: function(){
			var authConfAttributes = this.getConfiguredModel().attributes;
			// add the type to hint Jackson at what to do
			authConfAttributes.type = this.model.get('protocol');
			return JSON.stringify(authConfAttributes);
		},
		
		ajax: function(conf){
			
			this.setAjaxMode();
			
			var self = this;
			
			return $.ajax(conf)
			.done(function(){
				self.$('.error-message').text('');
			})
			.fail(function(xhr){
				
				/*
				 * Server errors handling is a bit tricky here.
				 * We want every exception displayed in the message pane (instead of the default error dialog), 
				 * except field validation errors, for which we let the regular handler kick in (it will display 
				 * the error messages next to the offending fields).
				 * 
				 * The way we identify field validation errors is explained in function isFieldValidationError().
				 * 
				 * #workaround #fixthismessplease
				 */
				if (self.isFieldValidationError(xhr)){
					// let it flyyyyy
				}
				// else display in the panel
				else{
					// a shame that our current version of jquery doesn't support
					// deferred.catch()
					try{
						xhr.errorIsHandled = true;
						radio.trigger(self.name, 'failure', notification.getErrorMessage(xhr));
					}
					catch(damnit){
						radio.trigger(self.name, 'error', translator.get('error.generic.label'));
					}
				}
				return this;
			})
			.always(function(){
				self.unsetAjaxMode();
			});
		},
		
		/*
		 * The form validation error are all returned with status 412. However not all 412 yield a form validation error : 
		 * action exception are also 412 precondition failed, at the time I'm writing this. Plus, some other errors (that should 
		 * have been action exception) posture as form validation error but aren't really because they target no object and merely
		 * hold a message.
		 * 
		 * Thus, we identify genuine field validation errors if http status is 412, the response can be parsed as JSON and 
		 * there is an attribute 'fieldValidationError', and 'objectName' is not empty.
		 * 
		 * #workaround #fixthismessplease
		 */
		isFieldValidationError : function(xhr){
			var is412 = (xhr.status === 412);
			
			var hasFieldValidationError = false;
			var hasObjectName = false;
			try{
				var ex = JSON.parse(xhr.responseText);
				var hasFieldValidationError = (ex.fieldValidationErrors !== undefined);
				var hasObjectName = (_.find(ex.fieldValidationErrors, function(err){
					return ! StringUtil.isBlank(err.objectName);
				}) !== undefined);
			}
			catch(parseException){
				var hasFieldValidationError = false;
			}
			return (is412 && hasFieldValidationError && hasObjectName);
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
	

	
	// ************************ Main views ****************************


	// -------------- the main model ------------------
	
	var AuthenticationModel = Backbone.Model.extend({
		
		defaults : {
			btUrl : "",
			protocol: null,		// selected protocol
		
			// conf mapped by protocol
			// cannot be set in the defaults yet because the 
			// backbone models aren't defined yet
			authConfMap : {
				'BASIC_AUTH': null,
				'OAUTH_1A' : null
			},	
			policy : null,		// selected policy
			
			// app-level credentials mapped by protocol
			// cannot be set in the defaults yet because the 
			// backbone models aren't defined yet
			credentialsMap : {
				'BASIC_AUTH' : null,
				'OAUTH_1A': null
			}
		},
		
		initialize: function(){
			this.set('authConfMap', {
				'BASIC_AUTH': new Backbone.Model(),
				'OAUTH_1A' : new OAuthConfModel()				
			});
			this.set('credentialsMap', {
				'BASIC_AUTH' : new BasicAuthCredsModel(),
				'OAUTH_1A': new OAuthCredsModel()				
			});
			
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


	
	/*
	 * The main view merely handle interactions between ProtocolView and PolicyView
	 */
	var AuthenticationMasterView = Backbone.View.extend({

		el: "#bugtracker-authentication-masterpane",
		
		initialize : function(options){
			
			var model = this.initModel(options.conf);
			
			var conf = {
				model : model, 
				failureMessage: options.conf.failureMessage, 
				warningMessage: options.conf.warningMessage
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

	
	// ************ Protocol configuration section ***********
	
	// ---- protocol conf models -----------
	
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
	
	// ----- protocol conf views -----------------
	var OAuthConfView = BaseTemplatedView.extend({
		
		el: "#srv-auth-conf-form",
		
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
	
	// ------- main protocol panel view ---------
	var ProtocolView = BasePanelView.extend({
		
		el: "#bugtracker-auth-protocol",
		name: "srv-auth-conf",

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
			'change #srv-auth-proto-select' : 'updateProtocol',	
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
			var url = this.model.get('btUrl') + '/authentication-protocol/configuration';
			var payload = this.preparePayload();
			var self = this;
			
			this.ajax({
				type: 'POST',
				url : url,
				data : payload,
				contentType: 'application/json'
			})
			.done(function(){
				//rearm the unsaved data reminder and trigger success
				self.hideSaveReminder();
				radio.trigger('srv-auth-conf', 'success', translator.get('bugtracker.admin.messages.save.success'));
			});
		}
	
	
	});
	
	
	// ************ Policy section *****************************

	// -------- credentials configuration view -----------
	var BasicAuthCredentialsView = BaseTemplatedView.extend({

		el: '#srv-auth-creds-form',
		
		template: loadTemplate("#basic-creds-template")

	});

	var OAuthCredentialsView = BaseTemplatedView.extend({
		
		el: '#srv-auth-creds-form',
		
		template: loadTemplate("#oauth-creds-template")
		
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
	
	
	// ------- main policy panel view ----------------
	var PolicyView = BasePanelView.extend({

		el: "#bugtracker-auth-policy",
		name: "srv-auth-creds",
		
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
			'change input[name="srv-auth-policy"]' : 'updatePolicy'
		},
		
		specificModelEvents: function(){
			this.listenTo(this.model, 'change:policy', this.showSaveReminder);
			this.listenTo(this.model, 'change:policy', this.render);
		},
		
		render: function(){
			var policy = this.model.get('policy');
			if (policy === 'USER'){
				this.$main.hide();
			}
			else{
				BasePanelView.prototype.render.call(this);
			}
		},
		
		// ******* ajax **************
		
		updatePolicy: function(evt){
			var newPolicy=$(evt.currentTarget).val();
			
			var self = this;
			var url = this.model.get('btUrl') + '/authentication-policy';
			
			$.post(url, {value: newPolicy}).done(function(){
				// reset the message pane
				self.resetMessages();
				self.model.set('policy', newPolicy);
			});
			
		}, 
		
		_postCreds: function(url, payload){
			return this.ajax({
				type: 'POST',
				url: url,
				data: payload,
				contentType: 'application/json'
			});		
		},
		
		test: function(){
			var url = this.model.get('btUrl') + '/credentials/validator';
			var payload = this.preparePayload();
			
			this._postCreds(url, payload)
				.done(function(){
					radio.trigger('srv-auth-creds', 'success', translator.get('bugtracker.admin.messages.testcreds.success'));
				});
			
		},
		
		save: function(){
			var testUrl = this.model.get('btUrl') + '/credentials/validator';
			var saveUrl = this.model.get('btUrl') + '/credentials';
			var payload = this.preparePayload();
			var self = this;
			
			this._postCreds(testUrl, payload)
				.done(function(){
					return self._postCreds(saveUrl, payload);
				})
				.done(function(){
					//rearm the unsaved data reminder and trigger success
					self.hideSaveReminder();
					radio.trigger('srv-auth-creds', 'success',  translator.get('bugtracker.admin.messages.save.success'));					
				});
		}
		
		
	});
	
	
	// ****************** Message view *****************
	
	/*
	 * Listens to the radio for events and display messages with an accompanying icon.
	 * 
	 * Actually it waits only one event, and the arguments that comes along 
	 * will actually decide of what is displayed.
	 * 
	 * Arguments :  
	 * 1/ success | failure | error | reset, 
	 * 2/ the message
	 * 
	 * options: {
	 * 	el: its javascript dom element
	 * 	event: the name of the do-it-all event it must listens to.
	 * }
	 */
	var MessageView = Backbone.View.extend({
		
		// maps status to icons
		iconMap: {
			'success': 	'generic-info-signal',
			'failure': 	'generic-warning-signal',
			'error':	'generic-error-signal', 
			'reset':	''
		},

		initialize : function(options){
			var event = options.event; 
			this.listenTo(radio, event, this.displayMessage);
			this.$el.html(loadTemplate('#messagepane-template'));
		},
		
		/*
		disable : function(){
			this.$el.addClass('disabled-transparent');
		},

		enable : function(){
			this.$el.removeClass('disabled-transparent');
		},
		*/
		
		displayMessage : function(status, msg){
			var icon = this.iconMap[status];
			this.$('.generic-signal').prop('class', 'generic-signal '+icon);
			this.$('.txt-message').text(msg);
		}

	});
	

	// ********************* Returned object *********************

	return AuthenticationMasterView;


});
