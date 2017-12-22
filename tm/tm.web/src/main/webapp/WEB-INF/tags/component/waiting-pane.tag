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
<%@ tag body-content="empty" description="a generic pane that should be displayed when some time processing task is running (ajax requests mostly)" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
 		
<%--
<div class="waiting-loading full-size-hack centered minimal-height" >
 --%>
<div class="waiting-loading centered minimal-height" >
	<div style="font-size: 1.5em; margin-top : 300px;"><f:message key="squashtm.processing" /></div>
</div>	
