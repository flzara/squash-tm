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
			descriptionLabel: "label.Description"
		});

		function buildReport(viewID, jsonReport) {
			var vue = $(viewID)[0];

			var table = document.createElement("div");
			table.className = "display-table";
			table.style.margin = "10px 5px 10px 5px";
			table.appendChild(createNewRow(translations.nameLabel, jsonReport.name.bold()));
			table.appendChild(createNewRow(translations.typeLabel, jsonReport.label));
			table.appendChild(createNewRow(translations.descriptionLabel, jsonReport.description));

			vue.appendChild(table);

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

		return {
			buildReport: buildReport
		};
	});
