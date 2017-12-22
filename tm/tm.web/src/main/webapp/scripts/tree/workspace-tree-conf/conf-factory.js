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
 * conf : {
 *
 *	workspace : a name among ['test-case', 'requirement', 'campaign'] that will drive the rest of the configuration,
 *
 *	controller : the controller object that manages the tree, the buttons and popups,
 *
 *	model : model object for that tree,
 *
 *  messages :{
 *		cannotMove : the given node(s) cannot be moved,
 *		warningMove : moving the given node(s) might result in loss of data,
 *		warningCopy : the clones of the given node(s) might not inherit all the data of the originals
 *  }
 *
 * }
 *
 */
define(["jquery", "./common-conf", "./w-testcase-conf", "./w-requirement-conf", "./w-campaign-conf", "./w-customreport-conf"],
	function ($, genCommon, genTC, genReq, genCamp, genCustomReport) {
		"use strict";

		return {
			generate: function (settings) {
				var commonConf = genCommon.generate(settings);
				var specificConf;

				switch (settings.workspace) {
					case 'test-case'  :
						specificConf = genTC.generate(settings);
						break;
					case 'requirement'  :
						specificConf = genReq.generate(settings);
						break;
					case 'campaign'    :
						specificConf = genCamp.generate(settings);
						break;
					case 'custom-report'    :
						specificConf = genCustomReport.generate(settings);
						break;
				}

				return $.extend({}, commonConf, specificConf);

			}
		};


	});
