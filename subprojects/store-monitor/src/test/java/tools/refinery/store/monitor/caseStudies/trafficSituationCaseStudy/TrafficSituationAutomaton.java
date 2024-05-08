package tools.refinery.store.monitor.caseStudies.trafficSituationCaseStudy;

import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.internal.guards.Guard;
import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.logic.term.NodeVariable;

public class TrafficSituationAutomaton extends AutomatonInstance {
	public final State s2;
	public final State s3;
	public final State s4;
	public final State accept;
	public final State trap;
	public final NodeVariable a1;
	public final NodeVariable a2;


	public TrafficSituationAutomaton(TrafficSituationMetaModel metaModel) {
		super(0);
		s2 = stateMachine.createState(1);
		s3 = stateMachine.createState(2);
		s4 = stateMachine.createState(3);
		accept = stateMachine.createState(State.Type.ACCEPT, 10);
		trap = stateMachine.createState(State.Type.TRAP);

		a1 =  NodeVariable.of("a1");
		a2 =  NodeVariable.of("a2");

		var guard_0_2 = Guard.of(metaModel.isInDirection(a1, a2, 0, 2));
		var guard_0_1 = Guard.of(metaModel.isInDirection(a1, a2, 0, 1));
		var guard_1_0 = Guard.of(metaModel.isInDirection(a1, a2, 1, 0));
		var guard_0_neg1 = Guard.of(metaModel.isInDirection(a1, a2, 0, -1));

		stateMachine.createTransition(stateMachine.startState, guard_0_2, s2);
		stateMachine.createTransition(s2, guard_0_1, s3);
		stateMachine.createTransition(s3, guard_1_0, s4);
		stateMachine.createTransition(s4, guard_0_neg1, accept);

		stateMachine.createTransition(s2, guard_0_1, stateMachine.startState);
		stateMachine.createTransition(s4, guard_0_1, trap);
		stateMachine.createTransition(accept, guard_0_neg1.neg(), trap);
	}
}
