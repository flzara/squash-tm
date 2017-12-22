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

	That layout defines a special layer between common-outer-frame-layout and the more specific templates downward.
	
	That layout is meant to turn the page (whatever the actual content) into a 'sub page', that should visually be 
	different from the main workspaces of the application. In other words, it's optional.

--%>


<%@ tag description="optional" body-content="empty"%>

<%@ attribute name="titleKey" required="true"%>
<%@ attribute name="highlightedWorkspace" required="true"%>
<%@ attribute name="head" fragment="true" description="Additional html head fragment"%>
<%@ attribute name="titlePane" fragment="true" description="the title pane"%>
<%@ attribute name="subPageTitle" fragment="true" description="the sub page has its own title. Define it there."%>
<%@ attribute name="subPageButtons" fragment="true"
  description="Unlike a regular workspace, a sub page exists to perform 
																	a one shot operation. Those operations are proposed in a dedicated action panel.
																	That action panel already propose a 'go back' button."%>

<%@ attribute name="content" fragment="true" description="the actual content of the page."%>
<%@ attribute name="foot" fragment="true"
  description="Pseudo html foot fragment where one can put inlined script and js libraries imports"%>
<%@ attribute name="footer" fragment="true" description="Optional page foot"%>
<%@ attribute name="main" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<layout:common-import-outer-frame-layout highlightedWorkspace="${highlightedWorkspace}" titleKey="${titleKey}" main="${ main }">

  <jsp:attribute name="head">	
		<jsp:invoke fragment="head" />
		
		<%-- css override is needed in case of a sub page. --%>
		<comp:sq-css name="squash.core.override.css" />
		<comp:sq-css name="squash.subpage.override.css" />
	</jsp:attribute>

  <jsp:attribute name="titlePane">
		<jsp:invoke fragment="titlePane" />
	</jsp:attribute>

  <jsp:attribute name="foot">	
		<jsp:invoke fragment="foot" />
	</jsp:attribute>


  <jsp:attribute name="footer">	
		<jsp:invoke fragment="footer" />
	</jsp:attribute>

  <jsp:attribute name="content">
		<div id="sub-page" class="sub-page">
			
			<div id="sub-page-header" class="sub-page-header">
			
				<div id="sub-page-title" class="sub-page-title">
					<jsp:invoke fragment="subPageTitle" />
				</div>
				
				<div id="sub-page-buttons" class="sub-page-buttons">
					<jsp:invoke fragment="subPageButtons" />
				</div>
				
				<div class="unsnap"></div>
			</div>
			
			<div id="sub-page-content" class="sub-page-content">
				<jsp:invoke fragment="content" />
			</div>	
			
		</div>
	</jsp:attribute>
</layout:common-import-outer-frame-layout>

