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
define([ "jquery", "backbone", "underscore", "app/lnf/Forms" ], function($, Backbone, _, Forms) {
	"use strict";
	/**
	 * Backbone.BindView
	 *
	 * A BindView can be used to bind controls to model properties. When a
	 * control's value is changed, the model's bound property is set accordingly.
	 *
	 * to bind a model's property to a control, add a `data-prop="propertyName"` attribute to the `<input>`
	 *
	 * When BindViews are nested, one has to :
	 * 1. add a viewName property to the view definition
	 * var sub = BindView.extend({
	 *   viewName: "sub",
	 *   ...
	 * });
	 *
	 * 2. add a `data-view="<view name>"` attribute to the bound
	 *
	 * TODO add coercers for non string props ?
	 */

	/**
	 * change event callback which sets the current model's property
	 */
	var setProp = function(event) {
		if (!!this.model && !event.wasBound) {
			var control = event.target;
			this.model.set($(control).data("prop"), $(control).val());
			event.wasBound = true;
		}
	};
	
	/**
	 * change event callback which sets the current model's property
	 * only for checkboxes as they have no value attribute so generic method won't work.
	 * model attribute setted at true if checkbox checked, false on uncheck
	 */
	var setPropCheckBox = function(event) {
		if (!!this.model && !event.wasBound) {
			var control = event.target;
			this.model.set($(control).data("prop"), $(control).is(":checked"));
			event.wasBound = true;
		}
	};

	/**
	 * events hash used for model binding
	 */
	var defaultBindingsEvents = {
		"change input:text[data-prop]" : setProp,
		"change textarea[data-prop]" : setProp,
		"change input:checkbox[data-prop]" : setPropCheckBox,
		"change select[data-prop]" : setProp
	};

	function bindingsEvents(view) {
		var events = {};
		var selector = bindingSelectorPostfix(view);

		_.chain(defaultBindingsEvents)
			.pairs()
			.each(function(pair) {
				events[pair[0] + selector] = pair[1];
		});

		return events;
	}

	function bindingSelectorPostfix(view) {
		return view.viewName === undefined ? ":not([data-view])" : "[data-view='" + view.viewName + "']";
	}

	var BindView = Backbone.View.extend({});

	/**
	 * delegateEvents merges the events which would be processed by standard View
	 * and binding events
	 */
	BindView.prototype.delegateEvents = function bindViewDelegateEvents(events) {
		// when no events are passed, view events hash is expected to be processed
		var requestedEvents = events || _.result(this, 'events');

		var actualEvents = _.extend(bindingsEvents(this), requestedEvents);
		return Backbone.View.prototype.delegateEvents.call(this, actualEvents);
	};

	/**
	 * Retrieves the control bound to a model property.
	 *
	 * @param view
	 *          form view
	 * @param prop
	 *          property name
	 * @return matching `.form-group`
	 */
	BindView.prototype.$boundControl = function(prop) {
		var selector = "[data-prop='" + prop + "']" + bindingSelectorPostfix(this);
		return this.$(".control-group").has(selector);
	};

	BindView.prototype.boundControl = function(prop) {
		return Forms.control(this.$boundControl(prop));
	};

	BindView.prototype.resetState = function(prop) {
		return Forms.form(this.$el).clearState();
	};

	/**
	 * extend wraps the custom initialize so that it calls BindView's initialize function
	 */
	BindView.extend = function(protoProps, staticProps) {
		// TODO useless ?
		var parent = this;

		if (protoProps && _.has(protoProps, "initialize")) {
			var protoPropsInit = protoProps.initialize;

			var initWrapper = function initWrapper() {
				parent.prototype.initialize.apply(this, arguments);
				protoPropsInit.apply(this, arguments);
			};

			protoProps.initialize = initWrapper;
		}

		return Backbone.View.extend.call(this, protoProps, staticProps);
	};

	Backbone.BindView = BindView;

	return BindView;
});