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
<%@ tag description="Layout for a page where the user can pick items from a tree and add them to a list" body-content="empty" %>


<%@ attribute name="head" fragment="true" description="instructions to add to the page html head" %>
<%@ attribute name="foot" fragment="true" description="Pseudo html foot fragment where one can put inlined script and js libraries imports" %>
<%@ attribute name="tableTitlePane" fragment="true" %>
<%@ attribute name="tablePane" fragment="true" %>
<%@ attribute name="main" required="false" %>


<%@ attribute name="subPageTitle" fragment="true" description="the sub page has its own title. Define it there."%>
<%@ attribute name="subPageButtons" fragment="true" description="Unlike a regular workspace, a sub page exists to perform 
																	a one shot operation. Those operations are proposed in a dedicated action panel.
																	That action panel already propose a 'go back' button."	 %>



<%@ attribute name="workspaceTitleKey" description="key of page title" required="true" %>
<%@ attribute name="i18nLibraryTabTitle" required="false" description="boiler plate for layout:tree-page-layout#i18nLibraryTabTitle" %>

<%@ attribute name="tree" fragment="true" required="true" description="Tree definition" %>
<%@ attribute name="linkable" required="true" %>
<%@ attribute name="highlightedWorkspace" required="true" %>

<%@ attribute name="isSubPaged" required="false"  description="boolean. if set to true, the layout will be applied in a sub-paged form. Basically
it will insert sub-page-layout.tag between the top template and this one." %>


<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<layout:tree-page-layout 
  titleKey="squashtm" 
  highlightedWorkspace="${ highlightedWorkspace }" 
  i18nLibraryTabTitle="${i18nLibraryTabTitle}"
  linkable="${linkable}" 
  isSubPaged="${isSubPaged}"
  main="${ main }">
	
	<jsp:attribute name="head">
		<jsp:invoke fragment="head" />
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="${ workspaceTitleKey }" /></h2>	
	</jsp:attribute>	
	
	
	<jsp:attribute name="subPageTitle">
		<jsp:invoke fragment="subPageTitle" />
	</jsp:attribute>
		
	<jsp:attribute name="subPageButtons">
		<jsp:invoke fragment="subPageButtons" />	
	</jsp:attribute>
		

	<jsp:attribute name="tree">
		<jsp:invoke fragment="tree" />
	</jsp:attribute>
	
	<jsp:attribute name="contextualContent">		
		<div style="overflow:hidden;height:100%;">
			<div  id="tree-picker-actions-pane" class="centered">
				<div style="position:absolute;top:45%;margin-right:2em;">
					<div id="add-items-button" class="association-button" title="<f:message key="label.Associate" />"  ></div>  
					<div id="remove-items-button" class="association-button" title="<f:message key="label.Unbind" />" ></div>  
				</div>
			</div>
			
			<div id="tree-picker-target-pane">
				<div id="table-title-pane" class="ui-widget-header ui-corner-all fragment-header">
					<jsp:invoke fragment="tableTitlePane" />
				</div>
				
				<div id="table-pane" class="fragment-body jstree-drop">
					<jsp:invoke fragment="tablePane" />
				</div>
			</div>
		</div>
	</jsp:attribute>
	
	<jsp:attribute name="foot">
		<jsp:invoke fragment="foot" />
		<f:message var ="addLabel" key="label.Add" />
		<f:message var ="removeLabel" key="subpage.association.button.disassociate.label" />
		<script type="text/javascript">
require([ "common" ], function() {
	require([ "jquery", "jqueryui" ], function($) {
		$(function(){
			$("#add-items-button").button({
				disabled : false,
				text : "${addLabel}",
				icons : {
					primary : "ui-icon-seek-next"
				}
			});		
			$("#remove-items-button").button({
				disabled : false,
				text : "${removeLabel}",
				icons : {
					primary : "ui-icon-seek-prev"
				}
			});	
		});
	});
});
		</script>
	</jsp:attribute>
</layout:tree-page-layout>
	