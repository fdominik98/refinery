package tools.refinery.store.monitor.caseStudies.senderReceiverCaseStudy;

import tools.refinery.store.model.Interpretation;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.tuple.Tuple;
import tools.refinery.store.tuple.Tuple1;

public final class SenderReceiverInitializer2 extends ModelInitializer {
	public final Tuple1 message1;
	public final Tuple1 message2;
	public final Tuple1 message3;
	public final Tuple1 message4;
	public final Tuple1 message5;
	public final Tuple1 message6;
	public final Tuple1 sender;
	public final Tuple1 receiver;
	public final Tuple1 router1;
	public final Tuple1 router2;
	public final Tuple1 router3;
	public final Tuple1 router4;
	public final Tuple1 router5;
	public final Tuple1 router1_;
	public final Tuple1 router2_;
	public final Tuple1 router3_;
	public final Tuple1 router4_;
	public final Tuple1 router5_;
	public final Interpretation<Boolean> routerInterpretation;
	public final Interpretation<Boolean> senderInterpretation;
	public final Interpretation<Boolean> receiverInterpretation;
	public final Interpretation<Boolean> messageInterpretation;
	public final Interpretation<Boolean> nextInterpretation;
	public final Interpretation<Boolean> atInterpretation;
	public final Interpretation<Boolean> sendInterpretation;
	public final Interpretation<Boolean> receiveInterpretation;
	public final Interpretation<Boolean> doneInterpretation;

	public SenderReceiverInitializer2(Model model, SenderReceiverMetaModel metaModel) {
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
		message4 = modificationAdapter.createObject();
		message5 = modificationAdapter.createObject();
		message6 = modificationAdapter.createObject();

		router1 = modificationAdapter.createObject();
		router2 = modificationAdapter.createObject();
		router3 = modificationAdapter.createObject();
		router4 = modificationAdapter.createObject();
		router5 = modificationAdapter.createObject();

		router1_ = modificationAdapter.createObject();
		router2_ = modificationAdapter.createObject();
		router3_ = modificationAdapter.createObject();
		router4_ = modificationAdapter.createObject();
		router5_ = modificationAdapter.createObject();

		sender = modificationAdapter.createObject();
		receiver = modificationAdapter.createObject();

		routerInterpretation.put(router1, true);
		routerInterpretation.put(router2, true);
		routerInterpretation.put(router3, true);
		routerInterpretation.put(router4, true);
		routerInterpretation.put(router5, true);

		routerInterpretation.put(router1_, true);
		routerInterpretation.put(router2_, true);
		routerInterpretation.put(router3_, true);
		routerInterpretation.put(router4_, true);
		routerInterpretation.put(router5_, true);
		messageInterpretation.put(message1, true);
		messageInterpretation.put(message2, true);
		messageInterpretation.put(message3, true);

		messageInterpretation.put(message4, true);
		messageInterpretation.put(message5, true);
		messageInterpretation.put(message6, true);
		receiverInterpretation.put(receiver, true);
		senderInterpretation.put(sender, true);

		sendInterpretation.put(Tuple.of(sender.get(0), router1.get(0)), true);
		nextInterpretation.put(Tuple.of(router1.get(0), router4.get(0)), true);
		nextInterpretation.put(Tuple.of(router1.get(0), router2.get(0)), true);
		nextInterpretation.put(Tuple.of(router2.get(0), router3.get(0)), true);
		nextInterpretation.put(Tuple.of(router4.get(0), router5.get(0)), true);
		nextInterpretation.put(Tuple.of(router5.get(0), router3.get(0)), true);

		nextInterpretation.put(Tuple.of(router5.get(0), router1_.get(0)), true);
		nextInterpretation.put(Tuple.of(router1_.get(0), router4_.get(0)), true);
		nextInterpretation.put(Tuple.of(router1_.get(0), router2_.get(0)), true);
		nextInterpretation.put(Tuple.of(router2_.get(0), router3_.get(0)), true);
		nextInterpretation.put(Tuple.of(router4_.get(0), router5_.get(0)), true);
		nextInterpretation.put(Tuple.of(router5_.get(0), router3_.get(0)), true);

		receiveInterpretation.put(Tuple.of(router5_.get(0), receiver.get(0)), true);
		receiveInterpretation.put(Tuple.of(router3.get(0), receiver.get(0)), true);
		receiveInterpretation.put(Tuple.of(router3_.get(0), receiver.get(0)), true);

	}

	@Override
	public String getInstanceId() {
		return "6message10router";
	}
}
