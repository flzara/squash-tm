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

<%@ attribute name="workspaceName" description="name of the workspace we are working in" %>
<%@ attribute name="attachListId" type="java.lang.Object"  description="the entity to which we bind those attachments" %>
<%@ attribute name="attachmentSet" type="java.util.Set" description="Set of attachments" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="List of attachments is editable. Defaults to false." %>
<%@ attribute name="autoJsInit" type="java.lang.Boolean" 
                                description="TRANSITIONAL. Whether this tag should also insert a hook for javascript init. Defaults to true." 
                                required="false" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>

<s:url var="baseURL" value="/attach-list/${attachListId}/attachments"/>


<comp:toggle-panel id="attachment-panel" titleKey="label.Attachments" open="${ entity.attachmentList.notEmpty }">
	<jsp:attribute name="panelButtons">
		<c:if test="${ editable }">
			<f:message var="uploadAttachment" key="label.UploadAttachment" />
			<input id="upload-attachment-button" type="button" value="${uploadAttachment}" class="sq-btn" title="${uploadAttachment}" />
			<f:message var="manageAttachment" key="label.Organize" />
			<f:message var="manageAttachmentTooltip" key="label.OrganizeAttachment" />	
			<input id="manage-attachment-bloc-button" type="button" value="${manageAttachment}" class="sq-btn" title="${manageAttachmentTooltip}" />
		</c:if>
	</jsp:attribute>	
	
	<jsp:attribute name="body">
		<div id="attachment-container" class="div-attachments">
			<at:attachment-display attachListId="${attachListId}" attachmentSet="${attachmentSet}" />
		</div>
	</jsp:attribute>
</comp:toggle-panel>

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
			upload.initAttachmentsBloc({
				baseURL : "${baseURL}",
				workspace: "${workspaceName}"
			});
		});
	});
});
</script>
</c:if>
