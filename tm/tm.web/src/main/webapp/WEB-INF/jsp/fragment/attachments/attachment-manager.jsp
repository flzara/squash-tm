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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>



<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<c:url var="baseURL" value="/attach-list/${attachListId}/attachments" />


<div id="attachment-manager-header" class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	<div style="float: left; height: 100%;">
	<h2><span><f:message key="label.CurrentAttachments"/>&nbsp;:&nbsp;</span></h2>
	</div>
	<div class="snap-right">
  <c:choose>
      <c:when test="${openNewWindow}">
        <f:message var="back" key="label.Close" />
        <input id="back" type="button" value="${ back }" class="sq-btn" onClick="window.close();"/>
      </c:when>
      <c:otherwise>
        <f:message var="back" key="label.Back" />
        <input id="back" type="button" value="${ back }" class="sq-btn" onClick="history.back();"/>
      </c:otherwise>
  </c:choose>

	</div>
	<div class="unsnap"></div>

</div>


<div class="fragment-body">

	<div id="test-case-toolbar" class="toolbar-class ui-corner-all">
		<div class="toolbar-information-panel"></div>
		<div class="toolbar-button-panel">
			<f:message var="uploadAttachment" key="label.UploadAttachment" />
			<input id="add-attachment-button" type="button" value="${uploadAttachment}" class="sq-btn" title="${uploadAttachment}" />
		</div>
		<div class="unsnap"></div>
	</div>

	<%---------------------------------Attachments table ------------------------------------------------%>


	<comp:toggle-panel id="attachment-table-panel" titleKey="label.CurrentAttachments"  open="true" >
		<jsp:attribute name="panelButtons">
			<f:message var="renameAttachment" key="label.Rename" />
			<input type="button" value="${renameAttachment}" id="rename-attachment-button" class="sq-btn"  title="${renameAttachment}" />
			<f:message var="removeAttachment" key="label.Delete" />
			<input type="button" value="${removeAttachment}" id="delete-attachment-button" class="sq-btn"  title="${removeAttachment}" />
		</jsp:attribute>
		<jsp:attribute name="body">
			<at:attachment-table editable="${true}" baseURL="${baseURL}" />
		</jsp:attribute>
	</comp:toggle-panel>

	<div class="not-displayed">
		<at:attachment-dialogs />
	</div>

	<script type="text/javascript">
	require(["common"], function() {
		require(["jquery", "file-upload"], function($, upload){
			$(function(){
				upload.initAttachmentsManager({
					baseURL : "${baseURL}",
					aaData : ${json:serialize(attachmentsModel.aaData)}
				});
			});
		});
	});
	</script>

</div>

