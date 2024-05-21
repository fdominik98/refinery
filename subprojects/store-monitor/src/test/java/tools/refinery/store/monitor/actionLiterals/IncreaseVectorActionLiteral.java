package tools.refinery.store.monitor.actionLiterals;

import tools.refinery.store.dse.transition.actions.ActionLiteral;
import tools.refinery.store.dse.transition.actions.BoundActionLiteral;
import tools.refinery.store.model.Model;
import tools.refinery.logic.term.NodeVariable;
import tools.refinery.store.monitor.utils.Vector;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;

import java.util.List;


public class IncreaseVectorActionLiteral implements ActionLiteral {
	private final Vector incVector;
	private final Symbol<Vector> symbol;
	private final List<NodeVariable> parameters;

	public IncreaseVectorActionLiteral(Symbol<Vector> symbol,
										List<NodeVariable> parameters,
									   	Vector incVector) {
		if (symbol.arity() != parameters.size()) {
			throw new IllegalArgumentException("Expected %d parameters for symbol %s, got %d instead"
					.formatted(symbol.arity(), symbol, parameters.size()));
		}
		this.incVector = incVector;
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
			var vector = interpretation.get(tuple);
			var newVector = Vector.of(vector.x + incVector.x, vector.y + incVector.y);
			interpretation.put(tuple, newVector);
			return Tuple.of();
		};
	}
}
