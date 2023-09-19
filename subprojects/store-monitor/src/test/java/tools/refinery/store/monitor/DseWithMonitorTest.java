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
import tools.refinery.store.monitor.utils.SituationInitializer;
import tools.refinery.store.monitor.utils.TrafficSituationAutomaton;
import tools.refinery.store.monitor.utils.TrafficSituationMetaModel;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.query.viatra.ViatraModelQueryAdapter;
import tools.refinery.visualization.ModelVisualizerAdapter;
import tools.refinery.visualization.internal.FileFormat;

class DseWithMonitorTest {

	@Test
	void TakeOverFromLeft() {
		var metaModel = new TrafficSituationMetaModel();
		var monitor = new TrafficSituationAutomaton(metaModel);

		var store = ModelStore.builder()
				.symbols(metaModel.actorSymbol, metaModel.carSymbol, metaModel.laneSymbol,
						metaModel.pedestrianSymbol, metaModel.cellSymbol, metaModel.onCellSymbol,
						metaModel.southOfSymbol, metaModel.northOfSymbol, metaModel.eastOfSymbol,
						metaModel.westOfSymbol)
				.with(ViatraModelQueryAdapter.builder()
						.queries(metaModel.queries))
				.with(ModelMonitorAdapter.builder()
						.monitor(monitor.stateMachine))
				.with(ModelVisualizerAdapter.builder()
						.withOutputpath("test_output")
						.withFormat(FileFormat.SVG)
						.withFormat(FileFormat.DOT)
						.saveStates()
						.saveDesignSpace()
				)
				.with(DesignSpaceExplorationAdapter.builder()
						.transformations(metaModel.transformationRules)
						.strategy(new DepthFirstStrategy().withDepthLimit(3).continueIfHardObjectivesFulfilled())
				)
				.build();

		var model = store.createEmptyModel();
		var dseAdapter = model.getAdapter(DesignSpaceExplorationAdapter.class);
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);

		new SituationInitializer(model, metaModel, 4, 5);
		queryEngine.flushChanges();

		var states = dseAdapter.explore();
		System.out.println(states);
	}
}
