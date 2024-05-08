package tools.refinery.store.monitor.caseStudies.trafficSituationDemoCaseStudy;

import tools.refinery.interpreter.rete.network.Node;
import tools.refinery.store.dse.transition.Rule;
import tools.refinery.store.dse.transition.RuleBuilder;
import tools.refinery.store.dse.transition.actions.ActionLiteral;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.actionLiterals.IncreaseIntegerActionLiteral;
import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.caseStudies.MetaModelInstance;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.literal.CallLiteral;
import tools.refinery.store.query.literal.CallPolarity;
import tools.refinery.store.query.literal.Literal;
import tools.refinery.store.query.term.NodeVariable;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;

import java.util.ArrayList;
import java.util.List;

import static tools.refinery.store.dse.transition.actions.ActionLiterals.add;
import static tools.refinery.store.dse.transition.actions.ActionLiterals.remove;

public final class TrafficSituationDemoMetaModel extends MetaModelInstance {
	@Override
	public ModelInitializer createInitializer(Model model) {
		instance = new TrafficSituationDemoInitializer(model, this);
		return instance;
	}

	private final class CarMovementVariables {
		public NodeVariable car = NodeVariable.of();
		public NodeVariable cell1 = NodeVariable.of();
		public NodeVariable cell2 = NodeVariable.of();
		public CarMovementVariables(RuleBuilder builder) {
			builder.parameters(car, cell1, cell2);
		}
	};

	public Symbol<Boolean> cellSymbol = Symbol.of("Cell", 1);
	public KeyOnlyView<Boolean> cellView = new KeyOnlyView<>(cellSymbol);
	public Symbol<Boolean> behindSymbol = Symbol.of("Behind", 2);
	public KeyOnlyView<Boolean> behindView = new KeyOnlyView<>(behindSymbol);
	public Symbol<Boolean> inFrontSymbol = Symbol.of("InFront", 2);
	public KeyOnlyView<Boolean> inFrontView = new KeyOnlyView<>(inFrontSymbol);
	public Symbol<Boolean> toLeftSymbol = Symbol.of("ToLeft", 2);
	public KeyOnlyView<Boolean> toLeftView = new KeyOnlyView<>(toLeftSymbol);
	public Symbol<Boolean> toRightSymbol = Symbol.of("ToRight", 2);
	public KeyOnlyView<Boolean> toRightView = new KeyOnlyView<>(toRightSymbol);
	public Symbol<Boolean> onCellSymbol = Symbol.of("OnCell", 2);
	public KeyOnlyView<Boolean> onCellView = new KeyOnlyView<>(onCellSymbol);
	public Symbol<Boolean> carSymbol = Symbol.of("Car", 1);
	public KeyOnlyView<Boolean> carView = new KeyOnlyView<>(carSymbol);
	public Symbol<Boolean> egoSymbol = Symbol.of("Ego", 1);
	public KeyOnlyView<Boolean> egoView = new KeyOnlyView<>(egoSymbol);
	public Symbol<Boolean> forwardLaneSymbol = Symbol.of("ForwardLane", 1);
	public Symbol<Boolean> reverseLaneSymbol = Symbol.of("ReverseLane", 1);
	public Symbol<Boolean> intermediateLaneSymbol = Symbol.of("IntermediateLane", 1);
	public KeyOnlyView<Boolean> forwardLaneView = new KeyOnlyView<>(forwardLaneSymbol);
	public KeyOnlyView<Boolean> reverseLaneView = new KeyOnlyView<>(reverseLaneSymbol);
	public KeyOnlyView<Boolean> intermediateLaneView = new KeyOnlyView<>(intermediateLaneSymbol);
	public Symbol<Boolean> intendedLaneSymbol = Symbol.of("IntendedLane", 2);
	public Symbol<Boolean> containingLaneSymbol = Symbol.of("ContainingLane", 2);
	public KeyOnlyView<Boolean> intendedLaneView = new KeyOnlyView<>(intendedLaneSymbol);
	public KeyOnlyView<Boolean> containingLaneView = new KeyOnlyView<>(containingLaneSymbol);


