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
 * jsTree DND plugin 1.0
 * Drag and drop plugin for moving/copying nodes
 */
 // Redifined for custom report workspace...just for the event listened during DnD but this event is binded to multiple callback
 // so it was not possible to extract that properly
define([],function () {
  return function () {

		 (function ($) {
			var o = false,
				r = false,
				m = false,
				sli = false,
				sti = false,
				dir1 = false,
				dir2 = false;
			$.vakata.dndCustomReport = {
				is_down : false,
				is_drag : false,
				helper : false,
				scroll_spd : 10,
				init_x : 0,
				init_y : 0,
				threshold : 5,
				user_data : {},

				drag_start : function (e, data, html) {
					if($.vakata.dndCustomReport.is_drag) { $.vakata.drag_stop({}); }
					try {
						e.currentTarget.unselectable = "on";
						e.currentTarget.onselectstart = function() { return false; };
						if(e.currentTarget.style) { e.currentTarget.style.MozUserSelect = "none"; }
					} catch(err) { }
					$.vakata.dndCustomReport.init_x = e.pageX;
					$.vakata.dndCustomReport.init_y = e.pageY;
					$.vakata.dndCustomReport.user_data = data;
					$.vakata.dndCustomReport.is_down = true;
					$.vakata.dndCustomReport.helper = $("<div id='vakata-dragged'>").html(html).css("opacity", "0.75");
					$(document).bind("mousemove", $.vakata.dndCustomReport.drag);
					$(document).bind("mouseup", $.vakata.dndCustomReport.drag_stop);
					return false;
				},
				drag : function (e) {
					if(!$.vakata.dndCustomReport.is_down) { return; }
					if(!$.vakata.dndCustomReport.is_drag) {
						if(Math.abs(e.pageX - $.vakata.dndCustomReport.init_x) > 5 || Math.abs(e.pageY - $.vakata.dndCustomReport.init_y) > 5) {
							$.vakata.dndCustomReport.helper.appendTo("body");
							$.vakata.dndCustomReport.is_drag = true;
							$(document).triggerHandler("drag_start.vakata", { "event" : e, "data" : $.vakata.dndCustomReport.user_data });
						}
						else { return; }
					}

					// maybe use a scrolling parent element instead of document?
					if(e.type === "mousemove") { // thought of adding scroll in order to move the helper, but mouse poisition is n/a
						var d = $(document), t = d.scrollTop(), l = d.scrollLeft();
						if(e.pageY - t < 20) {
							if(sti && dir1 === "down") { clearInterval(sti); sti = false; }
							if(!sti) { dir1 = "up"; sti = setInterval(function () { $(document).scrollTop($(document).scrollTop() - $.vakata.dndCustomReport.scroll_spd); }, 150); }
						}
						else {
							if(sti && dir1 === "up") { clearInterval(sti); sti = false; }
						}
						if($(window).height() - (e.pageY - t) < 20) {
							if(sti && dir1 === "up") { clearInterval(sti); sti = false; }
							if(!sti) { dir1 = "down"; sti = setInterval(function () { $(document).scrollTop($(document).scrollTop() + $.vakata.dndCustomReport.scroll_spd); }, 150); }
						}
						else {
							if(sti && dir1 === "down") { clearInterval(sti); sti = false; }
						}

						if(e.pageX - l < 20) {
							if(sli && dir2 === "right") { clearInterval(sli); sli = false; }
							if(!sli) { dir2 = "left"; sli = setInterval(function () { $(document).scrollLeft($(document).scrollLeft() - $.vakata.dndCustomReport.scroll_spd); }, 150); }
						}
						else {
							if(sli && dir2 === "left") { clearInterval(sli); sli = false; }
						}
						if($(window).width() - (e.pageX - l) < 20) {
							if(sli && dir2 === "left") { clearInterval(sli); sli = false; }
							if(!sli) { dir2 = "right"; sli = setInterval(function () { $(document).scrollLeft($(document).scrollLeft() + $.vakata.dndCustomReport.scroll_spd); }, 150); }
						}
						else {
							if(sli && dir2 === "right") { clearInterval(sli); sli = false; }
						}
					}

					$.vakata.dndCustomReport.helper.css({ left : (e.pageX + 5) + "px", top : (e.pageY + 10) + "px" });
					$(document).triggerHandler("drag.vakata", { "event" : e, "data" : $.vakata.dndCustomReport.user_data });
				},
				drag_stop : function (e) {
					$(document).unbind("mousemove", $.vakata.dndCustomReport.drag);
					$(document).unbind("mouseup", $.vakata.dndCustomReport.drag_stop);
					$(document).triggerHandler("drag_stop.vakata", { "event" : e, "data" : $.vakata.dndCustomReport.user_data });
					$.vakata.dndCustomReport.helper.remove();
					$.vakata.dndCustomReport.init_x = 0;
					$.vakata.dndCustomReport.init_y = 0;
					$.vakata.dndCustomReport.user_data = {};
					$.vakata.dndCustomReport.is_down = false;
					$.vakata.dndCustomReport.is_drag = false;
				}
			};
			$(function() {
				var css_string = '#vakata-dragged { display:block; margin:0 0 0 0; padding:4px 4px 4px 24px; position:absolute; top:-2000px; line-height:16px; z-index:10000; } ';
				$.vakata.css.add_sheet({ str : css_string });
			});

			$.jstree.plugin("dndCustomReport", {
				__init : function () {
					this.data.dndCustomReport = {
						active : false,
						after : false,
						inside : false,
						before : false,
						off : false,
						prepared : false,
						w : 0,
						to1 : false,
						to2 : false,
						cof : false,
						cw : false,
						ch : false,
						i1 : false,
						i2 : false
					};
					this.get_container()
						.bind("mouseenter.jstree", $.proxy(function () {
								if($.vakata.dndCustomReport.is_drag && $.vakata.dndCustomReport.user_data.jstree && this.data.themes) {
									m.attr("class", "jstree-" + this.data.themes.theme);
									$.vakata.dndCustomReport.helper.attr("class", "jstree-dnd-helper jstree-" + this.data.themes.theme);
								}
							}, this))
						.bind("mouseleave.jstree", $.proxy(function () {
								if($.vakata.dndCustomReport.is_drag && $.vakata.dndCustomReport.user_data.jstree) {
									if(this.data.dndCustomReport.i1) { clearInterval(this.data.dndCustomReport.i1); }
									if(this.data.dndCustomReport.i2) { clearInterval(this.data.dndCustomReport.i2); }
								}
							}, this))
						.bind("mousemove.jstree", $.proxy(function (e) {
								if($.vakata.dndCustomReport.is_drag && $.vakata.dndCustomReport.user_data.jstree) {
									var cnt = this.get_container()[0];

									// Horizontal scroll
									if(e.pageX + 24 > this.data.dndCustomReport.cof.left + this.data.dndCustomReport.cw) {
										if(this.data.dndCustomReport.i1) { clearInterval(this.data.dndCustomReport.i1); }
										this.data.dndCustomReport.i1 = setInterval($.proxy(function () { this.scrollLeft += $.vakata.dndCustomReport.scroll_spd; }, cnt), 100);
									}
									else if(e.pageX - 24 < this.data.dndCustomReport.cof.left) {
										if(this.data.dndCustomReport.i1) { clearInterval(this.data.dndCustomReport.i1); }
										this.data.dndCustomReport.i1 = setInterval($.proxy(function () { this.scrollLeft -= $.vakata.dndCustomReport.scroll_spd; }, cnt), 100);
									}
									else {
										if(this.data.dndCustomReport.i1) { clearInterval(this.data.dndCustomReport.i1); }
									}

									// Vertical scroll
									if(e.pageY + 24 > this.data.dndCustomReport.cof.top + this.data.dndCustomReport.ch) {
										if(this.data.dndCustomReport.i2) { clearInterval(this.data.dndCustomReport.i2); }
										this.data.dndCustomReport.i2 = setInterval($.proxy(function () { this.scrollTop += $.vakata.dndCustomReport.scroll_spd; }, cnt), 100);
									}
									else if(e.pageY - 24 < this.data.dndCustomReport.cof.top) {
										if(this.data.dndCustomReport.i2) { clearInterval(this.data.dndCustomReport.i2); }
										this.data.dndCustomReport.i2 = setInterval($.proxy(function () { this.scrollTop -= $.vakata.dndCustomReport.scroll_spd; }, cnt), 100);
									}
									else {
										if(this.data.dndCustomReport.i2) { clearInterval(this.data.dndCustomReport.i2); }
									}

								}
							}, this))
						.bind("mouseup.jstree", $.proxy(function (e) {
							//stop "crazy shaking tree scroll"
		                    if($.vakata.dndCustomReport.is_drag && $.vakata.dndCustomReport.user_data.jstree) {
		                        if(this.data.dndCustomReport.i1) { clearInterval(this.data.dndCustomReport.i1); }
		                        if(this.data.dndCustomReport.i2) { clearInterval(this.data.dndCustomReport.i2); }
		                    }
		                }, this))
						.delegate("a", "mousedown.jstree", $.proxy(function (e) {
								if(e.which === 1) {
									this.start_drag(e.currentTarget, e);
									return false;
								}
							}, this))
						.delegate("a", "mouseenter.jstree", $.proxy(function (e) {
								if($.vakata.dndCustomReport.is_drag && $.vakata.dndCustomReport.user_data.jstree) {
									this.dnd_enter(e.currentTarget);
								}
							}, this))
						.delegate("a", "mousemove.jstree", $.proxy(function (e) {
								if($.vakata.dndCustomReport.is_drag && $.vakata.dndCustomReport.user_data.jstree) {
									if(typeof this.data.dndCustomReport.off.top === "undefined") { this.data.dndCustomReport.off = $(e.target).offset(); }
									this.data.dndCustomReport.w = (e.pageY - (this.data.dndCustomReport.off.top || 0)) % this.data.core.li_height;
									if(this.data.dndCustomReport.w < 0) { this.data.dndCustomReport.w += this.data.core.li_height; }
									this.dnd_show();
								}
							}, this))
						.delegate("a", "mouseleave.jstree", $.proxy(function (e) {
								if($.vakata.dndCustomReport.is_drag && $.vakata.dndCustomReport.user_data.jstree) {
									this.data.dndCustomReport.after		= false;
									this.data.dndCustomReport.before	= false;
									this.data.dndCustomReport.inside	= false;
									$.vakata.dndCustomReport.helper.children("ins").attr("class","jstree-invalid");
									m.hide();
									if(r && r[0] === e.target.parentNode) {
										if(this.data.dndCustomReport.to1) {
											clearTimeout(this.data.dndCustomReport.to1);
											this.data.dndCustomReport.to1 = false;
										}
										if(this.data.dndCustomReport.to2) {
											clearTimeout(this.data.dndCustomReport.to2);
											this.data.dndCustomReport.to2 = false;
										}
									}
								}
							}, this))
						.delegate("a", "mouseup.jstree", $.proxy(function (e) {
								if($.vakata.dndCustomReport.is_drag && $.vakata.dndCustomReport.user_data.jstree) {
									this.dnd_finish(e);
								}
							}, this));

					$(document)
						.bind("drag_stop.vakata", $.proxy(function () {
								this.data.dndCustomReport.after		= false;
								this.data.dndCustomReport.before	= false;
								this.data.dndCustomReport.inside	= false;
								this.data.dndCustomReport.off		= false;
								this.data.dndCustomReport.prepared	= false;
								this.data.dndCustomReport.w			= false;
								this.data.dndCustomReport.to1		= false;
								this.data.dndCustomReport.to2		= false;
								this.data.dndCustomReport.active	= false;
								this.data.dndCustomReport.foreign	= false;
								if(m) { m.css({ "top" : "-2000px" }); }
							}, this))
						.bind("drag_start.vakata", $.proxy(function (e, data) {
								if(data.data.jstree) {
									var et = $(data.event.target);
									if(et.closest(".jstree").hasClass("jstree-" + this.get_index())) {
										this.dnd_enter(et);
									}
								}
							}, this));

					var s = this._get_settings().dndCustomReport;

					if(s.drag_target) {
						$(document)
							.delegate(s.drag_target, "mousedown.jstree", $.proxy(function (e) {
								o = e.target;
								$.vakata.dndCustomReport.drag_start(e, { jstree : true, obj : e.target }, "<ins class='jstree-icon'></ins>" + $(e.target).text() );
								if(this.data.themes) {
									m.attr("class", "jstree-" + this.data.themes.theme);
									$.vakata.dndCustomReport.helper.attr("class", "jstree-dnd-helper jstree-" + this.data.themes.theme);
								}
								$.vakata.dndCustomReport.helper.children("ins").attr("class","jstree-invalid");
								var cnt = this.get_container();
								this.data.dndCustomReport.cof = cnt.offset();
								this.data.dndCustomReport.cw = parseInt(cnt.width(),10);
								this.data.dndCustomReport.ch = parseInt(cnt.height(),10);
								this.data.dndCustomReport.foreign = true;
								return false;
							}, this));
					}
					if(s.drop_target) {
						$(document)
							.delegate(s.drop_target, "mouseover.jstree", $.proxy(function (e) {
									if(this.data.dndCustomReport.active && this._get_settings().dndCustomReport.drop_check.call(this, { "o" : o, "r" : $(e.target) })) {
										$.vakata.dndCustomReport.helper.children("ins").attr("class","jstree-ok");
									}
								}, this))
							.delegate(s.drop_target, "mouseleave.jstree", $.proxy(function (e) {
									if(this.data.dndCustomReport.active) {
										$.vakata.dndCustomReport.helper.children("ins").attr("class","jstree-invalid");
									}
								}, this))
							.delegate(s.drop_target, "mouseup.jstree", $.proxy(function (e) {
									if(this.data.dndCustomReport.active && $.vakata.dndCustomReport.helper.children("ins").hasClass("jstree-ok")) {
										this._get_settings().dndCustomReport.drop_finish.call(this, { "o" : o, "r" : $(e.target), "e" : e });
									}
								}, this));
					}
				},
				defaults : {
					copy_modifier	: "ctrl",
					check_timeout	: 200,
					open_timeout	: 500,
					drop_target		: ".jstree-drop",
					drop_check		: function (data) {
						var type = data.o.treeNode().getResType();
						if(type==='custom-report-chart'){
							return true;
						}
						return false;
					},
					drop_finish		: function (data) {
           var wreqr = squashtm.app.wreqr;
           wreqr.trigger("dropFromTree",data);
					},
					drag_target		: ".jstree-draggable",
					drag_finish		: $.noop,
					drag_check		: function (data) { return { after : false, before : false, inside : true }; }
				},
				_fn : {
					dnd_prepare : function () {
						if(!r || !r.length) { return; }
						this.data.dndCustomReport.off = r.offset();
						if(this._get_settings().core.rtl) {
							this.data.dndCustomReport.off.right = this.data.dndCustomReport.off.left + r.width();
						}
						if(this.data.dndCustomReport.foreign) {
							var a = this._get_settings().dndCustomReport.drag_check.call(this, { "o" : o, "r" : r });
							this.data.dndCustomReport.after = a.after;
							this.data.dndCustomReport.before = a.before;
							this.data.dndCustomReport.inside = a.inside;
							this.data.dndCustomReport.prepared = true;
							return this.dnd_show();
						}
						this.prepare_move(o, r, "before");
						this.data.dndCustomReport.before = this.check_move();
						this.prepare_move(o, r, "after");
						this.data.dndCustomReport.after = this.check_move();
						if(this._is_loaded(r)) {
							this.prepare_move(o, r, "inside");
							this.data.dndCustomReport.inside = this.check_move();
						}
						else {
							this.data.dndCustomReport.inside = false;
						}
						this.data.dndCustomReport.prepared = true;
						return this.dnd_show();
					},
					dnd_show : function () {
						if(!this.data.dndCustomReport.prepared) { return; }
						var o = ["before","inside","after"],
							r = false,
							rtl = this._get_settings().core.rtl,
							pos;
						if(this.data.dndCustomReport.w < this.data.core.li_height/3) { o = ["before","inside","after"]; }
						else if(this.data.dndCustomReport.w <= this.data.core.li_height*2/3) {
							o = this.data.dndCustomReport.w < this.data.core.li_height/2 ? ["inside","before","after"] : ["inside","after","before"];
						}
						else { o = ["after","inside","before"]; }
						$.each(o, $.proxy(function (i, val) {
							if(this.data.dndCustomReport[val]) {
								$.vakata.dndCustomReport.helper.children("ins").attr("class","jstree-ok");
								r = val;
								return false;
							}
						}, this));
						if(r === false) { $.vakata.dndCustomReport.helper.children("ins").attr("class","jstree-invalid"); }

						pos = rtl ? (this.data.dndCustomReport.off.right - 18) : (this.data.dndCustomReport.off.left + 10);
						switch(r) {
							case "before":
								m.css({ "left" : pos + "px", "top" : (this.data.dndCustomReport.off.top - 6) + "px" }).show();
								break;
							case "after":
								m.css({ "left" : pos + "px", "top" : (this.data.dndCustomReport.off.top + this.data.core.li_height - 7) + "px" }).show();
								break;
							case "inside":
								m.css({ "left" : pos + ( rtl ? -4 : 4) + "px", "top" : (this.data.dndCustomReport.off.top + this.data.core.li_height/2 - 5) + "px" }).show();
								break;
							default:
								m.hide();
								break;
						}
						return r;
					},
					dnd_open : function () {
						this.data.dndCustomReport.to2 = false;
						this.open_node(r, $.proxy(this.dnd_prepare,this), true);
					},
					dnd_finish : function (e) {
						if(this.data.dndCustomReport.foreign) {
							if(this.data.dndCustomReport.after || this.data.dndCustomReport.before || this.data.dndCustomReport.inside) {
								this._get_settings().dndCustomReport.drag_finish.call(this, { "o" : o, "r" : r });
							}
						}
						else {
							this.dnd_prepare();
							this.move_node(o, r, this.dnd_show(), e[this._get_settings().dndCustomReport.copy_modifier + "Key"]);
						}
						o = false;
						r = false;
						m.hide();
					},
					dnd_enter : function (obj) {
						var s = this._get_settings().dndCustomReport;
						this.data.dndCustomReport.prepared = false;
						r = this._get_node(obj);
						if(s.check_timeout) {
							// do the calculations after a minimal timeout (users tend to drag quickly to the desired location)
							if(this.data.dndCustomReport.to1) { clearTimeout(this.data.dndCustomReport.to1); }
							this.data.dndCustomReport.to1 = setTimeout($.proxy(this.dnd_prepare, this), s.check_timeout);
						}
						else {
							this.dnd_prepare();
						}
						if(s.open_timeout) {
							if(this.data.dndCustomReport.to2) { clearTimeout(this.data.dndCustomReport.to2); }
							if(r && r.length && r.hasClass("jstree-closed")) {
								// if the node is closed - open it, then recalculate
								this.data.dndCustomReport.to2 = setTimeout($.proxy(this.dnd_open, this), s.open_timeout);
							}
						}
						else {
							if(r && r.length && r.hasClass("jstree-closed")) {
								this.dnd_open();
							}
						}
					},
					start_drag : function (obj, e) {
						o = this._get_node(obj);
						if(this.data.ui && this.is_selected(o)) { o = this._get_node(null, true); }
						$.vakata.dndCustomReport.drag_start(e, { jstree : true, obj : o }, "<ins class='jstree-icon'></ins>" + (o.length > 1 ? "Multiple selection" : this.get_text(o)) );
						if(this.data.themes) {
							m.attr("class", "jstree-" + this.data.themes.theme);
							$.vakata.dndCustomReport.helper.attr("class", "jstree-dnd-helper jstree-" + this.data.themes.theme);
						}
						var cnt = this.get_container();
						this.data.dndCustomReport.cof = cnt.children("ul").offset();
						this.data.dndCustomReport.cw = parseInt(cnt.width(),10);
						this.data.dndCustomReport.ch = parseInt(cnt.height(),10);
						this.data.dndCustomReport.active = true;
					}
				}
			});
			$(function() {
				var css_string = '' +
					'#vakata-dragged ins { display:block; text-decoration:none; width:16px; height:16px; margin:0 0 0 0; padding:0; position:absolute; top:6px; left:6px; } ' +
					'#vakata-dragged .jstree-ok { background:green; } ' +
					'#vakata-dragged .jstree-invalid { background:red; } ' +
					'#jstree-marker { padding:0; margin:0; line-height:12px; font-size:1px; overflow:hidden; height:12px; width:14px; position:absolute; top:-30px; z-index:10000; background-repeat:no-repeat; display:none; background-color:silver; } ';
				$.vakata.css.add_sheet({ str : css_string });
				m = $("<div>").attr({ id : "jstree-marker" }).hide().appendTo("body");
				$(document).bind("drag_start.vakata", function (e, data) {
					if(data.data.jstree) {
						m.show();
					}
				});
				$(document).bind("drag_stop.vakata", function (e, data) {
					if(data.data.jstree) { m.hide(); }
				});
			});
		})(jQuery);
		//*/
		//For V1.13 we don't want copy/past or drag/drop inside tree. Cannot do it in workspace conf because we override some core function of jstree
    //with custom plugin so the conf will be overriden
    //It's safer to put deactivation here because of it's temporary nature and remove the line when copy/pasta will be done in custom-report-workspace
    // $.jstree._fn.check_move = function(){
    //   return false;
    // };
 };
});
//*/
