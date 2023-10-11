package tools.refinery.store.monitor.internal.objectives;

import tools.refinery.store.dse.transition.objectives.Criterion;
import tools.refinery.store.dse.transition.objectives.CriterionCalculator;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;

public class MonitorFitnessExcludeCriterion implements Criterion{

	private final Monitor monitor;
	private final Symbol<Integer> clockSymbol;
	private final int timeOut;

	public MonitorFitnessExcludeCriterion(Monitor monitor, Symbol<Integer> clockSymbol, int timeOut) {
		this.monitor = monitor;
		this.clockSymbol = clockSymbol;
		this.timeOut = timeOut;
	}

	@Override
	public CriterionCalculator createCalculator(Model model) {
		var fitnessInterpretation = model.getInterpretation(monitor.fitnessSymbol);
		var acceptanceInterpretation = model.getInterpretation(monitor.acceptanceSymbol);
		var clockInterpretation = model.getInterpretation(clockSymbol);

		Double fitness = fitnessInterpretation.get(Tuple.of());
		Boolean accepted = acceptanceInterpretation.get(Tuple.of());
		Integer clock = clockInterpretation.get(Tuple.of());
		boolean valuesExist = fitness != null && accepted != null && clock != null;
		return () -> valuesExist && (fitness > 1 || (!accepted && clock > timeOut));
	}
}
