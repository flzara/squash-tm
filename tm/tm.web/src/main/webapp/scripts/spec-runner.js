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
require([ "common", "require" ], function(common) {
	var specs = [
	"specs/report/ReportCriteriaPanelSpec",
	"specs/report/ConciseFormModelSpec",
	"specs/squashtable/ColDefsBuilderSpec",
	"specs/app/BindViewSpec",
	"specs/info-list-manager/InfoListModelSpec",
	"specs/info-list-manager/InfoListOptionModelSpec",
	];

	// expands require config for jasmine usage
	window.requirejs.config({
		paths : {
			'jasmine' : 'lib/jasmine-2.0.0/jasmine',
			'jasmine-html' : 'lib/jasmine-2.0.0/jasmine-html',
			'boot' : 'lib/jasmine-2.0.0/boot'
		},
		shim : {
			'jasmine' : {
				exports : 'jasmine'
			},
			'jasmine-html' : {
				deps : [ 'jasmine' ],
				exports : 'jasmine'
			},
			'boot' : {
				deps : [ 'jasmine', 'jasmine-html' ],
				exports : 'jasmine'
			}
		}

	});

	// shit breaks otherwise
	window.squashtm = window.squashtm || {};
	window.squashtm.app = window.squashtm.app || {};
	window.squashtm.app.contextRoot = window.squashtm.app.contextRoot || ".";

	require([ 'boot' ], function() {
		require(specs, function() {
			// Initialize the HTML Reporter and execute the environment (setup by `boot.js`)
			window.onload();
		});
	});
});
// Load Jasmine - This will still create all of the normal Jasmine browser globals unless `boot.js` is re-written to use
// the
// AMD or UMD specs. `boot.js` will do a bunch of configuration and attach it's initializers to `window.onload()`.
// Because
// we are using RequireJS `window.onload()` has already been triggered so we have to manually call it again. This will
// initialize the HTML Reporter and execute the environment.
// require(['boot'], function () {
//
// // Load the specs
// require(specs, function () {
//
// // Initialize the HTML Reporter and execute the environment (setup by `boot.js`)
// window.onload();
// });
// });
// })();
