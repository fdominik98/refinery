package tools.refinery.store.monitor.caseStudies.gestureRecognitionCaseStudy;

import tools.refinery.store.dse.transition.Rule;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.actionLiterals.IncreaseIntegerActionLiteral;
import tools.refinery.store.monitor.actionLiterals.IncreaseVectorActionLiteral;
import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.caseStudies.MetaModelInstance;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.monitor.utils.VectorYTerm;
import tools.refinery.logic.dnf.Query;
import tools.refinery.logic.dnf.RelationalQuery;
import tools.refinery.logic.literal.CallPolarity;
import tools.refinery.logic.term.DataVariable;
import tools.refinery.logic.term.NodeVariable;
import tools.refinery.logic.term.Variable;
import tools.refinery.store.query.view.AnySymbolView;
import tools.refinery.store.query.view.FunctionView;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;
import java.util.List;
import static tools.refinery.store.dse.transition.actions.ActionLiterals.*;
import static tools.refinery.logic.literal.Literals.check;
import static tools.refinery.logic.term.int_.IntTerms.*;

public class GestureRecognitionMetaModel extends MetaModelInstance {
	@Override
	public ModelInitializer createInitializer(Model model) {
		instance = new GestureRecognitionInitializer1(model, this);
		return instance;
	}

	@Override
	public AutomatonInstance createAutomaton() {
		return new GestureRecognitionAutomaton(this);
	}

	@Override
	public String getCaseStudyId() {
		return "REC";
	}

	public static class Vector {
		Vector(int x, int y){
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "(" + x + ", " + y + ")";
		}

		public int x;
		public int y;
		public static Vector of(int x, int y) {
			return new Vector(x, y);
		}
	}

	public Symbol<Boolean> joinSymbol = Symbol.of("Join", 2);
	public AnySymbolView joinView = new KeyOnlyView<>(joinSymbol);
	public Symbol<Boolean> bodySymbol = Symbol.of("Body", 1);
	public AnySymbolView bodyView = new KeyOnlyView<>(bodySymbol);
	public Symbol<Vector> handSymbol = Symbol.of("hand",1, Vector.class);
	public AnySymbolView handView = new FunctionView<>(handSymbol);
	public Symbol<Vector> elbowSymbol = Symbol.of("elbow",1, Vector.class);
	public AnySymbolView elbowView = new FunctionView<>(elbowSymbol);
	public Symbol<Vector> shoulderSymbol = Symbol.of("shoulder",1, Vector.class);
	public AnySymbolView shoulderView = new FunctionView<>(shoulderSymbol);
	public Symbol<Vector> headSymbol = Symbol.of("Head",1, Vector.class);
	public AnySymbolView headView = new FunctionView<>(headSymbol);
	public Symbol<Boolean> handMovedUpSymbol = Symbol.of("HandMovedUp", 1);
	public AnySymbolView handMovedUpView = new KeyOnlyView<>(handMovedUpSymbol);

	RelationalQuery armQuery = Query.of(builder -> {
		var head = builder.parameter();
		var body = builder.parameter();
		var shoulder = builder.parameter();
		var elbow = builder.parameter();
		var hand = builder.parameter();

		DataVariable<Vector> shoulderVector = Variable.of(Vector.class);
		DataVariable<Vector> handVector = Variable.of(Vector.class);
		DataVariable<Vector> elbowVector = Variable.of(Vector.class);
		DataVariable<Vector> headVector = Variable.of(Vector.class);

		builder.clause(
				handView.call(hand, handVector),
				elbowView.call(elbow, elbowVector),
				shoulderView.call(shoulder, shoulderVector),
				headView.call(head, headVector),
				bodyView.call(body),
				joinView.call(hand, elbow),
				joinView.call(elbow, shoulder),
				joinView.call(shoulder, body),
				joinView.call(head, body)
		);
	});

