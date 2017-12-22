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
define(['module', "jquery", "handlebars", "squash.translator", "squash.basicwidgets", "jeditable.selectJEditable",
			"squash.configmanager", "workspace.routing", "jquery.squash.formdialog", "jeditable.datepicker",
    "squashtable", "jquery.squash.confirmdialog"], function (module, $, Handlebars, translator, basic, SelectJEditable, confman, routing) {

		var config = module.config();

		function clickBackButton() {
			console.log('back');
			history.back();
		}

		function initRenameDialog() {
			var renameDialog = $("#rename-milestone-dialog");
			renameDialog.formDialog();

        renameDialog.on('formdialogopen', function () {
				var name = $.trim($('#milestone-name-header').text());
				$("#rename-milestone-input").val($.trim(name));
			});

        renameDialog.on('formdialogconfirm', function () {
				var params = {
                newName: $("#rename-milestone-input").val()
				};
				$.ajax({
                url: config.urls.milestoneUrl,
                type: 'POST',
                dataType: 'json',
                data: params
            }).success(function (data) {
					$('#milestone-name-header').html(data.newName);
					renameDialog.formDialog('close');
				});
			});

        renameDialog.on('formdialogcancel', function () {
				renameDialog.formDialog('close');
			});

        $("#rename-milestone-button").on('click', function () {
				renameDialog.formDialog('open');
			});

		}

    var postfn = function (value) {
			var localizedDate = value;
			var postDateFormat = $.datepicker.ATOM;
			var date = $.datepicker.parseDate(translator.get("squashtm.dateformatShort.datepicker"), localizedDate);
			var postDate = $.datepicker.formatDate(postDateFormat, date);

			return $.ajax({
            url: config.urls.milestoneUrl,
            type: 'POST',
            data: {
                newEndDate: postDate
				}
        }).done(function () {
				$("#milestone-end-date").text(value);
			});
		};

    var initJEditables = function () {
		var dateSettings = confman.getStdDatepicker();
		$("#milestone-end-date").editable(postfn, {
            type: 'datepicker',
            datepicker: dateSettings,
            name: "value"
		});

		var statusEditable = new SelectJEditable({
            target: function (value) {
                changeStatus(value, statusEditable);
            },
            componentId: "milestone-status",
            jeditableSettings: {
                data: config.data.milestone.status

			}

		});

		var rangeEditable = new SelectJEditable({
            target: function (value) {
                changeRange(value, rangeEditable);
            },
            componentId: "milestone-range",
            jeditableSettings: {
                data: config.data.milestone.range
						}
		});

		createOwnerEditable();
		};


    function createOwnerEditable() {
			var ownerEditable = new SelectJEditable({
            target: function (value) {
                changeOwner(value, ownerEditable);
            },
            componentId: "milestone-owner",
            jeditableSettings: {
                data: config.data.userList
            }
        });
		}


    var changeOwner = function changeOwner(value, ownerEditable) {
			//warning you are going to give your milestone !!
        if (!config.data.isAdmin && config.data.currentUser != value) {

					var popup = $("#changeOwner-popup");
					popup.data('value', value);
					popup.data('ownerEditable', ownerEditable);
					popup.confirmDialog('open');

			} else {
				changeOwnerRequest(value, ownerEditable);
			}
		};


    $("#delete-milestone-button").on('click', function () {
			var popup = $("#delete-milestone-popup");
			popup.confirmDialog('open');
		});

    $("#delete-milestone-popup").confirmDialog().on('confirmdialogconfirm', function () {
			var url =  config.urls.deleteMilestoneUrl;
			$.ajax({
            url: url,
            type: 'delete'
        }).done(function () {
				document.location.href = config.urls.milestonesUrl;
			});

		});


    $("#changeOwner-popup").confirmDialog().on('confirmdialogconfirm', function () {
			var $this = $(this);
			var value = $this.data('value');
			var ownerEditable = $this.data('ownerEditable');
			changeOwnerRequest(value, ownerEditable);
			clickBackButton();
		});


    $("#changeOwner-popup").confirmDialog().on('confirmdialogcancel', function () {
			var $this = $(this);
			var ownerEditable = $this.data('ownerEditable');
			var data = ownerEditable.settings.jeditableSettings.data;
			var user = data[config.data.currentUser];
			ownerEditable.component.html(user);
		});


    function changeOwnerRequest(value, ownerEditable) {

			$.ajax({
            type: "POST",
            url: config.urls.milestoneUrl,
				data: {
					id: ownerEditable.settings.componentId,
					value: value
				}
        }).then(function (value) {
				ownerEditable.component.html(value);
			});
		}


    var changeStatus = function changeStatus(value, statusEditable) {

			if (value == "PLANNED") {
				$.ajax({
                type: "GET",
                url: config.urls.milestoneUrl,
					data: {
						isBoundToAtleastOneObject: ""
						}
            }).done(function (isBoundToAtleastOneObject) {

						//popup hit you (again) for 9999 dmg. You die.
                if (isBoundToAtleastOneObject) {
							var popup = $("#changeStatus-popup");
							popup.data('value', value);
							popup.data('statusEditable', statusEditable);
							popup.confirmDialog('open');


						} else {
							//you are lucky no objects are bound to this object so you evaded the popup !
							changeStatusRequest(value, statusEditable);
						}

					});
			} else {
				//cool, other status allow you to change it without popup !
				changeStatusRequest(value, statusEditable);
			}

		};


    var changeStatusRequest = function changeStatus(value, statusEditable) {
			$.ajax({
            type: "POST",
            url: config.urls.milestoneUrl,
			data: {
				id: statusEditable.settings.componentId,
				value: value
			}
        }).then(function (value) {
				var data = JSON.parse(statusEditable.settings.jeditableSettings.data);
				var newStatus;
            for (var prop in data) {
                if (data.hasOwnProperty(prop)) {
                    if (data[prop] === value) {
							newStatus = prop;
							}
						}
					}
				config.data.milestone.currentStatus = newStatus;
				statusEditable.component.html(value);
			});
		};


    var changeRange = function changeRange(value, rangeEditable) {

			//If the range is global we need to check if the project is bound to a template
        if (config.data.milestone.currentRange == "GLOBAL") {
				$.ajax({
                type: "GET",
                url: config.urls.milestoneUrl,
					data: {
						isBoundToTemplate: ""
						}
            }).done(function (isBoundToATemplate) {

						//if bound to template show a popup (noooooo....) to know if the user really want to change range
                if (isBoundToATemplate) {

							var popup = $("#changeRange-popup");
							popup.data('value', value);
							popup.data('rangeEditable', rangeEditable);
							popup.confirmDialog('open');
						} else {
							//if not bound to template do the "classical" change range
							changeRangeRequest(value, rangeEditable);
						}
					});

			} else {
				changeRangeRequest(value, rangeEditable);
			}

		};

    $("#changeRange-popup").confirmDialog().on('confirmdialogcancel', function () {
			var $this = $(this);
			var rangeEditable = $this.data('rangeEditable');
			var data = JSON.parse(rangeEditable.settings.jeditableSettings.data);
			rangeEditable.component.html(data[config.data.milestone.currentRange]);
		});

    $("#changeRange-popup").confirmDialog().on('confirmdialogconfirm', function () {

			var $this = $(this);
			var value = $this.data('value');
			var rangeEditable = $this.data('rangeEditable');
			//remove all template from the milestone
			var url = routing.buildURL('milestone.unbind-templates', config.data.milestone.id);
			$.ajax({
            url: url,
            type: 'delete'
			});
			//now we can change the range
			changeRangeRequest(value, rangeEditable);

		});


    $("#changeStatus-popup").confirmDialog().on('confirmdialogconfirm', function () {

			var $this = $(this);
			var value = $this.data('value');
			var statusEditable = $this.data('statusEditable');

			var url = routing.buildURL('milestone.unbind-objects', config.data.milestone.id);
			$.ajax({
            url: url,
            type: 'delete'
			});

			changeStatusRequest(value, statusEditable);
		});


    $("#changeStatus-popup").confirmDialog().on('confirmdialogcancel', function () {

			var $this = $(this);
			var value = $this.data('value');
			var statusEditable = $this.data('statusEditable');
			var data = JSON.parse(statusEditable.settings.jeditableSettings.data);
			statusEditable.component.html(data[config.data.milestone.currentStatus]);
		});


    var changeRangeRequest = function changeRange(value, rangeEditable) {
			$.ajax({
            type: "POST",
            url: config.urls.milestoneUrl,
			data: {
				id: rangeEditable.settings.componentId,
				value: value
			}
        }).then(function (value) {
			rangeEditable.component.html(value);
			var data = JSON.parse(rangeEditable.settings.jeditableSettings.data);
			var newRange;
            for (var prop in data) {
                if (data.hasOwnProperty(prop)) {
                    if (data[prop] === value) {
                		 newRange = prop;
                	 }
                 }
                   }
			updateAfterRangeChange(newRange);
		});
	};


	function updateAfterRangeChange(newRange) {

		var ownerEditable = $("#milestone-owner-cell");
		// update the currentRange with the new value
		config.data.milestone.currentRange = newRange;
		// redraw the table so the project binding is editable or not depending on range
		$('#projects-table').squashTable().fnDraw();
        if (newRange === "GLOBAL") {
			// If new range is global, the owner is not editable and equal to <Admin>
			ownerEditable.html(translator.get("label.milestone.global.owner"));
		} else {
			// If new range is restricted, we must update the owner to the current user
			$.ajax({
                type: "POST",
                url: config.urls.milestoneUrl,
				data: {
					id: "milestone-owner",
					value: config.data.currentUser
				}
            }).then(function (value) {
				// recreate the editable
				ownerEditable.html('<span id="milestone-owner" >' + value + '</span>');
		        createOwnerEditable();
			});
		}

		$('#projects-table').squashTable()._fnAjaxUpdate();
		$('#bind-to-projects-table').squashTable()._fnAjaxUpdate();
	}


    var drawCallBack = function () {

			// the bind to project is editable only if range is restricted
        if (config.data.milestone.currentRange === "RESTRICTED") {
				$("td.binded-to-project").editable('enable');
			} else {
				$("td.binded-to-project").editable('disable');
			}

			// turn elements to editable
        $("td.binded-to-project").editable(function (value, settings) {
			    var returned;
			    var cell = this.parentElement;
			    var id = $("#projects-table").squashTable().getODataId(cell);
			    var origvalue = this.revert;
            if (!currentStatusIsEditable()) {
					displayStatusForbidUnbind();
					returned = origvalue;
				}
				else {
                if (value === "yes") {
						bindProjectInPerimeter(id);
					} else {
                    unbindProjectInPerimeter(id);
						returned = 	origvalue;
					}
				}
            return (returned);
			  }, {
            data: " {'yes':'" + translator.get("squashtm.yesno.true") + "', 'no' :'" + translator.get("squashtm.yesno.false") + "'}",
            type: 'select',
            submit: translator.get("label.Confirm"),
            cancel: translator.get("label.Cancel")
			 });
		};

    function unbindProjectInPerimeter(id) {
			var popup = $("#unbind-project-but-keep-in-perimeter-popup");
			popup.data('entity-id', id);
			popup.confirmDialog('open');
		}

    function bindProjectInPerimeter(id) {
        var url = routing.buildURL('milestone.bind-projects-to-milestone', config.data.milestone.id);
			$.ajax({
            url: url,
            type: 'POST',
            data: {Ids: [id]}
        }).success(function () {
				$('#projects-table').squashTable()._fnAjaxUpdate();
				$('#bind-to-projects-table').squashTable()._fnAjaxUpdate();
			});
		}


    var projectTableRowCallback = function (row, data, displayIndex) {
			// add template icon
			var type = data["raw-type"];
			$(row).find(".type").addClass("type-" + type);

			return row;
		};


    var uncheck = function () {
			var chkbx = $("#bind-to-projects-table").find(":checkbox");
			chkbx.prop('checked', false);
			chkbx.trigger('change');
		};

    var checkAll = function () {
			var chkbx = $("#bind-to-projects-table").find(":checkbox");
			chkbx.prop('checked', true);
			chkbx.trigger('change');
		};

    var invertCheck = function () {
			var checked = $("#bind-to-projects-table").find(":checkbox").filter(":checked");
			var unchecked = $("#bind-to-projects-table").find(":checkbox").filter(":not(:checked)");
			/*
			checked.each(function() {
				$(this).prop('checked', false);
			});
			unchecked.each(function() {
				$(this).prop('checked', true);
			});
			*/
			checked.prop('checked', false);
			unchecked.prop('checked', true);

			checked.add(unchecked).trigger('change');
		};

		// unbind project but keep in perimeter

    $("#unbind-project-but-keep-in-perimeter-popup").confirmDialog().on('confirmdialogconfirm', function () {

			var $this = $(this);
			var id = $this.data('entity-id');
        var ids = !$.isArray(id) ? [id] : id;
			var url = routing.buildURL('milestone.bind-projects-to-milestone', config.data.milestone.id) + "/" + ids.join(',') + "/keep-in-perimeter";
			$.ajax({
                url: url,
                type: 'delete'
			})
            .done(function () {
				$('#projects-table').squashTable()._fnAjaxUpdate();
				$('#bind-to-projects-table').squashTable()._fnAjaxUpdate();
			});
		});

		// Unbind project

		var bindedTable = $("#projects-table").squashTable();
		var bindableTable = $("#bind-to-projects-table").squashTable();


    $("#unbind-project-popup").confirmDialog().on("confirmdialogopen", function () {
			// if the status don't allow unbind kill the popup and show another popup
        if (!currentStatusIsEditable()) {
				$("#unbind-project-popup").confirmDialog("close");
				displayStatusForbidUnbind();
			}
		});

    $("#unbind-project-popup").confirmDialog().on("confirmdialogcancel", function () {
			$("#projects-table").squashTable().deselectRows();
		});


    function displayStatusForbidUnbind() {
			var warn = translator.get({
            errorTitle: 'popup.title.Info',
            errorMessage: 'dialog.milestone.unbind.statusforbid'
			});
			$.squash.openMessage(warn.errorTitle, warn.errorMessage);
		}


    $("#unbind-project-popup").confirmDialog().on('confirmdialogconfirm', function () {

			var $this = $(this);
			var id = $this.data('entity-id');
        var ids = ( !!id) ? [id] : id;
			var url = routing.buildURL('milestone.bind-projects-to-milestone', config.data.milestone.id) + "/" + ids.join(',');

			var selectedRow = $("#projects-table").squashTable().getRowsByIds(ids);

			$.ajax({
                url: url,
                type: 'delete'
			})
            .done(function () {
				$('#projects-table').squashTable()._fnAjaxUpdate();
				$('#bind-to-projects-table').squashTable()._fnAjaxUpdate();
			});


		});

    $("#unbind-project-button").on('click', function () {
			var bindedTable = $("#projects-table").squashTable();
			var ids = bindedTable.getSelectedIds();

        if (ids.length > 0) {
				var popup = $("#unbind-project-popup");
				var popupTxt = $("#unbind-project-popup .generic-error-main");
				popup.data('entity-id', ids);
				popup.confirmDialog('open');
				popupTxt.text(translator.get('dialog.milestone.unbind.milestone.warning.single'));
				//checking for multiselection
            if (ids.length > 1) {
					popupTxt.text(translator.get('dialog.milestone.unbind.milestone.warning.multi'));
				}
			}
        else {
				displayNothingSelected();
			}


		});

    function displayNothingSelected() {
			var warn = translator.get({
            errorTitle: 'popup.title.Info',
            errorMessage: 'message.EmptyTableSelection'
			});
			$.squash.openMessage(warn.errorTitle, warn.errorMessage);
		}


		$("#checkAll").on('click', checkAll);
		$("#uncheckAll").on('click', uncheck);
		$("#invertSelect").on('click', invertCheck);

    $("#bind-project-button").on('click', function () {

        if (currentStatusIsEditable()) {
				bindProjectDialog.formDialog('open');
			} else {
				var warn = translator.get({
                errorTitle: 'popup.title.Info',
                errorMessage: 'dialog.milestone.bind.statusforbid'
				});
				$.squash.openMessage(warn.errorTitle, warn.errorMessage);
			}
		});


    function currentStatusIsEditable() {
			var curStat = config.data.milestone.currentStatus;
        if (curStat == "LOCKED") {
				return false;
			}
			return true;
		}

		var bindProjectDialog = $("#bind-project-dialog");

		bindProjectDialog.formDialog();

    bindProjectDialog.on('formdialogcancel', function () {
			bindProjectDialog.formDialog('close');
		});


		function getCheckedId() {
			$("#bind-to-projects-table").find(":checkbox:checked").parent("td").parent("tr").addClass(
					'ui-state-row-selected');
			var ids = $("#bind-to-projects-table").squashTable().getSelectedIds();
			$("#bind-to-projects-table").squashTable().deselectRows();
			return ids;
		}


    bindProjectDialog.on('formdialogconfirm', function () {

			var ids = getCheckedId();
        if (ids == 0) {
				bindProjectDialog.formDialog('close');
			}
			else {
            var url = routing.buildURL('milestone.bind-projects-to-milestone', config.data.milestone.id);
			$.ajax({
                url: url,
                type: 'POST',
                data: {Ids: ids}
            }).success(function () {
				$('#projects-table').squashTable()._fnAjaxUpdate();
				$('#bind-to-projects-table').squashTable()._fnAjaxUpdate();

				bindProjectDialog.formDialog('close');
			});
			}
		});

		// ************** init *******************

    $(function () {

        var squashSettings = {functions: {}};

        squashSettings.functions.computeSelectionRange = function (row) {
				var baseRow = this.data("lastSelectedRow");
            var baseIndex = baseRow ? baseRow.rowIndex - 1 : 0;
				var currentIndex = row.rowIndex - 1;
				var rangeMin = Math.min(baseIndex, currentIndex);

				var rangeMax = Math.max(baseIndex, currentIndex);
				var rows = this.$("tr");

            return [rangeMin, rangeMax];
			};

        if (config.data.canEdit === true) {
				initJEditables();
			} else {
            squashSettings.functions.drawUnbindButton = function (template, cell) {
								// do nothing so the unbind button are not displayed
            };

			}

			// When you open the dialog, change the message in it (with or without associated project)
        $("#delete-milestone-popup").confirmDialog().on('confirmdialogopen', function () {

				var projects = $("#projects-table").dataTable().fnGetData(0);

            if (projects == null) {
						$("#errorMessageDeleteMilestone").text(translator.get("dialog.delete-milestone.message"));
					}
					else {
						$("#errorMessageDeleteMilestone").text(translator.get("dialog.delete-milestone.messageproject"));
					}

			});

			var $table = $("#projects-table");
			var projectLinkTpl = Handlebars.compile("<a href='" + $table.data("projectUrl") + "'>{{name}}</a>");
			var projectLinkRenderer = function (data, type, row) {
				if (type === "display" && row.link === true) {
                return projectLinkTpl({name: data, "entity-id": row["entity-id"]});
				}
				return data;
			};

			var projectTable = $table.squashTable({
            "bServerSide": false,
            fnDrawCallback: drawCallBack,
            aoColumnDefs: [{
					aTargets: [2],
					mRender: projectLinkRenderer
            }]
			}, squashSettings);

			var bindTable = $("#bind-to-projects-table").squashTable({
				"bServerSide":false,
				"fnRowCallback" : projectTableRowCallback,
				aoColumnDefs: [ {
					aTargets: [3],
					mRender: projectLinkRenderer
				} ]
			}, squashSettings);

			basic.init();
			$("#back").click(clickBackButton);
			initRenameDialog();


			//fix order
			projectTable.on('order.dt search.dt', function () {

            $.each(projectTable.fnGetNodes(), function (index, cell) {
					 cell.firstChild.innerHTML = index + 1;
				 });
			});


			// *******save/restore selection (issue 4816) ********

			// save on click
        bindTable.on('change', 'input[type="checkbox"]', function () {

				var selection = (bindTable.selection || []),
					$cbox = $(this),
					checked = this.checked,
					$row = $cbox.parents('tr:first'),
					id = bindTable.getODataId($row.get(0)),
					index = selection.indexOf(id);

				// add
            if (checked && index === -1) {
					selection.push(id);
				}
				// remove
            else if (index !== -1) {
					selection.splice(index, 1);
				}

				bindTable.selection = selection;

			});

			// restore
        bindTable.drawcallbacks.push(function (oSettins) {
				var selection = bindTable.selection || [];

            bindTable.find('>tbody>tr').each(function () {
                if (selection.indexOf(bindTable.getODataId(this)) > -1) {
						$(this).find('input').prop('checked', true);
					}
				});
			});


		});
});
