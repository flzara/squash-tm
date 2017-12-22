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
package org.squashtest.tm.service.internal.importer;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.internal.library.LibraryUtils;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;

/**
 * <p>The idea behind the implementation of this class is the following :</p>
 *
 * parameters :
 * <ul>
 * 	<li>the receiving persistent container : D <li>
 * 	<li>the detached container containing the transient entities we want to persist : S</li>
 * </ul>
 *
 * <ul>
 * 	<li>for each n in S :</li>
 * 		<ul>
 * 			<li>if n is a TestCase : </li>
 * 			<ul>
 * 				<li>if name(n) already exists in names(D) :</li>
 * 				<ul>
 * 					<li>rename n with a available name in D</li>
 * 					<li>add a warning regarding this operation</li>
 * 				</ul>
 * 				<li>in any case, persist n into D</li>
 * 			</ul>
 * 			<li>if n is a folder :</li>
 * 			<ul>
 * 				<li>if name(n) already exists in names(D) :</li>
 * 				<ul>
 * 					<li>fetch node p in D where name(p) = name(n)</li>
 * 					<li>if p is a TestCase :</li>
 * 					<ul>
 * 						<li>rename n with an available name in D</li>
 * 						<li>add a warning regarding this operation</li>
 * 						<li>persist n into D</li>
 * 					</ul>
 * 					<li>if p is a Folder</li>
 * 					<ul>
 * 						<li>call this function recursively using D <- p and S <- n </li>
 * 					</ul>
 * 				</ul>
 * 			</ul>
 * 		</ul>
 * </ul>
 *
 * Regarding the summary, may increment failures and warning, but not total test cases nor success.
 *
 * @author bsiri
 *
 */

/*
 * Node : the use of visitors and the distinct interfaces between libraries and folders made the following implementation unnecessarily complex.
 */

class TestCaseLibraryMerger {


	private TestCaseLibraryNavigationService service;

	private ImportSummaryImpl summary = new ImportSummaryImpl();

	private Deque<FolderPair> nonTreated = new LinkedList<>();


	public TestCaseLibraryMerger() {
		super();
	}

	public TestCaseLibraryMerger(TestCaseLibraryNavigationService service) {
		this();
		this.service = service;
	}


	public void setLibraryService(TestCaseLibraryNavigationService service) {
		this.service = service;
	}

	public ImportSummary getSummary() {
		return summary;
	}

	private NodeMerger merger = new NodeMerger();


	/**
	 * the Library is the root of the hierarchy, and that's where we're importing our data. the data that couldn't be added to the root of the library
	 * (mostly duplicate folders) will be treated in additional loops (see #mergerIntoFolder)
	 *
	 */
	public void mergeIntoLibrary(TestCaseLibrary dest, TestCaseFolder src) {

		//phase 1 : add the content of the root of the library
		merger.setMergingContext(this);
		merger.setDestination(dest);

		for (TestCaseLibraryNode node : src.getContent()) {
			node.accept(merger);
		}

		//phase 2 : if some source folder already exists, then no need to persist it, but we must merge its content instead with the content of the
		//corresponding persistent entity.

		//important : do not replace the while loop with a for or foreach :
		//nonTreated may/should be modified during treatment
		FolderPair pair;

		while (!nonTreated.isEmpty()) {

			pair = nonTreated.removeFirst();

			merger.setDestination(pair.dest);

			for (TestCaseLibraryNode node : pair.src.getContent()) {
				node.accept(merger);
			}

		}

	}


	/* ******************************** private classes ************************************ */

	private static class FolderPair {
		private TestCaseFolder dest;
		private TestCaseFolder src;

		public FolderPair(TestCaseFolder dest, TestCaseFolder src) {
			this.dest = dest;
			this.src = src;
		}

	}


	/*
	 * This class is an adapter to help with the API differences between Libraries and Folders
	 */

	private static class DestinationManager {

		protected TestCaseLibraryMerger context;

		protected TestCaseLibrary destLibrary;
		protected TestCaseFolder destFolder;


		public void setMergingContext(TestCaseLibraryMerger merger) {
			this.context = merger;
		}

		public void setDestination(TestCaseLibrary library) {
			this.destLibrary = library;
			this.destFolder = null;
		}

		public void setDestination(TestCaseFolder folder) {
			this.destFolder = folder;
			this.destLibrary = null;
		}


