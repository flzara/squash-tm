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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="reqs" tagdir="/WEB-INF/tags/requirements-components" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<c:url var="backUrl" value="/requirement-workspace/" />
<c:url var="requirementUrl" value="/requirements/${ requirement.id }" />
<c:url var="linkedReqVersionsUrl" value="/requirement-versions/${ requirementVersion.id }/linked-requirement-versions" />

<f:message var="modifylabel" key="label.Modify" />
<f:message var="removeLabel" key="label.Delete" />
<layout:tree-picker-layout  workspaceTitleKey="workspace.requirement.title"
              highlightedWorkspace="requirement"
              linkable="requirement"
              isSubPaged="true"
              main="linked-requirement-version-manager">

  <jsp:attribute name="head">
    <comp:sq-css name="squash.blue.css" />
    <script type="text/javascript">
      var squashtm = squashtm || {};
      squashtm.bindingsManager = {
    		  bindingsUrl: "${linkedReqVersionsUrl}",
    		  requirementVersion: "${requirementVersion}",
    		  model : ${json:serialize(linkedReqVersionsModel.aaData)}
   	  };
    </script>
  </jsp:attribute>

  <jsp:attribute name="tree">
    <tree:linkables-tree workspaceType="requirement" elementType="requirement" elementId="${requirementVersion.id}" id="linkable-requirements-tree" rootModel="${ linkableLibrariesModel }" />
  </jsp:attribute>

  <jsp:attribute name="tableTitlePane">
      <div class="snap-left" style="height:100%;">
        <h2>
          <f:message var="title" key="requirement-version.linked-requirement-version.panel.title"/>
          <span>${title}</span>
        </h2>
      </div>
      <div class="unsnap"></div>
  </jsp:attribute>
  <jsp:attribute name="tablePane">
      <div class="right btn-toolbar">
        <span class="btn-group">
          <button id="modify-linked-requirement-button" class="sq-btn btn-sm" title="${modifylabel}" value="${modifylabel}">
          ${modifylabel}
            <span class="ui-icon ui-icon-pencil"></span>
          </button>
          <button id="remove-linked-requirement-button" class="sq-btn btn-sm" value="${removeLabel}" title="${removeLabel}">
            ${removeLabel}
            <span class="ui-icon ui-icon-trash"></span>
          </button>
        </span>
      </div>
    <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ requirementUrl }" />

    <reqs:linked-requirements-table
            editable="true"
            model="${linkedReqVersionsModel}"
            requirementVersion="${requirementVersion}"
            batchRemoveButtonId="none"
            milestoneConf="${milestoneConf}"/>

  </jsp:attribute>

  <jsp:attribute name="subPageTitle">
    <h2><c:out value="${requirementVersion.name}"/>&nbsp;:&nbsp;<f:message key="squashtm.library.linked-requirement-versions.title" /></h2>
  </jsp:attribute>

  <jsp:attribute name="subPageButtons">
    <f:message var="backButtonLabel" key="label.Back" />
    <input id="back" type="button" class="button" value="${backButtonLabel}" onClick="document.location.href=squashtm.workspace.backurl"/>
  </jsp:attribute>


  <jsp:attribute name="foot">
  </jsp:attribute>

</layout:tree-picker-layout>

