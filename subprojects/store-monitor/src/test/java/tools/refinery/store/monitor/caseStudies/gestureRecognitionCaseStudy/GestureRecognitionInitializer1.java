package tools.refinery.store.monitor.caseStudies.gestureRecognitionCaseStudy;

import tools.refinery.store.model.Interpretation;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.monitor.utils.Vector;
import tools.refinery.store.tuple.Tuple;
public final class GestureRecognitionInitializer1 extends ModelInitializer {
	public final Interpretation<Boolean> joinInterpretation;
	public final Interpretation<Boolean> bodyInterpretation;
	public final Interpretation<Vector> headInterpretation;
	public final Interpretation<Vector> handInterpretation;
	public final Interpretation<Vector> shoulderInterpretation;
	public final Interpretation<Vector> elbowInterpretation;

	public final Tuple body;
	public final Tuple rightHand;
	public final Tuple rightElbow;
	public final Tuple rightShoulder;

	public final Tuple leftHand;
	public final Tuple leftElbow;
	public final Tuple leftShoulder;
	public final Tuple head;

	public GestureRecognitionInitializer1(Model model, GestureRecognitionMetaModel metaModel) {
		super(model);
		joinInterpretation = model.getInterpretation(metaModel.joinSymbol);
		bodyInterpretation = model.getInterpretation(metaModel.bodySymbol);
		headInterpretation = model.getInterpretation(metaModel.headSymbol);
		handInterpretation = model.getInterpretation(metaModel.handSymbol);
		shoulderInterpretation = model.getInterpretation(metaModel.shoulderSymbol);
		elbowInterpretation = model.getInterpretation(metaModel.elbowSymbol);

		body = modificationAdapter.createObject();
		head = modificationAdapter.createObject();
		rightHand = modificationAdapter.createObject();
		rightElbow = modificationAdapter.createObject();
		rightShoulder = modificationAdapter.createObject();
		leftHand = modificationAdapter.createObject();
		leftElbow = modificationAdapter.createObject();
		leftShoulder = modificationAdapter.createObject();

		bodyInterpretation.put(body, true);
		headInterpretation.put(head, Vector.of(5, 10));
		joinInterpretation.put(Tuple.of(head.get(0), body.get(0)), true);

		shoulderInterpretation.put(rightShoulder, Vector.of(5, 8));
		elbowInterpretation.put(rightElbow,	Vector.of(5, 8));
		handInterpretation.put(rightHand, Vector.of(5,	8));

		joinInterpretation.put(Tuple.of(rightShoulder.get(0), body.get(0)), true);
		joinInterpretation.put(Tuple.of(rightElbow.get(0), rightShoulder.get(0)), true);
		joinInterpretation.put(Tuple.of(rightHand.get(0), rightElbow.get(0)), true);

		shoulderInterpretation.put(leftShoulder, Vector.of(-5, 8));
		elbowInterpretation.put(leftElbow, Vector.of(-5, 8));
		handInterpretation.put(leftHand, Vector.of(-5,8));

		joinInterpretation.put(Tuple.of(leftShoulder.get(0), body.get(0)), true);
		joinInterpretation.put(Tuple.of(leftElbow.get(0), leftShoulder.get(0)), true);
		joinInterpretation.put(Tuple.of(leftHand.get(0), leftElbow.get(0)), true);

		/*
		    O
		    |
		   ||
		   ||
		 __||
		 */
	}

	@Override
	public String getInstanceId() {
		return "1arm";
	}
}