		protected Collection<TestCaseLibraryNode> getDestinationContent() {
			if (destLibrary != null) {
				return destLibrary.getRootContent();
			} else {
				return destFolder.getContent();
			}
		}


		protected void persistTestCase(TestCase tc) {
			if (destLibrary != null) {
				context.service.addTestCaseToLibrary(destLibrary.getId(), tc, null);
			} else {
				context.service.addTestCaseToFolder(destFolder.getId(), tc, null);
			}
		}

		protected void persistFolder(TestCaseFolder folder) {
			if (destLibrary != null) {
				context.service.addFolderToLibrary(destLibrary.getId(), folder);
			} else {
				context.service.addFolderToFolder(destFolder.getId(), folder);
			}
		}

		protected void applyConfigurationTo(DestinationManager otherManager) {
			otherManager.setMergingContext(context);

			if (destLibrary != null) {
				otherManager.setDestination(destLibrary);
			} else {
				otherManager.setDestination(destFolder);
			}
		}


	}


	private static class NodeMerger extends DestinationManager implements TestCaseLibraryNodeVisitor {

		private TestCaseMerger tcMerger = new TestCaseMerger();
		private FolderMerger fMerger = new FolderMerger();

		@Override
		public void visit(TestCase visited) {
			applyConfigurationTo(tcMerger);
			tcMerger.setTransientTestCase(visited);

			tcMerger.merge();

		}

		@Override
		public void visit(TestCaseFolder visited) {
			applyConfigurationTo(fMerger);
			fMerger.setTransientFolder(visited);

			fMerger.merge();
		}


	}


	private static class TestCaseMerger extends DestinationManager {

		private TestCase toMerge;

		public void setTransientTestCase(TestCase tc) {
			toMerge = tc;
		}


		public void merge() {
			List<String> names = collectNames(getDestinationContent());

			if (names.contains(toMerge.getName())) {
				String newName = generateUniqueName(names, toMerge.getName());
				toMerge.setName(newName);
				context.summary.incrRenamed();
			}

			try {
				persistTestCase(toMerge);
			} catch (Exception ex) {    // NOSONAR this is temporary, it should change once we add proper javax validators
				context.summary.incrFailures();
			}

		}

	}


	private static class FolderMerger extends DestinationManager implements TestCaseLibraryNodeVisitor {

		private TestCaseFolder toMerge;


		public void setTransientFolder(TestCaseFolder folder) {
			this.toMerge = folder;
		}

		public void merge() {

			Collection<String> names = collectNames(getDestinationContent());

			if (names.contains(toMerge.getName())) {
				TestCaseLibraryNode conflictingNode = getByName(getDestinationContent(), toMerge.getName());
				conflictingNode.accept(this);
			} else {
				persistFolder(toMerge);
			}

		}

		//in the case of a conflict with an existing test case we have to rename the transient folder then persist it
		@Override
		public void visit(TestCase persisted) {
			List<String> allNames = collectNames(getDestinationContent());

			String newName = generateUniqueName(allNames, toMerge.getName());
			toMerge.setName(newName);

			context.summary.incrRenamed();

			persistFolder(toMerge);

		}


		//in the case of a conflict with an existing folder it's fine : we don't have to persist it.
		//However we must handle the transient content and merge them in turn : we notify the context that it must now merge it.
		@Override
		public void visit(TestCaseFolder persisted) {
			FolderPair pair = new FolderPair(persisted, toMerge);
			context.nonTreated.add(pair);
		}

	}


	/* ******************************** util functions ************************************* */


	private static List<String> collectNames(Collection<TestCaseLibraryNode> nodes) {
		List<String> res = new LinkedList<>();

		for (TestCaseLibraryNode node : nodes) {
			res.add(node.getName());
		}

		return res;
	}


	private static String generateUniqueName(List<String> pickedNames, String baseName) {
		String copyToken = "-import";
		return LibraryUtils.generateUniqueName(pickedNames, baseName, copyToken, Sizes.NAME_MAX);
	}

	private static TestCaseLibraryNode getByName(Collection<TestCaseLibraryNode> hayStack, String needle) {
		for (TestCaseLibraryNode node : hayStack) {
			if (node.getName().equals(needle)) {
				return node;
			}
		}
		throw new IllegalArgumentException("that method should never have been called if not preceeded by a preventive call to " +
			"collectName().contains() or if this preventive call returned false - something is wrong with your code dude ");

	}


}
