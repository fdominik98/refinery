package tools.refinery.store.monitor.actionLiterals;

import tools.refinery.store.dse.transition.actions.ActionLiteral;
import tools.refinery.store.dse.transition.actions.BoundActionLiteral;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.caseStudies.gestureRecognitionCaseStudy.GestureRecognitionMetaModel;
import tools.refinery.logic.term.NodeVariable;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;

import java.util.List;


public class IncreaseVectorActionLiteral implements ActionLiteral {
	private final GestureRecognitionMetaModel.Vector incVector;
	private final Symbol<GestureRecognitionMetaModel.Vector> symbol;
	private final List<NodeVariable> parameters;

	public IncreaseVectorActionLiteral(Symbol<GestureRecognitionMetaModel.Vector> symbol,
										List<NodeVariable> parameters,
									   	GestureRecognitionMetaModel.Vector incVector) {
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
			var newVector = GestureRecognitionMetaModel.Vector.of(vector.x + incVector.x, vector.y + incVector.y);
			interpretation.put(tuple, newVector);
			return Tuple.of();
		};
	}
}
