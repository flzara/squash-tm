package org.squashtest.tm.service.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.bdd.ActionWord;

public interface ActionWordDao extends JpaRepository<ActionWord, Long> {

	@Query("from ActionWord where word = :word")
	ActionWord findByWord(@Param("word") String researchedWord);
}
