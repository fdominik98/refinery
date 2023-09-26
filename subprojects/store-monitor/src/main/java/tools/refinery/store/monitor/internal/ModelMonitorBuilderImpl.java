package tools.refinery.store.monitor.internal;

import tools.refinery.store.monitor.internal.timeProviders.AbstractTimeProvider;
import tools.refinery.store.monitor.ModelMonitorBuilder;
import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.store.adapter.AbstractModelAdapterBuilder;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelStore;
import tools.refinery.store.model.ModelStoreBuilder;
import tools.refinery.store.monitor.internal.terms.ClockValueTerm;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.query.ModelQueryBuilder;
import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.literal.*;
import tools.refinery.store.query.term.ConstantTerm;
import tools.refinery.store.query.term.DataVariable;
import tools.refinery.store.query.term.NodeVariable;
import tools.refinery.store.query.term.Variable;
import tools.refinery.store.query.view.FunctionView;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;
import java.util.*;
import java.util.function.BiConsumer;
import static tools.refinery.store.query.literal.Literals.check;
import static tools.refinery.store.query.term.int_.IntTerms.*;

public class ModelMonitorBuilderImpl extends AbstractModelAdapterBuilder<ModelMonitorStoreAdapterImpl>
		implements ModelMonitorBuilder{

	private final List<BiConsumer<Model, Integer>> actionSet = new ArrayList<>();
	private final Set<Query> querySet = new HashSet<>();
	private Monitor monitor;
	private AbstractTimeProvider timeProvider;


	@Override
	protected ModelMonitorStoreAdapterImpl doBuild(ModelStore store) {
		return new ModelMonitorStoreAdapterImpl(store, timeProvider, actionSet, monitor);
	}

	@Override
	public ModelMonitorBuilder monitor(Monitor monitor) {
		checkNotConfigured();
		this.monitor = monitor;
		return this;
	}

	@Override
	public ModelMonitorBuilder timeProvider(AbstractTimeProvider timeProvider) {
		checkNotConfigured();
		this.timeProvider = timeProvider;
		return this;
	}

	@Override
	public ModelMonitorBuilder withStateQueries() {
		checkNotConfigured();
		querySet.addAll(monitor.queryList);
		return this;
	}

	@Override
	protected void doConfigure(ModelStoreBuilder storeBuilder) {
		storeBuilder.symbols(monitor.symbolList);
		storeBuilder.symbol(monitor.fitnessSymbol);

		if (timeProvider != null){
			BiConsumer<Model, Integer> refreshTimeAction = (model, now) -> {
				var clockInterpretation = model.getInterpretation(timeProvider.clockSymbol);
				clockInterpretation.put(Tuple.of(), now);
				var queryEngine = model.getAdapter(ModelQueryAdapter.class);
				queryEngine.flushChanges();
			};
			actionSet.add(refreshTimeAction);
			storeBuilder.symbols(timeProvider.clockSymbol);
		}

		for (Transition t : monitor.stateMachine.transitions) {
			for(List<NodeVariable> fromParamList : monitor.get(t.from).keySet()){
				Symbol<ClockHolder> fromStateSymbol = monitor.get(t.from, fromParamList).symbol;

				var query = Query.of(t + "_query", ClockHolder.class, (builder, output) -> {
					List<Literal> literals = new ArrayList<>();

					builder.parameters(fromParamList);
					List<Variable> stateVariables = new ArrayList<>(fromParamList);
					stateVariables.add(output);

					var stateView = new FunctionView<>(fromStateSymbol);
					literals.add(stateView.call(CallPolarity.POSITIVE, stateVariables));

					if(t.guard.query != null){
						List<NodeVariable> newVariables = new ArrayList<>(t.getParameters());
						newVariables.removeAll(fromParamList);
						builder.parameters(newVariables);
						if (t.guard.negated){
							literals.add(t.guard.query.call(CallPolarity.NEGATIVE, t.getParameters()));
						}
						else {
							literals.add(t.guard.query.call(CallPolarity.POSITIVE, t.getParameters()));
						}
					}

					if(t.guard.timeConstraints.length != 0){
						var clockView = new FunctionView<>(timeProvider.clockSymbol);
						DataVariable<Integer> now = Variable.of("now", Integer.class);
						literals.add(clockView.call(CallPolarity.POSITIVE, List.of(now)));

						for (TimeConstraint tc : t.guard.timeConstraints){
							var time = new ClockValueTerm(output, new ConstantTerm<>(Clock.class, tc.clock));
							if(tc instanceof ClockGreaterThanTimeConstraint) {
								var term = greater(sub(now, time), constant(tc.timeSpan));
								literals.add(check(term));
							}
							else if(tc instanceof ClockLessThanTimeConstraint) {
								var term = less(sub(now, time), constant(tc.timeSpan));
								literals.add(Literals.check(term));
							}
						}
					}
					builder.clause(literals);
				});
				querySet.add(query);

				List<NodeVariable> toParamList = new ArrayList<>(fromParamList);
				for (NodeVariable p : t.getParameters())  {
					if(!toParamList.contains(p)){
						toParamList.add(p);
					}
				}
				Symbol<ClockHolder> toStateSymbol = monitor.get(t.to, toParamList).symbol;

				BiConsumer<Model, Integer> action = (model, now) -> {
					var queryEngine = model.getAdapter(ModelQueryAdapter.class);
					var cursor = queryEngine.getResultSet(query).getAll();
					var fromStateInterpretation = model.getInterpretation(fromStateSymbol);
					var toStateInterpretation = model.getInterpretation(toStateSymbol);

					while(cursor.move()){
						Tuple res = cursor.getKey();
						int[] fromTupleArray = new int[fromStateSymbol.arity()];
						for (int i = 0; i < fromStateSymbol.arity(); i++){
							fromTupleArray[i] = res.get(i);
						}
						Tuple fromTuple = Tuple.of(fromTupleArray);
						fromStateInterpretation.put(fromTuple, null);
						ClockHolder clockHolder = new ClockHolder(cursor.getValue());
						clockHolder.reset(t.action.clocksToReset, now);
						if (t.to.type != State.Type.TRAP) {
							toStateInterpretation.put(res, clockHolder);
						}
					}
				};
				actionSet.add(action);
			}
		}

		BiConsumer<Model, Integer> flushAction = (model, now) -> {
			var queryEngine = model.getAdapter(ModelQueryAdapter.class);
			queryEngine.flushChanges();
		};
		actionSet.add(flushAction);

		BiConsumer<Model, Integer> fitnessActon = (model, now) -> {
			var queryEngine = model.getAdapter(ModelQueryAdapter.class);
			double minWeight = Double.MAX_VALUE;

			for(State s : monitor.stateMachine.states) {
				for(var entry : monitor.get(s).entrySet()) {
					var resultSet = queryEngine.getResultSet(entry.getValue().query);
					if(resultSet.size() != 0 && minWeight > s.weight) {
						minWeight = s.weight;
					}
				}
			}
			var fitnessInterpretation = model.getInterpretation(monitor.fitnessSymbol);
			fitnessInterpretation.put(Tuple.of(), minWeight);
		};
		actionSet.add(fitnessActon);

		var queryBuilder = storeBuilder.getAdapter(ModelQueryBuilder.class);
		queryBuilder.queries(querySet);
	}
}
