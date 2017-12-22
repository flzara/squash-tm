<%--

        This file is part of the Squashtest platform.
        Copyright (C) Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ attribute name="requirementVersion" required="true" type="java.lang.Object" rtexprvalue="true" %>
<%@ attribute name="jsonCriticalities" required="true" rtexprvalue="true" %>
<%@ attribute name="jsonCategories" required="true" rtexprvalue="true" %>
<%@ attribute name="verifyingTestCasesModel" required="true" rtexprvalue="true" type="java.lang.Object" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="reqs" tagdir="/WEB-INF/tags/requirements-components" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="issues" tagdir="/WEB-INF/tags/issues"%>


<s:url var="requirementUrl" value="/requirement-versions/${ requirementVersion.id }"/>
<c:url var="attachmentsUrl" value="/attach-list/${requirementVersion.attachmentList.id}/attachments"/>
<c:url var="btEntityUrl" value="/bugtracker/requirement-version/${requirementVersion.id}"/>


<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="cancelLabel" key="label.Cancel"/>
<f:message var="okLabel" key="label.Ok"/>

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<%-- that page won't be editable if
   * the user don't have the correct permission,
   * the requirement status doesn't allow it.
   * one of the milestones this version belongs to doesn't allow modification

--%>


<c:set var="attachable" value="${false}"/>
<c:set var="moreThanReadOnly" value="${false}"/>
<c:set var="writable" value="${false}"/>
<c:set var="deletable" value="${false}"/>
<c:set var="creatable" value="${false}"/>
<c:set var="linkable" value="${false}"/>
<c:set var="status_editable" value="${false}"/>


<%-- permission 'linkable' is not subject to the milestone statuses, ACL only --%>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK" domainObject="${ requirementVersion }">
	<c:set var="linkable" value="${ requirementVersion.linkable }"/>
</authz:authorized>

<%-- other permissions. ACL should be evaluated only if the milestone statuses allows it.  --%>
<c:if test="${not milestoneConf.locked}">

	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ requirementVersion }">
		<c:set var="attachable" value="${ requirementVersion.modifiable }"/>
	</authz:authorized>
	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirementVersion }">
		<c:set var="writable" value="${ requirementVersion.modifiable }"/>
		<c:set var="status_editable" value="${ requirementVersion.status.allowsStatusUpdate }"/>
	</authz:authorized>

</c:if>

<%-- ----------------------------------- /Authorization ----------------------------------------------%>
<%-- ----------------------------------- header ----------------------------------------------%>

<script type="text/javascript">
	requirejs.config({
		config: {
			'requirement-version-page': {
				basic: {
					identity: {resid: ${requirementVersion.requirement.id}, restype: "requirements"},
					requirementId: ${requirementVersion.requirement.id},
					currentVersionId: ${requirementVersion.id},
					criticalities: ${json:serialize(criticalityList)},
					categories: ${json:serialize(categoryList)},
					verifyingTestcases: ${json:serialize(verifyingTestCasesModel.aaData)},
					linkedRequirementVersions: ${json:serialize(linkedRequirementVersionsModel.aaData)},
					attachments: ${json:serialize(attachmentsModel.aaData)},
					audittrail: ${json:serialize(auditTrailModel.aaData)},
					hasBugtracker: ${requirementVersion.project.bugtrackerConnected},
					hasCufs: ${hasCUF},
					requirementVersionId: ${requirementVersion.id},
					projectId: ${requirementVersion.requirement.project.id}
				},
				permissions: {
					moreThanReadOnly: ${moreThanReadOnly},
					attachable: ${attachable},
					writable: ${writable},
					deletable: ${deletable},
					creatable: ${creatable},
					linkable: ${linkable},
					status_editable: ${status_editable}
				},
				urls: {
					baseURL: "${requirementUrl}",
					attachmentsURL: "${attachmentsUrl}",
					btEntityUrl: "${btEntityUrl}"

				}
			}
		}
	});

	require(['common'], function () {
		require(['requirement-version-page'], function () {
		});
	});

</script>
<%-- TODO find a way to ditch that requirement statement --%>


