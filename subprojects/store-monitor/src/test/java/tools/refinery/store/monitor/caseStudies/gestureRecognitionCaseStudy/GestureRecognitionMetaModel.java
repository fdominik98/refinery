package tools.refinery.store.monitor.caseStudies.gestureRecognitionCaseStudy;

import tools.refinery.store.dse.transition.Rule;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.actions.IncreaseVectorActionLiteral;
import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.caseStudies.MetaModelInstance;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.monitor.utils.VectorYTerm;
import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.literal.CallPolarity;
import tools.refinery.store.query.term.DataVariable;
import tools.refinery.store.query.term.NodeVariable;
import tools.refinery.store.query.term.Variable;
import tools.refinery.store.query.view.AnySymbolView;
import tools.refinery.store.query.view.FunctionView;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;
import java.util.List;
import static tools.refinery.store.dse.transition.actions.ActionLiterals.*;
import static tools.refinery.store.query.literal.Literals.check;
import static tools.refinery.store.query.term.int_.IntTerms.*;

public class GestureRecognitionMetaModel extends MetaModelInstance {
	@Override
	public ModelInitializer createInitializer(Model model) {
		return new GestureRecognitionInitializer(model, this);
	}

	@Override
	public AutomatonInstance createAutomaton() {
		return new GestureRecognitionAutomaton(this);
	}

	public static class Vector {
		Vector(int x, int y){
			this.x = x;
			this.y = y;
		}
		public int x;
		public int y;
		public static Vector of(int x, int y) {
			return new Vector(x, y);
		}
	}

	public Symbol<Boolean> bodySymbol = Symbol.of("Body", 1);
	public AnySymbolView bodyView = new KeyOnlyView<>(bodySymbol);
	public Symbol<Vector> rightHandSymbol = Symbol.of("RightHand",2, Vector.class);
	public AnySymbolView rightHandView = new FunctionView<>(rightHandSymbol);
	public Symbol<Vector> rightElbowSymbol = Symbol.of("RightElbow",2, Vector.class);
	public AnySymbolView rightElbowView = new FunctionView<>(rightElbowSymbol);
	public Symbol<Vector> rightShoulderSymbol = Symbol.of("RightShoulder",2, Vector.class);
	public AnySymbolView rightShoulderView = new FunctionView<>(rightShoulderSymbol);
	public Symbol<Vector> headSymbol = Symbol.of("Head",2, Vector.class);
	public AnySymbolView headView = new FunctionView<>(headSymbol);
	public Symbol<Boolean> handMovedUpSymbol = Symbol.of("HandMovedUp", 2);
	public AnySymbolView handMovedUpView = new KeyOnlyView<>(handMovedUpSymbol);

