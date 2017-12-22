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
 * This module initializes the Wreqr event bus.
 * It creates :
 * * `squashtm.vent` : event bus
 * * `squashtm.reqres` : request / response messages bus
 */
define([ "backbone.wreqr" ], function(Wreqr) {
	"use strict";

	window.squashtm = window.squashtm || {};
	window.squashtm.vent = window.squashtm.vent || new Wreqr.EventAggregator();
	window.squashtm.reqres = new Wreqr.RequestResponse();

	return window.squashtm;
});