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
package org.squashtest.tm.service.testutils

import org.squashtest.tm.domain.infolist.InfoList
import org.squashtest.tm.domain.infolist.InfoListItem
import org.squashtest.tm.domain.infolist.ListItemReference
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.scm.ScmRepository
import spock.lang.Specification

import java.nio.file.Files;

class MockFactory extends Specification {

	def mockProject(){

		Project p = Mock();

		// this is random enough for my needs
		def id = System.currentTimeMillis().value
		p.getId() >> id

		InfoList natures = new InfoList()
		InfoListItem nat = new 	ListItemReference("NAT")
		nat.setDefault(true)
		natures.items << nat

		InfoList types = new InfoList()
		InfoListItem typ = new 	ListItemReference("TYP")
		typ.setDefault(true)
		types.items << typ

		InfoList categories = new InfoList()
		InfoListItem cat = new ListItemReference("CAT_BUSINESS");
		cat.setDefault(true)
		categories.items << cat

		p.getTestCaseNatures() >> natures
		p.getTestCaseTypes() >> types
		p.getRequirementCategories() >> categories


		p

	}

/*
	def mockScmRepository(){
		// create the file structure
		File base = Files.createTempDirectory("ATMSTest_").toFile()
		base.deleteOnExit()


		FileTreeBuilder builder = new FileTreeBuilder(base)
		builder {
			dir("squash") {
				file "815_test1.ta"
				file "220_test2.ta"
				dir("subfolder") {
					file "999_test3.ta"
				}
			}
			file "unrelated_file.txt"
			dir("unrelated_folder") {
				file "another_file.txt"
			}

		}

		// now we can create the ScmRepository
		return new ScmRepository( name: "my repository", repositoryPath: base.absolutePath, workingFolderPath: "squash")


	}
*/
	def mockScmRepository(name = "my repo", workingFolder = "squash", @DelegatesTo(FileTreeBuilder) Closure contentPopulator = null){

		File base = Files.createTempDirectory("ATMSTest_").toFile()
		base.deleteOnExit()

		FileTreeBuilder builder = new FileTreeBuilder(base)

		// default contentPopulator if it was left null
		if (contentPopulator == null){
			contentPopulator = {
				dir("squash") {
					file "815_test1.ta"
					file "220_test2.ta"
					dir("subfolder") {
						file "999_test3.ta"
					}
				}
				file "unrelated_file.txt"
				dir("unrelated_folder") {
					file "another_file.txt"
				}
			}
		}

		// populate
		builder.call contentPopulator

		// create
		def scm = new ScmRepository( name: name, repositoryPath: base.absolutePath, workingFolderPath: workingFolder)

	}




}
