/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * conf : must be available as config for the main module. { completeTitle : "title for the completion message dialog",
 * completeTestMessage : "content for the completion message dialog" completeSuiteMessage : "content for the completion
 * of the whole suite message dialog" } + the rest described as in the 'state' variable below
 *
 * comment[1] [Issue 1126] Had to use directly "refreshParent" instead of "iframe.unload(refreshParent)" beacause the
 * latest do not work with IE 8
 *
 */

define(["jquery", "app/ws/squashtm.notification", "squash.translator", "jquery.squash.messagedialog"],
	function ($, notification, translator) {

		/* this is a constructor */
		return function (settings) {

			// ***************** init function **********************

			this.state = $.extend({

				optimized: undefined,
				lastTestCase: undefined,
				testSuiteMode: undefined,
				prologue: undefined,

				baseStepUrl: undefined,
				nextTestCaseUrl: undefined,

				currentExecutionId: undefined,
				currentStepId: undefined,

				firstStepIndex: undefined,
				lastStepIndex: undefined,
				currentStepIndex: undefined,

				currentStepStatus: undefined

			}, settings);

			// ***************** private stuffs ****************

			var _updateState = $.proxy(function (newState) {
				this.state = newState;
			}, this);

			var getJson = $.proxy(function (url) {
				return $.get(url, null, null, "json");
			}, this);

			var refreshParent = $.proxy(function () {
				if (!!window.opener) {
					try {
						window.opener.squashtm.execution.refresh(); // should be defined in the calling context.
						if (window.opener.config.identity.restype === "test-suites") {
							window.opener.squashtm.execution.refreshTestSuiteInfo();
						}
					} catch (anyex) {
						window.opener.location.href = window.opener.location.href;
					}
					if (window.opener.progressWindow) {
						window.opener.progressWindow.close();
					}
				}
				// when url is input in browser location bar, there is no opener
			}, this);

			var testComplete = $.proxy(function () {
				if (!this.state.testSuiteMode) {
					$.squash.openMessage(settings.completeTitle, settings.completeTestMessage).done(function () {
						refreshParent();// see "comment[1]"
						window.close();

					});
				} else if (canNavigateNextTestCase()) {
					this.navigateNextTestCase();
					refreshParent();

				} else {
					$.squash.openMessage(settings.completeTitle, settings.completeSuiteMessage).done(function () {
						refreshParent();// see "comment[1]"
						window.close();
					});
				}

			}, this);

			var navigateLeftPanel = $.proxy(function (url) {
				parent.frameleft.document.location.href = url;
				refreshParent();
			}, this);

			// ************ public functions ****************

			this.fillRightPane = function (url) {
				try {

					url = (url.indexOf('://') == -1) ? 'http://' + url : url;
					var iframeBody = this.rightPane.find('iframe body');
					var iframe = this.rightPane.find('iframe');

					$.post(squashtm.app.contextRoot + "/checkXFO/", {URL: url}, function (xframeAllowed) {
						if (!xframeAllowed) {
							iframe.attr('src', "about:blank");
							notification.showError(translator.get('message.exception.OER.XFODoNotPermitIFrame'));
						} else {
							iframe.attr('src', url);
						}
					});

				} catch (ex) {

					this.rightPane.find('iframe body').text(ex);
				}

			};

			this.navigateNext = function () {
				var state = this.state;

				if (!isLastStep()) {
					var nextStep = state.currentStepIndex + 1;
					this.navigateRandom(nextStep);
				} else {
					testComplete();
				}
			};

			this.submitComment = function () {
				$("#iframe-left").contents().find("#execution-comment-panel").find("button[type=submit]").click();
			};

			this.navigatePrevious = function () {
				var state = this.state;

				if (!isPrologue()) {
					var prevStep = state.currentStepIndex - 1;
					this.navigateRandom(prevStep);
				}
			};

			this.navigatePrologue = function () {
				var state = this.state;
				var url = state.baseStepUrl + "/prologue?optimized=true";
				navigateLeftPanel(url);
				state.currentStepIndex = 0;
				this.control.ieoControl("navigateRandom", 0);
			};

			this.navigateRandom = function (newStepIndex) {
				var state = this.state;
				var control = this.control;

				if (newStepIndex === 0) {
					this.navigatePrologue();
				} else {
					var zeroBasedIndex = newStepIndex - 1;
					var nextUrl = state.baseStepUrl + "/index/" + zeroBasedIndex + "?optimized=true";
					getJson(nextUrl).success(function (json) {
						state.currentStepStatus = json.currentStepStatus;
						state.currentStepId = json.currentStepId;

						var frameLeftUrl = state.baseStepUrl + "/index/" + zeroBasedIndex + "?optimized=true";
						navigateLeftPanel(frameLeftUrl);

						state.currentStepIndex = newStepIndex;
						control.ieoControl("navigateRandom", newStepIndex);

					});
				}
			};

			this.navigateNextTestCase = function () {
				var state = this.state;
				var url = state.nextTestCaseUrl + "?optimized=true";
				var that = this;
				getJson(url).success(function (json) {
					_updateState(json);
					that.navigateRandom(that.state.currentStepIndex);
				});
			};

			this.closeWindow = function () {
				refreshParent();
				window.close();
			};

			this.getState = function () {
				return this.state;
			};

			// ********************** predicates ************************

			var canNavigateNextTestCase = $.proxy(function () {
				var state = this.state;
				return ((state.testSuiteMode) && (!state.lastTestCase) && (isLastStep()));
			}, this);

			var isLastStep = $.proxy(function () {
				return (this.state.currentStepIndex === this.state.lastStepIndex);
			}, this);

			var isPrologue = $.proxy(function () {
				return (this.state.currentStepIndex === this.state.firstStepIndex);
			}, this);

			// *********** setters etc *********************

			var getStatusUrl = $.proxy(function () {
				var state = this.state;
				return state.baseStepUrl + "/" + state.currentStepId;
			}, this);

			this.setControl = function (control) {

				var self = this;

				this.control = control;
				control.ieoControl("setManager", this);

				var nextButton = control.ieoControl("getNextStepButton");
				var prevButton = control.ieoControl("getPreviousStepButton");
				var stopButton = control.ieoControl("getStopButton");
				var untsButton = control.ieoControl("getUntestableButton");
				var blckButton = control.ieoControl("getBlockedButton");
				var succButton = control.ieoControl("getSuccessButton");
				var failButton = control.ieoControl("getFailedButton");
				var mvTCButton = control.ieoControl("getNextTestCaseButton");
				var statusCombo = control.ieoControl("getStatusCombo");

				nextButton.click(function () {
					self.submitComment();
					self.navigateNext();
				});

				prevButton.click(function () {
					self.submitComment();
					self.navigatePrevious();
				});

				mvTCButton.click(function () {
					self.navigateNextTestCase();
				});

				stopButton.click(function () {
					self.closeWindow();
				});

				statusCombo.change(function () {
					var cbox = this;
					$.post(getStatusUrl(), {
						executionStatus: $(cbox).val()
					});
				});

				succButton.click(function () {
					$.post(getStatusUrl(), {
						executionStatus: "SUCCESS"
					}).success(function () {
						self.submitComment();
						self.navigateNext();
					});
				});

				failButton.click(function () {
					$.post(getStatusUrl(), {
						executionStatus: "FAILURE"
					}).success(function () {
						self.submitComment();
						self.navigateNext();
					});
				});

				untsButton.click(function () {
					$.post(getStatusUrl(), {
						executionStatus: "UNTESTABLE"
					}).success(function () {
						self.submitComment();
						self.navigateNext();
					});
				});

				blckButton.click(function () {
					$.post(getStatusUrl(), {
						executionStatus: "BLOCKED"
					}).success(function () {
						self.submitComment();
						self.navigateNext();
					});
				});

			};

			this.setRightPane = function (rightPane) {
				this.rightPane = rightPane;
			};

		};

	});
