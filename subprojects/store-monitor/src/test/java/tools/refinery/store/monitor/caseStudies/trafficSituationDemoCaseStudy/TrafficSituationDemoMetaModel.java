package tools.refinery.store.monitor.caseStudies.trafficSituationDemoCaseStudy;

import tools.refinery.logic.term.DataVariable;
import tools.refinery.logic.term.Variable;
import tools.refinery.logic.term.int_.IntTerms;
import tools.refinery.logic.term.real.RealTerms;
import tools.refinery.store.dse.transition.Rule;
import tools.refinery.store.dse.transition.RuleBuilder;
import tools.refinery.store.dse.transition.actions.ActionLiteral;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.actionLiterals.IncreaseIntegerActionLiteral;
import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.caseStudies.MetaModelInstance;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.logic.dnf.Query;
import tools.refinery.logic.dnf.RelationalQuery;
import tools.refinery.logic.literal.CallPolarity;
import tools.refinery.logic.literal.Literal;
import tools.refinery.logic.term.NodeVariable;
import tools.refinery.store.monitor.utils.Vector;
import tools.refinery.store.monitor.utils.VectorTerms;
import tools.refinery.store.query.view.FunctionView;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;

import java.util.ArrayList;
import java.util.List;

import static tools.refinery.logic.literal.Literals.check;
import static tools.refinery.logic.term.bool.BoolTerms.and;
import static tools.refinery.logic.term.bool.BoolTerms.or;
import static tools.refinery.logic.term.real.RealTerms.*;
import static tools.refinery.store.dse.transition.actions.ActionLiterals.add;
import static tools.refinery.store.dse.transition.actions.ActionLiterals.remove;
import static tools.refinery.store.monitor.utils.VectorTerms.*;


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
	}

	public Symbol<Vector> cellSymbol = Symbol.of("Cell", 1, Vector.class);
	public FunctionView<Vector> cellView = new FunctionView<>(cellSymbol);
	public Symbol<Boolean> onCellSymbol = Symbol.of("OnCell", 2);
	public KeyOnlyView<Boolean> onCellView = new KeyOnlyView<>(onCellSymbol);
	public Symbol<Integer> carSymbol = Symbol.of("Car", 1, Integer.class);
	public FunctionView<Integer> carView = new FunctionView<>(carSymbol);
	public Symbol<Boolean> egoSymbol = Symbol.of("Ego", 1);
	public KeyOnlyView<Boolean> egoView = new KeyOnlyView<>(egoSymbol);
	public Symbol<Boolean> neighboringLanesSymbol = Symbol.of("NeighboringLanes", 2);
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
	public KeyOnlyView<Boolean> neighboringLanesView = new KeyOnlyView<>(neighboringLanesSymbol);

	public TrafficSituationDemoMetaModel(){
		super();

		addSymbol(cellSymbol);
		addSymbol(onCellSymbol);
		addSymbol(carSymbol);
		addSymbol(egoSymbol);
		addSymbol(forwardLaneSymbol);
		addSymbol(reverseLaneSymbol);
		addSymbol(intermediateLaneSymbol);
		addSymbol(intendedLaneSymbol);
		addSymbol(containingLaneSymbol);
		addSymbol(neighboringLanesSymbol);

		RelationalQuery noLanePassingQuery = Query.of("noLanePassingQuery",
				(builder, car, cell2) ->{
					var lane = NodeVariable.of();
					builder.clause(
							egoView.call(car),
							containingLaneView.call(cell2, lane)
					).clause(
							egoView.call(CallPolarity.NEGATIVE, car),
							intendedLaneView.call(car, lane),
							containingLaneView.call(cell2, lane)
					);
				}
		);

		RelationalQuery cellsNextToEachOther =Query.of("neighborhoodPrecondition",
				(builder, cell1, cell2) ->{
					DataVariable<Vector> cell1Vector = Variable.of(Vector.class);
					DataVariable<Vector> cell2Vector = Variable.of(Vector.class);

					builder.clause(
						cellView.call(cell1, cell1Vector),
						cellView.call(cell2, cell2Vector),
						check(VectorTerms.isInLineByX(cell1Vector, cell2Vector))
					)
					.clause(
						cellView.call(cell1, cell1Vector),
						cellView.call(cell2, cell2Vector),
						check(greater(VectorTerms.distance(cell1Vector, cell2Vector), constant(1.0)))
					);
				}
		);

		RelationalQuery neighborhoodPrecondition = Query.of("neighborhoodPrecondition",
				(builder, car, cell1, cell2) ->{
					DataVariable<Vector> cell1Vector = Variable.of(Vector.class);
					DataVariable<Vector> cell2Vector = Variable.of(Vector.class);

					builder.clause(
							cellView.call(cell1, cell1Vector),
							cellView.call(cell2, cell2Vector),
							onCellView.call(car, cell1),
							forwardIntent.call(car),
							check(
									or(
										or(
											isInDirection(cell1Vector, cell2Vector, 0, 1),
												isInDirection(cell1Vector, cell2Vector, 1, 1)
										),
											isInDirection(cell1Vector, cell2Vector, -1, 1)
									)
								)
							).clause(
								cellView.call(cell1, cell1Vector),
								cellView.call(cell2, cell2Vector),
								onCellView.call(car, cell1),
								reverseIntent.call(car),
								check(
									or(
										or(
											isInDirection(cell1Vector, cell2Vector, 0, -1),
											isInDirection(cell1Vector, cell2Vector, -1, -1)
										),
										isInDirection(cell1Vector, cell2Vector, 1, -1)
									)
								)
							).clause(
								onCellView.call(car, cell1),
								cell1.isEquivalent(cell2)
							);
				}
		);

		RelationalQuery swappingCells = Query.of("swappingCells",
				(builder) ->{
					var car1 = NodeVariable.of();
					var aCell1 = NodeVariable.of();
					var aCell2 = NodeVariable.of();
					var car2 = NodeVariable.of();
					var bCell1 = NodeVariable.of();
					var bCell2 = NodeVariable.of();
					builder.parameters(car1, aCell1, aCell2, car2, bCell1, bCell2);
					builder.clause(
								onCellView.call(car1, aCell1),
								onCellView.call(car2, bCell1),
								onCellView.call(car1, bCell2),
								onCellView.call(car2, aCell2)
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
				DataVariable<Integer> idVar = DataVariable.of(Integer.class);
				callLiterals.add(carView.call(variable.car, idVar));
				callLiterals.add(check(IntTerms.eq(idVar, IntTerms.constant(i))));
				callLiterals.add(neighborhoodPrecondition.call(variable.car, variable.cell1, variable.cell2));
				callLiterals.add(noLanePassingQuery.call(variable.car, variable.cell2));

				actionLiterals.add(remove(onCellSymbol, variable.car, variable.cell1));
				actionLiterals.add(add(onCellSymbol, variable.car, variable.cell2));
				variables.add(variable);
			}

			for (int i = 0; i < carCount -1; i++) {
				for (int j = i + 1; j < carCount; j++) {
					var aCell1 = variables.get(i).cell1;
					var aCell2 = variables.get(i).cell2;
					var bCell1 = variables.get(j).cell1;
					var bCell2 = variables.get(j).cell2;
					callLiterals.add(variables.get(i).car.notEquivalent(variables.get(j).car));
					callLiterals.add(aCell2.notEquivalent(bCell2));
					callLiterals.add(swappingCells.call(CallPolarity.NEGATIVE,
							variables.get(i).car, aCell1, aCell2,
							variables.get(j).car, bCell1, bCell2));
					//callLiterals.add(cellsNextToEachOther.call(aCell2, bCell2));
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

	private final RelationalQuery onLane = Query.of((builder, car, lane) -> {
		var cell = NodeVariable.of();
		builder.clause(
				containingLaneView.call(cell, lane),
				onCellView.call(car, cell)
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

	public RelationalQuery egoIsBehindCar(NodeVariable ego, NodeVariable car) {
		return Query.of(builder -> {
			builder.parameters(ego, car);
			builder.clause(
				egoView.call(ego),
				isInFront.call(car, ego),
				onOwnLane(ego).call(ego),
				onOwnLane(car).call(car),
				isDistanceLess(ego, car, 3).call(ego, car),
				isInLine(ego, car).call(ego, car));
		});
	}

	public RelationalQuery otherCarAppearedInFront(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			builder.clause(
				otherCarOnLane(car1, car2).call(car1, car2),
				isInFront.call(car2, car1));
		});
	}

	public RelationalQuery switchingToOppositeLaneWithTraffic(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			var lane1 = NodeVariable.of();
			var lane2 = NodeVariable.of();
			builder.clause(	intermediateLaneView.call(lane1),
							onLane.call(car1, lane1),
							neighboringLanesView.call(lane1, lane2),
							isOppositeLane.call(car1, lane2),
							onLane.call(car2, lane2)
					);
		});
	}

	public RelationalQuery switchingToSameDirectionLane(NodeVariable car1) {
		return Query.of(builder -> {
			builder.parameters(car1);
			var lane1 = NodeVariable.of();
			var lane2 = NodeVariable.of();
			builder.clause(	intermediateLaneView.call(lane1),
					onLane.call(car1, lane1),
					neighboringLanesView.call(lane1, lane2),
					isSameDirectionLane.call(car1, lane2)
			);
		});
	}

	public RelationalQuery switchingToOwnLane(NodeVariable car1, NodeVariable car2, NodeVariable car3) {
		return Query.of(builder -> {
			builder.parameters(car1);
			var lane1 = NodeVariable.of();
			var lane2 = NodeVariable.of();
			builder.clause(	intermediateLaneView.call(lane1),
					onLane.call(car1, lane1),
					neighboringLanesView.call(lane1, lane2),
					intendedLaneView.call(car1, lane2)
			);
		});
	}

	public RelationalQuery egoInFrontOfCar(NodeVariable ego, NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(ego, car1, car2);
			builder.clause(
					isDistanceGreater(ego, car2, 2.0).call(ego, car2),
					isInFront.call(ego, car1));
		});
	}

	public RelationalQuery isDistance(NodeVariable car1, NodeVariable car2, int dist) {
		return Query.of(builder -> {
			var cell1 = NodeVariable.of();
			var cell2 = NodeVariable.of();
			builder.parameters(car1, car2);
			DataVariable<Vector> cell1Vector = Variable.of(Vector.class);
			DataVariable<Vector> cell2Vector = Variable.of(Vector.class);
			var distTerm = distance(cell1Vector, cell2Vector);

			builder.clause(
				cellView.call(cell1, cell1Vector),
				cellView.call(cell2, cell2Vector),
				placedOnCells.call(car1, cell1, car2, cell2),
				check(
					and(
						greaterEq(distTerm, RealTerms.constant((double)dist)),
						less(distTerm, RealTerms.constant((double)(dist + 1)))
					)
				)
			);
		});
	}

	public RelationalQuery isDistanceGreater(NodeVariable car1, NodeVariable car2, double dist) {
		return Query.of(builder -> {
			var cell1 = NodeVariable.of();
			var cell2 = NodeVariable.of();
			builder.parameters(car1, car2);
			DataVariable<Vector> cell1Vector = Variable.of(Vector.class);
			DataVariable<Vector> cell2Vector = Variable.of(Vector.class);
			var distTerm = distance(cell1Vector, cell2Vector);

			builder.clause(
					cellView.call(cell1, cell1Vector),
					cellView.call(cell2, cell2Vector),
					placedOnCells.call(car1, cell1, car2, cell2),
					check(greater(distTerm, RealTerms.constant(dist)))
			);
		});
	}

	public RelationalQuery isDistanceLess(NodeVariable car1, NodeVariable car2, double dist) {
		return Query.of(builder -> {
			var cell1 = NodeVariable.of();
			var cell2 = NodeVariable.of();
			builder.parameters(car1, car2);
			DataVariable<Vector> cell1Vector = Variable.of(Vector.class);
			DataVariable<Vector> cell2Vector = Variable.of(Vector.class);
			var distTerm = distance(cell1Vector, cell2Vector);

			builder.clause(
					cellView.call(cell1, cell1Vector),
					cellView.call(cell2, cell2Vector),
					placedOnCells.call(car1, cell1, car2, cell2),
					check(less(distTerm, RealTerms.constant(dist)))
			);
		});
	}

	public RelationalQuery onOwnLane(NodeVariable car) {
		return Query.of(builder -> {
			builder.parameters(car);
			var lane = NodeVariable.of();
			builder.clause(intendedLaneView.call(car, lane),
					onLane.call(car, lane));
		});
	}
	public RelationalQuery onIntermediateLane(NodeVariable car) {
		return Query.of(builder -> {
			builder.parameters(car);
			var lane = NodeVariable.of();
			builder.clause(	intermediateLaneView.call(lane),
					onLane.call(car, lane));
		});
	}

	public RelationalQuery onOppositeLane(NodeVariable car) {
		return Query.of(builder -> {
			builder.parameters(car);
			var lane = NodeVariable.of();
			builder.clause(onLane.call(car, lane),
				isOppositeLane.call(car, lane));
		});
	}

	public RelationalQuery onSameDirectionLane(NodeVariable car) {
		return Query.of(builder -> {
			builder.parameters(car);
			var lane = NodeVariable.of();
			builder.clause(onLane.call(car, lane),
					isSameDirectionLane.call(car, lane));
		});
	}

	public RelationalQuery isOppositeLane = Query.of((builder, car, lane) -> {
			builder.clause(reverseIntent.call(car),
							forwardLaneView.call(lane)
					)
					.clause(
							forwardIntent.call(car),
							reverseLaneView.call(lane)
					);
	});

	private final RelationalQuery isSameDirectionLane = Query.of((builder, car, lane) -> {
		builder.clause(
						reverseIntent.call(car),
						reverseLaneView.call(lane)
				)
				.clause(
						forwardIntent.call(car),
						forwardLaneView.call(lane)
				);}
	);


	public RelationalQuery otherCarOnLane(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			var lane = NodeVariable.of();
			builder.clause(onLane.call(car1, lane),
					onLane.call(car2, lane),
					car1.notEquivalent(car2));
		});
	}

	public RelationalQuery isInFront = Query.of((builder,car1, car2)  -> {
			var cell1 = NodeVariable.of();
			var cell2 = NodeVariable.of();
			DataVariable<Vector> cell1Vector = Variable.of(Vector.class);
			DataVariable<Vector> cell2Vector = Variable.of(Vector.class);
			var cell1Y = VectorTerms.y(cell1Vector);
			var cell2Y = VectorTerms.y(cell2Vector);
			builder.clause(
						placedOnCells.call(car1, cell1, car2, cell2),
						cellView.call(cell1, cell1Vector),
						cellView.call(cell2, cell2Vector),
						forwardIntent.call(car2),
						check(IntTerms.greater(cell1Y, cell2Y))
					)
					.clause(
						placedOnCells.call(car1, cell1, car2, cell2),
						cellView.call(cell1, cell1Vector),
						cellView.call(cell2, cell2Vector),
						reverseIntent.call(car2),
						check(IntTerms.greater(cell2Y, cell1Y)));
		});

	public RelationalQuery isToLeft(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			var cell1 = NodeVariable.of();
			var cell2 = NodeVariable.of();
			DataVariable<Vector> cell1Vector = Variable.of(Vector.class);
			DataVariable<Vector> cell2Vector = Variable.of(Vector.class);
			var cell1X = VectorTerms.x(cell1Vector);
			var cell2X = VectorTerms.x(cell2Vector);
			builder.clause(
							placedOnCells.call(car1, cell1, car2, cell2),
							cellView.call(cell1, cell1Vector),
							cellView.call(cell2, cell2Vector),
							forwardIntent.call(car2),
							check(IntTerms.greater(cell2X, cell1X))
					)
					.clause(
							placedOnCells.call(car1, cell1, car2, cell2),
							cellView.call(cell1, cell1Vector),
							cellView.call(cell2, cell2Vector),
							reverseIntent.call(car2),
							check(IntTerms.greater(cell1X, cell2X)));
		});
	}

	public RelationalQuery isInLine(NodeVariable car1, NodeVariable car2) {
		return Query.of(builder -> {
			builder.parameters(car1, car2);
			var cell1 = NodeVariable.of();
			var cell2 = NodeVariable.of();
			DataVariable<Vector> cell1Vector = Variable.of(Vector.class);
			DataVariable<Vector> cell2Vector = Variable.of(Vector.class);
			var inLineTerm = VectorTerms.isInLineByX(cell1Vector, cell2Vector);
			builder.clause(
					placedOnCells.call(car1, cell1, car2, cell2),
					cellView.call(cell1, cell1Vector),
					cellView.call(cell2, cell2Vector),
					check(inLineTerm));
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
