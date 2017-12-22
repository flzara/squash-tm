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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="is" tagdir="/WEB-INF/tags/issues"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >

<c:if test="${ config.prologue }">
  <c:set var="executeThis" value="${config.baseStepUrl}/prologue?optimized=true" />
</c:if>
<c:if test="${ not config.prologue }">
  <c:set var="executeThis" value="${config.baseStepUrl}/index/${config.currentStepIndex -1}?optimized=true" />
</c:if>


<head>
	<layout:common-head />		
	<comp:sq-css name="squash.purple.css" />
	<layout:_common-script-import highlightedWorkspace="" />
   
   
    <script type="text/javascript">
    
    requirejs.config({
        config : {
          'oer-main-page' : ${json:serialize(config)}
        }
      });
    
    require(["common"], function(){   
      require(["oer-main-page"], function(){})
    });
  
  </script>
  
  <%-- Wizard initialization --%> 
  <comp:init-wizards workspace="campaign"/>
  
</head>



<body id="ieo-body">

	<div id="ieo-left-panel" >
		<iframe id="iframe-left" name="frameleft" class="ieo-frame" src="${executeThis}">
		</iframe>
	</div>

	
	<div id="ieo-right-panel">
		<iframe id="iframe-right" name="frameright" class="ieo-frame" >
		</iframe>
	</div>
	
<script type="text/javascript">
publish("reload.oer.panelsready");  
</script>
	
	<%-- structure of the toolbox --%>
  <f:message var="stopTitle"            key="execute.header.button.stop.title" />
  <f:message var="untestableLabel"      key="execute.header.button.untestable.title" />
  <f:message var="blockedTitle"         key="execute.header.button.blocked.title" />
  <f:message var="failureTitle"         key="execute.header.button.failure.title" />
  <f:message var="passedTitle"          key="execute.header.button.passed.title" />
  <f:message var="gotoTitle"            key="execution.IEO.address.go.to.button" />
  <f:message var="previousTitle"        key="execute.header.button.previous.title" />
  <f:message var="nextTitle"            key="execute.header.button.next.title" />
  <f:message  var="nextTestCaseTitle" key="execute.header.button.next-test-case.title" />
  
	<div id="ieo-control" class="ui-state-active not-displayed">		
		<table >		
			<tr>
				<td class="left-aligned">
                  <button class="sq-btn ui-button stop-execution" title="${stopTitle}">
                    <span class="ui-icon ui-icon-power"></span>
                  </button>
                </td>
				<td class="right-aligned">
					<label id="evaluation-label-status"><f:message key="execute.header.status.label" /></label>
					<comp:execution-status-combo name="executionStatus" id="step-status-combo" allowsUntestable="${config.allowsUntestable}" 
                                                  allowsSettled="${config.allowsSettled}" selected="${config.currentStepStatus }"/>
					<c:if test="${config.allowsUntestable}">
						<button class="sq-btn ui-button step-untestable" title="${untestableLabel}">
                          <span class="ui-icon exec-status-untestable"></span>
                        </button>
					</c:if>
					<button class="sq-btn ui-button step-blocked" title="${blockedTitle}" >
                      <span class="ui-icon exec-status-blocked"></span>
                    </button>
					<button class="sq-btn ui-button step-failed" title="${failureTitle}">
                      <span class="ui-icon exec-status-failure"></span>
                     </button>
					<button class="sq-btn ui-button step-succeeded" title="${passedTitle}">
                      <span class="ui-icon exec-status-success"></span>
                    </button>
				</td>
				<td class="centered">
					<input type="button" id="open-address-dialog-button" class="sq-btn" value="${gotoTitle}"/>
					<span class="step-paging"></span>
					<button class="sq-btn ui-button execute-previous-step" title="${prevTitle}" >
                      <span class="ui-icon ui-icon-triangle-1-w"></span>
                    </button>	
					<button class="sq-btn ui-button execute-next-step" title="${nextTitle}">
                      <span class="ui-icon ui-icon-triangle-1-e"></span>
                    </button>
				</td>
				<td class="centered not-displayed execute-next-test-case-panel">					
					<button class="sq-btn ui-button execute-next-test-case" title="${ nextTestCaseTitle }" >
                      <span class="ui-icon ui-icon-seek-next"></span>
                    </button>
				</td>
			</tr>
			<tr>
				<td class="centered" colspan="4">
					<div class="slider"></div>
				</td>
			</tr>
		</table>
	</div>
	
 <script type="text/javascript">
 publish("reload.oer.control");
 </script>
	
	<%-- Popup to enter the url we want the right panel to be filled with --%>
    <f:message var="openurlTitle" key="execution.IEO.address.bar.label"/>
    <f:message var="gotoLabel" key="execution.IEO.address.go.to.button" />
    <div id="open-address-dialog" class="popup-dialog not-displayed"
          title="${openurlTitle}" >
        <label><f:message key="execution.execute.IEO.address.label" /></label>
        <input id="address-input" type="text" size="50" /><br/>
        
        <div class="popup-dialog-buttonpane">
          <input type="button" value="${gotoLabel}" data-def="mainbtn, evt=confirm"/>
        </div>
                 
    </div>
        
<script type="text/javascript">
publish("reload.oer.urldialog");
</script>

	
	<c:if test="${not empty bugTracker and not isOslc}">
	<is:issue-add-popup 
  id="issue-report-dialog" 
  interfaceDescriptor="${interfaceDescriptor}"  
  bugTrackerId="${bugTracker.id}"
  projectId="${projectId}"
  projectNames="${projectNames}"/>		
	</c:if>

	
<c:if test="${not empty bugTracker and isOslc}">
	<is:issue-add-popup-oslc id="issue-report-dialog"
		interfaceDescriptor="${interfaceDescriptor}"  
        bugTrackerId="${bugTracker.id}" 
        projectId="${projectId}" 
        projectNames="${projectNames}"/>
</c:if>	
	
	
  <%-- 
   Here we define a generic error dialog, much like in the notification system
   used in every other pages. The thing is there is no notification section 
   in the OER so we have to insert a copycat of that dialog here 
   so that the js module squashtm.notification can use it seamlessly.
   --%>
	<f:message var="errorTitle" key="popup.title.error"/>
	<f:message var="okLabel" key="label.Ok"/>
	<div id="generic-error-dialog" class="not-displayed popup-dialog" title="${errorTitle}">
	  <div>
	     <div class="display-table-row">
	        <div class="generic-error-main display-table-cell" style="padding-top:20px">
	        </div>
	      </div>
	  </div>
	  <input type="button" value="${okLabel}"/>  
	</div>

<script type="text/javascript">
publish("reload.oer.genericerrordialog");
publish("reload.oer.complete");
</script>
	

</body>
</html>
