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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="tsuites" tagdir="/WEB-INF/tags/test-suites-components"%>
<%@ taglib prefix="it" tagdir="/WEB-INF/tags/iterations-components"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>



<c:url var="testSuiteUrl" value="/test-suites/${ testSuite.id }" />
<c:url var="testPlanUrl" value="/test-suites/${testSuite.id}/test-plan/" />
<c:url var="testSuiteTestPlanUrl" value="/test-suites/${testSuite.id}/info" />

<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message"  />


<%-- ----------------------------------- Authorization ----------------------------------------------%>

<%-- should be programmatically stuffed into page context --%>

<c:set var="writable"         value="${false}" />
<c:set var="moreThanReadOnly" value="${false}" />
<c:set var="attachable"       value="${false}" />
<c:set var="linkable"         value="${false}" />
<c:set var="deletable"        value="${false}" />
<c:set var="extendedDeletable" value="${false}" />


<c:if test="${not milestoneConf.locked}">

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE" domainObject="${ testSuite }">
  <c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ testSuite }">
  <c:set var="writable" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ testSuite }">
  <c:set var="attachable" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE" domainObject="${ testSuite }">
  <c:set var="deletable" value="${true}" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXTENDED_DELETE" domainObject="${ testSuite }">
  <c:set var="extendedDeletable" value="${true}" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK" domainObject="${ testSuite }">
  <c:set var="linkable" value="${ true }" />
</authz:authorized>

<c:set var="moreThanReadOnly" value="${moreThanReadOnly or writable or attachable or deletable or extendedDeletable or linkable}" />

</c:if>

<%-- ----------------------------------- /Authorization ----------------------------------------------%>





<%-- TODO : why is that no tree-picker-layout like the rest of association interface  ? --%>

<layout:tree-picker-layout workspaceTitleKey="workspace.campaign.title"
                           i18nLibraryTabTitle="squashtm.library.test-case.title"
                           highlightedWorkspace="campaign"
                           linkable="test-case"
                           isSubPaged="true">

  <jsp:attribute name="head">
	<comp:sq-css name="squash.purple.css" />
	</jsp:attribute>

  <jsp:attribute name="subPageTitle">
    <h2><c:out value="${testSuite.name}"/> &nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
  </jsp:attribute>

	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" id="back" class="sq-btn button" value="${backButtonLabel}"
                onClick="document.location.href=squashtm.workspace.backurl;" />
	</jsp:attribute>


	<jsp:attribute name="tree">
		<tree:linkables-tree
                workspaceType="test-case"  elementType="testsuite"
                elementId="${testSuite.id}" id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" />
	</jsp:attribute>


    <jsp:attribute name="tableTitlePane">
       <div class="snap-left" style="height:100%;">
          <h2>
            <span><f:message key="label.TestPlan"/></span>
          </h2>
       </div>
       <div class="unsnap"></div>
    </jsp:attribute>

    <jsp:attribute name="tablePane">
        <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ testSuiteUrl }"/>
        <tsuites:test-suite-test-plan-manager-table
            testSuite="${testSuite}"
            milestoneConf="${milestoneConf}"
            editable="${writable}"
            linkable="${linkable}"
            reorderable="${linkable}"
            deletable="${deletable}"
            extendedDeletable="${extendedDeletable}"
            />
    </jsp:attribute>

  <jsp:attribute name="foot">

  <script type="text/javascript">
  requirejs.config({
    waitSeconds: 0
  });
  require(["common"], function() {
    require(["jquery", "tree", "workspace.event-bus", "squash.translator", "app/ws/squashtm.notification", "app/ws/squashtm.workspace" ], function($, zetree, eventBus, msg, notification) {
      $(function(){

    	  function lock(){
    		  $('#add-items-button').button('disable');
    		  $('#remove-items-button').button('disable');
    	  }

    	  function unlock(){
    		  $('#add-items-button').button('enable');
    		  $('#remove-items-button').button('enable');
    	  }

            $( '#add-items-button' ).on('click', function() {
				lock();
				var tree = zetree.get('#linkable-test-cases-tree');
				var ids =	[];
				var nodes = 0;
				if( tree.jstree('get_selected').length > 0 ) {
					 nodes = tree.jstree('get_selected').not(':library').treeNode();
					 ids = nodes.all('getResId');
				}

				if (ids.length === 0) {
					notification.showError(msg.get('message.emptySelectionTestCase'));

				}

		          tree.jstree('deselect_all'); //todo : each panel should define that method too.
		          firstIndex = null;
		          lastIndex = null;

				if (ids.length > 0) {
					 $.post('${ testPlanUrl }', { testCasesIds: ids})
	        				   .done(function(){
	        					unlock();
	         				    eventBus.trigger('context.content-modified');
						})
				}
				else{
					unlock();
				}

        });

            $("#remove-items-button").on('click', function(){
              $("#remove-test-plan-button").click();
            });


        eventBus.onContextual("context.content-modified", function() {
          $("#test-suite-test-plans-table").squashTable().refresh();
        });

      });
    });
  });

  </script>

  </jsp:attribute>

</layout:tree-picker-layout>
