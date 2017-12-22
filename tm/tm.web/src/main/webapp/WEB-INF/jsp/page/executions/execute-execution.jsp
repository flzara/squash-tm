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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="issues" tagdir="/WEB-INF/tags/issues" %>
<%@ taglib prefix="wu" uri="http://org.squashtest.tm/taglib/workspace-utils" %>

<s:url var="attachmentsURL" value="/attach-list/${executionStep.attachmentList.id}/attachments"/>
<s:url var="btEntityUrl" value="/bugtracker/execution-step/${executionStep.id}"/>


<%-- ----------------------------------- Authorization ----------------------------------------------%>


<c:set var="editable" value="${false}"/>

<c:if test="${not milestoneConf.locked}">
  <authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE"
                    domainObject="${ execution }">
    <c:set var="editable" value="${ true }"/>
  </authz:authorized>
</c:if>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" class="execute-html">

<c:choose>
  <c:when test="${totalSteps == 0 }">
		<span><f:message key="execute.header.nostep.label"/>
		</span>
  </c:when>
  <c:otherwise>
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
      <c:set var="stIndex" value="${executionStep.executionStepOrder}"/>
      <title> #${stIndex + 1 } - ${execution.name}
        <c:if
          test="${execution.datasetLabel != null && execution.datasetLabel != ''}">- ${execution.datasetLabel}</c:if>
        (${stIndex + 1 }/${totalSteps})
      </title>

      <layout:common-head/>
      <layout:_common-script-import highlightedWorkspace=""/>

        <%-- cautious : below are used StepIndexes and StepIds. Dont get confused. --%>
      <s:url var="executeNext" value="${ currentStepsUrl }/index/${stIndex+1}?optimized=${param.optimized}"/>
      <c:choose>
        <c:when test="${executionStep.first}">
          <s:url var="executePrevious" value="${ currentStepsUrl }/prologue?optimized=${param.optimized}"/>
        </c:when>
        <c:otherwise>
          <s:url var="executePrevious" value="${ currentStepsUrl }/index/${stIndex-1}?optimized=${param.optimized}"/>
        </c:otherwise>
      </c:choose>

      <s:url var="executeThis" value="${ currentStepsUrl }/index/${stIndex}?optimized=${param.optimized}"/>
      <s:url var="executeComment" value="${ currentStepsUrl }/${executionStep.id}"/>
      <s:url var="executeStatus" value="${ currentStepsUrl }/${executionStep.id}"/>
      <s:url var="executeInfos" value="${ currentStepsUrl }/index/${stIndex}"/>

      <comp:sq-css name="squash.purple.css"/>
    </head>


    <body class="execute-html-body">


    <script type="text/javascript">
      requirejs.config({
        config: {
          'execution-dialog-main': {
            model: {
              id: ${executionStep.id},
              type: 'execution-step',
              index: ${stIndex}
            },
            basic: {
              stepId: ${executionStep.id},
              execId: ${execution.id},
              id: ${executionStep.id},
              index: ${stIndex},
              status: "${executionStep.executionStatus}",
              isFirst: ${executionStep.first},
              mode: ${not empty hasNextTestCase ? '"suite-mode"' : '"single-mode"' },
              hasNextTestCase: ${ (not empty hasNextTestCase) and hasNextTestCase },
              hasNextStep: ${ (not empty hasNextStep) and hasNextStep },
              hasCufs: ${hasCustomFields},
              hasDenormCufs: ${hasDenormFields},
                				hasBugtracker : ${executionStep.project.bugtrackerConnected},
                        bugtrackerMode:"${bugtrackerMode}"
            },
            urls: {
              baseURL: "${currentStepsUrl}",
              executeNext: "${executeNext}",
              executePrevious: "${executePrevious}",
              executeThis: "${executeThis}",
              executeComment: "${executeComment}",
              executeStatus: "${executeStatus}",
              executeInfos: "${executeInfos}",
              testPlanItemUrl: "${testPlanItemUrl}",
              attachments: "${attachmentsURL}",
              bugtracker: "${btEntityUrl}"
            },
            permissions: {
              editable: ${editable}
            }
          }

        }
      });

      require(["common"], function () {
        require(["execution-dialog-main"], function () {
        });
      });


    </script>

      <%-- Wizard initialization --%>
    <comp:init-wizards workspace="campaign"/>


    <f:message var="stopTitle" key="execute.header.button.stop.title"/>
    <f:message var="untestableLabel" key="execute.header.button.untestable.title"/>
    <f:message var="blockedTitle" key="execute.header.button.blocked.title"/>
    <f:message var="failureTitle" key="execute.header.button.failure.title"/>
    <f:message var="passedTitle" key="execute.header.button.passed.title"/>
    <f:message var="previousTitle" key="execute.header.button.previous.title"/>
    <f:message var="nextTitle" key="execute.header.button.next.title"/>
    <f:message var="modifyTcLabel" key="execution.execute.modify.testcase"/>
    <f:message var="nextTestCaseTitle" key="execute.header.button.next-test-case.title"/>

    <div id="execute-header">
      <table width="100%">
        <tr style="vertical-align:top;">
          <td class="centered">
            <button id="execute-stop-button" class="sq-btn std-btn ui-button control-button" title="${stopTitle}">
              <span class="ui-icon ui-icon-power"></span>
            </button>
          </td>
          <td id="execution-previous-next" style="position: relative; top: -2px;" class="centered">
            <button id="execute-previous-button" class="sq-btn std-btn ui-button control-button"
                    title="${previousTitle}">
              <span class="ui-icon ui-icon-triangle-1-w"></span>
            </button>
  				<span id="execute-header-numbers-label">
  					${executionStep.executionStepOrder+1} / ${totalSteps}
  				</span>
            <button id="execute-next-button" class="sq-btn std-btn ui-button control-button"
                    ${not hasNextStep ? 'disabled="disabled"' : ''}title="${nextTitle}">
              <span class="ui-icon ui-icon-triangle-1-e"></span>
            </button>
          </td>
          <c:if test="${not empty testPlanItemUrl}">
            <td class="centered " id="execute-next-test-case-panel">
              <c:url var="nextTCUrl" value='${ testPlanItemUrl }/next-execution/runner?optimized=false'/>
              <form action="${nextTCUrl}" method="post">
                <c:set var="nextTCdisabled" value="${(empty hasNextTestCase) or (not hasNextTestCase) or hasNextStep}"/>
                <button id="execute-next-test-case" name="classic" class="sq-btn std-btn control-button"
                  ${ nextTCdisabled ? 'disabled="disabled"' : ''}
                        title="${ nextTestCaseTitle }">
                  <span class="ui-button-icon-primary ui-icon ui-icon-seek-next"></span>
                </button>
              </form>
            </td>
          </c:if>
          <td class="centered">
            <label id="evaluation-label-status">
              <f:message key="execute.header.status.label"/>
            </label>
            <c:choose>
              <c:when test="${editable }">
                <comp:execution-status-combo name="executionStatus" id="execution-status-combo"
                                             allowsUntestable="${allowsUntestable}" allowsSettled="${allowsSettled}"
                                             selected="${executionStep.executionStatus}"/>
                <c:if test="${allowsUntestable}">
                  <button id="execute-untestable-button" class="sq-btn std-btn ui-button control-button status-button"
                          data-status="UNTESTABLE" title="${untestableLabel}">
                    <span class="ui-icon exec-status-untestable"></span>
                  </button>
                </c:if>
                <button id="execute-blocked-button" class="sq-btn std-btn ui-button control-button status-button"
                        data-status="BLOCKED" title="${blockedTitle}">
                  <span class="ui-icon exec-status-blocked"></span>
                </button>
                <button id="execute-fail-button" class="sq-btn std-btn ui-button control-button status-button"
                        data-status="FAILURE" title="${failureTitle}">
                  <span class="ui-icon exec-status-failure"></span>
                </button>
                <button id="execute-success-button" class="sq-btn std-btn ui-button control-button status-button"
                        data-status="SUCCESS" title="${passedTitle}">
                  <span class="ui-icon exec-status-success"></span>
                </button>
                <c:if test="${execution.project.allowTcModifDuringExec()}">
                  <button id="edit-tc" style="float: right"
                          class="sq-btn std-btn ui-button control-button " ${ executionStep.referencedTestStep == null ? 'disabled="disabled"' : ''}
                          title="${modifyTcLabel}">
                    <span class="ui-icon ui-icon-pencil"/>
                  </button>
                </c:if>
              </c:when>
              <c:otherwise>
                <%--
                    I strongly doubt one can ever access this jsp if not granted the edit status
                    so this 'otherwise' block might never be called
                 --%>
                <c:set var="spanExecstatus" value="${executionStep.executionStatus.canonicalStatus}"/>
                            <span style="white-space:nowrap; display:inline-block;"
                                  class="exec-status-label exec-status-${fn:toLowerCase(spanExecstatus)}">
                              <f:message key="${spanExecstatus.i18nKey}"/>
                            </span>
              </c:otherwise>
            </c:choose>
          </td>

        </tr>
      </table>
    </div>

    <script type="text/javascript">
      publish("reload.executedialog.toolbar");
    </script>

    <div id="execute-body" class="execute-fragment-body">
      <c:if test="${ hasCustomFields or hasDenormFields }">
        <comp:toggle-panel id="custom-fields-panel" titleKey="title.step.fields" open="true">
            <jsp:attribute name="body">
              <div id="dfv-information-table" class="display-table"></div>
              <div id="cuf-information-table" class="display-table"></div>
            </jsp:attribute>
        </comp:toggle-panel>

        <script type="text/javascript">
          publish("reload.executedialog.cufs");
        </script>
      </c:if>
      <comp:toggle-panel id="execution-action-panel"
                         titleKey="execute.panel.action.title"
                         open="true">
					<jsp:attribute name="body">
						<div id="execution-action">${executionStep.action}</div>
					</jsp:attribute>
      </comp:toggle-panel>

      <comp:toggle-panel id="execution-expected-result-panel"
                         titleKey="execute.panel.expected-result.title"
                         open="true">
					<jsp:attribute name="body">
						<div id="execution-expected-result">${executionStep.expectedResult}</div>
					</jsp:attribute>
      </comp:toggle-panel>

      <div id="execute-evaluation">

        <div id="execute-evaluation-leftside">
          <c:if test="${editable}">
            <c:set var="descrRicheditAttributes"
                   value="class='editable rich-editable' data-def='url=${executeComment}'"/>
          </c:if>
          <comp:toggle-panel id="execution-comment-panel"
                             titleKey="execute.panel.comment.title"
                             open="true">
							<jsp:attribute name="body">
								<div id="execution-comment" ${descrRicheditAttributes}>${executionStep.comment}</div>
							</jsp:attribute>
          </comp:toggle-panel>
        </div>

        <div id="execute-evaluation-rightside">
          <comp:step-information-panel auditableEntity="${executionStep}" entityUrl="${executeInfos}"/>
        </div>
        <div style="clear: both; visibility: hidden"></div>
      </div>

      <at:attachment-bloc attachListId="${executionStep.attachmentList.id}" workspaceName="campaign"
                          editable="${ editable }"
                          attachmentSet="${attachments}" autoJsInit="${false}"/>

      <script type="text/javascript">
        publish('reload.executedialog.attachments');
      </script>

        <%-- ----------------------- bugtracker (if present)----------------------------------------%>
      <c:if test="${executionStep.project.bugtrackerConnected}">
        <issues:butracker-panel entity="${executionStep}" issueDetector="true"/>

        <script type="text/javascript">
          publish('reload.executedialog.issues');
        </script>
      </c:if>

        <%-- ----------------------- /bugtracker (if present)----------------------------------------%>


      <script type="text/javascript">
        publish("reload.executedialog.complete");
      </script>


    </div>


    </body>
  </c:otherwise>
</c:choose>
</html>
