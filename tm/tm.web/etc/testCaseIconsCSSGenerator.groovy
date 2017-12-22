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
widthOfOneIcon = 24
heightOfOneIcon = 16

println('hello groovy')

fileName = 'testCaseTreeIcons.css'
statuses = [
	"work_in_progress",
	"under_review",
	"approved",
	"obsolete",
	"to_be_updated"
]

importances = [
	"very_high",
	"high",
	"medium",
	"low"
]

totalWidth = (statuses.size() * widthOfOneIcon)*2 + widthOfOneIcon
totalHeight = (heightOfOneIcon*importances.size())

def printBackgroundProperty(xpos, ypos, File f3){
	
	f3.append('background-image: url("../images/Icon_Tree_TestCase_map.png");  /** sprite-ref: sprites-icons; '
		+'\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t sprite-margin-top: '+xpos +'px; '
		+'\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t sprite-margin-left: '+ypos+'px*/\n')
	f3.append('background-position : -'+ypos+'px -'+xpos+'px ;\n')
	f3.append('width: 24px;\n')
	f3.append('height: 16px;\n')
}

def processHasStep( hasReq,  ypos2,  statuses2,  importances2, File f2){
	ypos2 -= widthOfOneIcon
	for(int j = 0; j<statuses2.size(); j++){
		ypos2 += widthOfOneIcon
		for(int i = 0; i<importances2.size(); i++){
			println('process combination : has steps and status='+statuses2[j]+' importance='+importances2[i]+' req='+hasReq)
			f2.append('li[rel="test-case"][hasSteps="true"][isreqcovered="'+hasReq+'"][importance="'+importances2[i]+'"][status="'+statuses2[j]+'"]> a > .jstree-icon {\n')
			printBackgroundProperty(i*heightOfOneIcon, ypos2, f2)
			f2.append('}\n')
		}
	}
}



File f = new File(fileName)
boolean isNew = f.createNewFile()
if(!isNew){
	f.delete()
	println("delete existing file")
}
println("create new file : "+fileName)
f = new File(fileName)
f.append ("/** \n * smartsprites directive :\n */\n /** sprite: sprites-icons; sprite-image: url('../images/sprites-icons.png'); sprite-layout: vertical */\n\n")


//if no test step
def ypos = totalWidth - widthOfOneIcon
for(int i = 0; i<importances.size(); i++){
	println('process combination : has no steps and importance='+importances[i])
	f.append('li[rel="test-case"][hasSteps="false"][importance="'+importances[i]+'"] > a > .jstree-icon {\n')
	printBackgroundProperty(i*heightOfOneIcon, ypos, f)
	f.append('}\n')
}
//if step
//if has req
processHasStep("true", 0, statuses, importances, f);
//if has no req
processHasStep("false", statuses.size() * widthOfOneIcon, statuses, importances, f);

