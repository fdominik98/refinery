package tools.refinery.store.monitor.utils;

import tools.refinery.logic.substitution.Substitution;
import tools.refinery.logic.term.BinaryTerm;
import tools.refinery.logic.term.Term;

public class VectorDistanceTerm extends BinaryTerm<Double, Vector, Vector> {
	protected VectorDistanceTerm(Term<Vector> left, Term<Vector> right) {
		super(Double.class, Vector.class, Vector.class, left, right);
	}

	@Override
	public Term<Double> doSubstitute(Substitution substitution, Term<Vector> substitutedLeft,
									 Term<Vector> substitutedRight) {
		return new VectorDistanceTerm(substitutedLeft, substitutedRight);
	}

	@Override
	protected Double doEvaluate(Vector leftValue, Vector rightValue) {
		return Math.sqrt(Math.pow(leftValue.x - rightValue.x, 2) + Math.pow(leftValue.y - rightValue.y, 2));
	}

	@Override
	public String toString() {
		return "|%s - %s|".formatted(getLeft(), getRight());
	}
}
