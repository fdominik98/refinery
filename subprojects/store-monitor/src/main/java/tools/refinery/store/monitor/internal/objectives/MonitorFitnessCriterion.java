package tools.refinery.store.monitor.internal.objectives;

import tools.refinery.store.dse.transition.objectives.Criterion;
import tools.refinery.store.dse.transition.objectives.CriterionCalculator;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.tuple.Tuple;
import tools.refinery.store.tuple.Tuple0;

public class MonitorFitnessCriterion implements Criterion{

	private final Monitor monitor;
	private final boolean negated;

	public MonitorFitnessCriterion(Monitor monitor, boolean negated) {
		this.monitor = monitor;
		this.negated = negated;
	}

	@Override
	public CriterionCalculator createCalculator(Model model) {
		var fitnessInterpretation = model.getInterpretation(monitor.fitnessSymbol);
		return () -> negated ?
				(fitnessInterpretation.get(Tuple.of()) > 1) :
				(fitnessInterpretation.get(Tuple.of()) <= 1);
	}
}
