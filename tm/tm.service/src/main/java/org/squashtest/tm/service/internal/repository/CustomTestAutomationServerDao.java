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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.squashtest.tm.service.internal.repository;

/**
 *
 * @author bsiri
 */
public interface CustomTestAutomationServerDao {
    
    
	/**
	 * Checks if the {@link TestAutomationServer} is bound to at least one {@link TestAutomationProject}
	 * 
	 * @param serverId
	 *            : the id of the concernedTestAutomationServer
	 * @return : true if the TestAutomationServer is bound to a TA-project
	 */
	boolean hasBoundProjects(long serverId);
    
	/**
	 * Will delete the given {@linkplain TestAutomationServer} and dereference it from TM {@linkplain Project}s.
	 * <p>
	 * <b style="color:red">Warning :</b> When using this method there is a risk that your Hibernate beans are not up to
	 * date. Use {@link Session#clear()} and {@link Session#refresh(Object)} to make sure your they are.
	 * </p>
	 * 
	 * @param serverId
	 *            the id of the {@linkplain TestAutomationServer} to delete.
	 */
	void deleteServer(long serverId);
}
