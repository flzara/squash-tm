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
/**
 * 
 * The structure of the table should be the following (copy/paste if you like). Pay attention
 * to the css classes 'bind-milestone-dialog-X" 
 * 
 * <div class="bind-milestone-dialog popup-dialog" >
 * 
 * 	<div>
 * 		<table class="bind-milestone-dialog-table">
 * 			<thead>
 * 				<th data-def="sClass=bind-milestone-dialog-check"></th>
 * 				// and other headers
 * 			</thead>
 * 			<tbody>
 * 
 * 			</tbody>
 * 		</table>
 * 
 * 		<div class="bind-milestone-dialog-selectors">
 * 			<span class="bind-milestone-dialog-selectall cursor-pointer"/>select all</span>
 * 			<span class="bind-milestone-dialog-selectnone cursor-pointer"/>select none</span>
 * 			<span class="bind-milestone-dialog-invertselect cursor-pointer"/>invert selection</span>
 *		</div> 	
 * 	</div>
 * 
 * 	<div class="popup-dialog-buttonpane">
 * 		<input type="button" class="bind-milestone-dialog-confirm" data-def="evt=confirm, mainbtn" value="confirm"/>
 * 		<input type="button" class="bind-milestone-dialog-cancel" data-def="evt=cancel" value="cancel"/>
 * 	</div>
 * 
 * </div>
 * 
 * 
 * 
 * available options : 
 * 
 * {
 *	multilines : boolean, whether the table allows for multilines selection,
 *	mustChoose : boolean, says whether empty choice is allowed or not. Default is false.
 *	tableSource : the URL from where the data should be fetch to 
 *  
 *  milestonesURL : which URL the bindings should be posted to 
 *  identity : the identity of the entity to which we add milestones
 *  
 * }
 * 
 * 
 * events : 
 * 	- node.bindmilestones : the dialog committed some new milestone belongings.
 * 							The event comes with a companion data : { identity : identity, milestones : [array, of, milestoneids] } 
 * 							
 * 
 * Notes : the datatable row model should provide at least the attribute 'entity-id' 
 * 
 * 
 */
