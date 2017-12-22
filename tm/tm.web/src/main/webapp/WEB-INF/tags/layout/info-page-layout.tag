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
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ attribute name="head" fragment="true" description="Additional html head fragment" %>
<%@ attribute name="titlePane" fragment="true" description="the title pane" %>
<%@ attribute name="informationContent" fragment="true" description="Optional informational content" %>
<%@ attribute name="subPageButtons" fragment="true" %>
<%@ attribute name="subPageTitle" fragment="true" %>
<%@ attribute name="accessDenied" %>
<%@ attribute name="footer" fragment="true" description="Optional page foot" %>
<%@ attribute name="titleKey" required="true" %>
<%@ attribute name="main" required="false" %>
<%@ attribute name="isSubPaged" required="false"  description="boolean. if set to true, the layout will be applied in a sub-paged form. Basically
it will insert sub-page-layout.tag between the top template and this one." %>
<%@ attribute name="highlightedWorkspace" required="false" description="Workspace which should be highlighted. Values : test-case, campaign, requirement. Empty for no workspace highlight" %>

<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sq" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"  %>
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<c:choose>
<c:when test="${not empty isSubPaged and isSubPaged }">
<layout:sub-page-layout highlightedWorkspace="${ highlightedWorkspace }" titleKey="${ titleKey }" main="${ main }">
  <jsp:attribute name="head" >
    <jsp:invoke fragment="head"/>
  </jsp:attribute>

  <jsp:attribute name="titlePane">  
    <jsp:invoke fragment="titlePane"/>
  </jsp:attribute>    
  
  <jsp:attribute name="footer">  
    <jsp:invoke fragment="footer"/>
  </jsp:attribute>    
  

  <jsp:attribute name="subPageTitle">
    <jsp:invoke fragment="subPageTitle" />
  </jsp:attribute>
    
  <jsp:attribute name="subPageButtons">
    <jsp:invoke fragment="subPageButtons" />  
  </jsp:attribute>

  <jsp:attribute name="content">
    <div id="information-content" class="unstyled">
      <jsp:invoke fragment="informationContent" />
    </div>
  </jsp:attribute>  
</layout:sub-page-layout>
</c:when>
<c:otherwise>
<layout:common-import-outer-frame-layout highlightedWorkspace="${ highlightedWorkspace }" titleKey="${ titleKey }" main="${ main }">
  <jsp:attribute name="head" >  
    <jsp:invoke fragment="head"/>
  </jsp:attribute>
  
  <jsp:attribute name="titlePane">  
    <jsp:invoke fragment="titlePane"/>
  </jsp:attribute>    

  <jsp:attribute name="footer">  
    <jsp:invoke fragment="footer"/>
  </jsp:attribute>    

  <jsp:attribute name="content">
    <div id="information-content" class="unstyled">
      <jsp:invoke fragment="informationContent" />
    </div>
  </jsp:attribute>
</layout:common-import-outer-frame-layout>
</c:otherwise>
</c:choose>

</html>