<div class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	<div style="float: left; height: 100%;">
		<h2>

			<c:set var="completeRequirementName" value="${ requirementVersion.name }"/>
			<c:if test="${ not empty requirementVersion.reference && fn:length(requirementVersion.reference) > 0 }">
				<c:set var="completeRequirementName"
					   value='${ requirementVersion.reference } - ${ requirementVersion.name }'/>
			</c:if>
			<a id="requirement-name" href="${ requirementUrl }/info"><c:out value="${ completeRequirementName }"/></a>
			<%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
			<span id="requirement-raw-reference" style="display: none">
        <c:out value="${ requirementVersion.reference }"/>
      </span>
			<span id="requirement-raw-name" style="display: none">
        <c:out value="${ requirementVersion.name }"/>
      </span>
		</h2>
	</div>
	<div class="unsnap"></div>
</div>
<%-- ----------------------------------- /header ----------------------------------------------%>
<%-- ----------------------------------- toolbar ----------------------------------------------%>
<div id="requirement-toolbar" class="toolbar-class ui-corner-all">
	<div class="toolbar-information-panel">
		<comp:general-information-panel auditableEntity="${ requirementVersion }" entityUrl="${ requirementUrl }"/>
	</div>


	<div class="toolbar-button-panel">
		<c:if test="${ writable }">
			<input type="button" value='<f:message key="requirement.button.rename.label" />'
				   value='<f:message key="requirement.button.rename.label" />' id="rename-requirement-button"
				   class="sq-btn"/>
		</c:if>
		<input type="button" value="<f:message key='label.print'/>"
			   title='<f:message key="requirement.button.new-version.label" />' id="print-requirement-version-button"
			   class="sq-btn"/>
	</div>

	<comp:milestone-messages milestoneConf="${milestoneConf}" nodeType="requirement"/>

	<div class="unsnap"></div>
</div>
<script type="text/javascript">
	publish('reload.requirement.toolbar');
</script>

