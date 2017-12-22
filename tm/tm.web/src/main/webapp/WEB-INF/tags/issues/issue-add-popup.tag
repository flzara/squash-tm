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
<%@ tag description="Popup filling informations regarding issues"
	body-content="empty"%>

<%@ attribute name="id" required="true"
	description="the desired name for that popup"%>
<%@ attribute name="interfaceDescriptor" type="java.lang.Object"
	required="true"
	description="an object holding the labels for the interface"%>
<%@ attribute name="bugTrackerId" required="true"
	description="id of the entity's project bug-tracker"%>
<%@ attribute name="projectId" required="false"
	description="id the project (on squash)"%>
<%@ attribute name="projectNames" required="false"
  description="names of the remote project (hosted on the bugtracker)"%>

  
  
<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<f:message var="addIssueLabel" key="label.Add" />
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="closeLabel" key="label.Close" />
<f:message var="bugreportTitle" key="dialog.issue.report.title" />

<c:url var="remoteIssues" value="/bugtracker/find-issue/" />

<%--
 this is not a full form dialog, although it could use some of its features.
 --%>
 
 <div>
<div id="${id}" class="not-displayed popup-dialog" title="${bugreportTitle}">
  <div class="issue-report-dialog">
  
    <div class="pleasewait" >
      <comp:waiting-pane/>    
    </div>
    
    <div class="content"> 
  
      <div class="issue-report-error">
        <comp:error-message forField="bugtracker" />
      </div>
      <form>
        <div class="attach-issue">
          <span class="issue-radio">
            <input type="radio" name="add-issue-mode" class="attach-radio"
              value="attach" />
            <span class="issue-radio-label"><f:message
                  key="dialog.issue.radio.attach.label" /></span> <!--  I don't want a <label> here because of the default style -->
          </span>
          <label>${interfaceDescriptor.tableIssueIDHeader}</label>
          <input type="text" class="id-text" name="issue-key" value="" />
          <f:message var="searchIssueLabel" key="label.Search" />
          <input type="button" name="search-issue"
              value="${searchIssueLabel}" />
        </div>
      
        <div class="issue-report-break">
          
        </div>
        
        <span class="issue-radio">
          <input type="radio" class="report-radio" name="add-issue-mode"  value="report" />
          <span class="issue-radio-label">
            <f:message key="dialog.issue.radio.new.label" />
          </span>
            <span id="project-selector" />
         </span>
           <script id="project-selector-tpl" type="text/x-handlebars-template">         
               <select>
                {{#each options}}
                  <option value="{{this.code}}">{{this.value}}</option>
                {{/each}}
                </select>
           </script>
         <div class="issue-report-fields">
          <%-- populated by javascript --%>
        </div>
      </form>
    </div>
  </div>

  <div class="popup-dialog-buttonpane">
    <input type="button" value="${addIssueLabel}" class="post-button" data-def="evt=confirm"/>
    <input type="button" value="${closeLabel}" class="cancel-button" data-def="evt=cancel"/>
  </div> 

</div>
</div>



<%-- state manager code of the popup --%>
<script type="text/javascript">
require( ["common"], function(){
		require(["jquery","bugtracker/bugtracker-panel"], function($){
	$(function(){
			 
			var conf = {					
				searchUrl : "${remoteIssues}",
				bugTrackerId : "${bugTrackerId}",
				labels : ${ json:serialize(interfaceDescriptor) },
				currentProjectId : ${projectId},
				projectNames : ${projectNames}
			};
			
			
			squashtm.bugReportPopup = $("#${id}").btIssueDialog(conf);	
			
		});	
	});
});
</script>
