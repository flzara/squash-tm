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
<%@ tag description="definition of the popup that follows the execution of an automated test suite." pageEncoding="utf-8"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>

<s:url var="automatedSuitesUrl" value="/automated-suites" />

<f:message var="popupTitle" key='dialog.execute-auto.title'/>
<f:message var="closeLabel" key='label.Close' />
<f:message var="confirmLabel" key='label.Confirm' />
<f:message var="okLabel" 	key='label.Ok' />
<f:message var="cancelLabel" 	key='label.Cancel' />
<div>
<script id="exec-info-tpl" type="text/x-handlebars-template">
{{#each execs}}
	<tr id="execution-info-{{id}}" class="display-table-row {{oddity @index}}">
		<td class="execution-auto-project display-table-cell">{{automatedProject}}</td>
		<td class="execution-name display-table-cell">{{name}}</td>
		<td class="execution-status display-table-cell">{{{ballsyStatus status}}}</td>
		<td class="execution-node display-table-cell">{{node}}</td>
	</tr>
{{/each}}
</script>

<script id="node-selector-pnl-tpl" type="text/x-handlebars-template">
{{#each contexts}}
<p>
  <fieldset data-proj-id="{{project.id}}">
    <legend>
      <f:message key="message.automatedTests.ofProject" />&nbsp;<strong>{{project.label}}</strong>&nbsp;
      <f:message key="message.fromMasterNode" />&nbsp;<em>{{project.server.name}}</em>
    </legend>
  
    <div>
      <label for="nodes-list-{{project.id}}"><f:message key="message.automatedTests.executedOn" /></label>
      <select id="nodes-list-{{project.id}}">
        <option selected="selected" value=""><f:message key="label.irrelevant" /></option>
        <option value="master">{{project.server.name}}</option>
        {{#each project.nodes}}
        <option value="{{this}}">{{this}}</option>
        {{/each}}
      </select>
    </div>
  
    <div id="tests-tl-{{project.server.id}}" class="collapse sq-tl">
      <h5 class="tl-head">
        <span class="tl-state-icon"></span><f:message key="message.automatedTestsList" />({{tests.length}} <f:message key="label.testCases.lower" />)
      </h5>
      <ul class="tl-body">
        {{#each tests}}
        <li>{{name}}</li>
        {{/each}}
      </ul>
    </div>
    {{#unless orderGuaranteed}}
      <p class="error-message" ><label class="error-message"><f:message key="label.warning"/></label><f:message key="message.orderNonGuaranteed"/></p>
    {{/unless}}
  </fieldset>
</p>
{{/each}}
</script>

<!-- *************************POPUP*********************** -->
<div id="execute-auto-dialog" class="popup-dialog not-displayed" title="${popupTitle}" 
	data-def="url=${automatedSuitesUrl}, height=490">

	<div data-def="state=main">
		<div class="executions-auto-top" style="height:335px; width: 100%; overflow-y: scroll">
      <table class="display-table dataTable" style="width:100%">
        <thead>
          <tr>
            <td class="ui-state-default"><f:message key="label.automatedProject" /></td>
            <td class="ui-state-default"><f:message key="label.testCase" /></td>
            <td class="ui-state-default"><f:message key="label.Status" /></td>
            <td class="ui-state-default"><f:message key="label.TestAutomationServer" /></td>
          </tr>
        </thead>
        <tbody id="executions-auto-infos">
        </tbody>
      </table>
		</div>
		
		<div class="executions-auto-bottom" style="min-height:45px; width: 100%;">
		
			<div id="execution-auto-progress" style="width: 80%; margin: auto; margin-top: 20px">
				<div style="width: 80%; display: inline-block; vertical-align: middle">
					<div id="execution-auto-progress-bar" ></div>
				</div>
				<div id="execution-auto-progress-amount" style="width: 10%; display: inline-block"></div>
			</div>
			
		</div>
	</div>
	
	<div data-def="state=warning">
		<span><f:message key='message.CloseAutomatedSuiteOverview'/></span>
	</div>

    <div data-def="state=node-selector">
      <div id="node-selector-pnl">
      </div>
    </div>
    
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${closeLabel}" data-def="evt=mainclose, state=main, mainbtn=main"/>
		<input type="button" value="${confirmLabel}" data-def="evt=warningok, state=warning"/>
		<input type="button" value="${cancelLabel}" data-def="evt=warningcancel, state=warning, mainbtn=warning"/>	
    <input type="button" value="<f:message key="label.Confirm" />" data-def="evt=submitNodes, state=node-selector"/>
    <input type="button" value="<f:message key="label.Cancel" />" data-def="evt=discardNodes, state=node-selector"/>
	</div>
</div>

<script type="text/javascript">
publish("reload.auto-suite-overview-popup");
</script>
</div>
<!-- *************************/POPUP*********************** -->