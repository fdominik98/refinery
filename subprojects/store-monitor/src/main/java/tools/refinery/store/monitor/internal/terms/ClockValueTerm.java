package tools.refinery.store.monitor.internal.terms;

import tools.refinery.store.monitor.internal.model.Clock;
import tools.refinery.store.monitor.internal.model.ClockHolder;
import tools.refinery.store.query.substitution.Substitution;
import tools.refinery.store.query.term.BinaryTerm;
import tools.refinery.store.query.term.Term;

public class ClockValueTerm extends BinaryTerm<Integer, ClockHolder, Clock> {

	public ClockValueTerm(Term<ClockHolder> left, Term<Clock> right) {
		super(Integer.class, ClockHolder.class, Clock.class, left, right);
	}

	@Override
	protected Integer doEvaluate(ClockHolder leftValue, Clock rightValue) {
		return leftValue.get(rightValue);
	}

	@Override
	public Term<Integer> doSubstitute(Substitution substitution, Term<ClockHolder> substitutedLeft, Term<Clock> substitutedRight) {
		return new ClockValueTerm(substitutedLeft, substitutedRight);
	}
}
