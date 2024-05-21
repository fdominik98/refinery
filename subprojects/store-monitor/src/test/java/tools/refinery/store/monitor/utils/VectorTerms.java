package tools.refinery.store.monitor.utils;

import tools.refinery.logic.term.ConstantTerm;
import tools.refinery.logic.term.Term;
import tools.refinery.logic.term.comparable.*;

public final class VectorTerms {

	private VectorTerms() {
		throw new IllegalArgumentException("This is a static utility class and should not be instantiated directly");
	}

	public static Term<Vector> constant(Vector value) {
		return new ConstantTerm<>(Vector.class, value);
	}

	public static Term<Vector> add(Term<Vector> left, Term<Vector> right) {
		return new VectorAddTerm(left, right);
	}

	public static Term<Vector> sub(Term<Vector> left, Term<Vector> right) {
		return new VectorSubTerm(left, right);
	}


	public static Term<Boolean> eq(Term<Vector> left, Term<Vector> right) {
		return new EqTerm<>(Vector.class, left, right);
	}

	public static Term<Boolean> notEq(Term<Vector> left, Term<Vector> right) {
		return new NotEqTerm<>(Vector.class, left, right);
	}

	public static Term<Double> distance(Term<Vector> left, Term<Vector> right) {
		return new VectorDistanceTerm(left, right);
	}

	public static Term<Boolean> isInLineByX(Term<Vector> left, Term<Vector> right) {
		return new VectorIsInLineByXTerm(left, right);
	}

	public static Term<Boolean> isInLineByY(Term<Vector> left, Term<Vector> right) {
		return new VectorIsInLineByYTerm(left, right);
	}

	public static Term<Boolean> isInDirection(Term<Vector> left, Term<Vector> right, int x, int y) {
		return eq(add(left, constant(Vector.of(x, y))), right);
	}

	public static Term<Integer> x(Term<Vector> vector) {
		return new VectorXTerm(vector);
	}

	public static Term<Integer> y(Term<Vector> vector) {
		return new VectorYTerm(vector);
	}


}

