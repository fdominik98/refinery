package tools.refinery.store.monitor.utils;

import tools.refinery.store.monitor.gestureRecognitionCaseStudy.GestureRecognitionMetaModel.Vector;
import tools.refinery.store.query.substitution.Substitution;
import tools.refinery.store.query.term.Term;
import tools.refinery.store.query.term.UnaryTerm;

public class VectorYTerm extends UnaryTerm<Integer, Vector> {

	public VectorYTerm(Term<Vector> left) {
		super(Integer.class, Vector.class, left);
	}

	@Override
	protected Integer doEvaluate(Vector bodyValue) {
		return bodyValue.y;
	}

	@Override
	protected Term<Integer> doSubstitute(Substitution substitution, Term<Vector> substitutedBody) {
		return new VectorYTerm(substitutedBody);
	}
}
