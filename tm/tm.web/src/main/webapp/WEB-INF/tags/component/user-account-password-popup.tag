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

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<c:url var="userAccountUrl" value="/user-account/update" />

<f:message var="oldPassError" key="user.account.oldpass.error" />
<f:message var="newPassError" key="user.account.newpass.error"/>
<f:message var="confirmPassError" key="user.account.confirmpass.error"/>
<f:message var="samePassError" key="user.account.newpass.differ.error"/>  
<f:message var="passSuccess" key="user.account.changepass.success" />
<f:message var="confirmLabel" key="label.Confirm" />
<f:message var="cancelLabel" key="label.Cancel" />

<f:message var="passchangeTitle" key="user.account.password.label" />


<div id="password-change-popup" class="popup-dialog not-displayed" title="${passchangeTitle}">

    <div id="user-account-password-panel">
      
      <div >
        <label><f:message key="user.account.oldpass.label"/></label>
        <input type="password" id="oldPassword"/>
        <comp:error-message forField="oldPassword" />
      </div>
    
      <div>
        <label ><f:message key="user.account.newpass.label"/></label>
        <input type="password" id="newPassword"/>
        <comp:error-message forField="newPassword" />
      </div>
      
      <div>
        <label ><f:message key="user.account.confirmpass.label"/></label>
        <input type="password" id="user-account-confirmpass"/>
        <comp:error-message forField="user-account-confirmpass" />
      </div>      
      
    </div>
    
    <%-- the next comp:error is currently unused, however that might change later --%>
    <comp:error-message forField="user-account-changepass-status"/>

  <div class="popup-dialog-buttonpane">
    <input type="button" value="${confirmLabel}" data-def="mainbtn, evt=confirm" />
    <input type="button" value="${cancelLabel}" data-def="evt=cancel" />
  </div>

</div>

<script type="text/javascript">
require( ["common"], function(){
	require( ["jquery", "app/ws/squashtm.notification",  "jquery.squash.formdialog"], function($, notification){

	$(function(){
		
		var pwdDialog = $("#password-change-popup");
		pwdDialog.formDialog({width : 420});
		
		pwdDialog.on('formdialogconfirm', function(){
			if (! validatePassword()) return;		
			
			var oldPassword= $("#oldPassword").val();
			var newPassword = $("#newPassword").val();
			
			$.ajax({
				url : "${userAccountUrl}",
				type : "POST",
				dataType : "json",
				data : { "oldPassword" : oldPassword, "newPassword" : newPassword }
			})
			.done(function(){
				pwdDialog.formDialog('close');
				notification.showInfo("${passSuccess}");
			});
		});
		
		pwdDialog.on('formdialogcancel', function(){
			pwdDialog.formDialog('close');
		});
		
		$("#change-password-button").on('click', function(){
			pwdDialog.formDialog('open');
		});
	});

	
	<%-- we validate the passwords only. Note that validation also occurs server side. --%>
	function validatePassword(){
		
		//first, clear error messages
		$("#user-account-password-panel span.error-message").html('');
		
		var oldPassOkay=true,
			newPassOkay=true,
			confirmPassOkay=true,
			samePassesOkay=true;
		
		function filledOrDie(selector, errorspan, errmsg){
			var filled = ($(selector).val().length !== 0);
			if (! filled){
				$("span.error-message."+errorspan).html(errmsg);
			}
			return filled;
		}
		
		oldPassOkey = filledOrDie("#oldPassword", "oldPassword-error", "${oldPassError}");
		newPassOkey = filledOrDie("#newPassword", "newPassword-error", "${newPassError}");;
		newPassOkey = filledOrDie("#user-account-confirmpass", "user-account-confirmpass-error", "${confirmPassError}");
		
		if ((newPassOkay==true) && (confirmPassOkay==true)){
			var pass = $("#newPassword").val();
			var confirm = $("#user-account-confirmpass").val();
			
			if ( pass != confirm){
				$("span.error-message.newPassword-error").html("${samePassError}");
				samePassesOkay=false;
			}
		}

		return ( (oldPassOkay) && (newPassOkay) && (confirmPassOkay) &&(samePassesOkay) );
		
	}


	});
});
</script>




