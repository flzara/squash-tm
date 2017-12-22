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
	that page is the root of the page template chain.
	
	It defines :
		- the common script and css imports,
		- the navigation bar,
		- the menu bar, 
		- the status bar (ajax processing, error and warning display zones etc)

	the following is variable and may be defined by other templates depending of it :
	
		- additional header,
		- the title pane,
		- footer (foot)
		- another footer (footer) //todo : Gregory, please explain why we need it twice.
		- the actual content of the page.
		
	@author bsiri

 --%>


<%@ tag description="The outer frame of the application : navigation bar and menu bar. Declare ajax-errors handling and renders nice buttons"  body-content="empty"%>

<%@ attribute name="titleKey" required="true" %>
<%@ attribute name="highlightedWorkspace" required="true" %>
<%@ attribute name="main" required="false" %>

<%@ attribute name="head" fragment="true" description="Additional html head fragment" %>
<%@ attribute name="titlePane" fragment="true" description="the title pane" %>

<%@ attribute name="content" fragment="true" description="the actual content of the page." %>

<%@ attribute name="foot" fragment="true" description="Pseudo html foot fragment where one can put inlined script and js libraries imports" %>
<%@ attribute name="footer" fragment="true" description="Optional page foot" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sq" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"  %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ tag language="java" pageEncoding="utf-8"%>

<?xml version="1.0" encoding="utf-8" contentType="text/html; charset=utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 	
		<title><f:message key="${ titleKey }"/></title>
		<layout:common-head />		
		<layout:_common-script-import highlightedWorkspace="${ highlightedWorkspace }" main="${ main }" />
		<jsp:invoke fragment="head" />
	</head>
	<body>
		<layout:navigation highlighted="${ highlightedWorkspace }" />		
		<div id="workspace">
			<div id="workspace-title">
				<div class="snap-left">
					<jsp:invoke fragment="titlePane"/>
				</div>
				<div class="snap-right">
					<div class="unstyled notification-pane">
						<layout:_ajax-notifications  cssClass="snap-right"/>					
					<%-- 
						note about the sticked </div><div> below : IT DOES MATTER
						if you insert any separator character between them the rendering will be altered for Chrome.
					 --%>
					</div><div class="main-menubar unstyled">
						<layout:_menu-bar />
					</div>
				
                    <script type="text/javascript">
                      publish("load.notification");
                    </script>
				</div>
			</div>
			
			<jsp:invoke fragment="content"/>
		</div>
		<jsp:invoke fragment="footer" />	
	</body>
	<layout:_init_workspace_variables />
	<jsp:invoke fragment="foot" />
    <script type="text/javascript">
    require([ "common" ], function() {
      require([ "jquery", "squash.basicwidgets" ], function($, basicwidg){
      	$(function() {
    			basicwidg.init();
        		$(".unstyled").fadeIn("fast", function() { $(this).removeClass("unstyled"); });
        		$.ajaxSetup({
        			scriptCharset : "utf-8"
        		});
      	});
      });
    });
    </script>
</html>