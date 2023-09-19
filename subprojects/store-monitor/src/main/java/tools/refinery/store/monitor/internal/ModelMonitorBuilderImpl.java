package tools.refinery.store.monitor.internal;

import tools.refinery.store.monitor.AbstractTimeProvider;
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

import static tools.refinery.store.query.term.int_.IntTerms.*;

public class ModelMonitorBuilderImpl extends AbstractModelAdapterBuilder<ModelMonitorStoreAdapterImpl>
		implements ModelMonitorBuilder{

	private StateMachine monitor;
	private final List<BiConsumer<Model, Integer>> actionSet = new ArrayList<>();
	private final Set<Query> querySet = new HashSet<>();
	private SymbolHolder symbolHolder;
	private AbstractTimeProvider timeProvider;
	private final StateMachineSummary summary = new StateMachineSummary();


	@Override
	protected ModelMonitorStoreAdapterImpl doBuild(ModelStore store) {
		return new ModelMonitorStoreAdapterImpl(store, timeProvider, actionSet, symbolHolder, summary);
	}

	@Override
	public ModelMonitorBuilder monitor(StateMachine monitor) {
		checkNotConfigured();
		this.monitor = monitor;
		StateMachineTraversal traverser = new StateMachineTraversal(monitor);
		this.symbolHolder = traverser.symbolHolder;
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
		querySet.addAll(symbolHolder.queryList);
		return this;
	}

	@Override
	protected void doConfigure(ModelStoreBuilder storeBuilder) {
		storeBuilder.symbols(symbolHolder.symbolList);

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

		for (Transition t : monitor.transitions) {
			for(List<NodeVariable> fromParamList : symbolHolder.get(t.from).keySet()){
				Symbol<ClockHolder> fromStateSymbol = symbolHolder.get(t.from, fromParamList).symbol;

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
						literals.add(t.guard.query.call(CallPolarity.POSITIVE, t.getParameters()));
					}

					if(t.guard.timeConstraints.length != 0){
						var clockView = new FunctionView<>(timeProvider.clockSymbol);
						DataVariable<Integer> now = Variable.of("now", Integer.class);
						literals.add(clockView.call(CallPolarity.POSITIVE, List.of(now)));

						for (TimeConstraint tc : t.guard.timeConstraints){
							var time = new ClockValueTerm(output, new ConstantTerm<>(Clock.class, tc.clock));
							if(tc instanceof ClockGreaterThanTimeConstraint) {
								var term = greater(sub(now, time), constant(tc.timeSpan));
								literals.add(Literals.assume(term));
							}
							else if(tc instanceof ClockLessThanTimeConstraint) {
								var term = less(sub(now, time), constant(tc.timeSpan));
								literals.add(Literals.assume(term));
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
				Symbol<ClockHolder> toStateSymbol = symbolHolder.get(t.to, toParamList).symbol;

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
						toStateInterpretation.put(res, clockHolder);

						switch(t.to.type){
							case TRAP -> summary.trapStateCount++;
							case ACCEPT -> summary.acceptStateCount++;
							default -> summary.intermediateStateCount++;
						}
						switch(t.from.type){
							case TRAP -> summary.trapStateCount--;
							case ACCEPT -> summary.acceptStateCount--;
							default -> summary.intermediateStateCount--;
						}
					}
				};
				actionSet.add(action);
			}
		}

		BiConsumer<Model, Integer> lastFlushAction = (model, now) -> {
			var queryEngine = model.getAdapter(ModelQueryAdapter.class);
			queryEngine.flushChanges();
		};
		actionSet.add(lastFlushAction);

		var queryBuilder = storeBuilder.getAdapter(ModelQueryBuilder.class);
		queryBuilder.queries(querySet);
	}
}
