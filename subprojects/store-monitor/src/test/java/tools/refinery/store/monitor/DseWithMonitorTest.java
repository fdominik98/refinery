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
import tools.refinery.store.monitor.caseStudies.trafficSituationDemoCaseStudy.TrafficSituationDemoMetaModel;
import tools.refinery.store.monitor.internal.StateMachineTraversal;
import tools.refinery.store.monitor.internal.objectives.DummyRandomObjective;
import tools.refinery.store.monitor.internal.objectives.MonitorFitnessAcceptCriterion;
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

import java.util.Random;

class DseWithMonitorTest {
	int[] solutions = {10};
	boolean viz = false;
	int warmups = 10;
	int meas = 30;

	@Test
		//@Disabled("This test is only for debugging purposes")
	void TrafficSituationTestForDemo() {
		int[] solutions = {2};
		var metaModel = new TrafficSituationDemoMetaModel();
		runTrajectoryGenerations(metaModel, true,
				"trafficDemoTestOutput", solutions,
				0,1,true, 0, true);
	}

	@Test
	//@Disabled("This test is only for debugging purposes")
	void TrafficSituationTest() {
		var metaModel = new TrafficSituationMetaModel();
		runTrajectoryGenerations(metaModel, viz,
				"trafficTestOutput", solutions,
				warmups,meas,	true, 0, !viz);

		runTrajectoryGenerations(metaModel, viz,
				"trafficTestOutput", solutions,
				warmups,meas,	false, 0, !viz);
	}

	@Test
		//@Disabled("This test is only for debugging purposes")
	void GestureRecognitionTest() {
		var metaModel = new GestureRecognitionMetaModel();

		/*runTrajectoryGenerations(metaModel, viz,
				"gestureTestOutput", solutions,
				warmups,meas,	true, 0, !viz);*/

		runTrajectoryGenerations(metaModel, viz,
				"gestureTestOutput", solutions,
				warmups,meas,	true, 0, !viz);
	}

	@Test
		//@Disabled("This test is only for debugging purposes")
	void SenderReceiverTest() {
		var metaModel = new SenderReceiverMetaModel();

		runTrajectoryGenerations(metaModel, viz,
				"senderReceiverTestOutput", solutions,
				warmups,meas,	true, 0, !viz);

		runTrajectoryGenerations(metaModel, viz,
				"senderReceiverTestOutput", solutions,
				warmups,meas,	false, 0, !viz);

	}

	private ModelEvaluationAdapter.EvaluationResult runTrajectoryGeneration(
			MetaModelInstance metaModel, boolean visualization, String outPath, int solutionNumber, boolean guided,
			int timeOut) {

		StateMachineTraversal traverser = new StateMachineTraversal(metaModel.createAutomaton().stateMachine);


		var designSpaceExplorationAdapterBuilder = DesignSpaceExplorationAdapter.builder()
				.transformations(metaModel.transformationRules);

		if(guided) {
			designSpaceExplorationAdapterBuilder
					//.exclude(new MonitorFitnessExcludeCriterion(traverser.monitor, metaModel.clockSymbol, timeOut))
					.objective(new MonitorBasedObjective(traverser.monitor));
		}
		else {
			designSpaceExplorationAdapterBuilder.objective(new DummyRandomObjective());
		}

		var storeBuilder = ModelStore.builder()
				.symbols(metaModel.symbols)
				.with(StateCoderAdapter.builder())
				.with(ModificationAdapter.builder())
				.with(QueryInterpreterAdapter.builder())
				.with(ModelMonitorAdapter.builder()
						.monitor(traverser.monitor)
						.clock(metaModel.clockSymbol)
						.withStateQueries())
				.with(ModelEvaluationAdapter.builder())
				.with(designSpaceExplorationAdapterBuilder
						.accept(new MonitorFitnessAcceptCriterion(traverser.monitor, metaModel.clockSymbol, timeOut)));
		if (visualization) {
			storeBuilder.with(ModelVisualizerAdapter.builder()
					.withOutputPath(outPath)
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
		System.gc();
		long seed = System.currentTimeMillis();
		System.out.println("Random seed: " + seed);
		bestFirst.startExploration(initialVersion, seed);

		if (visualization) {
			model.getAdapter(ModelVisualizerAdapter.class).visualize(bestFirst.getVisualizationStore());
		}

		return evaluationAdapter.getEvaluationResult( bestFirst.getEvaluationStore(), metaModel.symbols);
	}

	private void runTrajectoryGenerations(
			MetaModelInstance metaModel, boolean visualization, String outPath, int[] solutions, int warmUps,
			int measurements, boolean guided, int timeOut, boolean print) {

		for(int i = 0; i < solutions.length; i++) {
			System.out.println("Generating " + solutions[i] + " solutions...");
			var evaluationResults = new	ModelEvaluationAdapter.EvaluationResultContainer();

			for(int j = 0; j < warmUps; j++) {
				System.out.println("Warmup " + j);
				runTrajectoryGeneration(metaModel, false, null, solutions[i], guided, timeOut);
			}

			for(int j = 0; j < measurements - 1; j++) {
				System.out.println("Measurement " + j);
				evaluationResults.add(runTrajectoryGeneration(metaModel, false, null, solutions[i], guided
						, timeOut));
			}
			System.out.println("Measurement " + (measurements - 1));
			evaluationResults.add(runTrajectoryGeneration(metaModel, visualization, outPath, solutions[i], guided
					, timeOut));

			System.out.println("Median all version number: " + evaluationResults.medianByAllVersionNumber().allVersionNumber());
			System.out.println("Median trajectory length: " + evaluationResults.medianByTrajectoryLength().trajectoryLength());
			System.out.println("Median timespan: " + evaluationResults.medianByTimeSpan().timeSpan());
			System.out.println("Median diversity: " + evaluationResults.medianByDiversity().diversity());
			if(print) {
				evaluationResults.toFile(metaModel.getCaseStudyId(), metaModel.instance.getInstanceId(),
						solutions[i], guided);
			}
		}
	}
}
