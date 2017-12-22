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
<%@ tag body-content="empty"
	description="test case toolbar and messages"%>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="milestoneConf" required="true" type="java.lang.Object" description="an instance of MilestoneFeatureConfiguration"%>
<%@ attribute name="nodeType" required="false" type="java.lang.String" description="Node type define the type of selected node. Values :
campaign, iteration, testsuite, execution, requirement, testcase"%>
 

<%--
  See MilestoneFeatureConfiguration to check the rules about what must be displayed
 --%>
 
 <%-- reduces iteration, testsuite, execution to the type 'campaign', let the others untouched --%>
 <c:set var="reducedType" value="${(nodeType == 'iteration' || nodeType=='testsuite' || nodeType == 'execution') ? 'campaign' : nodeType}"/>
 
 <%-- message key for the lock message, depending of the node type  --%>
 <c:set var="lockMsgKey" value="${(reducedType == 'campaign') ? 
              'message.CannotModifyBecauseMilestoneLocking.campaign' : 
              'message.CannotModifyBecauseMilestoneLocking'}"/>
 
 <%-- message key for the multiple binding message, according to the node type. Note that the cas is also stated for the campaigns although it will 
 never occur. --%>
 <c:set var="multipleBindingMsgKey" value="${(reducedType == 'requirement') ? 'messages.boundToMultipleMilestones.requirement' : 
                                             (reducedType == 'testcase') ? 'messages.boundToMultipleMilestones.testcase' : 
                                             'messages.boundToMultipleMilestones.testcase'}"/>  
  
 
<c:if test="${milestoneConf.messagesEnabled}">

    <c:if test="${milestoneConf.showMultipleBindingMessage}">
    <div data-milestones="${milestoneConf.totalMilestones}" 
        class="milestone-count-notifier entity-edit-general-warning ${(milestoneConf.multipleBindings) ? '' : 'not-displayed'}"> 
	   <p><f:message key="${multipleBindingMsgKey}"/></p>
    </div>
    </c:if>
    
   <c:if test="${milestoneConf.showLockMessage}">
    <div class="entity-edit-general-warning">
      <p><f:message key="${lockMsgKey}"/></p>
    </div>        
    </c:if>

</c:if>