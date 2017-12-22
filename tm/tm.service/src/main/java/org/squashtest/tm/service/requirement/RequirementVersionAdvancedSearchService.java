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
package org.squashtest.tm.service.requirement;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;



public interface RequirementVersionAdvancedSearchService {



	//Querying


	PagedCollectionHolder<List<RequirementVersion>> searchForRequirementVersions(AdvancedSearchModel searchModel, PagingAndMultiSorting paging, MessageSource source, Locale locale);

	List<RequirementVersion> searchForRequirementVersions(AdvancedSearchModel model, Locale locale);

	List<String> findAllUsersWhoCreatedRequirementVersions(List<Long> idList);

	List<String> findAllUsersWhoModifiedRequirementVersions(List<Long> idList);


}
