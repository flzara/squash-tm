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
<!-- DEPRECATED - USE TAG FROM JQ TAGLIB -->
<%@ tag body-content="empty" description="Applies jquery l'n'f to all buttons and links with the 'button' class" %>
<script type="text/javascript">
//TODO remove this tag. Init in js file instead.
	$(function() {
		$.squash.decorateButtons();
	});
	
	function decorateButton(domButton){
		$(domButton).squashButton();
	}
</script>