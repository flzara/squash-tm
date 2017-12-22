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
SUFFIX = 0
DTYPE = 1
ATTR = 2
ROLES = 3
QUEREF = 4

/*
 * the magic number 2 stands for CallTestStep. 
 * Long story short, it wouldn't work otherwise. 
 * This might not work either.
 */
MAGIC_CALLSTEP_CLASS = 2 

filtercount = 1

/*
 * Structure :
 *
 * definition = [
 *
 * 		<entity1> : [
 * 			columns : [
 * 				<attribute column ref> : ['labelsuffix', 'datatype', 'attribute', "list, of, roles"], -- 4 args
 * 				<calculate column ref> : ['labelsuffix', 'datatype', 'attribute', "list, of, roles", "subquery reference"] -- 5 args
 * 			],
 * 			subqueries : [
 * 				<subquery reference 1> : [
 * 					strategy : SUBQUERY | INLINED (default SUBQUERY)
 * 					joinStyle : LEFT_JOIN | INNER_JOIN (default INNER_JOIN)
 * 					measures : ['column_ref [optional operation]'],
 * 					filters : ['column_ref OPERATION value1 value2...'],
 * 					axes : ['column_ref [optional operation']]
 * 				]
 * 			]
 * 		]
 *
 * ]
 * ---------
 * 
 * About the calculated column ref :
 * 
 * the 3rd argument (attribute) is completely irrelevant, but you should use it as a hint of what this column means
 * 
 * ---------
 * 
 * About "list, of, roles" :
 * 
 * accepts either "none", "all" or explicitly "[measure, ][axis, ][filter]" in any order
 * 
 * ---------
 * 
 * The code that processes it comes after and there's not much to say on it except that it's ugly and hopefully functional 
 *
 */

