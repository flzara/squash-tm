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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Internal Server Error</title>

<style type="text/css">

	.detail-entry {
		font-weight : bold;
	}
	
	body {
		background-repeat : no-repeat;
		background-attachment : fixed;
		background-size : 100% 100%;	
	}

</style>

</head>



<body>

<h1>Error (status : ${code})</h1>

<p>
	<span class="detail-entry">error : </span>
	<span>${error}</span>
</p>

<p>
	<span class="detail-entry">type :</span>
	<span>${exception}</span>
</p>

<p>
	<span class="detail-entry">time : </span>
	<span>${timestamp.toString()}</span>
</p>

<p>
	<span class="detail-entry">message : </span>
	<span>${message}</span>
</p>

<p>
	<pre>
	<c:forEach var="frame" items="${trace}">
	<p>${frame.toString()}</p>
	</c:forEach>
	</pre>
</p>

</body>
</html>