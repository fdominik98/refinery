package tools.refinery.store.monitor.caseStudies.trafficSituationCaseStudy;

import tools.refinery.store.model.Interpretation;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.tuple.Tuple;

public final class TrafficSituationInitializer2 extends ModelInitializer {
	public final Tuple[][] grid;
	public final Tuple actor1;
	public final Tuple actor2;
	public final Tuple actor3;
	public final Interpretation<Boolean> actorInterpretation;
	public final Interpretation<Boolean> carInterpretation;
	public final Interpretation<Boolean> onCellInterpretation;
	public final Interpretation<Boolean> cellInterpretation;
	public final Interpretation<Boolean> ToRightInterpretation;
	public final Interpretation<Boolean> behindInterpretation;
	public final Interpretation<Boolean> toLeftInterpretation;
	public final Interpretation<Boolean> inFrontInterpretation;

	public TrafficSituationInitializer2(Model model, TrafficSituationMetaModel metaModel) {
		super(model);
		grid = new Tuple[4][20];

		actorInterpretation = model.getInterpretation(metaModel.actorSymbol);
		carInterpretation = model.getInterpretation(metaModel.carSymbol);
		onCellInterpretation = model.getInterpretation(metaModel.onCellSymbol);
		cellInterpretation = model.getInterpretation(metaModel.cellSymbol);
		ToRightInterpretation = model.getInterpretation(metaModel.toRightSymbol);
		behindInterpretation = model.getInterpretation(metaModel.behindSymbol);
		toLeftInterpretation = model.getInterpretation(metaModel.toLeftSymbol);
		inFrontInterpretation = model.getInterpretation(metaModel.inFrontSymbol);

		for(int x = 0; x < grid.length; x++) {
			for(int y = 0; y < grid[x].length; y++) {
				var cell = grid[x][y] = modificationAdapter.createObject();
				cellInterpretation.put(cell, true);
				if(x > 0){
					ToRightInterpretation.put(Tuple.of(cell.get(0), grid[x - 1][y].get(0)), true);
					toLeftInterpretation.put(Tuple.of(grid[x - 1][y].get(0), cell.get(0)), true);
				}
				if (y > 0) {
					inFrontInterpretation.put(Tuple.of(cell.get(0), grid[x][y - 1].get(0)), true);
					behindInterpretation.put(Tuple.of(grid[x][y - 1].get(0), cell.get(0)), true);
				}
			}
		}

		actor1 = modificationAdapter.createObject();
		actor2 = modificationAdapter.createObject();
		actor3 = modificationAdapter.createObject();

		carInterpretation.put(actor1, true);
		actorInterpretation.put(actor1, true);
		onCellInterpretation.put(Tuple.of(actor1.get(0), grid[1][0].get(0)), true);
		carInterpretation.put(actor2, true);
		actorInterpretation.put(actor2, true);
		onCellInterpretation.put(Tuple.of(actor2.get(0), grid[1][2].get(0)), true);
		carInterpretation.put(actor3, true);
		actorInterpretation.put(actor3, true);
		onCellInterpretation.put(Tuple.of(actor3.get(0), grid[3][2].get(0)), true);
	}

	@Override
	public String getInstanceId() {
		return "3Car4x20Grid";
	}
}
