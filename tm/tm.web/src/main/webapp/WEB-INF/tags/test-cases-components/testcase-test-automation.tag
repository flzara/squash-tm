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
<%@ tag description="test automation panel (test case level)" body-content="empty"%>


<%@ attribute name="testCase" required="true" type="java.lang.Object" description="the test case"%>
<%@ attribute name="canModify" required="no" type="java.lang.Boolean" description="whether the script name link is editable (or not). Default is false."%>


<%@ tag language="java" 	pageEncoding="utf-8" %>
<%@ taglib prefix="f"	 	uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" 	tagdir="/WEB-INF/tags/component" %>

<f:message var="testPick" 					key="label.dot.pick"/>
<f:message var="testNone"					key="label.none"/>
<f:message var="testAutomationPickerTitle" 	key="test-case.testautomation.popup.title" />
<f:message var="testAutomationPickerOk" 	key="test-case.testautomation.popup.ok" />
<f:message var="labelCancel" 				key="label.Cancel"/>
<f:message var="labelConfirm"               key="label.Confirm"/>
<f:message var="labelRemove"                key="label.Remove"/>
<f:message var="deleteAutoTitle" 			key='title.confirmDeleteAutomatedTestLink'/>

<c:set var="isTestSet" value="${not empty testCase.automatedTest}"/>
<c:set var="scriptnameLabel" value="${not canModify && not isTestSet ? testNone : 
									  isTestSet ? testCase.automatedTest.fullLabel : 
									  ''}"/>

 			
<div class="display-table-row">
	
	<label class="display-table-cell"><f:message key="test-case.testautomation.section.label"/></label>
	
	<div class="display-table-cell">
     
		<span id="ta-script-picker-span" style="width:255px" class="cursor-pointer"><c:out value="${scriptnameLabel}"/></span>

		<%--
		The best would have been to declare a button here : 
		
		<input id="ta-script-picker-button" type="button" value="${testPick}" class="not-displayed"/>
        <input id="ta-script-remove-button" type="button" value="${labelRemove}" class="not-displayed"/>
		
		however for several reasons (for nicer rendering, limitations of jeditable), we must 
		handle such button programatically. see 'test-automation/testcase-test-automation.js to see how it's 
		done.
		--%>
 
	</div>
</div>

<c:if test="${canModify}">
<%-- we must enforce display:none here because of inheritance of other css rules that override the display mode --%>
<div style="display:none">

	<div id="ta-picker-popup" class="popup-dialog" title="${testAutomationPickerTitle}">

		<div class="ta-picker-structure-maindiv">
		
		 	<div data-def="state=pleasewait" class="structure-pleasewait">
	 			<comp:waiting-pane/>		
	 		</div>
		
			<div data-def="state=main" class="structure-treepanel has-standard-margin">
				<div class="structure-tree"></div>		
			</div>
			
		</div>
	
		<div class="popup-dialog-buttonpane">
			<input type="button" value="${testAutomationPickerOk}" data-def="mainbtn, evt=confirm"/>
			<input type="button" value="${labelCancel}" data-def="evt=cancel" /> 		
		</div>
		
	</div>


    <f:message var="removePopupTitle" key="dialog.unbind-ta-script.title"/>
    <div id="ta-remove-popup" class="popup-dialog" title="${removePopupTitle}">
    
      <div class="std-margin-top std-margin-bottom"> 
        <span><f:message key="dialog.unbind-ta-script.message"/></span>
      </div>
    
      <div class="popup-dialog-buttonpane">
        <input type="button" value="${labelConfirm}" data-def="mainbtn, evt=confirm"/>
        <input type="button" value="${labelCancel}" data-def="evt=cancel"/>      
      </div>
    
    </div>

</div>

</c:if>
			