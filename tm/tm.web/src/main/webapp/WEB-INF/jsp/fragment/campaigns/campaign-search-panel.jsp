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

	TODO : dump this pile of **** and put something decent in place of it.
	-- bsiri
	
	14/01/24 : pruned unused parts of the code but I'll have rest only when 
	I can finally dump it
	
	15/06/15 : i get this file from an old commit, dunno if these previous comments should be still here.
	
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="stru"
	uri="http://org.squashtest.tm/taglib/string-utils"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>


<s:url var="searchUrl" value="/search/campaigns" />
<s:url var="breadCrumbUrl" value="/search/campaigns/breadcrumb" />

<s:url var="baseURL" value="/" />

<f:message var="InputFailMessage" key='search.validate.failure.label' />
<f:message var="InputEmptyMessage" key='search.validate.empty.label' />

<script type="text/javascript">
require(["common"], function() {
	require(["jquery", "jquery.cookie", "jqueryui"], function($) {

 
    	// ************** init function ************************
    	
    	$(function(){
    		
    		// restore previous search
    		var oldSearch = $.cookie('searchcampaign');
    		if ( oldSearch != null && oldSearch != "undefined"){
    			$("#searchName").val(oldSearch);
    		}
    		
    		// bind search button
    		$("#search-button").button().on("click", function(){
    			var name =  $("#searchName").val(),
    				sortbyproject = $('#project-view').is(':checked'); 
    			
    	   		$.get("${searchUrl}", { 'name' : name, 'order' : sortbyproject} , function(data) {
        			$("#search-result-pane").html(data);
        		});
    			
    			$.cookie('searchcampaign', name);	
    		});
    		
    		// bind hover on result rows
    		$("#search-result-pane").on('mouseenter mouseleave', '#search-result-datatable tr', function(evt){
    			$(this).toggleClass('ui-state-active jstree-hovered');
    			/*if (event.type === 'mouseenter') */
    		});
    		
    		// bind click on result rows
    		$("#search-result-pane").on('click', '#search-result-datatable td.non-tree', function(){
    			if ($(this).attr('id').match("Library") != null){
    				return;
    			}
    			
    			$("#search-result-datatable tr").removeClass("jstree-clicked ui-state-default");
    			$(this).addClass("jstree-clicked ui-state-default");
    			
   				var url = getEntityURL($(this));   				
   				squashtm.workspace.contextualContent.loadWith(url);
    		});
    		
    		// bind double click on result rows
    		$("#search-result-pane").on('dblclick', '#search-result-datatable td.non-tree', function(){
    			selectTreeNode($(this).attr("id"));
    			$("#tabbed-pane").tabs("select", 0);
    		});
    		
    	});
    	
    	
    	// ********************* library part **********************
    	
    	function sortBy(value){
    		var table = $("#search-result-datatable").dataTable();
    		table.fnSort([[parseInt(value),'asc']]);
    	}

    	
    	function getEntityURL(jqRow){
    		var expr = /([a-z])(?=[A-Z])/g
    		var idParts = jqRow.attr("id").split("-");
    		
    		var lowerCase = idParts[1].replace(expr, "$1-").toLowerCase()+"s";
    		var id = idParts[2];
    		
    		return "${baseURL}/"+lowerCase+"/"+id;
    	} 
    	
    	
    	function selectTreeNode(searchNodeDomId){
    		var offset = "searchnode-";
    		var treeNodeName = searchNodeDomId.substring(offset.length);	
    		var treeNode = $("#tree li[id=\'"+treeNodeName+"\']");

    		if(treeNode[0] == null){
    			unfoldTree(treeNodeName);
    			
    		}else{
	    		jqTree=$("#tree");
	    		jqTree.jstree("deselect_all");
	    		jqTree.jstree("select_node",treeNode);
    		}
    		
    	}


    	function unfoldTree(treeNodeName){
  
        	$.ajax({
        		url : "${breadCrumbUrl}",
        		type : 'POST',
        		data : {
        			nodeName : treeNodeName
        		},
        		dataType : 'json'
        	})
        	.done(function(data){
    				openBreadCrumb(data);
    		});
    		
    	}

    	function openBreadCrumb(treeNodesIds){
    		var jqTree = $("#tree"), 
    			breadCrumbLength = treeNodesIds.length,
    			libraryName = treeNodesIds[breadCrumbLength - 1];
    		
    		jqTree.jstree("deselect_all");
    		var librayNode = jqTree.find("li[id=\'"+libraryName+"\']");
   		  	var start = breadCrumbLength -2;
   		  	jqTree.jstree("open_node",librayNode, function(){openFoldersUntillEnd(treeNodesIds,  jqTree, start);});

    	}
    	
    	
    	function openFoldersUntillEnd(treeNodesIds,  jqTree, i){
			  var treeNodeName = treeNodesIds[i];
			  var treeNode = jqTree.find("li[id=\'"+treeNodeName+"\']");
    		 
			if (i === 0){
  			  jqTree.jstree("deselect_all");
			  jqTree.jstree("select_node",treeNode);				
			} 
			  
			else {  
    			  jqTree.jstree("open_node",treeNode, function(){openFoldersUntillEnd(treeNodesIds,  jqTree, i-1);});
    		}
    	}
	});
});
</script>

<div id="search-input" style="padding-top: 1em;  bottom: .1cm;  float: left;  position: absolute;  top: .2cm;  left: 2px;  right: 2px;  padding: 0;    max-height: 7.8cm;" >
	<table>
		
		<tr>
			<td><span class="gray-text"> <f:message	key="label.Name" /> </span> : 
			<input id="searchName" type="text"
				class="std-height snap-right" style="width: 66%; margin-left: 2em;" />
			</td>
		</tr>

		<tr>
			<td><input type="checkbox" id="project-view" /> <span
				class="gray-text"> <f:message key="search.project.view" /> </span>
			</td>
		</tr>
		<f:message key="search.button.label" var="searchLabel" />
		<tr>
			<td style="text-align: center;"><input type="button"
				id="search-button" value="${ searchLabel }" />
			</td>
		</tr>

	</table>
</div>

	
<div id="search" class="search-div search-div-campaign">
	<div id="search-result-pane"></div>
</div>

