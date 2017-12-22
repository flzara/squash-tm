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
<%@ attribute name="locale" description="example : fr"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


  <style>
	.date-hidden {display:none};
  </style>


<script type="text/javascript">
require(["common"], function() {
	require(["datepicker/jquery.squash.datepicker-locales", "jquery.squash.datepicker", "jquery.squash.datepicker-auto"], function() {
		// noop
	});
});
</script>

