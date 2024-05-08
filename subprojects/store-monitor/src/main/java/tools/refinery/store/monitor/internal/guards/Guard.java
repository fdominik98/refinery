package tools.refinery.store.monitor.internal.guards;

import tools.refinery.store.monitor.internal.model.TimeConstraint;
import tools.refinery.logic.dnf.RelationalQuery;

public class Guard {
	public boolean negated = false;

	public RelationalQuery query;
	public TimeConstraint[] timeConstraints;
	public Guard(RelationalQuery query, TimeConstraint... timeConstraints) {
		this.query = query;
		this.timeConstraints = timeConstraints;
	}

	public Guard(TimeConstraint... timeConstraints) {
		this.query = null;
		this.timeConstraints = timeConstraints;
	}

	public static Guard of(TimeConstraint... timeConstraints) {
		return new Guard(timeConstraints);
	}

	public static Guard of(RelationalQuery query, TimeConstraint... timeConstraints) {
		return new Guard(query, timeConstraints);
	}

	public Guard neg(){
		var guard = Guard.of(query, timeConstraints);
		guard.negated = true;
		return guard;
	}
}
