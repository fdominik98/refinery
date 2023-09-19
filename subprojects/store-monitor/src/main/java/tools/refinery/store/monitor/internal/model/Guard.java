package tools.refinery.store.monitor.internal.model;

import tools.refinery.store.query.dnf.RelationalQuery;

public class Guard {
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
}
