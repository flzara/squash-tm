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
 *
 * This module defines the most important urls in the application. Each url is a template because it may accept placeholders.
 * A placeholder is enclosed by curly braces '{}' and a regular expression in between that describe what an actual replacement
 * should look to.
 *
 * ==========API============
 *
 *	'url name' : returns the URL template mapped to the 'url name' (see the list right below)
 *
 *	buildURL : function(urlName, parameters) : builds an URL based on a template and values for its placeholders.
 *				The first argument is an 'url name' and the rest are arbitrary additional arguments.
 *				The placeholders will be filled with the arguments in the order they are those additional arguments are supplied.
 *				NB : the placeholders values won't be tested against the regular expression so
 *
 *	unbuildURL : function(urlName, url, [withQueryString]) : if "url" matches the template "urlName", will return an array containing the actual values
 *	from the url that corresponds to the placedholders in the template. Returns an empty array if there is nothing to deconstruct.
 *	will throw an error if url doesn't match the template.
 *	By default, the matching is performed including the whole querystring in both the url and the template. If you don't want to
 *	take the query string into account, you may pass 'false' as a third parameter.
 *
 *	matches : function(urlName, candidate) : tests the candidate url against the template named 'urlName'.
 *	By default, the matching is performed including the whole querystring in both the url and the template. If you don't want to
 *	take the query string into account, you may pass 'false' as a third parameter.
 */
