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
import tools.refinery.store.monitor.internal.objectives.MonitorFitnessCriterion;
import tools.refinery.store.monitor.internal.objectives.MonitorBasedObjective;
import tools.refinery.store.monitor.senderReceiverCaseStudy.SenderReceiverInitializer;
import tools.refinery.store.monitor.senderReceiverCaseStudy.SenderReceiverMetaModel;
import tools.refinery.store.monitor.senderReceiverCaseStudy.SenderReceiverAutomaton;
import tools.refinery.store.monitor.trafficSituationCaseStudy.TrafficSituationInitializer;
import tools.refinery.store.monitor.trafficSituationCaseStudy.TrafficSituationAutomaton;
import tools.refinery.store.monitor.trafficSituationCaseStudy.TrafficSituationMetaModel;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.query.interpreter.QueryInterpreterAdapter;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.statecoding.StateCoderAdapter;
import tools.refinery.store.tuple.Tuple;
import tools.refinery.visualization.ModelVisualizerAdapter;
import tools.refinery.visualization.internal.FileFormat;
import tools.refinery.evaluation.ModelEvaluationAdapter;

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
						.exclude(new MonitorFitnessCriterion(traverser.monitor, true))
						.accept(new MonitorFitnessCriterion(traverser.monitor, false))
						.objective(new MonitorBasedObjective(traverser.monitor))
				)
				.build();

		var model = store.createEmptyModel();
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);
		var monitorAdapter = model.getAdapter(ModelMonitorAdapter.class);

		new TrafficSituationInitializer(model,	metaModel, 2, 5);
		monitorAdapter.init();

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
				.with(ModelEvaluationAdapter.builder())
				.with(DesignSpaceExplorationAdapter.builder()
						.transformations(metaModel.transformationRules)
						.exclude(new MonitorFitnessCriterion(traverser.monitor, true))
						.accept(new MonitorFitnessCriterion(traverser.monitor, false))
						.objective(new MonitorBasedObjective(traverser.monitor))
				)
				.build();

		var model = store.createEmptyModel();
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);
		var monitorAdapter = model.getAdapter(ModelMonitorAdapter.class);

		new GestureRecognitionInitializer(model, metaModel);
		monitorAdapter.init();
		var initialVersion = model.commit();
		queryEngine.flushChanges();

		var bestFirst = new BestFirstStoreManager(store, 100);
		bestFirst.startExploration(initialVersion);
		var resultStore = bestFirst.getSolutionStore();
		System.out.println("states size: " + resultStore.getSolutions().size());
		model.getAdapter(ModelVisualizerAdapter.class).visualize(bestFirst.getVisualizationStore());

		var evaluationStore = bestFirst.getEvaluationStore();
		double accuracy = model.getAdapter(ModelEvaluationAdapter.class).evaluateAccuracy(evaluationStore,
				traverser.monitor.acceptedSymbol);
	}

	@Test
		//@Disabled("This test is only for debugging purposes")
	void SenderReceiverTest() {
		Symbol<Integer> clockSymbol = Symbol.of("Clock", 0, Integer.class);
		var metaModel = new SenderReceiverMetaModel(clockSymbol);
		var stateMachine = new SenderReceiverAutomaton(metaModel).stateMachine;
		StateMachineTraversal traverser = new StateMachineTraversal(stateMachine);

		var store = ModelStore.builder()
				.symbols(metaModel.symbols)
				.symbol(clockSymbol)
				.with(StateCoderAdapter.builder())
				.with(ModificationAdapter.builder())
				.with(QueryInterpreterAdapter.builder())
				.with(ModelMonitorAdapter.builder()
						.monitor(traverser.monitor)
						.clock(clockSymbol)
						.withStateQueries())
				.with(ModelVisualizerAdapter.builder()
						.withOutputPath("sender_receiver_test_output")
						.withFormat(FileFormat.SVG)
						.withFormat(FileFormat.DOT)
						.saveStates()
						.saveDesignSpace()
				)
				.with(DesignSpaceExplorationAdapter.builder()
						.transformations(metaModel.transformationRules)
						.exclude(new MonitorFitnessCriterion(traverser.monitor, true))
						.accept(new MonitorFitnessCriterion(traverser.monitor, false))
						.objective(new MonitorBasedObjective(traverser.monitor))
				)
				.build();

		var model = store.createEmptyModel();
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);
		var monitorAdapter = model.getAdapter(ModelMonitorAdapter.class);

		var clockInterpretation = model.getInterpretation(clockSymbol);
		clockInterpretation.put(Tuple.of(), 0);

		new SenderReceiverInitializer(model, metaModel);
		monitorAdapter.init();
		var initialVersion = model.commit();
		queryEngine.flushChanges();

		var bestFirst = new BestFirstStoreManager(store, 50);
		bestFirst.startExploration(initialVersion);
		var resultStore = bestFirst.getSolutionStore();
		System.out.println("states size: " + resultStore.getSolutions().size());
		model.getAdapter(ModelVisualizerAdapter.class).visualize(bestFirst.getVisualizationStore());
	}
}
