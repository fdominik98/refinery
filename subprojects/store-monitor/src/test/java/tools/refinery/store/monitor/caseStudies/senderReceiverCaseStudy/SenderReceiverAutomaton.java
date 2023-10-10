package tools.refinery.store.monitor.caseStudies.senderReceiverCaseStudy;

import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.term.Variable;

public class SenderReceiverAutomaton extends AutomatonInstance {

	public final State sent;
	public final State timeout;
	public final State done;

	public SenderReceiverAutomaton(SenderReceiverMetaModel metaModel) {
		super(0);

		sent = stateMachine.createState(5);
		timeout = stateMachine.createState(State.Type.ACCEPT, 10);
		done = stateMachine.createState(State.Type.TRAP, 0);

		Clock clock1 = new Clock("clock1");
		var router = Variable.of("router");
		var sender = Variable.of("sender");
		var message = Variable.of("message");

		var sendGuard = Guard.of(Query.of(builder -> {
			builder.parameters(sender, router, message);
			builder.clause(
					metaModel.sendView.call(sender, router),
					metaModel.atView.call(message, router)
			);
		}));

		var doneGuard = Guard.of(Query.of(builder -> {
			builder.parameters(message);
			builder.clause(
					metaModel.doneView.call(message, message)
			);
		}));

		var timeoutGuard = Guard.of(new ClockGreaterThanTimeConstraint(clock1, 5));

		stateMachine.createTransition(stateMachine.startState, sendGuard, sent, new ClockResetAction(clock1));
		stateMachine.createTransition(sent, doneGuard, done);
		stateMachine.createTransition(sent, timeoutGuard, timeout);
	}
}
