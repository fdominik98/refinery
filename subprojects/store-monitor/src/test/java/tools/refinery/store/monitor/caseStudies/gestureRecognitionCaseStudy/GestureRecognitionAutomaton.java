package tools.refinery.store.monitor.caseStudies.gestureRecognitionCaseStudy;

import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.internal.guards.Guard;
import tools.refinery.store.monitor.internal.model.State;
import tools.refinery.logic.term.NodeVariable;

public class GestureRecognitionAutomaton extends AutomatonInstance {

	public final State movedUp;
	public final State success;
	public final State movedDown;
	public final State trap;
	public final State above;
	public final NodeVariable hand;



	public GestureRecognitionAutomaton(GestureRecognitionMetaModel metaModel) {
		super(4);

		movedUp = stateMachine.createState(3, "MovedUp");
		movedDown = stateMachine.createState( 0, "MovedDown");
		success = stateMachine.createState(State.Type.ACCEPT, 10, "Success");
		above = stateMachine.createState( 6, "Above");
		trap = stateMachine.createState(State.Type.TRAP, "Trap");

		hand =  NodeVariable.of("hand");

		var handAboveHead = Guard.of(metaModel.handAboveHead(hand));
		var handBelowHead = Guard.of(metaModel.handBelowHead(hand));
		var stretchedArm = Guard.of(metaModel.stretchedArm(hand));
		var handMovedUp = Guard.of(metaModel.handMovedUp(hand));
		var handMovedDown = Guard.of(metaModel.handMovedDown(hand));

		stateMachine.createTransition(stateMachine.startState, stretchedArm, movedUp);

		stateMachine.createTransition(movedUp, handMovedDown, movedDown);

		stateMachine.createTransition(movedDown, handMovedUp, movedUp);

		stateMachine.createTransition(movedUp, handAboveHead, above);

		stateMachine.createTransition(above, handBelowHead, success);

		stateMachine.createTransition(success, Guard.of(), trap);
	}
}
