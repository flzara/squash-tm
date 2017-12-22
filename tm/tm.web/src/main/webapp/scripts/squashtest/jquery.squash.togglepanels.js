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
(function($) {
	/**
	 * The über-new toggle-panel: each `.sq-tg` is automatically turned into a toggle panel
	 *
	 * A toggle panel (`.sq-tg`) can be marked as `.frozen`. In that case, it no longer (un)folds
	 * Remove the `.frozen` class to let it go
	 */
	$(document).on("click", ".sq-tg:not(.frozen) .tg-head", function(event) {
		var $target = $(event.target);

		if ($target.parents(".tg-toolbar").length > 0) {
			// click from within the toolbar -> bail out
			return;
		} // else do the toggling.

		event.stopImmediatePropagation();

		var $panel = $target.parents(".sq-tg");

		$panel.find(".tg-body").toggle("blind", 500, function() {
			$panel.toggleClass("collapse");
			$panel.toggleClass("expand");

			$panel.find(".tg-toolbar .sq-btn, button, input, a").prop("disabled", $panel.is(".collapse"));
		});
	});

	// turns .sq-tl into toggle lists
	$(document).on("click", ".sq-tl .tl-head", function(event) {
		var $target = $(event.target);
		event.stopImmediatePropagation();
		var $panel = $target.parent(".sq-tl");

		$panel.find(".tl-body").toggle("blind", 500, function() {
			$panel.toggleClass("collapse");
			$panel.toggleClass("expand");
		});

	});
}(jQuery));
