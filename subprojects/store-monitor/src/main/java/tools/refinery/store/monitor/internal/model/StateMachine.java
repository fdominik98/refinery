package tools.refinery.store.monitor.internal.model;

import java.util.*;

public class StateMachine {
	private int stateId = 1;
	public final Set<Transition> transitions = new HashSet<>();
	public final Set<State> states = new HashSet<>();;
	public final State startState;
	public final ClockHolder clockHolder = new ClockHolder();
	private double maxWeight = 0;

	public StateMachine(int startWeight) {
		this.startState = this.createState(State.Type.START, startWeight);
	}

	public StateMachine() {
		this.startState = this.createState(State.Type.START);
	}

	public State createState() {
		return createState(State.Type.INTERMEDIATE, 0);
	}

	public State createState(int weight) {return createState(State.Type.INTERMEDIATE, weight);}

	public State createState(State.Type type) {return createState(type, 0);}

	public State createState(State.Type type, double weight) {
		State s = new State(this.stateId++, type, weight);
		this.states.add(s);
		if(maxWeight < s.weight) {
			maxWeight = s.weight;
		}
		return s;
	}

	public double getMaxWeight() {
		return maxWeight;
	}

	public Transition createTransition(State from, Guard guard, State to, ClockResetAction action) {
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
		for (Clock c : action.clocksToReset) {
			clockHolder.put(c, 0);
		}
		for (TimeConstraint tc : guard.timeConstraints) {
			clockHolder.put(tc.clock, 0);
		}
		return t;
	}
}
