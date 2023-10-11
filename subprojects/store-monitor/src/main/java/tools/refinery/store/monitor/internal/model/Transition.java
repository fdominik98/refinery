package tools.refinery.store.monitor.internal.model;

import tools.refinery.store.monitor.internal.actions.TransitionAction;
import tools.refinery.store.monitor.internal.guards.Guard;
import tools.refinery.store.query.dnf.SymbolicParameter;
import tools.refinery.store.query.term.NodeVariable;
import tools.refinery.store.query.term.Variable;
import java.util.*;

public class Transition {
	public final Guard guard;
	public final State from;
	public final State to;
	public final TransitionAction action;

	public Transition(State from, Guard guard, State to, TransitionAction action) {
		this.from = from;
		this.guard = guard;
		this.to = to;
		this.action = action;
	}

	@Override
	public String toString() {
		String sb = this.from.toString() +
				" -> [" +
				String.join(", ", this.getParameters().stream().map(Variable::toString).toList()) +
				"] -> " +
				this.to.toString();

		return sb;
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
