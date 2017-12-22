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
 * Note : Checks whether the History API could do just the same with less code.
 */

/*
 * =================
 * Summary :
 * =================
 *
 * This module keeps track of the breadcrumb by setting rules on back navigation (which page should be
 * navigated back to when the back button is clicked).
 *
 * Specifically it assigns to the global variable 'squashtm.workspace.backurl' the url where to navigate back.
 * This url depends on the current location and how this location is mapped.
 *
 *
 * =================
 * Usage :
 * =================
 *
 * Include this module on every pages. Currently the module 'app/ws/squashtm.workspace' requires it
 * so you don't need to bother much with that requirement.
 *
 * Then, choose the behavior of your back button :
 *
 * 1 - You want to navigate back to the previous page whatever it was, or to a specific location you
 * already know : you don't need this module, just configure the back button yourself.
 *
 * or
 *
 * 2 - The back navigation obeys to different rules : you need to
 *	2a : add an entry to the mapping (see below) and
 *	2b : add to your page a button with id="back" and onclick="document.location.href=squashtm.workspace.backurl"
 *
 *
 * ==================
 * Mapping :
 * ==================
 *
 * The module defines several "routes". It maps a given URL to one or several other URL that you can
 * potentially navigate back to. Because it is back navigation, such pages are likely to have been
 * visited first. For this reason such URLs are named here "approved referrer".
 *
 * For a given location, if a mapping is defined then document.referrer will be added to the breadcrumb
 * if and only the referrer is approved according to the mapping.
 *
 * The mappings are defined in object 'module.routes' in this present file. They use the symbolic names
 * of the "URL templates" defined in module "workspace.routing". An entry consists of one URL template
 * and an array of URL templates that define what are the approved referrers. You can negate a template
 * by prefixing it with a bang '!'.
 *
 * For a given url the approved referrer resolution is as follow : the url must either match a positive
 * template, either match none of the negative templates.
 *
 *   Examples :
 *
 *  1) Back navigation from 'pub' is allowed only to 'home', 'work' or 'otherpub :
 *
 *	'pub' : [ 'home', 'work', 'otherpub' ]
 *
 *	2) Back navigation from 'pub.beer' is allowed to anywhere but 'pub.sober' :
 *
 *  'pub.beer' : [ '!pub.sober' ]
 *
 *  3) Back navigation from 'pub' to anywhere except 'work' unless you are a cop :
 *
 *  'pub' : [ '!work', 'work.policestation' ]
 *
 *
 * ----
 * Keep in mind that if a location is not mapped then no special rules will apply and document.referrer
 * will always be considered a valid location to navigate back to.
 *
 *
 * ==================
 * How it works :
 * ==================
 *
 * 1 - The following executes when the document is ready.
 *
 * 2 - First, the module checks whether an element such as input[type="button"]#back exists. If not found the
 * module decides this page is a top level page, thus resets the breadcrumb and exits.
 *
 * 3 - If such button is present, the module first checks whether the last URL in the breadcrumb is the current
 * location, if so that URL is dropped (we eat up the breadcrumb).
 *
 * 4 - If no URL dropped at step 3, the module looks up a mapping (defined below) then one of the following happens :
 *   4a - the current location is not mapped -> document.referrer is enqueued in the breadcrumb
 *   4b - the current location is mapped and the referrer is approved -> document.referrer is enqueued in the breadcrumb
 *   4c - The current location is mapped and the referred is not approved -> the breadcrumb is not updated.
 *
 * 5 - Finally the variable 'squashtm.workspace.backurl' will be set to the last url of the breadcrumb.
 *
 * ==================
 * Returned object :
 * ==================
 *
 * Although the main job is performed at load time a module is returned on completion. It contains the following attributes
 * and methods :
 *
 *  routes: if you're curious, you can look at the mapping. *
 *  get() : returns the current breadcrumb.
 *
 */
