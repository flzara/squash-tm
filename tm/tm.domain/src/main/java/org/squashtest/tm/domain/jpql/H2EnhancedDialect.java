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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.squashtest.tm.domain.jpql;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.SQLFunction;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author bsiri
 */
public class H2EnhancedDialect extends H2Dialect{

	private static final String STRAIGHT_JOIN = "STRAIGHT_JOIN";

    public H2EnhancedDialect() {
        super();

        Map<String, SQLFunction> extensions = HibernateDialectExtensions.getH2DialectExtensions();
        for (Entry<String, SQLFunction> extension : extensions.entrySet()){
            registerFunction(extension.getKey(), extension.getValue());
        }

    }

	@Override
	public String getQueryHintString(String sql, List<String> hints) {

		// Override of original dialect method so that STRAIGHT_JOIN custom hint doesn't mess with initial behavior.
		hints.remove(STRAIGHT_JOIN);

		return super.getQueryHintString(sql, hints);
	}
}
