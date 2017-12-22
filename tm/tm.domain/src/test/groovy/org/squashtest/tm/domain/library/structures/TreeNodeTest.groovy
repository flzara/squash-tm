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
package org.squashtest.tm.domain.library.structures


import spock.lang.Specification;

class TreeNodeTest extends Specification {

    def "should return the hierarchy of a node"() {

        given:
        SubTreeNode node1 = new SubTreeNode();
        node1.parent = null
        node1.depth = 0

        SubTreeNode node2 = new SubTreeNode();
        node2.parent = node1
        node2.depth = 1

        SubTreeNode node3 = new SubTreeNode();
        node3.parent = node2
        node3.depth = 2

        and:
        def expected = [node3, node2, node1]


        when:
        def result = node3.getHierarchy();


        then:
        result == expected

    }


    def "should autocompute its depth"() {

        given:
        SubTreeNode node1 = new SubTreeNode();
        node1.parent = null
        node1.depth = 0

        SubTreeNode node2 = new SubTreeNode();
        node2.parent = node1
        node2.depth = 1

        SubTreeNode node3 = new SubTreeNode();
        node3.parent = node2
        node3.depth = 98987545

        and:
        def expected = 2

        when:
        def before = node3.depth
        node3.recomputeDepth()
        def after = node3.depth

        then:
        before == 98987545
        after == 2

    }


    def "should add a child"() {

        given:
        SubTreeNode node1 = new SubTreeNode();
        node1.parent = null;
        node1.depth = 0
        node1.name = "Vador"
        node1.gun = "red saber"

        and:
        SubTreeNode node2 = new SubTreeNode();
        node2.name = "Luke"
        node2.gun = "green saber"


        when:
        node1.addChild node2

        then:

        node1.children.contains node2
        node2.parent == node1
        node2.depth == 1


    }


    def "should tell that they are same"() {

        given:
        def node1 = new SubTreeNode();
        node1.key = 0
        node1.name = "Long cat"
        node1.gun = "light"

        and:
        def node2 = new SubTreeNode();
        node2.key = 0
        node2.name = "Tac gnol"
        node2.gun = "dark"


        when:
        def result = isSame(node1, node2)


        then:
        result == true


    }

    public boolean isSame(def kore, def sore) {
        boolean result;

        if (sore == null) {
            result = kore == null;
        } else if (kore.key == null) {
            result = (sore.key == null);
        } else {
            result = kore.key.equals(sore.key);
        }

        return result;
    }

    def "should tell that they aren't same"() {


        given:
        def node1 = new SubTreeNode();
        node1.key = 0
        node1.name = "Long cat"
        node1.gun = "light"

        and:
        def node2 = new SubTreeNode();
        node2.key = 1
        node2.name = "Long cat"
        node2.gun = "light"


        when:
        def result = isSame(node1, node2)


        then:
        result == false


    }


    def "should levelup the first guy"() {

        given: "level 1"
        def newbie = new SubTreeNode(1, "Sam", "watergun")

        and: "level up"
        def levelup = new SubTreeNode(1, "Super Sam", "double watergun")

        when:
        newbie.updateWith levelup


        then:
        newbie.key == levelup.key
        newbie.name == levelup.name
        newbie.gun == levelup.gun
    }


}
