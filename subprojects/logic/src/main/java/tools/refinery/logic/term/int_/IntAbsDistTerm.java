/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.store.query.term.int_;

import tools.refinery.store.query.substitution.Substitution;
import tools.refinery.store.query.term.Term;

import static java.lang.Math.abs;

public class IntAbsDistTerm extends IntBinaryTerm {
	public IntAbsDistTerm(Term<Integer> left, Term<Integer> right) {
		super(left, right);
	}

	@Override
	public Term<Integer> doSubstitute(Substitution substitution, Term<Integer> substitutedLeft,
									  Term<Integer> substitutedRight) {
		return new IntAbsDistTerm(substitutedLeft, substitutedRight);
	}

	@Override
	protected Integer doEvaluate(Integer leftValue, Integer rightValue) {
		return abs(abs(leftValue) - abs(rightValue));
	}

	@Override
	public String toString() {
		return "(||%s| - |%s||)".formatted(getLeft(), getRight());
	}
}
