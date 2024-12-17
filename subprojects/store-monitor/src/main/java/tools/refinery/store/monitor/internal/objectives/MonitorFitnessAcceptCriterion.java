package tools.refinery.store.monitor.internal.objectives;

import tools.refinery.store.dse.transition.objectives.Criterion;
import tools.refinery.store.dse.transition.objectives.CriterionCalculator;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;

public class MonitorFitnessAcceptCriterion implements Criterion{

	private final Monitor monitor;
	private final Symbol<Integer> clockSymbol;
	private final int timeOut;

	public MonitorFitnessAcceptCriterion(Monitor monitor, Symbol<Integer> clockSymbol, int timeOut) {
		this.monitor = monitor;
		this.clockSymbol = clockSymbol;
		this.timeOut = timeOut;
	}

	@Override
	public CriterionCalculator createCalculator(Model model) {
		var inAcceptInterpretation = model.getInterpretation(monitor.inAcceptSymbol);
		var isInvalidInterpretation = model.getInterpretation(monitor.isInvalidSymbol);
		return () -> {
			Boolean inAccept = inAcceptInterpretation.get(Tuple.of());
			Boolean isInvalid = isInvalidInterpretation.get(Tuple.of());
			boolean valuesExist = inAccept != null && isInvalid != null;
			return valuesExist && inAccept && !isInvalid;
		};
	}
}
