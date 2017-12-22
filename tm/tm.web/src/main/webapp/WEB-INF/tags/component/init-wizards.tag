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
<%--
  This tag will fetch all the wizards for a given workspace, collect their javascript modules and start them sequentially.

  For the initialization section we need to generate some javascript, apologize for that.
 --%>

<%@ tag description="snippet for wizards javascript initialization" body-content="empty" %>

<%@ attribute name="workspace" required="true" description="for now : 'test-case', 'requirement' or 'campaign'"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wu" uri="http://org.squashtest.tm/taglib/workspace-utils" %>


<c:set var="wkpTypeName" value="${ (workspace=='test-case') ? 'TEST_CASE_WORKSPACE' :                                 
                                    (workspace=='requirement') ?  'REQUIREMENT_WORKSPACE' : 
                                    'CAMPAIGN_WORKSPACE'  }"/>

<c:set var="wizz" value="${wu:getWizardPlugins(pageContext.servletContext, wkpTypeName)}"/>

<c:if test="${not empty wizz}">

 <script type="text/javascript">
 	var tmwizardmodules = [
 	<c:forEach items="${wizz}" var="item" varStatus="stats">
 		'${item.module}'<c:if test="(not stats.last) and (it.module not null)">,</c:if>
 	</c:forEach>
 	];
 	
 	require(['common'], function(){
 	 	require(tmwizardmodules, function(){
 	 		for (var i=0; i<arguments.length; i++){
 	 			arguments[i].init();
 	 		}
 	 	});
 	});
 
 </script>

</c:if>