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
package org.squashtest.tm.service.internal.deletion

import javax.inject.Inject

import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.requirement.RequirementNodeDeletionHandler
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder

@UnitilsSupport
@Transactional
@Rollback
public class RequirementNodeDeletionHandlerIT extends DbunitServiceSpecification {

    @Inject
    private RequirementNodeDeletionHandler deletionHandler

    @Inject
    private RequirementLibraryNavigationService reqNavService

    @Inject
    private RequirementDao reqDao

    @Inject
    private TestCaseDao testCaseDao

    @Inject
    private ActiveMilestoneHolder milestoneHolder

    //fixes the problem with circular dependencies between req and reqversion
    def setCurrentVersion(){
            [
                    [-112, -11],
                    [-121, -12],
                    [-123, -13],
                    [-124, -14],
                    [-125, -15],
                    [-3, -3],
                    [-31, -31],
                    [-32, -32],
                    [-311, -311],
            ].collect({"update REQUIREMENT set CURRENT_VERSION_ID = ${it[0]} where RLN_ID = ${it[1]}"})
            .each({
                    getSession().createSQLQuery(it).executeUpdate()
            })

    }
    
    def cleanup(){
        milestoneHolder.clearContext()
    }


    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
    def "should delete the requirement and cascade to its versions"(){

            setup : 
            setCurrentVersion()
        
            when :
            def result = deletionHandler.deleteNodes([-11L])
            flushAndClear()

            then :
            result.removed*.resid.containsAll([-11L])

            allDeleted("CustomFieldValue", [-1111L, -1112L, -1121L, -1122L])		
            allDeleted("RequirementVersion", [-111L, -112L])
            allDeleted("AttachmentList", [-111L, -112L])
            allDeleted("Requirement", [-11L])


            // requirement -12l is untouched
            allNotDeleted("CustomFieldValue", [-1211L, -1212L]);
            found (Requirement.class, -12L)


    }

    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
    def "should delete a folder and all its dependencies"(){

            setup : 
            setCurrentVersion()
        
            when :
            def result = deletionHandler.deleteNodes([-1L])
            flush()

            then :
            result.removed.collect{it.resid}.containsAll([-1L])

            allDeleted("Requirement", [-11L, -12L])
            allDeleted("RequirementVersion", [-111L, -112L, -121L])

            def lib = findEntity(RequirementLibrary.class, -1L)
            lib.rootContent.size() == 1	//that is, requirement 3
            allDeleted("CustomFieldValue", [-1111L, -1112L, -1121L, -1122L, -1211L, -1212L])
    }

    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
    def "should delete a folder and all its dependencies including attachments"(){

            setup : 
            setCurrentVersion()
        
            when :
            def result = deletionHandler.deleteNodes([-1L])
            flush()

            then :
            result.removed.collect{it.resid}.containsAll([-1L])

            allDeleted("Attachment", [-111L, -112L, -121L])
            allDeleted("AttachmentContent", [-111L, -112L, -121L])
            allDeleted("AttachmentList", [-111L, -112L, -121L])

    }
    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
    def "should delete a folder and all its dependencies including audit events"(){

            setup : 
            setCurrentVersion()
        
            when :
            def result = deletionHandler.deleteNodes([-1L])
            flush()

            then :
            result.removed.collect{it.resid}.containsAll([-1L])

            allDeleted("RequirementAuditEvent", [-111L, -112L, -121L, -122L, -123L])
            allDeleted("RequirementCreation", [-111L, -112L, -121L])
            allDeleted("RequirementPropertyChange", [-122L])
            allDeleted("RequirementLargePropertyChange", [-123L])
            allDeleted("AttachmentList", [-1L, -111L, -112L, -121L])	//requested after issue 2899
    }

