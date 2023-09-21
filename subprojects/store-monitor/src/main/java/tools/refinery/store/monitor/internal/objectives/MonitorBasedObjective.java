package tools.refinery.store.monitor.internal.objectives;

import tools.refinery.store.dse.DesignSpaceExplorationAdapter;
import tools.refinery.store.dse.objectives.BaseObjective;
import tools.refinery.store.dse.objectives.Objective;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.monitor.internal.model.State;
import tools.refinery.store.query.ModelQueryAdapter;

import java.util.Map;

public class MonitorBasedObjective extends BaseObjective {
	private static final String DEFAULT_NAME = "MonitorBasedObjective";
	private final Monitor monitor;


	public MonitorBasedObjective(Monitor monitor) {
		super(DEFAULT_NAME);
		this.monitor = monitor;
	}

	@Override
	public Double getFitness(DesignSpaceExplorationAdapter context) {
		var queryEngine = context.getModel().getAdapter(ModelQueryAdapter.class);
		int minWeight = Integer.MAX_VALUE;

		for(State s : monitor.stateMachine.states) {
			for(var entry : monitor.get(s).entrySet()) {
				var resultSet = queryEngine.getResultSet(entry.getValue().query);
				if(resultSet.size() != 0 && minWeight > s.weight) {
					minWeight = s.weight;
				}
			}
		}

		return -(double)minWeight;
	}

	@Override
	public Objective createNew() {
		return this;
	}

	@Override
	public boolean satisfiesHardObjective(Double fitness) {
		return true;
	}

	@Override
	public boolean isHardObjective() {
		return true;
	}
}
