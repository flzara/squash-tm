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
package org.squashtest.tm.service.internal.testcase;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.NamedReferencePair;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode;
import org.squashtest.tm.service.internal.repository.TestCaseDao;

/**
 *
 * @author Gregory Fouquet
 *
 */
@Component
public class TestCaseCallTreeFinder {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseCallTreeFinder.class);

	@Inject
	private TestCaseDao testCaseDao;

	/**
	 *  given the Id of a test case, will compute the subsequent test case call tree.
	 *
	 * @param rootTcId. Null is not legal and unchecked.
	 * @return a set containing the ids of the called test cases, that will not include the calling test case id. Not null, possibly empty.
	 */
	public Set<Long> getTestCaseCallTree(Long rootTcId) {

		Set<Long> calleesIds = new HashSet<>();
		List<Long> prevCalleesIds = testCaseDao.findAllDistinctTestCasesIdsCalledByTestCase(rootTcId);

		LOGGER.trace("TestCase #{} directly calls {}", rootTcId, prevCalleesIds);

		prevCalleesIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data

		while (!prevCalleesIds.isEmpty()) {
			// FIXME a tester avant correction : boucle infinie quand il y a un cycle dans les appels de cas de test
			calleesIds.addAll(prevCalleesIds);
			prevCalleesIds = testCaseDao.findAllTestCasesIdsCalledByTestCases(prevCalleesIds);

			LOGGER.trace("TestCase #{} indirectly calls {}", rootTcId, prevCalleesIds);
			prevCalleesIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data
		}

		return calleesIds;
	}

	/**
	 * same as {@link #getTestCaseCallTree(Long)}, but for multiple test cases
	 *
	 * @param tcIds
	 * @return
	 */
	public Set<Long> getTestCaseCallTree(Collection<Long> tcIds) {

		Set<Long> result = new HashSet<>();

		Collection<Long> process = tcIds;
		List<Long> next;

		while (!process.isEmpty()) {
			next = testCaseDao.findAllTestCasesIdsCalledByTestCases(process);
			next.removeAll(result);    // remove results that were already processed

			result.addAll(next);
			process = next;

		}

		return result;
	}


	/**
	 * Returns all test cases ids of test casese calling the one matching the given id.
	 * The search will go through all the calling hierarchy.
	 *
	 * @param rootTcId : the id of the potentially called test case we want the callers of.
	 * @return : all calling test case (even through multiple call)
	 */
	public Set<Long> getTestCaseCallers(Long rootTcId) {

		Set<Long> callerIds = new HashSet<>();
		List<Long> prevCallerIds = testCaseDao.findAllDistinctTestCasesIdsCallingTestCase(rootTcId);

		LOGGER.trace("TestCase #{} directly calls {}", prevCallerIds, rootTcId);

		prevCallerIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data

		while (!prevCallerIds.isEmpty()) {
			// FIXME a tester avant correction : boucle infinie quand il y a un cycle dans les appels de cas de test
			callerIds.addAll(prevCallerIds);
			prevCallerIds = testCaseDao.findAllTestCasesIdsCallingTestCases(prevCallerIds);

			LOGGER.trace("TestCase #{} indirectly calls {}", prevCallerIds, rootTcId);
			prevCallerIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data
		}

		return callerIds;
	}

	/**
	 * returns a graph of simple nodes representing the ancestry of the nodes in arguments.
	 *
	 * @param calledIds
	 * @return
	 */
	public LibraryGraph<NamedReference, SimpleNode<NamedReference>> getCallerGraph(List<Long> calledIds) {

		// remember which nodes were processed (so that we can spare less DB calls in the worst cases scenarios)
		Set<Long> allIds = new HashSet<>();
		allIds.addAll(calledIds);

		// the temporary result variable
		List<NamedReferencePair> allpairs = new LinkedList<>();

		// a temporary buffer variable
		List<Long> currentCalled = new LinkedList<>(calledIds);

		// phase 1 : data collection
		while (!currentCalled.isEmpty()) {

			List<NamedReferencePair> currentPair = testCaseDao.findTestCaseCallsUpstream(currentCalled);

			allpairs.addAll(currentPair);

			/*
			 * collect the caller ids in the currentPair for the next loop, with the following restrictions :
			 * 1) if the caller is not null, and
			 * 2) if that node was not already processed
			 *
			 * then we can add that id.
			 */

			List<Long> nextCalled = new LinkedList<>();

			for (NamedReferencePair pair : currentPair) {
				// no caller -> no need for further processing
				if (pair.getCaller() == null) {
					continue;
				}

				Long key = pair.getCaller().getId();
				if (!allIds.contains(key)) {
					nextCalled.add(key);
					allIds.add(key);
				}

			}

			currentCalled = nextCalled;

		}

		// phase 2 : make that graph

		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph<>();

		for (NamedReferencePair pair : allpairs) {
			graph.addEdge(node(pair.getCaller()), node(pair.getCalled()));
		}

		return graph;

	}

	/**
	 * returns an extended graph of ALL the test cases that can be reached by the source test cases, navigating on the call test steps
	 * in both directions. This graph include the callers and the called test cases, recursively including their callers and test cases
	 * etc until all relevant test cases are included in the resulting graph.
	 *
	 * @param calledIds
	 * @return
	 */
	// note :  for each node, call steps are searched both upward and downward. As a result every edge will
	// be found twice (once on the up, once on the down). We then must build the graph by using only one edge over two
	// that's why we use a bag here, and then we halve the cardinality.
	public LibraryGraph<NamedReference, SimpleNode<NamedReference>> getExtendedGraph(List<Long> sourceIds) {

		// remember which nodes were processed (so that we can spare less DB calls in the worst cases scenarios)
		Set<Long> treated = new HashSet<>();
		treated.addAll(sourceIds);

		// the temporary result variable
		Bag allpairs = new HashBag();

		// a temporary buffer variable
		List<Long> currentNodes = new LinkedList<>(sourceIds);

		// phase 1 : data collection

		while (!currentNodes.isEmpty()) {

			List<NamedReferencePair> currentPair = testCaseDao.findTestCaseCallsUpstream(currentNodes);
			currentPair.addAll(testCaseDao.findTestCaseCallsDownstream(currentNodes));

			allpairs.addAll(currentPair);

			/*
			 * collect the caller ids in the currentPair for the next loop, with the following restrictions :
			 * 1) if the "caller" slot of the Object[] is not null,
			 * 2) if the "called" slot is not null,
			 * 2) if that node was not already processed
			 *
			 * then we can add that id.
			 */

			List<Long> nextNodes = new LinkedList<>();

			for (NamedReferencePair pair : currentPair) {

				// no caller or no called -> no need for further processing
				if (pair.getCaller() == null || pair.getCalled() == null) {
					continue;
				}

				Long callerkey = pair.getCaller().getId();
				if (!treated.contains(callerkey)) {
					nextNodes.add(callerkey);
					treated.add(callerkey);
				}

				Long calledkey = pair.getCalled().getId();
				if (!treated.contains(calledkey)) {
					nextNodes.add(calledkey);
					treated.add(calledkey);
				}

			}

			currentNodes = nextNodes;

		}

		// phase 2 : halve the number of edges as explained in the comment above the method
		// every edges will appear two times, except for "boundaries" (ie caller is null or called is null),
		// for which the cardinality is 1.
		for (NamedReferencePair pair : (Set<NamedReferencePair>) allpairs.uniqueSet()) {
			int cardinality = allpairs.getCount(pair);
			if (cardinality > 1) {
				allpairs.remove(pair, cardinality / 2);
			}
		}

		// phase 3 : make that graph
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph<>();

		for (NamedReferencePair pair : (Iterable<NamedReferencePair>) allpairs) {
			graph.addEdge(node(pair.getCaller()), node(pair.getCalled()));
		}

		return graph;

	}

	private SimpleNode<NamedReference> node(NamedReference ref) {
		return ref != null ? new SimpleNode<>(ref) : null;
	}

}
