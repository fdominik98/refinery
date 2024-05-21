package tools.refinery.store.monitor.utils;

import tools.refinery.logic.substitution.Substitution;
import tools.refinery.logic.term.BinaryTerm;
import tools.refinery.logic.term.Term;

public class VectorIsInLineByXTerm extends BinaryTerm<Boolean, Vector, Vector> {
	protected VectorIsInLineByXTerm(Term<Vector> left, Term<Vector> right) {
		super(Boolean.class, Vector.class, Vector.class, left, right);
	}

	@Override
	public Term<Boolean> doSubstitute(Substitution substitution, Term<Vector> substitutedLeft,
									 Term<Vector> substitutedRight) {
		return new VectorIsInLineByXTerm(substitutedLeft, substitutedRight);
	}

	@Override
	protected Boolean doEvaluate(Vector leftValue, Vector rightValue) {
		return leftValue.x == rightValue.x;
	}

	@Override
	public String toString() {
		return "%s is in line by X with %s".formatted(getLeft(), getRight());
	}
}
