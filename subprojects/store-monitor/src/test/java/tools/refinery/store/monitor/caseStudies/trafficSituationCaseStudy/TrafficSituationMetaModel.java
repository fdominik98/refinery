package tools.refinery.store.monitor.caseStudies.trafficSituationCaseStudy;

import tools.refinery.store.dse.transition.Rule;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.actionLiterals.IncreaseIntegerActionLiteral;
import tools.refinery.store.monitor.caseStudies.AutomatonInstance;
import tools.refinery.store.monitor.caseStudies.MetaModelInstance;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.literal.Literal;
import tools.refinery.store.query.term.NodeVariable;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;
import java.util.ArrayList;
import java.util.List;
import static tools.refinery.store.dse.transition.actions.ActionLiterals.add;
import static tools.refinery.store.dse.transition.actions.ActionLiterals.remove;

public final class TrafficSituationMetaModel extends MetaModelInstance {
	@Override
	public ModelInitializer createInitializer(Model model) {
		instance = new TrafficSituationInitializer3(model, this);
		return instance;
	}

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
	public Symbol<Boolean> laneSymbol = Symbol.of("Lane", 1);
	public Symbol<Boolean> actorSymbol = Symbol.of("Actor", 1);
	public KeyOnlyView<Boolean> actorView = new KeyOnlyView<>(actorSymbol);
	public Symbol<Boolean> carSymbol = Symbol.of("Car", 1);
	public Symbol<Boolean> pedestrianSymbol = Symbol.of("Pedestrian", 1);

	public TrafficSituationMetaModel(){
		super();

		addSymbol(cellSymbol);
		addSymbol(behindSymbol);
		addSymbol(inFrontSymbol);
		addSymbol(toLeftSymbol);
		addSymbol(toRightSymbol);
		addSymbol(onCellSymbol);
		addSymbol(laneSymbol);
		addSymbol(actorSymbol);
		addSymbol(carSymbol);
		addSymbol(pedestrianSymbol);

		RelationalQuery neighborhoodPrecondition = Query.of("neighborhoodPrecondition",
				(builder, c1, c2) -> builder
						.clause(behindView.call(c1, c2))
						.clause(toRightView.call(c1, c2))
						.clause(toLeftView.call(c1, c2))
						.clause(inFrontView.call(c1, c2))
		);

		var moveToNeighborRule = Rule.of("MoveToNeighborRule", (builder, a1, c1, c2) -> builder
				.clause(
						actorView.call(a1),
						cellView.call(c1),
						onCellView.call(a1, c1),
						cellView.call(c2),
						neighborhoodPrecondition.call(c1, c2)
				)
				.action(
						remove(onCellSymbol, a1, c1),
						add(onCellSymbol, a1, c2),
						new IncreaseIntegerActionLiteral(clockSymbol, List.of(), 1)
				)
		);

		transformationRules.add(moveToNeighborRule);
	}

	private final RelationalQuery placedOnCells = Query.of((builder, actor1, cell1, actor2, cell2) ->
		builder.clause(
				actor1.notEquivalent(actor2),
				actorView.call(actor1),
				cellView.call(cell1),
				onCellView.call(actor1, cell1),
				actorView.call(actor2),
				cellView.call(cell2),
				onCellView.call(actor2, cell2)
		)
	);

	public RelationalQuery isInDirection(NodeVariable actor1, NodeVariable actor2, int x, int y) {
		return Query.of(builder -> {
			var cell1 = NodeVariable.of();
			var cell2 = NodeVariable.of();
			builder.parameters(actor1, actor2);
			List<Literal> literals = new ArrayList<>();
			literals.add(placedOnCells.call(actor1, cell1, actor2, cell2));

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
		return new TrafficSituationAutomaton(this);
	}

	@Override
	public String getCaseStudyId() {
		return "TRAF";
	}
}
