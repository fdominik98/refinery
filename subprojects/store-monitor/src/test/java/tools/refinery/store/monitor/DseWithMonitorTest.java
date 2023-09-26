/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.store.monitor;

import org.junit.jupiter.api.Test;
import tools.refinery.store.dse.modification.ModificationAdapter;
import tools.refinery.store.dse.strategy.BestFirstStoreManager;
import tools.refinery.store.dse.transition.DesignSpaceExplorationAdapter;
import tools.refinery.store.model.ModelStore;
import tools.refinery.store.monitor.gestureRecognitionCaseStudy.GestureRecognitionAutomaton;
import tools.refinery.store.monitor.gestureRecognitionCaseStudy.GestureRecognitionInitializer;
import tools.refinery.store.monitor.gestureRecognitionCaseStudy.GestureRecognitionMetaModel;
import tools.refinery.store.monitor.internal.StateMachineTraversal;
import tools.refinery.store.monitor.internal.objectives.MonitorBasedCriteria;
import tools.refinery.store.monitor.internal.objectives.MonitorBasedObjective;
import tools.refinery.store.monitor.trafficSituationCaseStudy.TrafficSituationInitializer;
import tools.refinery.store.monitor.trafficSituationCaseStudy.TrafficSituationAutomaton;
import tools.refinery.store.monitor.trafficSituationCaseStudy.TrafficSituationMetaModel;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.query.interpreter.QueryInterpreterAdapter;
import tools.refinery.store.statecoding.StateCoderAdapter;
import tools.refinery.visualization.ModelVisualizerAdapter;
import tools.refinery.visualization.internal.FileFormat;

class DseWithMonitorTest {

	@Test
	//@Disabled("This test is only for debugging purposes")
	void TrafficSituationTest() {
		var metaModel = new TrafficSituationMetaModel();
		var stateMachine = new TrafficSituationAutomaton(metaModel).stateMachine;
		StateMachineTraversal traverser = new StateMachineTraversal(stateMachine);

		var store = ModelStore.builder()
				.symbols(metaModel.symbols)
				.with(StateCoderAdapter.builder())
				.with(ModificationAdapter.builder())
				.with(QueryInterpreterAdapter.builder())
				.with(ModelMonitorAdapter.builder()
						.monitor(traverser.monitor)
						.withStateQueries())
				.with(ModelVisualizerAdapter.builder()
						.withOutputPath("traffic_test_output")
						.withFormat(FileFormat.SVG)
						.withFormat(FileFormat.DOT)
						.saveStates()
						.saveDesignSpace()
				)
				.with(DesignSpaceExplorationAdapter.builder()
						.transformations(metaModel.transformationRules)
						.exclude(new MonitorBasedCriteria(traverser.monitor, true))
						.accept(new MonitorBasedCriteria(traverser.monitor, false))
						.objective(new MonitorBasedObjective(traverser.monitor))
				)
				.build();

		var model = store.createEmptyModel();
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);

		new TrafficSituationInitializer(model,	metaModel, 2, 5);
		var initialVersion = model.commit();
		queryEngine.flushChanges();

		var bestFirst = new BestFirstStoreManager(store, 50);
		bestFirst.startExploration(initialVersion);
		var resultStore = bestFirst.getSolutionStore();
		System.out.println("states size: " + resultStore.getSolutions().size());
		model.getAdapter(ModelVisualizerAdapter.class).visualize(bestFirst.getVisualizationStore());
	}

	@Test
		//@Disabled("This test is only for debugging purposes")
	void GestureRecognitionTest() {
		var metaModel = new GestureRecognitionMetaModel();
		var stateMachine = new GestureRecognitionAutomaton(metaModel).stateMachine;
		StateMachineTraversal traverser = new StateMachineTraversal(stateMachine);

		var store = ModelStore.builder()
				.symbols(metaModel.symbols)
				.with(StateCoderAdapter.builder())
				.with(ModificationAdapter.builder())
				.with(QueryInterpreterAdapter.builder())
				.with(ModelMonitorAdapter.builder()
						.monitor(traverser.monitor)
						.withStateQueries())
				.with(ModelVisualizerAdapter.builder()
						.withOutputPath("gesture_test_output")
						.withFormat(FileFormat.SVG)
						.withFormat(FileFormat.DOT)
						.saveStates()
						.saveDesignSpace()
				)
				.with(DesignSpaceExplorationAdapter.builder()
						.transformations(metaModel.transformationRules)
						.objective(new MonitorBasedObjective(traverser.monitor))
				)
				.build();

		var model = store.createEmptyModel();
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);

		new GestureRecognitionInitializer(model, metaModel);
		var initialVersion = model.commit();
		queryEngine.flushChanges();

		var bestFirst = new BestFirstStoreManager(store, 50);
		bestFirst.startExploration(initialVersion);
		var resultStore = bestFirst.getSolutionStore();
		System.out.println("states size: " + resultStore.getSolutions().size());
		model.getAdapter(ModelVisualizerAdapter.class).visualize(bestFirst.getVisualizationStore());
	}
}
