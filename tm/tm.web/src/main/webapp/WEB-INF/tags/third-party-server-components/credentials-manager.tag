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
	That tag encapsulates the panes for configuring the modalities of authentication for a third party server.
	
	Usage:
	- server side : requires an instance of ThirdPartyServerCredentialsManagementBean (named authConf, see below).	
	- client side : require third-party-server/credentials-manager.js
 --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="authConf" required="true" type="java.lang.Object"  description="the configuration bean" %>

<div id="server-authentication-masterpane">


	<%-- state variables etc --%>
	<c:set var="policyUsr" 	value="${(authConf.authPolicy == 'USER') ? 'checked=\"checked\"' : ''}"/>
	<c:set var="policyApp" 	value="${(authConf.authPolicy == 'APP_LEVEL') ? 'checked=\"checked\"' : ''}"/>
	<f:message var="testLabel" key="label.test"/>
	<f:message var="saveLabel" key="label.save"/>

	<%-- protocol configuration --%>
	<comp:toggle-panel id="server-auth-protocol" titleKey="label.ServerAuthProtocol" open="true">
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
			 style="min-width:50%">

				<div id="srv-auth-conf-form" class="templated-form" >
				<%-- templated by handlebars --%>
				</div>

				<div class="centered srv-auth-buttonpane" style="position:relative">
					<span class="needs-save-msg" style="display:none;"><f:message key="thirdpartyserver.admin.messages.needs-save"/></span>
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
	<comp:toggle-panel id="server-auth-policy" titleKey="label.ServerAuthPolicy" open="true">
	<jsp:attribute name="body">
	<div class="adm-srv-auth">
		<%-- policy conf panel --%>
		<div class="side-panel">
			<div class="tbl side-panel">
				<%-- user policy choice --%>
				<div>
					<label><f:message key="thirdpartyserver.admin.policy.user-section"/></label>
					<div>
						<label style="vertical-align:middle; display:block;">
							<input type="radio" name="srv-auth-policy" value="USER" ${policyUsr}>
							<f:message key="thirdpartyserver.admin.policy.users"/>
						</label>

						<label style="vertical-align:middle; display:block;">
							<input type="radio" name="srv-auth-policy" value="APP_LEVEL" ${policyApp}>
							<f:message key="thirdpartyserver.admin.policy.app"/>
						</label>
					</div>
				</div>

				<%-- app-level credentials section --%>
				<div class="srv-form">
					<label><f:message key="thirdpartyserver.admin.policy.squashtm-section"/></label>
					<div class="srv-auth-form-main std-border std-border-radius" >

						<div id="srv-auth-creds-form" class="templated-form">
							<%-- templated by handlebars --%>
						</div>

						<div class="centered srv-auth-buttonpane" style="position:relative">
							<span class="needs-save-msg" style="display:none;"><f:message key="thirdpartyserver.admin.messages.needs-save"/></span>
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
					<label><f:message key="thirdpartyserver.admin.protocol.conf.oauth1a.consumerkey"/></label>
					<input type="text" value="{{consumerKey}}" data-bind="consumerKey"/>
					<span class="error-message consumerKey-error"></span>
				</div>

				<div>
					<label><f:message key="thirdpartyserver.admin.protocol.conf.oauth1a.request-tokens"/></label>
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
					<label><f:message key="thirdpartyserver.admin.protocol.conf.oauth1a.access-tokens"/></label>
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
					<label><f:message key="thirdpartyserver.admin.protocol.conf.oauth1a.autorize"/></label>
					<input type="text" value="{{userAuthorizationUrl}}" data-bind="userAuthorizationUrl"/>
					<span class="error-message userAuthorizationUrl-error"></span>
				</div>

				<div>
					<label><f:message key="thirdpartyserver.admin.protocol.conf.oauth1a.secret"/></label>
					<textarea data-bind="clientSecret" >{{clientSecret}}</textarea>
					<span class="error-message clientSecret-error"></span>
				</div>

				<div>
					<label><f:message key="thirdpartyserver.admin.protocol.conf.oauth1a.sig-method"/></label>
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
						<input type="password" value="{{password}}" data-bind="password">
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