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
<%@ tag body-content="empty" description="inserts the content of the connection history tab" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<div id="connection-table-pane" class="table-tab" >
  <div class="btn-toolbar right" style="z-index:1;">
    <button id="export-history-button" class="sq-icon-btn btn-sm"
      title="<f:message key='title.export.connectionHistory' />" >
      <span class="ui-button-text">&nbsp;<f:message key="label.Export" />&nbsp;</span>
    </button>
  </div>
  <div class="table-tab-wrap">
    <span class="ui-icon ui-icon-search" style="display: inline-block"></span>
    <label><f:message key="label.Login"/></label>
    <input id="login_filter_input"/>
    <label><f:message key="label.Dates"/></label>
    <input class="rangedatepicker-input" readonly="readonly" style="width: 30%!important"/>
    <div class="rangedatepicker-div" style="position:absolute;top:auto;left:auto;z-index:1;"></div>
    <input id="date_filter_input" type="hidden" class="rangedatepicker-hidden-input filter_input"/>
    <table id="connections-table" data-def="deferloading=${ pagedConnectionLogs.totalNumberOfItems }, pagesize=${ connectionsPageSize }" class="unstyled-table">
      <thead>
        <tr>
          <th class="not-displayed">Id(masked)</th>
          <th>#</th>
          <th class="datatable-filterable"><f:message key="label.Login" /></th>
          <th><f:message key="label.connectionDate" /></th>
          <th><f:message key="label.successful"/></th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="connection" varStatus="teamStat" items="${ pagedConnectionLogs.pagedItems }">
        <tr>
          <td class="not-displayed">${ connection.id }</td>
          <td >${ teamStat.index + 1 }</td>
          <td>${ connection.login }</td>
          <td><comp:date value="${ connection.connectionDate }" /></td>
          <c:if test="${connection.success}">
            <td><f:message key="label.Yes" /></td>
          </c:if>
          <c:if test="${!connection.success}">
            <td><f:message key="label.No" /></td>
          </c:if>
        </tr>
        </c:forEach>
        <%-- Will be populated through ajax --%>
      </tbody>
    </table>
  </div>
</div><%-- /div#connection-table-pane --%>

<!--  ================= EXPORT HISTORY POPUP =======================  -->

    <div id="export-connection-history-dialog" class="popup-dialog not-displayed"
      data-def="nameprefix=<f:message key='label.lower.dash.exportConnectionHistory' />, dateformat=<f:message key='export.dateformat' />" title="<f:message key='title.export.connectionHistory' />">

      <div data-def="state=main">

        <div class="display-table" style="width: 100%">
          <div style="display: table-column-group">
            <div style="display: table-column"></div>
            <div style="display: table-column; width: 70%"></div>
          </div>
          <div class="display-table-row">
            <label><f:message key="requirement.export.dialog.filename"/> </label>
            <div class="display-table-cell">
              <input type="text" id="export-name-input" style="width: 100%" />
            </div>
          </div>
        </div>
      </div>

      <div class="popup-dialog-buttonpane">
        <input type="button" value="<f:message key='label.Export' />" data-def="evt=confirm, mainbtn=main, state=main" />
        <input type="button" value="<f:message key='label.Close' />"
          data-def="evt=cancel, mainbtn=crossproerror, mainbtn=nonodeserror, state=main" />
      </div>

    </div>


<!--  ================= /EXPORT HISTORY POPUP ======================= -->
