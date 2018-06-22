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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%------------------------------------- URLs et back button ----------------------------------------------%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="bugtrackerUrl" value="/bugtracker/{bugtrackerId}">
	<s:param name="bugtrackerId" value="${bugtracker.id}" />
</s:url>
<s:url var="admBugtrackerUrl" value="/administration/bugtrackers" />

<f:message var="confirmLabel"   key="label.Confirm"/>
<f:message var="renameLabel" key="label.Rename" />
<f:message var="cancelLabel" key="label.Cancel" />


<layout:info-page-layout titleKey="workspace.bugtracker.info.title" isSubPaged="true">
	<jsp:attribute name="head">
		<comp:sq-css name="squash.grey.css" />
	</jsp:attribute>

	<jsp:attribute name="titlePane"><h2 class="admin"><f:message key="label.administration" /></h2></jsp:attribute>
	<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.bugtracker.info.title" /></h2>
	</jsp:attribute>

	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="document.location.href='${admBugtrackerUrl}'"/>
	</jsp:attribute>

	<jsp:attribute name="informationContent">

		<div id="bugtracker-name-div"
			class="ui-widget-header ui-corner-all ui-state-default fragment-header">

			<div style="float: left; height: 3em">
				<h2>
					<label for="bugtracker-name-header"><f:message
							key="label.Bugtracker" />
					</label><a id="bugtracker-name-header" ><c:out
							value="${ bugtracker.name }" escapeXml="true" />
					</a>
				</h2>
			</div>
			<div class="unsnap"></div>

		</div>

		<div class="fragment-body">
			<%------------------------------------------------ BODY -----------------------------------------------%>

			<%--- Toolbar ---------------------%>
			<div id="bugtracker-toolbar" class="toolbar-class ui-corner-all">

				<div class="toolbar-button-panel">
					<f:message var="rename" key="rename" />
					<input type="button" value="${ rename }" id="rename-bugtracker-button" class="sq-btn" />

					<f:message var="delete" key='project.button.delete.label' />
	    			<input type="button" value="${ delete }" id="delete-bugtracker-button" class="sq-btn" />
				</div>
			</div>
			<%--------End Toolbar ---------------%>

			<%----------------------------------- INFORMATION PANEL -----------------------------------------------%>
			<br />
			<br />
			<comp:toggle-panel id="bugtracker-info-panel"
				titleKey="label.BugtrackerInformations" open="true">

				<jsp:attribute name="body">
					<div id="bugtracker-description-table" class="display-table">

						<div class="display-table-row">
							<label for="bugtracker-kind" class="display-table-cell">
							<f:message key="label.Kind" />
							</label>
							<div class="display-table-cell" id="bugtracker-kind">${ bugtracker.kind }</div>
							<comp:select-jeditable componentId="bugtracker-kind" jsonData="${bugtrackerKinds}" targetUrl="${bugtrackerUrl}" />
						</div>

						<div class="display-table-row">
							<label for="bugtracker-url" class="display-table-cell">
							<f:message key="label.Url" />
							</label>
							<div class="display-table-cell editable text-editable" data-def="url=${bugtrackerUrl}" id="bugtracker-url">${ bugtracker.url }</div>
						</div>

						<div class="display-table-row">
							<label for="bugtracker-iframeFriendly" class="display-table-cell">
							<f:message key="label.DisplaysInIframe" />
							</label>
							<div class="display-table-cell" id="bugtracker-iframeFriendly" style="cursor:pointer">
								<input id="bugtracker-iframeFriendly-checkbx" type="checkbox"
								<c:if test="${bugtracker.iframeFriendly}">
								checked="checked"
								</c:if>
								/>
							</div>
						</div>

					</div>
				</jsp:attribute>
			</comp:toggle-panel>
			

			<%-----------------------------------END INFORMATION PANEL -----------------------------------------------%>
			
			
			
			<%----------------------------------- BEGIN AUTHENTICATION MGNT -----------------------------------------------%>
			<div id="bugtracker-authentication-masterpane">
			

				<%-- state variables etc --%>
				<c:set var="policyUsr" 	value="${(authConf.authPolicy == 'USER') ? 'checked=\"checked\"' : ''}"/>
				<c:set var="policyApp" 	value="${(authConf.authPolicy == 'APP_LEVEL') ? 'checked=\"checked\"' : ''}"/>
				<f:message var="testLabel" key="label.test"/>
				<f:message var="saveLabel" key="label.save"/>
			
				<%-- protocol configuration --%>
				<comp:toggle-panel id="bugtracker-auth-protocol" titleKey="label.BugtrackerAuthProtocol" open="true">
				<jsp:attribute name="body">
					<div class="adm-srv-auth">
						
						<%-- protocol selection --%>
						<select id="srv-auth-proto-select" style="display:block;">
							<c:forEach items="${authConf.availableProtos}" var="protocol">
							<option value="${protocol}" ${(authConf.selectedProto == protocol) ? 'selected' : ''} >
								<f:message key="authentication.protocol.${protocol.toString().toLowerCase()}"/>
							</option>
							</c:forEach>
						</select>
						
						<%-- protocol conf section --%>
						<div class="srv-form srv-auth-form-main side-panel std-border std-border-radius" 
						 ${featEnab} style="min-width:50%">
							
							<div id="srv-auth-conf-form" class="templated-form" >
							<%-- templated by handlebars --%>
							</div>
		
							<div class="centered srv-auth-buttonpane" style="position:relative">
								<span class="needs-save-msg" style="display:none;"><f:message key="bugtracker.admin.messages.needs-save"/></span>
								<%-- note : there is no 'test' button, because testing auth-configuration is hard or impossible --%>
								<input type="button" class="sq-btn auth-save" value="${saveLabel}"/>
							</div>						
							
						</div>
	
						
						<div class="side-panel srv-auth-messagepane" >
							<%--templated by handlebars --%>
						</div>
											
					</div>		
				</jsp:attribute>
				</comp:toggle-panel>
	
				<%-- policy configuration --%>
				<comp:toggle-panel id="bugtracker-auth-policy" titleKey="label.BugtrackerAuthPolicy" open="true">
				<jsp:attribute name="body">
				<div class="adm-srv-auth">
					<%-- policy conf panel --%>
					<div class="side-panel">
						<div class="tbl side-panel">	
							<%-- user policy choice --%>
							<div>
								<label><f:message key="bugtracker.admin.policy.user-section"/></label>
								<div>
									<label style="vertical-align:middle; display:block;">
										<input type="radio" name="srv-auth-policy" value="USER" ${policyUsr}>
										<f:message key="bugtracker.admin.policy.users"/>
									</label>		
									
									<label style="vertical-align:middle; display:block;">
										<input type="radio" name="srv-auth-policy" value="APP_LEVEL" ${policyApp}>
										<f:message key="bugtracker.admin.policy.app"/>
									</label>										
								</div>	
							</div>	
							
							<%-- app-level credentials section --%>
							<div class="srv-form">
								<label><f:message key="bugtracker.admin.policy.squashtm-section"/></label>
								<div class="srv-auth-form-main std-border std-border-radius
								${credsEnab} ${credsVisi}" >
			
									<div id="srv-auth-creds-form" class="templated-form">
										<%-- templated by handlebars --%>
									</div>
			
									<div class="centered srv-auth-buttonpane" style="position:relative">
										<span class="needs-save-msg" style="display:none;"><f:message key="bugtracker.admin.messages.needs-save"/></span>
										<input type="button" class="sq-btn auth-test" value="${testLabel}"/>
										<input type="button" class="sq-btn auth-save" value="${saveLabel}"/>
									</div>
			
								</div>	
							</div>
						</div>	
					</div>		
							
					<%-- message zone --%>
					<div class="side-panel srv-auth-messagepane">
						<%--templated by handlebars --%>
					</div>	
				</div>
				</jsp:attribute>				
				</comp:toggle-panel>
				
			</div>


			
			<%-- templates --%>
			<div class="not-displayed" id="auth-templates">
				
				<script id="oauth-conf-template" type="text/x-handlebars-template">
				<div class="tbl" style="width:100%">
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.consumerkey"/></label>
					<input type="text" value="{{consumerKey}}" data-bind="consumerKey"/>
					<span class="error-message consumerKey-error"></span>
				</div>
	
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.request-tokens"/></label>
					<div class="flexible">
						<select data-bind="requestTokenHttpMethod">
							<option value="GET" 	{{#equal requestTokenHttpMethod 'GET'}}selected="selected"{{/equal}}>GET</option>
							<option value="POST" 	{{#equal requestTokenHttpMethod 'POST'}}selected="selected"{{/equal}}>POST</option>
						</select>
						<input type="text" value="{{requestTokenUrl}}" data-bind="requestTokenUrl"/>
						<span class="error-message requestTokenUrl-error"></span>			
					</div>
				</div>
	
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.access-tokens"/></label>
					<div class="flexible">
						<select data-bind="accessTokenHttpMethod">
							<option value="GET" 	{{#equal accessTokenHttpMethod 'GET'}}selected="selected"{{/equal}}>GET</option>
							<option value="POST" {{#equal accessTokenHttpMethod 'POST'}}selected="selected"{{/equal}}>POST</option>
						</select>
						<input  type="text" value="{{accessTokenUrl}}" data-bind="accessTokenUrl"/>
						<span class="error-message accessTokenUrl-error"></span>			
					</div>
				</div>
	
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.autorize"/></label>
					<input type="text" value="{{userAuthorizationUrl}}" data-bind="userAuthorizationUrl"/>
					<span class="error-message userAuthorizationUrl-error"></span>			
				</div>
	
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.secret"/></label>
					<textarea data-bind="clientSecret" >{{clientSecret}}</textarea>
					<span class="error-message clientSecret-error"></span>			
				</div>
				
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.sig-method"/></label>
					<select data-bind="signatureMethod">
						<option value="HMAC_SHA1" {{#equal signatureMethod 'HMAC_SHA1'}}selected="selected"{{/equal}}>HMAC-SHA1</option>
						<option value="RSA_SHA1" {{#equal signatureMethod 'RSA_SHA1'}}selected="selected"{{/equal}}>RSA-SHA1</option>
					</select>
				</div>		
				</div>
				</script>
				
	
				<script id="oauth-creds-template" type="text/x-handlebars-template">
				<div class="tbl">
					<div>
						<label><f:message key="label.Token"/></label>
						<input type="text" value="{{token}}" data-bind="token">
					</div>
					<div>
						<label><f:message key="label.TokenSecret"/></label>
						<input value="{{tokenSecret}}" data-bind="tokenSecret">
					</div>
				</div>		
				</script>
				
				
				<script id="basic-creds-template" type="text/x-handlebars-template">
				<div class="tbl">
					<div>
						<label><f:message key="label.Login"/></label>
						<input type="text" value="{{username}}" data-bind="username">
					</div>
					<div>
						<label><f:message key="label.Password"/></label>
						<input value="{{password}}" data-bind="password">
					</div>
				</div>			
				</script>

				<script id="messagepane-template" type="text/x-handlebars-template">
				<div class="display-table-row">
				    <div class="display-table-cell warning-cell">
				      <div class="generic-signal"></div>
				    </div>
				                   
				    <div class="txt-message display-table-cell" style="padding-top:20px"></div>   
				</div>	
				</script>

			</div>
			
			<%----------------------------------- END AUTHENTICATION MGNT -----------------------------------------------%>
			
			</div>
		<%---------------------------------------------------------------END  BODY -----------------------------------------------%>
	</jsp:attribute>
</layout:info-page-layout>




<!-- --------------------------------RENAME POPUP--------------------------------------------------------- -->

    <f:message var="renameBTTitle" key="dialog.rename-bugtracker.title" />
    <div id="rename-bugtracker-dialog" class="not-displayed popup-dialog"
        title="${renameBTTitle}">

        <label><f:message key="dialog.rename.label" /></label>
        <input type="text" id="rename-bugtracker-input" maxlength="255" size="50" />
        <br />
        <comp:error-message forField="name" />


        <div class="popup-dialog-buttonpane">
          <input type="button" value="${renameLabel}" data-def="mainbtn, evt=confirm"/>
          <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
        </div>
    </div>



<!-- ------------------------------------END RENAME POPUP------------------------------------------------------- -->

<f:message var="deleteBugtrackerTitle" 	key="dialog.delete-bugtracker.title" 	/>
<f:message var="warningDelete" 			key="dialog.deleteBugTracker.warning" 	/>
<div id="delete-bugtracker-popup" class="popup-dialog not-displayed" title="${deleteBugtrackerTitle}">

    <comp:notification-pane type="error" txtcontent="${warningDelete}"/>

	<div class="popup-dialog-buttonpane">
	    <input class="confirm" type="button" value="${confirmLabel}" />
	    <input class="cancel" type="button" value="${cancelLabel}" />
	</div>
</div>



<script type="text/javascript">

  requirejs.config({
	 config :{
		 'bugtracker-manager/bugtracker-info': {
			 backUrl : "${admBugtrackerUrl}",
			 btUrl : "${bugtrackerUrl}",
			 authConf : ${json:serialize(authConf)}
		 }
	 }
  });

  require(['common'], function(){
	  require(['bugtracker-manager/bugtracker-info']);
  })


</script>
