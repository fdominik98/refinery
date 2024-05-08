package tools.refinery.store.monitor.caseStudies.trafficSituationDemoCaseStudy;

import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.internal.actions.ClockResetAction;
import tools.refinery.store.monitor.internal.guards.ClockGreaterThanTimeConstraint;
import tools.refinery.store.monitor.internal.guards.Guard;
import tools.refinery.store.monitor.internal.model.Clock;
import tools.refinery.store.monitor.internal.model.State;
import tools.refinery.logic.term.NodeVariable;

public class TrafficSituationDemoAutomaton extends AutomatonInstance {
	public final State s2;
	public final State s3;
	public final State s4;
	public final State accept;
	public final State trap;
	public final NodeVariable ego;
	public final NodeVariable c;


	public TrafficSituationDemoAutomaton(TrafficSituationDemoMetaModel metaModel) {
		super(0);
		s2 = stateMachine.createState(1);
		s3 = stateMachine.createState(2);
		s4 = stateMachine.createState(3);
		accept = stateMachine.createState(State.Type.ACCEPT, 10);
		trap = stateMachine.createState(State.Type.TRAP);

		Clock clock1 = new Clock("clock1");

		ego =  NodeVariable.of("ego");
		c =  NodeVariable.of("car");

		var isDistance1Query = metaModel.isEgoDistance1(ego, c);
		var isDistance2Query = metaModel.isEgoDistance2(ego, c);

		stateMachine.createTransition(stateMachine.startState, Guard.of(isDistance2Query), accept,
				new ClockResetAction(clock1));

		/*stateMachine.createTransition(s2, Guard.of(isDistance2Query).neg(), stateMachine.startState,
				new ClockResetAction(clock1));

		stateMachine.createTransition(s2, Guard.of(isDistance2Query,
						new ClockGreaterThanTimeConstraint(clock1, 2)),
						accept);*/

		stateMachine.createTransition(accept, Guard.of(isDistance2Query).neg(), stateMachine.startState,
				new ClockResetAction(clock1));

	}
}
