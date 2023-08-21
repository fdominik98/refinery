package tools.refinery.store.monitor.internal.model;

import java.util.List;
import java.util.Objects;

public class PartialModelGuard extends Guard {

	public final String relation;
	public final boolean negated;

	public PartialModelGuard neg() {
		return new PartialModelGuard(relation, parameters, true);
	}

	public PartialModelGuard(String relation, List<Parameter> parameters) {
		super(parameters);
		this.relation = relation;
		this.negated = false;
	}

	public PartialModelGuard(String relation, List<Parameter> parameters, boolean negated) {
		super(parameters);
		this.relation = relation;
		this.negated = negated;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(relation);
		if(negated){
			sb.append("!");
		}
		sb.append("(");
		sb.append(String.join(", ", parameters.stream().map(Object::toString).toList()));
		sb.append(")");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PartialModelGuard)) {
			return false;
		}
		PartialModelGuard guard = (PartialModelGuard) o;

		return relation.equals(guard.relation) && parameters.equals(guard.parameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash("CEPEventGuard prefix" + relation.hashCode() + parameters.hashCode());
	}
}
