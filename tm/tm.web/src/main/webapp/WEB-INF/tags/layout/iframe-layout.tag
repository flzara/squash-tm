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
<%@ attribute name="head" fragment="true"
	description="Additional html head fragment"%>	
<%@ attribute name="footer" fragment="true"
	description="Additional html foot fragment"%>
<%@ attribute name="resourceName" required="true"%>
<%@ attribute name="iframeUrl" required="true"%>
<%@ attribute name="foot" fragment="true"
	description="Pseudo html foot fragment where one can put inlined script and js libraries imports"%>
	
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<?xml version="1.0" encoding="utf-8" contentType="text/html; charset=utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<c:set var="titleKey" value="workspace.${resourceName}.title" />

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
<title><f:message key="${titleKey }" /></title>
<layout:common-head />
<layout:_common-script-import highlightedWorkspace="${ resourceName }"/>
<comp:sq-css name="squash.blue.css" />	
<jsp:invoke fragment="head" />
</head>
<body>
	<layout:navigation highlighted="${ resourceName }" />
	<div id="workspace">
		<div id="workspace-title">
			<div class="snap-left">
				<h2><f:message key="${ titleKey }" /></h2>
			</div>
			<div class="snap-right">
				<div class="unstyled notification-pane">
					<layout:_ajax-notifications cssClass="snap-right" />
				</div><div class="main-menubar unstyled">
					<layout:_menu-bar />
				</div>
			</div>
		</div><div id="iframeDiv" style="bottom:0px; position:absolute; top:1cm; width:100%">
		<iframe id="iframePpal" src="${ iframeUrl }" style="height: 100%; width: 100%;"></iframe>
		</div></div>
	<jsp:invoke fragment="footer" />
</body>
<layout:_init_workspace_variables />
<jsp:invoke fragment="foot" />
  <script type="text/javascript">
  require(["common"], function() {
	require(["jquery", "squash.basicwidgets"], function($, basic){
    	$(function() {
  			basic.init();
  		});
  		$(".unstyled").fadeIn("fast", function() { $(this).removeClass("unstyled"); });
  	});
  });
 
</script>
</html>