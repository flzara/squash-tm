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
<%@ tag body-content="empty" description="the automated suites panel for an iteration"%>
<%@ attribute name="iteration" type="java.lang.Object" description="the instance of iteration"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<s:url var="dtMessagesUrl" value="/datatables/messages" />
<s:url var="tableModelUrl" value="/iterations/{iterId}/automated-suite">
  <s:param name="iterId" value="${iteration.id}" />
</s:url>

<div id="iteration-automated-suites-panel" class="table-tab">

  <%-- ===================== THE TABLE ===================== --%>
  <%--
    Because the filtering/sorting system might not like that a column may be defined or not,
    the column must always be present. It may, however, be displayed or not.

    As per stupid specification, instead of the normal conditions the milestone dates column
    must be displayed if the feature is globally-enabled but not user-enabled

    for f*** sakes
   --%>

  <div class="table-tab-wrap">
    <table id="iteration-automated-suites-table" class="test-plan-table unstyled-table"
      data-def="ajaxsource=${tableModelUrl}"  data-entity-id="${iteration.id}" data-entity-type="iteration">
      <thead>
        <tr>
          <th class="no-user-select"
            data-def="map=entity-index, select, center, sClass=drag-handle, sWidth=2.5em">#</th>
          <th class="no-user-select tp-th-filter tp-th-reference" title="UUID"
              data-def="map=uuid">
            <f:message key="test-case.automation-uuid.short" />
          </th>
        </tr>
      </thead>
      <tbody>
        <%-- Will be populated through ajax --%>
      </tbody>
    </table>
  </div>
</div>
<!-- /test plan panel end -->

<script type="text/javascript">
	require(["common"], function(){
		require(["domReady", "iteration-management"], function(domReady, iterInit){

			domReady(function(){
				var conf = {
						basic : {
							iterationId : ${iteration.id}
						}
					};

				iterInit.initTestPlanPanel(conf);
			});

		});
	});


</script>
