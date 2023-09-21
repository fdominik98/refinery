package tools.refinery.store.monitor.utils;

import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.term.NodeVariable;
import java.util.ArrayList;
import java.util.List;

public class TrafficSituationAutomaton {

	public StateMachine stateMachine = new StateMachine(3);

	public final State s1 = stateMachine.startState;
	public final State s2;
	public final State s3;
	public final State s4;
	public final NodeVariable a1;
	public final NodeVariable a2;


	public TrafficSituationAutomaton(TrafficSituationMetaModel metaModel) {
		s2 = stateMachine.createState(2);
		s3 = stateMachine.createState(1);
		s4 = stateMachine.createState(State.Type.ACCEPT, 0);

		a1 =  NodeVariable.of("a1");
		a2 =  NodeVariable.of("a2");

		var guard_1_0 = Guard.of(metaModel.isInDirection(a1, a2, 1, 0));
		var guard_0_0 = Guard.of(metaModel.isInDirection(a1, a2, 0, 0));
		var guard_neg1_0 = Guard.of(metaModel.isInDirection(a1, a2, 0, -1));

		stateMachine.createTransition(s1, guard_1_0, s2);
		stateMachine.createTransition(s2, guard_0_0, s3);
		stateMachine.createTransition(s3, guard_neg1_0, s4);
	}
}
