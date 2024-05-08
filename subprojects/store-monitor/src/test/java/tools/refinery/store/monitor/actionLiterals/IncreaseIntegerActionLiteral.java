package tools.refinery.store.monitor.actionLiterals;

import tools.refinery.store.dse.transition.actions.ActionLiteral;
import tools.refinery.store.dse.transition.actions.BoundActionLiteral;
import tools.refinery.store.model.Model;
import tools.refinery.logic.term.NodeVariable;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;

import java.util.List;


public class IncreaseIntegerActionLiteral implements ActionLiteral {
	private final int incInteger;
	private final Symbol<Integer> symbol;
	private final List<NodeVariable> parameters;

	public IncreaseIntegerActionLiteral(Symbol<Integer> symbol,
                                        List<NodeVariable> parameters, int incInteger) {
		if (symbol.arity() != parameters.size()) {
			throw new IllegalArgumentException("Expected %d parameters for symbol %s, got %d instead"
					.formatted(symbol.arity(), symbol, parameters.size()));
		}
		this.incInteger = incInteger;
		this.parameters = parameters;
		this.symbol = symbol;
	}

	public List<NodeVariable> getParameters() {
		return parameters;
	}

	@Override
	public List<NodeVariable> getInputVariables() {
		return getParameters();
	}

	@Override
	public List<NodeVariable> getOutputVariables() {
		return List.of();
	}

	@Override
	public BoundActionLiteral bindToModel(Model model) {
		var interpretation = model.getInterpretation(symbol);
		return tuple -> {
			var integer = interpretation.get(tuple);
			interpretation.put(tuple, integer + incInteger);
			return Tuple.of();
		};
	}
}
