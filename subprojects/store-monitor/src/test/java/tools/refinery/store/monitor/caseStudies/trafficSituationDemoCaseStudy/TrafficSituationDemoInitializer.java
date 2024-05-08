package tools.refinery.store.monitor.caseStudies.trafficSituationDemoCaseStudy;

import tools.refinery.store.model.Interpretation;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.tuple.Tuple;

public final class TrafficSituationDemoInitializer extends ModelInitializer {
	public final Tuple[][] grid;
	public final Tuple ego;
	public final Tuple car1;
	public final Tuple car2;

	public final Tuple car3;
	public final Tuple car4;
	public final Tuple car5;
	public final Tuple car6;

	public final Tuple forwardLane;
	public final Tuple reverseLane;
	public final Tuple intermediateLane;
	public final Interpretation<Boolean> egoInterpretation;
	public final Interpretation<Boolean> carInterpretation;
	public final Interpretation<Boolean> onCellInterpretation;
	public final Interpretation<Boolean> cellInterpretation;
	public final Interpretation<Boolean> ToRightInterpretation;
	public final Interpretation<Boolean> behindInterpretation;
	public final Interpretation<Boolean> toLeftInterpretation;
	public final Interpretation<Boolean> inFrontInterpretation;
	public final Interpretation<Boolean> forwardLaneInterpretation;
	public final Interpretation<Boolean> reverseLaneInterpretation;
	public final Interpretation<Boolean> intermadiateLaneInterpretation;
	public final Interpretation<Boolean> intendedLaneInterpretation;
	public final Interpretation<Boolean> containingLaneInterpretation;

	public static final int CAR_NUMBER = 7;

	public TrafficSituationDemoInitializer(Model model, TrafficSituationDemoMetaModel metaModel) {
		super(model);
		grid = new Tuple[8][20];

		egoInterpretation = model.getInterpretation(metaModel.egoSymbol);
		carInterpretation = model.getInterpretation(metaModel.carSymbol);
		onCellInterpretation = model.getInterpretation(metaModel.onCellSymbol);
		cellInterpretation = model.getInterpretation(metaModel.cellSymbol);
		ToRightInterpretation = model.getInterpretation(metaModel.toRightSymbol);
		behindInterpretation = model.getInterpretation(metaModel.behindSymbol);
		toLeftInterpretation = model.getInterpretation(metaModel.toLeftSymbol);
		inFrontInterpretation = model.getInterpretation(metaModel.inFrontSymbol);
		forwardLaneInterpretation = model.getInterpretation(metaModel.forwardLaneSymbol);
		reverseLaneInterpretation = model.getInterpretation(metaModel.reverseLaneSymbol);
		intermadiateLaneInterpretation = model.getInterpretation(metaModel.intermediateLaneSymbol);
		intendedLaneInterpretation = model.getInterpretation(metaModel.intendedLaneSymbol);
		containingLaneInterpretation = model.getInterpretation(metaModel.containingLaneSymbol);

		forwardLane = modificationAdapter.createObject();
		reverseLane = modificationAdapter.createObject();
		intermediateLane = modificationAdapter.createObject();

		forwardLaneInterpretation.put(forwardLane, true);
		reverseLaneInterpretation.put(reverseLane, true);
		intermadiateLaneInterpretation.put(intermediateLane, true);

		for(int x = 0; x < grid.length; x++) {
			var containingLane = switch (x) {
                case 0, 1, 6, 7 -> forwardLane;
                case 2, 5 -> intermediateLane;
                default -> reverseLane;
            };
            for(int y = 0; y < grid[x].length; y++) {
				queryEngine.flushChanges();
				var cell = grid[x][y] = modificationAdapter.createObject();
				cellInterpretation.put(cell, true);
				containingLaneInterpretation.put(Tuple.of(cell.get(0), containingLane.get(0)), true);
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

		ego = modificationAdapter.createObject();
		car1 = modificationAdapter.createObject();
		car2 = modificationAdapter.createObject();
		car3 = modificationAdapter.createObject();
		car4 = modificationAdapter.createObject();
		car5 = modificationAdapter.createObject();
		car6 = modificationAdapter.createObject();


		onCellInterpretation.put(Tuple.of(car3.get(0), grid[1][1].get(0)), true);
		onCellInterpretation.put(Tuple.of(car2.get(0), grid[0][0].get(0)), true);
		onCellInterpretation.put(Tuple.of(car1.get(0), grid[4][19].get(0)), true);
		onCellInterpretation.put(Tuple.of(car6.get(0), grid[3][14].get(0)), true);
		onCellInterpretation.put(Tuple.of(ego.get(0), grid[3][19].get(0)), true);
		onCellInterpretation.put(Tuple.of(car4.get(0), grid[6][0].get(0)), true);
		onCellInterpretation.put(Tuple.of(car5.get(0), grid[7][2].get(0)), true);

		queryEngine.flushChanges();

		intendedLaneInterpretation.put(Tuple.of(car2.get(0), forwardLane.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car3.get(0), forwardLane.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(ego.get(0), reverseLane.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car1.get(0), reverseLane.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car6.get(0), reverseLane.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car4.get(0), forwardLane.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car5.get(0), forwardLane.get(0)), true);

		queryEngine.flushChanges();

		carInterpretation.put(car5, true);
		carInterpretation.put(car4, true);
		carInterpretation.put(car6, true);
		carInterpretation.put(car1, true);
		carInterpretation.put(car3, true);
		carInterpretation.put(car2, true);

		carInterpretation.put(ego, true);
		egoInterpretation.put(ego, true);

		queryEngine.flushChanges();
	}

	@Override
	public String getInstanceId() {
		return "TrafficSituationDemoInitialScene";
	}
}
