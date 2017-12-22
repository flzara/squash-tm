/**
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
package org.squashtest.tm.bugtracker.advanceddomain;


/**
 * 
 * This class represents an arbitrary command that a widget of Squash UI may ask the bugtracker connector to perform in order to enhance its behaviour. 
 * It may happen for instance if {@link InputType.meta} contains certain values (such at {@link InputType#ONCHANGE}), that native Squash widget can handles 
 * (that are listed in {@link InputType})
 * 
 * 
 * @author bsiri
 *
 */
public class DelegateCommand {
	
	private String command;
	
	private Object argument;
	
	public DelegateCommand(){
		super();
	}
	
	public DelegateCommand(String command, Object argument){
		super();
		this.command = command;
		this.argument = argument;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Object getArgument() {
		return argument;
	}

	public void setArgument(Object argument) {
		this.argument = argument;
	}
	
	
	
}
