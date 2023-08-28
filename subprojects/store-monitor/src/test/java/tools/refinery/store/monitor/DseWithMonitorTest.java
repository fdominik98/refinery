/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.store.monitor;

import org.junit.jupiter.api.Test;
import tools.refinery.store.dse.DesignSpaceExplorationAdapter;
import tools.refinery.store.dse.strategy.BestFirstStrategy;
import tools.refinery.store.dse.strategy.DepthFirstStrategy;
import tools.refinery.store.model.ModelStore;
import tools.refinery.store.monitor.utils.TrafficSituationMetaModel;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.viatra.ViatraModelQueryAdapter;
import tools.refinery.store.representation.Symbol;

import java.lang.reflect.Array;
import java.util.List;

class DseWithMonitorTest {

	public static <T> List<T> flattenArray(T[][] twoDArray, Class<T> clazz) {
		int rows = twoDArray.length;
		int cols = twoDArray[0].length;
		T[] oneDArray = (T[]) Array.newInstance(clazz, rows * cols);

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				oneDArray[i * cols + j] = twoDArray[i][j];
			}
		}
		return List.of(oneDArray);
	}

	@Test
	void TakeOverFromLeft() {
		var metaModel = new TrafficSituationMetaModel();

		var grid = flattenArray(metaModel.grid, Symbol.class);

		var store = ModelStore.builder()
				.symbols(metaModel.actorSymbol, metaModel.carSymbol, metaModel.laneSymbol, metaModel.pedestrianSymbol)
				.symbols(grid)
				.with(ViatraModelQueryAdapter.builder()
						.queries(metaModel.movePreconditions))
				.with(DesignSpaceExplorationAdapter.builder()
						.transformations(metaModel.transformationRules)
						.strategy(new BestFirstStrategy().withDepthLimit(0))
				)
				.build();

		var model = store.createEmptyModel();
		var dseAdapter = model.getAdapter(DesignSpaceExplorationAdapter.class);
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);

		var laneInterpretation = model.getInterpretation(metaModel.laneSymbol);
		var actorInterpretation = model.getInterpretation(metaModel.actorSymbol);
		var carInterpretation = model.getInterpretation(metaModel.carSymbol);
		var pedestrianInterpretation = model.getInterpretation(metaModel.pedestrianSymbol);

		var actor1 = dseAdapter.createObject();
		var actor2 = dseAdapter.createObject();

		carInterpretation.put(actor1, true);
		actorInterpretation.put(actor1, true);
		model.getInterpretation(metaModel.grid[10][1]).put(actor1, true);
		carInterpretation.put(actor2, true);
		actorInterpretation.put(actor2, true);
		model.getInterpretation(metaModel.grid[10][2]).put(actor2, true);

		queryEngine.flushChanges();

		var states = dseAdapter.explore();
		System.out.println(states);
	}
}
