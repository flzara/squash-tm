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
package org.squashtest.tm.service.internal.infolist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.exception.customfield.CodeAlreadyExistsException;
import org.squashtest.tm.service.infolist.InfoListManagerService;
import org.squashtest.tm.service.infolist.IsBoundInfoListAdapter;
import org.squashtest.tm.service.internal.repository.InfoListDao;
import org.squashtest.tm.service.internal.repository.InfoListItemDao;

@Transactional
@Service("squashtest.tm.service.InfoListManagerService")
public class InfoListManagerServiceImpl implements InfoListManagerService {
	@PersistenceContext
	private EntityManager em;

	@Inject
	private InfoListDao infoListDao;

	@Inject
	private InfoListItemDao infoListItemDao;

	@Override
	public InfoList findById(Long id) {
		return infoListDao.findOne(id);
	}

	@Override
	public InfoList findByCode(String code) {
		return infoListDao.findByCode(code);
	}

	@Override
	public void changeDescription(long infoListId, String newDescription) {
		InfoList infoList = findById(infoListId);
		SystemInfoListCode.verifyModificationPermission(infoList);
		infoList.setDescription(newDescription);

	}

	@Override
	public void changeLabel(long infoListId, String newLabel) {
		InfoList infoList = findById(infoListId);
		SystemInfoListCode.verifyModificationPermission(infoList);
		infoList.setLabel(newLabel);

	}

	@Override
	public void changeCode(long infoListId, String newCode) {
		InfoList infoList = findById(infoListId);
		checkDuplicateCode(infoList, newCode);
		infoList.setCode(newCode);
	}

	private void checkDuplicateCode(InfoList infoList, String newCode) {
		if (StringUtils.equals(infoList.getCode(), newCode)) {
			return;
		}
		if (infoListDao.findByCode(newCode) != null) {
			throw new CodeAlreadyExistsException(infoList.getCode(), newCode, InfoList.class);
		}
	}

	@Override
	public void changeItemsPositions(long infoListId, int newIndex, List<Long> itemsIds) {

		InfoList infoList = findById(infoListId);
		SystemInfoListCode.verifyModificationPermission(infoList);

		List<InfoListItem> items = infoListItemDao.findAll(itemsIds);
		for (InfoListItem item : items) {
			infoList.removeItem(item);
		}
		infoList.addItems(newIndex, items);
	}

	@Override
	public boolean isUsedByOneOrMoreProject(long infoListId) {

		return infoListDao.isUsedByOneOrMoreProject(infoListId);
	}

	@Override
	public void remove(long infoListId) {

		InfoList infoList = infoListDao.findOne(infoListId);
		SystemInfoListCode.verifyModificationPermission(infoList);

		infoListDao.unbindFromProject(infoListId);
		infoListItemDao.unbindFromLibraryObjects(infoListId);

		for (InfoListItem item : infoList.getItems()) {
			infoListItemDao.delete(item);
		}

		infoListDao.delete(infoList);
	}

	@Override
	public List<InfoList> findAllUserLists() {
		List<InfoList> allList = infoListDao.findAllOrdered();
		List<InfoList> systemList = new ArrayList<>();
		for (SystemInfoListCode sysInfo : SystemInfoListCode.values()) {
			systemList.add(infoListDao.findByCode(sysInfo.getCode()));
		}
		allList.removeAll(systemList);
		return allList;
	}

	/**
	 * @see org.squashtest.tm.service.infolist.InfoListManagerService#remove(java.util.List)
	 */
	@Override
	public void remove(List<Long> ids) {
		for (long id : ids) {
			remove(id);
		}
	}

	/**
	 * @see org.squashtest.tm.service.infolist.InfoListManagerService#findAllWithBoundInfo()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IsBoundInfoListAdapter> findAllWithBoundInfo() {
		List<IsBoundInfoListAdapter> unbound = createBoundAdapters(infoListDao.findAllUnbound(), false);
		List<IsBoundInfoListAdapter> bound = createBoundAdapters(infoListDao.findAllBound(), true);

		SortedSet<IsBoundInfoListAdapter> res = new TreeSet<>(new Comparator<IsBoundInfoListAdapter>() {
			@Override
			public int compare(IsBoundInfoListAdapter kore, IsBoundInfoListAdapter sore) {
				return kore.getLabel().compareTo(sore.getLabel());
			}
		});

		res.addAll(bound);
		res.addAll(unbound);

		return filterSystemLists(res);
	}

	/**
	 * Returns a collection in the same (iterator) order as the given collection where systems lists have been filtered out.
	 *
	 */
	private List<IsBoundInfoListAdapter> filterSystemLists(Collection<IsBoundInfoListAdapter> lists) {
		List<IsBoundInfoListAdapter> res = new ArrayList<>(lists.size());

		for (IsBoundInfoListAdapter list : lists) {
			if (SystemInfoListCode.isNotSystem(list.getCode())) {
				res.add(list);
			}
		}

		return res;
	}

	private List<IsBoundInfoListAdapter> createBoundAdapters(List<InfoList> lists, boolean isBound) {
		List<IsBoundInfoListAdapter> adapted = new ArrayList<>(lists.size());
		for (InfoList list : lists) {
			adapted.add(new IsBoundInfoListAdapter(list, isBound));
		}

		return adapted;
	}

	/**
	 * @see org.squashtest.tm.service.infolist.InfoListManagerService#persist(org.squashtest.tm.domain.infolist.InfoList)
	 */
	@Override
	public InfoList persist(InfoList infoList) {
		infoListDao.save(infoList);

		for (InfoListItem item : infoList.getItems()) {
			infoListItemDao.save(item);
		}

		return infoList;
	}

	/**
	 * @see org.squashtest.tm.service.infolist.InfoListFinderService#findByUniqueProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public InfoList findByUniqueProperty(@NotNull String prop, @NotNull String value) {
		return (InfoList) em.unwrap(Session.class)
			.createCriteria(InfoList.class)
			.add(Restrictions.eq(prop, value))
			.uniqueResult();

	}

}
