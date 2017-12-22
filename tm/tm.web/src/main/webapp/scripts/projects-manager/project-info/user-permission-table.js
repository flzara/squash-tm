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
define([ "jquery", "squashtable" ], function($) {
	var selector = "#user-permissions-table";
	// ***************** user-permissions-table section
	// *****************************
	// ************* combo boxes *******************
	function refreshTableAndPopup(){
		$(selector).squashTable().refresh();		
	}

	function bindSelectChange(settings) {

		$(selector).on(
				'change',
				'td.permissions-cell select',
				function() {

					var projectId = settings.basic.projectId;

					var $this = $(this);
					var tr = $this.parents("tr").get(0);
					var data = $("#user-permissions-table").squashTable().fnGetData(tr);
					var partyId = data['party-id'];

					// the slash at the end of the url below cannot be
					// removed because it would cause the last part of
					// the
					// permission to be interpreted as a file extension
					// by spring MVC
					var url = squashtm.app.contextRoot + "/generic-projects/" + projectId + "/parties/" + partyId +
							"/permissions/" + $this.val() + "/";

					$.ajax({
						type : 'POST',
						url : url,
						dataType : 'json',
						success : refreshTableAndPopup
					// defined in the surrounding html, need to be moved
					// elsewhere too
					});
				});

	}

	function drawCallbackFactory(settings) {

		var comboTemplate = $("div.permission-select-template > select");

		// sort the options of the select
		comboTemplate.append(comboTemplate.find('option').get().sort(function(a, b) {
			return (a.innerText || a.textContent) > (b.innerText || b.textContent);
		}));

		function decorateCombo(cell) {
			var value = cell.html();
			var combo = comboTemplate.clone();
			combo.val(value);
			cell.empty().append(combo);
		}

		return function() {
			var table = this;
			this.find('tbody tr').each(function(){
				var data = table.fnGetData(this);
				if (!! data && data['party-active'] === false){
					$(this).addClass('disabled-transparent');
				}
			});
			table.find('td.permissions-cell').each(function() {
				decorateCombo($(this));
			});
		};
	}

	// ************ init table *********************

	function initUserPermissions(settings) {

		var drawCallback = drawCallbackFactory(settings);
		var userPermissions = settings.basic.userPermissions;

		var language = settings.language;

		var datatableSettings = {
			"fnDrawCallback" : drawCallback,
			"iDeferLoading" : userPermissions.length,
			"bServerSide" : true,
			"sAjaxSource" : squashtm.app.contextRoot + "/generic-projects/" + settings.basic.projectId +
					"/party-permissions",
			"aaData" : userPermissions,
			"sDom" : 'ft<"dataTables_footer"lp>',
			"iDisplayLength" : 25,
			"aaSorting" : [ [ 1, 'asc' ] ],
			"aoColumnDefs" : [ {
				'bSortable' : false,
				'mDataProp' : 'party-index',
				'aTargets' : [ 0 ],
				'sWidth' : '2em',
				'sClass' : 'select-handle centered'
			}, {
				'bSortable' : false,
				'mDataProp' : 'party-id',
				'aTargets' : [ 1 ],
				'sClass' : 'party-id centered',
				'bVisible' : false
			}, {
				'bSortable' : true,
				'mDataProp' : 'party-name',
				'aTargets' : [ 2 ],
				'sClass' : 'party-name centered'
			}, {
				'bSortable' : true,
				'mDataProp' : 'permission-group.qualifiedName',
				'aTargets' : [ 3 ],
				'sClass' : 'permissions-cell centered'
			}, {
				'bSortable' : true,
				'mDataProp' : 'party-type',
				'aTargets' : [ 4 ],
				'sClass' : 'party-type centered'
			}, {
				'bSortable' : false,
				'mDataProp' : 'empty-delete-holder',
				'aTargets' : [ 5 ],
				'sWidth' : '2em',
				'sClass' : "unbind-button centered"
			} ]
		};

		// configure the delete button and the hlink to the user
		var squashSettings = {
			enableHover : true,
			 dataKeys : { entityId : "party-id" },
			confirmPopup : {
				oklabel : language.ok,
				cancellabel : language.cancel
			},
			unbindButtons : {
				popupmessage : language.deleteMessage,
				url : squashtm.app.contextRoot + "/generic-projects/" + settings.basic.projectId +
						"/parties/{party-id}/permissions",
				tooltip : language.deleteTooltip,
				success : refreshTableAndPopup
			},
			bindLinks : {
				list : [ {
					url : squashtm.app.contextRoot + "/administration/parties/{party-id}/info",
					targetClass : 'party-reference'
				} ]
			}

		};

		$(selector).squashTable(datatableSettings, squashSettings);

		bindSelectChange(settings);
	}

	return initUserPermissions;

});
