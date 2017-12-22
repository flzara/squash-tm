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
define(["jquery", "backbone", "handlebars", "underscore", "workspace.routing", "squash.translator","tree","workspace.storage","jquery.squash.formdialog"],
  function ($, Backbone, Handlebars, _, urlBuilder, translator, tree, storage) {
  var viewConstructor = Backbone.View.extend({

    el: "#coverage-stat",
    treeSelector : "#perimeter-tree",
    storagePrefix : "requirement-coverage-stat-perimeter-",

    events : {
        "click #change-perimeter-button" :"showSelectPerimeter"
    },

    initialize: function () {
        this.initializeData();
        this.initPerimeterDialog();
    },

    initializeData : function () {
        var url = urlBuilder.buildURL("requirements.coverageStats.model",this.model.get("id"));
        var self = this;
        var key = this.getStorageKey();
        var value = storage.get(key) ? storage.get(key) : {id:"",name:""};
        var data = {
          perimeter : value.id
        };

          $.ajax({
            url: url,
            type: 'GET',
            data : data
          })
          .done(function(response) {
            if (value.id!=="") {
              self.model.set("hasPerimeter",!response.corruptedPerimeter);
              self.model.set("perimeterName",value.name);
              self.model.set("verification",response.rates.verification);
              self.model.set("validation",response.rates.validation);
            }
            else {
              self.model.set("hasPerimeter",false);
            }
            self.model.set("corruptedPerimeter",response.corruptedPerimeter);
            self.model.set("isAncestor",response.ancestor);
            self.model.set("coverage",response.rates.coverage);
            self.render();
          });
        return this;
    },

    render : function () {
        this.$el.find("#table-rates").html("");
        var templatedRates = this.makeTemplating("#tpl-table-rates",this.model.attributes);
        this.$el.find("#table-rates").html(templatedRates);
    },

    initPerimeterDialog : function () {
        var self = this;
        var templated = this.makeTemplating("#tpl-dialog-select-perimeter");
        this.$el.find("#dialog-select-perimeter-wrapper").html(templated);
        var dialog = this.$el.find("#dialog-select-perimeter").formDialog();

        //Init popup events
        dialog.on('formdialogconfirm', function(){
        self.changePerimeter();
            dialog.formDialog('close');
        });

        dialog.on('formdialogcancel', function(){
            dialog.formDialog('close');
        });

        $.ajax({
            url : squashtm.app.contextRoot + "/" + 'campaign-workspace/tree/0',
            datatype : 'json'


        }).done(function(model){

        var treeConfig = {
            model : model,
            treeselector: self.treeSelector,
            workspace: "campaign-it"
        };
        //[Issue 6039] giving focus back to workspace tree after initialize the tree picker
        //Has to keep workspace tree reference before init, and regive focus back to the first tree after instanciation of the second tree
        var worksptree= tree.get();
        tree.initLinkableTree(treeConfig);
        if (!! worksptree){
        	worksptree.jstree('set_focus');
        }
      });
    },

    makeTemplating : function (selector, data) {
        var source = $(selector).html();
        var template = Handlebars.compile(source);
        return template(data);
    },

    showSelectPerimeter : function () {
        $("#dialog-select-perimeter").formDialog("open");
    },

    /**
     * Set the choosen perimeter in local storage.
     * Each project will have is own perimeter, choosen when you choose a perimeter for a requirement version of this project
     * @return {[type]} [description]
     */
    changePerimeter : function () {
        var selectedNode = $(this.treeSelector).jstree("get_selected");
        var key = this.getStorageKey();
        var id = selectedNode.getDomId();
        var name = selectedNode.getName();
        var value = {
          id : id,
          name : name
        };

        storage.set(key,value);
        this.initializeData();
    },

    getStorageKey : function () {
        return this.storagePrefix + this.model.get("projectId");
    }
  });

  return viewConstructor;
});
