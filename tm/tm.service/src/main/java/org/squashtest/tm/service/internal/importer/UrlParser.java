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

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpagnon
 *
 */
/* package-private */final class UrlParser {
	private UrlParser(){

	}
	private static final Logger LOGGER = LoggerFactory.getLogger(UrlParser.class);
	/* *****************************Path extraction*********************************** */
	/**
	 * <strong>return names extracted the following way :</strong><br>
	 * <ul>
	 * <li>names are added one by one to the end of the list</li>
	 * <li>folder names are separated by '/', </li>
	 * <li>'//' is read as a '/' in the folder name</li>
	 * <li>'///' is interpreted as one '/' in the name and the end of the name</li>
	 * <li>'/ //' is interpreted as the end of the name and one '/' in the next name</li>
	 * <li>all space characters at the beginning and at the end of extracted names are removed.</li>
	 * </ul>
	 * <br>
	 * As examples:
	 * <ul>
	 * <li>"name1/name2/name3" => "name1" + "name2" + "name3"</li>
	 * <li>"/name1//name2/name3" => "name1/name2" + "name3"</li>
	 * <li>"//name1///name2/name3" => "/name1/" + "name2" + "name3"</li>
	 * <li>"///name1/ //name2/ name3 " => "/name1" + "/name2" + "name3"</li>
	 * </ul>
	 */
	public static List<String> extractFoldersNames(String path) {
		LinkedList<String> nameList = new LinkedList<>();
		String trimedPath = path.trim();
		StringReader pathReader = new StringReader(trimedPath);
		try {
			int pathCharInt = pathReader.read();
			if (pathCharInt != -1) {
				if ((char) pathCharInt != '/') {
					pathCharInt = readNextFolder(nameList, pathReader, pathCharInt);
				} else {
					pathCharInt = readFirstFolder(nameList, pathReader, pathCharInt);
				}
				while (pathCharInt != -1) {
					pathCharInt = readNextFolder(nameList, pathReader, pathCharInt);
				}
			}
		} catch (IOException e) {
			LOGGER.warn(e.getMessage());

		}

		return nameList;
	}

	private static int readFirstFolder(List<String> nameList, StringReader pathReader, int pathCharIntParam) throws IOException {
		int pathCharInt = pathCharIntParam;
		StringBuilder nameBuffer = new StringBuilder();
		int slashesNumber = 1;
		pathCharInt = pathReader.read();
		while (pathCharInt != -1 && (char) pathCharInt == '/') {
			slashesNumber++;
			pathCharInt = pathReader.read();
		}
		int slashesToaddToBuffer;
		if (isEven(slashesNumber)) {
			slashesToaddToBuffer = slashesNumber / 2;
		} else {
			slashesToaddToBuffer = (slashesNumber - 1) / 2;
		}
		addSlashesToBuffer(nameBuffer, slashesToaddToBuffer);
		pathCharInt = readName(pathCharInt, pathReader, nameBuffer, nameList);

		return pathCharInt;
	}

	private static int readNextFolder(List<String> nameList, StringReader pathReader, int pathCharInt) throws IOException {
		StringBuilder folderNameBuffer = new StringBuilder();
		return readName(pathCharInt, pathReader, folderNameBuffer, nameList);
	}

	private static int readName(int pathCharIntParam, StringReader pathReader, StringBuilder folderNameBuffer,
			List<String> nameList) throws IOException {
		Integer slashesNumber = 0;
		int pathCharInt = pathCharIntParam;
		while(pathCharInt != -1){
			if((char)pathCharInt == '/'){
				while((char)pathCharInt == '/'){
					slashesNumber ++;
					pathCharInt = pathReader.read();
				}
				int slashesToAdd ;
				if(isEven(slashesNumber)){
					slashesToAdd = slashesNumber / 2;
					addSlashesToBuffer(folderNameBuffer, slashesToAdd);
				}else{
					slashesToAdd = (slashesNumber - 1) / 2;
					addSlashesToBuffer(folderNameBuffer, slashesToAdd);
					break;
				}
				slashesNumber = 0;
			}
			folderNameBuffer.append((char)pathCharInt);
			pathCharInt = pathReader.read();

		}
		String name = folderNameBuffer.toString();
		nameList.add(name.trim());
		return pathCharInt;
	}

	private static void addSlashesToBuffer(StringBuilder folderNameBuffer, int slashesToAdd) {
		for (int i = 0; i < slashesToAdd; i++) {
			folderNameBuffer.append('/');
		}

	}

	private static boolean isEven(int n) {
		return n % 2 == 0;
	}

}
