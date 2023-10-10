package tools.refinery.store.monitor.internal.model;

import tools.refinery.store.monitor.internal.terms.ClockValueTerm;
import tools.refinery.store.query.term.DataVariable;
import tools.refinery.store.query.term.Term;
import static tools.refinery.store.query.term.int_.IntTerms.*;

public class ClockEqTimeConstraint extends TimeConstraint{
	public ClockEqTimeConstraint(Clock clock, int value) {
		super(clock, value);
	}
	@Override
	public Term<Boolean> getTerm(DataVariable<Integer> now, ClockValueTerm time) {
		return eq(sub(now, time), constant(timeSpan));
	}
}