define([], function(){

	"use strict";

	var root = window.squashtm.app.contextRoot.replace(/\/$/, '');



	/*
	 * returns the template as a RegExp object.
	 * it is done by escaping the static part of the template,
	 * and inlining the regex embedded in the placeholders.
	 *
	 * if parameter 'capture' is true, the generated regex
	 * will
	 */

	/*
	 * Tech note : here is how it works
	 *
	 * 1 - break down the template, using a delimiter that match the placeholders {expr}. The 'expr' within a placeholder
	 * is itself captured and returned as part of the breakdown.
	 *
	 * 2 - the breakdown is an array. At event indexes (i%2===0), you'll find the litteral (ie, invariant) parts of the template.
	 * At odd indexes (i%2===1), you'll get the expressions
	 *
	 * 3 - escape all litteral parts if they contain special characters we want litteraly as is
	 *
	 * 4 - if the 'capture' parameter is true, the expr parts will be rewritten as capturing group in the output regex
	 *
	 * 5 - join the array. This is the final output regex.
	 *
	 */
	function templateToRegex(template, cap){

		var capture = (cap !== undefined) ? cap : false;

		var mPholder = /\{([^\}]+)\}/g;

		var breakdown = template.split(mPholder);

		// here we use a boolean switch instead of
		// testing a modulo at each iteration of the loop
		var litteral = true;
		for (var i=0; i<breakdown.length; i++){
			// litteral case (see step 3 in the doc)
			if (litteral){
				breakdown[i]=breakdown[i].replace(/[\-\[\\\]\{\}\(\)\*\+\?\.\,\\\^\$\|\#\s\/]/g, "\\$&");
			}
			else if (capture){
				breakdown[i] = '(' + breakdown[i] + ')';
			}
			// flip the switch
			litteral = !litteral;
		}

		var expression = breakdown.join('');

		return new RegExp("^"+expression+"$");
	}

	return {

		// url names mapping
		'home': 	root + '/home-workspace/',
        'home.content.message': 	        root + '/home-workspace/choose-message',
        'home.content.dashboard': 	        root + '/home-workspace/choose-dashboard',
		'attachments.manager':				root + '/attach-list/{\\d+}/attachments/manager',
		'search' :							root + '/advanced-search',
		'search.results' :					root + '/advanced-search/results',

		'bugtracker.campaignfolder' :		root + '/bugtracker/campaign-folder/{\\d+}',
		'bugtracker.execution' :			root + '/bugtracker/execution/{\\d+}',
		'bugtracker.execsteps.new' :		root + '/bugtracker/execution-step/{\\d+}/new-issue',

		'testcases.workspace' :				root + '/test-case-workspace/',
		'testcases.base' :					root + '/test-cases',
		'testcases.info' :					root + '/test-cases/{\\d+}/info',
		'testcases.requirements.manager' :	root + '/test-cases/{\\d+}/verified-requirement-versions/manager',

		'teststeps.info' :					root + '/test-steps/{\\d+}',
		'teststeps.fromExec' :              root + '/test-steps/{\\d+}/from-exec?optimized={true|false}',
		'teststeps.requirements.manager' :	root + '/test-steps/{\\d+}/verified-requirement-versions/manager',

		'requirements.workspace':			root + '/requirement-workspace/',
		'requirements' :					root + '/requirements/{\\d+}',
		'requirements.info'	:				root + '/requirements/{\\d+}/info',
		'requirements.versions.new' :		root + '/requirements/{\\d+}/versions/new',
		'requirements.currentversion'	:	root + '/requirement-versions/{\\d+}"',
		'requirements.linkedRequirementVersions' : root + '/requirement-versions/{\\d+}/linked-requirement-versions',
		'requirements.linkedRequirementVersions.manager' : root + '/requirement-versions/{\\d+}/linked-requirement-versions/manager',
		'requirementLinkType' : root + '/requirement-link-type',
		'requirementLinkType.checkCodes' : root + '/requirement-link-type/check-codes',
		'requirement.link.type' : root + '/requirement-link-type/{\\d+}',
		'requirements.statuses'			:	root + '/requirements/{\\d+}/next-status',
		'requirements.versions.manager'	:	root + '/requirements/{\\d+}/versions/manager',
		'requirements.testcases' :			root + '/requirement-versions/{\\d+}/verifying-test-cases',
		'requirements.testcases.manager':	root + '/requirement-versions/{\\d+}/verifying-test-cases/manager',
		'requirements.audittrail.change' :	root + '/audit-trail/requirement-versions/fat-prop-change-events/{\\d+}',
		'requirements.audittrail.model' :	root + '/audit-trail/requirement-versions/{\\d+}/events-table',
		'requirements.coverageStats.model' :root + '/requirement-versions/{\\d+}/coverage-stats',

		'requirementversions'	:			root + '/requirement-versions/{\\d+}',
		'requirementversions.info': root + '/requirement-versions/{\\d+}/info',
		'requirementversions.bulkupdate' : root + '/requirement-versions/{(,?\\d+)+}/bulk-update',

		'campaigns.workspace' :				root + '/campaign-workspace/',
		'campaigns.testplan.manager' :		root + '/campaigns/{\\d+}/test-plan//manager',
		'campaigns.countIterations'	:		root + '/campaigns/{\\d+}/iterations/count',
		'iterations.base' :					root + '/iterations',
		'iterations.testplan.manager' :		root + '/iterations/{\\d+}/test-plan-manager',
		'iterations.testplan.lastexec' :	root + '/iterations/{\\d+}/test-plan/{\\d+}/last-execution',
		'iterations.testplan.changestatus' : root + '/iterations/test-plan/{\\d+}',


		'testsuites.base' :					root + '/test-suites',
		'testsuites.testplan.manager' :		root + '/test-suites/{\\d+}/test-plan-manager',
		'testsuites.testplan.lastexec' :	root + '/test-suites/{\\d+}/test-plan/{\\d+}/last-execution',
		'testsuites.execute.stepbyindex' :	root + '/test-suites/{\\d+}/test-plan/{\\d+}/executions/{\\d+}/steps/index/{\\d+}',
		'testsuites.execute.prologue' :		root + '/test-suites/{\\d+}/test-plan/{\\d+}/executions/{\\d+}/steps/prologue',
		'testsuites.runner'				:	root + '/test-suites/{\\d+}/test-plan/execution/runner',

		'executions' :						root + '/executions/{\\d+}',
		'executions.generalinfos' :			root + '/executions/{\\d+}/general',
		'executions.steps' :				root + '/executions/{\\d+}/steps',
		'executions.autosteps' :			root + '/executions/{\\d+}/auto-steps',
		'executions.runner' :				root + '/executions/{\\d+}/runner',
		'execute' :							root + '/execute',
		'execute.stepbyid' :				root + '/execute/{\\d+}/step/{\\d+}',
		'execute.stepbyindex' :				root + '/execute/{\\d+}/step/index/{\\d+}',
		'execute.prologue' :				root + '/execute/{\\d+}/step/prologue',


		'administration.bugtrackers'	:	root + '/administration/bugtrackers',
		'customfield.values' :				root + '/custom-fields/values',
		'customfield.values.get' :			root + '/custom-fields/values?boundEntityId={\\d+}&boundEntityType={[A-Z_]+}',
		'denormalizefield.values.get' :		root + '/denormalized-fields/values?denormalizedFieldHolderId={\\d+}&denormalizedFieldHolderType={[A-Z_]+}',
		'administration.milestones'   :     root + '/administration/milestones',
		'administration.milestones.clone' : root + '/administration/milestones/{\\d+}/clone',
		'milestone.bindedproject'     :     root + '/milestones-binding/milestone/{\\d+}/project?binded',
		'milestone.bindableproject'   :     root + '/milestones-binding/milestone/{\\d+}/project?bindable',
		'milestone.bind-projects-to-milestone':  root + '/milestones-binding/milestone/{\\d+}/project',
		'milestone.bind-milestones-to-project':  root + '/milestones-binding/project/{\\d+}/milestone',
		'milestone.info' :					root + '/milestones/{\\d+}/info',
		'milestone.unbind-templates' :		root + '/milestones-binding/milestone/{\\d+}/template',
		'milestone.unbind-objects' :		root + '/milestones/{\\d+}/unbindallobjects',
		'milestones.selectable' :			root + '/milestones?selectable',
		'info-list.info':					root + '/info-lists/{\\d+}',
		'info-list-item.info' :				root + '/info-list-items/{\\d+}',
		'info-list.position' :				root + '/info-lists/{\\d+}/items/positions',
		'info-list.items' :					root + '/info-lists/{\\d+}/items',
		'info-list.isUsed' :				root +  '/info-lists/{\\d+}/isUsed',
		'info-list.defaultItem' :			root + '/info-lists/{\\d+}/defaultItem ',
		'info-list-item.isUsed' :			root + '/info-list-items/{\\d+}/isUsed',
		'info-list-item.delete' :			root + '/info-lists/{\\d+}/items/{\\d+}',
		'info-list-item.exist' :			root + '/info-lists/items/code/{\\d}',
		'info-list.bind-to-project' :		root + '/info-list-binding/project/{\\d+}/{\\w+}',
		'docxtemplate' :					root + '/reports/{\\w+}/views/{\\d+}/docxtemplate',
		'ie9sucks' :						root + '/reports/0/ie9',
		'milestone.synchronize' :			root + '/administration/milestones/{\\d+}/synchronize/{\\d+}',

		'search-tc.mass-change.associable-milestone'  : root + '/advanced-search/milestones/tc-mass-modif-associables/{\\d+}',
		'search-tc.mass-change.data' :		root + '/advanced-search/milestones/tc-mass-modif-data/{\\d+}',
		'search-tc.mass-change.bindmilestones' : root + '/advanced-search/tcs/{\\d+}/milestones',
		'search-reqV.mass-change.associable-milestone'  : root + '/advanced-search/milestones/reqV-mass-modif-associables/{\\d+}',
		'search-reqV.mass-change.data' :	root + '/advanced-search/milestones/reqV-mass-modif-data/{\\d+}',
		'search-reqV.mass-change.bindmilestones' : root + '/advanced-search/reqVersions/{\\d+}/milestones',
		'project.new' :						root + '/projects/new',
		'generic.project.description' :		root + '/generic-projects/{\\d+}/description',
		'generic.template.new' :			root + '/generic-projects/new-template',
		'template.new' :					root + '/project-templates/new',
		'template' :						root + '/project-templates?dropdownList',
		'execution.update-from-tc' :		root + '/execute/{\\d+}/update-from-tc',
		'execution.updateExecStep' :		root + '/executions/{\\d+}/updateSteps',
		'chart.wizard.data' :				root + '/charts',
		'chart.new' :						root + '/charts/new/{\\d+}',
		'chart.update' :					root + '/charts/update/{\\d+}',
		'chart.wizard' :					root + '/charts/wizard/{\\d+}',
		'chart.instance' :					root + '/charts/{\\d+}/instance',

		//custom report workspace
		// note : I understand that the next 4 urls below don't need a root
		// because they are used in a backbone router (custom-report-workspace/init-action.js)
		'custom-report-library' :			'/custom-report-library/{\\d+}',
		'custom-report-folder' :			'/custom-report-folder/{\\d+}',
		'custom-report-dashboard' :			'/custom-report-dashboard/{\\d+}',
		'custom-report-report' :				'/custom-report-report/{\\d+}',
		'custom-report-report-redirect' :	root + '/custom-report-workspace/#custom-report-report/{\\d+}',
		'custom-report-chart' :				'/custom-report-chart/{\\d+}',
        'custom-report-chart-redirect' :	root + '/custom-report-workspace/#custom-report-chart/{\\d+}',
		'custom-report-library-server' :	root + '/custom-report-library',
		'custom-report-folder-server' :		root + '/custom-report-folder',
		'custom-report-dashboard-server' :	root + '/custom-report-dashboard/{\\d+}',
        'custom-report-dashboard-favorite' :    root + '/custom-report-dashboard/favorite/{[a-z]+}/{\\d+}',
		'custom-report-chart-server' :		root + '/custom-report-chart/{\\d+}',
		'custom-report-report-server' :		root + '/custom-report-report/{\\d+}',
		'custom-report-chart-binding' :		root + '/custom-report-chart-binding',
		'custom-report-chart-binding-with-id' : root + '/custom-report-chart-binding/{\\d+}',
		'custom-report-chart-binding-replace-chart' : root + '/custom-report-chart-binding-replace-chart/{\\d+}/{\\d+}',
		//don't forget the '/' at the end so the tree will set correctly the cookie. Else the url will be /squash/custom-report-workspace#...
		//and the cookie path will be a nasty '/squash'
		'custom-report-base' : root + '/custom-report-workspace/',

		// report-workspace
		'report-workspace' : root + '/report-workspace/{\\d+}',
		'reports': root + '/reports/',

		// project plugins
		'project-plugins' :					root + '/generic-projects/{\\d+}/plugins/{[^\\/]+}/',	// the trailing '/' is important

		// server authentication
		'servers.authentication' :			root + '/servers/{\\d+}/authentication',

		// authorizations
		'acls': root + '/acls',

		//user prefs
		'user-pref-update' : root + '/user-prefs/update',

		// helper methods
		buildURL : function(){
			var args = Array.prototype.slice.call(arguments);
			var template = this[args.shift()];

			var res = template;
			while (args.length>0) {
				res = res.replace(/\{[^\}]+\}/, args.shift());
			}

			return res;
		},

		unbuildURL : function(urlName, urlp, withQuery){

			var template = this[urlName];
			var url = urlp;

			// remove the query strings if asked to
			withQuery = (withQuery !== undefined) ? withQuery : true;

			if (! withQuery){
				template = template.split(/[?#]/)[0];
				url = url.split(/[?#]/)[0];
			}

			// process
			var tplExp = templateToRegex(template, true);
			var matches = tplExp.exec(url.replace(/\/\//,'/'));	// we remove the double '//' that may occur sometimes

			if (matches !== null){
				return matches.slice(1);
			}
			else{
				throw "workspace.routing : cannot unbuildURL('"+url+"', '"+urlName+"') because they do not match";
			}

		},

		// equivalent - but faster - to  { try{ return (this.unbuildURL(candidate, urlName) !== null); }catch(e){return false) }
		matches : function(urlName, candidate, withQuery){

			var template = this[urlName];
			var url = candidate;

			// remove the query strings if asked to
			withQuery = (withQuery !== undefined) ? withQuery : true;

			if (! withQuery){
				template = template.split(/[?#]/)[0];
				url = url.split(/[?#]/)[0];
			}

			// process
			var tplExp = templateToRegex(template, false);
			return tplExp.test(url.replace(/\/\//,'/'));	// we remove the double '//' that may occur sometimes
		}

	};


});
