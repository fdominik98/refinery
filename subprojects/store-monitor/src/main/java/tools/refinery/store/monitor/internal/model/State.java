package tools.refinery.store.monitor.internal.model;

import java.util.ArrayList;
import java.util.List;

public class State {

	public enum Type {
		INTERMEDIATE,
		TRAP,
		ACCEPT
	}

	public final List<Transition> transitionsOut = new ArrayList<>();
	public final List<Transition> transitionsIn = new ArrayList<>();

	public final String name;
	public final double weight;
	public final Type type;

	public State(String name, Type type, double weight) {
		this.name = name;
		this.type = type;
		this.weight = weight;
	}

	public boolean isTrap() {
		return type == Type.TRAP;
	}
	public boolean isAccept() {
		return type == Type.ACCEPT;
	}


	@Override
	public String toString() {
		return "MonitorState: " + name;
	}


	public void removeOutTransition(Transition t) {
		transitionsOut.remove(t);
	}

	public void removeInTransition(Transition t) {
		transitionsIn.remove(t);
	}

	public void addOutTransition(Transition t) {
		if(!transitionsOut.contains(t)) {
			transitionsOut.add(t);
		}
	}

	public void addInTransition(Transition t) {
		if(!transitionsIn.contains(t)) {
			transitionsIn.add(t);
		}
	}

	// Override equals and hashCode to compare State objects by their name
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		State state = (State) obj;
		return name.equals(state.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode() + type.hashCode() + (int)weight;
	}
}
