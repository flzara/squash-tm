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
package org.squashtest.tm.service.internal.chart.engine;

import com.google.common.collect.ImmutableMap;
import com.querydsl.core.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartSeries;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.query.DataType;
import org.squashtest.tm.domain.query.QueryAggregationColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.internal.repository.InfoListItemDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static org.squashtest.tm.domain.execution.ExecutionStatus.BLOCKED;
import static org.squashtest.tm.domain.execution.ExecutionStatus.ERROR;
import static org.squashtest.tm.domain.execution.ExecutionStatus.FAILURE;
import static org.squashtest.tm.domain.execution.ExecutionStatus.NOT_FOUND;
import static org.squashtest.tm.domain.execution.ExecutionStatus.NOT_RUN;
import static org.squashtest.tm.domain.execution.ExecutionStatus.READY;
import static org.squashtest.tm.domain.execution.ExecutionStatus.RUNNING;
import static org.squashtest.tm.domain.execution.ExecutionStatus.SETTLED;
import static org.squashtest.tm.domain.execution.ExecutionStatus.SUCCESS;
import static org.squashtest.tm.domain.execution.ExecutionStatus.UNTESTABLE;
import static org.squashtest.tm.domain.execution.ExecutionStatus.WARNING;



/**
 * This is a companion class for ChartDataFinder. Its role is to turn the tuples from the database
 * into instances of ChartSeries. This operation include bits of data postprocessing, for transformations
 * that we were unable or unwilling to undergo in the database.
 *
 * Usage :
 * <ol>
 * 		<li>get an instance with a Provider&lt;TupleProcessor>&gt;</li>
 * 		<li>setDefinition</li>
 * 		<li>initialize</li>
 * 		<li>process</li>
 * 		<li>createChartSeries</li>
 * </ol>
 */
