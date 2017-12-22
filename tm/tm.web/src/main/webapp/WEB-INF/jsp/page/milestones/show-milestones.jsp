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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<s:url var="administrationUrl" value="/administration" />

<f:message var="addAnotherLabel"       key="label.addAnother"/>
<f:message var="addLabel"       key="label.Add"/>
<f:message var="confirmLabelDelete"   key="label.ConfirmDelete"/>
<f:message var="confirmLabel"   key="label.Confirm"/>
<f:message var="cancelLabel"    key="label.Cancel"/>
<f:message var="closeLabel"    key="label.Close"/>
<jsp:useBean id="now" class="java.util.Date"  />   
<f:message var="dateFormat" key="squashtm.dateformatShort" />

<layout:info-page-layout titleKey="squashtm.milestone.title" isSubPaged="true" main="milestone-manager">
  <jsp:attribute  name="head">	
    <comp:sq-css name="squash.grey.css" />
      <script type="text/javascript">
      squashtm = squashtm || {}
      squashtm.milestoneManager = {
        data: {
          currentUser : "${currentUser}",
          isAdmin : ${isAdmin},
          editableMilestoneIds : ${editableMilestoneIds}
        }
      }
      </script>
<script id="confirm-milestone-switch-tpl" type="text/x-handlebars-template">
<div class='display-table-row'>
  <div class='display-table-cell warning-cell'>
    <div class='generic-question-signal'></div>
  </div>
  <div class='display-table-cell'>
    <span>{{first}}</span>
    <span class='red-warning-message'>{{second}}</span>
    <span>{{third}}</span>
    <span class='bold-warning-message'>{{fourth}}</span>
  </div>
</div>
</script>
  </jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>
	</jsp:attribute>
		<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.milestone.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<a class="sq-btn" href="${administrationUrl}">${backButtonLabel}</a>
	</jsp:attribute>
	<jsp:attribute name="informationContent">
		<c:url var="milestonesUrl" value="/administration/milestones/list" />
		<c:url var="addMilestoneUrl" value="/administration/milestones" />
		<c:url var="milestoneDetailsBaseUrl" value="/milestones" />
		<c:url var="dtMessagesUrl" value="/datatables/messages" />

		
		<%----------------------------------- Milestone Table -----------------------------------------------%>

