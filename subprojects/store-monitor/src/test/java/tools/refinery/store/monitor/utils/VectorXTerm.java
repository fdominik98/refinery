package tools.refinery.store.monitor.utils;

import tools.refinery.logic.substitution.Substitution;
import tools.refinery.logic.term.Term;
import tools.refinery.logic.term.UnaryTerm;

public class VectorXTerm extends UnaryTerm<Integer, Vector> {

	public VectorXTerm(Term<Vector> left) {
		super(Integer.class, Vector.class, left);
	}

	@Override
	protected Integer doEvaluate(Vector bodyValue) {
		return bodyValue.x;
	}

	@Override
	protected Term<Integer> doSubstitute(Substitution substitution, Term<Vector> substitutedBody) {
		return new VectorXTerm(substitutedBody);
	}
}
