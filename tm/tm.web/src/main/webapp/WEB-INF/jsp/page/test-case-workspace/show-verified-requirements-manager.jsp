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
<%@ taglib prefix="sq" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags/test-cases-components" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>


<%--
  requires some things in the context, including :
  - milestoneConf : an instance of MilestoneFeatureConfiguration 
  
 --%>

<c:url var="testCaseUrl" value="/requirements/${ testCase.id }" />
<c:url var="verifiedRequirementsUrl" value="/test-cases/${ testCase.id }/verified-requirement-versions" />

<layout:tree-picker-layout workspaceTitleKey="workspace.test-case.title" 
               highlightedWorkspace="test-case"
               linkable="requirement" isSubPaged="true">
               
  <jsp:attribute name="head">
    <comp:sq-css name="squash.green.css" />
    
    <c:url var="addVerifiedRequirementsUrl" value="/test-cases/${ testCase.id }/verified-requirements" />
    <script type="text/javascript">
  require([ "common" ], function() {
  	require([ "jquery", "squash.translator", "app/ws/squashtm.notification", "jqueryui", "jquery.squash.messagedialog", "datatables", "app/ws/squashtm.workspace" ], function($, msg, notification) {
  		
  	  function lock(){
		  $('#add-items-button').button('disable');
		  $('#remove-items-button').button('disable');
	  }
	  
	  function unlock(){
		  $('#add-items-button').button('enable');
		  $('#remove-items-button').button('enable');
	  }
  		
      $(function() {
        $( "#add-summary-dialog" ).messageDialog();
        
        var summaryMessages = {
          alreadyVerifiedRejections: "<f:message key='test-case.verified-requirement-version.already-verified-rejection' />",
          notLinkableRejections: "<f:message key='test-case.verified-requirement-version.not-linkable-rejection' />",
          noVerifiableVersionRejections: "<f:message key='test-case.verified-requirement-version.no-verifiable-version-rejection' />" 
        };
          
        var showAddSummary = function(summary) {
          if (summary) {
            var summaryRoot = $( "#add-summary-dialog > ul" );
            summaryRoot.empty();
            
            for(rejectionType in summary) {
              var message = summaryMessages[rejectionType];
              
              if (message) {
                summaryRoot.append('<li>' + message + '</li>');
              }
            }
            
            if (summaryRoot.children().length > 0) {
              $( "#add-summary-dialog" ).messageDialog("open");
            }
          }          
        };
        
        var addHandler = function(data) {
      		unlock();
          showAddSummary(data);
          <%-- uh, dependency on something defined in decorate-verified-requirements-table, try 
                        using the event bus instead --%>
          squashtm.verifiedRequirementsTable.refresh();
        };
        
        <%-- verified requirements removal --%>
        $('#remove-items-button').click(function(){
          squashtm.verifiedRequirementsTable.removeSelectedRequirements();
        });
        
        <%-- verified requirements addition --%>

		$( '#add-items-button' ).click(function() {
			lock();
			var tree = $("#linkable-requirements-tree");
			var ids =	[];
			var nodes = 0; 
			if( $( '#linkable-requirements-tree' ).jstree('get_selected').length > 0 ) {
				 nodes = $( '#linkable-requirements-tree' ).jstree('get_selected').not(':library').treeNode();
				 ids = nodes.all('getResId');
			}	

			if (ids.length === 0) {
				notification.showError(msg.get('message.emptySelectionRequirement'));
				return;
			}
			
			tree.jstree('deselect_all');
			
			if (ids.length > 0) {
				$.post('${ addVerifiedRequirementsUrl }', { requirementsIds: ids}, addHandler);
			}
			else{
				unlock();
			}
        });        
      });        
    });
  });
    </script>
  </jsp:attribute>
  
  <jsp:attribute name="tree">
    <tree:linkables-tree workspaceType="requirement" elementType="test-case" elementId="${ testCase.id }" id="linkable-requirements-tree" rootModel="${ linkableLibrariesModel }"/>
  </jsp:attribute>
  
  <jsp:attribute name="tableTitlePane">    
    <div class="snap-left" style="height:100%;">      
      <h2>
        <f:message var="title" key="test-case.verified_requirements.panel.title"/>
        <span>${title}</span>
      </h2>
    </div>            
    <div class="unsnap"></div>
  </jsp:attribute>
  
  <jsp:attribute name="subPageTitle">
    <h2>${testCase.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifiedrequirements.title" /></h2>
  </jsp:attribute>
  
  <jsp:attribute name="subPageButtons">
    <f:message var="backButtonLabel" key="label.Back" />
    <input type="button" class="sq-btn button" id="back" value="${ backButtonLabel }" onClick="document.location.href=squashtm.workspace.backurl"/>  
  </jsp:attribute>    
  
  <jsp:attribute name="tablePane">
  <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ testCaseUrl }" />
    <tc:verified-requirements-table 
        includeIndirectlyVerified="${ false }" 
        linkable="${ true }" 
        verifiedRequirementsTableUrl="${ verifiedRequirementsUrl }" 
        verifiedRequirementsUrl="${verifiedRequirementsUrl }" 
        containerId="contextual-content"
        milestoneConf="${milestoneConf}"/>
        
      <div id="add-summary-dialog" class="not-displayed" title="<f:message key='test-case.verified-requirement-version.add-summary-dialog.title' />">
      <ul><li>summary message here</li></ul>
    </div>
  </jsp:attribute>
</layout:tree-picker-layout>
