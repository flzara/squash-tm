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
/*
 *conf : 
 *  {
 *      projectId : the projectId,
 *      bindableEntity : the bindable entity type,
 *      getURL : the URL from where the available custom fields are fetched,
 *      selector : the selector for the popup,
 *      title : title of that popup,
 *      oklabel : localized label for 'ok',
 *      cancellabel : localized label for 'cancel',
 *      
 *  }
 * 
 * 
 */
define([ "require", "./models", "app/util/ButtonUtil", "jquery.squash", "jquery.squash.formdialog"], function(require, Model, ButtonUtil) {

	return function(settings) {

		// save the reference now, before the DOM is moved around
		var popup = $(settings.selector);
		
		popup.formDialog();
		
		popup.on('formdialogconfirm', submit);
		
		popup.on('formdialogcancel', function(){
			popup.formDialog('close');
		});

		popup.postSuccessListeners = [];

		// ************* private attributes ************************

		var lineTemplate = popup.find(".row-template-holder tr");

		var table = popup.find("table");

		// ************* private methods ***************************

		var rowHandleClick = function(evt) {
			var chkbx = $(this).find('td:first input');
			if (!chkbx.is(evt.target)) {
				chkbx.click();
			}
		};

		var rowHoverIn = function() {
			$(this).addClass('ui-state-highlight');
		};

		var rowHoverOut = function() {
			$(this).removeClass('ui-state-highlight');
		};

		var reset = function() {
			table.find("tbody").empty();
		};

		var populate = function(json) {
			var i = 0;
			var tbody = table.find("tbody.available-fields");
			var rows = $();

			if (json.length > 0) {
				for (i = 0; i < json.length; i++) {
					var data = json[i];
					var newLine = lineTemplate.clone(true);
					var rowCss = (i % 2) ? "odd" : "even";
					newLine.addClass(rowCss);
					var tds = newLine.find("td");

					tds.eq(0).prop("id", data.id)
							.data("type", data.inputType.enumName);
					tds.eq(1).text(data.name);
					tds.eq(2).text(data.inputType.friendlyName);
					tds.eq(3).text(data.friendlyOptional);

					rows = rows.add(newLine);
				}

				rows.click(rowHandleClick);
				rows.hover(rowHoverIn, rowHoverOut);
				tbody.append(rows);
			} else {
				rows = rows.add('<tr class="odd"><td colspan="4" class="centered">--</td></tr>');
				rows.hover(rowHoverIn, rowHoverOut);
				tbody.append(rows);
			}
		};

		var reload = function() {
			
			popup.formDialog('setState', 'pleasewait');
			
			$.ajax({
				type : 'GET',
				dataType : 'json',
				url : settings.getURL
			}).success(function(json) {
				reset();
				populate(json);
				popup.formDialog('setState', 'select');
			});

		};

		var makePayload = function() {
			return table.find("tbody.available-fields input:checked").parent("td").map(function() {
				return Model.newBinding(
						settings.projectId, 
						this.id, 
						settings.bindableEntity, 
						$(this).data('type'));
			}).get();

		};

		function submit(event) {
			popup.formDialog('setState', 'pleasewait');
			
			ButtonUtil.disable($(event.target));
			var payload = makePayload();
			if (payload.length === 0) {
				popup.formDialog("close");
				return;
			}
			$.ajax({
				url : settings.postURL,
				type : 'POST',
				data : JSON.stringify(payload),
				contentType : "application/json; charset=utf-8"
			}).done(function() {
				popup.formDialog("close");
				var i = 0;
				for (i = 0; i < popup.postSuccessListeners.length; i++) {
					popup.postSuccessListeners[i].update();
				}
			}).always(function(){
				ButtonUtil.enable($(event.target));
			});
		}

		// popup events

		popup.bind("formdialogopen", function() {
			reload();
		});

		popup.addPostSuccessListener = function(listener) {
			popup.postSuccessListeners.push(listener);
		};

		return popup;
	};

});