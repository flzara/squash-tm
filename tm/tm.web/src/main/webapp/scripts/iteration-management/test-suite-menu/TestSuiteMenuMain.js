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
define(
		[ "./TestSuiteManager", "./TestSuiteMenu", "./TestSuiteModel" ],
		function(TestSuiteManager, TestSuiteMenu, TestSuiteModel) {

			/*
			 * settings is an array of objects
			 * 
			 * settings = { modelSettings : modelSettings, managerSettings:
			 * managerSettings, menuSettings : menuSettings, tableListener :
			 * tableListener }
			 * 
			 * modelSettings = { createUrl : test suite creation url,
			 * baseUpdateUrl : base url for updating test suites, getUrl : url
			 * of the test suite, removeUrl : test suite deletion url, initData :
			 * initData }
			 * 
			 * initData = array of { id : suite id, name : suite name }
			 * 
			 * managerSettings = { instance : location of the message within the page, 
			 * deleteConfirmMessage : deletion popup message, deleteConfirmTitle : deletion popup title }
			 * 
			 * 
			 * menuSettings = { instanceSelector : menu id , datatableSelector :
			 * datatable id, isContextual : boolean value indicating whether the
			 * menu is contextual or not }
			 * 
			 */
			return function(settings) {

				squashtm.testSuiteManagement = {};

				squashtm.testSuiteManagement.testSuiteModel = new TestSuiteModel(
						settings.modelSettings);

				settings.managerSettings.model = squashtm.testSuiteManagement.testSuiteModel;
				squashtm.testSuiteManagement.testSuiteManager = new TestSuiteManager(
						settings.managerSettings);

				settings.menuSettings.model = squashtm.testSuiteManagement.testSuiteModel;
				squashtm.testSuiteManagement.testSuiteMenu = new TestSuiteMenu(
						settings.menuSettings);

				squashtm.testSuiteManagement.testSuiteModel
						.addView(settings.tableListener);

			};
		});