package tools.refinery.store.monitor.utils;

import tools.refinery.store.dse.DesignSpaceExplorationAdapter;
import tools.refinery.store.model.Model;
import tools.refinery.store.tuple.Tuple;

public class SituationInitializer {

	public Tuple[][] grid;

	public SituationInitializer(Model model, TrafficSituationMetaModel metaModel, int X, int Y) {
		grid = new Tuple[X][Y];

		var dseAdapter = model.getAdapter(DesignSpaceExplorationAdapter.class);

		var actorInterpretation = model.getInterpretation(metaModel.actorSymbol);
		var carInterpretation = model.getInterpretation(metaModel.carSymbol);
		var onCellInterpretation = model.getInterpretation(metaModel.onCellSymbol);
		var cellInterpretation = model.getInterpretation(metaModel.cellSymbol);
		var eastOfInterpretation = model.getInterpretation(metaModel.eastOfSymbol);
		var southOfInterpretation = model.getInterpretation(metaModel.southOfSymbol);
		var westOfInterpretation = model.getInterpretation(metaModel.westOfSymbol);
		var northOfInterpretation = model.getInterpretation(metaModel.northOfSymbol);

		for(int x = 0; x < grid.length; x++) {
			for(int y = 0; y < grid[x].length; y++) {
				var cell = grid[x][y] = dseAdapter.createObject();
				cellInterpretation.put(cell, true);
				if(x > 0){
					eastOfInterpretation.put(Tuple.of(cell.get(0), grid[x - 1][y].get(0)), true);
					westOfInterpretation.put(Tuple.of(grid[x - 1][y].get(0), cell.get(0)), true);
				}
				if (y > 0) {
					northOfInterpretation.put(Tuple.of(cell.get(0), grid[x][y - 1].get(0)), true);
					southOfInterpretation.put(Tuple.of(grid[x][y - 1].get(0), cell.get(0)), true);
				}
			}
		}

		var actor1 = dseAdapter.createObject();
		var actor2 = dseAdapter.createObject();

		carInterpretation.put(actor1, true);
		actorInterpretation.put(actor1, true);
		onCellInterpretation.put(Tuple.of(actor1.get(0), grid[1][0].get(0)), true);
		carInterpretation.put(actor2, true);
		actorInterpretation.put(actor2, true);
		onCellInterpretation.put(Tuple.of(actor2.get(0), grid[2][0].get(0)), true);

	}
}