	public GestureRecognitionMetaModel(){
		super(null);
		addSymbol(bodySymbol);
		addSymbol(rightHandSymbol);
		addSymbol(rightElbowSymbol);
		addSymbol(rightShoulderSymbol);
		addSymbol(headSymbol);
		addSymbol(handMovedUpSymbol);

		var moveHandDownRule = Rule.of("MoveHandDownRule", (builder, body, hand) -> {
			builder.clause(handCondition(false).call(body, hand))
					.action(
							new IncreaseVectorActionLiteral(rightHandSymbol, List.of(body, hand), Vector.of(0, -1)),
							remove(handMovedUpSymbol, body, hand)
					);
		});
		var moveHandUpRule = Rule.of("MoveHandUpRule", (builder, body, hand) -> {
			builder.clause(handCondition(true).call(body, hand))
					.action(
							new IncreaseVectorActionLiteral(rightHandSymbol, List.of(body, hand), Vector.of(0, 1)),
							add(handMovedUpSymbol, body, hand)
					);
		});
		var moveElbowDownRule = Rule.of("MoveElbowDownRule", (builder, body, elbow, hand) -> {
			builder.clause(elbowCondition(false).call(body, elbow, hand))
					.action(
							new IncreaseVectorActionLiteral(rightElbowSymbol, List.of(body, elbow), Vector.of(0, -1)),
							new IncreaseVectorActionLiteral(rightHandSymbol, List.of(body, hand), Vector.of(0, -1)),
							remove(handMovedUpSymbol, body, hand)
					);
		});
		var moveElbowUpRule = Rule.of("MoveElbowUpRule", (builder, body, elbow, hand) -> {
			builder.clause(elbowCondition(true).call(body, elbow, hand))
					.action(
							new IncreaseVectorActionLiteral(rightElbowSymbol, List.of(body, elbow), Vector.of(0, 1)),
							new IncreaseVectorActionLiteral(rightHandSymbol, List.of(body, hand), Vector.of(0, 1)),
							add(handMovedUpSymbol, body, hand)
					);
		});

		var moveShoulderDownRule = Rule.of("MoveShoulderDownRule", builder -> {
			var body = builder.parameter();
			var head = builder.parameter();
			var shoulder = builder.parameter();
			var elbow = builder.parameter();
			var hand = builder.parameter();
			builder.clause(shoulderCondition(false).call(body, head, elbow, shoulder, hand))
					.action(
							new IncreaseVectorActionLiteral(rightElbowSymbol, List.of(body, elbow), Vector.of(0, -1)),
							new IncreaseVectorActionLiteral(rightHandSymbol, List.of(body, hand), Vector.of(0, -1)),
							new IncreaseVectorActionLiteral(rightShoulderSymbol,
									List.of(body, shoulder), Vector.of(0,-1)),
							remove(handMovedUpSymbol, body, hand)
					);
		});
		var moveShoulderUpRule = Rule.of("MoveShoulderUpRule", builder -> {
			var body = builder.parameter();
			var head = builder.parameter();
			var shoulder = builder.parameter();
			var elbow = builder.parameter();
			var hand = builder.parameter();
			builder.clause(shoulderCondition(true).call(body, head, elbow, shoulder, hand))
					.action(
							new IncreaseVectorActionLiteral(rightElbowSymbol, List.of(body, elbow), Vector.of(0, 1)),
							new IncreaseVectorActionLiteral(rightHandSymbol, List.of(body, hand), Vector.of(0, 1)),
							new IncreaseVectorActionLiteral(rightShoulderSymbol,
									List.of(body, shoulder), Vector.of(0,1)),
							add(handMovedUpSymbol, body, hand)
					);
		});

		transformationRules.add(moveHandDownRule);
		transformationRules.add(moveHandUpRule);
		transformationRules.add(moveElbowDownRule);
		transformationRules.add(moveElbowUpRule);
		transformationRules.add(moveShoulderUpRule);
		transformationRules.add(moveShoulderDownRule);
	}

	private RelationalQuery handCondition(boolean lift) {
		return Query.of((builder, body, hand) -> {
			DataVariable<Vector> handVector = Variable.of(Vector.class);
			DataVariable<Vector> elbowVector = Variable.of(Vector.class);

			var elbowY = new VectorYTerm(elbowVector);
			var handY = new VectorYTerm(handVector);

			builder.clause(
					bodyView.call(body),
					rightHandView.call(body, hand, handVector),
					rightElbowView.call(body, Variable.of(), elbowVector),
					lift ? check(lessEq(sub(handY, elbowY), constant(2))) :
							check(lessEq(sub(elbowY, handY), constant(2)))
			);
		});
	}

	private RelationalQuery elbowCondition(boolean lift) {
		return Query.of((builder, body, elbow, hand) -> {
			DataVariable<Vector> elbowVector = Variable.of(Vector.class);
			DataVariable<Vector> shoulderVector = Variable.of(Vector.class);
			DataVariable<Vector> handVector = Variable.of(Vector.class);

			var elbowY = new VectorYTerm(elbowVector);
			var shoulderY = new VectorYTerm(shoulderVector);

			builder.clause(
					bodyView.call(body),
					rightElbowView.call(body, elbow, elbowVector),
					rightShoulderView.call(body, Variable.of(), shoulderVector),
					rightHandView.call(body, hand, handVector),
					lift ? check(lessEq(sub(elbowY, shoulderY), constant(2))) :
							check(lessEq(sub(shoulderY, elbowY), constant(2)))
			);
		});
	}

