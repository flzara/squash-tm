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
define(function(){
	

	function toArrayOfFieldValue(array){
		var res = [];
		for (var i=0;i<array.length;i++){
			var item = array[i];
			var converted = (item instanceof FieldValue) ? item : new FieldValue(item);
			res.push(converted);
		}
		return res;		
	}
	
	
	/*
	 * This constructor accepts either 1 or 3 arguments. The 3 arguments are documented as below, 
	 * the 1 argument constructor expects a basic javascript object. 
	 */
	
	function FieldValue(objectOrId, typename, value){
		this.id = "";
		this.scalar = "";
		this.composite = [];
		this.custom = null;
		this.typename = "";
		
		//1 argument constructor
		if (arguments.length==1){
			var o = objectOrId;
			if (!!o.id){
				this.id = o.id;
			}
			if (!!o.scalar){
				this.scalar = o.scalar;
			}
			if (!!o.composite){
				this.composite = toArrayOfFieldValue(o.composite);
			}
			if (!!o.custom){
				this.custom = o.custom;
			}
			if (!!o.typename){
				this.typename = o.typename;
			}
		}
		
		//3 arguments constructor
		else{
			this.id = objectOrId;
			this.typename = typename;
			if (value instanceof Array){
				this.composite = toArrayOfFieldValue(value);
			}
			else{
				this.scalar = value;
			}			
		}
		

		// *********** the methods now ***********
				
		this._getName = function(){
			
			if (this.composite.length>0){
				var res="";
				for (var i=0;i<this.composite.length;i++){
					res+= this.composite[i].getName() + ", ";
				}
				return res;				
			}
			else{			
				return this.scalar+", ";
			}		
		};
		
		this.getName = function(){
			return this._getName().replace(/,\s*$/, '');
		};

	}	
	
	
	return FieldValue;
});
	

	