package tools.refinery.store.monitor.internal;

import tools.refinery.store.monitor.ModelMonitorBuilder;
import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.store.adapter.AbstractModelAdapterBuilder;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelStore;
import tools.refinery.store.model.ModelStoreBuilder;
import tools.refinery.store.monitor.internal.terms.ClockValueTerm;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.query.ModelQueryBuilder;
import tools.refinery.store.query.dnf.AnyQuery;
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
import java.util.function.Consumer;
import static tools.refinery.store.query.literal.Literals.check;

public class ModelMonitorBuilderImpl extends AbstractModelAdapterBuilder<ModelMonitorStoreAdapterImpl>
		implements ModelMonitorBuilder{

	private final List<Consumer<Model>> actionSet = new ArrayList<>();
	private final Set<AnyQuery> querySet = new HashSet<>();
	private Monitor monitor;
	private Symbol<Integer> clockSymbol;


	@Override
	protected ModelMonitorStoreAdapterImpl doBuild(ModelStore store) {
		return new ModelMonitorStoreAdapterImpl(store, actionSet, monitor, clockSymbol);
	}

	@Override
	public ModelMonitorBuilder monitor(Monitor monitor) {
		checkNotConfigured();
		this.monitor = monitor;
		return this;
	}

	@Override
	public ModelMonitorBuilder clock(Symbol<Integer> clockSymbol) {
		checkNotConfigured();
		this.clockSymbol = clockSymbol;
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
		storeBuilder.symbol(monitor.inAcceptSymbol);

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
						var clockView = new FunctionView<>(clockSymbol);
						DataVariable<Integer> now = Variable.of("now", Integer.class);
						literals.add(clockView.call(CallPolarity.POSITIVE, List.of(now)));

						for (TimeConstraint tc : t.guard.timeConstraints){
							var time = new ClockValueTerm(output, new ConstantTerm<>(Clock.class, tc.clock));
							literals.add(check(tc.getTerm(now, time)));
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

				Consumer<Model> action = (model) -> {
					var queryEngine = model.getAdapter(ModelQueryAdapter.class);
					var cursor = queryEngine.getResultSet(query).getAll();
					var fromStateInterpretation = model.getInterpretation(fromStateSymbol);
					var toStateInterpretation = model.getInterpretation(toStateSymbol);

					int now = 0;
					if(clockSymbol != null){
						var clockInterpretation = model.getInterpretation(clockSymbol);
						now = clockInterpretation.get(Tuple.of());
					}

					while(cursor.move()){
						Tuple res = cursor.getKey();
						// We keep the default start state
						if(fromStateSymbol != monitor.getStartSymbol().symbol){
							int[] fromTupleArray = new int[fromStateSymbol.arity()];
							for (int i = 0; i < fromStateSymbol.arity(); i++){
								fromTupleArray[i] = res.get(i);
							}
							Tuple fromTuple = Tuple.of(fromTupleArray);
							fromStateInterpretation.put(fromTuple, fromStateSymbol.defaultValue());
						}
						// The token has to disappear if the next state is a trap state or
						// the token binding is already present in a direct neighbor of the default start state
						if (!t.to.isTrap() && !(fromStateSymbol == monitor.getStartSymbol().symbol
										&& toStateInterpretation.get(res) != toStateSymbol.defaultValue())) {
							ClockHolder clockHolder = new ClockHolder(cursor.getValue());
							t.action.execute(clockHolder, now);
							toStateInterpretation.put(res, clockHolder);
						}
					}
				};
				actionSet.add(action);
			}
		}

		Consumer<Model> flushAction = (model) -> {
			var queryEngine = model.getAdapter(ModelQueryAdapter.class);
			queryEngine.flushChanges();
		};
		actionSet.add(flushAction);

		Consumer<Model> afterAction = (model) -> {
			var queryEngine = model.getAdapter(ModelQueryAdapter.class);
			double weightSum = 0;
			boolean inAccept = false;

			for(State s : monitor.stateMachine.states) {
				for(var entry : monitor.get(s).entrySet()) {
					var resultSet = queryEngine.getResultSet(entry.getValue().query);
					weightSum += resultSet.size() * s.weight;
					if(s.isAccept() && resultSet.size() > 0) {
						inAccept = true;
					}
				}
			}
			var fitnessInterpretation = model.getInterpretation(monitor.fitnessSymbol);
			fitnessInterpretation.put(Tuple.of(), 1 / (weightSum + 1));

			var inAcceptInterpretation = model.getInterpretation(monitor.inAcceptSymbol);
			inAcceptInterpretation.put(Tuple.of(), inAccept);
		};
		actionSet.add(afterAction);
		actionSet.add(flushAction);

		var queryBuilder = storeBuilder.getAdapter(ModelQueryBuilder.class);
		queryBuilder.queries(querySet);
	}
}
