/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.store.monitor;

import org.junit.jupiter.api.Test;
import tools.refinery.store.dse.DesignSpaceExplorationAdapter;
import tools.refinery.store.monitor.internal.StateMachineTraversal;
import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.store.model.ModelStore;
import tools.refinery.store.monitor.internal.timeProviders.TimeProviderMock;
import tools.refinery.store.monitor.utils.SituationInitializer;
import tools.refinery.store.monitor.utils.TrafficSituationAutomaton;
import tools.refinery.store.monitor.utils.TrafficSituationMetaModel;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.term.Variable;
import tools.refinery.store.query.viatra.ViatraModelQueryAdapter;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;
import java.util.List;
import java.util.Map;
import static tools.refinery.store.monitor.utils.QueryAssertions.assertResults;

class AdapterTest {
	private static final Symbol<Boolean> actor = Symbol.of("Actor", 1);
	private static final KeyOnlyView actorView = new KeyOnlyView<>(actor);
	private static final Symbol<Boolean> hasBehind = Symbol.of("HasBehind", 2);
	private static final KeyOnlyView hasBehindView = new KeyOnlyView<>(hasBehind);

	@Test
	void TestTimedStateMachine() {

		StateMachine sm = new StateMachine();
		var s2 = sm.createState();
		var s3 = sm.createState();

		var c1 = Variable.of("c1");
		var c2 = Variable.of("c2");
		var a1 = Variable.of("a1");
		Clock clock1 = new Clock("clock1");

		TimeProviderMock timeProvider = new TimeProviderMock();


		var guard1 = Guard.of(Query.of(builder -> {
			builder.parameters(c1, c2);
			builder.clause(hasBehindView.call(c1, c2));
		}), new ClockGreaterThanTimeConstraint(clock1, 4));

		var lessThan2Guard = new ClockLessThanTimeConstraint(clock1, 2);

		var guard2 = Guard.of(Query.of(builder -> {
			builder.parameters(a1);
			builder.clause(actorView.call(a1));
		}), lessThan2Guard);

		var guard3 = Guard.of(lessThan2Guard);

		sm.createTransition(sm.startState, guard1, s2, new ClockResetAction(clock1));
		sm.createTransition(s2, guard2, sm.startState, new ClockResetAction(clock1));
		sm.createTransition(s2, guard3, s3, new ClockResetAction(clock1));

		StateMachineTraversal traverser = new StateMachineTraversal(sm);

		var store = ModelStore.builder()
				.symbols(hasBehind, actor)
				.with(ViatraModelQueryAdapter.builder())
				.with(ModelMonitorAdapter.builder()
						.monitor(traverser.monitor)
						.timeProvider(timeProvider)
						.withStateQueries())
				.build();

		var model = store.createEmptyModel();
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);

		var monitor = model.getAdapter(ModelMonitorAdapter.class);
		var stateSymbols = monitor.getMonitor();

		var actorInterpretation = model.getInterpretation(actor);
		var hasBehindInterpretation = model.getInterpretation(hasBehind);

		var inState2Results = queryEngine.getResultSet(stateSymbols.get(s2, List.of(c1, c2)).query);
		var inState1Results = queryEngine.getResultSet(stateSymbols.get(sm.startState, List.of()).query);
		var inState1Results2 =
				queryEngine.getResultSet(stateSymbols.get(sm.startState, List.of(c1, c2, a1)).query);
		var inState3Results = queryEngine.getResultSet(stateSymbols.get(s3, List.of(c1, c2)).query);
		var inState3Results2 = queryEngine.getResultSet(stateSymbols.get(s3, List.of(c1, c2, a1)).query);

		// Init model
		hasBehindInterpretation.put(Tuple.of(0, 2), true);
		queryEngine.flushChanges();

		timeProvider.stepTime(4);

		assertResults(Map.of(Tuple.of(), true), inState1Results);
		assertResults(Map.of(Tuple.of(0, 2), false), inState2Results);
		assertResults(Map.of(Tuple.of(0, 2), false), inState3Results);
		assertResults(Map.of(
				Tuple.of(0, 2, 0), false,
				Tuple.of(0, 2, 1), false), inState3Results2);
		assertResults(Map.of(
				Tuple.of(0, 2, 0), false,
				Tuple.of(0, 2, 1), false), inState1Results2);

		timeProvider.stepTime(1);

		assertResults(Map.of(Tuple.of(), false), inState1Results);
		assertResults(Map.of(Tuple.of(0, 2), true), inState2Results);
		assertResults(Map.of(Tuple.of(0, 2), false), inState3Results);
		assertResults(Map.of(
				Tuple.of(0, 2, 0), false,
				Tuple.of(0, 2, 1), false), inState3Results2);
		assertResults(Map.of(
				Tuple.of(0, 2, 0), false,
				Tuple.of(0, 2, 1), false), inState1Results2);

