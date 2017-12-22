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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	
	
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<?xml version="1.0" encoding="utf-8" contentType="text/html; charset=utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<c:url var="folderStatisticsUrl" value="/campaign-folders/${folder.id}/dashboard-statistics" />
<c:url var="folderStatisticsPrintUrl" value="/campaign-folders/${folder.id}/dashboard?printmode=true" />


<div class="ui-widget-header ui-state-default ui-corner-all fragment-header purple">
	<h2><span>${folder.name}</span></h2>			
</div>

<div class="fragment-body">
	<dashboard:campaign-folder-dashboard-panel url="${folderStatisticsUrl}" 
                                                  printUrl="${folderStatisticsPrintUrl}" 
                                                  printmode="${printmode}" 
                                                  allowsSettled="${allowsSettled}" 
                                                  allowsUntestable="${allowsUntestable}" />
</div>

<script type="text/javascript">

	require(["common"], function(){
		require(["domReady","campaign-folder-management"], function(domReady, campmanager){
			domReady(function(){
				campmanager.initDashboardPanel({
					master : '#dashboard-master',
					model : ${json:serialize(dashboardModel)}
				});	
			});
		});
	});

</script>