	public TrafficSituationDemoMetaModel(){
		super();

		addSymbol(cellSymbol);
		addSymbol(behindSymbol);
		addSymbol(inFrontSymbol);
		addSymbol(toLeftSymbol);
		addSymbol(toRightSymbol);
		addSymbol(onCellSymbol);
		addSymbol(carSymbol);
		addSymbol(egoSymbol);
		addSymbol(forwardLaneSymbol);
		addSymbol(reverseLaneSymbol);
		addSymbol(intermediateLaneSymbol);
		addSymbol(intendedLaneSymbol);
		addSymbol(containingLaneSymbol);


		RelationalQuery neighborhoodPrecondition = Query.of("neighborhoodPrecondition",
				(builder, car, cell1, cell2) ->{
					var intermediateCell = NodeVariable.of();
					builder.clause(
									forwardIntent.call(car),
									behindView.call(cell1, cell2)
							)
							.clause(
									forwardIntent.call(car),
									toRightView.call(cell1, intermediateCell),
									behindView.call(intermediateCell, cell2)
							)
							.clause(
									forwardIntent.call(car),
									toLeftView.call(cell1, intermediateCell),
									behindView.call(intermediateCell, cell2)
							).clause(
									reverseIntent.call(car),
									inFrontView.call(cell1, cell2)
							)
							.clause(
									reverseIntent.call(car),
									toRightView.call(cell1, intermediateCell),
									inFrontView.call(intermediateCell, cell2)
							)
							.clause(
									reverseIntent.call(car),
									toLeftView.call(cell1, intermediateCell),
									inFrontView.call(intermediateCell, cell2)
							).clause(
									onCellView.call(car, cell1),
									toLeftView.call(cell1, intermediateCell),
									toRightView.call(intermediateCell, cell2)
							);
				}
		);

		int carCount = TrafficSituationDemoInitializer.CAR_NUMBER;
		var moveToNeighborRule = Rule.of("MoveToNeighborRule", (builder) -> {
			List<Literal> callLiterals = new ArrayList<>();
			List<ActionLiteral> actionLiterals = new ArrayList<>();
			List<CarMovementVariables> variables = new ArrayList<>();
			for (int i = 0; i < carCount; i++) {
				CarMovementVariables variable = new CarMovementVariables(builder);
				callLiterals.add(onCellView.call(variable.car, variable.cell1));
				callLiterals.add(neighborhoodPrecondition.call(variable.car, variable.cell1, variable.cell2));

				actionLiterals.add(remove(onCellSymbol, variable.car, variable.cell1));
				actionLiterals.add(add(onCellSymbol, variable.car, variable.cell2));
				variables.add(variable);
			}

			for (int i = 0; i < carCount -1; i++) {
				for (int j = i + 1; j < carCount; j++) {
					callLiterals.add(variables.get(i).car.notEquivalent(variables.get(j).car));
					callLiterals.add(variables.get(i).cell2.notEquivalent(variables.get(j).cell2));
				}
			}
			actionLiterals.add(new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1));
			builder.clause(callLiterals).action(actionLiterals);
		});

