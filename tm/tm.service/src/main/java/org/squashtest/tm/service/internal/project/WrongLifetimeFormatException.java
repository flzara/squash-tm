package org.squashtest.tm.service.internal.project;

import org.squashtest.tm.core.foundation.exception.ActionException;

public class WrongLifetimeFormatException extends ActionException {

	public WrongLifetimeFormatException(Exception cause) {
		super(cause);
	}

	@Override
	public String getI18nKey() {
		return "squashtm.action.exception.wrong-lifetime-format";
	}
}