<%-- ----------------------------------- /toolbar ----------------------------------------------%>
<%-- -------------------------------------------------------- TABS-----------------------------------------------------------%>
<csst:jq-tab>
	<div class="fragment-tabs fragment-body">
		<ul class="tab-menu">
			<li>
				<a href="#tabs-1">
					<f:message key="tabs.label.information"/>
				</a>
			</li>
			<c:if test="${milestoneConf.displayTab}">
				<li>
					<a href="${requirementUrl}/milestones/panel"><f:message key="tabs.label.milestone"/></a>
				</li>
			</c:if>
			<li>
				<a href="#tabs-2">
					<f:message key="label.Attachments"/>
					<c:if test="${ requirementVersion.attachmentList.notEmpty }">
						<span class="hasAttach">!</span>
					</c:if>
				</a>
			</li>
			<c:if test="${requirementVersion.project.bugtrackerConnected}">
				<li>
						<%-- div#bugtracker-section-main-div is declared in tagfile issues:bugtracker-panel.tag --%>
					<a href="#bugtracker-section-main-div"><f:message key="tabs.label.issues"/></a>
				</li>
			</c:if>
		</ul>
			<%-- --------------------------------------------- tab1 Information----------------------------------------------%>
		<div id="tabs-1">
			<c:if test="${ writable }">
				<c:set var="descrRicheditAttributes"
					   value="class='editable rich-editable' data-def='url=${requirementUrl}'"/>
			</c:if>
			<f:message var="requirementInformationPanelLabel" key="requirement.panel.general-informations.title"/>


			<comp:toggle-panel id="requirement-information-panel"
							   title='${requirementInformationPanelLabel} <span class="small txt-discreet">[ID = ${requirementVersion.requirement.id }]</span>'
							   open="true">
        <jsp:attribute name="body">
			<div id="edit-requirement-table" class="display-table">
				<div class="display-table-row">
					<label for="requirement-version-number"><f:message
						key="requirement-version.version-number.label"/></label>
					<div class="display-table-cell"
						 id="requirement-version-number">${ requirementVersion.versionNumber }&nbsp;&nbsp;
					</div>
				</div>


				<div class="display-table-row">
					<label class="display-table-cell" for="requirement-reference"><f:message
						key="label.Reference"/></label>
					<div id="requirement-reference">${ requirementVersion.reference }</div>
				</div>

				<div class="display-table-row">
					<label for="requirement-status" class="display-table-cell"><f:message
						key="requirement.status.combo.label"/></label>
					<div class="display-table-cell">
						<div id="requirement-status"><comp:level-message level="${ requirementVersion.status }"/></div>
					</div>

				</div>
			</div>
		</jsp:attribute>
			</comp:toggle-panel>

				<%--------------------------- Attributs section ------------------------------------%>

			<comp:toggle-panel id="requirement"
							   titleKey="label.Attributes"
							   open="true">

        	<jsp:attribute name="body">
        	       <div id="requirement-attribut-table" class="display-table">
					   <div class="display-table-row">
						   <label for="requirement-criticality" class="display-table-cell"><f:message
							   key="requirement.criticality.combo.label"/></label>
						   <div class="display-table-cell">
							   <div id="requirement-criticality"><comp:level-message
								   level="${ requirementVersion.criticality }"/></div>
						   </div>
					   </div>
					   <div class="display-table-row">
						   <label for="requirement-category" class="display-table-cell"><f:message
							   key="requirement.category.combo.label"/></label>
						   <div class="display-table-cell">
							   <span id="requirement-icon" style="vertical-align : middle;"
									 class="sq-icon sq-icon-${(requirementVersion.category.iconName == 'noicon') ? 'def_cat_noicon' : requirementVersion.category.iconName}"></span>
							   <span id="requirement-category">
                                    <s:message code="${requirementVersion.category.label}"
											   text="${requirementVersion.category.label}" htmlEscape="true"/>
                                </span>
						   </div>
					   </div>

				   </div>
        		</jsp:attribute>
			</comp:toggle-panel>


			<script type="text/javascript">
				publish('reload.requirement.generalinfo');
			</script>


			<comp:toggle-panel id="requirement-description-panel" titleKey="label.Description" open="true">
        <jsp:attribute name="body">
					<div
						id="requirement-description" ${descrRicheditAttributes}>${ requirementVersion.description }</div>
		</jsp:attribute>
			</comp:toggle-panel>

				<%--------------------------- coverage stat section ------------------------------------%>
			<comp:toggle-panel id="coverage-stat-requirement-panel" titleKey="requirement.rate.panel.title" open="true">
			<jsp:attribute name="panelButtons">
				<div class="icon-helper" title='<f:message key="requirement.rate.doc" />'></div>
			</jsp:attribute>
				<jsp:attribute name="body">
				<reqs:requirement-version-coverage-stats/>
			</jsp:attribute>
			</comp:toggle-panel>


				<%------------------------- Verifying TestCase Section -------------------------%>

			<comp:toggle-panel id="verifying-test-case-panel" titleKey="requirement.verifying_test-case.panel.title"
							   open="true">
        <jsp:attribute name="panelButtons">
			<c:if test="${ linkable }">
				<f:message var="associateLabel" key="requirement.verifying_test-case.manage.button.label"/>
				<f:message var="removeLabel" key="label.removeRequirementsTestCase"/>
					<button id="verifying-test-case-button" class="sq-icon-btn btn-sm" type="submit"
							title="${associateLabel}">
						<span class="ui-icon ui-icon-plus squared-icons">+</span>
					</button>
                    <button id="remove-verifying-test-case-button" class="sq-icon-btn btn-sm" type="submit"
							title="${removeLabel}">
						<span class="ui-icon ui-icon-minus squared-icons">-</span>
					</button>
			</c:if>
		</jsp:attribute>

				<jsp:attribute name="body">
			<reqs:verifying-test-cases-table
				batchRemoveButtonId="remove-verifying-test-case-button"
				editable="${ linkable }"
				model="${verifyingTestCaseModel}"
				requirementVersion="${requirementVersion}"
				milestoneConf="${milestoneConf}"
			/>
		</jsp:attribute>
			</comp:toggle-panel>

			<script type="text/javascript">
				publish('reload.requirement.verifyingtestcases');
			</script>

				<%------------------------- /Verifying TestCase Section -------------------------%>

				<%------------------------- Linked Requirements Section -------------------------%>

			<comp:toggle-panel id="linked-requirement-version-panel"
							   titleKey="requirement-version.linked-requirement-version.panel.title" open="true">

      <jsp:attribute name="panelButtons">
      	<c:if test="${ linkable }">
      		<f:message var="associateLabel"
					   key="requirement-version.linked-requirement-version.panel.button.add.label"/>
      		<f:message var="removeLabel"
					   key="requirement-version.linked-requirement-version.panel.button.remove.label"/>
      		<button id="bind-requirements-button" class="sq-icon-btn btn-sm" type="submit" title="${associateLabel}">
				<span class="ui-icon ui-icon-plus squared-icons">+</span>
			</button>
      		<button id="unbind-requirements-button" class="sq-icon-btn btn-sm" type="submit" title="${removeLabel}">
				<span class="ui-icon ui-icon-minus squared-icons">-</span>
			</button>
      	</c:if>
      </jsp:attribute>

				<jsp:attribute name="body">
        <c:if test="${ linkable }">
          <div class="jstree-drop">
        </c:if>
          <reqs:linked-requirements-table batchRemoveButtonId="unbind-requirements-button"
										  requirementVersion="${requirementVersion}"
										  editable="${ linkable }" model="${linkedRequirementVersions}"
										  milestoneConf="${milestoneConf}"/>
         <c:if test="${ linkable }">
            </div>
         </c:if>
      </jsp:attribute>

			</comp:toggle-panel>

			<script type="text/javascript">
				publish('reload.requirement.linkedrequirementversions');
			</script>

				<%------------------------- /Linked Requirements Section -------------------------%>

				<%--------------- Audit Trail ------------------------------------%>
			<reqs:requirement-version-audit-trail requirementVersion="${ requirementVersion }"
												  tableModel="${auditTrailModel}"/>
			<script type="text/javascript">
				publish('reload.requirement.audittrail');
			</script>
		</div>
			<%-- --------------------------------------------- /tab1 Information----------------------------------------------%>
			<%-- --------------------------------------------- tab2 Attachments ----------------------------------------------%>
		<at:attachment-tab tabId="tabs-2" entity="${ requirementVersion }" editable="${ attachable }"
						   tableModel="${attachmentsModel}" autoJsInit="${false}"/>
			<%-- --------------------------------------------- /tab2 Attachments ----------------------------------------------%>
		<script type="text/javascript">
			publish('reload.requirement.attachments');
		</script>

			<%-- ----------------------- bugtracker (if present)----------------------------------------%>
		<c:if test="${requirementVersion.project.bugtrackerConnected}">
			<script type="text/javascript">
				publish('reload.requirement.bugtracker');
			</script>
			<issues:butracker-panel entity="${requirementVersion}"/>
		</c:if>
			<%-- ----------------------- /bugtracker (if present)----------------------------------------%>

	</div>
