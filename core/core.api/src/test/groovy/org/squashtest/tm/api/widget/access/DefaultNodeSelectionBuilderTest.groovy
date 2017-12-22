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
package org.squashtest.tm.api.widget.access;

import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.squashtest.tm.api.security.acls.Permission;
import org.squashtest.tm.api.widget.TreeNodeType;
import org.squashtest.tm.api.widget.access.AnyNode;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class DefaultNodeSelectionBuilderTest extends Specification {
	DefaultNodeSelectionBuilder builder = new DefaultNodeSelectionBuilder(SelectionMode.MULTIPLE_SELECTION)
	
	def "should create rule for multiple selection of test case with create permission"() {
		when:
		def res = builder.nodePermission(TreeNodeType.TEST_CASE, Permission.CREATE).build()
		
		then:
		res.selectionMode == SelectionMode.MULTIPLE_SELECTION
		res.rules*.nodeType == [TreeNodeType.TEST_CASE]
		res.rules*.permission == [Permission.CREATE]
	}
	
	def "should create rule for multiple selection of test case with create permission or folder with read permission"() {
		when:
		def res = builder.nodePermission(TreeNodeType.TEST_CASE, Permission.CREATE)
			.or().nodePermission(TreeNodeType.FOLDER, Permission.READ).build();
		
		then:
		res.selectionMode == SelectionMode.MULTIPLE_SELECTION
		res.rules*.nodeType.containsAll([TreeNodeType.TEST_CASE, TreeNodeType.FOLDER])
		res.rules*.permission.containsAll([Permission.CREATE, Permission.READ])
	}

	def "should create rule for multiple selection of any nodes with any permission"() {
		when:
		def res = builder.anyNode().build();
		
		then:
		res.selectionMode == SelectionMode.MULTIPLE_SELECTION
		res.rules*.class == [AnyNode]
		res.rules*.permission == [Permission.ANY]
	}

	def "should create rule for multiple selection of any nodes with admin permission"() {
		when:
		def res = builder.anyNode(Permission.ADMIN).build();
		
		then:
		res.selectionMode == SelectionMode.MULTIPLE_SELECTION
		res.rules*.class == [AnyNode]
		res.rules*.permission == [Permission.ADMIN]
	}
}
