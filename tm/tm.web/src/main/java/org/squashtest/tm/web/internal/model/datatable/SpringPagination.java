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
package org.squashtest.tm.web.internal.model.datatable;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.domain.Sort.Direction;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;

/**
 *
 * @author bsiri
 */
public class SpringPagination {

    private SpringPagination(){
        super();
    }

    /**
    * Will create a Pageable object from the DataTableDrawParameter in arguments.
    * In this version, the Pageable will only consider the pagination but will
    * not include sorting features
    * (you would rather consider {#link pageable(DataTableDrawParameters, DatatableMapper)}
    *
    * @param dtParams the parameters sent by the datatable (client-size)
    */
    public static Pageable pageable(DataTableDrawParameters dtParams){

        PageNumSize pns = createPageNumSize(dtParams);
        return new PageRequest(pns.pagenum, pns.pagesize);

    }

    /**
     * Will create a Pageable object from the DataTableDrawParameter and DatatableMapper in arguments.
     * This will include the pagination information, and also if applicable the directives about data sorting.
     *
     */
    public static Pageable pageable(DataTableDrawParameters dtParams, DatatableMapper mapper){

        // 1 - paging
        PageNumSize pns = createPageNumSize(dtParams);

        // 2 - sorting
        Sort sort = createSort(dtParams, mapper);

        return new PageRequest(pns.pagenum, pns.pagesize, sort);

    }


    private static PageNumSize createPageNumSize(DataTableDrawParameters dtParams){

        // Default to no pagination (ie should display all results)
        // this is the official trick from Spring community to emulate 'infinity'
        int pagesize = Integer.MAX_VALUE;
        int pagenum = 0;

        // in case we actually want pagination :
        // page size = page size
        // page number = first item index / page size
        // note : normally only integer division will occur, no need to worry about float result
        if (dtParams.getiDisplayLength() > 0){
            pagesize = dtParams.getiDisplayLength();
            pagenum = dtParams.getiDisplayStart() / pagesize;
        }

        return new PageNumSize(pagenum, pagesize);
    }

    private static Sort createSort(DataTableDrawParameters dtParams, DatatableMapper mapper){
        Sort sort = null;
        int howMany = dtParams.getiSortingCols();

        if ( howMany > 0){
            List<Order> orders = new ArrayList<>(howMany);

            int sortedcol;
            String sorteddir;
            Object mappingkey;
            String attribute;

            for (int i=0; i < howMany; i++){

                sortedcol = dtParams.getiSortCol(i);
                mappingkey = dtParams.getmDataProp(sortedcol);
                attribute = mapper.getMapping(mappingkey);

                sorteddir = dtParams.getsSortDir(i);
                Direction direction = ("asc".equals(sorteddir)) ? Direction.ASC : Direction.DESC;

                orders.add(new Order(direction, attribute));
            }

            sort = new Sort(orders);
        }
        return sort;
    }

    // IGNOREVIOLATIONS:START
    private static final class PageNumSize{
        int pagenum;
        int pagesize;
        PageNumSize(int pagenum, int pagesize){
            this.pagenum = pagenum;
            this.pagesize = pagesize;
        }
    }
    // IGNOREVIOLATIONS:END
}
