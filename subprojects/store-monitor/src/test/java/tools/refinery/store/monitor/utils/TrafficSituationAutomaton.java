package tools.refinery.store.monitor.utils;

import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.term.NodeVariable;
import java.util.ArrayList;
import java.util.List;

public class TrafficSituationAutomaton {

	public StateMachine stateMachine = new StateMachine();

	public TrafficSituationAutomaton(TrafficSituationMetaModel metaModel) {

		NodeVariable a1 = NodeVariable.of("a1");
		NodeVariable a2 = NodeVariable.of("a2");

		var guard_1_0 = Guard.of(metaModel.isInDirection(a1, a2, 1, 0));
		var guard_0_0 = Guard.of(metaModel.isInDirection(a1, a2, 0, 0));
		var guard_neg1_0 = Guard.of(metaModel.isInDirection(a1, a2, -1, 0));

		State s2 = stateMachine.createState();
		State s3 = stateMachine.createState();
		State s4 = stateMachine.createState(State.Type.ACCEPT);

		stateMachine.createTransition(stateMachine.startState, guard_1_0, s2);
		stateMachine.createTransition(s2, guard_1_0, s2);
		stateMachine.createTransition(s2, guard_0_0, s3);
		stateMachine.createTransition(s3, guard_0_0, s3);
		stateMachine.createTransition(s3, guard_neg1_0, s4);
	}
}
