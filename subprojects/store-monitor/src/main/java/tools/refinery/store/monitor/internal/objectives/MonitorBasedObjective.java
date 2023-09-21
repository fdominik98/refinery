package tools.refinery.store.monitor.internal.objectives;

import tools.refinery.store.dse.transition.objectives.Objective;
import tools.refinery.store.dse.transition.objectives.ObjectiveCalculator;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.monitor.internal.model.State;
import tools.refinery.store.query.ModelQueryAdapter;

public class MonitorBasedObjective implements Objective {
	private final Monitor monitor;

	public MonitorBasedObjective(Monitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public ObjectiveCalculator createCalculator(Model model) {
		return () -> {
			var queryEngine = model.getAdapter(ModelQueryAdapter.class);
			int minWeight = Integer.MAX_VALUE;

			for(State s : monitor.stateMachine.states) {
				for(var entry : monitor.get(s).entrySet()) {
					var resultSet = queryEngine.getResultSet(entry.getValue().query);
					if(resultSet.size() != 0 && minWeight > s.weight) {
						minWeight = s.weight;
					}
				}
			}
			return (double)minWeight;
		};
	}
}
