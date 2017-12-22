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
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="writable"  required="true" type="java.lang.Boolean"  description="if the user has write permission on this test case" %>
<%@ attribute name="milestoneConf" required="true" type="java.lang.Object" description="an instance of MilestoneFeatureConfiguration" %>

<f:message var="renameDialogTitle" key="dialog.rename-test-case.title"/>
<f:message var="confirmLabel" key="label.Confirm" />
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="createLabel" key="label.Create" />
<f:message var="createNewVersionLabel" key="label.createNewVersion" />
<f:message var="nameLabel" key="label.Name"/>
<f:message var="referenceLabel" key="label.Reference"/>
<f:message var="descriptionLabel" key="label.Description"/>

<%---------------------------- Rename test case popup ------------------------------%>

<%-- 
  
  That div is important because of the use of jquery.squash.formDialog, I'll explain
  why here.
  
  When an element in the DOM is turned into a jquery dialog it is detached then 
  attached to the <body>. Which means that, in the main view ('library'), when 
  one navigate from one test case to another the popup is not removed along 
  the rest of this part of the document.
  
  This commonly leads to 'widget leaks' because the more test cases are displayed 
  and the more dialogs leaks to the body. This is especially tricky because 
  those dialogs have all the same ID but jquery won't care and when selecting 
  a dialog by ID it'll just pick the first one it finds.
  
  It may also keep alive javascript handles that manage the data of test cases
  that are no longer displayed, possibly like in Issue 3474.
  
  Our custom widget jquery.squash.formDialog ensures that such leak cannot happen. 
  When a DOM element is turned to a dialog, it registers itself as a listener on 
  its immediate DOM parent before it is attached to <body>. That way, upon destruction 
  of the parent, the listener will also destroy and remove the dialog.
  
  Henceforth, the <div class="not-displayed"> acts as this parent. When another test 
  case is displayed, this div will be removed and thus trigger the destruction of the 
  dialogs it contains. 
  
  Hadn't it be there, the dialogs would depend on the  <div id="contextual-content">, 
  which is never removed, and thus the dialogs would never be destroyed and removed.

 --%>
<div class="not-displayed">

<c:if test="${ writable }">

<div id="rename-test-case-dialog" title="${renameDialogTitle}" class="popup-dialog not-displayed">
	
	<div>
        <c:if test="${milestoneConf.showMultipleBindingMessage}">
          <div data-milestones="${milestoneConf.totalMilestones}" 
          class="milestone-count-notifier centered std-margin-top std-margin-bottom ${(milestoneConf.multipleBindings) ? '' : 'not-displayed'}">
            <f:message key="message.RenameTestCaseBoundToMultipleMilestones"/>
          </div>
        </c:if>  
		<label><f:message key="dialog.rename.label" /></label>
		<input type="text" id="rename-test-case-input" maxlength="255"	size="50" />
		<br />
		<comp:error-message forField="name" />
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn"/>
		<input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
	</div>
</div>

</c:if>
    <%-- create new version dialog --%>
 <c:if test="${milestoneConf.activeMilestone.canEdit}">
    <div id="create-test-case-version-dialog" class="popup-dialog not-displayed" title="${createNewVersionLabel}">
      
      <div data-def="state=wait">
        <comp:waiting-pane />
      </div>
      
      <div data-def="state=confirm">
         <div class="std-margin-top std-margin-bottom centered">
          <c:if test="${not empty cookie['milestones'] }">
            <f:message key="message.newTestCaseVersionWillDisassociateMilestone"/>
          </c:if>
         </div>
      
         <div >     
           <table class="add-node-attributes">
  
          <tr>
            <td>
              <label for="new-version-test-case-name" >${nameLabel}</label>
            </td>
  
            <td>
              <input id="new-version-test-case-name" type="text" size="50" maxlength="255" />
              <br />
              <span class="error-message name-error"></span>
            </td>
          </tr>
          <tr>
            <td>
              <label for="new-version-test-case-reference" >${referenceLabel}</label>
            </td>
  
            <td>
              <input id="new-version-test-case-reference" type="text" size="20" maxlength="50" />
              <br />
              <span class="error-message reference-error"></span>
            </td>
          </tr>
          <tr>
            <td>
              <label for="new-version-test-case-description">${descriptionLabel}</label>
            </td>
            <td>
              <textarea id="new-version-test-case-description" data-def="isrich"></textarea>
            </td>
          </tr>
        </table>
        </div>
      </div>
      
      <div class="popup-dialog-buttonpane">
        <input type="button" value="${confirmLabel}" data-def="state=confirm, evt=confirm, mainbtn=confirm"/>
        <input type="button" value="${cancelLabel}" data-def="evt=cancel" />      
      </div>
      
    </div>
</c:if>


</div>

