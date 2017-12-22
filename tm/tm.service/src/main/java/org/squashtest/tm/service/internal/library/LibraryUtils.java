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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LibraryUtils {
	private static final String COPY_TOKEN = "-Copie";

	private LibraryUtils() {

	}
	/**
	 * Will generate a unique name assuming that there is a clash with the given copiesNames.
	 *
	 */
	public static String generateUniqueName(List<String> copiesNames, String sourceName, String token, int maxNameSize) {
		TokenHelper helper = new SimpleTokenHelper(token);
		return generateUniqueName(copiesNames, sourceName, maxNameSize, helper);
	}


	private static String generateUniqueName(Collection<String> copiesNames, String sourceName, int maxNameSize,
			TokenHelper helper) {
		String result;
		String baseName = sourceName;
		String tokenRegexp = helper.getTokenRegexp();

		int newCopyNumber = generateUniqueIndex(copiesNames, baseName, 0, tokenRegexp);
		result =  helper.buildResult(newCopyNumber, baseName);

		while(result.length() > maxNameSize){
			int extraCharsNumber = result.length() - maxNameSize;
			baseName = substringBaseName(baseName, extraCharsNumber);

			newCopyNumber = generateUniqueIndex(copiesNames, baseName, newCopyNumber, tokenRegexp);
			result =  helper.buildResult(newCopyNumber, baseName);
		}

		return result;
	}


	private interface TokenHelper{
		String getTokenRegexp();
		String buildResult(int index, String baseName);
	}

	private static class SimpleTokenHelper implements TokenHelper{
		private String token;

		private SimpleTokenHelper(String token){
			this.token = token;
		}

		@Override
		public String buildResult(int index, String baseName) {
			return baseName + token + index;
		};
		@Override
		public String getTokenRegexp() {
			return token+"(\\d+)";
		}
	}

	private static class ParenthesisTokenHelper implements TokenHelper{

		@Override
		public String buildResult(int index, String baseName) {
			return baseName+ " (" + index + ")";
		};
		@Override
		public String getTokenRegexp() {
			return " \\((\\d+)\\)";
		}
	}

	public static String generateUniqueCopyName(List<String> copiesNames, String sourceName, int maxNameSize) {
		return generateUniqueName(copiesNames, sourceName, COPY_TOKEN, maxNameSize);

	}

	private static String substringBaseName(String baseName, int extraCharsNumber) {
		baseName = baseName.substring(0, baseName.length() - extraCharsNumber-3)+"...";
		return baseName;
	}

	/**
	 * Generates a non-clashing name for a "source" to be added amongst "siblings". The non-clashing name is either the
	 * source (when no clash) or the source appended with "(n)"
	 *
	 * @param source
	 *            the non <code>null</code> source name.
	 * @param siblings
	 *            a non <code>null</code> collection of siblings of the source name
	 * @return a non clashing name
	 */
	public static String generateNonClashingName(String source, Collection<String> siblings, int maxNameSize) {

		if (noNameClash(source, siblings)) {
			return source;
		}
		TokenHelper helper = new ParenthesisTokenHelper();
		return generateUniqueName(siblings, source, maxNameSize, helper);
	}



	private static int generateUniqueIndex(Collection<String> siblings, String baseName, int minIndex, String tokenRegexp){
		List<String> potentialClashes = filterPotentialClashes(baseName, siblings);
		Pattern p = Pattern.compile(Pattern.quote(baseName) + tokenRegexp);
		return computeNonClashingIndex(p, potentialClashes, minIndex);
	}

	private static int computeNonClashingIndex(Pattern indexLookupPattern, Collection<String> potentialClashes, int minCopyNumber) {
		int maxIndex = 0;

		for (String sibling : potentialClashes) {
			Matcher m = indexLookupPattern.matcher(sibling);

			if (m.find()) {
				int siblingIndex = Integer.parseInt(m.group(1)); // regexp pattern ensures it always parses as int
				maxIndex = Math.max(maxIndex, siblingIndex);
			}
		}
		int result = ++maxIndex;
		if(result<minCopyNumber){
			result = minCopyNumber;
		}
		return result;
	}

	private static List<String> filterPotentialClashes(String source, Collection<String> siblings) {
		List<String> potentialClashes = new ArrayList<>(siblings.size());

		for (String sibling : siblings) {
			if (sibling.startsWith(source)) {
				potentialClashes.add(sibling);
			}
		}
		return potentialClashes;
	}

	private static boolean noNameClash(String name, Collection<String> siblings) {
		return siblings.isEmpty() || !siblings.contains(name);
	}


}
