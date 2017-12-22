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
define(['jquery', 'squash.translator', 'datepicker/jquery.squash.datepicker-locales',
		'jeditable.datepicker'], function($, translator, regionale){
	
	return {
		init : function(){
			var localemeta = {
					format : 'squashtm.dateformatShort.datepicker',
					locale : 'squashtm.locale'
				};

			var message = translator.get(localemeta);

			var language = regionale[message.locale] || regionale;

			$.datepicker._defaults.onAfterUpdate = null;
			$.datepicker.setDefaults(language);

			var datepicker__updateDatepicker = $.datepicker._updateDatepicker;

			$.datepicker._updateDatepicker = function(inst) {

				datepicker__updateDatepicker.call(this, inst);

				var onAfterUpdate = this._get(inst, 'onAfterUpdate');
				if (onAfterUpdate) {
					onAfterUpdate.apply((inst.input ? inst.input[0] : null), [
							(inst.input ? inst.input.val() : ''), inst ]);
				}
			};

			var cur = -1, prv = -1;
			$('.rangedatepicker-div')
					.datepicker(
							{
								dateFormat : message.format,
								changeMonth : true,
								changeYear : true,
								showButtonPanel : true,

								beforeShowDay : function(date) {
									return [
											true,
											((date.getTime() >= Math.min(prv, cur) && date
													.getTime() <= Math.max(prv, cur)) ? 'date-range-selected'
													: '') ];
								},

								onSelect : function(dateText, inst) {
									var d1, d2;

									prv = cur;
									cur = (new Date(inst.selectedYear, inst.selectedMonth,
											inst.selectedDay)).getTime();
									if (prv == -1) {
										prv = cur;
										d1 = $.datepicker.formatDate(message.format,
												new Date(cur), {});
										df1 = $.datepicker.formatDate('dd/mm/yy', new Date(
												cur), {});
										$('.rangedatepicker-input', this.parentNode).val(d1);
										$('.rangedatepicker-hidden-input', this.parentNode).val(df1);
										$('.rangedatepicker-hidden-input', this.parentNode).change();
									} else if (prv == cur) {
										d1 = $.datepicker.formatDate(message.format,
												new Date(cur), {});
										df1 = $.datepicker.formatDate('dd/mm/yy', new Date(
												cur), {});
										$('.rangedatepicker-input', this.parentNode).val(d1);
										$('.rangedatepicker-hidden-input', this.parentNode).val(df1);
										$('.rangedatepicker-hidden-input', this.parentNode).change();
									} else {
										d1 = $.datepicker.formatDate(message.format,
												new Date(Math.min(prv, cur)), {});
										d2 = $.datepicker.formatDate(message.format,
												new Date(Math.max(prv, cur)), {});
										df1 = $.datepicker.formatDate('dd/mm/yy', new Date(
												Math.min(prv, cur)), {});
										df2 = $.datepicker.formatDate('dd/mm/yy', new Date(
												Math.max(prv, cur)), {});
										$('.rangedatepicker-input', this.parentNode).val(d1 + ' - ' + d2);
										$('.rangedatepicker-hidden-input', this.parentNode)
												.val(df1 + ' - ' + df2);
										$('.rangedatepicker-hidden-input', this.parentNode).change();
									}
								},

								onChangeMonthYear : function(year, month, inst) {
									// prv = cur = -1;
								},

								onAfterUpdate : function(inst) {

									$(".rangedatepicker div td").hover(function(event) {
										event.stopPropagation();
									});
									
									if (!$("#okbutton",
											".rangedatepicker div .ui-datepicker-buttonpane").length) {
										var self = this;
										$(
												'<button type="button" id="okbutton" class="ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all" data-handler="hide" data-event="click">Ok</button>')
												.appendTo(
														$('.ui-datepicker-buttonpane', this))
												.on('click', function() {
													$(self).hide();
												});

										$(
												'<button type="button" id="resetbutton" class="ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all" data-handler="hide" data-event="click">'+translator.get("label.Reset")+'</button>')
												.appendTo(
														$('.ui-datepicker-buttonpane', this))
												.on('click', function() {
													$('.rangedatepicker-input', self.parentNode).val("");
													$('.rangedatepicker-hidden-input', self.parentNode).val("");
													$('.rangedatepicker-hidden-input', self.parentNode).change();
												});

									}
								}
							}).hide();
			
			$('.rangedatepicker-input').on('focus', function(e) {
				var v = this.value, d;

				try {
					if (v.indexOf(' - ') > -1) {
						d = v.split(' - ');

						prv = $.datepicker.parseDate(message.format, d[0]).getTime();
						cur = $.datepicker.parseDate(message.format, d[1]).getTime();

					} else if (v.length > 0) {
						prv = cur = $.datepicker.parseDate(message.format, v).getTime();
					}
				} catch (exception) {
					cur = prv = -1;
				}

				if (cur > -1) {
					$('.rangedatepicker-div', this.parentNode).datepicker('setDate', new Date(cur));
				}

				$('.rangedatepicker-div', this.parentNode).datepicker('refresh').show();
			});
		}
	};
	
});



