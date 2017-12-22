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
<%@ tag language="java" pageEncoding="utf-8" body-content="empty" description="structure of a dashboard for test cases. No javascript."%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ attribute name="workspace" required="false" %>

<div id="favorite-dashboard-wrapper" class="favorite-dashboard-wrapper">
 <div id='contextual-content-wrapper' class="dashboard-grid-in-classic-workspace  ui-corner-all">
	<!--empty -->
 </div>
</div>

<!-- SHOW DASHBOARD -->
  <script id="tpl-show-dashboard" type="text/x-handlebars-template">
	<div id="dashboard-name-div" class="ui-default-black">
		<div class="ui-favorite-dashboard-name small-margin-left">

				{{name}}

		</div>

	<div class="dashboard-grid-toolbar">
		<span>(<f:message key="workspace.custom-report.timestamp.label"/>)</span>
		<a class="favorite-dashboard-refresh-button sq-btn" role="button" title="<f:message key='label.Refresh' />">
			<span><f:message key='label.Refresh' /></span>
		</a>
		<a class="show-default-dashboard-button sq-btn" role="button"  title="<f:message key='label.default' />">
			<span><f:message key='label.default' /></span>
		</a>

		<div class="unsnap"></div>
	</div>
	</div>
			{{#if emptyDashboard}}
				{{> dashboardDoc}}
			{{/if}}
	<div id="dashboard-grid" class="dashboard-grid gridster jstree-drop">
		{{#each chartBindings}}
			{{> chart canWrite=../canWrite}}
		{{/each}}
	</div>
  </script>

  <script id="tpl-chart-in-dashboard" type="text/x-handlebars-template">
	<div id="widget-chart-binding-{{id}}" data-binding-id="{{id}}" class="dashboard-graph" data-row="{{row}}" data-col="{{col}}" data-sizex="{{sizeX}}" data-sizey="{{sizeY}}">
    	<div id="chart-binding-{{id}}" data-binding-id="{{id}}" class="chart-display-area" style="height:100%; width:100%;"></div>
    </div>
  </script>

  <script id="tpl-chart-display-area" type="text/x-handlebars-template">
	<div id="chart-binding-{{id}}" data-binding-id="{{id}}" class="chart-display-area" style="height:100%; width:100%;"></div>
  </script>

  <script id="tpl-new-chart-in-dashboard" type="text/x-handlebars-template">
	<div id="chart-binding-{{id}}" data-binding-id="{{id}}" class="chart-display-area" style="height:100%; width:100%;"></div>
	<span class="gs-resize-handle gs-resize-handle-both"></span>
  </script>

  <script id="tpl-dashboard-doc" type="text/x-handlebars-template">
    <div id="dashboard-doc">
    	<strong><f:message key="workspace.home.dashboard.empty"/></strong>
    </div>
  </script>

  <script id="tpl-default-dashboard" type="text/x-handlebars-template">
  	<div id="dashboard-name-div" class="fragment-header ui-default-black">
		<div class="ui-favorite-dashboard-name small-margin-left">
				<span><f:message key='report.view.code.dashboard' /></span>
		</div>

    	<div class="dashboard-grid-toolbar">
    		<a class="show-default-dashboard-button sq-btn" role="button"  title="<f:message key='label.default' />">
    			<span><f:message key='label.default' /></span>
    		</a>

    		<div class="unsnap"></div>
    	</div>
    	</div>

      <div id="dashboard-doc">
        <strong><f:message key="workspace.home.dashboard.default"/></strong>
      </div>
  </script>

  <!-- /SHOW DASHBOARD  -->


