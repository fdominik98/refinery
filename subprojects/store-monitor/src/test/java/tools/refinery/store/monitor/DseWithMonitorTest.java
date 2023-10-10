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
import tools.refinery.store.monitor.caseStudies.MetaModelInstance;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.monitor.caseStudies.gestureRecognitionCaseStudy.GestureRecognitionMetaModel;
import tools.refinery.store.monitor.internal.StateMachineTraversal;
import tools.refinery.store.monitor.internal.objectives.DummyRandomObjective;
import tools.refinery.store.monitor.internal.objectives.MonitorFitnessCriterion;
import tools.refinery.store.monitor.internal.objectives.MonitorBasedObjective;
import tools.refinery.store.monitor.caseStudies.senderReceiverCaseStudy.SenderReceiverMetaModel;
import tools.refinery.store.monitor.caseStudies.trafficSituationCaseStudy.TrafficSituationMetaModel;
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
		runTrajectoryGenerations(metaModel, true, "traffic_test_output", 100, 0, 1, true);
	}

	@Test
		//@Disabled("This test is only for debugging purposes")
	void GestureRecognitionTest() {
		var metaModel = new GestureRecognitionMetaModel();
		var results = runTrajectoryGenerations(metaModel, true,
				"gesture_test_output", 100, 0, 1, true);
		System.out.println(results);
	}

	@Test
		//@Disabled("This test is only for debugging purposes")
	void SenderReceiverTest() {
		//runTrajectoryGenerations(metaModel, true, "sender_receiver_test_output",
		//		1000, 0, 1);

		var medTimeSpans = new ModelEvaluationAdapter.EvaluationResultContainer();
		var medAccuracy = new ModelEvaluationAdapter.EvaluationResultContainer();
		var medDiversity = new ModelEvaluationAdapter.EvaluationResultContainer();

		int[] solutions = {50, 100, 500, 1000};
		int[] warmUps = {20, 0, 0, 0};

		for(int i = 0; i < solutions.length; i++) {
			Symbol<Integer> clockSymbol = Symbol.of("Clock", 0, Integer.class);
			var metaModel = new SenderReceiverMetaModel(clockSymbol);
			var res = runTrajectoryGenerations(metaModel, false, null, solutions[i], warmUps[i], 30, false);
			medTimeSpans.add(res.medianByTimeSpan());
			medAccuracy.add(res.medianByAccuracy());
			medDiversity.add(res.medianByDiversity());
		}
		System.out.println(medTimeSpans);
		System.out.println(medAccuracy);
		System.out.println(medDiversity);
	}

	private ModelEvaluationAdapter.EvaluationResult runTrajectoryGeneration(
			MetaModelInstance metaModel, boolean visualization, String outPath, int solutions, boolean guided) {

		StateMachineTraversal traverser = new StateMachineTraversal(metaModel.createAutomaton().stateMachine);


		var designSpaceExplorationAdapterBuilder = DesignSpaceExplorationAdapter.builder()
				.transformations(metaModel.transformationRules);

		if(guided) {
			designSpaceExplorationAdapterBuilder
					.exclude(new MonitorFitnessCriterion(traverser.monitor, true))
					.accept(new MonitorFitnessCriterion(traverser.monitor, false))
					.objective(new MonitorBasedObjective(traverser.monitor));
		}
		else {
			designSpaceExplorationAdapterBuilder.objective(new DummyRandomObjective());
		}

		var storeBuilder = ModelStore.builder()
				.symbols(metaModel.symbolsWithClock)
				.with(StateCoderAdapter.builder())
				.with(ModificationAdapter.builder())
				.with(QueryInterpreterAdapter.builder())
				.with(ModelMonitorAdapter.builder()
						.monitor(traverser.monitor)
						.clock(metaModel.clockSymbol)
						.withStateQueries())
				.with(ModelEvaluationAdapter.builder()
						.acceptanceSymbol(traverser.monitor.acceptanceSymbol))
				.with(designSpaceExplorationAdapterBuilder);
		if (visualization) {
			storeBuilder.with(ModelVisualizerAdapter.builder()
					.withOutputPath(outPath)
					.withFormat(FileFormat.SVG)
					.withFormat(FileFormat.DOT)
					.saveStates()
					.saveDesignSpace()
			);
		}
		var store = storeBuilder.build();

		var model = store.createEmptyModel();
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);
		var monitorAdapter = model.getAdapter(ModelMonitorAdapter.class);
		var evaluationAdapter = model.getAdapter(ModelEvaluationAdapter.class);

		if(metaModel.clockSymbol != null) {
			var clockInterpretation = model.getInterpretation(metaModel.clockSymbol);
			clockInterpretation.put(Tuple.of(), 0);
		}

		ModelInitializer initializer = metaModel.createInitializer(model);
		monitorAdapter.init();
		var initialVersion = model.commit();
		queryEngine.flushChanges();

		var bestFirst = new BestFirstStoreManager(store, solutions);
		bestFirst.startExploration(initialVersion);
		var resultStore = bestFirst.getSolutionStore();
		System.out.println("states size: " + resultStore.getSolutions().size());

		if (visualization) {
			model.getAdapter(ModelVisualizerAdapter.class).visualize(bestFirst.getVisualizationStore());
		}

		var evaluationStore = bestFirst.getEvaluationStore();
		return evaluationAdapter.getEvaluationResult(evaluationStore, metaModel.symbols);
	}

	private ModelEvaluationAdapter.EvaluationResultContainer runTrajectoryGenerations(
			MetaModelInstance metaModel, boolean visualization, String outPath, int solutions, int warmUp,
			int measurements, boolean guided) {

		var evaluationResults = new	ModelEvaluationAdapter.EvaluationResultContainer();

		for(int i = 0; i < warmUp; i++) {
			runTrajectoryGeneration(metaModel, visualization, outPath, solutions, guided);
		}

		for(int i = 0; i < measurements; i++) {
			evaluationResults.add(runTrajectoryGeneration(metaModel, visualization, outPath, solutions, guided));
		}

		return evaluationResults;
	}
}