@Component
@Scope("prototype")
class TupleProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(TupleProcessor.class);

	@Inject
	private InfoListItemDao infoListItemDao;

	@Inject
	private CustomFieldDao customFieldDao;

	// must be set before invocation
	DetailedChartQuery definition;


	// set during initialization
	private boolean initialized = false;
	private Comparator<Tuple> tupleComparator = null;
	private Consumer<Object[]> abscissaValuePostProcessor = null;

	// produced during processing phase
	private List<Object[]> abscissa;
	private List<List<Object>> series;
	private List<String> colours = new ArrayList<>();

	// ------ static utilities

	//ExecutionStatus order remapping, as requested by [Issue 7724]
	private static Map<ExecutionStatus, Integer> EXECUTION_STATUS_ORDER =
		new ImmutableMap.Builder<ExecutionStatus, Integer>()
			.put(READY, 1)
			.put(RUNNING, 2)
			.put(SUCCESS, 3)
			.put(SETTLED, 4)
			.put(WARNING, 5)
			.put(FAILURE, 6)
			.put(BLOCKED, 7)
			.put(ERROR, 8)
			.put(UNTESTABLE, 9)
			.put(NOT_RUN, 10)
			.put(NOT_FOUND, 11)
			.build();

	private static final Comparator<Tuple> neutralComparator = (tuple1, tuple2) -> 0;


	// ************* main methods ************************

	TupleProcessor setDefinition(DetailedChartQuery definition){
		this.definition = definition;
		return this;
	}

	TupleProcessor initialize(){
		if (definition == null){
			throw new IllegalStateException("no definition given to the TupleProcessor, this is a programming error");
		}

		initializeTupleSorter();

		initializeAbscissaPostProcessors();

		initialized = true;

		return this;
	}



	TupleProcessor process(List<Tuple> tuples){
		if (! initialized){
			throw new IllegalStateException("TupleProcessor is not initialized, this is a programming error");
		}

		List<Tuple> sortedTuples = sortTuples(tuples);

		extractAbscissaAndSeries(sortedTuples);

		extractColours();

		postProcessAbscissa();

		return this;
	}


	ChartSeries createChartSeries(){

		if (abscissa == null){
			throw new IllegalStateException("TupleProcessor has not yet processed anything, this is a programming error");
		}

		int nbMeasures = definition.getProjectionColumns().size();

		ChartSeries chartSeries = new ChartSeries();

		chartSeries.setAbscissa(abscissa);
		chartSeries.setColours(colours);

		for (int mIdx = 0; mIdx < nbMeasures; mIdx++) {
			QueryProjectionColumn projectionColumn = definition.getProjectionColumns().get(mIdx);
			chartSeries.addSerie(projectionColumn.getLabel(), series.get(mIdx));
		}

		return chartSeries;

	}


	// ****************** processors initialization ******************

	private void initializeTupleSorter(){

		List<QueryAggregationColumn> aggregationColumns = definition.getAggregationColumns();

		Comparator<Tuple> mainComparator = null;

		for (int idx=0; idx< aggregationColumns.size(); idx++) {

			QueryAggregationColumn axis = aggregationColumns.get(idx);

			Comparator<Tuple> inLoopComparator = null;

			// switch over the data
			switch(axis.getDataType()){

				// the default case : use the natural order (if the datatype is comparable and has no special sorting rules)
				default:
					inLoopComparator = comparing(defaultExtractor(idx), nullsFirst(naturalOrder()));
					break;

				// the cases below are why we are doing all of this
				case REQUIREMENT_STATUS:
				case LEVEL_ENUM:
					inLoopComparator  = comparing(levelExtractor(idx), nullsFirst(this::compareLevelEnum));
					break;

				case EXECUTION_STATUS:
					inLoopComparator = comparing(execStatusExtractor(idx), nullsFirst(this::compareExecutionStatus));
					break;

				case EXISTENCE:
				case BOOLEAN:
					inLoopComparator = comparing(boolExtractor(idx), nullsFirst(this::compareBoolean));
					break;

			}

			// if defined, assign that column comparator to the main comparator
			if (inLoopComparator  != null){
				// if the main comparator is null assign it, otherwise compose the main and the new one
				mainComparator = (mainComparator == null) ?
									 inLoopComparator :
									 mainComparator.thenComparing(inLoopComparator);
			}

		}

		tupleComparator = mainComparator;
	}


	private void initializeAbscissaPostProcessors(){

		List<QueryAggregationColumn> aggregationColumns = definition.getAggregationColumns();

		Consumer<Object[]> mainPostprocessor = null;

		for (int idx=0; idx< aggregationColumns.size(); idx++){

			QueryAggregationColumn axis = aggregationColumns.get(idx);

			Consumer<Object[]> inLoopPostProcessor = null;

			switch (axis.getDataType()){
				case INFO_LIST_ITEM:
					inLoopPostProcessor = fnReplaceCodeWithLabel(idx);
					break;
				default:
					break;
			}

			// if defined, assign that post processor to the main post processor
			if (inLoopPostProcessor != null){
				// if the main postprocessor is null, initialize with the current function, else compose
				mainPostprocessor = (mainPostprocessor == null) ?
										inLoopPostProcessor :
										mainPostprocessor.andThen(inLoopPostProcessor);
			}
		}

		abscissaValuePostProcessor = mainPostprocessor;
	}

	// ***************************** processing *********************************

	// note : all the values here should match the types handled in #initializeTupleSorter()
	private boolean isResortableType(DataType type){
		return type == DataType.EXECUTION_STATUS || type == DataType.LEVEL_ENUM || type == DataType.BOOLEAN || type == DataType.EXISTENCE;
	}

	private boolean isResortRequired(){
		List<QueryAggregationColumn> aggregationColumns = definition.getAggregationColumns();

		return aggregationColumns.stream()
				   .map(QueryAggregationColumn::getDataType)
				   .filter(this::isResortableType)
				   .findAny()
				   .isPresent();
	}

	private List<Tuple> sortTuples(List<Tuple> original){
		if (isResortRequired()){
			// not that I care of modifying the original collection,
			// but I don't want to worry of the collection type returned by the JPA query
			List<Tuple> sorted = new ArrayList<>(original);
			Collections.sort(sorted, tupleComparator);
			return sorted;
		}
		else{
			return original;
		}
	}


	// note : all the values here should match the types handled in #initializeAbscissaPostProcessors()
	private boolean isPostProcessableType(DataType type){
		return type == DataType.INFO_LIST_ITEM;
	}

	private boolean isPostProcessingRequired(){
		List<QueryAggregationColumn> aggregationColumns = definition.getAggregationColumns();

		return aggregationColumns.stream()
				   .map(QueryAggregationColumn::getDataType)
				   .filter(this::isPostProcessableType)
				   .findAny()
				   .isPresent();
	}


	private void postProcessAbscissa(){
		if (isPostProcessingRequired()){
			for (Object[] entry : abscissa){
				abscissaValuePostProcessor.accept(entry);
			}
		}
	}


	// @codetrack : imported from ChartDataFinder 1.19.0.RELEASE
	private void extractColours() {

		// here we will only get colours in the case where we work with a CUF List or an InfoList
		// colours associated to "fixed list" (some classes implement Level), the colours are on the client in colours-utils.js
		// colours-utils.js also includes the part where we fill the empty colours
		List<QueryAggregationColumn> axis = definition.getAggregationColumns();

		// if there's only one axis (with a measure => bar chart, or not => pie chart), the first axis is the one with the colors
		// if there are two (trend or cumulative chart) it's the second one
		// hence the axis.size()-1
		int lastIndex = axis.size() -1;

		QueryAggregationColumn lastAxis = axis.get(lastIndex);
		switch(lastAxis.getDataType()){

			case LIST :
				// Note : here the attributeValues are the labels of the selected CustomFieldOption
				SingleSelectField cuf = customFieldDao.findSingleSelectFieldById(lastAxis.getCufId());

				// Note : here we enforce the stream ordering with .sequential to ensure the order is preserved
				//[TM-114] When in Default case, an axis value can be null (for example, when doing a pie chart about the executions number by last Executioner).
				//Therefore, if axis values are initialized before switch block, the toString method call can result in a nullPointerException.
				//As axisValues variable is only needed in case of list or info list item type axis (which I think can't have null value), I choose to duplicate
				//code of axisValues initialization in each switch case instead of factoring initialization before switch block with a null value check on entry[lastIndex].
				List<String> axisValues = abscissa.stream().sequential().map(entry -> entry[lastIndex].toString()).distinct().collect(Collectors.toList());
				colours = axisValues.stream().sequential().map(cuf::findColourOf).collect(Collectors.toList());
				break;


			case INFO_LIST_ITEM:
				// Note : here we enforce the stream ordering with .sequential to ensure the order is preserved
				List<String> axisValues2 = abscissa.stream().sequential().map(entry -> entry[lastIndex].toString()).distinct().collect(Collectors.toList());
				// Note : here the attributeValues are the codes of the selected InfoListItem
				List<InfoListItem> items = infoListItemDao.findByCodeIn(axisValues2);

				for (String code : axisValues2) {
					for (InfoListItem item : items){
						if (item.getCode().equals(code)){
							colours.add(item.getColour());
						}
					}
				}

				break;

			default:
				break;

		}
	}


	/*
	Here we split each tuple into axis data (the abscissa) and measures data (the series).

	The tuples read from the database are structured as a matrix like this :

	Axe 1	|	Axe 2	|	...	|	Axe N	|	Measure 1	|	...	|	Measure M
	--------+-----------+-------+-----------+---------------+-------+------------
	a(11)	|	a(12)	|	...	|	a(1N)	|	m(11)		|	...	|	m(1M)
	a(21)	|	a(22)	|	...	|	a(2N)	|	m(21)		|	...	|	m(2N)
	a(31)	|	a(32)	|	...	|	a(3N)	|	m(31)		|	...	|	m(3M)

	etc, with N the number of axis and M the number of measures.

	And we need to transform it by separating the abscissa and series like the following :

	Abscissa :
	[ a(11), a(12), ..., a(1N) ]
	[ a(21), a(22), ..., a(2N) ]
	[ a(31), a(32), ..., a(3N) ]

	Series (note the transposition of the indices) :
	serie 1 (first measure) [ m(11), m(21), m(31) ]
	serie M (M-th measure)  [ m(1M), m(2M), m(3M) ]


	In the same loop we do both at once.

 	*/
	private void extractAbscissaAndSeries(List<Tuple> tuples){
		// initialize temporary structures
		int nbAxes = definition.getAggregationColumns().size();
		int nbMeasures = definition.getProjectionColumns().size();

		abscissa = new ArrayList<>();
		series = new ArrayList<List<Object>>();

		for (int me = 0; me < nbMeasures; me++) {
			List<Object> serie = new ArrayList<>(tuples.size());
			series.add(serie);
		}

		// loop
		for (Tuple tuple : tuples) {
			// create the entry for the abscissa
			Object[] axis = new Object[nbAxes];
			for (int aIdx = 0; aIdx < nbAxes; aIdx++) {
				axis[aIdx] = tuple.get(aIdx, Object.class);
			}
			abscissa.add(axis);

			// create the entries for the series
			for (int sIdx = 0; sIdx < nbMeasures; sIdx++) {
				List<Object> serie = series.get(sIdx);
				Object measureValue = tuple.get(sIdx + nbAxes, Object.class);
				serie.add(measureValue);
			}
		}
	}



	// ***************************** various operators **************************

	// ----- tuple data extractors

	private Function<Tuple, Comparable> defaultExtractor(int index){
		return (tuple) -> tuple.get(index, Comparable.class);
	}

	private Function<Tuple, Level> levelExtractor(int index){
		return (tuple) -> tuple.get(index, Level.class);
	}

	private Function<Tuple, ExecutionStatus> execStatusExtractor(int index){
		return (tuple) -> tuple.get(index, ExecutionStatus.class);
	}

	private Function<Tuple, Boolean> boolExtractor(int index) { return (tuple) -> tuple.get(index, Boolean.class); }


	// ----- tuple data comparators

	private int compareLevelEnum(Level level1, Level level2){
		return level1.getLevel() - level2.getLevel();
	}

	private int compareExecutionStatus(ExecutionStatus status1, ExecutionStatus status2){
		Integer order1 = EXECUTION_STATUS_ORDER.get(status1);
		Integer order2 = EXECUTION_STATUS_ORDER.get(status2);
		return order1 - order2;
	}

	private int compareBoolean(boolean value1, boolean value2){
		// equal values -> return 0
		if (value1 == value2) return 0;
		// else, return the tuple which has the value "true"
		return (value1 == true) ? -1 : 1;
	}

	// ------- abscissa data replacers

	/**
	 * Replace the string at the given index, interpreted as an InfoListItem code, by the
	 * label of the same item.
	 *
	 * @param index
	 * @return
	 */
	private Consumer<Object[]> fnReplaceCodeWithLabel(int index){
		return (aObject) -> {
			String code = (String)aObject[index];
			if (code != null){
				InfoListItem infoListItem = infoListItemDao.findByCode(code);
				String label = infoListItem.getLabel();
				aObject[index] = label;
			}
		};
	}

}
