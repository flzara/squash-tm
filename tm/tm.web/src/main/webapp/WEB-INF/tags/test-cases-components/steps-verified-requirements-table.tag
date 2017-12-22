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
<%@ tag body-content="empty" description="inserts the html table of verified requirements" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ attribute name="testStep" required="true"  type="java.lang.Object" description="the concerned test step" %>
<%@ attribute name="containerId" required="true" description="if of dom container that will hold the table events" %>


<%--
<%@ attribute name="milestoneConf" required="true" type="java.lang.Object" description="an instance of MilestoneFeatureConfiguration"%>
 --%>

<c:url var="tableLanguageUrl" value="/datatables/messages" />
<c:url var="requirementVersionsUrl" value="/requirement-versions" />
<c:url var="verifiedRequirementsUrl" value="/test-cases/${ testStep.testCase.id }/verified-requirement-versions" />
<c:url var="stepVerifiedRequirementsUrl" value="/test-steps/${ testStep.id }/verified-requirement-versions" />
<c:url var="root" value="/" />

<%-- This tag is used for requirement/testStep manager --%>
<%-- Warning ! if you migrate the page in tymeleaf go see : templates/verified-requirements-bloc.frag.html --%>
	<script type="text/javascript">

	require([ "common" ], function(common) {
		require([ "jquery",  "domReady","verified-requirements/TestStepVerifiedRequirementsTable" ], function($, domReady, TestStepVerifiedRequirementsTable) {
			//domReady(function() {
				
				squashtm.verifiedRequirementsTable = new TestStepVerifiedRequirementsTable();
			//});
		});
	});
			if (!squashtm) {
				var squashtm = {};
			}
			if (!squashtm.app) {
				squashtm.app = {
					locale : "<f:message key='squashtm.locale'/>",
					contextRoot : "${root}",
				};
			}
			squashtm.app.verifiedRequirementsTableSettings = {
				containerId : "${containerId}",
				linkable : true,
				url :"${verifiedRequirementsUrl}",
				stepUrl : "${stepVerifiedRequirementsUrl}",
				messages : {
					cancel : "<f:message key='label.Cancel' />",
					ok : "<f:message key='rich-edit.button.ok.label' />",
					remove : "<f:message key='label.Delete'/>",
				},
			};
		</script>
		
		<div class="toolbar">
<input  id="remove-verified-requirements-from-step-button"
				type="button" value="<f:message key='label.removeRequirementsFromTestCase'/>"
				class="button" />
				</div>
<table id="verified-requirements-table" class="unstyled-table"
data-def='datakeys-id=entity-id ,ajaxsource=${ stepVerifiedRequirementsUrl }'>
	<thead>
		<tr>
			<th data-def="select, map=entity-index">#</th>
			<th data-def="sClass=link-checkbox, map=empty-link-checkbox, narrow" style="width:32px;">&nbsp;</th>
			<th data-def="sortable, map=project"><f:message key="label.project" /></th>
			<th data-def="sortable, map=entity-id"><f:message key="label.versionId"/></th>
			<c:if test="${milestoneConf.milestoneDatesColumnVisible}"> 
            <th data-def="sortable, map=milestone-dates"><f:message key="label.Milestone"/></th>
            </c:if>
			<th data-def="sortable, map=reference"><f:message key="label.Reference"/></th>
			<th data-def="sortable, map=name, link=${requirementVersionsUrl}/{entity-id}/info"><f:message key="requirement.name.label" /></th>
			<th data-def="sClass=versionNumber, sortable, map=versionNumber"><f:message key="requirement-version.version-number.label" /></th>
			<th data-def="sortable, map=criticality"><f:message key="requirement.criticality.label"/></th>
			<th data-def="sortable, map=category"><f:message key="requirement.category.label"/></th>
			<th data-def='unbind-button, map=empty-delete-holder, narrow'>&nbsp;</th>
			<th data-def="invisible, map=status">status(masked)</th>
			<th data-def='invisible, map=verifiedByStep'>verifiedByStep(masked)</th>
		</tr>
	</thead>
	<tbody>
		<%-- Will be populated through ajax --%>
	</tbody>
</table>

<div id="remove-verified-requirement-version-dialog" class="popup-dialog not-displayed" title="<f:message key='label.Confirm'/>">
<div>
    <f:message key='dialog.remove-requirement-version-association.message' /></div>
    <div class="popup-dialog-buttonpane">
			<input class="confirm" type="button" value="<f:message key='label.Confirm'/>" />
			 <input class="cancel" type="button" value="<f:message key='label.Cancel'/>" />
	</div>
</div>

<div id="remove-obsolete-verified-requirement-version-dialog" class="popup-dialog not-displayed" title="<f:message key='dialog.obsolete.requirement.version.removal.confirm.title'/>">
<div><f:message key='dialog.obsolete.requirement.version.removal.confirm.text' /></div>
<div class="popup-dialog-buttonpane">
			<input class="confirm" type="button" value="<f:message key='label.Confirm'/>" />
			 <input class="cancel" type="button" value="<f:message key='label.Cancel'/>" />
		</div>
</div>
<div id="remove-verified-requirement-version-from-step-dialog" class="popup-dialog not-displayed" title="<f:message key='label.Confirm'/>">
<div><f:message key='message.remove-requirement-version.step' /></div>
<div class="popup-dialog-buttonpane">
			<input class="confirm" type="button" value="<f:message key='label.Confirm'/>" />
			 <input class="cancel" type="button" value="<f:message key='label.Cancel'/>" />
		</div>
</div>

