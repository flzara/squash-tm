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
define(["backbone", "squash.translator"],
	function (Backbone, translator) {

		var translations = translator.get({
			nameLabel: "label.Name",
			typeLabel: "label.Type",
			descriptionLabel: "label.Description",
			generateLabel: "report.criteria.panel.button.generate.label"
		});

		function buildReport(viewID, jsonReport) {
			var target = $(viewID)[0];
			target.style.position = "relative";

			var table = document.createElement("div");
			table.className = "display-table";
			table.style.margin = "10px 5px 10px 5px";
			table.appendChild(createNewRow(translations.nameLabel, jsonReport.name.bold()));
			table.appendChild(createNewRow(translations.typeLabel, jsonReport.label));
			table.appendChild(createNewRow(translations.descriptionLabel, jsonReport.description));

			target.appendChild(createGenerateButton(jsonReport));
			target.appendChild(table);

			return target;
		}

		function createNewRow(label, value) {
			var rowElement = document.createElement("div");
			rowElement.className = "display-table-row";
			var labelElement = document.createElement("label");
			labelElement.className = "display-table-cell";
			labelElement.innerHTML = label;
			var valueElement = document.createElement("div");
			valueElement.className = "display-table-cell";
			valueElement.setAttribute("style", "padding-left: 0em;");
			valueElement.innerHTML = value;
			rowElement.appendChild(labelElement);
			rowElement.appendChild(valueElement);
			return rowElement;
		}

		function createGenerateButton(jsonReport) {
			var container = document.createElement("div");
			container.style.color = "blue";
			var inputButton = document.createElement("input");
			inputButton.type = "button";
			inputButton.className = "sq-btn";
			inputButton.value = translations.generateLabel;
			inputButton.setAttribute("style", "position: absolute; bottom : 20px;");

			inputButton.onclick = function () {
				var url;
				var namespace = jsonReport.pluginNamespace;
				var parameters = jsonReport.parameters;
				if (jsonReport.docx){

					url = buildViewUrl(0, "docx", namespace);
					$.ajax({
						type : "get",
						url : url,
						dataType : "html",
						data : { json : parameters }
					}).done(function(html) {
						$("#document-holder").html(html);
					});
				} else {

					var promises = [];
					var nbPages = jsonReport.pdfViews;
					var result = "";
					for (var i = 0; i < nbPages; i++) {
						url = buildViewUrl(i, "html", namespace);

						var request = $.ajax({
							type : "get",
							url : url,
							dataType : "html",
							data : { json : parameters }
						}).done(function(html) {
							result += html;
						});
						promises.push(request);

					}

					$.when.apply(null, promises).done(function(){
						var win = window.open("", "_blank", "scrollbars=yes,resizable=yes");
						win.document.body.innerHTML = result;
					});
				}
			};

			container.appendChild(inputButton);
			return container;
		}

		function buildViewUrl(index, format, reportNamespace) {
			return document.location.protocol + "//" + document.location.host + "/squash/reports/" + reportNamespace + "/views/" + index +
				"/formats/" + format;
		}

		return {
			buildReport: buildReport
		};
	});
