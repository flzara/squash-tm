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
def hgRepo = new File(".hg")
def hgrc = new File("hgrc", hgRepo)
def createMarker = {
    // creates a marker file
    def hookMarker = new File(".sqpreco", hgRepo)
    if (!hookMarker.exists() ) {
	hookMarker.createNewFile()
    }
}    

def lines = []

if (hgrc.exists()) {
    hgrc.eachLine { line -> lines << line }
}

def hooksSection = /^\s*\[hooks\]\s*$/

def containsHooks = lines.find { it ==~ hooksSection }
def containsPreCommit = false;

if (containsHooks) {
    containsPreCommit = lines.find { it ==~ /^\s*pre-commit\s*=.*/ }
}

if (containsPreCommit) {
    println "precommit hook found, nothing to do"
    createMarker()
    return
}

def old = new File("hgrc.old", hgRepo)
old.withWriter { w -> lines.each { w.writeLine it } }

// hook is pre-commit, not precommit, because we will check additional args not passed in precommit
def addPreCommit = { it.writeLine "pre-commit = src\\main\\scripts\\precommit.bat" } 

Closure hgrcProcessor

if (!containsHooks) {
    println "no hooks section, will add section and hooks definitions"

    hgrcProcessor = { w -> 
        lines.each { w.writeLine it } 
        w.writeLine "[hooks]"
        addPreCommit w
    }
}

if (containsHooks && !containsPreCommit) {
    println "no precommit hook, will add hook definition"

    hgrcProcessor = { w -> 
        lines.each { 
            w.writeLine it 
            if (it ==~ hooksSection) {
               addPreCommit w
            }
        } 
    }
}

hgrc.withWriter hgrcProcessor
createMarker()