<div class="fragment-body">

  <div class="cf">
    <sec:authorize access=" hasRole('ROLE_ADMIN')">
    <div class="btn-toolbar left">
      <div id="milestone-feat-switch" class="btn-group" data-api="<c:url value='/features/milestones' />">
        <label for="milestone-feat-switch"><f:message key='message.UseMilestoneFeature' /></label>
        <button id="milestone-feat-on" class="sq-btn  btn-sm ${ milestoneFeatureEnabled ? 'active' : ''}" type="button"><f:message key='label.active' /></button>
        <button id="milestone-feat-off" class="sq-btn  btn-sm ${ not milestoneFeatureEnabled ? 'active' : ''}" type="button"><f:message key='label.inactive' /></button>
      </div>
      <script type="text/javascript">publish("loaded.milestoneFeatureSwitch");</script>
    </div>
    </sec:authorize>
    <c:set var="actionState" value="${ milestoneFeatureEnabled ? '' : 'disabled=\"disabled\"' }" />
    <div class="btn-toolbar right">
      <button id="new-milestone-button" ${ actionState } class="sq-btn milestone-dep" title="<f:message key='milestone.tooltip.add' />">
        <span class="ui-icon ui-icon-plusthick">+</span>&nbsp;<f:message key="label.Add" />
      </button>
      
      <button id="clone-milestone-button" ${ actionState } class="sq-btn milestone-dep" title="<f:message key='milestone.tooltip.duplicate' />">
        <f:message key="label.milestone.duplicate" />
      </button>
       
      <button id="synchronize-milestone-button" ${ actionState } class="sq-btn milestone-dep" title="<f:message key='milestone.tooltip.synchronize' />">
        <f:message key="label.milestone.synchronize" />
      </button>
      
      <button id="delete-milestone-button" ${ actionState } class="sq-btn milestone-dep" title="<f:message key='milestone.tooltip.delete' />">
        <span class="ui-icon ui-icon-trash">-</span>&nbsp;<f:message key="label.Delete" />
      </button>
    </div>
  </div>
  <table id="milestones-table" class="unstyled-table" data-def="ajaxsource=${milestonesUrl}, hover, filter, pre-sort=3-desc">
    <thead>
      <tr>
        <th data-def="map=index, select">#</th>
        <th data-def="map=label, sortable, link=${milestoneDetailsBaseUrl}/{entity-id}/info"  class="datatable-filterable"><f:message key="label.Milestones" /></th>
        <th data-def="map=status, sortable" class="datatable-filterable"><f:message key="label.Status"   /></th>
        <th data-def="map=endDate, sortable, sType=squashdateShort"><f:message key="label.EndDate"/></th>
        <th data-def="map=nbOfProjects, sortable"><f:message key="label.projectsSharp"/></th>
        <th data-def="map=range, sortable"><f:message key="label.Range" /></th>
        <th data-def="map=owner, sortable"><f:message key="label.Owner" /></th>
        <th data-def="map=description, sortable"><f:message key="label.Description" /></th>
        <th data-def="map=created-on, sortable, sType=squashdateShort"><f:message key="label.CreatedOn" /></th>
        <th data-def="map=created-by, sortable" ><f:message key="label.createdBy" /></th>
        <th data-def="map=last-mod-on, sortable, sType=squashdateLong"><f:message key="label.modifiedOn" /></th>
        <th data-def="map=last-mod-by, sortable"><f:message key="label.modifiedBy" /></th> 
        <th data-def="map=delete, delete-button=#delete-milestone-popup"></th>
      </tr>
    </thead>
    <tbody>
    </tbody>
  </table>
  <%-- 
   Here we define a generic error dialog, much like in the notification system
   used in every other pages. The thing is there is no notification section 
   in the OER so we have to insert a copycat of that dialog here 
   so that the js module squashtm.notification can use it seamlessly.
   --%>
	<f:message var="errorTitle" key="popup.title.error"/>
	<f:message var="noSelectedMilestone" key="dialog.milestone.noselection"/>
	<f:message var="okLabel" key="label.Ok"/>
	<f:message var="closeLabel" key="label.Close"/>
	<div id="milestone-noselection-error-dialog" class="not-displayed popup-dialog" title="${errorTitle}">
	  <div>
	     <div class="display-table-row">
	        <div class="generic-error-main display-table-cell" style="padding-top:20px">
	        </div>
	        <div class="display-table-cell">		
			 	<p>
	              <span>	${noSelectedMilestone} </span>
	            </p>     	            
          </div>
	      </div>
	  </div>
	  <input type="button" value="${closeLabel}"/>  
	</div>
	
	<f:message var="deleteMilestoneTitle" key="dialog.delete-milestone.title" />	
	<div id="delete-milestone-popup" class="popup-dialog not-displayed" title="${deleteMilestoneTitle}" data-action="menu">
		<div class="display-table-row">
            <div class="display-table-cell warning-cell">
                <div class="generic-error-signal"></div>
            </div>
            <div id="warning-delete" class="display-table-cell">
            </div>          
			<div class="display-table-cell">		
			 	<p>
	              <span id="errorMessageDeleteMilestone">	  </span>
	            </p>     	            
          </div>
		</div>
		<div class="popup-dialog-buttonpane">
		    <input class="confirm" type="button" value="${confirmLabel}" />
		    <input class="cancel" type="button" value="${cancelLabel}" />
		</div>
	
	</div>	

    <f:message var="addMilestoneTitle" key="milestone.create"/>
    <div id="add-milestone-dialog" class="not-displayed popup-dialog" 
          title="${addMilestoneTitle}" />
          
        <table>
          <tr>
            <td><label for="add-milestone-label"><f:message
              key="label.Label" /></label></td>
            <td><input id="add-milestone-label" type="text" size="30" maxlength="30" data-def="maininput"/>
            <comp:error-message forField="label" /></td>
          </tr>
        
            <td><label for="add-milestone-status"><f:message
              key="label.Status" /></label></td>
            <td>
		<select id="add-milestone-status" class="combobox">
            <c:forEach items="${milestoneStatus}" var="status" > 
            <option value = "${status.key}" >${status.value} </option>
            </c:forEach>
            </select>
    
        </td>
        
         <tr>
       
            <td><label><f:message key="label.EndDate" /></td>
            <td><span id="add-milestone-end-date"></span>
        <comp:error-message forField="endDate" /></td>
         </tr>  

          <tr>
            <td>
               <label for="add-milestone-description">
                   <f:message key="label.Description" />
                </label>
            </td>
            <td>
                <textarea id="add-milestone-description" name="add-milestone-description" data-def="isrich"></textarea>
            <comp:error-message forField="description" /></td>
          </tr>     
        </table>
      <div class="popup-dialog-buttonpane">
        <input type="button" value="${addAnotherLabel}" data-def="mainbtn, evt=addanother"/>
        <input type="button" value="${addLabel}" data-def="evt=confirm"/>
        <input type="button" value="${closeLabel}" data-def="evt=cancel"/>
      </div>     
