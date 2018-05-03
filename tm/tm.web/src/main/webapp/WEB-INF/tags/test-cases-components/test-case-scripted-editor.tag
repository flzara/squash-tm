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

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ attribute name="writable" required="true" type="java.lang.Boolean"  description="can current user have rights to edit this script" %>

<div id="tab-tc-script-editor">
  <%-- ==================== toolbar definition ===================--%>
  <div>

    <div class="left btn-toolbar">
            <span class="group">
              <button id="tc-script-save-button" title="Ctrl+Alt+S"
                      data-icon="ui-icon ui-icon-plusthick"
                      class="sq-btn ui-icon-plusthick" style="display: none">
                    <f:message key="label.save"/>
              </button>
              <button id="tc-script-cancel"
                      data-icon="ui-icon-plusthick"
                      class="sq-btn" style="display: none">
                    <f:message key="label.Cancel"/>
              </button>
               <button id="tc-script-snippets-button" title="Ctrl+<f:message key="label.Space"/>"
                       data-icon="ui-icon ui-icon-plusthick"
                       class="sq-btn ui-icon-plusthick" style="display: none">
                 <f:message key="label.Insert"/>
              </button>
            </span>
    </div>
    <div class="right btn-toolbar">
            <span class="group">
              <button id="tc-script-toggle-help-panel"
                      data-icon="ui-icon-plusthick"
                      class="sq-btn">
                    <f:message key="label.Help"/>
              </button>
            </span>
    </div>
  </div>
  <div id="tc-script-editor" class="tc-script-editor tc-script-editor-option-closed"></div>
</div>

