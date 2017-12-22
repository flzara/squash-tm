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
package org.squashtest.tm.infrastructure.hibernate;

import static org.junit.Assert.*
import org.hibernate.boot.model.naming.PhysicalNamingStrategy
import org.hibernate.boot.model.naming.Identifier


import spock.lang.Specification
import spock.lang.Unroll

class UppercaseUnderscorePhysicalNamingTest extends Specification{
	PhysicalNamingStrategy strategy = new UppercaseUnderscorePhysicalNaming()
	
	def "table name should be UC-US class  name"() {
		when:
		def ident = strategy.toPhysicalTableName(id("foo.bar.EntityName"), null)
		then:
		ident.text == "ENTITY_NAME"
	}
	
	def "column name should be UC-US prop name"() {
		when:
		def ident = strategy.toPhysicalColumnName(id("propertyNameOfAKind"), null)
		then:
		ident.text == "PROPERTY_NAME_OF_A_KIND"
	}
	
	def "contuiguous capitals should be considered as one word"() {
		when:
		def ident = strategy.toPhysicalTableName(id("MyURLParser"), null)
		then:
		ident.text == "MY_URL_PARSER"
	}
        
        def "should not doubleprocess a name that is already compliant"(){
            
        when :
            def ident = strategy.toPhysicalTableName(id("BOB_MIKE"), null)
        
        then :
            ident.text == "BOB_MIKE"        
        }
        
        def "should process names that ends with an uppercase"(){
        
        when :
            def ident = strategy.toPhysicalTableName(id("bobX"), null)

        then :
            ident.text == "BOB_X"  
        }
    
        def "should also process names that ends with an uppercase acronyms"(){
        when :
            def ident = strategy.toPhysicalTableName(id("bobMIKE"), null)
        
        then :
            ident.text == "BOB_MIKE"                 
        }
        
        
    @Unroll
    def "should not add extra underscores when the name is rich on them"(){
        expect :
            strategy.toPhysicalTableName(id(inText), null).text == outText
        
        where :
        
            inText          |   outText
            "RENT_A_BOB"    |   "RENT_A_BOB"
            "rent_A_bob"    |   "RENT_A_BOB"
    }
        
    def id(name){
        return Identifier.toIdentifier(name)
    }
}
