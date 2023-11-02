package tools.refinery.store.monitor.caseStudies.senderReceiverCaseStudy;

import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.internal.actions.ClockResetAction;
import tools.refinery.store.monitor.internal.guards.ClockGreaterThanTimeConstraint;
import tools.refinery.store.monitor.internal.guards.ClockLessOrEqThanTimeConstraint;
import tools.refinery.store.monitor.internal.guards.Guard;
import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.literal.CallPolarity;
import tools.refinery.store.query.term.Variable;

public class SenderReceiverAutomaton extends AutomatonInstance {

	public final State sent;
	public final State timeout;
	public final State close;
	public final State done;

	public SenderReceiverAutomaton(SenderReceiverMetaModel metaModel) {
		super(0);

		sent = stateMachine.createState(3);
		timeout = stateMachine.createState(State.Type.ACCEPT, 5);
		close = stateMachine.createState( 1);
		done = stateMachine.createState(State.Type.TRAP, 0);

		Clock clock1 = new Clock("clock1");
		var message = Variable.of("message");

		var doneQuery = Query.of(builder -> {
			builder.clause(
					metaModel.doneView.call(Variable.of(), Variable.of())
			);});

		var closeTOReceiverQuery = Query.of(builder -> {
			builder.parameters(message);
			var router = Variable.of();
			var receiver = Variable.of();
			builder.clause(
					metaModel.receiveView.call(router, receiver),
					metaModel.atView.call(message, router),
					doneQuery.call(CallPolarity.NEGATIVE)
			);
		});

		var sentGuard = Guard.of(Query.of(builder -> {
			builder.parameters(message);
			var sender = Variable.of();
			var router = Variable.of();
			builder.clause(
					metaModel.sendView.call(sender, router),
					metaModel.atView.call(message, router),
					closeTOReceiverQuery.call(CallPolarity.NEGATIVE, message),
					doneQuery.call(CallPolarity.NEGATIVE)
			);
		}));

		var closeToRecieverGuard = Guard.of(closeTOReceiverQuery);

		var doneGuard = Guard.of(doneQuery);

		var timeoutGuard = Guard.of(Query.of(builder -> builder.clause(doneQuery.call(CallPolarity.NEGATIVE))),
				new ClockGreaterThanTimeConstraint(clock1, 24));

		stateMachine.createTransition(stateMachine.startState, sentGuard, sent, new ClockResetAction(clock1));
		stateMachine.createTransition(stateMachine.startState, closeToRecieverGuard, close, new ClockResetAction(clock1));
		stateMachine.createTransition(sent, closeToRecieverGuard, close);

		stateMachine.createTransition(close, doneGuard, done);
		stateMachine.createTransition(sent, doneGuard, done);
		stateMachine.createTransition(stateMachine.startState, doneGuard, done);
		stateMachine.createTransition(timeout, doneGuard, done);

		stateMachine.createTransition(sent, timeoutGuard, timeout);
		stateMachine.createTransition(close, timeoutGuard, timeout);
	}
}