</csst:jq-tab>
<%-- --------------------------------------------------------------- /TABS ------------------------------------------------------------%>

<!------------------------------------------ POPUPS ------------------------------------------------------>
<%------------------- confirm new status if set to obsolete ---------------------%>
<div class="not-displayed">
	<c:if test="${ writable }">

		<f:message var="renameDialogTitle" key="dialog.rename-requirement.title"/>
		<div id="rename-requirement-dialog" class="not-displayed popup-dialog"
			 title="${renameDialogTitle}">

			<c:if test="${milestoneConf.showMultipleBindingMessage}">
				<div data-milestones="${milestoneConf.totalMilestones}"
					 class="milestone-count-notifier centered std-margin-top std-margin-bottom ${(milestoneConf.multipleBindings) ? '' : 'not-displayed'}">
					<f:message key="message.RenameRequirementBoundToMultipleMilestones"/>
				</div>
			</c:if>

			<label><f:message key="dialog.rename.label"/></label>
			<input type="text" id="rename-requirement-input" maxlength="255" size="50"/><br/>
			<comp:error-message forField="name"/>


			<div class="popup-dialog-buttonpane">
				<input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn"/>
				<input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
			</div>

		</div>

	</c:if>

	<%------------------------------- confirm new status if set to obsolete popup---------------------%>
	<c:if test="${status_editable}">

		<f:message var="statusChangeDialogTitle" key="dialog.requirement.status.confirm.title"/>
		<div id="requirement-status-confirm-dialog" class="not-displayed"
			 title="${statusChangeDialogTitle}">

			<span><f:message key="dialog.requirement.status.confirm.text"/></span>

			<div class="popup-dialog-buttonpane">
				<input type="button" value="${confirmLabel}" data-def="mainbtn, evt=confirm"/>
				<input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
			</div>
		</div>
	</c:if>

</div>
<script type="text/javascript">
	publish('reload.requirement.popups');
</script>
<%-- -----------------------------------/POPUPS ----------------------------------------------%>

<script type="text/javascript">
	publish('reload.requirement.complete');
</script>

