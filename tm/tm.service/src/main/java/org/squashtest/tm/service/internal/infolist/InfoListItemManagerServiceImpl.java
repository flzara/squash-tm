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

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.domain.infolist.SystemInfoListItemCode;
import org.squashtest.tm.domain.infolist.SystemListItem;
import org.squashtest.tm.service.annotation.CachableType;
import org.squashtest.tm.service.annotation.CacheResult;
import org.squashtest.tm.service.infolist.InfoListItemManagerService;
import org.squashtest.tm.service.infolist.InfoListManagerService;
import org.squashtest.tm.service.internal.repository.InfoListItemDao;

@Transactional
@Service("squashtest.tm.service.InfoListItemManagerService")
public class InfoListItemManagerServiceImpl implements InfoListItemManagerService {

	@Inject
	private InfoListItemDao itemDao;


	@Inject
	private InfoListManagerService infoListService;
	// ************* "Finder" methods **************** \\

	@Override
	public SystemListItem getSystemRequirementCategory() {
		return itemDao.getSystemRequirementCategory();
	}

	@Override
	public SystemListItem getSystemTestCaseNature() {
		return itemDao.getSystemTestCaseNature();
	}

	@Override
	public void changeCode(long infoListItemId, String newCode) {

		InfoListItem item = itemDao.findOne(infoListItemId);
		SystemInfoListItemCode.verifyModificationPermission(item);
		item.setCode(newCode);
	}

	@Override
	public void changeLabel(long infoListItemId, String newLabel) {
		InfoListItem item = itemDao.findOne(infoListItemId);
		SystemInfoListItemCode.verifyModificationPermission(item);
		item.setLabel(newLabel);
	}

	@Override
	public void changeDefault(long infoListItemId) {

		InfoListItem changedItem = itemDao.findOne(infoListItemId);
		SystemInfoListItemCode.verifyModificationPermission(changedItem);
		List<InfoListItem> items = changedItem.getInfoList().getItems();
		for(InfoListItem item : items){
			item.setDefault(false);
		}
		changedItem.setDefault(true);
	}



	@Override
	public void changeIcon(long infoListItemId, String icon) {
		InfoListItem item = itemDao.findOne(infoListItemId);
		SystemInfoListItemCode.verifyModificationPermission(item);
		item.setIconName(icon);

	}


	@Override
	public void addInfoListItem(long infoListId, InfoListItem item) {

		InfoList infoList = infoListService.findById(infoListId);
		SystemInfoListCode.verifyModificationPermission(infoList);

		if (infoList.getItems().isEmpty()) {
			item.setDefault(true);
		}
		item.setInfoList(infoList);
		itemDao.save(item);
		infoList.addItem(item);
	}

	@Override
	public SystemListItem getSystemTestCaseType() {
		return itemDao.getSystemTestCaseType();
	}

	@Override
	public InfoListItem findById(Long id){
		return itemDao.findOne(id);
	}

	@Override
	public InfoListItem findByCode(String code){
		return itemDao.findByCode(code);
	}

	@Override
	public InfoListItem findReference(ListItemReference reference){
		return itemDao.findByCode(reference.getCode());
	}

	@Override
	public InfoListItem findDefaultRequirementCategory(long projectId) {
		return itemDao.findDefaultRequirementCategory(projectId);
	}

	@Override
	public InfoListItem findDefaultTestCaseNature(long projectId) {
		return itemDao.findDefaultTestCaseNature(projectId);
	}

	@Override
	public InfoListItem findDefaultTestCaseType(long projectId) {
		return itemDao.findDefaultTestCaseType(projectId);
	}

	@Override
	@CacheResult(type = CachableType.CATEGORY)
	public boolean isCategoryConsistent(long projectId, String itemCode) {
		return itemDao.isCategoryConsistent(projectId, itemCode);
	}

	@Override
	@CacheResult(type = CachableType.NATURE)
	public boolean isNatureConsistent(long projectId, String itemCode) {
		return itemDao.isNatureConsistent(projectId, itemCode);
	}

	@Override
	@CacheResult(type = CachableType.TYPE)
	public boolean isTypeConsistent(long projectId, String itemCode) {
		return itemDao.isTypeConsistent(projectId, itemCode);
	}

	@Override
	public boolean isUsed(long infoListItemId) {

		return itemDao.isUsed(infoListItemId);
	}

	@Override
	public void removeInfoListItem(long infoListItemId, long infoListId) {
		InfoList infoList = infoListService.findById(infoListId);
		InfoListItem item = findById(infoListItemId);
		InfoListItem defaultItem = infoList.getDefaultItem();

		if (item.references(defaultItem)){
			throw new IllegalArgumentException("cannot delete this item : it is default item of its list");
		}

		infoList.removeItem(item);
		itemDao.removeInfoListItem(infoListItemId, defaultItem);
		itemDao.delete(item);
	}

}
