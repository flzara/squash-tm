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
 * ripped and adapted from jqplot.CanvasGridRenderer.
 *
 * This grid renderer let your customize further the grid lines per axis, and adds the linedash feature (for OSes that support it).
 * This extra configuration is nested in the axes.[axis].tickOptions item :
 * 
 *  gridStyle : {
 *		lineDash : line dash configuration for the grid(see cavans API),
 *		strokeStyle : the css color spec for the grid
 *  },
 *  markStyle : {
 *		lineDash : line dash configuration for the tick mark (see cavans API),
 *		strokeStyle : the css color spec for the tick mark
 *  }
 * 
 * 
 */
define([ "jquery", "jqplot-core" ], function($) {

	$.jqplot.StylableGridRenderer = function() {
		$.jqplot.CanvasGridRenderer.call(this);
	};

	$.jqplot.StylableGridRenderer.prototype = new $.jqplot.CanvasGridRenderer();
	$.jqplot.StylableGridRenderer.prototype.constructor = $.jqplot.StylableGridRenderer;

	/*
	 * modified in several places
	 */
	$.jqplot.StylableGridRenderer.prototype.draw = function() {
		this._ctx = this._elem.get(0).getContext("2d");
		var ctx = this._ctx;
		var pos, t, axis, ticks, j, points;
		
		/*
		 * BEGIN modification
		 */
		if (!ctx.setLineDash) {
			ctx.setLineDash = function() {
			};
			ctx.getLineDash = function() {
				return [];
			};
		}
		/*
		 * END modification
		 */
		
		var axes = this._axes;
		// Add the grid onto the grid canvas. This is the bottom most layer.
		ctx.save();
		ctx.clearRect(0, 0, this._plotDimensions.width, this._plotDimensions.height);
		ctx.fillStyle = this.backgroundColor || this.background;
		ctx.fillRect(this._left, this._top, this._width, this._height);

		ctx.save();
		ctx.lineJoin = 'miter';
		ctx.lineCap = 'butt';
		ctx.lineWidth = this.gridLineWidth;
		ctx.strokeStyle = this.gridLineColor;
		var b, e, s, m;
		var ax = [ 'xaxis', 'yaxis', 'x2axis', 'y2axis' ];
		for ( var i = 4; i > 0; i--) {
			var name = ax[i - 1];
			axis = axes[name];
			ticks = axis._ticks;
			var numticks = ticks.length;
			if (axis.show) {
				if (axis.drawBaseline) {
					var bopts = {};
					if (axis.baselineWidth !== null) {
						bopts.lineWidth = axis.baselineWidth;
					}
					if (axis.baselineColor !== null) {
						bopts.strokeStyle = axis.baselineColor;
					}
					switch (name) {
					case 'xaxis':
						drawLine(this._left, this._bottom, this._right, this._bottom, bopts);
						break;
					case 'yaxis':
						drawLine(this._left, this._bottom, this._left, this._top, bopts);
						break;
					case 'x2axis':
						drawLine(this._left, this._bottom, this._right, this._bottom, bopts);
						break;
					case 'y2axis':
						drawLine(this._right, this._bottom, this._right, this._top, bopts);
						break;
					}
				}
				for ( j = numticks; j > 0; j--) {
					t = ticks[j - 1];
					if (t.show) {
						pos = Math.round(axis.u2p(t.value)) + 0.5;
						switch (name) {
						case 'xaxis':
							// draw the grid line if we should
							if (t.showGridline &&
									this.drawGridlines &&
									((!t.isMinorTick && axis.drawMajorGridlines) || (t.isMinorTick && axis.drawMinorGridlines))) {
								overrideCtx(ctx, t.gridStyle);
								drawLine(pos, this._top, pos, this._bottom);
								unoverrideCtx(ctx);
							}
							// draw the mark
							if (t.showMark &&
									t.mark &&
									((!t.isMinorTick && axis.drawMajorTickMarks) || (t.isMinorTick && axis.drawMinorTickMarks))) {
								s = t.markSize;
								m = t.mark;
								pos = Math.round(axis.u2p(t.value)) + 0.5;
								switch (m) {
								case 'outside':
									b = this._bottom;
									e = this._bottom + s;
									break;
								case 'inside':
									b = this._bottom - s;
									e = this._bottom;
									break;
								case 'cross':
									b = this._bottom - s;
									e = this._bottom + s;
									break;
								default:
									b = this._bottom;
									e = this._bottom + s;
									break;
								}
								// draw the shadow
								if (this.shadow) {
									this.renderer.shadowRenderer.draw(ctx, [ [ pos, b ], [ pos, e ] ], {
										lineCap : 'butt',
										lineWidth : this.gridLineWidth,
										offset : this.gridLineWidth * 0.75,
										depth : 2,
										fill : false,
										closePath : false
									});
								}
								// draw the line
								overrideCtx(ctx, t.markStyle);
								drawLine(pos, b, pos, e);
								unoverrideCtx(ctx);
							}
							break;

						case 'yaxis':

							// draw the grid line
							if (t.showGridline &&
									this.drawGridlines &&
									((!t.isMinorTick && axis.drawMajorGridlines) || (t.isMinorTick && axis.drawMinorGridlines))) {
								overrideCtx(ctx, t.gridStyle);
								drawLine(this._right, pos, this._left, pos);
								unoverrideCtx(ctx);
							}
							
							// draw the mark
							if (t.showMark &&
									t.mark &&
									((!t.isMinorTick && axis.drawMajorTickMarks) || (t.isMinorTick && axis.drawMinorTickMarks))) {
								s = t.markSize;
								m = t.mark;
								pos = Math.round(axis.u2p(t.value)) + 0.5;
								switch (m) {
								case 'outside':
									b = this._left - s;
									e = this._left;
									break;
								case 'inside':
									b = this._left;
									e = this._left + s;
									break;
								case 'cross':
									b = this._left - s;
									e = this._left + s;
									break;
								default:
									b = this._left - s;
									e = this._left;
									break;
								}
								// draw the shadow
								if (this.shadow) {
									this.renderer.shadowRenderer.draw(ctx, [ [ b, pos ], [ e, pos ] ], {
										lineCap : 'butt',
										lineWidth : this.gridLineWidth * 1.5,
										offset : this.gridLineWidth * 0.75,
										fill : false,
										closePath : false
									});
								}
								overrideCtx(ctx, t.markStyle);
								drawLine(b, pos, e, pos, {
									strokeStyle : axis.borderColor
								});
								unoverrideCtx(ctx);
								
							}
							break;

						case 'x2axis':

							// draw the grid line
							if (t.showGridline &&
									this.drawGridlines &&
									((!t.isMinorTick && axis.drawMajorGridlines) || (t.isMinorTick && axis.drawMinorGridlines))) {
								overrideCtx(ctx, t.gridStyle);
								drawLine(pos, this._bottom, pos, this._top);
								unoverrideCtx(ctx);
							}
							// draw the mark
							if (t.showMark &&
									t.mark &&
									((!t.isMinorTick && axis.drawMajorTickMarks) || (t.isMinorTick && axis.drawMinorTickMarks))) {
								s = t.markSize;
								m = t.mark;
								pos = Math.round(axis.u2p(t.value)) + 0.5;
								switch (m) {
								case 'outside':
									b = this._top - s;
									e = this._top;
									break;
								case 'inside':
									b = this._top;
									e = this._top + s;
									break;
								case 'cross':
									b = this._top - s;
									e = this._top + s;
									break;
								default:
									b = this._top - s;
									e = this._top;
									break;
								}
								// draw the shadow
								if (this.shadow) {
									this.renderer.shadowRenderer.draw(ctx, [ [ pos, b ], [ pos, e ] ], {
										lineCap : 'butt',
										lineWidth : this.gridLineWidth,
										offset : this.gridLineWidth * 0.75,
										depth : 2,
										fill : false,
										closePath : false
									});
								}
								overrideCtx(ctx, t.markStyle);
								drawLine(pos, b, pos, e);
								unoverrideCtx(ctx);
							}
							break;

						case 'y2axis':
							// draw the grid line
							if (t.showGridline &&
									this.drawGridlines &&
									((!t.isMinorTick && axis.drawMajorGridlines) || (t.isMinorTick && axis.drawMinorGridlines))) {
								overrideCtx(ctx, t.gridStyle);
								drawLine(this._left, pos, this._right, pos);
								unoverrideCtx(ctx);
							}
							// draw the mark
							if (t.showMark &&
									t.mark &&
									((!t.isMinorTick && axis.drawMajorTickMarks) || (t.isMinorTick && axis.drawMinorTickMarks))) {
								s = t.markSize;
								m = t.mark;
								pos = Math.round(axis.u2p(t.value)) + 0.5;
								switch (m) {
								case 'outside':
									b = this._right;
									e = this._right + s;
									break;
								case 'inside':
									b = this._right - s;
									e = this._right;
									break;
								case 'cross':
									b = this._right - s;
									e = this._right + s;
									break;
								default:
									b = this._right;
									e = this._right + s;
									break;
								}
								// draw the shadow
								if (this.shadow) {
									this.renderer.shadowRenderer.draw(ctx, [ [ b, pos ], [ e, pos ] ], {
										lineCap : 'butt',
										lineWidth : this.gridLineWidth * 1.5,
										offset : this.gridLineWidth * 0.75,
										fill : false,
										closePath : false
									});
								}
								overrideCtx(ctx, t.markStyle);
								drawLine(b, pos, e, pos, {
									strokeStyle : axis.borderColor
								});
								unoverrideCtx(ctx);
							}
							break;
						default:
							break;
						}
					}
				}
				t = null;
			}
			axis = null;
			ticks = null;
		}
		// Now draw grid lines for additional y axes
		// ////
		// TO DO: handle yMidAxis
		// ////
		ax = [ 'y3axis', 'y4axis', 'y5axis', 'y6axis', 'y7axis', 'y8axis', 'y9axis', 'yMidAxis' ];
		for ( i = 7; i > 0; i--) {
			axis = axes[ax[i - 1]];
			ticks = axis._ticks;
			if (axis.show) {
				var tn = ticks[axis.numberTicks - 1];
				var t0 = ticks[0];
				var left = axis.getLeft();
				points = [ [ left, tn.getTop() + tn.getHeight() / 2 ],
						[ left, t0.getTop() + t0.getHeight() / 2 + 1.0 ] ];
				// draw the shadow
				if (this.shadow) {
					this.renderer.shadowRenderer.draw(ctx, points, {
						lineCap : 'butt',
						fill : false,
						closePath : false
					});
				}
				// draw the line
				drawLine(points[0][0], points[0][1], points[1][0], points[1][1], {
					lineCap : 'butt',
					strokeStyle : axis.borderColor,
					lineWidth : axis.borderWidth
				});
				// draw the tick marks
				for ( j = ticks.length; j > 0; j--) {
					t = ticks[j - 1];
					s = t.markSize;
					m = t.mark;
					pos = Math.round(axis.u2p(t.value)) + 0.5;
					if (t.showMark && t.mark) {
						switch (m) {
						case 'outside':
							b = left;
							e = left + s;
							break;
						case 'inside':
							b = left - s;
							e = left;
							break;
						case 'cross':
							b = left - s;
							e = left + s;
							break;
						default:
							b = left;
							e = left + s;
							break;
						}
						points = [ [ b, pos ], [ e, pos ] ];
						// draw the shadow
						if (this.shadow) {
							this.renderer.shadowRenderer.draw(ctx, points, {
								lineCap : 'butt',
								lineWidth : this.gridLineWidth * 1.5,
								offset : this.gridLineWidth * 0.75,
								fill : false,
								closePath : false
							});
						}
						// draw the line
						drawLine(b, pos, e, pos, {
							strokeStyle : axis.borderColor
						});
					}
					t = null;
				}
				t0 = null;
			}
			axis = null;
			ticks = null;
		}

		ctx.restore();

		function drawLine(bx, by, ex, ey, opts) {
			ctx.save();
			opts = opts || {};
			if (opts.lineWidth == null || opts.lineWidth !== 0) {
				$.extend(true, ctx, opts);
				ctx.beginPath();
				ctx.moveTo(bx, by);
				ctx.lineTo(ex, ey);
				ctx.stroke();
				ctx.restore();
			}
		}

		if (this.shadow) {
			points = [ [ this._left, this._bottom ], [ this._right, this._bottom ],
					[ this._right, this._top ] ];
			this.renderer.shadowRenderer.draw(ctx, points);
		}
		// Now draw border around grid. Use axis border definitions. start at
		// upper left and go clockwise.
		if (this.borderWidth !== 0 && this.drawBorder) {
			drawLine(this._left, this._top, this._right, this._top, {
				lineCap : 'round',
				strokeStyle : axes.x2axis.borderColor,
				lineWidth : axes.x2axis.borderWidth
			});
			drawLine(this._right, this._top, this._right, this._bottom, {
				lineCap : 'round',
				strokeStyle : axes.y2axis.borderColor,
				lineWidth : axes.y2axis.borderWidth
			});
			drawLine(this._right, this._bottom, this._left, this._bottom, {
				lineCap : 'round',
				strokeStyle : axes.xaxis.borderColor,
				lineWidth : axes.xaxis.borderWidth
			});
			drawLine(this._left, this._bottom, this._left, this._top, {
				lineCap : 'round',
				strokeStyle : axes.yaxis.borderColor,
				lineWidth : axes.yaxis.borderWidth
			});
		}
		// ctx.lineWidth = this.borderWidth;
		// ctx.strokeStyle = this.borderColor;
		// ctx.strokeRect(this._left, this._top, this._width, this._height);

		ctx.restore();
		ctx = null;
		axes = null;
	};
	
	
	/*
	 * ADDITIONAL FUNCTIONS HERE
	 * 
	 */
	
	function overrideCtx(ctx, style){
		
		if (style === undefined){
			return;
		}
		
		var saveStyle = {
			strokeStyle : ctx.strokeStyle,
			lineDash : ctx.getLineDash()
		};
		
		if (!! style.lineDash){
			ctx.setLineDash(style.lineDash);
		}
		
		if (!! style.strokeStyle){
			ctx.strokeStyle = style.strokeStyle;
		}
		
		ctx._save = saveStyle;
	}
	
	function unoverrideCtx(ctx){
		
		var saveStyle=ctx._save;
		
		if (saveStyle === undefined){
			return;
		}
		
		ctx.setLineDash(saveStyle.lineDash);
		ctx.strokeStyle = saveStyle.strokeStyle;
		
		delete ctx._save;
	}

});
