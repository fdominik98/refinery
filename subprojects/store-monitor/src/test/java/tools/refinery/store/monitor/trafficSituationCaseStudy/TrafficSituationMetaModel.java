package tools.refinery.store.monitor.trafficSituationCaseStudy;

import tools.refinery.store.dse.transition.Rule;
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

public final class TrafficSituationMetaModel {
	public Symbol<Boolean> cellSymbol = Symbol.of("Cell", 1);
	public KeyOnlyView<Boolean> cellView = new KeyOnlyView<>(cellSymbol);
	public Symbol<Boolean> southOfSymbol = Symbol.of("SouthOf", 2);
	public KeyOnlyView<Boolean> southOfView = new KeyOnlyView<>(southOfSymbol);
	public Symbol<Boolean> northOfSymbol = Symbol.of("NorthOf", 2);
	public KeyOnlyView<Boolean> northOfView = new KeyOnlyView<>(northOfSymbol);
	public Symbol<Boolean> westOfSymbol = Symbol.of("WestOf", 2);
	public KeyOnlyView<Boolean> westOfView = new KeyOnlyView<>(westOfSymbol);
	public Symbol<Boolean> eastOfSymbol = Symbol.of("EastOf", 2);
	public KeyOnlyView<Boolean> eastOfView = new KeyOnlyView<>(eastOfSymbol);
	public Symbol<Boolean> onCellSymbol = Symbol.of("OnCell", 2);
	public KeyOnlyView<Boolean> onCellView = new KeyOnlyView<>(onCellSymbol);
	public Symbol<Boolean> laneSymbol = Symbol.of("Lane", 1);
	public Symbol<Boolean> actorSymbol = Symbol.of("Actor", 1);
	public KeyOnlyView<Boolean> actorView = new KeyOnlyView<>(actorSymbol);
	public Symbol<Boolean> carSymbol = Symbol.of("Car", 1);
	public Symbol<Boolean> pedestrianSymbol = Symbol.of("Pedestrian", 1);
	public List<Symbol<Boolean>> symbols = new ArrayList<>();
	public List<Rule> transformationRules = new ArrayList<>();

	public TrafficSituationMetaModel(){
		symbols.add(cellSymbol);
		symbols.add(southOfSymbol);
		symbols.add(northOfSymbol);
		symbols.add(westOfSymbol);
		symbols.add(eastOfSymbol);
		symbols.add(onCellSymbol);
		symbols.add(laneSymbol);
		symbols.add(actorSymbol);
		symbols.add(carSymbol);
		symbols.add(pedestrianSymbol);

		RelationalQuery neighborhoodPrecondition = Query.of("neighborhoodPrecondition",
				(builder, c1, c2) -> builder
						.clause(southOfView.call(c1, c2))
						.clause(eastOfView.call(c1, c2))
						.clause(westOfView.call(c1, c2))
						.clause(northOfView.call(c1, c2))
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
						add(onCellSymbol, a1, c2)
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
						literals.add(westOfView.call(tempCell1, tempCell2));
						tempCell1 = tempCell2;
					}
					tempCell2 = (y == 0) ? cell2 : NodeVariable.of();
					literals.add(westOfView.call(tempCell1, tempCell2));
				} else if (x < 0) {
					for (int i = 0; i < -x - 1; i++) {
						tempCell2 = NodeVariable.of();
						literals.add(eastOfView.call(tempCell1, tempCell2));
						tempCell1 = tempCell2;
					}
					tempCell2 = (y == 0) ? cell2 : NodeVariable.of();
					literals.add(eastOfView.call(tempCell1, tempCell2));
				}

				if(x != 0){
					tempCell1 = tempCell2;
				}
				// For Y direction
				if (y > 0) {
					for (int i = 0; i < y - 1; i++) {
						tempCell2 = NodeVariable.of();
						literals.add(southOfView.call(tempCell1, tempCell2));
						tempCell1 = tempCell2;
					}
					literals.add(southOfView.call(tempCell1, cell2));
				} else if (y < 0) {
					for (int i = 0; i < -y - 1; i++) {
						tempCell2 = NodeVariable.of();
						literals.add(northOfView.call(tempCell1, tempCell2));
						tempCell1 = tempCell2;
					}
					literals.add(northOfView.call(tempCell1, cell2));
				}
			}
			builder.clause(literals);
		});
	}
}
