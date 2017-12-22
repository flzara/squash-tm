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
window.squashtm = window.squashtm || {};
window.squashtm.app = window.squashtm.app || {};
window.squashtm.app.contextRoot = window.squashtm.app.contextRoot || "."; 


server = sinon.fakeServer.create();
server.respondWith([200, { "Content-Type": "application/json"},  JSON.stringify({
	 sLengthMenu: "Show _MENU_ entries",
		sInfoEmpty: "Showing 0 to 0 of 0 entries",
		sSearch: "Search",
		sInfoFiltered: "(filtered from _MAX_ total entries)",
		sInfo: "Showing _START_ to _END_ of _TOTAL_ entries",
		sZeroRecords: "No matching records found",
		oPaginage: {
		sFirst: "First",
		sLast: "Last",
		sNext: "Next",
		sPrevious: "Previous"
		
		}
		})])


define([ "jquery", "app/report/ReportCriteriaPanel", "app/report/ConciseFormModel", "common" ], function($, Panel, FormModel) {
	"use strict";


	
	function arrayMatcher(dis, dat) {
		if (!_.isArray(dis) || !_.isArray(dat)) return false;

		return _.difference(dis, dat) === _.difference(dat, dis) === [];
	}

	describe("ReportCriteriaPanel", function() {
		var $input;

		beforeEach(function() {
			//jasmine.addCustomEqualityTester(arrayMatcher);
	
			$("body").append($("<div>", {id: "rcp"}));

			
		});

		afterEach(function() {
			$("#rcp").remove();

		});

		function fixtureInput(options) {

			options.name = "rcp-input";
			var $panel = $("#rcp");
			var $input = $("<input>", options);
			$panel.append($input);
			return $input;
		}

		function fixtureModel(type, value) {
			if (arguments.length === 0) {
				return new FormModel();
			}
			return new FormModel({"rcp-input": {type: type, val: value}});
		}

		function fixtureFormerState(type, value) {
			return { "rcp-input": { type: type, val: value } };
		}

		function testedPanel(model, former) {
			return new Panel({ el: "#rcp", model: model }, { formerState: former });
		}

		describe("with text controls", function() {
			beforeEach(function() {
				// given a text input
				$input = fixtureInput({ type: "text", value:"", class: "rpt-text-crit" });
			});

			it("should init text model", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model)

				// model should be inited
				expect(model.get("rcp-input").type).toBe("TEXT");
				expect(model.get("rcp-input").val).toBe("");
			});

			it("should update a text control", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model);


				// when text is changed and blurred
				$input.val("batman").blur();
				$input.val("batman").change();

				// then
				expect(model.get("rcp-input").val).toBe("batman");
			});

			it("should render text control", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model, fixtureFormerState("TEXT", "spidey"));

				// then
				expect($input.val()).toBe("spidey");
			});
		});

		describe("with checkbox controls", function() {
			beforeEach(function() {
				$input = fixtureInput({ type: "checkbox", value:"cbx", checked: false });
			});

			it("should init model", function() {
				// and a controller
				var model = fixtureModel();
				var panel = testedPanel(model);

				// model should be inited
				expect(model.get("rcp-input").type).toBe("CHECKBOX");
				expect(model.get("rcp-input").val).toBe(false);
			});

			it("should update a checkbox control", function() {
				var model = fixtureModel();
				var panel = testedPanel(model);

				// when
				$input.prop("checked", true).change();

				// then
				expect(model.get("rcp-input").val).toBe(true);

				// when
				$input.val("batman").prop("checked", false).change();

				// then
				expect(model.get("rcp-input").val).toBe(false);
			});

			it("should render checkbox", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model, fixtureFormerState("CHECKBOX", true));

				// then
				expect($input.prop("checked")).toBe(true);
			});
		});

		describe("with radio group controls", function() {
			var $radio1, $radio4;

			beforeEach(function() {
				$radio1 = fixtureInput({ type: "radio", value:"radio on", checked: true });
				$radio4 = fixtureInput({ type: "radio", value:"radio 4", checked: false });
			});

			it("should init model", function() {
				// and a controller
				var model = fixtureModel();
				var panel = testedPanel(model);

				// model should be inited
				expect(model.get("rcp-input").type).toBe("RADIO_BUTTONS_GROUP");
				expect(model.get("rcp-input").val).toBe("radio on");
			});

			it("should update a radio group control", function() {
				var model = fixtureModel();
				var panel = testedPanel(model);

				// when
				$radio1.prop("checked", false).change();
				$radio4.prop("checked", true).change();

				// then
				expect(model.get("rcp-input").val).toBe("radio 4");
			});

			it("should render radio group", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model, fixtureFormerState("RADIO_BUTTONS_GROUP", "radio 4"));

				// then
				console.log($radio1);
				console.log($radio4);
				expect($radio1.prop("checked")).toBe(false);
				expect($radio4.prop("checked")).toBe(true);
			});
		});

		describe("with bound buttons", function() {
			var $radio1, $radio4, $button1, $button4;

			function fixtureRadio(options) {
				options.name = "rcp-input";
				var $panel = $("#rcp").append("<li>");
				var $input = $("<input>", options);
				$panel.append($input);
				return $input;
			}

			beforeEach(function() {
				$radio1 = fixtureRadio({ id: "one-binder", type: "radio", value:"radio on", checked: true });
				$radio4 = fixtureRadio({ id: "four-binder", type: "radio", value:"radio 4", checked: false });

				$button1 = fixtureInput({ type: "button", id: "one-open" });
				$button4 = fixtureInput({ type: "button", id: "four-open" });
			});


			it("should update bound buttons state", function() {
				var model = fixtureModel();
				var panel = testedPanel(model);

				// when
				$radio1.prop("checked", false).change();
				$radio4.prop("checked", true).change();

				// then
				expect($button1.prop("disabled")).toBe(false);
				expect($button4.prop("disabled")).toBe(true);
			});

			it("should render bound buttons", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model, fixtureFormerState("RADIO_BUTTONS_GROUP", "radio 4"));

				// then
				expect($button1.prop("disabled")).toBe(true);
				expect($button4.prop("disabled")).toBe(false);
			});
		});

		describe("with dropdown controls", function() {
			var $batman, $robin;

			beforeEach(function() {
				var $panel = $("#rcp");
				$panel.append($("<div>", { class: "rpt-drop" }).append($("<select>", { name: "rcp-input" })));
				$input = $("select[name='rcp-input']");

				$batman = $("<option>", { value: "batman", selected: false });
				$input.append($batman);

				$robin = $("<option>", { value: "robin", selected: true });
				$input.append($robin);
			});

			it("should init model", function() {
				// and a controller
				var model = fixtureModel();
				var panel = testedPanel(model);

				// model should be inited
				expect(model.get("rcp-input").type).toBe("DROPDOWN_LIST");
				expect(model.get("rcp-input").val).toBe("robin");
			});

			it("should update a dropdown control", function() {
				var model = fixtureModel();
				var panel = testedPanel(model)


				// when selection is changed
				$batman.prop("selected", true).change();
				$robin.prop("selected", false).change();

				// then
				expect(model.get("rcp-input").val).toBe("batman");
			});

			it("should render dropdown list", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model, fixtureFormerState("DROPDOWN_LIST", "batman"));

				// then
				expect($input.val()).toBe("batman");
			});
		});

		describe("with checkboxes groups controls", function() {
			var $batman, $robin;

			beforeEach(function() {
				$batman = fixtureInput({ type: "checkbox", value:"batman", checked: true, "data-grouped": true });
				$robin = fixtureInput({ type: "checkbox", value:"robin", checked: false, "data-grouped": true });
			});

			it("should init model", function() {
				// and a controller
				var model = fixtureModel();
				var panel = testedPanel(model);

				// model should be inited
				expect(model.get("rcp-input").type).toBe("CHECKBOXES_GROUP");
				expect(model.get("rcp-input").val).toEqual(["batman"]);
			});

			it("should update a checkboxes group control", function() {
				var model = fixtureModel();
				var panel = testedPanel(model)

				// when
				$robin.prop("checked", true).change();

				// then
				expect(model.get("rcp-input").val).toEqual(["batman", "robin"]);

				// when
				$batman.prop("checked", false).change();

				// then
				expect(model.get("rcp-input").val).toEqual(["robin"]);

				// when
				$robin.prop("checked", false).change();

				// then
				expect(model.get("rcp-input").val).toEqual([]);
			});

			it("should render checkboxes group", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model, fixtureFormerState("CHECKBOXES_GROUP", ["robin"]));

				// then
				expect($batman.prop("checked")).toBe(false);
				expect($robin.prop("checked")).toBe(true);
			});
		});

		describe("with date picker controls", function() {
			beforeEach(function() {
				// given a text input
				$input = $("<span>", { class: "rpt-date-crit", "data-locale": "fr", "data-nodate": "-", id: "rcp-input" });
				$("#rcp").append($input);
			});

			it("should init text model", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model)

				// model should be inited
				expect(model.get("rcp-input").type).toBe("DATE");
				expect(model.get("rcp-input").val).toBe("--");
			});

			it("should render date control", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model, fixtureFormerState("DATE", "2014-01-02"));

				// then
				expect($input.text()).toBe("02/01/2014");
			});
		});

		describe("with project pickers controls", function() {
			beforeEach(function() {
				$input = $("<div>", { class: "project-picker", id: "rcp-input" });
				$("#rcp").append($input);
			});

			it("should init model", function() {
				// given
				var model = fixtureModel();
				console.log("fisture model ", model)
				var panel = testedPanel(model)

				// model should be inited
				console.log("model after", model)
				expect(model.get("rcp-input").type).toBe("PROJECT_PICKER");
				expect(model.get("rcp-input").val).toEqual([]);
			});

			it("should render control", function() {
				// given
				var model = fixtureModel();
				var panel = testedPanel(model);

				// then
				console.log(panel);
				expect(panel.projectPickers["rcp-input"]).not.toBeUndefined();
			});
		});

		it("should update a radio buttons group control", function() {
			// given an input
			var $batman = fixtureInput({ type: "radio", value:"batman", checked: true });
			var $robin = fixtureInput({ type: "radio", value:"robin", checked: false });

			// and a controller
			var model = fixtureModel();
			var panel = testedPanel(model)

			// model should be inited
			expect(model.get("rcp-input").type).toBe("RADIO_BUTTONS_GROUP");
			expect(model.get("rcp-input").val).toBe("batman");

			// when
			$robin.prop("checked", true).change();

			// then
			expect(model.get("rcp-input").val).toBe("robin");
		});
	});
});