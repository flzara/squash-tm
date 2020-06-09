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
 * configuration an object as follow :
 *
 * {
 *		basic : {
 *			iterationId : the id of the current iteration
 *		}
 *	}
 *
 */

define(['squash.translator', './table'], function(translator, table){

	function enhanceConfiguration(origconf){

		var conf = $.extend({}, origconf);

		var baseURL = squashtm.app.contextRoot;

		conf.messages = translator.get({
			executionStatus : {
				SETTLED : "execution.execution-status.SETTLED",
				UNTESTABLE : "execution.execution-status.UNTESTABLE",
				BLOCKED : "execution.execution-status.BLOCKED",
				FAILURE : "execution.execution-status.FAILURE",
				SUCCESS : "execution.execution-status.SUCCESS",
				RUNNING : "execution.execution-status.RUNNING",
				READY	: "execution.execution-status.READY"
			}
		});

		conf.urls = {
			automatedSuiteUrl : baseURL + 'test-suites/'+conf.basic.iterationId+'/automated-suite/'
		};

		return conf;
	}

	return {
		init : function(origconf){
			var conf = enhanceConfiguration(origconf);
			table.init(conf);
		}
	};
});
