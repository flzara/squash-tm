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
define(["common", "app/report/ConciseFormModel"], function(_, FormModel) {
	"use strict";

	describe("ConciseFormModel", function() {
		function fixtureModel(type, value) {
			return new FormModel({batman: {type: type, val: value}});
		}

		function applyFormerState(type, value) {
			return {
				to: function(model) {
					model.applyFormerState({"batman": { type: type, val: value}});
				}
			};
		}

		it("should apply former state to uninitialized attribute", function() {

			var model = new FormModel();

			// when
			applyFormerState("TEXT", "leatherpants").to(model);

			// then
			expect(model.get("batman").type).toBe("TEXT");
			expect(model.get("batman").val).toBe("leatherpants");
		});

		it("should apply initialized former text state", function() {
			var model = fixtureModel("TEXT", "leatherpants");

			//when
			applyFormerState("TEXT", "spandex").to(model);

			// then
			expect(model.get("batman").type).toBe("TEXT");
			expect(model.get("batman").val).toBe("spandex");
		});

		it("should apply initialized former project picker state", function() {
			var model = fixtureModel("PROJECT_PICKER", []);

			//when
			applyFormerState("PROJECT_PICKER", ["spandex"]).to(model);

			// then
			expect(model.get("batman").type).toBe("PROJECT_PICKER");
			expect(model.get("batman").val).toEqual(["spandex"]);
		});

		it("should apply initialized former text state", function() {
			var model = fixtureModel("TEXT", "leatherpants");

			//when
			applyFormerState("TEXT", "spandex").to(model);

			// then
			expect(model.get("batman").type).toBe("TEXT");
			expect(model.get("batman").val).toBe("spandex");
		});

		it("should ditch mismatching former text state type", function() {
			var model = fixtureModel("CHECKBOX", true);

			//when
			model.applyFormerState({"batman":  { type: "TEXT", val: "leatherpants" }});

			// then
			expect(model.get("batman").type).toBe("CHECKBOX");
			expect(model.get("batman").val).toBe(true);
		});

		it("should ditch gibberish former checkbox state", function() {
			//given
			var model = fixtureModel("CHECKBOX", true);

			//when
			model.applyFormerState({"batman":  { type: "CHECKBOX", value: "leatherpants" }}); // gibberish attr

			// then
			expect(model.get("batman").type).toBe("CHECKBOX");
			expect(model.get("batman").val).toBe(true);

			//when
			applyFormerState("CHECKBOX", "leatherpants" ).to(model); // gibberish value type

			// then
			expect(model.get("batman").type).toBe("CHECKBOX");
			expect(model.get("batman").val).toBe(true);
		});

		function ditchMismatchingFormerState(type, initial, gibberish) {
			var model = fixtureModel(type, initial);

			//when
			model.applyFormerState({"batman":  { type: type, val: gibberish }});

			// then
			expect(model.get("batman").type).toBe(type);
			expect(model.get("batman").val).toBe(initial);
		}

		it("should ditch gibberish former checkboxes state", function() {
			ditchMismatchingFormerState("CHECKBOXES_GROUP", ["spandex"], "leatherpants")
		});

		it("should ditch gibberish former date state", function() {
			ditchMismatchingFormerState("DATE", "2014-02-01", "2014/02/01")
		});

		it("should ditch gibberish former project picker state", function() {
			ditchMismatchingFormerState("PROJECT_PICKER", ["spandex"], "leatherpants")
		});

		it("should ditch gibberish former tree picker state", function() {
			ditchMismatchingFormerState("TREE_PICKER", ["spandex"], "leatherpants")
		});

		it("should apply former atom date state to uninitialized attribute", function() {
			var model = new FormModel();

			// when
			model.applyFormerState({"date": { type: "DATE", val: "2014-01-02" }});

			// then
			expect(model.get("date").type).toBe("DATE");
			expect(model.get("date").val).toBe("2014-01-02");
		});

		it("should set value for inited attribute", function() {
			var model = fixtureModel("TEXT", "leatherpants");

			//when
			model.setVal("batman", "spandex");

			// then
			expect(model.get("batman")).not.toBeUndefined();
			expect(model.get("batman").type).toBe("TEXT");
			expect(model.get("batman").val).toBe("spandex");
		});

		describe("when validating simple data boundary", function() {
			it("should not validate form without a boundary", function() {
				var model = new FormModel({
					foo: {type: "OPTION", val: "false"},
					bar: {type: "TEXT", val: []},
				});

				expect(model.hasBoundary()).toBe(false);
			});

			it("should validate form with selected project", function() {
				var model = new FormModel({
					foo: {type: "PROJECT_PICKER", val: [ 10 ]},
				});

				expect(model.hasBoundary()).toBe(true);
			});

			it("should validate form with selected nodes", function() {
				var model = new FormModel({
					foo: {type: "TREE_PICKER", val: [ { resid: 10, restype: "folder" } ]},
				});

				expect(model.hasBoundary()).toBe(true);
			});

			it("should validate form with 'everything' item selected", function() {
				var model = new FormModel({
					foo: {type: "RADIO_BUTTONS_GROUP", val: "EVERYTHING"},
				});

				expect(model.hasBoundary()).toBe(true);
			});
		});

		describe("when validating boundary mode", function() {
			it("should not validate project mode when only nodes are picked", function() {
				var model = new FormModel({
					boundaryMode: {type: "RADIO_BUTTONS_GROUP", val: "PROJECT_PICKER"},
					treePicker: {type: "TREE_PICKER", val: [{ resid: 10, restype: "folder" }]},
					projectPicker: {type: "PROJECT_PICKER", val: []},
				});

				expect(model.hasBoundary()).toBe(false);
			});

			it("should not validate tree mode when only projects are picked", function() {
				var model = new FormModel({
					boundaryMode: {type: "RADIO_BUTTONS_GROUP", val: "TREE_PICKER"},
					treePicker: {type: "TREE_PICKER", val: []},
					projectPicker: {type: "PROJECT_PICKER", val: [10]},
				});

				expect(model.hasBoundary()).toBe(false);
			});

			it("should validate against project picker", function() {
				var model = new FormModel({
					boundaryMode: {type: "RADIO_BUTTONS_GROUP", val: "PROJECT_PICKER"},
					treePicker: {type: "TREE_PICKER", val: []},
					projectPicker: {type: "PROJECT_PICKER", val: [10]},
				});

				expect(model.hasBoundary()).toBe(true);
			});

			it("should validate against treepicker", function() {
				var model = new FormModel({
					boundaryMode: {type: "RADIO_BUTTONS_GROUP", val: "TREE_PICKER"},
					treePicker: {type: "TREE_PICKER", val: [{ resid: 10, restype: "folder" }]},
					projectPicker: {type: "PROJECT_PICKER", val: []},
				});

				expect(model.hasBoundary()).toBe(true);
			});

			it("should not validate against any picker", function() {
				var model = new FormModel({
					boundaryMode: {type: "RADIO_BUTTONS_GROUP", val: "EVERYTHING"},
					treePicker: {type: "TREE_PICKER", val: []},
					projectPicker: {type: "PROJECT_PICKER", val: []},
				});

				expect(model.hasBoundary()).toBe(true);
			});
		});
	});
});