package tools.refinery.store.monitor.internal.model;

import java.util.*;

public class Transition {

	public final List<Guard> guardTriggers;
	public final List<Parameter> parameters;

	public final State from;

	public final State to;

	public  Transition(State from, List<Guard> guardTriggers, State to) {
		this.from = from;
		this.guardTriggers = guardTriggers;
		this.to = to;
		this.parameters = new ArrayList<>();
		for(Guard guard : guardTriggers) {
			parameters.addAll(guard.parameters);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.from.toString());
		sb.append(" -> [");
		sb.append(String.join(", ", this.guardTriggers.stream().map(Guard::toString).toList()));
		sb.append("] -> ");
		sb.append(this.to.toString());

		return sb.toString();
	}
}
