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

import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.function.SQLFunction;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author bsiri
 */
public class MySQLEnhancedDialect extends MySQLDialect{

	private static final String STRAIGHT_JOIN = "STRAIGHT_JOIN";

    public MySQLEnhancedDialect() {
        super();

        Map<String, SQLFunction> extensions = HibernateDialectExtensions.getMysqlDialectExtensions();
        for (Entry<String, SQLFunction> extension : extensions.entrySet()){
            registerFunction(extension.getKey(), extension.getValue());
        }

    }

    @Override
	public String getQueryHintString(String sql, List<String> hints) {

    	// Override of original dialect method which does nothing with query hint to apply STRAIGHT_JOIN hint.
		if(hints.contains(STRAIGHT_JOIN)){
			sql = sql.replaceAll("(select|SELECT)", "SELECT STRAIGHT_JOIN");
		}

		return sql;
	}
}
