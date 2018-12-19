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
 * That object makes things according to the state of sorting of a datatable.
 */

/*
 * configuration : add the following data attr to a test plan table
 * <pre>
 *   <table class="test-plan-table" data-entity-id="..." data-entity-type="...">
 * </pre>
 *
 */
define([ "jquery", "workspace.storage"],
	function($, storage) {
	"use strict";

	var tableSelector = ".automation-table";

	function SortMode(conf) {

		var $table = $(tableSelector);
        this.storage = storage;
		this.key = "automation-sort-" + conf.customKey;
        var sorting = conf.aaSorting;
        this.update = function(_sort) {
            var _sorting = _sort || $table.squashTable().fnSettings().aaSorting;
            this._saveaaSorting(_sorting)
        }

		// ******************** I/O ********************

		this.loadaaSorting = function() {
            var _sorting = this.storage.get(this.key);

            if(!! _sorting) {
                this.storage.set(this.key, _sorting);
            } else {
                this.storage.set(this.key, sorting);
                _sorting = sorting;
            }
            return _sorting;
		};

		this._saveaaSorting = function(aaSorting) {
            var sorts = [];
            for(var i=0; i< aaSorting.length; i++) {
                var sort = aaSorting[i];
                sorts.push([sort[0], sort[1]])
            }
			this.storage.set(this.key, sorts);
		};

		this._deleteaaSorting = function() {
			this.storage.remove(this.key);
		};
		var initialsort = this.loadaaSorting();
		this.update(initialsort);

	}

	var StaticSortMode = {
		newInst : function(conf) {
			return new SortMode(conf);
		}
	};

	return StaticSortMode;

});