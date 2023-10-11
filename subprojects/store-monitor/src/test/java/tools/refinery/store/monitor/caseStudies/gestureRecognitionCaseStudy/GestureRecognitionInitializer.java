package tools.refinery.store.monitor.caseStudies.gestureRecognitionCaseStudy;

import tools.refinery.store.dse.modification.ModificationAdapter;
import tools.refinery.store.model.Interpretation;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.tuple.Tuple;
public final class GestureRecognitionInitializer extends ModelInitializer {
	public final Interpretation<Boolean> bodyInterpretation;
	public final Interpretation<GestureRecognitionMetaModel.Vector> headInterpretation;
	public final Interpretation<GestureRecognitionMetaModel.Vector> rightHandInterpretation;
	public final Interpretation<GestureRecognitionMetaModel.Vector> rightShoulderInterpretation;
	public final Interpretation<GestureRecognitionMetaModel.Vector> rightElbowInterpretation;

	public final Tuple body;
	public final Tuple rightHand;
	public final Tuple rightElbow;
	public final Tuple rightShoulder;
	public final Tuple head;

	public GestureRecognitionInitializer(Model model, GestureRecognitionMetaModel metaModel) {
		super(model);

		bodyInterpretation = model.getInterpretation(metaModel.bodySymbol);
		headInterpretation = model.getInterpretation(metaModel.headSymbol);
		rightHandInterpretation = model.getInterpretation(metaModel.rightHandSymbol);
		rightShoulderInterpretation = model.getInterpretation(metaModel.rightShoulderSymbol);
		rightElbowInterpretation = model.getInterpretation(metaModel.rightElbowSymbol);

		body = modificationAdapter.createObject();
		rightHand = modificationAdapter.createObject();
		rightElbow = modificationAdapter.createObject();
		head = modificationAdapter.createObject();
		rightShoulder = modificationAdapter.createObject();

		bodyInterpretation.put(body, true);
		headInterpretation.put(Tuple.of(body.get(0), head.get(0)),
				GestureRecognitionMetaModel.Vector.of(5, 10));
		rightShoulderInterpretation.put(Tuple.of(body.get(0), rightShoulder.get(0)),
				GestureRecognitionMetaModel.Vector.of(5, 8));
		rightHandInterpretation.put(Tuple.of(body.get(0), rightHand.get(0)),
				GestureRecognitionMetaModel.Vector.of(5,	8));
		rightElbowInterpretation.put(Tuple.of(body.get(0), rightElbow.get(0)),
				GestureRecognitionMetaModel.Vector.of(5, 8));

		/*
		    O
		    |
		   ||
		   ||
		 __||
		 */
	}
}