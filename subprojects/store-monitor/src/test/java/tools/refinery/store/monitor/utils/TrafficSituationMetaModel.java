package tools.refinery.store.monitor.utils;

import tools.refinery.store.dse.ActionFactory;
import tools.refinery.store.dse.internal.TransformationRule;
import tools.refinery.store.model.Interpretation;
import tools.refinery.store.model.Model;
import tools.refinery.store.query.dnf.Query;
import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.literal.Literals;
import tools.refinery.store.query.view.AnySymbolView;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;

import java.util.ArrayList;
import java.util.List;

import static tools.refinery.store.query.term.int_.IntTerms.*;

public class TrafficSituationMetaModel {

	private int GRID_ROWS = 14;
	private int GRID_COLUMNS = 4;

	public Symbol laneSymbol = Symbol.of("Lane", 1);
	public AnySymbolView laneView = new KeyOnlyView<>(laneSymbol);
	public Symbol actorSymbol = Symbol.of("Actor", 1);
	public AnySymbolView actorView = new KeyOnlyView<>(actorSymbol);
	public Symbol carSymbol = Symbol.of("Car", 1);
	public AnySymbolView carView = new KeyOnlyView<>(carSymbol);
	public Interpretation<Boolean> carInterpretation;
	public Symbol pedestrianSymbol = Symbol.of("Pedestrian", 1);
	public AnySymbolView pedestrianView = new KeyOnlyView<>(pedestrianSymbol);
	public Symbol<Boolean> grid[][] = new Symbol[GRID_ROWS][GRID_COLUMNS];
	public AnySymbolView gridView[][] = new KeyOnlyView[GRID_ROWS][GRID_COLUMNS];
	public List<RelationalQuery> movePreconditions = new ArrayList<>(GRID_COLUMNS*GRID_ROWS);
	public List<TransformationRule> transformationRules = new ArrayList<>(GRID_COLUMNS*GRID_ROWS);

	public TrafficSituationMetaModel(){
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				grid[i][j] = Symbol.of("grid_" + i + "_" + j, 1);
				gridView[i][j] = new KeyOnlyView<>(grid[i][j]);
				transformationRules.addAll(createGridMoveTrafoRules(i, j));
				createGridMoveTrafoRules(i, j);
			}
		}
	}

	List<TransformationRule> createGridMoveTrafoRules(int i, int j) {
		List<TransformationRule> rules = new ArrayList<>(8);
		RelationalQuery precondition = Query.of("GridMovePrecondition_" + i + "_" + j,
				(builder, model) -> builder.clause(
						actorView.call(model),
						gridView[i][j].call(model)
				));
		movePreconditions.add(precondition);

		if(i < GRID_ROWS - 1){
			rules.add(createGridMoveTrafoRuleNorth(precondition, i, j));
		}
		if(i > 0) {
			rules.add(createGridMoveTrafoRuleSouth(precondition, i, j));
		}
		if(j < GRID_COLUMNS - 1){
			rules.add(createGridMoveTrafoRuleEast(precondition, i, j));
		}
		if(j > 0) {
			rules.add(createGridMoveTrafoRuleWest(precondition, i, j));
		}
		return rules;
	}


	TransformationRule createGridMoveTrafoRuleEast(RelationalQuery precondition, int i, int j) {
		ActionFactory actionFactory = (model) -> {
			var fromCellInterpretation = model.getInterpretation(grid[i][j]);
			var toCellInterpretation = model.getInterpretation(grid[i][j + 1]);
			return ((Tuple activation) -> {
				fromCellInterpretation.put(activation, false);
				toCellInterpretation.put(activation, true);
			});
		};
		return new TransformationRule("Move_" + i + "_" + j + "_east_rule", precondition, actionFactory);
	}

	TransformationRule createGridMoveTrafoRuleWest(RelationalQuery precondition, int i, int j) {
		ActionFactory actionFactory = (model) -> {
			var fromCellInterpretation = model.getInterpretation(grid[i][j]);
			var toCellInterpretation = model.getInterpretation(grid[i][j - 1]);
			return ((Tuple activation) -> {
				fromCellInterpretation.put(activation, false);
				toCellInterpretation.put(activation, true);
			});
		};
		return new TransformationRule("Move_" + i + "_" + j + "_west_rule", precondition, actionFactory);
	}

	TransformationRule createGridMoveTrafoRuleNorth(RelationalQuery precondition, int i, int j) {
		ActionFactory actionFactory = (model) -> {
			var fromCellInterpretation = model.getInterpretation(grid[i][j]);
			var toCellInterpretation = model.getInterpretation(grid[i + 1][j]);
			return ((Tuple activation) -> {
				fromCellInterpretation.put(activation, false);
				toCellInterpretation.put(activation, true);
			});
		};
		return new TransformationRule("Move_" + i + "_" + j + "_north_rule", precondition, actionFactory);
	}

	TransformationRule createGridMoveTrafoRuleSouth(RelationalQuery precondition, int i, int j) {
		ActionFactory actionFactory = (model) -> {
			var fromCellInterpretation = model.getInterpretation(grid[i][j]);
			var toCellInterpretation = model.getInterpretation(grid[i - 1][j]);
			return ((Tuple activation) -> {
				fromCellInterpretation.put(activation, false);
				toCellInterpretation.put(activation, true);
			});
		};
		return new TransformationRule("Move_" + i + "_" + j + "_south_rule", precondition, actionFactory);
	}
}