	public GestureRecognitionMetaModel(){
		super();
		addSymbol(bodySymbol);
		addSymbol(handSymbol);
		addSymbol(elbowSymbol);
		addSymbol(shoulderSymbol);
		addSymbol(headSymbol);
		addSymbol(handMovedUpSymbol);
		addSymbol(joinSymbol);

		var moveHandDownRule = Rule.of("MoveHandDownRule", (builder, hand) -> {
			builder.clause(handCondition(false).call(hand))
					.action(
							new IncreaseVectorActionLiteral(handSymbol, List.of(hand), Vector.of(0, -1)),
							remove(handMovedUpSymbol, hand),
							new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
					);
		});
		var moveHandUpRule = Rule.of("MoveHandUpRule", (builder, hand) -> {
			builder.clause(handCondition(true).call(hand))
					.action(
							new IncreaseVectorActionLiteral(handSymbol, List.of(hand), Vector.of(0, 1)),
							add(handMovedUpSymbol, hand),
							new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
					);
		});
		var moveElbowDownRule = Rule.of("MoveElbowDownRule", (builder, elbow, hand) -> {
			builder.clause(elbowCondition(false).call(elbow, hand))
					.action(
							new IncreaseVectorActionLiteral(elbowSymbol, List.of(elbow), Vector.of(0, -1)),
							new IncreaseVectorActionLiteral(handSymbol, List.of(hand), Vector.of(0, -1)),
							remove(handMovedUpSymbol, hand),
							new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
					);
		});
		var moveElbowUpRule = Rule.of("MoveElbowUpRule", (builder, elbow, hand) -> {
			builder.clause(elbowCondition(true).call(elbow, hand))
					.action(
							new IncreaseVectorActionLiteral(elbowSymbol, List.of(elbow), Vector.of(0, 1)),
							new IncreaseVectorActionLiteral(handSymbol, List.of(hand), Vector.of(0, 1)),
							add(handMovedUpSymbol, hand),
							new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
					);
		});

		var moveShoulderDownRule = Rule.of("MoveShoulderDownRule", (builder, shoulder, elbow, hand) -> {
			builder.clause(shoulderCondition(false).call(shoulder, elbow, hand))
					.action(
							new IncreaseVectorActionLiteral(elbowSymbol, List.of(elbow), Vector.of(0, -1)),
							new IncreaseVectorActionLiteral(handSymbol, List.of(hand), Vector.of(0, -1)),
							new IncreaseVectorActionLiteral(shoulderSymbol,	List.of(shoulder), Vector.of(0,-1)),
							remove(handMovedUpSymbol, hand),
							new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
					);
		});
		var moveShoulderUpRule = Rule.of("MoveShoulderUpRule", (builder, shoulder, elbow, hand) -> {
			builder.clause(shoulderCondition(true).call(shoulder, elbow, hand))
					.action(
							new IncreaseVectorActionLiteral(elbowSymbol, List.of(elbow), Vector.of(0, 1)),
							new IncreaseVectorActionLiteral(handSymbol, List.of(hand), Vector.of(0, 1)),
							new IncreaseVectorActionLiteral(shoulderSymbol,
									List.of(shoulder), Vector.of(0,1)),
							add(handMovedUpSymbol, hand),
							new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
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
		return Query.of("HandCondition", (builder, hand) -> {
			DataVariable<Vector> handVector = Variable.of(Vector.class);
			DataVariable<Vector> elbowVector = Variable.of(Vector.class);

			var elbowY = new VectorYTerm(elbowVector);
			var handY = new VectorYTerm(handVector);

			var elbow = Variable.of();
			builder.clause(
					handView.call(hand, handVector),
					elbowView.call(elbow, elbowVector),
					armQuery.call(Variable.of(), Variable.of(), Variable.of(), elbow, hand),
					lift ? check(lessEq(sub(handY, elbowY), constant(2))) :
							check(lessEq(sub(elbowY, handY), constant(2)))
			);
		});
	}

	private RelationalQuery elbowCondition(boolean lift) {
		return Query.of("ElbowCondition", (builder, elbow, hand) -> {
			DataVariable<Vector> elbowVector = Variable.of(Vector.class);
			DataVariable<Vector> shoulderVector = Variable.of(Vector.class);

			var elbowY = new VectorYTerm(elbowVector);
			var shoulderY = new VectorYTerm(shoulderVector);

			var shoulder = Variable.of();
			builder.clause(
					elbowView.call(elbow, elbowVector),
					shoulderView.call(shoulder, shoulderVector),
					armQuery.call(Variable.of(), Variable.of(), shoulder, elbow, hand),
					lift ? check(lessEq(sub(elbowY, shoulderY), constant(2))) :
							check(lessEq(sub(shoulderY, elbowY), constant(2)))
			);
		});
	}

	private RelationalQuery shoulderCondition(boolean lift) {
		return Query.of("ShoulderCondition", (builder, shoulder, elbow, hand) ->  {
			var head = Variable.of();
			var body = Variable.of();

			DataVariable<Vector> shoulderVector = Variable.of(Vector.class);
			DataVariable<Vector> headVector = Variable.of(Vector.class);

			var shoulderY = new VectorYTerm(shoulderVector);
			var headY = new VectorYTerm(headVector);

			builder.clause(
					shoulderView.call(shoulder, shoulderVector),
					headView.call(head, headVector),
					armQuery.call(head, body, shoulder, elbow, hand),
					lift ? check(lessEq(sub(headY, headY), constant(1))) :
							check(lessEq(sub(headY, shoulderY), constant(2)))
			);
		});
	}

	public RelationalQuery handAboveHead(NodeVariable hand){
		return Query.of((builder) -> {
			builder.parameters(hand);
			var head = Variable.of();

			DataVariable<Vector> handVector = Variable.of(Vector.class);
			DataVariable<Vector> headVector = Variable.of(Vector.class);

			var handY = new VectorYTerm(handVector);
			var headY = new VectorYTerm(headVector);
			builder.clause(
					armQuery.call(head, Variable.of(), Variable.of(), Variable.of(), hand),
					handView.call(hand, handVector),
					headView.call(head, headVector),
					check(greater(handY, headY))
			);
		});
	}

	public RelationalQuery handBelowHead(NodeVariable hand){
		return Query.of((builder) -> {
			builder.parameters(hand);
			var head = Variable.of();

			DataVariable<Vector> handVector = Variable.of(Vector.class);
			DataVariable<Vector> headVector = Variable.of(Vector.class);

			var handY = new VectorYTerm(handVector);
			var headY = new VectorYTerm(headVector);
			builder.clause(
					handView.call(hand, handVector),
					headView.call(head, headVector),
					armQuery.call(head, Variable.of(), Variable.of(), Variable.of(), hand),
					check(less(handY, headY))
			);
		});
	}

	public RelationalQuery stretchedArm(NodeVariable hand) {
		return Query.of((builder) -> {
			builder.parameter(hand);
			var elbow = Variable.of();
			var shoulder = Variable.of();
			DataVariable<Vector> handVector = Variable.of(Vector.class);
			DataVariable<Vector> elbowVector = Variable.of(Vector.class);
			DataVariable<Vector> shoulderVector = Variable.of(Vector.class);

			var handY = new VectorYTerm(handVector);
			var elbowY = new VectorYTerm(elbowVector);
			var shoulderY = new VectorYTerm(shoulderVector);
			builder.clause(
					handView.call(hand, handVector),
					elbowView.call(elbow, elbowVector),
					shoulderView.call(shoulder, shoulderVector),
					armQuery.call(Variable.of(), Variable.of(), shoulder, elbow, hand),
					check(eq(handY, elbowY)),
					check(eq(elbowY, shoulderY))
			);
		});
	}

	public RelationalQuery handMovedUp(NodeVariable hand){
		return Query.of((builder) -> {
			builder.parameters(hand);
			builder.clause(
					armQuery.call(Variable.of(), Variable.of(), Variable.of(), Variable.of(), hand),
					handMovedUpView.call(hand)
			);
		});
	}

	public RelationalQuery handMovedDown(NodeVariable hand){
		return Query.of((builder) -> {
			builder.parameters(hand);
			builder.clause(
					armQuery.call(Variable.of(), Variable.of(), Variable.of(), Variable.of(), hand),
					handMovedUpView.call(CallPolarity.NEGATIVE, hand)
			);
		});
	}

	public RelationalQuery stretchedRightArmAndMovedDown(NodeVariable hand){
		return Query.of((builder) -> {
			builder.parameter(hand);
			builder.clause(
					handMovedUp(hand).call(CallPolarity.NEGATIVE, hand),
					stretchedArm(hand).call(hand)
			);
		});
	}
}


