package tools.refinery.store.monitor.actionLiterals;

import tools.refinery.store.dse.transition.actions.ActionLiteral;
import tools.refinery.store.dse.transition.actions.BoundActionLiteral;
import tools.refinery.store.model.Model;
import tools.refinery.logic.term.NodeVariable;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;
import java.util.List;
import java.util.Random;

public class RandomTransmitMessageActionLiteral implements ActionLiteral {
	private final Symbol<Boolean> atSymbol;
	private final List<NodeVariable> parameters;
	private final double successRate;

	public RandomTransmitMessageActionLiteral(Symbol<Boolean> atSymbol,
                                              NodeVariable message,
											  NodeVariable router1,
											  NodeVariable router2,
											  double successRate) {
		this.successRate = successRate;
		this.parameters = List.of(message, router1, router2);
		this.atSymbol = atSymbol;
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
		var atInterpretation = model.getInterpretation(atSymbol);
		return tuple -> {
			if (shouldSucceed(successRate)) {
				atInterpretation.put(Tuple.of(tuple.get(0), tuple.get(1)), false);
				atInterpretation.put(Tuple.of(tuple.get(0), tuple.get(2)), true);
			}
			return Tuple.of();
		};
	}

	private boolean shouldSucceed(double successRate) {
		if (successRate < 0.0 || successRate > 1.0) {
			throw new IllegalArgumentException("Success rate must be between 0.0 and 1.0");
		}

		Random random = new Random();
		return random.nextDouble() < successRate;
	}
}
