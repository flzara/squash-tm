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
<%@ tag language="java" pageEncoding="utf-8"%>

<%@ attribute name="entity" type="java.lang.Object"  description="the entity to which we bind those attachments" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="List of attachments is editable. Defaults to false." %>
<%@ attribute name="tabId" description="id of the concerned tab" required="true" %>
<%@ attribute name="tableModel" type="java.lang.Object" description="datatable model for preloaded attachments. Optional." required="false" %>
<%@ attribute name="autoJsInit" type="java.lang.Boolean" 
                                description="TRANSITIONAL. Whether this tag should also insert a hook for javascript init. Defaults to true." 
                                required="false" %>



<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>


<c:url var="baseURL" value="/attach-list/${entity.attachmentList.id}/attachments" />

<%-- ------------------------------------- DOM -------------------------------------------------- --%>

<div id="${tabId}" class="table-tab">

	<div class="toolbar" >
	<c:if test="${ editable }">
			<f:message var="uploadAttachment" key="label.UploadAttachment" />
			<f:message var="uploadValue" key="label.Add" />
			<input id="add-attachment-button" type="button" value="${uploadValue}" class="sq-btn"  title="${uploadAttachment}" />
			<f:message var="renameAttachment" key="label.Rename" />
			<input type="button" value="${renameAttachment}" id="rename-attachment-button" class="sq-btn"  title="${renameAttachment}" />
			<f:message var="removeAttachment" key="label.DeleteAttachment" />
			<f:message var="removeValue" key="label.Delete" />
			<input type="button" value="${removeValue}" id="delete-attachment-button" class="sq-btn"   title="${removeAttachment}" />
	</c:if>
	</div>
	
	<div class="table-tab-wrap" >
		<at:attachment-table baseURL="${baseURL}" editable="${editable}"/>
	</div>
	

<c:if test="${ editable }">
	<div class="not-displayed">		
		<at:attachment-dialogs />
	</div>
</c:if>		

<c:if test="${empty autoJsInit or autoJsInit}">
  <script type="text/javascript">
  require(["common"], function() {
  	require(["jquery", "file-upload"], function($, upload){
  		$(function(){
  			upload.initAttachmentsManager({
  				baseURL : "${baseURL}",
  				aaData : ${json:serialize(tableModel.aaData)}
  			});
  		});
  	});
  });
  </script>
</c:if>

</div>

