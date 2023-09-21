/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.store.monitor;

import org.junit.jupiter.api.Test;
import tools.refinery.store.dse.DesignSpaceExplorationAdapter;
import tools.refinery.store.dse.strategy.BestFirstStrategy;
import tools.refinery.store.model.ModelStore;
import tools.refinery.store.monitor.internal.StateMachineTraversal;
import tools.refinery.store.monitor.internal.objectives.MonitorBasedObjective;
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
		var stateMachine = new TrafficSituationAutomaton(metaModel).stateMachine;
		StateMachineTraversal traverser = new StateMachineTraversal(stateMachine);

		var store = ModelStore.builder()
				.symbols(metaModel.symbols)
				.with(ViatraModelQueryAdapter.builder()
						.queries(metaModel.queries))
				.with(ModelMonitorAdapter.builder()
						.monitor(traverser.monitor)
						.withStateQueries())
				.with(ModelVisualizerAdapter.builder()
						.withOutputpath("test_output")
						.withFormat(FileFormat.SVG)
						.withFormat(FileFormat.DOT)
						.saveStates()
						.saveDesignSpace()
				)
				.with(DesignSpaceExplorationAdapter.builder()
						.transformations(metaModel.transformationRules)
						.strategy(new BestFirstStrategy()
								.withDepthLimit(3)
								.goOnOnlyIfFitnessIsBetter())
						.objective(new MonitorBasedObjective(traverser.monitor))
				)
				.build();

		var model = store.createEmptyModel();
		var dseAdapter = model.getAdapter(DesignSpaceExplorationAdapter.class);
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);

		new SituationInitializer(model,
				model.getAdapter(DesignSpaceExplorationAdapter.class)::createObject, metaModel, 2, 5);
		queryEngine.flushChanges();

		var states = dseAdapter.explore();
		System.out.println(states);
	}
}
