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
 * The initialization module takes settings, as you expect. Here is what the
 * configuration object looks like :
 *
 * {
 *
 *
 *  basic : {
 *      testCaseId : the id of the test case,
 *      projectId : the id of the project this test case belongs to
 *      rootContext : the root url
 *      testCaseUrl : the baseTestCaseUrl
 *  },
 *
 *
 *  permissions : {
 *      isWritable : says whether the table content or structure can be modified by the user
 *      isLinkable : says whether the access to the requirement/test-step association page is accessible
 *      isAttachable : says if you can attach attachments to the steps
 *  },
 *
 *  language : {
 *      errorTitle : the title of the error popup
 *      noStepSelected : the message when no steps where selected although some were needed
 *      oklabel : the ok label for any confirmation popup
 *      cancellabel : the cancellabel for any confirmation popup
 *      deleteConfirm : the message for confirmation of deletion of the popup
 *      deleteTitle : the tooltip for the delete popup buttons
 *      infoTitle : the title for the popup that says close your widgets in edit mode
 *      popupMessage : the content of that popup
 *      btnExpand : the label of the expand button
 *      btnCollapse : the label of the collapse button
 *      addStepTitle : title for the add step popup
 *      addStep : label for the add step button
 *      addAnotherStep :  label for the add another step button
 *      ckeLang : the language for ckEditor
 *      placeholder : the placeholder title
 *      submit : the submit button value
 *  }
 *
 * }
 *
 *
 *
 */

