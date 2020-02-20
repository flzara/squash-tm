package org.squashtest.tm.service.testcase.scripted;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Transactional(readOnly = true)
@DynamicManager(name="squashtest.tm.service.ScriptedTestCaseFinder", entity = ScriptedTestCase.class)
public interface ScriptedTestCaseFinder {

	/**
	 * Find scripted test case by id.
	 * @param scriptedTestCaseId id of the scripted test case
	 * @return The requested ScriptedTestCase
	 */
	@PostAuthorize("hasPermission(returnObject , 'READ')" + OR_HAS_ROLE_ADMIN)
	ScriptedTestCase findById(long scriptedTestCaseId);
}
