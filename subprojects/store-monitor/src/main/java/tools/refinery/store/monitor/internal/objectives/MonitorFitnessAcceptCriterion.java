package tools.refinery.store.monitor.internal.objectives;

import tools.refinery.store.dse.transition.objectives.Criterion;
import tools.refinery.store.dse.transition.objectives.CriterionCalculator;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.tuple.Tuple;

public class MonitorFitnessAcceptCriterion implements Criterion{

	private final Monitor monitor;

	public MonitorFitnessAcceptCriterion(Monitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public CriterionCalculator createCalculator(Model model) {
		var fitnessInterpretation = model.getInterpretation(monitor.fitnessSymbol);
		Double fitness = fitnessInterpretation.get(Tuple.of());
		return () -> fitness != null && fitness <= 1;
	}
}
