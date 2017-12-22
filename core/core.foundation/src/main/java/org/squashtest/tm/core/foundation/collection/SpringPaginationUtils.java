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
package org.squashtest.tm.core.foundation.collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * TRANSITIONAL
 *
 * his toolbox bridge the gap between our old messy API and
 * the Pageable API from Spring
 */
public class SpringPaginationUtils {

    public static final int DEFAULT_SIZE = 50;

    private SpringPaginationUtils(){

    }

    public static Pageable toPageable(PagingAndSorting pas){

            // 1 - pagination
            // default for "should display all"
            int pagenum = 0;
            int pagesize = Integer.MAX_VALUE;

            if (! pas.shouldDisplayAll()){
                    pagenum = pas.getFirstItemIndex() / pas.getPageSize();
                    pagesize = pas.getPageSize();
            }


            // 2 - sorting
            Sort sort = null;
            if (! StringUtils.isBlank(pas.getSortedAttribute())){
                    Direction dir = SortOrder.DESCENDING == pas.getSortOrder() ? Direction.DESC : Direction.ASC;
                    sort = new Sort(dir, pas.getSortedAttribute());
            }

            return new PageRequest(pagenum, pagesize, sort);
    }

    /**
     * Returns a Pageable that will search for the DEFAULT_SIZE first items. Since no sorting directive
     * is specified either, this method rather means "return me DEFAULT_SIZE random items".
     *
     * @return Pageable
     */
    public static Pageable defaultPaging(){
        return new PageRequest(0, DEFAULT_SIZE);
    }

    /**
     * Returns a Pageable that will search for the DEFAULT_SIZE first items, sorted by the given
     * attribute ascending.
     *
     * @param attribute attribute
     * @return Pageable
     */
    public static Pageable defaultPaging(String attribute){
        return new PageRequest(0, DEFAULT_SIZE, Direction.ASC, attribute);
    }

}