    @DataSet("RequirementNodeDeletionHandlerIT.should update tc importance.xml")
    def "should update test case importance when requirement is deleted"(){
        
            when :
            def result = deletionHandler.deleteNodes([-11L])
            flush()

            then :
            result.removed*.resid.containsAll([-11L])
            allDeleted("Requirement", [-11L])
            TestCase testCase = testCaseDao.findById(-31L)
            testCase.getImportance()== TestCaseImportance.LOW

    }

    // ********************* test deletion on requirement hierarchy *******************

    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
    def "when specifically targetting a requirement, should remove it and attach its children to its former parent"(){
 
            setup : 
            setCurrentVersion()
        
            when :
            def lib = findEntity(RequirementLibrary.class, -1L)
            def result = deletionHandler.deleteNodes([-3L])
            flush()

            then :
            result.removed.collect{it.resid}.containsAll([-3L])

            result.moved.collect{ [it.dest.resid, it.dest.rel] } == [[-1L, "drive"]]
            result.moved.collect{ it.moved.collect {it.resid }  }[0]  as Set== [-31L,-32L] as Set

            result.renamed == []

            allDeleted("Requirement", [-3L])
            allNotDeleted("Requirement", [-31L, -32L, -311L]);

            Requirement r31 = findEntity(Requirement.class, -31L)
            Requirement r32 = findEntity(Requirement.class, -32L)
            lib.rootContent.containsAll([r31, r32])

    }

    /* this test is required after issue 2899 */
    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
    def "when a folder is removed, the SimpleResource is removed too and so is the attachmentlist"(){

            setup : 
            setCurrentVersion()
        
            when :
            deletionHandler.deleteNodes([-1L])
            flush()

            then :
            ! found(RequirementFolder.class, -1L)
            ! found("SIMPLE_RESOURCE", "RES_ID", -1L)
            ! found("RESOURCE", "RES_ID", -1L)
            ! found(AttachmentList.class, -1L)

    }


    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
    def "should delete a requirement in a hierarchy"(){
            setup : 
            setCurrentVersion()
        
            when :
            deletionHandler.deleteNodes([-15L])
            flush()
            then :
            ! found(Requirement.class, -15L)
    }

    // ********************* test with milestones *******************


  
    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.milestones.xml")
    def "active milestone not related : do not delete anything"(){

        setup :
            setCurrentVersion()
            milestoneHolder.setActiveMilestone(-2L)
                        
        when :
            deletionHandler.deleteNodes([-12L])
            flush()
            
        then :
            found(RequirementVersion.class, -121L)
            found(Requirement.class, -12L)

    }
    
    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.milestones.xml")
    def "active milestone related + requirement has only one version + that version has only that milestone : delete the requirement"(){

        setup :
            setCurrentVersion()
            milestoneHolder.setActiveMilestone(-1L)
                        
        when :
            deletionHandler.deleteNodes([-12L])
            flush()
            
        then :
            allDeleted("Requirement", [-12L])
            allDeleted("RequirementVersion", [-121L])

    }

    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.milestones.xml")
    def "active milestone related + requirement has two versions + one version has only that milestone : delete the version"(){
        setup :
            setCurrentVersion()
            milestoneHolder.setActiveMilestone(-1L)
                        
        when :
            deletionHandler.deleteNodes([-11L])
            flush()
            
        then :
            allNotDeleted("Requirement", [-11L])
            allNotDeleted("RequirementVersion", [-112L])
            allDeleted("RequirementVersion", [-111L])
    }
    
        
    @DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.milestones.xml")       
    def "active milestone related + requirement has one version + that version has two milestones : unbind from the milestone"(){
         setup :
            setCurrentVersion()
            milestoneHolder.setActiveMilestone(-1L)
                        
        when :
            deletionHandler.deleteNodes([-13L])
            flush()
            
        then :
            allNotDeleted("Requirement", [-13L])
            allNotDeleted("RequirementVersion", [-123L])
            
            def milestones = getSession().createQuery("select v.milestones from RequirementVersion v where v.id = -123").list()
            milestones.size() == 1
            milestones[0].id == -2L
    }
}
