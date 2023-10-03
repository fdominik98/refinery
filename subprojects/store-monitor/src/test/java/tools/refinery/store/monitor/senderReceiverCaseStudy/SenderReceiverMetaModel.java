package tools.refinery.store.monitor.senderReceiverCaseStudy;

import tools.refinery.store.dse.transition.Rule;
import tools.refinery.store.monitor.actions.IncreaseIntegerActionLiteral;
import tools.refinery.store.query.literal.CallPolarity;
import tools.refinery.store.query.term.Variable;
import tools.refinery.store.query.view.AnySymbolView;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;

import java.util.ArrayList;
import java.util.List;

import static tools.refinery.store.dse.transition.actions.ActionLiterals.add;
import static tools.refinery.store.dse.transition.actions.ActionLiterals.remove;
import static tools.refinery.store.query.literal.Literals.not;

public final class SenderReceiverMetaModel {
	public Symbol<Boolean> messageSymbol = Symbol.of("Message", 1);
	public AnySymbolView messageView = new KeyOnlyView<>(messageSymbol);
	public Symbol<Boolean> senderSymbol = Symbol.of("Sender", 1);
	public AnySymbolView senderView = new KeyOnlyView<>(senderSymbol);
	public Symbol<Boolean> receiverSymbol = Symbol.of("Receiver", 1);
	public AnySymbolView receiverView = new KeyOnlyView<>(receiverSymbol);
	public Symbol<Boolean> routerSymbol = Symbol.of("Router", 1);
	public AnySymbolView routerView = new KeyOnlyView<>(routerSymbol);

	public Symbol<Boolean> sendSymbol = Symbol.of("send", 2);
	public AnySymbolView sendView = new KeyOnlyView<>(sendSymbol);
	public Symbol<Boolean> receiveSymbol = Symbol.of("receive", 2);
	public AnySymbolView receiveView = new KeyOnlyView<>(receiveSymbol);
	public Symbol<Boolean> nextSymbol = Symbol.of("next", 2);
	public AnySymbolView nextView = new KeyOnlyView<>(nextSymbol);
	public Symbol<Boolean> doneSymbol = Symbol.of("done", 2);
	public AnySymbolView doneView = new KeyOnlyView<>(doneSymbol);
	public Symbol<Boolean> atSymbol = Symbol.of("at", 2);
	public AnySymbolView atView = new KeyOnlyView<>(atSymbol);
	public List<Symbol<Boolean>> symbols = new ArrayList<>();
	public List<Rule> transformationRules = new ArrayList<>();

	public SenderReceiverMetaModel(Symbol<Integer> clockSymbol){
		symbols.add(messageSymbol);
		symbols.add(senderSymbol);
		symbols.add(receiverSymbol);
		symbols.add(routerSymbol);
		symbols.add(sendSymbol);
		symbols.add(receiveSymbol);
		symbols.add(nextSymbol);
		symbols.add(doneSymbol);
		symbols.add(atSymbol);

		var sendRule = Rule.of("send", (builder, sender, router, message) -> builder
				.clause(
						messageView.call(message),
						senderView.call(sender),
						routerView.call(router),
						sendView.call(sender, router),
						atView.call(CallPolarity.NEGATIVE, message, Variable.of()),
						doneView.call(CallPolarity.NEGATIVE, message, message)
				)
				.action(
						add(atSymbol, message, router),
						new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
				)
		);

		var receiveRule = Rule.of("receive", (builder, router, receiver, message) -> builder
				.clause(
						messageView.call(message),
						receiverView.call(receiver),
						routerView.call(router),
						receiveView.call(router, receiver),
						atView.call(message, router),
						doneView.call(CallPolarity.NEGATIVE, message, message)
				)
				.action(
						add(doneSymbol, message, message),
						remove(atSymbol, message, router),
						new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
				)
		);

		var transmitRule = Rule.of("transmit", (builder, router1, router2, message) -> builder
				.clause(
						messageView.call(message),
						routerView.call(router1),
						routerView.call(router2),
						nextView.call(router1, router2),
						atView.call(message, router1),
						doneView.call(CallPolarity.NEGATIVE, message, message)
				)
				.action(
						remove(atSymbol, message, router1),
						add(atSymbol, message, router2),
						new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
				)
		);

		transformationRules.add(sendRule);
		transformationRules.add(receiveRule);
		transformationRules.add(transmitRule);
	}
}
