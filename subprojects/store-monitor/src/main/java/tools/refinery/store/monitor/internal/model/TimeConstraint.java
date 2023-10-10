package tools.refinery.store.monitor.internal.model;

import tools.refinery.store.monitor.internal.terms.ClockValueTerm;
import tools.refinery.store.query.term.DataVariable;
import tools.refinery.store.query.term.Term;

public abstract class TimeConstraint {
	public final Clock clock;
	public final int timeSpan;

	public TimeConstraint(Clock clock, int timeSpan) {
		this.clock = clock;
		this.timeSpan = timeSpan;
	}

	public abstract Term<Boolean> getTerm(DataVariable<Integer> now, ClockValueTerm time);
}
