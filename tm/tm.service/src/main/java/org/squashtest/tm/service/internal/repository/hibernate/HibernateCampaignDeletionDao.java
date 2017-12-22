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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.service.internal.repository.CampaignDeletionDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Repository
public class HibernateCampaignDeletionDao extends HibernateDeletionDao
implements CampaignDeletionDao {



	@Override
	public void removeEntities(List<Long> entityIds) {
		if (!entityIds.isEmpty()) {

			for(Long entityId : entityIds){

				CampaignLibraryNode node = entityManager().getReference(CampaignLibraryNode.class, entityId);

				removeEntityFromParentLibraryIfExists(entityId, node);

				removeEntityFromParentFolderIfExists(entityId, node);

				if(node != null){
					entityManager().remove(node);
					entityManager().flush();
				}
			}


		}
	}

	private void removeEntityFromParentLibraryIfExists(Long entityId, CampaignLibraryNode node){
		Query query = getSession().getNamedQuery("campaignLibraryNode.findParentLibraryIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		CampaignLibrary library = (CampaignLibrary) query.uniqueResult();
		if(library != null){
			for (CampaignLibraryNode tcln : library.getContent()) {
				if (tcln.getId().equals(node.getId())) {
					library.removeContent(tcln);
					break;
				}
			}
		}
	}

	private void removeEntityFromParentFolderIfExists(Long entityId, CampaignLibraryNode node){
		Query query = getSession().getNamedQuery("campaignLibraryNode.findParentFolderIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		CampaignFolder folder = (CampaignFolder) query.uniqueResult();
		if(folder != null){
			for (CampaignLibraryNode tcln : folder.getContent()) {
				if (tcln.getId().equals(node.getId())) {
					folder.removeContent(tcln);
					break;
				}
			}
		}
	}

	@Override
	public List<Long>[] separateFolderFromCampaignIds(List<Long> originalIds) {
		List<Long> folderIds = new ArrayList<>();
		List<Long> campaignIds = new ArrayList<>();

		List<BigInteger> filtredFolderIds = executeSelectSQLQuery(
				NativeQueries.CAMPAIGNLIBRARYNODE_SQL_FILTERFOLDERIDS, "campaignIds", originalIds);

		for (Long oId : originalIds){
			if (filtredFolderIds.contains(BigInteger.valueOf(oId))){
				folderIds.add(oId);
			} else {
				campaignIds.add(oId);
			}
		}

		List<Long>[] result = new List[2];
		result[0] = folderIds;
		result[1] = campaignIds;

		return result;
	}

	@Override
	public void unbindFromMilestone(List<Long> campaignIds, Long milestoneId){

		if (! campaignIds.isEmpty()){
			Query query = getSession().createSQLQuery(NativeQueries.CAMPAIGN_SQL_UNBIND_MILESTONE);
			query.setParameterList("campaignIds", campaignIds, LongType.INSTANCE);
			query.setParameter("milestoneId", milestoneId);
			query.executeUpdate();
		}

	}

	@Override
	public List<Long> findCampaignsWhichMilestonesForbidsDeletion(List<Long> originalId) {
		if (! originalId.isEmpty()){
			MilestoneStatus[] lockedStatuses = new MilestoneStatus[]{ MilestoneStatus.PLANNED, MilestoneStatus.LOCKED};
			Query query = getSession().getNamedQuery("campaign.findCampaignsWhichMilestonesForbidsDeletion");
			query.setParameterList("campaignIds", originalId, LongType.INSTANCE);
			query.setParameterList("lockedStatuses", lockedStatuses);
			return query.list();
		}else{
			return new ArrayList<>();
		}
	}

	@Override
	public List<Long> findRemainingCampaignIds(List<Long> originalIds) {
		List<BigInteger> rawids = executeSelectSQLQuery(NativeQueries.CAMPAIGN_SQL_FINDNOTDELETED, "allCampaignIds", originalIds);
		List<Long> cIds = new ArrayList<>(rawids.size());
		for (BigInteger rid : rawids){
			cIds.add(rid.longValue());
		}
		return cIds;
	}

}
