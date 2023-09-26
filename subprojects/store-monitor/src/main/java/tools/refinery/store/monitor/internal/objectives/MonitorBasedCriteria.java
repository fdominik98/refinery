package tools.refinery.store.monitor.internal.objectives;

import tools.refinery.store.dse.transition.objectives.Criterion;
import tools.refinery.store.dse.transition.objectives.CriterionCalculator;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.tuple.Tuple;

public class MonitorBasedCriteria implements Criterion{

	private final Monitor monitor;
	private final boolean negated;

	public MonitorBasedCriteria(Monitor monitor, boolean negated) {
		this.monitor = monitor;
		this.negated = negated;
	}

	@Override
	public CriterionCalculator createCalculator(Model model) {
		var fitnessInterpretation = model.getInterpretation(monitor.fitnessSymbol);
		return () -> negated ?
				(fitnessInterpretation.get(Tuple.of()) > monitor.stateMachine.getMaxWeight()) :
				(fitnessInterpretation.get(Tuple.of()) <= monitor.stateMachine.getMaxWeight());
	}
}
