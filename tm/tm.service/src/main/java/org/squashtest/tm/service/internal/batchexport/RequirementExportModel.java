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
package org.squashtest.tm.service.internal.batchexport;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CoverageModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CustomField;

public class RequirementExportModel {
	
	private List<RequirementModel> requirementsModels = new LinkedList<>();

	private List<CoverageModel> coverages = new LinkedList<>();
	
	private List<RequirementLinkModel> reqLinks = new LinkedList<>();


	public RequirementExportModel() {
		super();
	}
	
	
	
	
	public List<CoverageModel> getCoverages() {
		return coverages;
	}

	public void setCoverages(List<CoverageModel> coverages) {
		this.coverages = coverages;
	}

	public List<RequirementModel> getRequirementsModels() {
		return requirementsModels;
	}

	public void setRequirementsModels(List<RequirementModel> requirementsModels) {
		this.requirementsModels = requirementsModels;
	}
	
	

	public List<RequirementLinkModel> getReqLinks() {
		return reqLinks;
	}

	public void setReqLinks(List<RequirementLinkModel> reqLinks) {
		this.reqLinks = reqLinks;
	}





	public interface RequirementPathSortable {
		String getProjectName();

		String getPath();

		int getRequirementVersionNumber();

	}

	public static final class RequirementModel implements RequirementPathSortable {

		public static final Comparator<RequirementPathSortable> COMPARATOR = new Comparator<RequirementPathSortable>() {
			@Override
			public int compare(RequirementPathSortable o1, RequirementPathSortable o2) {

				int path1length = PathUtils.splitPath(o1.getPath()).length;
				int path2length = PathUtils.splitPath(o2.getPath()).length;
				// alpha order
				int compareProjectName = o1.getProjectName().compareTo(o2.getProjectName());

				// order by ascending path size
				int comparePathSize = Integer.compare(path1length, path2length);

				// alpha order
				int comparePathName = o1.getPath().compareTo(o2.getPath());

				// order by ascending version number
				int compareVersionNumber = Integer.compare(o1.getRequirementVersionNumber(),
						o2.getRequirementVersionNumber());

				return compareProjectName != 0 ? compareProjectName
						: comparePathSize != 0 ? comparePathSize
								: comparePathName != 0 ? comparePathName : compareVersionNumber;
			}
		};

		private Long id;
		private Long requirementId;
		private Long projectId;
		private String projectName;
		private String path;
		private int requirementIndex;
		private int requirementVersionNumber;
		private String reference;
		private String name;
		private RequirementCriticality criticality;
		private String categoryCode;
		private RequirementStatus status;
		private String description;
		private Long requirementVersionCoveragesSize;
		private Long attachmentListSize;
		private Date createdOn;
		private String createdBy;
		private Date lastModifiedOn;
		private String lastModifiedBy;
		private String milestonesLabels;
		private List<CustomField> cufs = new LinkedList<>();

		// That monster constructor will be used by Hibernate in a hql query.
		// Note that attributes not present in the hql request mustn't be in this constructor
		// as Hibernate use index to map the result set to this object attribute.
		public RequirementModel(Long id, Long requirementId, Long projectId,
				String projectName, int requirementVersionNumber,
				String reference, String name,
				RequirementCriticality criticality, String categoryCode,
				RequirementStatus status, String description,
				Long requirementVersionCoveragesSize, Long attachmentListSize,
				Date createdOn, String createdBy, Date lastModifiedOn,
				String lastModifiedBy, String milestonesLabels) {
			super();
			this.id = id;
			this.requirementId = requirementId;
			this.projectId = projectId;
			this.projectName = projectName;
			this.requirementVersionNumber = requirementVersionNumber;
			this.reference = reference;
			this.name = name;
			this.criticality = criticality;
			this.categoryCode = categoryCode;
			this.status = status;
			this.description = description;
			this.requirementVersionCoveragesSize = requirementVersionCoveragesSize;
			this.attachmentListSize = attachmentListSize;
			this.createdOn = createdOn;
			this.createdBy = createdBy;
			this.lastModifiedOn = lastModifiedOn;
			this.lastModifiedBy = lastModifiedBy;
			this.milestonesLabels = milestonesLabels;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getRequirementId() {
			return requirementId;
		}

		public void setRequirementId(Long requirementId) {
			this.requirementId = requirementId;
		}

		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		@Override
		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}

