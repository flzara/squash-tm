package org.squashtest.tm.domain.testcase;

import org.squashtest.tm.core.foundation.lang.Wrapped;

public class GetKindTestCaseVisitor implements TestCaseVisitor {

	private Wrapped<TestCaseKind> kind = new Wrapped<>();

	@Override
	public void visit(TestCase testCase) {
		kind.setValue(TestCaseKind.STANDARD);
	}

	@Override
	public void visit(KeywordTestCase keywordTestCase) {
		kind.setValue(TestCaseKind.KEYWORD);
	}

	@Override
	public void visit(ScriptedTestCase scriptedTestCase) {
		kind.setValue(TestCaseKind.GHERKIN);
	}

	public TestCaseKind getKind() {
		return kind.getValue();
	}
}
