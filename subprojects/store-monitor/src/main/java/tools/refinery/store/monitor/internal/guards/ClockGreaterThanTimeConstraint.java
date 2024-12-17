package tools.refinery.store.monitor.internal.guards;

import tools.refinery.store.monitor.internal.model.Clock;
import tools.refinery.store.monitor.internal.model.TimeConstraint;
import tools.refinery.store.monitor.internal.terms.ClockValueTerm;
import tools.refinery.logic.term.DataVariable;
import tools.refinery.logic.term.Term;
import static tools.refinery.logic.term.int_.IntTerms.*;

public class ClockGreaterThanTimeConstraint extends TimeConstraint {
	public ClockGreaterThanTimeConstraint(Clock clock, int value) {
		super(clock, value);
	}

	@Override
	public Term<Boolean> getTerm(DataVariable<Integer> now, ClockValueTerm time) {
		return greater(sub(now, time), constant(timeSpan));
	}
}