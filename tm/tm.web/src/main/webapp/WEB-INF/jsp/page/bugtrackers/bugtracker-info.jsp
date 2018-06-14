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
		<style>
.adm-srv-auth .srv-auth-credentials-section {
  margin-top: 1em;
  padding: 1em;
  margin-bottom: 1em;
}
.adm-srv-auth .srv-auth-messagepane {
  max-width: 500px;
}
.adm-srv-auth .side-panel.not-displayed {
  display: none;
}
.adm-srv-auth label:after {
  content: "";
}
.adm-srv-auth .tbl {
  display: table;
}
.adm-srv-auth .tbl > div,
.adm-srv-auth .tbl .tr {
  display: table-row;
  margin-bottom: 1em;
  line-height: 3;
}
.adm-srv-auth .tbl > div > label,
.adm-srv-auth .tbl .tr > label {
  display: table-cell;
  padding-left: 10px;
  padding-right: 10px;
  vertical-align: top;
}
.adm-srv-auth .tbl > div > div.flexible,
.adm-srv-auth .tbl .tr > div.flexible {
  display: flex;
}
.adm-srv-auth .tbl > div > div.flexible > input,
.adm-srv-auth .tbl .tr > div.flexible > input {
  width: 300px;
  margin-left: 5px;
}
.adm-srv-auth .tbl > div > input,
.adm-srv-auth .tbl .tr > input {
  width: 100%;
}
.adm-srv-auth .tbl > div > textarea,
.adm-srv-auth .tbl .tr > textarea {
  width: 100%;
  height: 60px;
}

	
		</style>
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
			
				<%-- all the state variables necessary for the pre-rendering --%>
				
				<%--
				<c:set var="credsEnab" value="${(authConf.authPolicy == 'USER') ? 'disabled-transparent' : ''}"/>
				<c:set var="credsVisi" 	value="${(not empty authConf.failureMessage) ? 'not-displayed' : ''}" />
				<c:set var="policyAppEnab" value="${(not empty authConf.failureMessage) ? 'disabled=disabled' : ''}"/>

				<c:set var="failVisi" 	value="${(empty authConf.failureMessage) ? 'not-displayed' : ''}" />
				<c:set var="warnVisi" 	value="${(empty authConf.warningMessage) ? 'not-displayed' : ''}" />
				 --%>
				 
				<c:set var="featEnab" 	value="${(not empty authConf.failureMessage) ? 'disabled=disabled' : ''}"/>
				<c:set var="policyUsr" 	value="${(authConf.authPolicy == 'USER') ? 'checked=\"checked\"' : ''}"/>
				<c:set var="policyApp" 	value="${(authConf.authPolicy == 'APP_LEVEL') ? 'checked=\"checked\"' : ''}"/>
				<c:set var="failVisi" 	value="${(empty authConf.failureMessage) ? 'not-displayed' : ''}" />
				<c:set var="warnVisi" 	value="${(empty authConf.warningMessage) ? 'not-displayed' : ''}" />

				<f:message var="labelSuccess" key="bugtracker.admin.messages.success"/>
				<f:message var="labelSaveSuccess" key="bugtracker.admin.messages.save.success"/>
				<f:message var="testLabel" key="label.test"/>
				<f:message var="saveLabel" key="label.save"/>
			
				<%-- protocol configuration --%>
				<comp:toggle-panel id="bugtracker-auth-protocol" titleKey="label.BugtrackerAuthProtocol" open="true">
				<jsp:attribute name="body">
					<div class="adm-srv-auth">
						
						<%-- protocol selection --%>
						<select id="bt-auth-proto-select" style="display:block;">
							<c:forEach items="${authConf.availableProtos}" var="protocol">
							<option value="${protocol}" ${(authConf.selectedProto == protocol) ? 'selected' : ''} >
								<f:message key="authentication.protocol.${protocol.toString().toLowerCase()}"/>
							</option>
							</c:forEach>
						</select>
						
						<%-- protocol conf section --%>
						<div id="bt-auth-conf-main" class="srv-auth-credentials-section side-panel std-border std-border-radius" 
						 ${featEnab}>
							
							<div id="bt-auth-conf-form">
							<%-- templated by handlebars --%>
							</div>
	
							<div id="bt-auth-conf-buttonpane" class="centered" style="position:relative">
								<%-- note : there is no 'test' button, because testing auth-configuration is hard --%>
								<input type="button" class="sq-btn" id="bt-auth-conf-save" value="${saveLabel}"/>
							</div>						
							
						</div>
	
						<%-- msg pane of the protocol section --%>
						<div id="bt-auth-conf-messagezone" class="side-panel srv-auth-messagepane">

							<div id="bt-auth-failure" class="std-border std-border-radius ${failVisi}">
								<comp:notification-pane type="warning" txtcontent="${authConf.failureMessage}"/>
							</div>

							<div id="bt-auth-warning" class="${warnVisi}">
								<comp:notification-pane type="warning" txtcontent="${authConf.warningMessage}"/>
							</div>

							<div id="bt-auth-info" class="not-displayed">
								<comp:notification-pane type="info" txtcontent="${labelSuccess}"/>
							</div>

		                    <div id="bt-auth-save-info" class="not-displayed">
		                      <comp:notification-pane type="info" txtcontent="${labelSaveSuccess}"/>
		                    </div>
						</div>
											
					</div>		
				</jsp:attribute>
				</comp:toggle-panel>
	
				<%-- policy configuration --%>
				<comp:toggle-panel id="bugtracker-auth-policy" titleKey="label.BugtrackerAuthPolicy" open="true">
				<jsp:attribute name="body">
				<div class="adm-srv-auth">
					
					<%-- policy choice --%>
					<div>
						<label style="vertical-align:middle;">
							<input id="bt-auth-policy-user" type="radio" name="bt-auth-policy" value="user" ${policyUsr}>
							<f:message key="bugtracker.admin.policy.users"/>
						</label>
					</div>

					<div>
						<label style="vertical-align:middle;">
							<input id="bt-auth-policy-application" type="radio" name="bt-auth-policy" value="application" ${policyApp} ${policyAppEnab}>
							<f:message key="bugtracker.admin.policy.app"/>
						</label>
					</div>										
					
					
					<%-- app-level credentials section --%>
					<div id="bt-auth-creds-main" class="srv-auth-credentials-section side-panel std-border std-border-radius
					${credsEnab} ${credsVisi}" >

						<div id="bt-auth-creds-form">
							<%-- templated by handlebars --%>
						</div>

						<div id="bt-auth-creds-buttonpane" class="centered" style="position:relative">
							<input type="button" class="sq-btn" id="bt-auth-creds-test" value="${testLabel}"/>
							<input type="button" class="sq-btn" id="bt-auth-creds-save" value="${saveLabel}"/>
						</div>

					</div>	
					
											
	
					<%-- msg pane of the policy section --%>
					<div id="bt-auth-creds-messagezone" class="side-panel srv-auth-messagepane">

						<div id="bt-auth-failure" class="std-border std-border-radius ${failVisi}">
							<comp:notification-pane type="warning" txtcontent="${authConf.failureMessage}"/>
						</div>

						<div id="bt-auth-warning" class="${warnVisi}">
							<comp:notification-pane type="warning" txtcontent="${authConf.warningMessage}"/>
						</div>

						<div id="bt-auth-info" class="not-displayed">
							<comp:notification-pane type="info" txtcontent="${labelSuccess}"/>
						</div>

	                    <div id="bt-auth-save-info" class="not-displayed">
	                      <comp:notification-pane type="info" txtcontent="${labelSaveSuccess}"/>
	                    </div>
					</div>
						
				</div>
				</jsp:attribute>				
				</comp:toggle-panel>
				
			</div>


			
			<%-- templates --%>
			<div class="not-displayed" id="auth-templates">
				
				<script id="oauth-conf-template" type="text/x-handlebars-template">
				<div class="tbl">
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.consumerkey"/></label>
					<input id="oauth-conf-consumer-key" type="text" value="{{consumerKey}}" data-bind="consumerKey"/>
					<span class="error-message consumerKey-error"></span>
				</div>
	
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.request-tokens"/></label>
					<div class="flexible">
						<select id="oauth-conf-temptok-method" data-bind="requestTokenHttpMethod">
							<option value="GET" 	{{#equal requestTokenHttpMethod 'GET'}}selected="selected"{{/equal}}>GET</option>
							<option value="POST" {{#equal requestTokenHttpMethod 'POST'}}selected="selected"{{/equal}}>POST</option>
						</select>
						<input id="oauth-conf-temptok-url" type="text" value="{{requestTokenUrl}}" data-bind="requestTokenUrl"/>
						<span class="error-message requestTokenUrl-error"></span>			
					</div>
				</div>
	
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.access-tokens"/></label>
					<div class="flexible">
						<select id="oauth-conf-accesstok-method" data-bind="accessTokenHttpMethod">
							<option value="GET" 	{{#equal accessTokenHttpMethod 'GET'}}selected="selected"{{/equal}}>GET</option>
							<option value="POST" {{#equal accessTokenHttpMethod 'POST'}}selected="selected"{{/equal}}>POST</option>
						</select>
						<input id="oauth-conf-accesstok-url" type="text" value="{{accessTokenUrl}}" data-bind="accessTokenUrl"/>
						<span class="error-message accessTokenUrl-error"></span>			
					</div>
				</div>
	
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.autorize"/></label>
					<input id="oauth-conf-authorize-url" type="text" value="{{userAuthorizationUrl}}" data-bind="userAuthorizationUrl"/>
					<span class="error-message userAuthorizationUrl-error"></span>			
				</div>
	
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.secret"/></label>
					<textarea id="oauth-conf-secret" data-bind="clientSecret" >{{clientSecret}}</textarea>
					<span class="error-message clientSecret-error"></span>			
				</div>
				
				<div>
					<label><f:message key="bugtracker.admin.protocol.conf.oauth1a.sig-method"/></label>
					<select id="oauth-conf-sig-method" data-bind="signatureMethod">
						<option value="HMAC_SHA1" {{#equal signatureMethod 'HMAC_SHA1'}}selected="selected"{{/equal}}>HMAC-SHA1</option>
						<option value="RSA_SHA1" {{#equal signatureMethod 'RSA_SHA1'}}selected="selected"{{/equal}}>RSA-SHA1</option>
					</select>
				</div>		
				</div>
				</script>
				
				<script id="oauth-creds-template" type="text/x-handlebars-template">

				</script>
				
				<script id="basic-creds-template" type="text/x-handlebars-template">

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
