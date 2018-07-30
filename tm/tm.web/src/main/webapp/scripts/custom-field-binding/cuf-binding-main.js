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
define([ "jquery", "./entity-manager" ], function(jquery, EntityManager) {

	function makeTCConf(conf) {
		return $.extend({}, conf.general, conf.tcSettings);
	}

	function makeTStepConf(conf) {
		return $.extend({}, conf.general, conf.tstepSettings);
	}

	function makeReqConf(conf) {
		return $.extend({}, conf.general, conf.reqSettings);
	}

	function makeCampConf(conf) {
		return $.extend({}, conf.general, conf.campSettings);
	}

	function makeIterConf(conf) {
		return $.extend({}, conf.general, conf.iterSettings);
	}

	function makeTSConf(conf) {
		return $.extend({}, conf.general, conf.tsSettings);
	}

	function makeExecConf(conf) {
		return $.extend({}, conf.general, conf.execSettings);
	}

	function makeCampaignFoldersConf(conf) {
		return $.extend({}, conf.general, conf.campaignFoldersSettings);

	}
	function makeReqFoldersConf(conf) {
		return $.extend({}, conf.general, conf.reqFoldersSettings);

	}
	function makeTcFoldersConf(conf) {
		return $.extend({}, conf.general, conf.tcFoldersSettings);

	}
	function makeCrFoldersConf(conf) {
		return $.extend({}, conf.general, conf.crFoldersSettings);
	}

	function makeProjectConf(conf) {
		return $.extend({}, conf.general, conf.projectSettings);
	}

	function makeExecStepConf(conf) {
		return $.extend({}, conf.general, conf.execStepSettings);
	}


	var manager = {

		setConfig : function(conf) {
			this.config = conf;
			return this;
		},

		init : function() {

			// test case
			var tcConf = makeTCConf(this.config);
			new EntityManager(tcConf);

			// test step
			var tstepConf = makeTStepConf(this.config);
			new EntityManager(tstepConf);

			// requirement
			var reqConf = makeReqConf(this.config);
			new EntityManager(reqConf);

			// campaign
			var campConf = makeCampConf(this.config);
			new EntityManager(campConf);

			// iteration
			var iterConf = makeIterConf(this.config);
			new EntityManager(iterConf);

			// test suite
			var tsConf = makeTSConf(this.config);
			new EntityManager(tsConf);

			// execution
			var execConf = makeExecConf(this.config);
			new EntityManager(execConf);

			// execution step
			var execStepConf = makeExecStepConf(this.config);
			new EntityManager(execStepConf);

			// project
			var projectConf = makeProjectConf(this.config);
			new EntityManager(projectConf);

			// folders
			var campaignFolderConf = makeCampaignFoldersConf(this.config);
			new EntityManager(campaignFolderConf);

			// folders
			var crFoldersConf = makeCrFoldersConf(this.config);
			new EntityManager(crFoldersConf);

			// folders
			var reqFoldersConf = makeReqFoldersConf(this.config);
			new EntityManager(reqFoldersConf);

			// folders
			var tcFoldersConf = makeTcFoldersConf(this.config);
			new EntityManager(tcFoldersConf);

		}
	};

	return manager;

});
