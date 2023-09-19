package tools.refinery.store.monitor.internal.model;

import java.util.*;

public class StateMachine {
	protected int stateId = 1;
	public final Set<Transition> transitions;
	public final Set<State> states;
	public final State startState;
	public final ClockHolder clockHolder;

	public StateMachine() {
		this.transitions = new HashSet<>();
		this.states = new HashSet<>();
		this.startState = this.createState(State.Type.START);
		this.clockHolder = new ClockHolder();
	}

	public State createState(State.Type type) {
		State s = new State(this.stateId++, type);
		this.states.add(s);
		return s;
	}

	public State createState() {
		return createState(State.Type.INTERMEDIATE);
	}

	public Transition createTransition(State from, Guard guard, State to, ClockResetAction action) {
		for (Clock c : action.clocksToReset) {
			clockHolder.put(c, 0);
		}
		return createTransitionImpl(from, guard, to, action);
	}

	public Transition createTransition(State from, Guard guard, State to) {
		return createTransitionImpl(from, guard, to, new ClockResetAction());
	}

	public Transition createTransitionImpl(State from, Guard guard, State to, ClockResetAction action) {
		Transition t = new Transition(from, guard, to, action);
		from.addOutTransition(t);
		to.addInTransition(t);
		this.transitions.add(t);
		return t;
	}
}
