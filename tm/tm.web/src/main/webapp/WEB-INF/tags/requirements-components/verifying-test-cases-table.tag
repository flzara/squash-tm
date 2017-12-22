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
<%@ tag body-content="empty" description="jqueryfies a verified reqs table" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="requirementVersion" type="java.lang.Object" required="true" description="The RequirementVersion instance for which we render the verifying testcases" %>
<%@ attribute name="model" type="java.lang.Object" required="true" description="the initial rows of the table"%>
<%@ attribute name="milestoneConf" type="java.lang.Object" required="true" description="an instance of MilestoneFeatureConfiguration" %>
<%@ attribute name="coverageStats" type="java.lang.Boolean" description="Show or " %>


<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<%-- ======================== VARIABLES & URLS ============================ --%>


<s:url var="tableModelUrl" value="/requirement-versions/${requirementVersion.id}/verifying-test-cases/table" />
<c:url var="verifyingTestCasesUrl" value="/requirement-versions/${ requirementVersion.id }/verifying-test-cases" />
<c:url var="tableLanguageUrl" value="/datatables/messages"/>
<c:url var="testCaseUrl" value="/test-cases"/>

<f:message var="labelConfirm" key="label.Confirm"/>
<f:message var="labelCancel"  key="label.Cancel"/>

<c:set var="tblRemoveBtnClause" value=""/>
<c:if test="${editable}" >
<c:set var="tblRemoveBtnClause" value=", unbind-button" />
</c:if>

<%-- ======================== /VARIABLES & URLS ============================ --%>

<c:set var="milestoneVisibility" value="${(milestoneConf.milestoneDatesColumnVisible) ? '' : ', invisible'}"/>

<table id="verifying-test-cases-table" class="unstyled-table" data-def="ajaxsource=${tableModelUrl}, deferloading=${model.iTotalRecords},
  datakeys-id=tc-id, pre-sort=2-asc, pagesize=50 ">
  <thead>
    <tr>
      <th data-def="map=tc-index, select">#</th>
      <th data-def="map=project-name, sortable"><f:message key="label.project" /></th>
      <th data-def="sortable, map=milestone-dates, tooltip-target=milestone ${milestoneVisibility}"><f:message key="label.Milestones"/></th>
      <th data-def="map=tc-reference, sortable"><f:message key="test-case.reference.label" /></th>
      <th data-def="map=tc-name, sClass=verif-tc-description, sortable, link=${testCaseUrl}/{tc-id}/info"><f:message key="test-case.name.label" /></th>
      <th data-def="map=tc-type, sortable"><f:message key="verifying-test-cases.table.column-header.type.label"/></th>
      <th data-def="map=empty-delete-holder${tblRemoveBtnClause}">&nbsp;</th>
      <th data-def="map=milestone, invisible"></th>
    </tr>
  </thead>
  <tbody>
  </tbody>
</table>


<script type="text/x-handlebars-template" id="unbind-dialog-tpl">
  <div id="{{dialogId}}" class="not-displayed popup-dialog" title="<f:message key='dialog.remove-testcase-associations.title'/>">
    <div data-def="state=confirm-deletion">
      <span><f:message key="dialog.remove-testcase-requirement-association.message"/></span>
    </div>
    <div data-def="state=multiple-tp">
      <span><f:message key="dialog.remove-testcase-requirement-associations.message"/></span>
    </div>
    <div class="popup-dialog-buttonpane">
      <input type="button" class="button" value="${labelConfirm}" data-def="evt=confirm, mainbtn"/>
      <input type="button" class="button" value="${labelCancel}" data-def="evt=cancel"/>
    </div>
  </div>
</script>