		transformationRules.add(moveToNeighborRule);
	}

	private final RelationalQuery forwardIntent = Query.of((builder, car) -> {
		var lane = NodeVariable.of();
		builder.clause(
				forwardLaneView.call(lane),
				intendedLaneView.call(car, lane)
		);}
	);

	private final RelationalQuery reverseIntent = Query.of((builder, car) -> {
		var lane = NodeVariable.of();
		builder.clause(
				reverseLaneView.call(lane),
				intendedLaneView.call(car, lane)
		);}
	);

	private final RelationalQuery placedOnCells = Query.of((builder, car1, cell1, car2, cell2) ->
		builder.clause(
				car1.notEquivalent(car2),
				onCellView.call(car1, cell1),
				onCellView.call(car2, cell2)
		)
	);

	public RelationalQuery isDistance1(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			builder.clause(isInDirection(car1, car2, -1, -1).call(car1, car2))
					.clause(isInDirection(car1, car2, 0, -1).call(car1, car2))
					.clause(isInDirection(car1, car2, 1, -1).call(car1, car2))
					.clause(isInDirection(car1, car2, -1, 0).call(car1, car2))
					.clause(isInDirection(car1, car2, 1, 0).call(car1, car2))
					.clause(isInDirection(car1, car2, -1, 1).call(car1, car2))
					.clause(isInDirection(car1, car2, 0, 1).call(car1, car2))
					.clause(isInDirection(car1, car2, 1, 1).call(car1, car2));
		});
	}

	public RelationalQuery isEgoDistance2(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			builder.clause(
				egoView.call(car1),
				isDistance2(car1, car2).call(car1, car2));
		});
	}

	public RelationalQuery isEgoDistance1(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			builder.clause(
					egoView.call(car1),
					isDistance1(car1, car2).call(car1, car2));
		});
	}

	public RelationalQuery isDistance2(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			builder.clause(isInDirection(car1, car2, -2, -2).call(car1, car2))
					.clause(isInDirection(car1, car2, -1, -2).call(car1, car2))
					.clause(isInDirection(car1, car2, 0, -2).call(car1, car2))
					.clause(isInDirection(car1, car2, 1, -2).call(car1, car2))
					.clause(isInDirection(car1, car2, 2, -2).call(car1, car2))
					.clause(isInDirection(car1, car2, 2, -1).call(car1, car2))
					.clause(isInDirection(car1, car2, 2, 0).call(car1, car2))
					.clause(isInDirection(car1, car2, 2, 1).call(car1, car2))
					.clause(isInDirection(car1, car2, 2, 2).call(car1, car2))
					.clause(isInDirection(car1, car2, 1, 2).call(car1, car2))
					.clause(isInDirection(car1, car2, 0, 2).call(car1, car2))
					.clause(isInDirection(car1, car2, -1, 2).call(car1, car2))
					.clause(isInDirection(car1, car2, -2, 2).call(car1, car2))
					.clause(isInDirection(car1, car2, -2, 1).call(car1, car2))
					.clause(isInDirection(car1, car2, -2, 0).call(car1, car2))
					.clause(isInDirection(car1, car2, -2, -1).call(car1, car2));
		});
	}

	public RelationalQuery isInZone1(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			builder.clause(isInDirection(car1, car2, 0, 0).call(car1, car2))
					.clause(isDistance1(car1, car2).call(car1, car2));
		});
	}
	public RelationalQuery isInZone2(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			builder.clause(isInZone1(car1, car2).call(car1, car2))
					.clause(isDistance2(car1, car2).call(car1, car2));
		});
	}

	public RelationalQuery isInDirection(NodeVariable car1, NodeVariable car2, int x, int y) {
		return Query.of(builder -> {
			var cell1 = NodeVariable.of();
			var cell2 = NodeVariable.of();
			builder.parameters(car1, car2);
			List<Literal> literals = new ArrayList<>();
			literals.add(placedOnCells.call(car1, cell1, car2, cell2));

			if(x == 0 && y == 0) {
				literals.add(cell1.isEquivalent(cell2));
			}
			else {
				NodeVariable tempCell1 = cell1;
				NodeVariable tempCell2 = null;
				// For X direction
				if (x > 0) {
					for (int i = 0; i < x - 1; i++) {
						tempCell2 = NodeVariable.of();
						literals.add(toLeftView.call(tempCell1, tempCell2));
						tempCell1 = tempCell2;
					}
					tempCell2 = (y == 0) ? cell2 : NodeVariable.of();
					literals.add(toLeftView.call(tempCell1, tempCell2));
				} else if (x < 0) {
					for (int i = 0; i < -x - 1; i++) {
						tempCell2 = NodeVariable.of();
						literals.add(toRightView.call(tempCell1, tempCell2));
						tempCell1 = tempCell2;
					}
					tempCell2 = (y == 0) ? cell2 : NodeVariable.of();
					literals.add(toRightView.call(tempCell1, tempCell2));
				}

				if(x != 0){
					tempCell1 = tempCell2;
				}
				// For Y direction
				if (y > 0) {
					for (int i = 0; i < y - 1; i++) {
						tempCell2 = NodeVariable.of();
						literals.add(behindView.call(tempCell1, tempCell2));
						tempCell1 = tempCell2;
					}
					literals.add(behindView.call(tempCell1, cell2));
				} else if (y < 0) {
					for (int i = 0; i < -y - 1; i++) {
						tempCell2 = NodeVariable.of();
						literals.add(inFrontView.call(tempCell1, tempCell2));
						tempCell1 = tempCell2;
					}
					literals.add(inFrontView.call(tempCell1, cell2));
				}
			}
			builder.clause(literals);
		});
	}

	@Override
	public AutomatonInstance createAutomaton() {
		return new TrafficSituationDemoAutomaton(this);
	}

	@Override
	public String getCaseStudyId() {
		return "TRAFDEMO";
	}

}
