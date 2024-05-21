package tools.refinery.store.monitor.caseStudies.trafficSituationDemoCaseStudy;

import tools.refinery.store.model.Interpretation;
import tools.refinery.store.model.Model;
import tools.refinery.store.monitor.caseStudies.ModelInitializer;
import tools.refinery.store.monitor.utils.Vector;
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
	public final Tuple car7;

	public final Tuple forwardLane1;
	public final Tuple forwardLane2;
	public final Tuple reverseLane;
	public final Tuple intermediateLane1;
	public final Tuple intermediateLane2;
	public final Interpretation<Boolean> egoInterpretation;
	public final Interpretation<Integer> carInterpretation;
	public final Interpretation<Boolean> onCellInterpretation;
	public final Interpretation<Vector> cellInterpretation;
	public final Interpretation<Boolean> forwardLaneInterpretation;
	public final Interpretation<Boolean> reverseLaneInterpretation;
	public final Interpretation<Boolean> intermadiateLaneInterpretation;
	public final Interpretation<Boolean> intendedLaneInterpretation;
	public final Interpretation<Boolean> containingLaneInterpretation;

	public static final int CAR_NUMBER = 8;

	public TrafficSituationDemoInitializer(Model model, TrafficSituationDemoMetaModel metaModel) {
		super(model);
		grid = new Tuple[8][20];

		egoInterpretation = model.getInterpretation(metaModel.egoSymbol);
		carInterpretation = model.getInterpretation(metaModel.carSymbol);
		onCellInterpretation = model.getInterpretation(metaModel.onCellSymbol);
		cellInterpretation = model.getInterpretation(metaModel.cellSymbol);
		forwardLaneInterpretation = model.getInterpretation(metaModel.forwardLaneSymbol);
		reverseLaneInterpretation = model.getInterpretation(metaModel.reverseLaneSymbol);
		intermadiateLaneInterpretation = model.getInterpretation(metaModel.intermediateLaneSymbol);
		intendedLaneInterpretation = model.getInterpretation(metaModel.intendedLaneSymbol);
		containingLaneInterpretation = model.getInterpretation(metaModel.containingLaneSymbol);

		forwardLane1 = modificationAdapter.createObject();
		forwardLane2 = modificationAdapter.createObject();
		reverseLane = modificationAdapter.createObject();
		intermediateLane1 = modificationAdapter.createObject();
		intermediateLane2 = modificationAdapter.createObject();

		forwardLaneInterpretation.put(forwardLane1, true);
		forwardLaneInterpretation.put(forwardLane2, true);
		reverseLaneInterpretation.put(reverseLane, true);
		intermadiateLaneInterpretation.put(intermediateLane1, true);
		intermadiateLaneInterpretation.put(intermediateLane2, true);

		for(int x = 0; x < grid.length; x++) {
			var containingLane = switch (x) {
                case 0, 1 -> forwardLane1;
				case 6, 7 -> forwardLane2;
                case 2 -> intermediateLane1;
				case 5 -> intermediateLane2;
                default -> reverseLane;
            };
            for(int y = 0; y < grid[x].length; y++) {
				var cell = grid[x][y] = modificationAdapter.createObject();
				cellInterpretation.put(cell, Vector.of(x, y));
				containingLaneInterpretation.put(Tuple.of(cell.get(0), containingLane.get(0)), true);
			}
		}

		ego = modificationAdapter.createObject();
		car1 = modificationAdapter.createObject();
		car2 = modificationAdapter.createObject();
		car3 = modificationAdapter.createObject();
		car4 = modificationAdapter.createObject();
		car5 = modificationAdapter.createObject();
		car6 = modificationAdapter.createObject();
		car7 = modificationAdapter.createObject();

		carInterpretation.put(car5, 0);
		carInterpretation.put(car4, 1);
		carInterpretation.put(car6, 2);
		carInterpretation.put(car1, 3);
		carInterpretation.put(car3, 4);
		carInterpretation.put(car2, 5);
		carInterpretation.put(car7, 6);

		carInterpretation.put(ego, 7);
		egoInterpretation.put(ego, true);


		onCellInterpretation.put(Tuple.of(car3.get(0), grid[1][1].get(0)), true);
		onCellInterpretation.put(Tuple.of(car2.get(0), grid[0][0].get(0)), true);
		onCellInterpretation.put(Tuple.of(car1.get(0), grid[4][19].get(0)), true);
		onCellInterpretation.put(Tuple.of(car6.get(0), grid[3][18].get(0)), true);
		onCellInterpretation.put(Tuple.of(ego.get(0), grid[3][19].get(0)), true);
		onCellInterpretation.put(Tuple.of(car4.get(0), grid[6][0].get(0)), true);
		onCellInterpretation.put(Tuple.of(car5.get(0), grid[7][2].get(0)), true);
		onCellInterpretation.put(Tuple.of(car7.get(0), grid[5][5].get(0)), true);

		intendedLaneInterpretation.put(Tuple.of(car2.get(0), forwardLane1.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car3.get(0), forwardLane1.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(ego.get(0), reverseLane.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car1.get(0), reverseLane.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car6.get(0), reverseLane.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car4.get(0), forwardLane2.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car5.get(0), forwardLane2.get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car7.get(0), reverseLane.get(0)), true);

		queryEngine.flushChanges();
	}

	@Override
	public String getInstanceId() {
		return "TrafficSituationDemoInitialScene";
	}
}