define(["jquery", "squashtable/squashtable.collapser", "custom-field-values", "workspace.event-bus",
	"./popups", 'workspace.storage', 'squash.translator', "jquery.squash.oneshotdialog",
	"jquery.squash.formdialog", "squashtable"], function ($, TableCollapser,
	cufValuesManager, eventBus, popups, storage, translator, oneshot) {
	"use strict";

	// ************************* configuration functions
	// ************************************

	var COOKIE_NAME = "testcase-tab-cookie";

	//module scoped variable to add new step at proper index in datatable even if the index is out of the datatable pagination
	var targetTestStepIndex = 0;

	function makeTableUrls(conf) {
		var tcUrl = conf.basic.testCaseUrl;
		var ctxUrl = conf.basic.rootContext;
		return {
			dropUrl: tcUrl + "/steps/move",
			attachments: ctxUrl + "/attach-list/{attach-list-id}/attachments/manager?workspace=test-case&open=true",
			steps: ctxUrl + "test-steps/",
			callTC: ctxUrl + "/test-cases/{called-tc-id}/info",
			pasteStep: tcUrl + "/steps",
			deleteStep: tcUrl + "/steps",
			addStep: tcUrl + "/steps/add",
			editActionUrl: tcUrl + "/steps/{step-id}/action",
			editResultUrl: tcUrl + "/steps/{step-id}/result",
			stepcufBindingUrl: ctxUrl + "/custom-fields-binding?projectId=" + conf.basic.projectId +
			"&bindableEntity=TEST_STEP&optional=false",
			ckeConfigUrl: ctxUrl + "styles/ckeditor/ckeditor-config.js",
			indicatorUrl: ctxUrl + "/scripts/jquery/indicator.gif",
			tableLanguageUrl: ctxUrl + "/datatables/messages",
			tableAjaxUrl: tcUrl + "/steps",
			projectId: conf.basic.projectId,
			testCaseId: conf.basic.testCaseId
			// yes, that's no url. Uh.
		};
	}

	// ************************* table configuration functions
	// ******************************

	function refresh() {
		$(".test-steps-table").squashTable().refreshRestore();
	}

	function stepsTableCreatedRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
		nRow.className += (aData['step-type'] === "action") ? " action-step-row" : " call-step-row";
	}

	function specializeCellClasses(table) {

		var actionRows = table.find("tr.action-step-row");
		var callRows = table.find("tr.call-step-row");

		// remove useless classes for action step rows
		actionRows.find("td.called-tc-cell").removeClass("called-tc-cell");

		// remove useless classes for call step rows
		callRows.find("td.rich-edit-action").removeClass("rich-edit-action");
		callRows.find("td.rich-edit-result").removeClass("rich-edit-result");
		callRows.find("td.has-attachment-cell").removeClass("has-attachment-cell");
		callRows.find("td.custom-field-value").removeClass();

		callRows.find("td.called-tc-cell").next().remove().end().attr("colspan", 2);
	}


	function setCallStepsContent(table) {
		table.find('tr.call-step-row').each(function () {
			_callStepContent(table, this);
		});
	}


	function _callStepContent(table, row) {
		var alllang = translator.get({
			template: 'test-case.call-step.action.template',
			none: 'label.callstepdataset.PickDataset',
			delegate: 'label.callstepdataset.Delegate'
		});

		var data = table.fnGetData(row);
		var stepinfo = data['call-step-info'];

		var tcUrl = window.squashtm.app.contextRoot + '/test-cases/' + stepinfo.calledTcId + '/info',
			dsName = (stepinfo.paramMode === 'NOTHING') ? alllang.none :
				(stepinfo.paramMode === 'DELEGATE') ? alllang.delegate :
					stepinfo.calledDatasetName;

		var encodedTcName = $("<div/>").text(stepinfo.calledTcName).html();
		var tcLink = '<a href="' + tcUrl + '">' + encodedTcName + '</a>',
			dsLink = '<a href="javascript:void(0)" class="called-dataset-link">' + dsName + '</a>';

		var text = alllang.template.replace('{0}', tcLink).replace('{1}', dsLink);

		$(row).find('td.called-tc-cell').html(text);
	}

	function save_dt_view(oSettings, oData, testCaseId) {
		var id = $(".test-steps-table")[0].id;
		storage.set('DataTables_' + window.location.pathname + "_" + id, oData);
	}

	function load_dt_view(oSettings, testCaseId) {
		var id = $(".test-steps-table")[0].id;
		return storage.get('DataTables_' + window.location.pathname + "_" + id);
	}

	function stepsTableDrawCallback() {

		// rework the td css classes to inhibit some post processing on
		// them when not relevant
		/*jshint validthis: true */
		specializeCellClasses(this);

		// handles the content of the call step rows
		/*jshint validthis: true */
		setCallStepsContent(this);

		// collapser
		/*jshint validthis: true */
		var collapser = this.data("collapser");
		if (collapser) {
			collapser.refreshTable();
		}

		// the cookie used when navigating back from the attachment manager. This solution is crap
		// and I hope we come up with something better.
		/*jshint validthis: true */
		this.on('click', 'td.has-attachment-cell > a', function (evt) {
			$.cookie(COOKIE_NAME, 1, {expires: 1, path: '/'});
			return true;
		});
	}

	function stepDropHandlerFactory(dropUrl) {
		return function stepDropHandler(dropData) {
			$.post(dropUrl, dropData, function () {
				refresh();
			});
		};
	}

	// ************************************ table initialization
	// *****************************

	function initTable(settings) {

		var cufColumnPosition = 4;
		var language = settings.language, urls = makeTableUrls(settings), permissions = settings.permissions;

		// select the table and the panel that holds the scrollbar
		var table = $("#test-steps-table-" + urls.testCaseId);

		var cufTableHandler = cufValuesManager.cufTableSupport;

		// first we must process the DOM table for cufs
		cufTableHandler.decorateDOMTable(table, settings.basic.cufDefinitions, cufColumnPosition);

		// now let's move to the datatable configuration
		// in order to enable/disable some features regarding the
		// permissions, one have to tune the css classes of some
		// columns.
		var editActionClass = "", editResultClass = "", deleteClass = "", dragClass = "", linkButtonClass = "", attachButtonClass = "";

		if (permissions.isWritable) {
			editActionClass = "rich-edit-action";
			editResultClass = "rich-edit-result";
			deleteClass = "delete-button";
			dragClass = "drag-handle";
		}
		if (!permissions.isLinkable) {
			linkButtonClass = "default-cursor";
		}
		if (!permissions.isAttachable) {
			attachButtonClass = "default-cursor";
		}

		var id = $(".test-steps-table")[0].id;
		var savedData = storage.get('DataTables_' + window.location.pathname + "_" + id);

		// create the settings
		var datatableSettings = {
			aaData: settings.basic.tableData,
			/* Issue #6568: Reset is needed. */
			// bStateSave: true,
			fnStateSave: function (oSettings, oData) {
				save_dt_view(oSettings, oData);
			},
			fnStateLoad: function (oSettings) {
				return load_dt_view(oSettings);
			},
			sAjaxSource: urls.tableAjaxUrl,
			fnDrawCallback: stepsTableDrawCallback,
			fnCreatedRow: stepsTableCreatedRowCallback,
			iDeferLoading: settings.basic.totalRows,
			iDisplayLength: 50,
			aoColumnDefs: [{
				'bVisible': false,
				'bSortable': false,
				'aTargets': [0],
				'mDataProp': "step-id"
			}, {
				'bVisible': true,
				'bSortable': false,
				'aTargets': [1],
				'mDataProp': "step-index",
				'sClass': "select-handle centered " + dragClass,
				'sWidth': "2em"
			}, {
				'bVisible': true,
				'bSortable': false,
				'aTargets': [2],
				'mDataProp': "empty-requirements-holder",
				'sClass': "centered requirements-button " + linkButtonClass,
				'sWidth': "2em"
			}, {
				'bVisible': true,
				'bSortable': false,
				'aTargets': [3],
				'mDataProp': "attach-list-id",
				'sClass': "centered has-attachment-cell " + attachButtonClass,
				'sWidth': "2em"
			}, {
				'bVisible': true,
				'bSortable': false,
				'aTargets': [4],
				'mDataProp': "step-action",
				'sClass': "called-tc-cell collapsible " + editActionClass
			}, {
				'bVisible': true,
				'bSortable': false,
				'aTargets': [5],
				'mDataProp': "step-result",
				'sClass': "collapsible " + editResultClass
			}, {
				'bVisible': true,
				'bSortable': false,
				'aTargets': [6],
				'mDataProp': "empty-browse-holder",
				'sClass': "centered browse-button",
				'sWidth': "2em"
			}, {
				'bVisible': true,
				'bSortable': false,
				'aTargets': [7],
				'mDataProp': "empty-delete-holder",
				'sClass': "centered " + deleteClass,
				'sWidth': "2em"
			}]

		};

		var cookie = $.cookie(COOKIE_NAME);

		if (!!savedData && !!cookie) {
			datatableSettings.aaSorting = savedData.aaSorting;
			datatableSettings.abVisCols = savedData.abVisCols;
			datatableSettings.aoSearchCols = savedData.aoSearchCols;
			datatableSettings.iCreate = savedData.iCreate.aoSearchCols;
			datatableSettings.iEnd = savedData.iEnd;
			datatableSettings.iLength = savedData.iLength;
			datatableSettings.iStart = savedData.iStart;
			datatableSettings.oSearch = savedData.oSearch;
			$.cookie(COOKIE_NAME, null, {path: '/'});
		} else {
			storage.remove('DataTables_' + window.location.pathname + "_" + id);
		}

		// decorate the settings with the cuf values support
		datatableSettings = cufTableHandler.decorateTableSettings(datatableSettings, settings.basic.cufDefinitions,
			cufColumnPosition, permissions.isWritable);

		var squashSettings = {

			dataKeys: {
				entityId: "step-id",
				entityIndex: "step-index"
			},

			enableHover: true,

			confirmPopup: {
				oklabel: language.oklabel,
				cancellabel: language.cancellabel
			},

			attachments: {
				url: "#"
			},

			buttons: [{
				tooltip: language.edit,
				tdSelector: "td.browse-button",
				uiIcon: "edit-pencil",
				onClick: function (table, cell) {
					var row = cell.parentNode.parentNode;
					var stepId = table.getODataId(row);
					var url = urls.steps + stepId;
					window.open(url, "_blank");
					window.focus();
					localStorage.removeItem("selectedRow");
				}
			}, {
				tooltip: language.requirements,
				tdSelector: "td.requirements-button",
				'cssclass': "icon-entity",
				uiIcon: function (row, data) {
					return (data["has-requirements"]) ? "icon-requirement" : "icon-requirement-off";
				},
				condition: function (row, data) {
					return data["step-type"] == "action";
				},
				onClick: function (table, cell) {
					if (permissions.isLinkable) {
						$.cookie(COOKIE_NAME, 1, {expires: 1, path: '/'});
						var row = cell.parentNode.parentNode;
						var stepId = table.getODataId(row);
						var url = urls.steps + stepId + "/verified-requirement-versions/manager";
						/*window.open(url, "_blank");
						window.focus();*/
						document.location.href = url;
					}
				}
			}]

		};

		if (permissions.isWritable) {

			var moreSettings = {

				enableDnD: true,

				deleteButtons: {
					delegate: "#delete-test-step-dialog",
					tooltip: language.deleteTitle
				},

				richEditables: {
					'rich-edit-action': urls.editActionUrl,
					'rich-edit-result': urls.editResultUrl
				},

				functions: {
					dropHandler: stepDropHandlerFactory(urls.dropUrl)
				}
			};

			$.extend(squashSettings, moreSettings);

			table.on('click', '.called-dataset-link', function (evt) {
				var sqtable = table.squashTable(),
					popup = $("#pick-call-step-dataset-dialog");

				var $row = $(evt.currentTarget).closest('tr');
				var data = sqtable.fnGetData($row.get(0));
				popup.data('opener-id', data['step-id']);

				popup.formDialog('open');
			});

			/*
			 * [Issue 4932] :
			 *
			 * 1 - the plugin jeditable-ckeditor creates the "input" as it should. This input is a compound of a
			 * a div (the actual elemeent which the user actually interact with) and a textarea
			 * (invisible, and backs the data of the div), both required by CKEditor
			 * to work properly. Note that the textarea is a regular input while the div is not.
			 *
			 * 2 - jeditable at some point does the following : $(':input:visible:enabled:first', form).focus();
			 * which makes it focus on the textarea while we want it to focus on the div instead.
			 *
			 * The only working solution was to amend the source of jeditable and have it trigger an event
			 * (named opencomplete.editable) and make the viewport refocus on the correct element this time.
			 *
			 */


			table.on('opencomplete.editable', 'td', function (evt) {
				var td = $(evt.currentTarget);
				var zediv = td.find('div.cke');
				if (zediv.length > 0) {
					zediv.focus();
				}
			});
		}

		if (permissions.isAttachable) {
			squashSettings.attachments = {
				url: urls.attachments,
				target:"_blank"
			};
		}

		table.squashTable(datatableSettings, squashSettings);

		// Commenting out the 'refresh' just below, see https://ci.squashtest.org/mantis/view.php?id=2627#c4959
		//$("#test-steps-table-"+urls.testCaseId).squashTable().refresh();

		// also listen to the parameter assignation mode of its own steps
		eventBus.onContextual('testStepsTable.changedCallStepParamMode', function (evt, params) {
			var row = table.getRowsByIds([params.stepId]).get(0);
			if (row !== undefined) {
				var stepInfo = table.fnGetData(row)['call-step-info'];
				stepInfo.calledDatasetId = params.datasetId;
				stepInfo.paramMode = params.mode;
				stepInfo.calledDatasetName = params.datasetName;
				_callStepContent(table, row);
			}
		});
	}

	// ************************************ toolbar utility functions
	// *************************


	// *************************** add step popup
	// ***************************

	function addTestStepSuccess() {
		var dialog = $("#add-test-step-dialog");
		if (dialog.formDialog("isOpen")) {
			dialog.formDialog("close");
		}
		eventBus.trigger("testStepsTable.stepAdded");
		refresh();
	}

	function addTestStepSuccessAnother(response) {
		CKEDITOR.instances["add-test-step-action"].setData("");
		CKEDITOR.instances["add-test-step-result"].setData("");

		var dialog = $("#add-test-step-dialog");
		dialog.data("cuf-values-support").reset();
		eventBus.trigger("testStepsTable.stepAdded");
		refresh();
		//increment index counter;
		targetTestStepIndex ++;
		dialog.formDialog("focusMainInput");
	}

	function readAddStepParams() {

		var cufSupport = $("#add-test-step-dialog").data("cuf-values-support");

		var params = {};
		params.action = $("#add-test-step-action").val();
		params.expectedResult = $("#add-test-step-result").val();

		//reading the global target index counter
		params.index = targetTestStepIndex;

		$.extend(params, cufSupport.readValues());

		return params;

	}

	function initAddTestStepDialog(language, urls) {


		function postStep(data) {
			return $.ajax({
				url: urls.addStep,
				type: "POST",
				data: JSON.stringify(data),
				contentType: "application/json;charset=UTF-8"
			});
		}

		// main popup definition

		var dialog = $("#add-test-step-dialog");
		dialog.formDialog();

		dialog.on('formdialogaddandmore', function () {
			var data = readAddStepParams();
			postStep(data).success(addTestStepSuccessAnother);
		});

		dialog.on('formdialogadd', function () {
			var data = readAddStepParams();
			postStep(data).success(addTestStepSuccess);
		});

		dialog.on('formdialogcancel', function () {
			refresh();
			dialog.formDialog('close');
		});

		$("#add-test-step-button").on('click', function () {
			var $table = $(".test-steps-table");
			var selectedIds = $table.squashTable().getSelectedIds();
			if(selectedIds.length > 0 ){
				var idTargetStep = selectedIds[selectedIds.length - 1];
				targetTestStepIndex = $table.squashTable().getDataById(idTargetStep)["step-index"];
			} else {
				//get the number of results in datatable ie total line number on all pages
				targetTestStepIndex = $table.squashTable().fnSettings().fnRecordsTotal();
			}
			dialog.formDialog('open');
		});

		// cuf value support

		var cufTable = $("#add-test-step-custom-fields");
		var bindingsUrl = urls.stepcufBindingUrl;

		var cufValuesSupport = cufValuesManager.newCreationPopupCUFHandler({
			source: bindingsUrl,
			table: cufTable
		});
		cufValuesSupport.reloadPanel();
		dialog.data("cuf-values-support", cufValuesSupport);

		dialog.on("formdialogopen", function () {
			cufValuesSupport.reset();
		});
	}


	// ************************* Call Other Test Case
	// **********************************

	function callTestCase(settings) {
		var $table = $(".test-steps-table");
		var stepIdSelected = $table.squashTable().getSelectedIds();
		var stepTargetIndex = 0;

		//now selecting the last selected row in squashTable and retrieve index
		if (stepIdSelected !== undefined && stepIdSelected !== null && stepIdSelected.length > 0) {
			var idTargetStep = stepIdSelected[stepIdSelected.length - 1];
			stepTargetIndex = $table.squashTable().getDataById(idTargetStep)["step-index"];
		}

		//redirect to level 2 interface Calling Test Case with proper url formatting
		var ctxUrl = settings.basic.testCaseUrl;
		document.location.href = ctxUrl + "/called-test-cases/manager";
	}

	function initCallTestCaseLink(settings) {
		//setting an eventhandler for the anchor "add-call-step-button" (For evol 5208)
		$("#add-call-step-button").on("click", function () {
			//closure on callTestCase to bake testCaseId, urls... in init phase
			return callTestCase(settings);
		});
	}


	// ************************* other buttons code
	// **********************************

	function initStepCopyPastaButtons(language, urls) {

		var table = $("#test-steps-table-" + urls.testCaseId).squashTable();


		$("#copy-step").bind("click", function () {
			var stepIds = table.getSelectedIds();
			if (!stepIds.length) {
				$.squash.openMessage(language.errorTitle, language.noStepSelected);
			} else {
				var oPath = {
					path: "/"
				};
				$.cookie("squash-test-step-ids", stepIds.toString(), oPath);
				$.cookie("squash-test-step-project", urls.projectId, oPath);
			}

		});

		$("#paste-step").bind("click", function () {

			var cookieIds = $.cookie("squash-test-step-ids");
			var cookieProject = $.cookie("squash-test-step-project");
			var currentProject = urls.projectId;

			if (parseInt(cookieProject, 10) !== currentProject) {
				oneshot.show(language.infoTitle, language.warnCopy).then(function () {
					performPaste(cookieIds); // see definition below
				});
			} else {
				performPaste(cookieIds); // see definition below
			}
		});

		function performPaste(rawIds) {
			var stepIds = rawIds.split(",");

			try {
				if (!stepIds.length) {
					// FIXME how come we throw an ex that is caught 10 lines lower ?
					throw language.noStepSelected;
				}

				var position = table.getSelectedIds();

				var data = {};
				data.copiedStepId = stepIds;

				var pasteUrl = urls.pasteStep;

				if (position.length > 0) {
					data.idPosition = position[0];
					pasteUrl = pasteUrl + "/paste";
				} else {
					pasteUrl = pasteUrl + "/paste-last-index";
				}

				var $paste = $("#paste-step");
				$paste.squashButton('disable');
				$.ajax({
					type: "POST",
					data: data,
					url: pasteUrl,
					dataType: "json",
					success: pasteSuccess
				});

				$paste.removeClass("ui-state-focus");
			} catch (damn) {
				$.squash.openMessage(language.errorTitle, damn);
			}
		}
	}

	function pasteSuccess(pastedCallSteps) {
		$("#paste-step").squashButton('enable');
		if (pastedCallSteps) {
			eventBus.trigger("testStepsTable.pastedCallSteps");
		}
		eventBus.trigger("testStepsTable.stepAdded");
		refresh();
	}

	function initDeleteAllStepsButtons(language, urls) {


		$("#delete-all-steps-button").bind(
			'click', function () {
				$("#delete-test-step-dialog").formDialog('open');
			});
	}


	// ******************************* toolbar initialization
	// *********************************

	function initTableToolbar(language, urls) {

		// copy pasta buttons
		initStepCopyPastaButtons(language, urls);

		// delete all button
		initDeleteAllStepsButtons(language, urls);

		// add test step
		initAddTestStepDialog(language, urls);

	}

	// ************************************* table collapser code
	// ****************************

	function isEditing(collapser) {
		var collapsibleCells = collapser.collapsibleCells;
		for (var k = 0; k < collapsibleCells.length; k++) {
			if (collapsibleCells[k].editing) {
				return true;
			}
		}
		return false;
	}

	function makeCollapsibleCellsHandlers(collapser) {

		var openEdit = $.proxy(function (eventObject) {
			this.openAll();
			$(eventObject.target).click();
		}, collapser);

		return {
			open: function (collapser) {
				var collapsibleCells = $(collapser.collapsibleCells);
				collapsibleCells.addClass("editable").off("click", openEdit).editable("enable");
			},

			close: function (collapser) {
				var collapsibleCells = $(collapser.collapsibleCells);
				collapsibleCells.removeClass("editable").on("click", openEdit).editable("disable");
			}
		};
	}

	function initCollapser(language, urls, isWritable, testCaseId) {


		var collapser;

		var collapseButton = $("#collapse-steps-button");

		var table = $("#test-steps-table-" + testCaseId);

		// begin

		var cellSelector = function (row) {
			return $(row).find("td.collapsible").not("called-tc-id").get();
		};

		collapser = new TableCollapser(table, cellSelector);

		// button handlers

		var buttonOpenHandler = $.proxy(function () {
			this.squashButton("option", "icons", {
				primary: "ui-icon-zoomout"
			});
			this.attr("title", language.btnCollapse);
			this.squashButton("option", "label", language.btnCollapse);
		}, collapseButton);

		var buttonCloseHandler = $.proxy(function () {
			this.squashButton("option", "icons", {
				primary: "ui-icon-zoomin"
			});
			this.attr("title", language.btnExpand);
			this.squashButton("option", "label", language.btnExpand);

		}, collapseButton);

		collapser.onOpen(buttonOpenHandler);
		collapser.onClose(buttonCloseHandler);

		// writable handlers

		if (isWritable) {
			var handlers = makeCollapsibleCellsHandlers(collapser);
			collapser.onOpen(handlers.open);
			collapser.onClose(handlers.close);
		}

		collapseButton.click(function () {
			if (collapser.isOpen) {
				if (isEditing(collapser)) {
					$.squash.openMessage(language.infoTitle, language.collapseMessage);
				} else {
					collapser.closeAll();
				}
			} else {
				collapser.openAll();
			}
		});

		// end
		table.data("collapser", collapser);

	}


	// ******************************* main
	// *********************************

	function init(settings) {
		$.squash.decorateButtons();

		var language = settings.language;
		var urls = makeTableUrls(settings);
		var permissions = settings.permissions;

		// the js table
		initTable(settings);

		//the popups
		var conf = {};
		conf.permissions = {};
		conf.permissions.writable = settings.permissions.isWritable;
		conf.urls = {};
		conf.urls.testCaseStepsUrl = urls.deleteStep;
		conf.testCaseId = settings.basic.testCaseId;
		/*jshint validthis: true */
		conf.stepsTablePanel = this;
		popups.init(conf);


		// toolbar
		if (permissions.isWritable) {
			initTableToolbar(language, urls);
		}

		// table collapser
		initCollapser(language, urls, permissions.isWritable, settings.basic.testCaseId);

		//init the link for calling a test case
		initCallTestCaseLink(settings);

		// listen to call steps events
		eventBus.onContextual('call-test-case', function(){
			$("#test-steps-table-"+conf.testCaseId).squashTable().refresh();
		});
	}

	return {
		init: init,
		refreshTable: refresh
	};

});
