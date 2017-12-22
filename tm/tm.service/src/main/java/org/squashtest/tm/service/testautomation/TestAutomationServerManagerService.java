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
package org.squashtest.tm.service.testautomation;

import java.net.URL;
import java.util.List;

import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.internal.repository.hibernate.NonUniqueEntityException;

public interface TestAutomationServerManagerService {

	// *********************** entity management *******************

	TestAutomationServer findById(long serverId);

	/**
	 * Will persist a new {@link TestAutomationServer}.
	 * 
	 * @param server
	 *            : the server to persist
	 * @throws NonUniqueEntityException
	 *             if the given server happen to exist already.
	 */
	void persist(TestAutomationServer server);

	boolean hasBoundProjects(long serverId);

	boolean hasExecutedTests(long serverId);

	/**
	 * <p>
	 * <b style="color:red">Warning :</b> When using this method there is a risk that your Hibernate beans are not up to
	 * date. Use {@link Session#clear()} and {@link Session#refresh(Object)} to make sure your they are.
	 * </p>
	 * 
	 * @param serverId
	 */
	void deleteServer(long serverId);

	/**
	 * <p>
	 * <b style="color:red">Warning :</b> When using this method there is a risk that your Hibernate beans are not up to
	 * date. Use {@link Session#clear()} and {@link Session#refresh(Object)} to make sure your they are.
	 * </p>
	 * 
	 * @param serverId
	 */
	void deleteServer(List<Long> serverId);

	List<TestAutomationServer> findAllOrderedByName();

	Page<TestAutomationServer> findSortedTestAutomationServers(Pageable pageable);

	// *********************** Properties mutators ****************************

	void changeURL(long serverId, URL url);

	/**
	 * Will change the name of the server.
	 * 
	 * @param serverId
	 *            : id of the {@link TestAutomationServer} to change the name of
	 * @param newName
	 *            : the new name for the concerned server
	 * @throws NameAlreadyInUseException
	 *             if name already exists in database
	 */
	void changeName(long serverId, String newName);

	void changeLogin(long serverId, String login);

	void changePassword(long serverId, String password);

	void changeDescription(long serverId, String description);

	void changeManualSlaveSelection(long serverId, boolean hasSlaves);

}
