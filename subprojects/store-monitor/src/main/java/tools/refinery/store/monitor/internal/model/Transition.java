package tools.refinery.store.monitor.internal.model;

import tools.refinery.store.query.dnf.SymbolicParameter;
import tools.refinery.store.query.term.NodeVariable;
import tools.refinery.store.query.term.Variable;
import java.util.*;

public class Transition {

	public final Guard guard;
	public final State from;
	public final State to;
	public final ClockResetAction action;

	public Transition(State from, Guard guard, State to, ClockResetAction action) {
		this.from = from;
		this.guard = guard;
		this.to = to;
		this.action = action;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.from.toString());
		sb.append(" -> [");
		sb.append(String.join(", ", this.getParameters().stream().map(Variable::toString).toList()));
		sb.append("] -> ");
		sb.append(this.to.toString());

		return sb.toString();
	}

	public List<NodeVariable> getParameters() {
		List<NodeVariable> variables = new ArrayList<>();
		if (guard.query == null) {
			return variables;
		}
		for (var param : guard.query.getDnf().getParameters()) {
			if (param instanceof SymbolicParameter sp && sp.getVariable() instanceof NodeVariable nv) {
				variables.add(nv);
			} else {
				throw new IllegalArgumentException("Parameter must be a NodeVariable");
			}
		}
		return variables;
	}
}
