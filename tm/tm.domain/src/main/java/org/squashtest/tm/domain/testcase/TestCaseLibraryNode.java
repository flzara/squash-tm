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
package org.squashtest.tm.domain.testcase;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;

import org.squashtest.tm.domain.SelfClassAware;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.library.GenericLibraryNode;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

/**
 * An organizational element ot the {@link TestCaseLibrary}
 *
 * @author Gregory Fouquet
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Auditable
public abstract class TestCaseLibraryNode extends GenericLibraryNode implements SelfClassAware {
	@Id
	@Column(name = "TCLN_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "test_case_library_node_tcln_id_seq")
	@SequenceGenerator(name = "test_case_library_node_tcln_id_seq", sequenceName = "test_case_library_node_tcln_id_seq", allocationSize = 1)
	private Long id;

	public TestCaseLibraryNode() {
		super();
	}

	@Override
	public Long getId() {
		return id;
	}

	public abstract void accept(TestCaseLibraryNodeVisitor visitor);



	@Override
	@AclConstrainedObject
	public Library<?> getLibrary() {
		return getProject().getTestCaseLibrary();
	}

	public Set<Attachment> getAllAttachments() {
		return getAttachmentList().getAllAttachments();
	}

	@Override
	protected Class<? extends GenericLibraryNode> getGenericNodeClass() {
		return TestCaseLibraryNode.class;
	}
	
}
