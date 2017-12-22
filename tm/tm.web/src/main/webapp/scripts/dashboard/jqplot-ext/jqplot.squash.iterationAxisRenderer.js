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
 * ripped and adapted from jqplot.CategoryAxisRenderer
 * 
 */

define([ "jquery", "jqplot-category" ], function($) {

	$.jqplot.IterationAxisRenderer = function(options) {
		$.jqplot.CategoryAxisRenderer.call(this);
	};

	$.jqplot.IterationAxisRenderer.prototype = new $.jqplot.CategoryAxisRenderer();
	$.jqplot.IterationAxisRenderer.prototype.constructor = $.jqplot.IterationAxisRenderer;

	/*
	 * The whole function is modified
	 * 
	 */
	$.jqplot.IterationAxisRenderer.prototype.createTicks = function() {
		// we're are operating on an axis here
		var ticks = this._ticks;
		var userTicks = this.ticks;
		var name = this.name;

		var min, max;
		var start, end;
		var tt, t, i, elt;

		if (userTicks.length) {
			min = userTicks[0];
			max = userTicks[userTicks.length - 1];

			this.min = ($.isArray(min)) ? min[0] : min;
			this.max = ($.isArray(max)) ? max[1] : max;

			for (i = 0; i < userTicks.length; i++) {
				elt = userTicks[i];

				if ($.isArray(elt)) {
					start = elt[0];
					end = elt[1];
					label = elt[2];

					// scheduled start. This one carries the label.
					t = new this.tickRenderer(this.tickOptions);
					t.label = label;
					t.setTick(start, this.name);
					this._ticks.push(t);

					// scheduled end.
					t = new this.tickRenderer(this.tickOptions);
					t.showLabel = false;
					t.setTick(end, this.name);
					this._ticks.push(t);
				}
				// case : it's a single, random value indicating boundaries
				else {
					tt = elt;
					t = new this.tickRenderer(this.tickOptions);
					t.showLabel = false;
					t.showMark = false;
					t.showGridline = false;
					t.setTick(tt, this.name);
					this._ticks.push(t);
				}
			}
		} else {
			throw "user must provide ticks !";
		}

	};

	/*
	 * Partially modified, can you find where ?
	 */
	$.jqplot.IterationAxisRenderer.prototype.pack = function(pos, offsets) {
		var ticks = this._ticks;
		var max = this.max;
		var min = this.min;
		var offmax = offsets.max;
		var offmin = offsets.min;
		var lshow = (this._label == null) ? false : this._label.show;
		var i, t, p;

		for ( p in pos) {
			this._elem.css(p, pos[p]);
		}

		this._offsets = offsets;

		var pixellength = offmax - offmin;
		var unitlength = max - min;

		/*
		 * The other branch was removed because never used here
		 */
		if (!this.reverse) {

			this.u2p = function(u) {
				return (u - min) * pixellength / unitlength + offmin;
			};

			this.p2u = function(p) {
				return (p - offmin) * unitlength / pixellength + min;
			};

			if (this.name == 'xaxis' || this.name == 'x2axis') {
				this.series_u2p = function(u) {
					return (u - min) * pixellength / unitlength;
				};
				this.series_p2u = function(p) {
					return p * unitlength / pixellength + min;
				};
			}

			else {
				this.series_u2p = function(u) {
					return (u - max) * pixellength / unitlength;
				};
				this.series_p2u = function(p) {
					return p * unitlength / pixellength + max;
				};
			}
		}

		if (this.show) {
			if (this.name == 'xaxis' || this.name == 'x2axis') {
				for (i = 0; i < ticks.length; i++) {
					t = ticks[i];
					/*
					 * BEGIN modification
					 */
					if (t.show && t.showLabel) {

						var leftOffset = this.u2p(t.value) + 'px';
						t._elem.css('left', leftOffset);

						var tplus1 = ticks[i + 1];
						var width = ((tplus1.value - t.value) * pixellength / unitlength) + 'px';
						t._elem.css('width', width);

						t._elem.css('text-align', 'center');
						t.pack();
					}
					/*
					 * END modification
					 */
				}

				var labeledge = [ 'bottom', 0 ];
				if (lshow) {
					var w = this._label._elem.outerWidth(true);
					this._label._elem.css('left', offmin + pixellength / 2 - w / 2 + 'px');
					if (this.name == 'xaxis') {
						this._label._elem.css('bottom', '0px');
						labeledge = [ 'bottom', this._label._elem.outerHeight(true) ];
					} else {
						this._label._elem.css('top', '0px');
						labeledge = [ 'top', this._label._elem.outerHeight(true) ];
					}
					this._label.pack();
				}

				// draw the group labels
				var step = parseInt(this._ticks.length / this.groups, 10) + 1;
				for (i = 0; i < this._groupLabels.length; i++) {
					var mid = 0;
					var count = 0;
					for ( var j = i * step; j < (i + 1) * step; j++) {
						if (j >= this._ticks.length - 1){
							continue; // the last tick does not exist as there is no other
						}
																	// group in order to have an empty one.
						if (this._ticks[j]._elem && this._ticks[j].label != " ") {
							t = this._ticks[j]._elem;
							p = t.position();
							mid += p.left + t.outerWidth(true) / 2;
							count++;
						}
					}
					mid = mid / count;
					this._groupLabels[i].css({
						'left' : (mid - this._groupLabels[i].outerWidth(true) / 2)
					});
					this._groupLabels[i].css(labeledge[0], labeledge[1]);
				}
			} else {
				throw "Y axises are unsupported";
			}
		}
	};

});
