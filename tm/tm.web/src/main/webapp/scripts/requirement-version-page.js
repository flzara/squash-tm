
define(["module", "jquery", "app/pubsub", "squash.basicwidgets", "app/ws/squashtm.workspace",
	        "contextual-content-handlers", "workspace.event-bus", "jquery.squash.fragmenttabs",
	        "custom-field-values", "squash.configmanager","jeditable.simpleJEditable", "app/ws/squashtm.notification",
	        "workspace.routing",  "squash.translator", "file-upload", "milestones/entity-milestone-count-notifier",
	        "app/squash.wreqr.init", "verifying-test-cases/VerifyingTestCasesPanel", "req-workspace/linked-requirements-panel", "req-workspace/requirement-coverage-stat-view",
					"bugtracker/bugtracker-panel", "jquery.squash.confirmdialog", "jquery.squash.formdialog"],
			function(module, $, pubsub, basicwidg, WS, contentHandlers, eventBus, Frag,
					cufvalues, confman,SimpleJEditable, notification, routing, translator, upload, milestoneNotifier,
					squash, VerifyingTestCasesPanel, LinkedRequirementsPanel, CoveverageStatView, bugtrackerPanel) {

		// event subscription
			pubsub.subscribe('reload.requirement.toolbar', initToolbar);

			pubsub.subscribe('reload.requirement.generalinfo', initGeneralinfos);

			pubsub.subscribe('reload.requirement.verifyingtestcases', initVerifyingtestcases);

			pubsub.subscribe('reload.requirement.linkedrequirementversions', initLinkedRequirementsPanel);

			pubsub.subscribe('reload.requirement.requirementversionrate', initRequirementVersionRates);

			pubsub.subscribe('reload.requirement.audittrail', initAudittrail);

			pubsub.subscribe('reload.requirement.attachments', initAttachments);

			pubsub.subscribe('reload.requirement.bugtracker', initBugtracker);

			pubsub.subscribe('reload.requirement.popups', initPopups);

			pubsub.subscribe('reload.requirement.complete', initFinalize);



			// ********************** library *************************

			function initToolbar(){

				// config
				var config = module.config();

				// name handler
				var nameHandler = contentHandlers.getNameAndReferenceHandler();

				nameHandler.identity = config.basic.identity;
				nameHandler.nameDisplay = "#requirement-name";
				nameHandler.nameHidden = "#requirement-raw-name";
				nameHandler.referenceHidden = "#requirement-raw-reference";

				// rename button
				if (config.permissions.writable && ! config.basic.isSynchronized){
					$("#rename-requirement-button").on('click', function(){
						$("#rename-requirement-dialog").formDialog('open');
					});
				}

				// new version button
				if (config.permissions.creatable && ! config.basic.isSynchronized){
					$( "#new-version-button" ).on( "click", function() {
						$( "#confirm-new-version-dialog" ).confirmDialog( "open" );
					});
				}

				// print button
				$("#print-requirement-version-button").click(function(){
					var url = routing.buildURL('requirementversions', config.basic.currentVersionId);
					window.open(url+"?format=printable", "_blank");
				});

				// milestone count notifier
				milestoneNotifier.newHandler(config.basic.identity);
			}

			function initGeneralinfos(){
				// config
				var config = module.config(),
					baseURL = config.urls.baseURL;

				if (config.permissions.writable){

					// ********** REFERENCE *****************
					var refconf = new SimpleJEditable({
						targetUrl : baseURL,
						componentId : "requirement-reference",
						submitCallback : function (reference) {
							eventBus.trigger('node.update-reference', { identity : config.basic.identity, newRef : reference});
						},
						jeditableSettings : {
							maxLength : 50
						}
					});

					// ********** CRITICALITY ***************
					var criticSelect = $("#requirement-criticality");
					var critconf = confman.getJeditableSelect();
					critconf.data = config.basic.criticalities;
					criticSelect.editable(baseURL, critconf).addClass('editable');


					// ********* CATEGORIES *****************
					var categSelect = $("#requirement-category");
					var catconf = confman.getJeditableSelect();
					catconf.data = confman.toJeditableSelectFormat(config.basic.categories.items, {'code' : 'friendlyLabel'});

					categSelect.editable(function(value, settings){
						var icon,
							cats = config.basic.categories.items;

						// find the icon name
					for (var i=0;i<cats.length;i++){
						if (cats[i].code === value){
							icon = cats[i].iconName;
							// feat 4553
							if (icon === "noicon"){
								icon = "def_cat_noicon";
							}
						}
					}

					// trigger the event
						$.post(baseURL, {id : 'requirement-category', value : value})
						.done(function(response){
							eventBus.trigger('node.attribute-changed', {
								identity : config.basic.identity,
								attribute : 'category-icon',
								value : icon
							});
						});

						// update icon
						var requirement = $("#requirement-icon");
						requirement.attr("class", "");	// reset
					    requirement.addClass("sq-icon sq-icon-" + icon);

					    // in the mean time, must return immediately
						return settings.data[value];

					}, catconf).addClass('editable');


				}


				// ********** STATUS CHANGE *************
				if (config.permissions.status_editable){

					/*************************************************************************************************************
					 *
					 * STATUS CHANGE DROPWDOWN AND CONFIRM LOGIC
					 *
					 * This combobox has an interplay with the status change dialog. Check what's happening in the function
					 * submitOrConfirm, 'OBSOLETE' branch.
					 ************************************************************************************************************/

					var statusChangeSelect = $("#requirement-status"),
						statusSelectConf = confman.getJeditableSelect(),
						statusChangeDialog = $("#requirement-status-confirm-dialog");

					var stsmessages = translator.get({
						'cannot-set-status-allowed' 	: 'requirement.status.notAllowed.approved',
						'cannot-set-status-defaultmsg'	: 'requirement.status.notAllowed.default'
					});

					function submitOrConfirm(settings, widget){
						var selected = this.find('select').val(),
							cansubmit = true;

						// if disabled, tell the user and exit
						if (selected.search(/disabled.*/)!=-1){
							cansubmit = false;
							var msg = ("disabled.APPROVED" === selected ) ?
										stsmessages['cannot-set-status-allowed'] :
										stsmessages['cannot-set-status-defaultmsg'];

							notification.showError(msg);
							widget.reset();
						}

						return cansubmit;
					}


					var finalStatusSelectConf = $.extend(true, statusSelectConf,
						{
							loadurl : baseURL + '/next-status',
							callback : function(){document.location.reload();},
							onsubmit : submitOrConfirm
						}
					);

					statusChangeSelect.editable(baseURL, finalStatusSelectConf)
								.addClass('editable');


				}

				// ******** CUSTOM FIELDS *************************

				if (config.basic.hasCufs){
					var cufurl =  routing.buildURL('customfield.values.get', config.basic.currentVersionId, 'REQUIREMENT_VERSION'),
						mode = (config.permissions.writable) ? 'jeditable' : 'static';
					$.getJSON(cufurl)
					.success(function(jsonCufs){
						cufvalues.infoSupport.init("#requirement-attribut-table", jsonCufs, mode);
					});
				}



			}

			function initVerifyingtestcases() {
				var config = module.config();

				var table = $("#verifying-test-cases-table").squashTable({
					aaData : config.basic.verifyingTestcases
				}, {
					unbindButtons : {
						delegate : "#unbind-active-row-dialog",
						tooltip : translator.get('dialog.unbind-ta-project.tooltip')
					},
					tooltips : [{
						tdSelector : 'td.verif-tc-description',
						value : function(row, data){return data["tc-description"];}
					}]
				});

				if (config.permissions.linkable) {

					$("#verifying-test-case-button").on('click', function(){
						var url = routing.buildURL('requirements.testcases.manager', config.basic.currentVersionId);
						document.location.href=url;
					});

					$( '#remove-verifying-test-case-button' ).on("click", function(event) {
						squash.vent.trigger("verifying-test-cases:unbind-selected", { source: event });
					});

					squash.vent.on("verifyingtestcasespanel:unbound", function(event) {
						var table = $("#verifying-test-cases-table").squashTable();
						eventBus.onContextual('tc-req-links-updated', function(evt) {
							table.refresh();
						});
					});

					var verifPanel = new VerifyingTestCasesPanel({ apiUrl: routing.buildURL('requirements.testcases', config.basic.currentVersionId) });

					eventBus.one("contextualcontent.clear", function() {
						verifPanel.remove();
						verifPanel = undefined;
					});
				}
			}

			function initLinkedRequirementsPanel() {
				var config = module.config();

				function openChooseTypeDialog(reqVersionId, relatedId, isRelatedIdANodeId) {
					var linkTypeDialog = $("#choose-link-type-dialog");
					linkTypeDialog.data('reqVersionId', reqVersionId);
					linkTypeDialog.data('relatedReqNodeId', relatedId);
					linkTypeDialog.data('isRelatedIdANodeId', isRelatedIdANodeId);
					linkTypeDialog.formDialog('open');
				}

				var table = $("#linked-requirement-versions-table").squashTable(
				{
					aaData : config.basic.linkedRequirementVersions
				},
				{
					unbindButtons : {
						delegate : "#unbind-active-linked-reqs-row-dialog",
						tooltip : translator.get('label.Unbind')
					},
					buttons: [{
						tooltip : translator.get("requirement-version.link.type.modify.tooltip"),
						tdSelector : "td.edit-link-type-button",
						uiIcon : "edit-pencil",
						onClick : function(table, cell){
							var row = cell.parentNode.parentNode;
							var modifiedVersionId = table.getODataId(row);
							openChooseTypeDialog(config.basic.currentVersionId, modifiedVersionId, false);
						}
					}],
					tooltips : [{
						tdSelector : 'td.linkedReqDescription',
						value : function(row, data){return data["rv-description"];}
					}]
				});

				if (config.permissions.linkable) {

					var currentVersionId = config.basic.currentVersionId;
					var apiUrl = routing.buildURL('requirements.linkedRequirementVersions', currentVersionId);
         	// 'squash/requirement-versions/{currentReqVerId}/linked-requirement-versions'

					var linkedReqPanel = new LinkedRequirementsPanel({apiUrl: apiUrl});
					var chooseLinkTypeDialog = $("#choose-link-type-dialog").formDialog();
          var addSummaryDialog = $("#add-summary-dialog").messageDialog();

					/* Event handler to access to the manager page */
					$("#bind-requirements-button").on('click', function(){
						var url = routing.buildURL('requirements.linkedRequirementVersions.manager', currentVersionId);
						document.location.href=url;
					});

					/* Trigger unbind-selected-versions event */
					$('#unbind-requirements-button' ).on("click", function(event) {
						squash.vent.trigger("linkedrequirementversions:unbind-selected", { source: event });
					});

					/* Event handler listening to unbound-versions event */
					squash.vent.on("linkedrequirementversions:unbound", function(event) {
						var table = $("#linked-requirement-versions-table").squashTable();
						eventBus.onContextual('req-versions-links-updated', function(evt) { // Cet évènement ne semble jamais triggered ?
							table.refresh();
						});
					});

					/* Event handler listening to clear event*/
					eventBus.one("contextualcontent.clear", function() {
						linkedReqPanel.remove();
						linkedReqPanel = undefined;
						chooseLinkTypeDialog.remove();
						chooseLinkTypeDialog = undefined;
						addSummaryDialog.remove();
						addSummaryDialog = undefined;
					});

					var bind = LinkedRequirementsPanel.bindingActionCallback(apiUrl, "POST");

					/* Event handler listening to the (drag'n) drop of a Requirement Node.
					* 	data: {
          * 		reqNodeId: the id of the current selected Requirement Node in the tree
          * 		relatedReqNodeIds: the ids of the nodes that were dropped.
          *										 	   these ids are already filtered and are only Requirement nodes ids
          *										 		 (no library and no folders).
          * 	}
					*/
					eventBus.onContextual("link-req-node-from-tree", function(evt, data) {
						var relatedReqNodeIds = data.relatedReqNodeIds;

						if(relatedReqNodeIds.length !== 1) {
							notification.showError(translator.get("message.SelectOneRequirement"));
							return;
						} else {
							var relatedReqNodeId = relatedReqNodeIds[0];
							bind(relatedReqNodeId).success(function(rejections) {
								if(Object.keys(rejections).length > 0) {
									// If the link was not created, display summary
		            	showAddSummary(rejections);
								} else {
									// Else, open the popup to choose type
									table.refresh();
									openChooseTypeDialog(currentVersionId, relatedReqNodeId, true);
								}
							});
						}
					});

					function showAddSummary(summary) {
          			if (summary) {
          				var summaryMessages = {
          						alreadyLinkedRejections: translator.get("requirement-version.linked-requirement-versions.rejection.already-linked-rejection"),
          						notLinkableRejections: translator.get("requirement-version.linked-requirement-versions.rejection.not-linkable-rejection"),
          						sameRequirementRejections: translator.get("requirement-version.linked-requirement-versions.rejection.same-requirement-rejection")
          				};

          				var summaryRoot = $( "#add-summary-dialog > ul" );
          				summaryRoot.empty();

          				for(var rejectionType in summary) {
          					var message = summaryMessages[rejectionType];

          					if (message) {
          						summaryRoot.append('<span>' + message + '</span>');
          					}
          				}

          				if (summaryRoot.children().length > 0) {
          					$( "#add-summary-dialog" ).messageDialog("open");
          				}
          			}
          		};

					chooseLinkTypeDialog.on('formdialogopen', function() {

							var self = $(this);
							self.formDialog('setState', 'wait');

							var relatedId = self.data('relatedReqNodeId');
							var isRelatedIdANodeId = self.data('isRelatedIdANodeId');

							var getRelatedReqVersionInfos = LinkedRequirementsPanel.bindingActionCallback(apiUrl, "GET");

							/* Fetching related RequirementVersion attributes in order to display them in the popup. */
							var reqVersionsInfos = getRelatedReqVersionInfos(relatedId, {"isRelatedIdANodeId": isRelatedIdANodeId}).success(
								function(relatedReqVersionInfos) {
									var relatedVersionName = relatedReqVersionInfos.versionName;
									var relatedVersionDescription = relatedReqVersionInfos.versionDescription;
									self.find("#relatedRequirementName").html(relatedVersionName);
									self.find("#relatedRequirementDescription").html(relatedVersionDescription);

									/* Fetching whole list of RequirementVersionTypes to populate comboBox
									TODO: Need an independant url for LinkTypes */
									var comboBox = self.find("#link-types-options");
									comboBox.empty();
									$.ajax({
										url: apiUrl + "/requirement-versions-link-types",
										method: 'GET',
										datatype: 'json'
									}).success(function(typesList) {
										var length = typesList.length;
										for(var i=0; i < length; i++) {
											var type = typesList[i];
											var id = type.id
											var role1 = type.role1;
											var role2 = type.role2;

											/* Creating one option for a non-directional link type, and two for the bi-directional ones. */
											var optionKey_1 = id + "_" + 0;
											var optionLabel_1 = role1 +  " - " + role2;
											comboBox.append('<option value = "' + optionKey_1 + '">' + optionLabel_1 + '</option>');

											if(role1 !== role2) {
												var optionKey_2 = id + "_" + 1;
												var optionLabel_2 = role2 + " - " + role1;
												comboBox.append('<option value = "' + optionKey_2 + '">' + optionLabel_2 + '</option>');
											}
										}
										self.formDialog('setState', 'confirm');
									});
							});

          });

					chooseLinkTypeDialog.on('formdialogconfirm', function() {
						var self = $(this);
            var relatedReqVersionId = self.data("relatedReqNodeId");
            var selectedKey = self.find('option:selected').val();
            var selectedTypeIdAndDirection = selectedKey.split("_");
            var selectedTypeId = parseInt(selectedTypeIdAndDirection[0]);
            var selectedTypeDirection = parseInt(selectedTypeIdAndDirection[1]);
            var params = {
            	reqVersionLinkTypeId: selectedTypeId,
            	isRelatedIdANodeId: self.data("isRelatedIdANodeId"),
            	reqVersionLinkTypeDirection: selectedTypeDirection
            };

            bind(relatedReqVersionId, params).success(function(data){
           		self.formDialog('close');
              table.refresh();
            });
           });

          chooseLinkTypeDialog.on('formdialogcancel', function() {
          	$(this).formDialog('close');
            table.refresh();
          });
				}

			}

			function initAudittrail(){

				var config = module.config();

				// ************************** library ***********************************

				function auditTrailTableRowCallback(row, data, displayIndex) {
					if (data['event-type'] == 'fat-prop') {

						var eventId = data['event-id'];
						var proto = $( '#show-audit-event-details-template' ).clone();
						var url = routing.buildURL('requirements.audittrail.change', eventId);

						proto.removeClass('not-displayed')
							 .find( 'span' )
							 .data('url', url)
							 .attr( 'id', 'show-audit-event-detail:' + eventId )
							 .click(function(){
								 showPropChangeEventDetails(this);
							 });

						$( 'td.event-message-cell', row ).append( proto );
					}
					// synchronized requirements
					if (data['event-type'] === 'sync-creation' || data['event-type'] === 'sync-update'){

						var msgcell = $(row).find('.event-message-cell');

						var srclabel = translator.get('label.source');
						var url = data['event-meta'];

						// 7014 - link must be opened in a new tab
						var link='<a href="'+url+'" target="_blank">'+srclabel+'</a>';

						var msgTemplate = msgcell.html();
						var newmsg = msgTemplate.replace('{url}', link);
						msgcell.html(newmsg);
					}

					return row;
				}

				function showPropChangeEventDetails(link) {

					$.getJSON( $(link).data('url'), function(data, textStatus, xhr) {
						var dialog = $( "#audit-event-details-dialog" );
						$( "#audit-event-old-value", dialog ).html(data.oldValue);
						$( "#audit-event-new-value", dialog ).html(data.newValue);
						dialog.messageDialog("open");
					});
				}

				// ********************* init ******************

				var conf = {
					fnRowCallback : auditTrailTableRowCallback,
					aaData : config.basic.audittrail
				};

				var table=$( "#requirement-audit-trail-table" ).squashTable(conf, {});

				$( "#audit-event-details-dialog" ).messageDialog();

				function updateOnSuccess(event, xhr, settings){
					if (settings.type == 'POST'
						&& !(settings.data && settings.data.match(/requirement-status/g))
						&& !settings.url.match(/versions\/new$/g)) {
					// We refresh tble on POSTs which do not uptate requirement status or create a new version (these ones already
					// refresh the whole page)
					table.refresh();
				}
				}

				/*
				 * we need to make sure this handler on document is removed when the requirement is unloaded, hence the listener
				 * on the contextualcontent.clear the function responsible to unload it must also be unloaded in turn, hence it
				 * registers against eventBus.onContextual (which does just that).
				 */
				$(document).on('ajaxSuccess', updateOnSuccess);
				eventBus.onContextual('contextualcontent.clear', function(){
					$(document).off('ajaxSuccess', updateOnSuccess);
				});

			}

			function initAttachments(){
				var config = module.config();
				upload.initAttachmentsManager({
					baseURL : config.urls.attachmentsURL,
					aaData : config.basic.attachments
				});
			}

			function initBugtracker() {
				var config = module.config();

				if (config.basic.hasBugtracker) {
					var targetUrl = config.urls.btEntityUrl;
					if ($("#tree-panel-left").length != 0) {
						// there is tree
						targetUrl += "/all";
					}else {
						// no tree
						targetUrl += "/info";
					}

					bugtrackerPanel.load({
						url: targetUrl,
						style: "fragment-tab",
						source: targetUrl
					});
				}
			}

      function initRequirementVersionRates() {
        var config = module.config();

        var modelConstructor = Backbone.Model.extend({
          defaults: {
            id : config.basic.requirementVersionId,
            requirementId : config.basic.requirementId,
            currentVersionId : config.basic.currentVersionId,
            projectId : config.basic.projectId
          },
          url : "whatever"
        });

        var model = new modelConstructor();

        var coverageStatView = new CoveverageStatView({
          model : model
        });
      }

			function initPopups(){
				var config = module.config();

				// ******** version creation **********

				var confirmHandler = function() {
					var url = routing.buildURL('requirements.versions.new', config.basic.requirementId);
					var inheritReqLinks = $("#req-version-inheritance").is(':checked');
					var inheritTestcasesReqLinks = $("#req-version-inheritance-testcases").is(':checked');
					var params = {
						inheritReqLinks: inheritReqLinks,
						inheritTestcasesReqLinks: inheritTestcasesReqLinks
					};
					$.ajax({
						type : 'POST',
						url : url,
						data: params
					}).done( function() {
						document.location.reload(true);
					});
				};

				var newversiondialog = $( "#confirm-new-version-dialog" );
				newversiondialog.confirmDialog({confirm: confirmHandler});


				// *********** status modification **********

				if (config.permissions.status_editable){

					var statusChangeDialog = $("#requirement-status-confirm-dialog");
					statusChangeDialog.formDialog();

					statusChangeDialog.on('formdialogconfirm', function(){
						statusChangeDialog.formDialog('close');
						statusChangeDialog.data('confirmed', true);
						var form = statusChangeDialog.data('form');
						form.submit();
					});

					statusChangeDialog.on('formdialogcancel', function(){
						statusChangeDialog.formDialog('close');
						statusChangeDialog.data('confirmed', false);
						var form = statusChangeDialog.data('form');
						form.submit();
					});
				}


		         if (config.permissions.writable && ! config.basic.isSynchronized){

					// ************ rename dialog ********

					var renameDialog = $("#rename-requirement-dialog");
					renameDialog.formDialog();

					renameDialog.on('formdialogconfirm', function(){
						var url = config.urls.baseURL,
							params = { newName : $("#rename-requirement-input").val() };

						$.ajax({
							url : url,
							type : 'POST',
							dataType : 'json',
							data : params
						}).success(function(json){
							renameDialog.formDialog('close');
							eventBus.trigger('node.rename', { identity : config.basic.identity, newName : json.newName});
						});

					});

					renameDialog.on('formdialogcancel', function(){
						renameDialog.formDialog('close');
					});

					renameDialog.on('formdialogopen', function(){
						var name = $.trim($('#requirement-raw-name').text());
						$("#rename-requirement-input").val(name);
					});


		         }




			}

			function initFinalize(){
				WS.init();
				basicwidg.init();
				Frag.init();
			}


	});
