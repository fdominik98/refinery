package tools.refinery.store.monitor.internal.model;

import java.util.ArrayList;
import java.util.List;

public class State {

	public enum Type {
		START,
		INTERMEDIATE,
		FINAL,
		TRAP,
		ACCEPT
	}

	public final List<Transition> transitionsOut = new ArrayList<>();
	public final List<Transition> transitionsIn = new ArrayList<>();

	public final int id;
	public final Type type;

	public State(int id, Type type) {
		this.id = id;
		this.type = type;
	}

	public boolean isStart() {
		return type == Type.START;
	}
	public boolean isFinal() {
		return type == Type.FINAL;
	}
	public boolean isTrap() {
		return type == Type.TRAP;
	}
	public boolean isAccept() {
		return type == Type.ACCEPT;
	}


	@Override
	public String toString() {
		return "state" + id;
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
}
