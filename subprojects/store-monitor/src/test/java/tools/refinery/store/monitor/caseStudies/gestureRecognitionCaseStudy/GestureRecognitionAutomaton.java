package tools.refinery.store.monitor.caseStudies.gestureRecognitionCaseStudy;

import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.internal.guards.Guard;
import tools.refinery.store.monitor.internal.model.State;
import tools.refinery.store.query.term.NodeVariable;

public class GestureRecognitionAutomaton extends AutomatonInstance {

	public final State s2;
	public final State s3;
	public final State s4;
	public final State s5;
	public final State s6;
	public final NodeVariable body;


	public GestureRecognitionAutomaton(GestureRecognitionMetaModel metaModel) {
		super(4);

		s2 = stateMachine.createState(3);
		s3 = stateMachine.createState( 7);
		s4 = stateMachine.createState(State.Type.ACCEPT, 10);
		s5 = stateMachine.createState(5);
		s6 = stateMachine.createState(State.Type.TRAP);

		body =  NodeVariable.of("body");

		var rightHandAboveHead = Guard.of(metaModel.rightHandAboveHead(body));
		var stretchedRightArm = Guard.of(metaModel.stretchedRightArm(body));
		var rightHandMovedUp = Guard.of(metaModel.rightHandMovedUp(body));
		var stretchedRightArmAndMovedDown = Guard.of(metaModel.stretchedRightArmAndMovedDown(body));
		var stretchedRightArmAndMovedUp = Guard.of(metaModel.stretchedRightArmAndMovedUp(body));

		stateMachine.createTransition(stateMachine.startState, stretchedRightArm, s2);

		stateMachine.createTransition(s2, rightHandMovedUp.neg(), s5);

		stateMachine.createTransition(s2, rightHandAboveHead, s3);
		stateMachine.createTransition(s3, rightHandAboveHead.neg(), s4);

		stateMachine.createTransition(s4, rightHandAboveHead.neg(), s6);
	}
}