	private RelationalQuery shoulderCondition(boolean lift) {
		return Query.of(builder -> {
			var body = builder.parameter();
			var head = builder.parameter();
			var elbow = builder.parameter();
			var shoulder = builder.parameter();
			var hand = builder.parameter();

			DataVariable<Vector> elbowVector = Variable.of(Vector.class);
			DataVariable<Vector> shoulderVector = Variable.of(Vector.class);
			DataVariable<Vector> handVector = Variable.of(Vector.class);
			DataVariable<Vector> headVector = Variable.of(Vector.class);

			var shoulderY = new VectorYTerm(shoulderVector);
			var headY = new VectorYTerm(shoulderVector);

			builder.clause(
					bodyView.call(body),
					rightElbowView.call(body, elbow, elbowVector),
					rightShoulderView.call(body, shoulder, shoulderVector),
					rightHandView.call(body, hand, handVector),
					headView.call(body, head, headVector),
					lift ? check(lessEq(sub(shoulderY, headY), constant(1))) :
							check(lessEq(sub(headY, shoulderY), constant(2)))
			);
		});
	}

	public RelationalQuery rightHandAboveHead(NodeVariable body){
		return Query.of((builder) -> {
			builder.parameter(body);
			var rightHand = Variable.of();
			var head = Variable.of();
			DataVariable<Vector> rightHandVector = Variable.of(Vector.class);
			DataVariable<Vector> headVector = Variable.of(Vector.class);

			var rightHandY = new VectorYTerm(rightHandVector);
			var headY = new VectorYTerm(headVector);
			builder.clause(
					bodyView.call(body),
					rightHandView.call(body, rightHand, rightHandVector),
					headView.call(body, head, headVector),
					check(greater(rightHandY, headY))
			);
		});
	}

	public RelationalQuery stretchedRightArm(NodeVariable body) {
		return Query.of((builder) -> {
			builder.parameter(body);
			var rightHand = Variable.of();
			var rightElbow = Variable.of();
			var rightShoulder = Variable.of();
			DataVariable<Vector> rightHandVector = Variable.of(Vector.class);
			DataVariable<Vector> rightElbowVector = Variable.of(Vector.class);
			DataVariable<Vector> rightShoulderVector = Variable.of(Vector.class);

			var rightHandY = new VectorYTerm(rightHandVector);
			var rightElbowY = new VectorYTerm(rightElbowVector);
			var rightShoulderY = new VectorYTerm(rightShoulderVector);
			builder.clause(
					bodyView.call(body),
					rightHandView.call(body, rightHand, rightHandVector),
					rightElbowView.call(body, rightElbow, rightElbowVector),
					rightShoulderView.call(body, rightShoulder, rightShoulderVector),
					check(eq(rightHandY, rightElbowY)),
					check(eq(rightElbowY, rightShoulderY))
			);
		});
	}

	public RelationalQuery rightHandMovedUp(NodeVariable body){
		return Query.of((builder) -> {
			builder.parameter(body);
			var rightHand = Variable.of();
			DataVariable<Vector> rightHandVector = Variable.of(Vector.class);
			builder.clause(
					bodyView.call(body),
					rightHandView.call(body, rightHand, rightHandVector),
					handMovedUpView.call(body, rightHand)
			);
		});
	}

	public RelationalQuery stretchedRightArmAndMovedDown(NodeVariable body){
		return Query.of((builder) -> {
			builder.parameter(body);
			builder.clause(
					rightHandMovedUp(body).call(CallPolarity.NEGATIVE, body),
					stretchedRightArm(body).call(body)
			);
		});
	}

	public RelationalQuery stretchedRightArmAndMovedUp(NodeVariable body){
		return Query.of((builder) -> {
			builder.parameter(body);
			builder.clause(
					rightHandMovedUp(body).call(body),
					stretchedRightArm(body).call(body)
			);
		});
	}
}


