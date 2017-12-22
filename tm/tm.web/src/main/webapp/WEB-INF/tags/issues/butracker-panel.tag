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
<%@ tag description="Table displaying the issues for an ExecutionStep" body-content="empty" %>

<%@ tag language="java" pageEncoding="utf-8"%>


<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>

<%@ attribute name="entity" required="true" type="java.lang.Object" description="the entity for which we're creating the panel"%>
<%@ attribute name="issueDetector" required="false" type="java.lang.Boolean" description="whether we should add a button 'report issue', defaults to false"%>


<%-- user allowed to report issues ? --%>
<c:set var="reportGranted" value="${ false }" />
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE"
  domainObject="${ entity }">
  <c:set var="reportGranted" value="${ true }" />
</authz:authorized>

<%-- issues can be reported if the used is allowed and the entity can report issues  --%>
<c:set var="reportable" value="${reportGranted and (not empty issueDetector ? issueDetector : false)}"/>

<%--
  As a rule of thumb, for now we consider that entities for which 'issueDetector' is false
  the panel is displayed as a fragment tab whereas when 'issueDetector' is true the display
  is toggle panel.

  If it changes later on, well check on that.
 --%>

<c:set var="panelStyle" value="${(issueDetector eq true) ? 'toggle' : 'fragment-tab'}" />

<%-- This variable is only used if this tag is a panel.
	 bugtrackerMode is only set in model while executing a test case. --%>
<c:set var="expandPanel" value="${bugtrackerMode == 'Manual' ? false : true}" />

<f:message var="issueReportOpenButtonLabel" key="issue.button.opendialog.label" />
<f:message var="activateButtonlabel" key="message.bugtracker.must-activate.button-label" />
<div id="bugtracker-section-main-div">

  <comp:structure-configurable-panel id="issue-panel"
    titleKey="issue.panel.title" isContextual="true" open="${expandPanel}"
    style="${panelStyle}">

    <jsp:attribute name="panelButtons">
      <c:if test="${ reportable }">
          <input type="button" class="sq-btn" id="issue-report-dialog-openbutton"
                 value="${issueReportOpenButtonLabel}" />
      </c:if>
    </jsp:attribute>

    <jsp:attribute name="body">

		<c:choose>
			<c:when test="${bugtrackerMode == 'Automatic'}">

      			<div id="bugtracker-section-pleasewait" style="margin-top:50px; margin-bottom:50px ;">
		  			<comp:waiting-pane/>
	  			</div>
			</c:when>
			<c:otherwise>
				<c:choose>
				<c:when test="${bugtrackerMode == 'Manual'}">
					<div id="bugtracker-section-must-activate">
						<div class="centered minimal-height" style="margin-top:50px; margin-bottom:50px ;">
       	   					<span style="font-size : 1.5em;">
            					<f:message key="message.bugtracker.must-activate.label"/>
          					</span>
							<div>
								<input type="button" class="sq-btn" id="issue-activate-button"
				   				value="${activateButtonlabel}"/>
							</div>

						</div>
					</div>
				</c:when>
					<c:otherwise>
						<div id="bugtracker-section-pleasewait" style="margin-top:50px; margin-bottom:50px ;">
							<comp:waiting-pane/>
						</div>
					</c:otherwise>
				</c:choose>
			</c:otherwise>
		</c:choose>
      	<div id="bugtracker-section-div" class="table-tab not-displayed">

	  	</div>

      	<div id="bugtracker-section-error" class="not-displayed">
		  <div class="centered minimal-height" style="margin-top:100px;">
          <span style="font-size : 1.5em;">
            <f:message key="message.bugtracker.unavailable"/>
          </span>
			<span id="bugtracker-section-error-details" class="cursor-pointer" style="text-decoration:underline">
        	    (<f:message key="label.Details"/>)
          	</span>
		  </div>
	  	</div>

    </jsp:attribute>

   </comp:structure-configurable-panel>
</div>
