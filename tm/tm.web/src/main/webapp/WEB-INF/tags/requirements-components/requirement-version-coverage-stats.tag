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
<%@ tag body-content="empty" description="show coverage stats" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<%-- ======================== VARIABLES & URLS ============================ --%>

<f:message var="labelConfirm" key="label.Confirm"/>
<f:message var="labelCancel"  key="label.Cancel"/>

<f:message var="titleCoverageRequirement"  key="requirement.rate.cover.main"/>
<f:message var="titleCoverageRequirementChildren"  key="requirement.rate.cover.children"/>
<f:message var="titleCoverageRequirementAll"  key="requirement.rate.cover.all"/>
<f:message var="titleVerificationRequirement"  key="requirement.rate.verification.main"/>
<f:message var="titleVerificationRequirementChildren"  key="requirement.rate.verification.children"/>
<f:message var="titleVerificationRequirementAll"  key="requirement.rate.verification.all"/>
<f:message var="titleValidationRequirement"  key="requirement.rate.validation.main"/>
<f:message var="titleValidationRequirementChildren"  key="requirement.rate.validation.children"/>
<f:message var="titleValidationRequirementAll"  key="requirement.rate.validation.all"/>


<%-- ======================== /VARIABLES & URLS ============================ --%>

<div id="coverage-stat">
	
	<div id="table-rates"></div>
	 
	<div id="dialog-select-perimeter-wrapper"></div>
</div>


<script type="text/x-handlebars-template" id="tpl-table-rates">
	<div class="display-table">
		<div class="display-table-row">
			<label for="validation-rate" class="display-table-cell">
				<f:message key="requirement.rate.perimeter" />
			</label>
			<div id="show-perimeter" class="display-table-cell">
				{{#if corruptedPerimeter}}
					<a id="change-perimeter-button" href="#"><f:message key="requirement.rate.perimeter.corrupted" /></a>
				{{else}}
					{{#if hasPerimeter}}
						<a id="change-perimeter-button" href="#">{{perimeterName}}</a>
					{{else}}
						<a id="change-perimeter-button" href="#"><f:message key="requirement.rate.perimeter.no" /></a>
					{{/if}}
				{{/if}}
			</div>
		</div>
	</div>

	<div class="display-table">
		<div class="display-table-row">
			<label class="display-table-cell">
				<f:message key="requirement.rate.cover" />
			</label>
			<div class="display-table-cell">
				<span title="${titleValidationRequirement}">{{coverage.requirementVersionRate}} % </span>
			</div>
			{{#if isAncestor}}
			<div class="display-table-cell">
				(<f:message key="requirement.rate.global" /> {{coverage.requirementVersionGlobalRate}} %,
				<f:message key="requirement.rate.child" /> {{coverage.requirementVersionChildrenRate}} %) 
			</div>
			{{/if}}
		</div>
		
		{{#if hasPerimeter}}
		<div class="display-table-row">
			<label class="display-table-cell">
				<f:message key="requirement.rate.verification" />
			</label>
			<div class="display-table-cell">
				<span title="${titleVerificationRequirement}">{{verification.requirementVersionRate}} % </span>
			</div>
			{{#if isAncestor}}
			<div class="display-table-cell">
				(<f:message key="requirement.rate.global" /> {{verification.requirementVersionGlobalRate}} %,
				<f:message key="requirement.rate.child" /> {{verification.requirementVersionChildrenRate}} %)
			</div>
			{{/if}}
		</div>
		<div class="display-table-row">
			<label class="display-table-cell">
				<f:message key="requirement.rate.validation" />
			</label>
			<div class="display-table-cell">
				<span title="${titleValidationRequirement}">{{validation.requirementVersionRate}} % </span>
			</div>
			{{#if isAncestor}}
			<div class="display-table-cell">
				(<f:message key="requirement.rate.global" /> {{validation.requirementVersionGlobalRate}} %,
				<f:message key="requirement.rate.child" /> {{validation.requirementVersionChildrenRate}} %)
			</div>
			{{/if}}
		</div>
		{{/if}}
	</div>
</script>

<script type="text/x-handlebars-template" id="tpl-show-perimeter">
	
</script>

<script type="text/x-handlebars-template" id="tpl-dialog-select-perimeter">
  <div id="dialog-select-perimeter" class="not-displayed popup-dialog" title="<f:message key='requirement.rate.perimeter.title'/>">
	<div class="main" style="height:600px; ">
		<div id="tree-pane">
    		<div id="perimeter-tree" class="tree jstree" style="position:relative; overflow:auto;"></div>
			<div class="popup-dialog-buttonpane">
     				<input type="button" class="button" value="${labelConfirm}" data-def="evt=confirm, mainbtn"/>
      				<input type="button" class="button" value="${labelCancel}" data-def="evt=cancel"/>
    		</div>
		<div>
	<div>
  </div>
</script>
<script type="text/javascript">
publish('reload.requirement.requirementversionrate');
</script>
