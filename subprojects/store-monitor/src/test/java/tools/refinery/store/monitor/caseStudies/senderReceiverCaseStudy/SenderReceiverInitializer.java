package tools.refinery.store.monitor.caseStudies.senderReceiverCaseStudy;

import tools.refinery.store.model.Interpretation;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.tuple.Tuple;
import tools.refinery.store.tuple.Tuple1;

public final class SenderReceiverInitializer extends ModelInitializer {
	public final Tuple1 message1;
	public final Tuple1 message2;
	public final Tuple1 message3;
	public final Tuple1 sender;
	public final Tuple1 receiver;
	public final Tuple1 router1;
	public final Tuple1 router2;
	public final Tuple1 router3;
	public final Tuple1 router4;
	public final Tuple1 router5;
	public final Interpretation<Boolean> routerInterpretation;
	public final Interpretation<Boolean> senderInterpretation;
	public final Interpretation<Boolean> receiverInterpretation;
	public final Interpretation<Boolean> messageInterpretation;
	public final Interpretation<Boolean> nextInterpretation;
	public final Interpretation<Boolean> atInterpretation;
	public final Interpretation<Boolean> sendInterpretation;
	public final Interpretation<Boolean> receiveInterpretation;
	public final Interpretation<Boolean> doneInterpretation;

	public SenderReceiverInitializer(Model model, SenderReceiverMetaModel metaModel) {
		super(model);

		routerInterpretation = model.getInterpretation(metaModel.routerSymbol);
		senderInterpretation = model.getInterpretation(metaModel.senderSymbol);
		receiverInterpretation = model.getInterpretation(metaModel.receiverSymbol);
		messageInterpretation = model.getInterpretation(metaModel.messageSymbol);
		nextInterpretation = model.getInterpretation(metaModel.nextSymbol);
		atInterpretation = model.getInterpretation(metaModel.atSymbol);
		sendInterpretation = model.getInterpretation(metaModel.sendSymbol);
		receiveInterpretation = model.getInterpretation(metaModel.receiveSymbol);
		doneInterpretation = model.getInterpretation(metaModel.doneSymbol);

		message1 = modificationAdapter.createObject();
		message2 = modificationAdapter.createObject();
		message3 = modificationAdapter.createObject();
		router1 = modificationAdapter.createObject();
		router2 = modificationAdapter.createObject();
		router3 = modificationAdapter.createObject();
		router4 = modificationAdapter.createObject();
		router5 = modificationAdapter.createObject();
		sender = modificationAdapter.createObject();
		receiver = modificationAdapter.createObject();

		routerInterpretation.put(router1, true);
		routerInterpretation.put(router2, true);
		routerInterpretation.put(router3, true);
		routerInterpretation.put(router4, true);
		routerInterpretation.put(router5, true);
		messageInterpretation.put(message1, true);
		messageInterpretation.put(message2, true);
		messageInterpretation.put(message3, true);
		receiverInterpretation.put(receiver, true);
		senderInterpretation.put(sender, true);

		sendInterpretation.put(Tuple.of(sender.get(0), router1.get(0)), true);
		receiveInterpretation.put(Tuple.of(router3.get(0), receiver.get(0)), true);
		nextInterpretation.put(Tuple.of(router1.get(0), router4.get(0)), true);
		nextInterpretation.put(Tuple.of(router1.get(0), router2.get(0)), true);
		nextInterpretation.put(Tuple.of(router2.get(0), router3.get(0)), true);
		nextInterpretation.put(Tuple.of(router4.get(0), router5.get(0)), true);
		nextInterpretation.put(Tuple.of(router5.get(0), router3.get(0)), true);
	}
}