define(["jquery", "workspace.routing", "workspace.storage", 'app/util/URLUtils'],
		function($, routing, storage, Urls) {
	"use strict";

	var storekey = 'squashtm.workspace.breadcrumb';
	window.squashtm = window.squashtm || {};
	window.squashtm.workspace = window.squashtm.workspace || {};


	/* ****************************************************************
	 *						Module definition
	 **************************************************************** */
	var module= {
		// mapping : 'location' : [array of approved referrer]
		routes : {
			'search.results' : ['!search'],
			'search' : ['!search.results']
		},

		get : function(){
			return storage.get(storekey) || [];
		}

	};


	/* ****************************************************************
	 *							Library code
	 **************************************************************** */

	// removes the current url from the breadcrumb if
	// we navigated back to it.
	function justNavigatedBack(breadcrumb){
		if (breadcrumb.length > 0){
			var current = document.location.href;
			return (breadcrumb[breadcrumb.length-1] === current);
		}
		else{
			return false;
		}
	}

	function enqueueNoduplicates(breadcrumb, url){
		if (breadcrumb.length===0 || breadcrumb[breadcrumb.length-1] !== url){
			breadcrumb.push(url);
		}
	}

	function resolveApprobation(approvedList, referPath){

		var i, r, match, negate;
		var passed = false,
			isBlacklist = false;

		for (i=0; i < approvedList.length; i++){
			r = approvedList[i];

			negate = (r.charAt(0) === '!');
			isBlacklist = isBlacklist || negate;

			r = (negate) ? r.substring(1) : r;

			match = routing.matches(r, referPath);

			// one positive match -> yay
			if (match && ! negate){
				return true;
			}

			// one negated match -> nay
			else if (match && negate){
				return false;
			}
		}

		/*
		 * If we ran through the whole loop,  then the result depends on
		 * whether the logic was 'white list' or 'black list', ie if
		 * we had at least one negated template.
		 *
		 * Explicitly, if there was at least negated templates, if we ran through the
		 * whole loop then the referrer is ok because it matched none of them.
		 * One the other hand if the whole list was made of positive templates,
		 * if the referrer matched none of them then it is not ok.
		 */
		return isBlacklist;

	}

  function resolveUndefinedUrl(url) {
    if (url) {
      return url;
    }
    else {
      var workspace;
      if (squashtm.app.campaignWorkspaceConf) {
        workspace = "campaigns.workspace";
      }
      else if (squashtm.app.testCaseWorkspaceConf) {
        workspace = "testcases.workspace";
      }
      else if (squashtm.app.requirementWorkspaceConf) {
        workspace = "requirements.workspace";
      }
      //now get url for the workspace and return
      if (workspace) {
        return routing.buildURL(workspace);
      }
    }
    //if we come here something really messed in breadcomb, fallback to home page to prevent nasty 404 or 500.
    return routing.buildURL("home");
  }

	function track(){

		var breadcrumb = storage.get(storekey) || [];
    var backUrl;


		// step 2
		// is a back button defined ? If not, reset the breadcrumb
		if ($("input[type='button']#back").length === 0){
			breadcrumb = [];
		}

		// step 3
		// if we navigated back here remove this location
		// from the breadcrumb.
		else if (justNavigatedBack(breadcrumb)){
			breadcrumb.pop();
			if (breadcrumb.length>0){
        backUrl = breadcrumb[breadcrumb.length-1];
        backUrl = resolveUndefinedUrl(backUrl);
				window.squashtm.workspace.backurl = backUrl;
			}
		}

		// step 4
		else{

			var location = document.location.href,
				referrer = document.referrer;

			var locationPath = Urls.extractPath(location),
				referrerPath = Urls.extractPath(referrer);

			var locationMapped = false,
				referrerMapped = false;

			for (var l in module.routes){
				if (routing.matches(l, locationPath)){
					locationMapped = true;
					var approvedList = module.routes[l];

					referrerMapped = resolveApprobation(approvedList, referrerPath);

				}
			}

			// tests 4a, 4b, 4c
			if ( (! locationMapped) || (locationMapped && referrerMapped)){
				enqueueNoduplicates(breadcrumb, referrer);
			}

			// step 5
      backUrl = breadcrumb[breadcrumb.length-1];
      backUrl = resolveUndefinedUrl(backUrl);
      window.squashtm.workspace.backurl = backUrl;

		}


		storage.set(storekey, breadcrumb);

	}

	/* ****************************************************************
	 *		This code is executed at load time
	 **************************************************************** */

	$(function(){
		track();
	});

	return module;

});
