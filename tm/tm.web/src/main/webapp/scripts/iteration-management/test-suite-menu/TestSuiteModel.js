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
/* ********************
This object somewhat implements a distributed MVC. Locally, within the contextual content, it represents the model. However, when modifications happen it must warn it's listeners, but also
the object squashtm.workspace.contextualContent. It will relay the information to other models (ie, the tree).

As such a model object is both master (of its listeners) and slave (of the contextual content).

 ********************* */

define([ "jquery", "workspace.event-bus", "jqueryui" ], function($, eventBus) {

	function TestSuiteModel(settings) {

		this.createUrl = settings.createUrl;
		this.baseUpdateUrl = settings.baseUpdateUrl;
		this.getUrl = settings.getUrl;
		this.removeUrl = settings.removeUrl;
		this.eventBus = eventBus;

		if (settings.initData !== undefined) {
			this.data = settings.initData;
		} else {
			this.data = [];
		}

		this.views = [];

		var self = this;

		/* ************** private ************* */

		// we have to re-implement indexOf because IE8 doesn't support it
		// returns -1 if not found
		var indexById = $.proxy(function(id) {
			for ( var i = 0; i < this.data.length; i++) {
				if (this.data[i].id == id) {
					return i;
				}
			}
			return -1;
		}, self);

		var renameSuite = $.proxy(function(json) {
			var index = indexById(json.id);
			if (index != -1) {
				this.data[index].name = json.name;
			}
		}, self);

		var removeSuites = $.proxy(function(commands) {
			var removed = commands.removed;
			for ( var i=0,len=removed.length; i<len; i++ ) {
				var index = indexById(removed[i].resid);
				if (index != -1) {
					this.data.splice(index, 1);
				}
			}
		}, self);

		var _getModel = function() {
			return $.ajax({
				'url' : self.getUrl,
				type : 'GET',
				dataType : 'json'
			}).success(function(json) {
				self.data = json;
			});
		};

		var redrawViews = $.proxy(function(evt) {
			for ( var i = 0; i < this.views.length; i++) {
				this.views[i].redraw(evt);
			}
		}, self);


		/* ************** public interface (master) *************** */

		this.addView = function(view) {
			this.views.push(view);
		};

		this.getData = function() {
			return this.data;
		};

		this.postNew = function(name) {

			return $.ajax({
				'url' : self.createUrl,
				type : 'POST',
				data : {
					'name' : name
				},
				dataType : 'json'
			}).success(function(json) {
				self.data.push(json);
				eventBus.trigger("node.add", json);
			});
		};

		this.postRename = function(toSend) {

			var url = this.baseUpdateUrl + "/" + toSend.id + "/rename";

			return $.ajax({
				'url' : url,
				type : 'POST',
				data : toSend,
				dataType : 'json'
			}).success(function(json) {
				renameSuite(json);
				var id = {
					resid : toSend.id,
					restype : "test-suites"
				};
				eventBus.trigger("node.rename", {identity : id, newName : toSend.name});
			});
		};

		this.postRemove = function(toSend) {

			var url = this.removeUrl;

			return $.ajax({
				'url' : url,
				type : 'POST',
				data : toSend,
				dataType : 'json'
			}).success(function(json) {
				removeSuites(json);
				eventBus.trigger("node.remove");
			});
		};

		this.postBind = function(toSend) {
			var url = this.baseUpdateUrl + '/' + toSend['test-suites'].join(',') + "/test-plan/";

			return $.ajax({
				'url' : url,
				type : 'POST',
				data :  { 'itemIds[]' : toSend['test-plan-items']},
				dataType : 'json'
			}).success(function(json) {
				eventBus.trigger("node.bind");
			});
		};

		this.postBindChanged = function(toSend){
			return $.ajax({
				'url': this.baseUpdateUrl+"/test-plan/",
				type : 'POST',
				data : { 'itemIds[]' : toSend['test-plan-items'],
					'boundSuiteIds[]' : toSend['bound-test-suites'],
					'unboundSuiteIds[]' : toSend['unbound-test-suites']},
				dataType : 'json'
			}).success(function(json){
				eventBus.trigger("node.bind");
			});
		};

		this.getModel = function(evt) {
			_getModel().success(function() {
				redrawViews(evt);
			});
		};


		// ************** other events **********

		eventBus.onContextual('node.add node.rename node.remove node.refresh node.bind', function(evt){
			self.getModel(evt);
		});

	}

	return TestSuiteModel;
});