def definition = [

	REQUIREMENT : [

		columns : [
			reqId : ['ID', 'NUMERIC', 'id',  "all" ],
			reqVCount : ['NB_VERSIONS', 'NUMERIC', 'count(requirementVersionCoverages)', 'all', 'reqVCountSub'],
			reqProject : ['PROJECT', 'NUMERIC', 'project.id', 'none'],
			reqCat : ['CRITICALITY', 'LEVEL_ENUM', 'resource.criticality', 'all'],
			reqStatus : ['STATUS', 'LEVEL_ENUM', 'resource.status', 'all'],
			reqCategory : ['CATEGORY', 'INFO_LIST_ITEM', 'resource.category.code', 'all']
		],

		subqueries : [
			reqVCountSub : [
				label : 'REQUIREMENT_NB_VERSIONS_SUBQUERY',
				measures : ['rvId COUNT'],
				axes : ['reqId']
			]
		]
	],

	REQUIREMENT_VERSION : [
		
		columns : [
			rvId : ['ID', 'NUMERIC', 'id', 'all' ],
			rvRef : ['REFERENCE', 'STRING', 'reference', 'filter, axis'],
			rvCat : ['CATEGORY', 'INFO_LIST_ITEM', 'category.code', 'all'],
			rvCrit : ['CRITICALITY', 'LEVEL_ENUM', 'criticality', 'all'],
			rvStatus : ['STATUS', 'LEVEL_ENUM', 'status', 'all'],
			rvCreatBy : ['CREATED_BY',  'STRING','audit.createdBy', 'axis, filter'],
			rvCreatOn : ['CREATED_ON', 'DATE','audit.createdOn',  'axis, filter'],
			rvModBy : ['MODIFIED_BY', 'STRING','audit.lastModifiedBy','axis, filter'],
			rvModOn : ['MODIFIED_ON',  'DATE', 'audit.lastModifiedOn','axis, filter'],			
			rvVersnum : ['VERS_NUM', 'NUMERIC', 'versionNumber', 'filter, measure'],
			rvVerifTcCount : ['TCCOUNT', 'NUMERIC', 'count(requirementVersionCoverages)', 'all', 'rvVerifTCCountSub'],
			rvMilesCount : ['MILCOUNT', 'NUMERIC', 'count(milestones)', 'all', 'rvMilesCountSub'],
		],
	
		subqueries : [
			rvVerifTCCountSub : [
				label : 'REQUIREMENT_VERSION_TCCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				measures : ['tcId COUNT'],
				axes : ['rvId']
			],
			rvMilesCountSub : [
				label : 'REQUIREMENT_VERSION_MILCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['rvmilId COUNT'],
				axes : ['rvId']
			]
		
		]	
		
	],

	TEST_CASE : [
		columns : [
			tcId : ['ID', 'NUMERIC', 'id', 'all'],
			tcRef : ['REFERENCE', 'STRING', 'reference', 'filter, axis'],
			tcImportance : ['IMPORTANCE', 'LEVEL_ENUM', 'importance', 'all'],
			tcNat : ['NATURE', 'INFO_LIST_ITEM', 'nature.code', 'all'],
			tcType : ['TYPE', 'INFO_LIST_ITEM', 'type.code', 'all'],
			tcStatus : ['STATUS', 'LEVEL_ENUM', 'status', 'all'],
			tcCreatBy : ['CREATED_BY', 'STRING', 'audit.createdBy', 'axis, filter'],
			tcCreatOn : ['CREATED_ON', 'DATE', 'audit.createdOn', 'axis, filter'],
			tcModBy : ['MODIFIED_BY', 'STRING', 'audit.lastModifiedBy', 'axis, filter'],			
			tcModOn : ['MODIFIED_ON', 'DATE', 'audit.lastModifiedOn', 'axis, filter'],		
			tcProject : ['PROJECT', 'NUMERIC', 'project.id', 'none'],
			tcVersionCount : ['VERSCOUNT', 'NUMERIC', 'count(requirementVersionCoverages)', 'all', 'tcVerifVersionCountSub'],
			tcCallStepsCount : ['CALLSTEPCOUNT', 'NUMERIC', 'count(steps[class="CallTestStep"])', 'all', 'tcCallStepsCountSub'],
			tcStepsCount : ['STEPCOUNT', 'NUMERIC', 'count(steps)', 'all', 'tcStepsCountSub'],
			tcMilesCount : ['MILCOUNT', 'NUMERIC', 'count(milestones)', 'all', 'tcMilesCountSub'],
			tcIterCount : ['ITERCOUNT', 'NUMERIC', 'count(iterations)', 'all', 'tcIterCountSub'],
			tcExeCount : ['EXECOUNT', 'NUMERIC', 'count(executions)', 'all', 'tcExeCountSub'],
			tcHasAutoScript : ['HASAUTOSCRIPT', 'BOOLEAN', 'notnull(automatedTest)', 'all', 'tcHasAutoSub']
		], 
	
		subqueries : [
			tcVerifVersionCountSub : [
				label : 'TEST_CASE_VERSCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['rvId COUNT'],
				axes : ['tcId']
			],
			tcCallStepsCountSub : [
				label : 'TEST_CASE_CALLSTEPCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['tsId COUNT'],
				filters : ['tsClass EQUALS '+MAGIC_CALLSTEP_CLASS], 
				axes : ['tcId']
			],
			tcStepsCountSub : [
				label : 'TEST_CASE_STEPCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['tsId COUNT'],
				axes : ['tcId']
			],
			tcMilesCountSub : [
				label : 'TEST_CASE_VERSION_MILCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['tcmilId COUNT'],
				axes : ['tcId']
			],
		
			/*
			 * About the iteration and execution count :
			 * 
			 *  According to the DomainGraph the join is performed using a where clause, from the 
			 *  ITP side. It is so because the relation is not mapped between TC and ITP.
			 *  
			 *  This means that there is no way to actually have a LEFT_JOIN when navigating 
			 *  from TC to ITP, unless one of the following solution is adopted (by increasing complexity) : 
			 *  
			 *  1/ the relationship TC -> ITP is mapped. 
			 *  ->  Simple, but burdens even more the loading of a test case and improper from the business point of view  
			 *  
			 *  2/ QueryPlanner generates a query plan backward (from measured entity to axis entity), when 
			 *  	it has to peform a joinstyle = "left_join" on a join which must use a "where join".
			 *  -> Simple, should work as long as there are no 	
			 *  
			 *  3/ the engine searches in lucene for it (there are appropriate bridges for that)
			 *  -> lots of boiler plate to do as a query preprocessing
			 *  
			 *  4/ the engine manages to leverage 'union' in subqueries when a 'where join' appears and joinstyle = 'left_join' is used
			 *  -> slows the performance, also not sure of what must be return in the second leg of the union
			 *  
			 *  Until then, test cases having no iteration/execution wont be counted in the 
			 *  subquery. It entails that 
			 *  
			 */
			tcIterCountSub : [
				label : 'TEST_CASE_ITERCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['itId COUNT'],
				axes : ['tcId']
			],
		
			tcExeCountSub : [
				label : 'TEST_CASE_EXECOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['exId COUNT'],
				axes : ['tcId']
			],
		
			tcHasAutoSub : [
				label : 'TEST_CASE_HASAUTOSCRIPT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'INLINED',
				measures : ['autoId NOT_NULL'],
				axes : ['tcId']
			]
		
		]
	],
	
	CAMPAIGN : [
		columns : [
			cId : ['ID', 'NUMERIC', 'id', 'all'],
			cProject : ['PROJECT', 'NUMERIC', 'project.id', 'none'],
			cRef : ['REFERENCE', 'STRING', 'reference', 'filter, measure'],
			cSchedStart : ['SCHED_START', 'DATE', 'scheduledPeriod.scheduledStartDate', 'axis, filter'],
			cSchedEnd : ['SCHED_END', 'DATE', 'scheduledPeriod.scheduledEndDate', 'axis, filter'],
			cActStart : ['ACTUAL_START', 'DATE', 'actualPeriod.actualStartDate', 'axis, filter'],
			cActEnd : ['ACTUAL_END', 'DATE', 'actualPeriod.actualEndDate', 'axis, filter'],	
			cIterCount : ['ITERCOUNT', 'NUMERIC', 'count(iterations)', 'all', 'cIterCountSub'],
			cIssueCount : ['ISSUECOUNT', 'NUMERIC', 'count(issues)', 'all', 'cIssueCountSub']
		], 
	
		subqueries : [
			cIterCountSub : [
				label : 'CAMPAIGN_ITERCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['itId COUNT'],
				axes : ['cId']
			],
			cIssueCountSub : [
				label : 'CAMPAIGN_ISSUECOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['isRemoteId COUNT'],
				axes : ['cId']
			]
			
		]	
		
	],
	
	ITERATION : [
		columns : [
			itId : ['ID', 'NUMERIC', 'id', 'all'],
			itRef : ['REFERENCE', 'STRING', 'reference', 'filter, measure'],
			itSchedStart : ['SCHED_START', 'DATE', 'scheduledPeriod.scheduledStartDate', 'axis, filter'],
			itSchedEnd : ['SCHED_END', 'DATE', 'scheduledPeriod.scheduledEndDate', 'axis, filter'],
			itActStart : ['ACTUAL_START', 'DATE', 'actualPeriod.actualStartDate', 'axis, filter'],
			itActEnd : ['ACTUAL_END', 'DATE', 'actualPeriod.actualEndDate', 'axis, filter'],	
			itItemCount : ['ITEMCOUNT', 'NUMERIC', 'count(testPlans)', 'all', 'itItemCountSub'],
			itIssueCount : ['ISSUECOUNT', 'NUMERIC', 'count(issues)', 'all', 'itIssueCountSub']
		], 
	
		subqueries : [
			itItemCountSub : [
				label : 'ITERATION_ITEMCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['itpId COUNT'],
				axes : ['itId']
			], 
			itIssueCountSub : [
				label : 'ITERATION_ISSUECOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['isRemoteId COUNT'],
				axes : ['itId']
			]
		]	
			
	],

	ITEM_TEST_PLAN : [		
		columns : [
			itpId : ['ID', 'NUMERIC', 'id', 'all'],
			itpLabel : ['LABEL', 'STRING', 'label', 'all'],
			itpStatus : ['STATUS', 'EXECUTION_STATUS', 'executionStatus', 'all'],
			itpLastExecOn : ['LASTEXECON', 'DATE', 'lastExecutedOn', 'all'],
			itpDataset : ['DATASET_LABEL', 'STRING', 'referencedDataset.name', 'all'],
			itpTester : ['TESTER', 'STRING', 'user.login', 'all'],
			itpTcId : ['TC_ID', 'NUMERIC', 'referencedTestCase.id', 'all'],
			itpTcDeleted : ['TC_DELETED', 'BOOLEAN', 'isnull(referencedTestCase)', 'all', 'itpTcDeletedSub'],
			itpIsExecuted : ['IS_EXECUTED', 'BOOLEAN', 'notnull(executions)', 'all', 'itpIsExecutedSub'],
			itpManExCount : ['MANEXCOUNT', 'NUMERIC', 'count(executions[auto="false"])', 'all', 'itpManExCountSub'],
			itpAutoExCount : ['AUTOEXCOUNT', 'NUMERIC', 'count(executions[auto="true"])', 'all', 'itpAutoExCountSub'],
			itpIssueCount : ['ISSUECOUNT', 'NUMERIC', 'count(issues)', 'all', 'itpIssueCountSub']
		],
	
		subqueries : [ 

			
			itpTcDeletedSub : [
				label : 'ITEM_TEST_PLAN_TCDELETED_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'INLINED',
				measures : ['tcId IS_NULL'],
				axes : ['itpId']
			],
		
			itpIsExecutedSub : [
				label : 'ITEM_TEST_PLAN_ISEXECUTED_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['exId NOT_NULL'],	
				axes : ['itpId']
			], 
		
			itpManExCountSub : [
				label : 'ITEM_TEST_PLAN_MANEXCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['exId COUNT'],
				filters : ['autoexId NOT_NULL FALSE'],
				axes : ['itpId']
			],
			
			itpAutoExCountSub : [
				label : 'ITEM_TEST_PLAN_AUTOEXCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['exId COUNT'],
				filters : ['autoexId NOT_NULL TRUE'],
				axes : ['itpId']
			],
		
			itpIssueCountSub : [
				label : 'ITEM_TEST_PLAN_ISSUECOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['isRemoteId COUNT'],
				axes : ['itpId']
			]		
		]	
	],

	EXECUTION : [
		columns : [
			exId : ['ID', 'NUMERIC', 'id', 'all'],
			exLabel : ['LABEL', 'STRING', 'name', 'axis, filter'],
			exDsLabel : ['DS_LABEL', 'STRING', 'datasetLabel', 'axis, filter'],
			exLastExec : ['LASTEXEC', 'DATE', 'lastExecutedOn', 'axis, filter'],
			exTesterLogin : ['TESTER_LOGIN', 'STRING', 'lastExecutedBy', 'axis, filter'],
			exStatus : ['STATUS', 'EXECUTION_STATUS', 'executionStatus', 'all'],
			
			exIsAuto : ['ISAUTO', 'BOOLEAN', 'notnull(automatedExecutionExtender)', 'filter', 'exIsAutoSub'],
			exIssueCount : ['ISSUECOUNT', 'NUMERIC', 'count(issues)', 'all', 'exIssueCountSub']
		],
	
		subqueries : [
			
			exIsAutoSub : [
				label : 'EXECUTION_ISAUTO_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['autoexId NOT_NULL'],
				axes : ['exId']
			],
		
			exIssueCountSub : [
				label : 'EXECUTION_ISSUECOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['isRemoteId COUNT'],
				axes : ['exId']
			]
		]		
	],

	ISSUE : [
		columns : [
			isId : ['ID', 'NUMERIC', 'id', 'none'],
			isRemoteId : ['REMOTE_ID', 'STRING', 'remoteIssueId', 'none'],
			isStatus : ['STATUS', 'STRING', 'status', 'none'],
			isSeverity : ['SEVERITY', 'STRING', 'severity', 'none'],
			isBugtrackerLabel : ['BUGTRACKER', 'STRING', 'bugtracker', 'none']	
		],
	
		subqueries : [:]		
		
	],

	TEST_CASE_STEP : [
		columns : [
			tsId : ['ID', 'NUMERIC', 'id', 'none'],
			tsClass : ['CLASS', 'NUMERIC', 'class', 'none']	
		],
	
		subqueries : [:]			
	],
	
	TEST_CASE_NATURE : [
		columns : [
			tcnatId : ['ID', 'NUMERIC', 'id', 'none'],
			tcnatLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	TEST_CASE_TYPE : [
		columns : [
			tctypId : ['ID', 'NUMERIC', 'id', 'none'],
			tctypLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	REQUIREMENT_VERSION_CATEGORY : [
		columns : [
			rvcatId : ['ID', 'NUMERIC', 'id', 'none'],
			rvcatLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	TEST_CASE_MILESTONE : [
		columns : [
			tcmilId : ['ID', 'NUMERIC', 'id', 'none'],
			tcmilLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	REQUIREMENT_VERSION_MILESTONE : [
		columns : [
			rvmilId : ['ID', 'NUMERIC', 'id', 'none'],
			rvmilLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	ITERATION_TEST_PLAN_ASSIGNED_USER : [
		columns : [
			itpassignId : ['ID', 'NUMERIC', 'id', 'none'],
			itpassignLogin : ['LOGIN', 'STRING', 'login', 'none']	
		],
	
		subqueries : [:]
			
	],

	AUTOMATED_TEST : [
		columns : [
			autoId : ['ID', 'NUMERIC', 'id', 'none']	
		],
	
		subqueries : [:]
	],

	AUTOMATED_EXECUTION_EXTENDER : [
		columns : [
			autoexId : ['ID', 'NUMERIC', 'id', 'none']	
		],
		subqueries : [:]
	],


]


// *********************** main ************************

idmap = createIDmap(definition)

toSQL(definition)



// ********************* functions *********************


// ids start at 1, not 0. If you try to count from 0, mysql will rant because of that
def createIDmap(definition){

	def colcount = 1
	def querycount = 1
	def idmap = [:]

	definition.each { entity, content ->
		// ids for columns
		content['columns'].each { colid, coldef ->
			idmap[colid] = colcount++
		}
		
		// ids for subqueries
		content['subqueries'].each { querid, querdef ->
			idmap[querid] = querycount++
		}
		
	}

	idmap
}



def toSQL(definition){

	File output = new File("chart-column-prototypes.sql")

	if (output.exists()){
		output.delete()
		output.createNewFile()
	}


	// first, process only attribute columns
	output.append """
-- --------------------------------------------
-- Chart Column Prototype Definition --
--
-- generated by src/main/script/MakeChartColumns.groovy
-- --------------------------------------------
"""

output.append """
-- -------------------------------------------
-- section 1 :  basic attribute columns
-- -------------------------------------------
\n"""
	
	definition.each{ entity, content ->
		

		def attributeColumns = content['columns'].findAll{
			it.value.size() == 4
		}
		
		if (attributeColumns.size()>0){
			processColumns(output, entity, attributeColumns)
		}
		
		if (haveRoles(attributeColumns)){
			processRoles(output, attributeColumns)
		}

		output.append "\n\n"
		
	}
	
	// now , insert subqueries
	output.append """
-- -------------------------------------------
-- section 2 :  subqueries
-- -------------------------------------------
\n"""
	
	definition.each { entity, content ->
		
		
		
		def subqueries = content['subqueries']
		
		if (!subqueries.empty){
			processSubqueries(entity, output, subqueries)
		}
		
	}

	// now insert the calculated columns that reference those subqueries
	output.append """
-- -------------------------------------------
-- section 3 :  calculated columns
-- -------------------------------------------
\n"""
	
	definition.each { entity, content ->
		
		
		
		def calculatedColumns = content['columns'].findAll{
			it.value.size() == 5
		}
		
		if (calculatedColumns.size() > 0){
			processColumns(output, entity, calculatedColumns)
		}
		
		if (haveRoles(calculatedColumns)){
			processRoles(output, calculatedColumns)
		}

		output.append "\n\n"
	}

}


def haveRoles(attributeColumns){
	
	def col = attributeColumns.find{ colid, coldef ->
		
		return (coldef[ROLES] != 'none')		
		
	}
	
}

def processColumns(output, entity, attributeColumns){
	
	output.append "\n-- columns for entity : ${entity} --\n\n"

	output.append  """insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values"""

	def colvalues = ""

	attributeColumns.each{ id, coldef ->
		colvalues += printColumn(entity, idmap[id], coldef)
	}

	output.append (colvalues.replaceAll(/,$/, ';\n\n'))

	
}

def printColumn(entity, id, coldef){
	def coltype = (coldef.size() <5) ? 'ATTRIBUTE' : 'CALCULATED'
	def business = (coldef[ROLES] == "none") ? 'FALSE' : 'TRUE'
	def label = entity + '_' + coldef[SUFFIX]

	def typrol = typerole(entity)
	def entityType = typrol[0]
	def entityRole = (typrol[1] == null) ? null : "'"+typrol[1]+"'"

	def datatype = coldef[DTYPE]
	def attname = coldef[ATTR]
	def queryid = (coldef.size() < 5 ) ? null : idmap[coldef[QUEREF]]

	return "\n\t($id, '$coltype', $business, '$label', '$entityType', $entityRole, '$datatype', '$attname', $queryid),"

}

def processRoles(output, attributeColumns){
	
	output.append """insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values"""

	def rolevalues = ""

	attributeColumns.each { id, coldef ->
		rolevalues += printRoles(idmap[id], coldef[ROLES])
	}

	output.append (rolevalues.replaceAll(/,$/, ';\n\n'))

}

def printRoles(id, roles){
	def roleinserts = "\n\t"
	
	def localroles = roles
	
	if (localroles == "none"){
		roleinserts = ""
	}
	else{
		if (localroles == "all"){
			localroles = "measure, axis, filter"
		}
		def tokens = localroles.split(',').collect{it.trim()}
	
		tokens.each{
			roleinserts +=" ($id, '${it.toUpperCase()}'),"
		}
	}

	return roleinserts;

}

def processSubqueries(entity, output, subqueries){
	
	output.append """-- subqueries for entity $entity --\n\n"""

	subqueries.each{ querid, querdef ->
		
		output.append """-- subquery $querid --\n\n"""

		// insert the query entry
		def basequeryid = idmap[querid]
		def label = querdef.label
		def strategy = (querdef['strategy'] != null) ? querdef['strategy'] : 'SUBQUERY'
		def joinStyle = (querdef['joinStyle'] != null) ? querdef['joinStyle'] : 'INNER_JOIN'
		
		output.append """insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values ($basequeryid, '$label', '$strategy', '$joinStyle');\n"""
		
		// insert the measures for it
		querdef['measures'].eachWithIndex { item, index ->
			def splitname = item.split()
			def colref = splitname[0]
			def operation = (splitname.size() > 1) ? splitname[1] : 'NONE'
			def id = idmap[colref]
			
			output.append """insert into CHART_MEASURE_COLUMN(CHART_COLUMN_ID, QUERY_ID, MEASURE_OPERATION, MEASURE_RANK) \
values ($id, $basequeryid, '$operation', $index);\n"""
		}
		
		
		// insert the filters (if any)
		querdef['filters'].eachWithIndex {item, index ->
			def splitargs = item.split()
			def colref = splitargs[0]
			def operation = splitargs[1]
			def values = (splitargs.length > 2) ? splitargs[2..splitargs.size()-1] : []
			def colrefid = idmap[colref]
			def filterid = filtercount++
			
			// the filter
			output.append """insert into CHART_FILTER(FILTER_ID, CHART_COLUMN_ID, QUERY_ID, FILTER_OPERATION) \
values ($filterid, $colrefid, $basequeryid, '$operation');\n"""
			
			// its filter values 
			output.append """insert into CHART_FILTER_VALUES(FILTER_ID, FILTER_VALUE)
values """
			def valuestring = ""
			values.each{
				valuestring += "\n\t($filterid,'$it'),"
			}
			output.append (valuestring.replaceAll(/,$/, ';\n\n'))
		}
		
		
		// insert the axes
		querdef['axes'].eachWithIndex { item, index ->
			def splitname = item.split()
			def colref = splitname[0]
			def operation = (splitname.size() > 1) ? splitname[1] : 'NONE'
			def id = idmap[colref]
			
			output.append """insert into CHART_AXIS_COLUMN(CHART_COLUMN_ID, QUERY_ID, AXIS_OPERATION, AXIS_RANK)  \
values ($id, $basequeryid, '$operation', $index);\n\n\n"""
		}
		output.append "\n"
		
	}  
	
}

def typerole(tpname){
	switch(tpname){
		case "REQUIREMENT" : return ["REQUIREMENT", null]
		case "REQUIREMENT_VERSION" : return ["REQUIREMENT_VERSION", null]
		case "TEST_CASE" : return ["TEST_CASE", null]
		case "CAMPAIGN" : return ["CAMPAIGN", null]
		case "ITERATION" : return ["ITERATION", null]
		case "ITEM_TEST_PLAN" : return ["ITEM_TEST_PLAN", null]
		case "EXECUTION" : return ["EXECUTION", null]
		case "ISSUE" : return ["ISSUE", null]
		case "TEST_CASE_STEP" : return ["TEST_CASE_STEP", null]
		case "TEST_CASE_NATURE" : return ["INFO_LIST_ITEM", "TEST_CASE_NATURE"]
		case "TEST_CASE_TYPE" : return ["INFO_LIST_ITEM", "TEST_CASE_TYPE"]
		case "REQUIREMENT_VERSION_CATEGORY" : return ["INFO_LIST_ITEM", "REQUIREMENT_VERSION_CATEGORY"]
		case "TEST_CASE_MILESTONE" : return ["MILESTONE", "TEST_CASE_MILESTONE"]
		case "REQUIREMENT_VERSION_MILESTONE" : return ["MILESTONE", "REQUIREMENT_VERSION_MILESTONE"]
		case "ITERATION_TEST_PLAN_ASSIGNED_USER" : return ["USER", "ITERATION_TEST_PLAN_ASSIGNED_USER"]
		case "AUTOMATED_TEST" : return ["AUTOMATED_TEST", null]
		case "AUTOMATED_EXECUTION_EXTENDER" : return ["AUTOMATED_EXECUTION_EXTENDER", null]
	}
}





