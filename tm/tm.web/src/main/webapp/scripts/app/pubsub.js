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
/**
 * Provides the publisher / subscriber pattern
 *
 * This is a crude mechanism to throw events from the DOM which are enqueued until processed. *It is meant to remain crude*.
 * For full blown event system, use the one from Backbone / jquery
 *
 * Known limitations : an event published before this module is loaded can only be consumed once
 *
 * This module adds the publish, subscribe and unsubscribe methods to the global
 * namespace.
 *
 * Needs to be bootstrapped by loading the pubsub-boot.js file *before* require kicks in.
 *
 */
define([ "jquery" ], function($) {
	var proxy = $({});
	var handledEvents = [];

	window.publish = function() {
		proxy.trigger.apply(proxy, arguments);
		if ($.inArray(arguments[0],handledEvents)<0) {
			// when event is triggered before subscription, we enqueue it so that it can be processed later
			document.eventsQueue.push(arguments);
		}
	};

	window.subscribe = function(event) {
		proxy.on.apply(proxy, arguments);

		$(document.eventsQueue).each(function(index) {
			if (this[0] === event) {
				proxy.trigger.apply(proxy, this);
				document.eventsQueue.splice(index, 1);
				handledEvents.push(event);
				return false;
			}
		});
	};

	window.unsubscribe = function() {
		proxy.off.apply(proxy, arguments);
	};

	return {
		publish : window.publish,
		subscribe : window.subscribe,
		unsubscribe : window.unsubscribe
	};
});
