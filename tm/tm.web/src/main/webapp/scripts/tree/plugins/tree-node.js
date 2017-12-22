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
 * This file defines several things we want a node to be able to know/perform.
 * Also able to sublass itself to implement nature-specific behaviours (eg, accepts in-droppped
 * nodes, which ones etc).
 *
 * A TreeNode also knows the address of the resource it designate and where to fetch its content.
 *
 *
 */


define(['jquery'], function($){

	// *********************** functions ************************************
	// 'library' and 'folder' are still named 'library' and 'folder'. Others
	// will be renamed according to their subtypes.
	function _getSemiSpecializedTypeName(node){
		var type = node.getDomType();
		var typeRepresentation = "";

		if ((type==='folder') || (type === 'drive')){
			return type+'s';
		}
		else{
			return node.getResType();
		}
	}

	function getResourceUrl(){
		return this.getBaseUrl() + this.getResType() + "/" + this.getResId();
	}

	function getBaseUrl(){
		return this.tree.data.squash.rootUrl + "/";
	}

	function getBrowserUrl(){
		return this.getBaseUrl() + this.getWorkspace() + "-browser";
	}

	function getContentUrl(){

		var wkspce = this.getWorkspace();
		var id = this.getResId();
		var representation = _getSemiSpecializedTypeName(this);

		return this.getBrowserUrl() + '/' + representation + "/" + id + "/content";

	}


	function getCopyUrl(){

		var representation =   _getSemiSpecializedTypeName(this);
		var url = this.getBrowserUrl() + '/' + representation + '/' +this.getResId();

		switch (this.getDomType()) {
			case "drive" :
			case "folder":
			case "requirement" :	url += '/content/new'; break;
			case "campaign" :		url += '/iterations/new'; break;
			case "iteration":		url += '/test-suites/new'; break;
			default : throw "copy aborted : node type '"+this.getDomType()+"' cannot receive new content.";
		}

		return url;

	}


	function getMoveUrl(){

		var representation =   _getSemiSpecializedTypeName(this);
		var url = this.getBrowserUrl() + '/' + representation + '/' +this.getResId();

		switch (this.getDomType()) {
			case "drive" :
			case "folder":
			case "requirement" :
			case "campaign" :	url += '/content/{nodeIds}/{position}'; break;
			default : throw "move aborted : node type '"+this.getDomType()+"' cannot receive moved content.";
		}

		return url;
	}


	function getDeleteUrl(){
		var specific = "";
		var options = "";
		switch (this.getDomType()){
			case "folder" :
			case "test-case" :
			case "requirement" :
			case "dashboard" :
			case "report":
      case "chart":
			case "campaign"		: specific = "/content"; break;
			case "iteration"	: specific = "/iterations"; break;
			case "test-suite"	: specific = '/test-suites'; options="?remove_from_iter={remove_from_iter}"; break;
		}
		return this.getBrowserUrl()+specific+"/{nodeIds}" + options;
	}

	function refreshLabel(){

		var label = null;
		var name;


		switch(this.getResType()){

		case 'requirements' :
		case 'test-cases' :
		case 'campaigns' :
		case 'iterations' :
			name = this.getName();
			var reference = this.getReference() || "";

			if (reference.length > 0) {
				reference += " - ";
			}

			label = reference + name;
			break;

		default :
			label = this.getName();
			break;
		}

		this.tree.set_text(this, label);
	}



	// *********************** plugin definition ************************************

	$.fn.treeNode = function() {

		// check validity of this call to treeNode();
		var lt = this.length;
		var tree = $.jstree._reference(this);
		var noLi = (!this.is('li')) && (!this.is('a') && (!this.is('ins')));

		if ((!lt) || (!tree) || (noLi)) {
			throw "this node is not a valid tree node";
		}

		this.tree = $.jstree._reference(this);
		this.reference = (this.is('a')) ? this.parent("li") : this;

		// ************* methods for 1-sized jquery object **************

		// ************ basic getters

		this.getDomId = function() {
			return this.reference.attr('id');
		};

		this.getDomType = function() {
			return this.reference.attr('rel');
		};

		// equivalent to getDomType
		this.getRel = function(){
			return this.reference.attr('rel');
		};

		this.getResId = function() {
			return this.reference.attr('resid');
		};

		this.getResType = function() {
			return this.reference.attr('restype');
		};

		this.getIdentity = function(){
			return {
				resid : this.reference.attr('resid'),
				restype : this.reference.attr('restype')
			};
		};

		this.isEditable = function() {
			return this.reference.attr('editable') === "true";
		};

		this.isCreatable = function() {
			return this.reference.attr('creatable') === "true";
		};

		this.isMilestoneCreatable = function(){
			return this.reference.attr('milestone-creatable-deletable') === "true";
		};

		this.isMilestoneEditable = function(){
			return this.reference.attr('milestone-editable') === "true";
		};

		this.isDeletable = function() {
			return this.reference.attr('deletable') === "true";
		};

		this.isExportable = function() {
			return this.reference.attr('exportable') === "true";
		};

		/**
		 * Checks if some permission is authorized for this node.
		 */
		this.isAuthorized = function(permission) {
			// binds a permission to its quality,
			var qualities = {
				READ : "readable",
				WRITE : "editable",
				CREATE : "creatable",
				DELETE : "deletable",
				EXECUTE : "executable",
				EXPORT : "exportable",
        //Issue 6138 for jirareq
        //We now need to have the import right directly in the library node
        IMPORT : "importable",
				MANAGEMENT : "manageable"
			};

			if (permission === "ANY") {
				return true;
			}

			var candidate = qualities[permission];
			if (candidate) {
				return this.reference.attr(candidate) === "true";
			}
			// permission not defined => not authorized
			return false;
		};
		/**
		 * Checks if a given workspace wizard is enabled for this node.
		 *
		 * @param wizard
		 *            an object with an id property which will be used to
		 *            perform the check
		 */
		this.isWorkspaceWizardEnabled = function(wizard) {
			// enabled wizards list is flattened into comma-separated string
			var enabledWizardsAttr = this.getLibrary().attr("wizards");
			var enabledWizards = (enabledWizardsAttr === undefined) ? []
					: enabledWizardsAttr.split(",");
			return enabledWizards
					&& ($.inArray(wizard.id, enabledWizards) > -1);
		};

		this.getName = function() {
			return this.reference.attr('name');
		};

		this.getReference = function() {
			return this.reference.attr('reference');
		};

		this.getIndex = function() {
			return this.reference.attr('iterationIndex');
		};

		this.getPath = function() {
			return '/'+this.getAncestors().all('getName').join().replace(/,/g, '/');
		};

		this.getProjectId = function() {
			return this.getLibrary().attr('project');
		};

		// ************ some setters **************

		this.setAttr = function(attrName, value){
			this.reference.attr(attrName, value);
			this.refreshLabel();
		};

		this.setName = function(name) {
			this.reference.attr('name', name);
			this.refreshLabel();
		};

		this.setReference = function(reference) {
			this.reference.attr('reference', reference);
			this.refreshLabel();
		};

		// ************ relationships getters

		this.getLibrary = function() {
			if (this.reference.is(':library')) {
				return this;
			} else {
				var library = this.reference.parents(':library');
				return library.treeNode();
			}
		};

		this.getParent = function() {
			return this.reference.parents("li").first().treeNode();
		};

		this.getWorkspace = function() {
			return this.getLibrary().getResType().replace('-libraries', '');
		};

		this.getChildren = function() {
			var children =  this.tree._get_children(this);
			return (children.length) ? children.treeNode() : $();
		};

		this.getFlatSubtree = function(){
			var subtree= this.find('li');
			return (subtree.length) ? subtree.treeNode() : $();
		};

		this.getAncestors = function() {
			return this.parents('li', this.tree).add(this).treeNode();
		};

		// ***************** tree actions

		this.deselectChildren = function(){
			var children = this.find('a.jstree-clicked').parent('li').not(this);
			this.tree.deselect_node(children);
		};

		this.refresh = function() {
			if (this.canContainNodes()){
				if (this.isLoaded()){
					this.tree.refresh(this);
				}
				else{
					return this.load();
				}
			}
		};

		this.isOpen = function() {
			// note : the original #isOpen() from the tree core module returns true when open, but when it's closed it returns anything but a boolean
			var isOpen = this.tree.is_open(this);
			return (!isOpen) ? false : true;
		};

		this.open = function() {
			var defer = $.Deferred();
			this.tree.open_node(this, defer.resolve);
			return defer.promise();
		};

		this.isLoaded = function(){
			return this.tree._is_loaded(this);
		};

		this.load = function() {
			var defer = $.Deferred();
			this.tree.load_node(this, defer.resolve, defer.reject);
			return defer.promise();
		};

		this.close = function() {
			this.tree.close_node(this);
		};

		this.appendNode = function(data) {
			var defer = $.Deferred();
			var res = this.tree.create_node(this, 'last', data, defer.resolve,
					true);
			var newNode = res.treeNode();
			return [ newNode, defer.promise() ];
		};


		/* Will move around the nodes without triggering events.
		 * Moved nodes will be removed from their container.
		 */
		this.moveTo = function(target){

			if (this.length===0) {return;}

			var oldParents = this.all('getParent');

			// remove me from my former parent
			this.removeMe();

			// if the target was empty, now it isn't anymore.
			if (target.hasClass('jstree-leaf')){
				target.removeClass('jstree-leaf').addClass('jstree-closed');
			}

			// if the target was loaded, we must actually move the nodes in there because they won't be fetched
			// from the server again. We create the <ul/> in the process if need be.
			if (target.isLoaded){
				var ul = (target.find('> ul'));
				if (ul.length===0){
					ul = $("<ul/>");
					target.append(ul);
				}
				this.appendTo(ul);
				this.afterMove(target, oldParents);
			}

		};


		/*
		 * post processing after movements. For now, only synchronized requirements needs that.
		 */
		this.afterMove = function(newParent, oldParents){
			var requirements = this.getFlatSubtree().add(this).filter(':requirement[synchronized="true"]');

			var parents = $(newParent).add(oldParents);
			if (parents.length === 0){
				// this is not supposed to happen, but meh.
				return;
			}
			parents = parents.treeNode();
			if (! parents.areSameLibs()){
				requirements.removeAttr('synchronized');
			}
			if(squashtm && squashtm.app && squashtm.app.wreqr){
				var wreqr = squashtm.app.wreqr;
				wreqr.trigger("tree.moveNodes.done");
			}
		};

		this.select = function() {
			this.tree.select_node(this);
		};

		this.deselect = function() {
			this.tree.deselect_node(this);
		};

		this.deselect_all = function() {
			this.tree.deselect_all();
		};

		this.removeMe = function(){
			var tr = this.tree;
			this.each(function(elt){
				tr.delete_node(this);
			});
		};

		// *********** tests **********************

		this.match = function(matchObject) {
			for ( var ppt in matchObject) {
				if ((this.attr(ppt) != matchObject[ppt])){
					return false;
				}
			}
			return true;
		};


		//TODO : this test relates to the configuration of the "types" plugin of their instance of jstree.
		this.acceptsAsContent = function(nodes) {

			// reject if this node cannot have children anyhow
			if (! this.canContainNodes()){
				return false;
			}

			var typePluginConf = this.tree._get_settings().types.types;
			var thisRel = this.getDomType();

			//might throw npe if the conf is invalid, and so is good candidate for fail-fast warning
			var validChildrenTypes = typePluginConf[thisRel].valid_children;

			if (! validChildrenTypes instanceof Array ){
				validChildrenTypes = [validChildrenTypes];
			}

			return nodes.areEither(validChildrenTypes);

		};


		this.canContainNodes = function(){
			//might throw npe if the conf is invalid, and so is good candidate for fail-fast warning
			var typePluginConf = this.tree._get_settings().types.types;
			var thisRel = this.getDomType();
			var thisConf = typePluginConf[thisRel];

			return (thisConf !== undefined && thisConf.valid_children !== 'none');
		};

		// ************* methods for multiple matched elements ************

		// one method to rule them all. Accepts a string, or an array of string.
		// those strings represent the name of the methods we want to call.
		// returns a collection of object which properties are the lowercased
		// name of the
		// methods (minus 'get' if present) and the corresponding values.
		this.all = function(strOrArray) {
			return this.collect(function(elt) {
				if (typeof strOrArray == 'string') {
					return $(elt).treeNode()[strOrArray]();
				} else {
					var data = {};
					var localNode = $(elt).treeNode();
					for ( var i = 0; i < strOrArray.length; i++) {
						var func = strOrArray[i];
						var res = localNode[func]();
						data[func.toLowerCase().replace('get', '')] = res;
					}
					return data;
				}
			});
		};

		// if 1 argument is present, it must be an array of strings representing
		// the
		// dom attributes we need and returns a collection of objects made of
		// the specified attributes.
		//
		// if no argument is specified, defaults to restype and resid.
		this.toData = function() {

			var attributes;

			if (arguments.length === 0) {
				attributes = [ "restype", "resid" ];
			}
			else {
				attributes = arguments[0];
			}

			return this.collect(function(elt) {
				var res = {};
				var localNode = $(elt);
				for ( var i in attributes) {
					var attr = attributes[i];
					res[attr] = localNode.attr(attr);
				}
				return res;
			});
		};

		// given a matchObject describing the name/value dom attributes they all
		// must share,
		// returns true if they all have the same or false if they differ.
		this.allMatch = function(matchObject) {

			if (this.length === 0){
				return false;
			}

			var shrinkingSet = this;

			for ( var ppt in matchObject) {
				var selector = "[" + ppt + "='" + matchObject[ppt] + "']";
				shrinkingSet = shrinkingSet.filter(selector);
			}

			return (shrinkingSet.length == this.length);
		};


		this.areEither = function(typesArray){
			var collected = this.all('getDomType');
			return $(collected).not(typesArray).length === 0;
		};

        this.areSameLibs = function() {
            var libs = this.collect(function(elt) {
                    return $(elt).treeNode().getLibrary().getDomId();
            });
            return ($.unique(libs).length == 1);
        };


		// *************** urls *******************************

		this.getResourceUrl = getResourceUrl;

		this.getBaseUrl = getBaseUrl;

		this.getBrowserUrl = getBrowserUrl;

		this.getContentUrl = getContentUrl;

		this.refreshLabel = refreshLabel;

		this.getCopyUrl = getCopyUrl;

		this.getMoveUrl = getMoveUrl;

		this.getDeleteUrl = getDeleteUrl;

		return this;
	};
});