		actorInterpretation.put(Tuple.of(0), true);
		actorInterpretation.put(Tuple.of(1), true);
		hasBehindInterpretation.put(Tuple.of(0, 2), false);
		queryEngine.flushChanges();

		timeProvider.stepTime(1);

		assertResults(Map.of(Tuple.of(), false), inState1Results);
		assertResults(Map.of(Tuple.of(0, 2), false), inState2Results);
		assertResults(Map.of(Tuple.of(0, 2), true), inState3Results);
		assertResults(Map.of(
				Tuple.of(0, 2, 0), false,
				Tuple.of(0, 2, 1), false), inState3Results2);
		assertResults(Map.of(
				Tuple.of(0, 2, 0), true,
				Tuple.of(0, 2, 1), true), inState1Results2);

		timeProvider.stepTime(5);

		assertResults(Map.of(Tuple.of(), false), inState1Results);
		assertResults(Map.of(Tuple.of(0, 2), false), inState2Results);
		assertResults(Map.of(Tuple.of(0, 2), true), inState3Results);
		assertResults(Map.of(
				Tuple.of(0, 2, 0), false,
				Tuple.of(0, 2, 1), false), inState3Results2);
		assertResults(Map.of(
				Tuple.of(0, 2, 0), true,
				Tuple.of(0, 2, 1), true), inState1Results2);
	}

	int modelSize = 0;
	public Tuple createObject() {
		return Tuple.of(modelSize++);
	}

	@Test
	void TestTrafficSituationStateMachine() {
		var metaModel = new TrafficSituationMetaModel();
		var scenario = new TrafficSituationAutomaton(metaModel);
		StateMachineTraversal traverser = new StateMachineTraversal(scenario.stateMachine);

		var store = ModelStore.builder()
				.symbols(metaModel.symbols)
				.with(ViatraModelQueryAdapter.builder()
						.queries(metaModel.queries))
				.with(ModelMonitorAdapter.builder()
						.monitor(traverser.monitor)
						.withStateQueries())
				.build();

		var model = store.createEmptyModel();
		var queryEngine = model.getAdapter(ModelQueryAdapter.class);

		var monitor = model.getAdapter(ModelMonitorAdapter.class);
		var stateSymbols = monitor.getMonitor();

		var inState1Results = queryEngine.getResultSet(stateSymbols.get(scenario.s1, List.of()).query);
		var inState2Results = queryEngine.getResultSet(stateSymbols.get(scenario.s2, List.of(scenario.a1,
				scenario.a2)).query);
		var inState3Results = queryEngine.getResultSet(stateSymbols.get(scenario.s3,
				List.of(scenario.a1, scenario.a2)).query);
		var inState4Results = queryEngine.getResultSet(stateSymbols.get(scenario.s4, List.of(scenario.a1,
				scenario.a2)).query);

		// Init model
		SituationInitializer initializer = new SituationInitializer(model, this::createObject, metaModel, 2,
				5);

		Tuple actors = Tuple.of(initializer.actor1.get(0), initializer.actor2.get(0));

		queryEngine.flushChanges();

		assertResults(Map.of(Tuple.of(), true), inState1Results);
		assertResults(Map.of(actors, false), inState2Results);
		assertResults(Map.of(actors, false), inState3Results);
		assertResults(Map.of(actors, false), inState4Results);

		model.commit();

		assertResults(Map.of(Tuple.of(), false), inState1Results);
		assertResults(Map.of(actors, true), inState2Results);
		assertResults(Map.of(actors, false), inState3Results);
		assertResults(Map.of(actors, false), inState4Results);

		initializer.onCellInterpretation.put(Tuple.of(initializer.actor1.get(0), initializer.grid[0][0].get(0)), false);
		initializer.onCellInterpretation.put(Tuple.of(initializer.actor1.get(0), initializer.grid[1][0].get(0)), true);

		queryEngine.flushChanges();
		model.commit();

		assertResults(Map.of(Tuple.of(), false), inState1Results);
		assertResults(Map.of(actors, false), inState2Results);
		assertResults(Map.of(actors, true), inState3Results);
		assertResults(Map.of(actors, false), inState4Results);

		initializer.onCellInterpretation.put(Tuple.of(initializer.actor1.get(0), initializer.grid[1][0].get(0)), false);
		initializer.onCellInterpretation.put(Tuple.of(initializer.actor1.get(0), initializer.grid[1][1].get(0)), true);

		queryEngine.flushChanges();
		model.commit();

		assertResults(Map.of(Tuple.of(), false), inState1Results);
		assertResults(Map.of(actors, false), inState2Results);
		assertResults(Map.of(actors, false), inState3Results);
		assertResults(Map.of(actors, true), inState4Results);
	}
}
