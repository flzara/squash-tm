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
define(["workspace.event-bus"], function(eventBus){
	"use strict";
	return {
		generate : function(){

			return {
				"types" : {
					"max_depth" : -2, // unlimited without check
					"max_children" : -2, // unlimited w/o check
					"valid_children" : [ "drive" ],
					"types" : {
						"requirement" : {
							"valid_children" : ['requirement']
						},
						"folder" : {
							"valid_children" : [ "requirement", "folder" ]
						},
						"drive" : {
							"valid_children" : [ "requirement", "folder" ]
						}
					}
				},
				"dnd" : {
        					drop_finish : function(dropData) {
										var selection = dropData.o.not(':library, :folder');
										var calledids = [];
										if(selection.length > 0) {
        							var node = dropData.o.treeNode();
        							calledids = [node.getResId()];
										}
        						var callerid = this.get_selected().treeNode().getResId();

										var data = {
											reqNodeId: callerid,
											relatedReqNodeIds: calledids
										};
        						eventBus.trigger('link-req-node-from-tree', data);
        					}

        				}
			};
		}

	};
});
