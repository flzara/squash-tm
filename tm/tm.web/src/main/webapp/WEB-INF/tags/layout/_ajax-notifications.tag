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
<%@ tag body-content="empty" description="Add script which handles json content of ajax errors and populates error-message tagsaccordingly" %>
<%@ attribute name="cssClass" description="additional css classes" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>


<div id="ajax-processing-indicator" class="ui-corner-all ${cssClass} not-processing">
	<div class="small-loading" ></div>
	<span><f:message key="squashtm.processing"/></span>
</div>
<div id="generic-error-notification-area" class="ui-state-error ui-corner-all ${ cssClass } not-displayed ">
	<span class="ui-icon ui-icon-alert icon"></span><span><f:message key="error.generic.label" />&nbsp;
  (<a href="#" id="show-generic-error-details" class="cursor-pointer"><f:message key="error.generic.button.details.label" /></a>)</span>
</div>


<f:message var="errorTitle" key="popup.title.error"/>
<f:message var="okLabel" key="label.Ok"/>
<div id="generic-error-dialog" class="not-displayed popup-dialog" title="${errorTitle}">
  <div>
    <comp:notification-pane type="error"/>
  </div>
  <input type="button" value="${okLabel}"/>  
</div>

<f:message var="warningTitle" key="label.warning" />
<div id="generic-warning-dialog" class="not-displayed popup-dialog" title="${warningTitle}">
  <div>
    <comp:notification-pane type="warning"/>
  </div>
  <input type="button" value="${okLabel}"/>  
</div>

  <f:message var="infoTitle" key="popup.title.info" />
 <div id="generic-info-dialog" class="not-displayed popup-dialog" title="${infoTitle}">
  <div>
    <comp:notification-pane type="info"/>
  </div>
  <input type="button" value="${okLabel}"/>  
</div> 