define(["jquery", "workspace.event-bus", "app/ws/squashtm.notification", 
        "squash.translator",
        "jqueryui", "jquery.squash.formdialog", "squashtable"], 
		function($, eventBus, notification, translator){
	
	
	
	function handleClickMultilines(evt){
		// don't trigger if the clicked element is 
		// the checkbox itself 
		if (! $(evt.target).is('input')){
			var chk = $(evt.currentTarget).find('.bind-milestone-dialog-check input');								
			var newstate = ! chk.is(':checked');
			chk.prop('checked', newstate);
		}		
	}
	
	function handleClickSingleline(evt){
		
		var table = $(evt.currentTarget).parents('table');
		
		// don't trigger if the clicked element is 
		// the checkbox itself 
		if (! $(evt.target).is('input')){
			var chk = $(evt.currentTarget).find('.bind-milestone-dialog-check input');								
			var newstate = ! chk.is(':checked');
			chk.prop('checked', newstate);
			table.find('.bind-milestone-dialog-check input').not(chk).prop('checked', false);
		}
		else{
			table.find('.bind-milestone-dialog-check input').not(evt.target).prop('checked', false);		
		}		
	}
	
	$.widget("squash.milestoneDialog", $.squash.formDialog, {
	
		options : {
			multilines : true,
			mustChoose : false
		},
		
		_create : function(){
			
			this._super();
			
			var self = this,
				element = $(this.element[0]);
			
			
			this.onOwnBtn('confirm', $.proxy(self.confirm, self));
			this.onOwnBtn('cancel', $.proxy(self.cancel, self));
			
			var table = element.find('.bind-milestone-dialog-table');
			

			this.initBlanketSelectors();
			
		},
		
		/*
		 * The table must init at first opening
		 */
		open : function(){
			this._super();
			
			var url = this.options.tableSource;
			var table = $(this.element[0]).find('.bind-milestone-dialog-table');
			
			
			multilines = this.options.multilines;
			if (!multilines) {
		  var tableLength = $('.milestone-panel-table >tbody >tr >td').length;
		  confirmLabel = (tableLength > 1) ? translator.get('label.Replace') : translator.get('label.Confirm');
		  $(".bind-milestone-dialog-confirm").val(confirmLabel);
			}
			else {
			confirmLabel = translator.get('label.Confirm');
			$(".bind-milestone-dialog-confirm").val(confirmLabel);		
			}
			// if initialized -> refresh
			if (!! table.data('squashtableInstance')){
				
				table.squashTable()._fnAjaxUpdate();
				
				table.one('draw.dt', function(){table.squashTable().refresh();});
				

			}
			// else -> init
			else{
				this._configureTable();
			}
				
		},
		
		/*
		 * One major aspect of this table is that, if no initialData is provided,
		 * the table initialization must be defered to when the dialog opens.
		 * 
		 */
		_configureTable : function(){
			
			var table = $(this.element[0]).find('.bind-milestone-dialog-table'),
				multilines = this.options.multilines;
			
			var selecthandler = (multilines) ? handleClickMultilines : handleClickSingleline,
				inputType = (multilines) ? "checkbox" : "radio";

			table.on('click', '>tbody>tr', selecthandler);			
			
			var tblCnf = {
					sAjaxSource : this.options.tableSource, 
					bServerSide : false,
					fnDrawCallback : function(){
						table.find('>tbody>tr>td.bind-milestone-dialog-check').each(function(){
							$(this).html('<input type="'+inputType+'"/>');
						});
						table.find('>tbody>tr').addClass('cursor-pointer');
					}
				},
	
				squashCnf = {};
			
			table.squashTable(tblCnf, squashCnf);
			
			table.squashTable().refresh();
		},
				
		confirm : function(){
			var self = this;
			var table = $(this.element[0]).find('.bind-milestone-dialog-table').squashTable();
			var checks = table.find('>tbody>tr>td.bind-milestone-dialog-check input:checked');
			var ids = [];
			
			checks.each(function(){
				var r = this.parentNode.parentNode;
				var id = table.fnGetData(r)['entity-id'];
				ids.push(id);
			});
			
			// if nothing selected : 
			if (ids.length===0){
				if (this.options.mustChoose){					
					notification.showError(translator.get('message.EmptyTableSelection'));						
				}
				else{
					self.close();
				}
				return;
			}
			
			
			if ( !! this.options.milestonesURL){
				var url = this.options.milestonesURL + '/'+ ids.join(',');
				
				$.ajax({
					url : url,
					type : 'POST'
				})
				.success(function(){
					eventBus.trigger('node.bindmilestones', {
						identity : self.options.identity,
						milestones : ids
					});
					self.close();
				});
			}
			else{
				// just trigger the event
				eventBus.trigger('node.bindmilestones', {
					identity : self.options.identity,
					milestones : ids
				});
				self.close();
			}
			
		},
		
		cancel : function(){
			this.close();
		},
		
		initBlanketSelectors : function(){
			
			var element = $(this.element[0]),
				table = element.find('.bind-milestone-dialog-table');
			
			// if multiline -> init the links
			if (this.options.multilines){			
				element.on('click', '.bind-milestone-dialog-selectall', function(){
					table.find('>tbody>tr>td.bind-milestone-dialog-check input').prop('checked', true);
				});			
				
				element.on('click', '.bind-milestone-dialog-selectnone', function(){
					table.find('>tbody>tr>td.bind-milestone-dialog-check input').prop('checked', false);				
				});			
				
				element.on('click', '.bind-milestone-dialog-invertselect', function(){
					table.find('>tbody>tr>td.bind-milestone-dialog-check input').each(function(){
						this.checked = ! this.checked;					
					});				
				});
			}
			// if not multilines -> hide the links
			else{
				element.find('.bind-milestone-dialog-selectors').hide();
			}
		}
		
	});
});