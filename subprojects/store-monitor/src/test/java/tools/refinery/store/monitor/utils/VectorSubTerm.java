/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.store.monitor.utils;

import tools.refinery.logic.substitution.Substitution;
import tools.refinery.logic.term.BinaryTerm;
import tools.refinery.logic.term.Term;

public class VectorSubTerm extends BinaryTerm<Vector, Vector, Vector> {
	protected VectorSubTerm(Term<Vector> left, Term<Vector> right) {
		super(Vector.class, Vector.class, Vector.class, left, right);
	}

	@Override
	public Term<Vector> doSubstitute(Substitution substitution, Term<Vector> substitutedLeft,
									 Term<Vector> substitutedRight) {
		return new VectorSubTerm(substitutedLeft, substitutedRight);
	}

	@Override
	protected Vector doEvaluate(Vector leftValue, Vector rightValue) {
		return Vector.of(leftValue.x - rightValue.x, leftValue.y - rightValue.y);
	}

	@Override
	public String toString() {
		return "(%s - %s)".formatted(getLeft(), getRight());
	}
}
