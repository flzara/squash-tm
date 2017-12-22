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
<%@ tag description="managment of iteration test suites" body-content="empty" %>
<%@ tag language="java" pageEncoding="utf-8"%>

<%@ attribute name="iteration" type="java.lang.Object" required="true" description="the iteration object of which we manage the test suites" %>

<%@ taglib prefix="f" 		uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s"		uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" 	tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="fn"		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>


<s:url var="baseSuiteUrl" value="/test-suites" />
<s:url var="testSuitesUrl" value="/iterations/{iterId}/test-suites">
  <s:param name="iterId" value="${iteration.id}" />
</s:url>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE" domainObject="${ iteration }">
  <c:set var="deletable" value="${true}" />
</authz:authorized>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE" domainObject="${ iteration }">
  <c:set var="creatable" value="${true}" />
</authz:authorized>



 <%-- ====================== POPUP STRUCTURE DEFINITION ========================= --%>

 <f:message var="manageTSDialog" key="dialog.testsuites.title" />
 <f:message var="closeLabel" key="label.Close" />
 <div id="manage-test-suites-popup" class="popup-dialog not-displayed" title="${manageTSDialog}">

  <div class="main-div-suites not-displayed">
  <c:if test="${ creatable }">
    <div class="create-suites-section">
      <f:message var="defaultMessage" key="dialog.testsuites.defaultmessage" />
      <f:message var="createLabel" key="label.Add"/>
      <input id="ts-popup-text" type="text" size="30" placeholder="${defaultMessage}"/>
      <input id="ts-popup-addButton" onClick="$('#ts-popup-text').focus()" type="button" class="button" value="${createLabel}"/><br/>
      <comp:error-message forField="name" />
    </div>
    </c:if>
    <div class="display-suites-section">
    </div>

    <div class="rename-suites-section">
      <f:message var="renameLabel" key="dialog.testsuites.rename.label" />
      <input type="text" size="30"/><input type="button" class="button" value="${renameLabel}" />
    </div>
    <c:if test="${ deletable }">
    <div class="remove-suites-section">
      <f:message var="removeLabel" key="dialog.testsuites.remove.label" />
      <input type="button" class="button" value="${removeLabel}"/>
    </div>
  </c:if>
  </div>

  <div class="popup-dialog-buttonpane">
    <input type="button" value="${closeLabel}" data-def="evt=closemanager"/>
  </div>

 </div>


<div id="suite-menu-empty-selection-popup" class="not-visible"
	title="<f:message key='title.suite.menu.emptySelection' />">
	<div>
		<f:message key="message.suite.menu.emptySelection" />
	</div>
</div>
<%-- ====================== /POPUP STRUCTURE DEFINITION  ========================= --%>


<f:message var="deleteMessage" key="dialog.delete-test-suite.message" />
<f:message var="deleteTitle" key="dialog.delete-test-suite.title" />

<script type="text/javascript">
require( ["common"], function(){

	require(["jquery","iteration-management"], function($,main){
$(function(){

		<%-- for this JSON serialisation is not an option because we don't iterate over pojos here : these
          are full fledged hibernate entities
		--%>
		var initData = [
						<c:forEach var="suite" items="${iteration.testSuites}" varStatus="status">
							{ id : '${suite.id}', name : '${fn:replace(suite.name, "'", "\\'")}' }<c:if test="${not status.last}">,</c:if>
						</c:forEach>
					];

		var tableListener = {
				redraw : function(evt_name){
					//"add" is none of our business.
					if ((evt_name===undefined) || (evt_name=="node.remove") || (evt_name=="node.rename") || (evt_name =="node.bind")){
						$('#iteration-test-plans-table').squashTable().refreshRestore();
					}
				}
			};

		var modelSettings = {
				createUrl : "${testSuitesUrl}/new",
				baseUpdateUrl : "${baseSuiteUrl}",
				getUrl : "${testSuitesUrl}",
				removeUrl : "${testSuitesUrl}/delete",
				initData : initData
			};

		var managerSettings = {
				dialog : $("#manage-test-suites-popup"),
				instance : $("#manage-test-suites-popup .main-div-suites"),
				deleteConfirmMessage : "${deleteMessage}",
				deleteConfirmTitle : "${deleteTitle}"
			};

		var menuSettings = {
				instanceSelector : "#manage-test-suites-buttonmenu",
				datatableSelector : "#iteration-test-plans-table"
			};

		var config = {
				modelSettings : modelSettings,
				managerSettings: managerSettings,
				menuSettings : menuSettings,
				tableListener : tableListener
			};

		main.initTestSuiteMenu(config);

		//now we can make reappear
		$("#manage-test-suites-popup .main-div-suites").removeClass("not-displayed");
		
		/* Enhancement #6754 - press 'enter' to add a test suite when the focus is on the inputText */
		$("#ts-popup-text").keyup(function(event) {
			if (event.keyCode == 13) {
				$("#ts-popup-addButton").click();
			}
		});
	});
});
});


</script>
