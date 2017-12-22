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
<%@ attribute name="id" required="true" description="id of the tree component" %>
<%@ attribute name="rootModel" required="true" type="java.lang.Object" description="JSON serializable model of root of tree." %>

<%@ attribute name="workspaceType" required="false" description="if set, will override the default icons"%>

<%@ attribute name="elementType" required="false" description="provides context for advanced research"%>
<%@ attribute name="elementId" required="false" description="provides context for advanced research"%>



<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="tree_element_menu" class="tree-top-toolbar">

	<div class="button-group snap-right">

		<c:choose>
		<c:when test="${workspaceType == 'test-case' && elementType != 'requirement'}">
    <a id="search-tree-button" class="buttonmenu sq-icon-btn" title="<f:message key='tree.button.search.label'/>...">
      <span class="ui-icon ui-icon-search"></span>
    </a>
		<ul id="search-tree-menu" class="not-displayed">
			<li id="test-case-search-button" 	class="cursor-pointer"><a ><f:message key="search.button.label" />...</a></li>
			<li id="search-by-requirement-button"  class="cursor-pointer"><a ><f:message key="search.button.byrequirement.label" />...</a></li>
		</ul>
		</c:when>
		<c:otherwise>
    <a id="search-tree-button" class="sq-icon-btn" title="<f:message key='tree.button.search.label' />...">
      <span class="ui-icon ui-icon-search"></span>
    </a>
		</c:otherwise>
		</c:choose>

	</div>


</div>

<tree:_html-tree treeId="${ id }" />
<script type="text/javascript">
require( ["common"], function(){
	require( ["jquery", "tree", "jquery.squash.buttonmenu"], function($, tree){

    	var conf = {
    		domain : "${elementType}",
    		model : ${ json:serialize(rootModel) },
    		workspace : "${workspaceType}",
    		treeselector : "#${id}"
    	}

    	$(function(){

    		// thre tree
    		tree.initLinkableTree(conf);

			<%--[Issue 5218] add top margin for jstree to avoid hiding filter reminder--%>
			$("#${id}")[0].style.marginTop = "1em";

			// the button menu
      		if(conf.workspace === "requirement"){
      			$("#search-tree-button").on('click', function(){
      				document.location.href = squashtm.app.contextRoot + "/advanced-search?searchDomain=requirement&id=${elementId}&associateResultWithType=${elementType}";
      			});

      		} else if(conf.domain != "requirement"){

      			$("#search-tree-button").buttonmenu();

      			$("#test-case-search-button").on('click', function(){
      				document.location.href = squashtm.app.contextRoot + "/advanced-search?searchDomain=test-case&id=${elementId}&associateResultWithType=${elementType}";
      			});

      			$("#search-by-requirement-button").on('click', function(){
      				document.location.href = squashtm.app.contextRoot + "/advanced-search?searchDomain=testcaseViaRequirement&id=${elementId}&associateResultWithType=${elementType}";
      			});

      		} else {
      			$("#search-tree-button").on('click', function(){
      				document.location.href = squashtm.app.contextRoot + "/advanced-search?searchDomain=test-case&id=${elementId}&associateResultWithType=${elementType}";
      			});

      		}

    	});
	});
});

</script>
