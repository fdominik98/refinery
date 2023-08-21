package tools.refinery.store.monitor.internal.model;

import java.util.*;

public class StateMachine {
	protected int stateId = 1;
	public final Set<Transition> transitions;
	public final Set<State> states;
	public final State startState;
	public final Set<Parameter> parameters;

	public StateMachine() {
		this.transitions = new HashSet<>();
		this.states = new HashSet<>();
		this.startState = this.createState(State.Type.START);
		this.parameters = new HashSet<>();
	}

	public State createState(State.Type type) {
		State s = new State(this.stateId++, type);
		this.states.add(s);
		return s;
	}

	public State createState() {
		return createState(State.Type.INTERMEDIATE);
	}

	public Transition createTransition(State from, List<Guard> guardTriggers, State to) {
		return createTransitionImpl(from, guardTriggers, to);
	}

	public Transition createTransitionImpl(State from, List<Guard> guardTriggers, State to) {
		Transition t = new Transition(from, guardTriggers, to);
		from.addOutTransition(t);
		to.addInTransition(t);
		this.transitions.add(t);
		return t;
	}
}
