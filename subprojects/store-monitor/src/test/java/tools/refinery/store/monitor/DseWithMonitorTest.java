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
import tools.refinery.store.monitor.caseStudies.gestureRecognitionCaseStudy.GestureRecognitionMetaModel;
import tools.refinery.store.monitor.internal.StateMachineTraversal;
import tools.refinery.store.monitor.internal.objectives.DummyRandomObjective;
import tools.refinery.store.monitor.internal.objectives.MonitorFitnessAcceptCriterion;
import tools.refinery.store.monitor.internal.objectives.MonitorFitnessExcludeCriterion;
import tools.refinery.store.monitor.internal.objectives.MonitorBasedObjective;
import tools.refinery.store.monitor.caseStudies.senderReceiverCaseStudy.SenderReceiverMetaModel;
import tools.refinery.store.monitor.caseStudies.trafficSituationCaseStudy.TrafficSituationMetaModel;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.query.interpreter.QueryInterpreterAdapter;
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
		/*int[] solutions = {1000};
		int[] warmUps = {0};
		runTrajectoryGenerations(metaModel, true,
				"trafficTestOutput", solutions,
				warmUps,1,
				true);*/

		int[] solutions = {50, 100, 500, 1000};
		int[] warmUps = {20, 0, 0, 0};
		runTrajectoryGenerations(metaModel, false,
				null, solutions,
				warmUps,30,	false, 15);
	}

	@Test
		//@Disabled("This test is only for debugging purposes")
	void GestureRecognitionTest() {
		var metaModel = new GestureRecognitionMetaModel();
		int[] solutions = {50};
		int[] warmUps = {0};
		runTrajectoryGenerations(metaModel, true,
				"gestureTestOutput", solutions,
				warmUps,1,true, 15);
	}

	@Test
		//@Disabled("This test is only for debugging purposes")
	void SenderReceiverTest() {
		var metaModel = new SenderReceiverMetaModel();

		int[] solutions = {100};
		int[] warmUps = {0};
		runTrajectoryGenerations(metaModel, true,
				"senderReceiverTestOutput", solutions,
				warmUps,1,true, 15);

		/*int[] solutions = {50, 100, 500, 1000};
		int[] warmUps = {20, 0, 0, 0};
		runTrajectoryGenerations(metaModel, false,
				null, solutions,
				warmUps,30,	false);*/
	}

	private ModelEvaluationAdapter.EvaluationResult runTrajectoryGeneration(
			MetaModelInstance metaModel, boolean visualization, String outPath, int solutionNumber, boolean guided,
			int timeOut) {

		StateMachineTraversal traverser = new StateMachineTraversal(metaModel.createAutomaton().stateMachine);


		var designSpaceExplorationAdapterBuilder = DesignSpaceExplorationAdapter.builder()
				.transformations(metaModel.transformationRules);

		if(guided) {
			designSpaceExplorationAdapterBuilder
					.exclude(new MonitorFitnessExcludeCriterion(traverser.monitor, metaModel.clockSymbol, timeOut))
					.accept(new MonitorFitnessAcceptCriterion(traverser.monitor))
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

		metaModel.createInitializer(model);
		monitorAdapter.init();
		var initialVersion = model.commit();
		queryEngine.flushChanges();

		var bestFirst = new BestFirstStoreManager(store, solutionNumber);
		bestFirst.startExploration(initialVersion);

		if (visualization) {
			model.getAdapter(ModelVisualizerAdapter.class).visualize(bestFirst.getVisualizationStore());
		}

		return evaluationAdapter.getEvaluationResult( bestFirst.getEvaluationStore(),
				bestFirst.getSolutionStore().getSolutions().size(), metaModel.symbols);
	}

	private void runTrajectoryGenerations(
			MetaModelInstance metaModel, boolean visualization, String outPath, int[] solutions, int[] warmUps,
			int measurements, boolean guided, int timeOut) {

		var medTimeSpans = new ModelEvaluationAdapter.EvaluationResultContainer();
		var medAccuracy = new ModelEvaluationAdapter.EvaluationResultContainer();
		var medDiversity = new ModelEvaluationAdapter.EvaluationResultContainer();

		for(int i = 0; i < solutions.length; i++) {
			var evaluationResults = new	ModelEvaluationAdapter.EvaluationResultContainer();

			for(int j = 0; j < warmUps[i]; j++) {
				runTrajectoryGeneration(metaModel, visualization, outPath, solutions[i], guided, timeOut);
			}

			for(int j = 0; j < measurements; j++) {
				evaluationResults.add(runTrajectoryGeneration(metaModel, visualization, outPath, solutions[i], guided
						, timeOut));
			}
			medTimeSpans.add(evaluationResults.medianByTimeSpan());
			medAccuracy.add(evaluationResults.medianByAccuracy());
			medDiversity.add(evaluationResults.medianByDiversity());
		}
		System.out.println(medTimeSpans);
		System.out.println(medAccuracy);
		System.out.println(medDiversity);
	}
}
