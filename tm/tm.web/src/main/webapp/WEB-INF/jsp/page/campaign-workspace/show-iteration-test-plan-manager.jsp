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
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="it" tagdir="/WEB-INF/tags/iterations-components"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>

<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<c:url var="testPlanUrl" value="/iterations/${iteration.id}/test-plan/" />
<c:url var="iterationUrl" value="/iterations/${iteration.id}/" />
<c:url var="iterationTestPlanUrl" value="/iterations/${iteration.id}/info" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>

<%-- should be programmatically stuffed into page context --%>

<c:set var="writable"         value="${false}" />
<c:set var="moreThanReadOnly" value="${false}" />
<c:set var="attachable"       value="${false}" />
<c:set var="linkable"         value="${false}" />  
<c:set var="deletable"        value="${false}" />
<c:set var="extendedDeletable" value="${false}" />
  
  
<c:if test="${not milestoneConf.locked}">
  
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE" domainObject="${ iteration }">
  <c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ iteration }">
  <c:set var="writable" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ iteration }">
  <c:set var="attachable" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE" domainObject="${ iteration }">
  <c:set var="deletable" value="${true}" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXTENDED_DELETE" domainObject="${ iteration }">
  <c:set var="extendedDeletable" value="${true}" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK" domainObject="${ iteration }">
  <c:set var="linkable" value="${ true }" />
</authz:authorized>

<c:set var="moreThanReadOnly" value="${moreThanReadOnly or writable or attachable or deletable or extendedDeletable or linkable}" />

</c:if>

<%-- ----------------------------------- /Authorization ----------------------------------------------%>




<layout:tree-picker-layout  workspaceTitleKey="workspace.campaign.title"
                            i18nLibraryTabTitle="squashtm.library.test-case.title" 
                            highlightedWorkspace="campaign" 
                            linkable="test-case" 
                            isSubPaged="true">
                            
	<jsp:attribute name="head">
    	<comp:sq-css name="squash.purple.css" />
	</jsp:attribute>
    
      
  <jsp:attribute name="subPageTitle">
    <h2>${iteration.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
  </jsp:attribute>
    
    
  <jsp:attribute name="subPageButtons">
    <f:message var="backButtonLabel" key="label.Back" />
    <input type="button" id="back" class="sq-btn button" value="${backButtonLabel}" 
          onClick="document.location.href=squashtm.workspace.backurl;" />	
  </jsp:attribute>    
  
  <jsp:attribute name="tree">
    <tree:linkables-tree workspaceType="test-case"  elementType="iteration" elementId="${iteration.id}" id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" />
  </jsp:attribute>

    <jsp:attribute name="tableTitlePane">		
      <div class="snap-left" style="height:100%;">			
        <h2>      	
      	   <span><f:message key="label.TestPlan"/></span>
      	</h2>
      </div>	
      <div class="unsnap"></div>
    </jsp:attribute>
	
  
  <jsp:attribute name="tablePane">
    <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ iterationUrl }" />
    <it:iteration-test-plan-manager-table 
        iteration="${iteration}" 
        milestoneConf="${milestoneConf}"
        editable="${writable}"
        linkable="${linkable}"
        reorderable="${linkable}"
        deletable="${deletable}"
        extendedDeletable="${extendedDeletable}"
        />      
    <it:test-suite-managment  iteration="${iteration}"/>  
  </jsp:attribute>


  <jsp:attribute name="foot"> 
  	<script type="text/javascript">
  		  		
    	require([ "common" ], function() {
          	require(["jquery", "iteration-management", "tree", "workspace.event-bus", "squash.translator", "app/ws/squashtm.notification", "app/ws/squashtm.workspace"], function($, iterManager, zetree, eventBus, msg, notification) {
          		
          	  function lock(){
        		  $('#add-items-button').button('disable');
        		  $('#remove-items-button').button('disable');
        	  }
        	  
        	  function unlock(){
        		  $('#add-items-button').button('enable');
        		  $('#remove-items-button').button('enable');
        	  }
          	  
          		$(function(){
          			
          			 $( '#add-items-button' ).on('click', function() {
          				lock();
       					var tree = zetree.get('#linkable-test-cases-tree'); 
       					var ids =	[];
       					var nodes = 0;
       					if( tree.jstree('get_selected').length > 0 ) {
       						 nodes = tree.jstree('get_selected').not(':library').treeNode();
       						 ids = nodes.all('getResId');
       					}	

       					if (ids.length === 0) {
       						notification.showError(msg.get('message.emptySelectionTestCase'));
       						
       					}
       					
                        tree.jstree('deselect_all');
       					
       					if (ids.length > 0) {						
       						 $.post('${ testPlanUrl }', { testCasesIds: ids})
                 				   .done(function(){
                 					unlock();
                  				    eventBus.trigger('context.content-modified');
       							})
       					}
       					else{
       						unlock();
       					}
                     });
                     
                     $("#remove-items-button").on('click', function(){
                       $("#remove-test-plan-button").click();
                     });
                   
          			iterManager.initEvents({});
          			
          		});
          	});
    	});
  	</script>
  </jsp:attribute>

</layout:tree-picker-layout>
