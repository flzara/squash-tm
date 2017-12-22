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
package org.squashtest.csp.core.bugtracker.internal.mantis;

import java.rmi.RemoteException;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.ProjectNotFoundException;


/**
 * that class will convert a Mantis RemoteException to a BugTrackerRemoteException.
 *
 * Its goal is to :
 * 	- provide a more detailed message for an user,
 *  - internationalize it,
 *
 * @author bsiri
 * @reviewed-on 2011/11/23
 */

/*
 *
 * as a developper, each RemoteException will be mapped by two keys in the MessageSource :
 * 		- the key that returns a String we will compare to remoteException.getMessage(), aka Mantis Key
 * 		- the key to look up for the internationalized version, aka Squash Key
 *
 */
@Component
public class MantisExceptionConverter {
	private interface MantisMessageKeys {
		String WRONG_CREDENTIAL = "exception.remote.accessdenied";
		String MANDATORY_SUMMARY_REQUIRED = "exception.remote.validation.mandatory.summary";
		String MANDATORY_DESCRIPTION_REQUIRED = "exception.remote.validation.mandatory.description";
		String ISSUE_NOT_FOUND = "exception.remote.notfound.issue";
	}

	private interface SquashMessageKeys {
		String WRONG_CREDENTIAL = "exception.squash.accessdenied";
		String MANDATORY_SUMMARY_REQUIRED = "exception.squash.validation.mandatory.summary";
		String MANDATORY_DESCRIPTION_REQUIRED = "exception.squash.validation.mandatory.description";
		String ISSUE_NOT_FOUND = "exception.squash.notfound.issue";

		String UNKNOWN_EXCEPTION = "exception.squash.unknownexception";
	}

	@Inject @Named("mantisConnectorMessageSource")
	private MessageSource messageSource;


	/* *************** keys that should match the Mantis error messages. Their initial values will hopefully match
	 * the error messages if the key wasn't found in the message source. ********************* */
	private String remoteWrongCredential = "Access denied";
	private String remoteSummaryRequired = "Mandatory field \'summary\'";
	private String remoteDescriptionRequired = "Mandatory field \'description\'";
	private String remoteIssueNotFound = "Issue does not exist";


	public MantisExceptionConverter(){
		super();
	}

	private Locale getLocale(){
		return LocaleContextHolder.getLocale();
	}

	public BugTrackerRemoteException convertException(RemoteException remoteException){

		BugTrackerRemoteException exception = setIfAccessDenied(remoteException);

		if (exception == null){
			exception = setIfMandatorySummaryNotSet(remoteException);
		}
		if (exception == null){
			exception = setIfMandatoryDescriptionNotSet(remoteException);
		}
		if (exception == null){
			exception = setIssueNotFoundException(remoteException);
		}
		if (exception == null){
			exception = setUnknownException(remoteException);
		}

		return exception;
	}


	public ProjectNotFoundException makeProjectNotFound(String projName){
		String translation = messageSource.getMessage("exception.squash.notfound.project", new Object[]{projName}, getLocale());
		return new ProjectNotFoundException(translation, null);
	}


	/* ********************* private stuffs ************************************** */




	/* that init code will map the Mantis poor error messages to the appropriate
	 * fields above.
	 *
	 * if not found, the fields will be set with default value that (hopefully) match
	 * the default messages from Mantis Server.
	 *
	 * The locale is irrelevant yet.
	 *
	 * as a developper, you should feed that constructor with more labels when you meet uncovered RemoteExceptions
	 */
	@PostConstruct
	public void init(){
		Locale locale = Locale.getDefault();

		remoteWrongCredential = messageSource.getMessage(MantisMessageKeys.WRONG_CREDENTIAL, null, remoteWrongCredential,locale);
		remoteSummaryRequired = messageSource.getMessage(MantisMessageKeys.MANDATORY_SUMMARY_REQUIRED, null, remoteSummaryRequired, locale);
		remoteDescriptionRequired = messageSource.getMessage(MantisMessageKeys.MANDATORY_DESCRIPTION_REQUIRED, null, remoteDescriptionRequired, locale);
		remoteIssueNotFound = messageSource.getMessage(MantisMessageKeys.ISSUE_NOT_FOUND, null, remoteIssueNotFound, locale);
	}

	private BugTrackerRemoteException setIfAccessDenied(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.equals(remoteWrongCredential )){
			String translation = messageSource.getMessage(SquashMessageKeys.WRONG_CREDENTIAL, null, getLocale());
			return new BugTrackerNoCredentialsException(translation, remoteException);
		}
		return null;
	}

	private BugTrackerRemoteException setIfMandatorySummaryNotSet(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.contains(remoteSummaryRequired )){
			String translation = messageSource.getMessage(SquashMessageKeys.MANDATORY_SUMMARY_REQUIRED, null, getLocale());
			return new BugTrackerRemoteException(translation, remoteException);
		}
		return null;
	}

	private BugTrackerRemoteException setIfMandatoryDescriptionNotSet(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.contains(remoteDescriptionRequired )){
			String translation = messageSource.getMessage(SquashMessageKeys.MANDATORY_DESCRIPTION_REQUIRED, null, getLocale());
			return new BugTrackerRemoteException(translation, remoteException);
		}
		return null;
	}

	private BugTrackerRemoteException setIssueNotFoundException(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.contains(remoteIssueNotFound )){
			String translation = messageSource.getMessage(SquashMessageKeys.ISSUE_NOT_FOUND, null, getLocale());
			return new BugTrackerNotFoundException(translation, remoteException);
		}
		return null;
	}


	public BugTrackerRemoteException newIssueNotFoundException(){
		String translation = messageSource.getMessage(SquashMessageKeys.ISSUE_NOT_FOUND, null, getLocale());
		return new BugTrackerNotFoundException(translation, null);
	}

	private BugTrackerRemoteException setUnknownException(RemoteException remoteException){
		String translation = messageSource.getMessage(SquashMessageKeys.UNKNOWN_EXCEPTION, null, getLocale());
		return new BugTrackerRemoteException(translation+remoteException.getMessage(), remoteException );
	}

	public String getIssueNotFoundMsg() {

		return messageSource.getMessage("interface.table.bug-in-error", null, getLocale());
	}

}
