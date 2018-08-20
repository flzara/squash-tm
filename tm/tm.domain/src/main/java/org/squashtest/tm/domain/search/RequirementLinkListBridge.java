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
package org.squashtest.tm.domain.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Session;
import org.hibernate.search.bridge.LuceneOptions;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequirementLinkListBridge extends SessionFieldBridge {

	private static final int EXPECTED_LENGTH = 7;

	private String padRawValue(BigInteger rawValue) {
		return StringUtils.leftPad(rawValue.toString(), EXPECTED_LENGTH, '0');
	}

	private String padRawValue(BigDecimal rawValue) {
		return StringUtils.leftPad(rawValue.toString(), EXPECTED_LENGTH, '0');
	}

	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document, LuceneOptions luceneOptions) {

		RequirementVersion r = (RequirementVersion) value;
		List<RequirementVersionLinkType> list = session.createNamedQuery("RequirementVersionLinkType.getAllRequirementVersionLinkTypes").setReadOnly(true)
			.list();

		Map<String, String> map = new HashMap<>();
		for (RequirementVersionLinkType type : list) {
			map.put(type.getRole1Code(), padRawValue(BigInteger.valueOf(0)));
			if (!map.containsKey(type.getRole2Code())) {
				map.put(type.getRole2Code(), padRawValue(BigInteger.valueOf(0)));
			}
		}
		List<Object> linkCount = session.createNativeQuery("SELECT case requirement_version_link.LINK_DIRECTION when true then requirement_version_link_type.role_1_code else requirement_version_link_type.role_2_code end as relationRole , SUM(CASE WHEN requirement_version_link.REQUIREMENT_VERSION_ID =:id THEN 1 ELSE 0 END) as count FROM requirement_version_link requirement_version_link, requirement_version_link_type requirement_version_link_type WHERE requirement_version_link.link_type_id = requirement_version_link_type.type_id group by relationRole;")
			.setReadOnly(true)
			.setParameter("id", r.getId())
			.list();


		for (Object result : linkCount) {
			Object[] temp = (Object[]) result;

			if (temp[1].getClass().equals(BigDecimal.class)) {
				map.put((String) temp[0], padRawValue((BigDecimal) temp[1]));
			}
			if (temp[1].getClass().equals(BigInteger.class)) {
				map.put((String) temp[0], padRawValue((BigInteger) temp[1]));
			}
		}

		Field field = null;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			field = new Field(entry.getKey(), entry.getValue(), luceneOptions.getStore(), luceneOptions.getIndex(),
				luceneOptions.getTermVector());
			field.setBoost(luceneOptions.getBoost());
			document.add(field);
		}


	}
}
