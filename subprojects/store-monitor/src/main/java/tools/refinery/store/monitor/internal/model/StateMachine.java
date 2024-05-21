package tools.refinery.store.monitor.internal.model;

import tools.refinery.store.monitor.internal.actions.ClockResetAction;
import tools.refinery.store.monitor.internal.guards.Guard;

import java.util.*;

public class StateMachine {
	public final Set<Transition> transitions = new HashSet<>();
	public final Set<State> states = new HashSet<>();;
	public final State startState;
	public final ClockHolder clockHolder = new ClockHolder();
	private double maxWeight = 0;

	public StateMachine(double startWeight) {
		this.startState = this.createState(startWeight, "StartState");
	}

	public StateMachine() {
		this.startState = this.createState("StartState");
	}

	public State createState(String name) {
		return createState(State.Type.INTERMEDIATE, 0, name);
	}

	public State createState(double weight, String name) {return createState(State.Type.INTERMEDIATE, weight, name);}

	public State createState(State.Type type, String name) {return createState(type, 0, name);}

	public State createState(State.Type type, double weight, String name) {
		for (State state : states) {
			if (state.name.equals(name)) {
				throw new IllegalArgumentException("Two states have the same name which is illegal.");
			}
		}
		State s = new State(name, type, weight);
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
