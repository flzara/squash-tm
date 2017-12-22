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
/**
<p>
	This package contains all the relevant concepts around test automation. But you already knew that, let's now talk about 
	why this package has its own doc.
</p>

<p>
	{@link org.squashtest.tm.domain.testautomation.TestAutomationServer}, {@link org.squashtest.tm.domain.testautomation.TestAutomationProject}
	etc represent remote entities. Those entities aren't managed by Squash TM, and Squash TM doesn't manage permanent synchronization with them.
	Rather, they are considered as detached entities : whenever Squash TM fetches an entity from a remote endpoint it must consider that it could 
	either already be represented in its database, either be completely new. 

</p>

<p>
	To tackle the synchronization-related problems with the remote server, and also ensure that updates doesn't break 
	the history for past executions, instances of TestAutomationServer and TestAutomationProject are immutable and a new
	instance will be persisted if an existing instance must be updated. 
</p>

<p>
	TestAutomation-X entities in this package will be considered equal either ID, either by content. First, their API normally forbid to ever 
	set their ID : no setters and no constructor allows that. One can only read them, unless you use reflection. 
	Since the user code cannot set the ID the only instances having non null ID are those created by the system. It ensures that those instances 
	are consistent with the database, and the system can then trust them. 
</p>

<p>
	Second, if an instance has no ID set, when asked to the system will look for a similar instance in the database and if found return it. 
	This is how the usercode can reattach the remote entities it just fetched to the ones existing in Squash TM.
</p>

*/
package org.squashtest.tm.domain.testautomation;