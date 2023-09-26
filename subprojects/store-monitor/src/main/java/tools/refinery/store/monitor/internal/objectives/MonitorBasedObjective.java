package tools.refinery.store.monitor.internal.objectives;

import tools.refinery.store.dse.transition.objectives.Objective;
import tools.refinery.store.dse.transition.objectives.ObjectiveCalculator;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.tuple.Tuple;

public class MonitorBasedObjective implements Objective {
	private final Monitor monitor;

	public MonitorBasedObjective(Monitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public ObjectiveCalculator createCalculator(Model model) {
		var fitnessInterpretation = model.getInterpretation(monitor.fitnessSymbol);
		return () -> fitnessInterpretation.get(Tuple.of());
	}
}