</div>


<!--  clone popup -->
 <f:message var="cloneMilestoneTitle" key="dialog.clone-milestone.title"/>
    <div id="clone-milestone-dialog" class="not-displayed popup-dialog" 
          title="${cloneMilestoneTitle}" />
          
        <table>
          <tr>
            <td><label for="clone-milestone-label"><f:message
              key="label.Label" /></label></td>
            <td><input id="clone-milestone-label" type="text" size="30" maxlength="30"/>
            <comp:error-message forField="label" /></td>
          </tr>
        <tr>
        
         <td><label for="add-milestone-status"><f:message
              key="label.Status" /></label></td>
            <td>
        	<select id="clone-milestone-status" class="combobox">
            <c:forEach items="${milestoneCloneStatus}" var="status" > 
            <option value = "${status.key}" >${status.value} </option>
            </c:forEach>
            </select>
        
        </td>
        </tr>
   
         <tr>
       
            <td><label><f:message key="label.EndDate" /></td>    
            <td><span id="clone-milestone-end-date"></span>
        <comp:error-message forField="endDate" /></td>
         </tr>  

          <tr>
            <td>
               <label for="clone-milestone-description">
                   <f:message key="label.Description" />
                </label>
            </td>
            <td>
                <textarea id="clone-milestone-description" name="add-milestone-description"></textarea>
            <comp:error-message forField="description" /></td>
          </tr>   
          
          
          <tr>
          <td> <f:message key="label.milestone.cloneoptions" /> </td>
          
          <td>
           <input id="bindToRequirements"  name="bindToRequirements" type="checkbox" checked="checked"/>
          <label class=" afterDisabled" for="bindToRequirements"><f:message key="label.milestone.bindToRequirements" /></label>
   </br>
          <input id="bindToTestCases" name="bindToTestCases" type="checkbox" checked="checked"/>
         <label class=" afterDisabled" for="bindToTestCases"><f:message key="label.milestone.bindToTestCases" /></label>
   </br>          
       </td>
   </tr>
       
        </table>
        
        <ul>
	
<li><a id="checkAll"><f:message key= "label.selectAllForSelection"/></a></li>
<li><a id="uncheckAll"><f:message key= "label.selectNoneForSelection"/></a></li>

</ul>

      <div class="popup-dialog-buttonpane">
        <input type="button" value="${addLabel}" data-def="mainbtn, evt=confirm"/>
        <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
      </div>     
</div>
<!--  clone popup end-->


<!--  synchronize popup -->

<f:message var="synchronizeMilestoneTitle" key="dialog.synchronize-milestone.title"/>
    <div id="synchronize-milestone-dialog" class="not-displayed popup-dialog" 
          title="${synchronizeMilestoneTitle}" />
    
    
     <table>
          <tr>
<td><input id="mil1" type="radio" name="synchro"></td>
<td><span id="mil1Label"></span>
<span id="mil1warn"></span></td>
          </tr>
        
     
         <tr>
<td><input id="mil2" type="radio" name="synchro"></td>
<td><span id="mil2Label"></span>
<span id="mil2warn"> </span>
</td>
         </tr>  

          <tr>
<td><input id="union" type="radio" name="synchro" ></td>
<td><span id="unionLabel"></span>
<span id="unionwarn"></span></td>
          </tr>   
          
          
          <tr>
        <td><input id="perim" type="checkbox" ></td>
<td id="perimtxt"><f:message key="label.milestone.synchronize.extendperim1"/>
</br>
<f:message key="label.milestone.synchronize.extendperim2"/></td>
   </tr>
       
        </table>
    
    
      <div class="popup-dialog-buttonpane">
        <input type="button" value="${confirmLabel}" data-def="mainbtn, evt=confirm"/>
        <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
      </div>     
</div>
<!--  synchronize popup end-->


	<f:message var="synchronizeWarning" key="dialog.milestone.synchronizeWarning"/>
	<div id="synchronize-milestone-dialog-confirm" class="popup-dialog not-displayed" title="${synchronizeMilestoneTitle}" data-action="menu">
		<div class="display-table-row">
           
             
                ${synchronizeWarning } 
			
		</div>
		<div class="popup-dialog-buttonpane">
		    <input class="confirm" type="button" value="${confirmLabel}" />
		    <input class="cancel" type="button" value="${cancelLabel}" />
		</div>
	
	</div>




</jsp:attribute>
</layout:info-page-layout>
