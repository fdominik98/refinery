package tools.refinery.store.monitor.caseStudies.trafficSituationDemoCaseStudy;

import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.internal.actions.ClockResetAction;
import tools.refinery.store.monitor.internal.guards.ClockGreaterThanTimeConstraint;
import tools.refinery.store.monitor.internal.guards.ClockLessOrEqThanTimeConstraint;
import tools.refinery.store.monitor.internal.guards.Guard;
import tools.refinery.store.monitor.internal.model.Clock;
import tools.refinery.store.monitor.internal.model.State;
import tools.refinery.logic.term.NodeVariable;

public class TrafficSituationDemoAutomaton extends AutomatonInstance {
	public final State EgoIsBehindCar;
	public final State EgoIsSwitchingToOppositeLane;
	public final State EgoIsAtOppositeLane;
	public final State ApproachingCarIsComing;
	public final State EgoIsInFrontOfCar;
	public final State ApproachingCarIsClose;
	public final State EgoIsSwitchingToOwnLane;
	public final State AcceptState;
	public final State TrapState;
	public final NodeVariable ego;
	public final NodeVariable c1;
	public final NodeVariable c2;


	public TrafficSituationDemoAutomaton(TrafficSituationDemoMetaModel metaModel) {
		super(0);
		EgoIsBehindCar = stateMachine.createState(1, "EgoIsBehindCar");
		EgoIsSwitchingToOppositeLane = stateMachine.createState(2, "EgoIsSwitchingToOppositeLane");
		EgoIsAtOppositeLane = stateMachine.createState( 4, "EgoIsAtOppositeLane");
		ApproachingCarIsComing = stateMachine.createState( 8, "ApproachingCarIsComing");
		EgoIsInFrontOfCar = stateMachine.createState(16, "EgoIsInFrontOfCar");
		ApproachingCarIsClose = stateMachine.createState(32, "ApproachingCarIsClose");
		EgoIsSwitchingToOwnLane = stateMachine.createState(64, "EgoIsSwitchingToOwnLane");

		AcceptState = stateMachine.createState(State.Type.ACCEPT, 128, "AcceptState");
		TrapState = stateMachine.createState(State.Type.TRAP, "TrapState");


		Clock acceptClock = new Clock("AcceptClock");
		Clock atOppositeLaneClock = new Clock("AtOppositeLaneClock");
		Clock behindCarClock = new Clock("BehindCarClock");

		ego =  NodeVariable.of("ego");
		c1 =  NodeVariable.of("car1");
		c2 =  NodeVariable.of("car2");

		// EgoIsBehindCar
		stateMachine.createTransition(
				stateMachine.startState,
				Guard.of(metaModel.egoIsBehindCar(ego, c1)),
				EgoIsBehindCar,
				new ClockResetAction(behindCarClock)
		);

		stateMachine.createTransition(EgoIsSwitchingToOppositeLane,
				Guard.of(metaModel.onOwnLane(ego)),
				EgoIsBehindCar,
				new ClockResetAction(behindCarClock)
		);


		// EgoIsSwitchingToOppositeLane
		stateMachine.createTransition(EgoIsBehindCar,
				Guard.of(metaModel.switchingToOppositeLaneWithTraffic(ego, c2)),
				EgoIsSwitchingToOppositeLane
		);

		stateMachine.createTransition(EgoIsAtOppositeLane,
				Guard.of(metaModel.switchingToSameDirectionLane(ego)),
				EgoIsSwitchingToOppositeLane
		);


		// EgoIsAtOppositeLane
		stateMachine.createTransition(EgoIsSwitchingToOppositeLane,
				Guard.of(metaModel.onOppositeLane(ego)),
				EgoIsAtOppositeLane,
				new ClockResetAction(atOppositeLaneClock)
		);

		/*// ApproachingCarIsComing
		stateMachine.createTransition(EgoIsAtOppositeLane,
				Guard.of(metaModel.otherCarAppearedInFront(ego, c2)),
				ApproachingCarIsComing
		);*/

		// EgoIsInFrontOfCar
		stateMachine.createTransition(EgoIsAtOppositeLane,
				Guard.of(metaModel.egoInFrontOfCar(ego, c1, c2)),
				EgoIsInFrontOfCar
		);

		// ApproachingCarIsClose
		/*
		stateMachine.createTransition(EgoIsAtOppositeLane,
				Guard.of(metaModel.isDistanceLess(ego, c2, 5)),
				ApproachingCarIsClose
		);*/

		stateMachine.createTransition(EgoIsInFrontOfCar,
				Guard.of(metaModel.isDistanceLess(ego, c2, 5)),
				ApproachingCarIsClose
		);

		// EgoIsSwitchingToOwnLane
		stateMachine.createTransition(ApproachingCarIsClose,
				Guard.of(metaModel.switchingToOwnLane(ego, c1, c2)),
				EgoIsSwitchingToOwnLane,
				new ClockResetAction(atOppositeLaneClock)
		);

		// AcceptState
		stateMachine.createTransition(EgoIsSwitchingToOwnLane,
				Guard.of(metaModel.onOwnLane(ego)),
				AcceptState,
				new ClockResetAction(acceptClock)
		);

		// TrapState
		/*stateMachine.createTransition(EgoIsAtOppositeLane,
				Guard.of(new ClockGreaterThanTimeConstraint(atOppositeLaneClock, 6)),
				TrapState
		);*/
	}
}
