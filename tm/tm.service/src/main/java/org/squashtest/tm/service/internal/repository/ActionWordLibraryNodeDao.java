package org.squashtest.tm.service.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;

public interface ActionWordLibraryNodeDao extends JpaRepository<ActionWordLibraryNode, Long> {
}
