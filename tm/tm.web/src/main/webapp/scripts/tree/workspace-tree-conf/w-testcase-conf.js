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
define(["workspace.event-bus"], function(eventBus) {
	"use strict";
	return {
		generate : function() {
			return {
				
				"types" : {
					"max_depth" : -2, // unlimited without check
					"max_children" : -2, // unlimited w/o check
					"valid_children" : [ "drive" ],

					"types" : {
						"test-case" : {
							"valid_children" : 'none'
						},
						"folder" : {
							"valid_children" : [ "test-case", "folder" ]
						},
						"drive" : {
							"valid_children" : [ "test-case", "folder" ]
						}

					}

				},
				
				"dnd" : {
					drop_finish : function(data) {
						var node = data.o.treeNode();
						var calledid = [node.getResId()];
						var callerid = this.get_selected().treeNode().getResId();

						var param = {
							'called-test-case' : calledid
						};

						var url = squashtm.app.contextRoot + "test-cases/" + callerid
								+ "/called-test-cases";

						$.post(url, param).success(function() {
							eventBus.trigger('call-test-case', {
								callerId : callerid,
								calledId : calledid
							});
						});

					}

				}

				
			};
		}

		

	};
});
