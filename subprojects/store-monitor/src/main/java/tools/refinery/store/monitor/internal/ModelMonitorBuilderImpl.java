package tools.refinery.store.monitor.internal;

import tools.refinery.store.monitor.AbstractTimeProvider;
import tools.refinery.store.monitor.ModelMonitorBuilder;
import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.store.adapter.AbstractModelAdapterBuilder;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelStore;
import tools.refinery.store.model.ModelStoreBuilder;
import tools.refinery.store.monitor.internal.model.PartialModelGuard;
import tools.refinery.store.query.ModelQueryAdapter;
import static tools.refinery.store.query.term.int_.IntTerms.*;
import tools.refinery.store.query.ModelQueryBuilder;
import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.literal.*;
import tools.refinery.store.query.term.DataVariable;
import tools.refinery.store.query.term.NodeVariable;
import tools.refinery.store.query.term.Variable;
import tools.refinery.store.query.view.FunctionView;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;
import java.util.*;
import java.util.function.BiConsumer;

public class ModelMonitorBuilderImpl extends AbstractModelAdapterBuilder<ModelMonitorStoreAdapterImpl>
		implements ModelMonitorBuilder{

	private StateMachine monitor;
	private final List<BiConsumer<Model, Integer>> actionSet = new ArrayList<>();
	private final Set<RelationalQuery> querySet = new HashSet<>();
	private SymbolHolder symbolHolder;
	private final Symbol<Integer> clockSymbol = Symbol.of("Clock", 0, Integer.class);
	private AbstractTimeProvider timeProvider;
	private StateMachineSummary summary = new StateMachineSummary();


	@Override
	protected ModelMonitorStoreAdapterImpl doBuild(ModelStore store) {
		return new ModelMonitorStoreAdapterImpl(store, timeProvider, actionSet, symbolHolder, summary);
	}

	@Override
	public ModelMonitorBuilder monitor(StateMachine monitor) {
		checkNotConfigured();
		this.monitor = monitor;
		StateMachineTraversal traverser = new StateMachineTraversal(monitor.startState);
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
		BiConsumer<Model, Integer> action = (model, now) -> {
			var clockInterpretation = model.getInterpretation(clockSymbol);
			clockInterpretation.put(Tuple.of(), now);
			var queryEngine = model.getAdapter(ModelQueryAdapter.class);
			queryEngine.flushChanges();
		};
		actionSet.add(action);

		for (Transition t : monitor.transitions) {
			for(List<Parameter> fromParamList : symbolHolder.get(t.from).keySet()){
				Symbol<Integer> fromStateSymbol = symbolHolder.get(t.from, fromParamList).symbol;

				RelationalQuery query = Query.of(t + "_query", builder -> {
					DataVariable<Integer> stepInTime = Variable.of("stepInTime", Integer.class);
					Map<Parameter, NodeVariable> variableMap = new HashMap<>();
					List<Literal> literals = new ArrayList<>();

					List<Variable> stateVariables = new ArrayList<>();
					for(Parameter p : fromParamList){
						var variable = builder.parameter(p.name);
						variableMap.put(p, variable);
						stateVariables.add(variable);
					}
					stateVariables.add(stepInTime);
					var stateView = new FunctionView<>(fromStateSymbol);
					literals.add(stateView.call(CallPolarity.POSITIVE, stateVariables));

					for(Guard g : t.guardTriggers) {
						if (g instanceof PartialModelGuard pmg){
							List<Variable> guardVariables = new ArrayList<>();
							for (Parameter p : pmg.parameters){
								if(!variableMap.containsKey(p)){
									variableMap.put(p, builder.parameter(p.name));
								}
								guardVariables.add(variableMap.get(p));
							}
							var view = new KeyOnlyView<>(pmg.relation);
							if (pmg.negated){
								literals.add(view.call(CallPolarity.NEGATIVE, guardVariables));
							}
							else{
								literals.add(view.call(CallPolarity.POSITIVE, guardVariables));
							}
						}
						else {
							var clockView = new FunctionView<>(clockSymbol);
							DataVariable<Integer> now = Variable.of("now", Integer.class);
							literals.add(clockView.call(CallPolarity.POSITIVE, List.of(now)));
							if(g instanceof TimeGreaterThanGuard tgtg) {
								var term = greater(sub(now, stepInTime), constant(tgtg.timeSpan));
								literals.add(Literals.assume(term));
							}
							else if(g instanceof TimeLessThanGuard tltg) {
								var term = less(sub(now, stepInTime), constant(tltg.timeSpan));
								literals.add(Literals.assume(term));
							}
						}
					}
					builder.clause(literals);
				});
				querySet.add(query);

				List<Parameter> toParamList = new ArrayList<>(fromParamList);
				for (Parameter p : t.parameters)  {
					if(!toParamList.contains(p)){
						toParamList.add(p);
					}
				}
				Symbol<Integer> toStateSymbol = symbolHolder.get(t.to, toParamList).symbol;

				action = (model, now) -> {
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
						toStateInterpretation.put(res, now);

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

		action = (model, now) -> {
			var queryEngine = model.getAdapter(ModelQueryAdapter.class);
			queryEngine.flushChanges();
		};
		actionSet.add(action);

		storeBuilder.symbols(symbolHolder.symbolList);
		storeBuilder.symbols(clockSymbol);

		var queryBuilder = storeBuilder.getAdapter(ModelQueryBuilder.class);
		queryBuilder.queries(querySet);
	}
}
