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
NOTE :  
	the prefilled translator cache is now useless because every data fetched by the 
	squash translator are now saved in the localStorage object.
--%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>

// -------------------- some shorthands for locale in .js---------------------------------------------
squashtm.message = squashtm.message || {};
squashtm.message.cancel = "<f:message key='label.Cancel'/>";
squashtm.message.placeholder = "<f:message key='rich-edit.placeholder'/>";
squashtm.message.confirm = "<f:message key='label.Confirm'/>";		
squashtm.message.infoTitle = "<f:message key='popup.title.info'/>";
squashtm.message.errorTitle = "<f:message key='popup.title.error'/>";	


// ------------------- prefilled translator cache ---------------------------------------------------- 

var _langcache = {	
	'error.generic.label' : "<f:message key ='error.generic.label'/>",
	'execution.execution-status.SETTLED' : "<f:message key='execution.execution-status.SETTLED'/>",
	'execution.execution-status.UNTESTABLE' : "<f:message key='execution.execution-status.UNTESTABLE'/>",
	'execution.execution-status.BLOCKED' : "<f:message key='execution.execution-status.BLOCKED'/>",
	'execution.execution-status.FAILURE' : "<f:message key='execution.execution-status.FAILURE'/>",
	'execution.execution-status.SETTLED' : "<f:message key='execution.execution-status.SETTLED'/>",
	'execution.execution-status.SUCCESS' : "<f:message key='execution.execution-status.SUCCESS'/>",
	'execution.execution-status.RUNNING' : "<f:message key='execution.execution-status.RUNNING'/>",
	'execution.execution-status.READY' : "<f:message key='execution.execution-status.READY'/>",
	'execution.execution-status.WARNING' : "<f:message key='execution.execution-status.WARNING'/>",
	'execution.execution-status.NOT_RUN' : "<f:message key='execution.execution-status.NOT_RUN'/>",
	'execution.execution-status.NOT_FOUND' : "<f:message key='execution.execution-status.NOT_FOUND'/>",
	'execution.execution-status.ERROR' : "<f:message key='execution.execution-status.ERROR'/>",
	'label.Cancel' : "<f:message key='label.Cancel'/>",
	'label.Ok' : "<f:message key='label.Ok'/>",
	'label.Confirm' : "<f:message key='label.Confirm'/>",
	'rich-edit.language.value' : "<f:message key='rich-edit.language.value'/>",
	'squashtm.locale' : "<f:message key='squashtm.locale'/>",
	'squashtm.message.infoTitle' : "<f:message key='popup.title.info'/>",
	'squashtm.dateformat' : "<f:message key='squashtm.dateformat'/>",
	'squashtm.dateformat.iso' : "<f:message key='squashtm.dateformat.iso'/>",
	'squashtm.dateformatShort.datepicker' : "<f:message key='squashtm.dateformatShort.datepicker'/>"
};

squashtm.message.cache = _langcache;