		@Override
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public int getRequirementIndex() {
			return requirementIndex;
		}

		public void setRequirementIndex(int requirementIndex) {
			this.requirementIndex = requirementIndex;
		}

		public String getReference() {
			return reference;
		}

		public void setReference(String reference) {
			this.reference = reference;
		}

		@Override
		public int getRequirementVersionNumber() {
			return requirementVersionNumber;
		}

		public void setRequirementVersionNumber(int requirementVersionNumber) {
			this.requirementVersionNumber = requirementVersionNumber;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public RequirementCriticality getCriticality() {
			return criticality;
		}

		public void setCriticality(RequirementCriticality criticality) {
			this.criticality = criticality;
		}

		public String getCategoryCode() {
			return categoryCode;
		}

		public void setCategoryCode(String categoryCode) {
			this.categoryCode = categoryCode;
		}

		public RequirementStatus getStatus() {
			return status;
		}

		public void setStatus(RequirementStatus status) {
			this.status = status;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Long getRequirementVersionCoveragesSize() {
			return requirementVersionCoveragesSize;
		}

		public void setRequirementVersionCoveragesSize(
				Long requirementVersionCoveragesSize) {
			this.requirementVersionCoveragesSize = requirementVersionCoveragesSize;
		}

		public Long getAttachmentListSize() {
			return attachmentListSize;
		}

		public void setAttachmentListSize(Long attachmentListSize) {
			this.attachmentListSize = attachmentListSize;
		}

		public Date getCreatedOn() {
			return createdOn;
		}

		public void setCreatedOn(Date createdOn) {
			this.createdOn = createdOn;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}

		public Date getLastModifiedOn() {
			return lastModifiedOn;
		}

		public void setLastModifiedOn(Date lastModifiedOn) {
			this.lastModifiedOn = lastModifiedOn;
		}

		public String getLastModifiedBy() {
			return lastModifiedBy;
		}

		public void setLastModifiedBy(String lastModifiedBy) {
			this.lastModifiedBy = lastModifiedBy;
		}

		public String getMilestonesLabels() {
			return milestonesLabels;
		}

		public void setMilestonesLabels(String milestonesLabels) {
			this.milestonesLabels = milestonesLabels;
		}

		public List<CustomField> getCufs() {
			return cufs;
		}

		public void setCufs(List<CustomField> cufs) {
			this.cufs = cufs;
		}

		@Override
		public String toString() {
			return "RequirementModel [id=" + id + ", requirementId="
					+ requirementId + ", projectId=" + projectId
					+ ", projectName=" + projectName + ", path=" + path
					+ ", requirementVersionNumber=" + requirementVersionNumber
					+ ", reference=" + reference + ", name=" + name
					+ ", criticality=" + criticality + ", categoryCode="
					+ categoryCode + ", status=" + status + ", description="
					+ description + ", requirementVersionCoveragesSize="
					+ requirementVersionCoveragesSize + ", attachmentListSize="
					+ attachmentListSize + ", createdOn=" + createdOn
					+ ", createdBy=" + createdBy + ", lastModifiedOn="
					+ lastModifiedOn + ", lastModifiedBy=" + lastModifiedBy
					+ ", milestonesLabels=" + milestonesLabels + "]";
		}

	}
	
	

	public static final class RequirementLinkModel{

		public static final Comparator<RequirementLinkModel> REQ_LINK_COMPARATOR = new Comparator<RequirementExportModel.RequirementLinkModel>() {
			
			// SONAR wont like the four-return clauses I guess
			@Override
			public int compare(RequirementLinkModel o1, RequirementLinkModel o2) {
				
				int comp = o1.getReqPath().compareTo(o2.getReqPath());
				if (comp != 0){
					return comp;
				}
				
				comp = o1.getReqVersion() - o2.getReqVersion();
				if (comp != 0){
					return comp;
				}
				
				comp = o1.getRelReqPath().compareTo(o2.getRelReqPath());
				if (comp != 0){
					return comp;
				}
				
				comp = o1.getRelReqVersion() - o2.getRelReqVersion();
				return comp;
			}
			
		};
		
		// those attributes will be set by Hibernate (they are part of the constructor)
		
		/**
		 * Id of the requirement for the requirement version under consideration. Not exported in the final 
		 * excel file, but useful to resolve the requirement path later on.
		 */
		private Long reqId;		
		/**
		 * Id of the requirement for the related requirement version. Not exported in the final 
		 * excel file, but useful to resolve the requirement path later on.
		 */
		private Long relReqId;	
		/**
		 * The version number of the requirement version under consideration
		 */
		private int reqVersion;
		/**
		 * The version number of the related requirement.
		 */
		private int relReqVersion;
		
		/**
		 * The role of the related requirement version. Corresponds to {@link RequirementVersionLinkType#getRole2Code()}.
		 */
		private String relatedReqRole;	
		
		/**
		 * The path of the requirement of the requirement version under consideration. Will not be extracted directly from 
		 * the database, must be set manually.
		 */
		private String reqPath;
		
		/**
		 * The path of the requirement of the related requirement version. Will not be extracted directly from 
		 * the database, must be set manually.
		 */
		private String relReqPath;
		
		
		/**
		 * Constuctor used by Hibernate
		 * 
		 * @param reqId
		 * @param relReqId
		 * @param reqVersion
		 * @param relReqVersion
		 * @param relatedReqRole
		 */
		public RequirementLinkModel(Long reqId, Long relReqId, int reqVersion, int relReqVersion, String relatedReqRole) {
			super();
			this.reqId = reqId;
			this.relReqId = relReqId;
			this.reqVersion = reqVersion;
			this.relReqVersion = relReqVersion;
			this.relatedReqRole = relatedReqRole;
		}


		/**
		 * factory method for test purposes
		 * 
		 * @return
		 */
		public static RequirementLinkModel create(int reqVersion, int relReqVersion, String relatedReqRole, String reqPath,
				String relReqPath) {
			
			RequirementLinkModel model = new RequirementLinkModel(null, null, reqVersion, relReqVersion, relatedReqRole);
			
			model.setReqPath(reqPath);
			model.setRelReqPath(relReqPath);
			
			return model;
		}

		public String getReqPath() {
			return reqPath;
		}

		public void setReqPath(String reqPath) {
			this.reqPath = reqPath;
		}


		public String getRelReqPath() {
			return relReqPath;
		}


		public void setRelReqPath(String relReqPath) {
			this.relReqPath = relReqPath;
		}


		public Long getReqId() {
			return reqId;
		}


		public Long getRelReqId() {
			return relReqId;
		}


		public int getReqVersion() {
			return reqVersion;
		}


		public int getRelReqVersion() {
			return relReqVersion;
		}


		public String getRelatedReqRole() {
			return relatedReqRole;
		}
		
		
		@Override
		public String toString(){
			return new ToStringBuilder(this)
					.append("req-id", relReqId)
					.append("req-path", reqPath)
					.append("req-version", reqVersion)
					.append("rel-req-id", relReqId)
					.append("rel-req-path", relReqPath)
					.append("rel-req-version", relReqVersion)
					.append("rel-req-role", relatedReqRole)
					.toString();
		}
		

	}


}
