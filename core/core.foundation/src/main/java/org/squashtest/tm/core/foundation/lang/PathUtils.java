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
package org.squashtest.tm.core.foundation.lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used internally mostly for operations on paths. Much like an URL instance can check if the protocol, host, path etc
 * are corrects.
 *
 * @author bsiri
 *
 */
// TODO : why this class in a package .lang ? (so is UrlUtils etc)
public final class PathUtils {

	/**
	 * a non terminal name is anything that ends with a (non included) slash and has non zero length. Escaped slashes
	 * are valid part of a name.
	 */
	private static final Pattern NON_TERMINAL_NAME = Pattern.compile("(.+?[^\\\\])/");

	private static final String SPLIT = "(?<!\\\\)/";

	private static final String CORRECT_MUTLTIPLE_SLASHE = "(?<!\\\\)\\/+";

	/**
	 * a well formed path starts with a slash, doesn't end with a slash (we consider only test cases here so they don't
	 * end with a slash), and contains at least two elements (the project name and element name in case it's at the root
	 * of the library).
	 */
	private static final Pattern WELL_FORMED_PATH = Pattern.compile("^\\/(.+?[^\\\\]/)+.*?(\\\\\\/$|[^\\/]$)");

	/**
	 * the first element of slash-separated names is the project name. Beware that escaped slashes aren't actual
	 * separator
	 */
	private static final Pattern PROJECT_PATTERN = Pattern.compile("^\\/" + NON_TERMINAL_NAME + ".*");// can concatenate
	// thanks to
	// toString()

	/** the last element is the entity name */
	private static final Pattern GENERIC_NAME_PATTERN = Pattern.compile(".*[^\\\\]\\/(.*)$");


	private PathUtils() {
		super();
	}

	public static boolean isPathWellFormed(String path) {
		return WELL_FORMED_PATH.matcher(path).matches();
	}

	public static String cleanMultipleSlashes(String path) {
		return path.replaceAll(CORRECT_MUTLTIPLE_SLASHE, "/");
	}

	/**
	 * Extracts the <strong>escaped</strong> projet name from a path.
	 * Note (GRF) : a path such as "/token" will return a null project name. Not too sure if that's what we want but it
	 * seem to work this way
	 * @see #extractUnescapedProjectName(String)
	 * @param path the non null path to extract the project name from.
	 * @return the <strong>escaped</strong> project name <strong>which might be null</strong>
	 */
	public static String extractProjectName(String path) {
		Matcher matcher = PROJECT_PATTERN.matcher(path);
		if (matcher.matches()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	/**
	 * Same as {@link #extractProjectName(String)} but unescapes the result.
	 * Project-less paths ("/foo") return null project name.
	 * @param path the non null path to extract the project name from.
	 * @return the project name <strong>which might be null</strong>
	 */
	public static String extractUnescapedProjectName(String path) {
		String esc = extractProjectName(path);
		return esc == null ? null : unescapePathPartSlashes(esc);
	}

	public static List<String> extractProjectNames(List<String> pathes) {
		Set<String> res = new HashSet<>();
		for (String path : pathes) {
			res.add(extractProjectName(path));
		}
		return new ArrayList<>(res);
	}


	public static String extractName(String path) {
		Matcher matcher = GENERIC_NAME_PATTERN.matcher(path);
		if (matcher.matches()) {
			return matcher.group(1);
		} else {
			throw new IllegalArgumentException("couldn't find a valid name in path '" + path
				+ "'. It might be malformed.");
		}
	}

	/**
	 * Will build a valid path from splits. Throw {@link IllegalArgumentException} if
	 * names concatenation lead to an ill formed path
	 * @param names names
	 * @return String
	 *
	 */
	public static String buildPathFromParts(String[] names) {
		StringBuilder builder = new StringBuilder();
		for (String name : names) {
			builder.append("/");
			builder.append(name);
		}
		String path = builder.toString();
		if (!isPathWellFormed(path)) {
			throw new IllegalArgumentException();
		}
		return path;
	}

	/**
	 * Just an alias for {@link #extractName(String)}.
	 * @param path path
	 * @return String
	 */
	public static String extractTestCaseName(String path) {
		return extractName(path);
	}


	public static boolean arePathsAndNameConsistents(String path, String name) {
		try {
			String pathName = extractName(path);
			return pathName.equals(name);

		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	/**
	 * Returns the path with a different test case name. You can't change directory that way (using "..")
	 *
	 * @param path path
	 * @param name name
	 * @return String
	 */
	public static String rename(String path, String name) {
		String oldname = extractTestCaseName(path);
		String oldpatt = "\\Q" + oldname + "\\E$";
		return path.replaceAll(oldpatt, name);
	}

	/**
	 * a well formed path starts with a '/' and we remove it right away before splitting (or else a false positive empty
	 * string would appear before it)
	 *
	 * @param path path
	 * @return String[]
	 */
	public static String[] splitPath(String path) {
		return path.replaceFirst("^/+", "").split(SPLIT);
	}

	/**
	 * <p>Accepts a well formed path and returns the list of paths of all of its ancestors, and then itself.</p>
	 *
	 *  <p>example : scanPath("/project/folder1/folder2/element") => ["/project", "/project/folder1/", "/project/folder1/folder2" ,"/project/folder1/folder2/element"]</p>
	 *
	 * 	<p>Some Scala guy would think of it as path.split("/").scanLeft("")(_ + "/" + _)</p>
	 *
	 */
	public static List<String> scanPath(String path) {

		String[] split = splitPath(path);
		List<String> paths = new ArrayList<>(split.length);
		StringBuilder buffer = new StringBuilder();

		// build all the paths on the way.
		for (String aSplit : split) {
			buffer.append("/");
			buffer.append(aSplit);
			paths.add(buffer.toString());
		}

		return paths;
	}

	/**
	 * Unescape a path. Beware that it will change the path structure by adding "/" so it should be use only with parts...
	 * @param pathPart pathPart
	 * @return String
	 */
	public static String unescapePathPartSlashes(String pathPart) {
		return pathPart.replaceAll("\\\\/", "/");
	}

	/**
	 * Unescape a path. Beware that it will change the path structure by adding "/" so it should be use only with parts...
	 * @param pathParts pathParts
	 * @return List<String>
	 */
	public static List<String> unescapePathPartSlashes(List<String> pathParts) {
		List<String> unescapedParts = new ArrayList<>();
		for (String part : pathParts) {
			unescapedParts.add(PathUtils.unescapePathPartSlashes(part));
		}
		return unescapedParts;
	}
}
