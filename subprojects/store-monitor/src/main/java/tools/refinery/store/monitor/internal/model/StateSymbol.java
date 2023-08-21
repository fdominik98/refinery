package tools.refinery.store.monitor.internal.model;

import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.literal.CallPolarity;
import tools.refinery.store.query.term.Variable;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;

import java.util.ArrayList;
import java.util.List;

public class StateSymbol {

	public final Symbol<Integer> symbol;

	private final RelationalQuery query;

	public StateSymbol(Symbol<Integer> symbol) {
		this.symbol = symbol;

		this.query = Query.of(symbol.name() + "_query", builder -> {
			var view = new KeyOnlyView<>(symbol);
			List<Variable> variables = new ArrayList<>();
			for (int i = 0; i < symbol.arity(); i++) {
				variables.add(builder.parameter("v_" + i));
			}
			builder.clause(List.of(view.call(CallPolarity.POSITIVE, variables)));
		});
	}

	public RelationalQuery getQuery(){
		return query;
	}
}
