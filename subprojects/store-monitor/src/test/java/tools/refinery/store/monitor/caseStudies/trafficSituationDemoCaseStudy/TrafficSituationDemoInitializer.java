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

	public final Tuple reverseLane1;
	public final Tuple intermediateLane1;
	public final Tuple forwardLane1;
	/*public final Tuple intermediateLane2;
	public final Tuple reverseLane2;
	public final Tuple intermediateLane3;
	public final Tuple forwardLane2;
	public final Tuple intermediateLane4;
	public final Tuple reverseLane3;*/


	public final Interpretation<Boolean> egoInterpretation;
	public final Interpretation<Integer> carInterpretation;
	public final Interpretation<Boolean> onCellInterpretation;
	public final Interpretation<Vector> cellInterpretation;
	public final Interpretation<Boolean> forwardLaneInterpretation;
	public final Interpretation<Boolean> reverseLaneInterpretation;
	public final Interpretation<Boolean> intermediateLaneInterpretation;
	public final Interpretation<Boolean> intendedLaneInterpretation;
	public final Interpretation<Boolean> containingLaneInterpretation;
	public final Interpretation<Boolean> neighboringLanesInterpretation;

	public static final int CAR_NUMBER = 4;

	public TrafficSituationDemoInitializer(Model model, TrafficSituationDemoMetaModel metaModel) {
		super(model);
		//grid = new Tuple[9][20];
		grid = new Tuple[3][20];

		reverseLane1 = modificationAdapter.createObject();
		intermediateLane1 = modificationAdapter.createObject();
		forwardLane1 = modificationAdapter.createObject();
		/*intermediateLane2 = modificationAdapter.createObject();
		reverseLane2 = modificationAdapter.createObject();
		intermediateLane3 = modificationAdapter.createObject();
		forwardLane2 = modificationAdapter.createObject();
		intermediateLane4 = modificationAdapter.createObject();
		reverseLane3 = modificationAdapter.createObject();*/

		Tuple[] lanes = {
				reverseLane1,
				intermediateLane1,
				forwardLane1,
				/*intermediateLane2,
				reverseLane2,
				intermediateLane3,
				forwardLane2,
				intermediateLane4,
				reverseLane3*/
		};

		egoInterpretation = model.getInterpretation(metaModel.egoSymbol);
		carInterpretation = model.getInterpretation(metaModel.carSymbol);
		onCellInterpretation = model.getInterpretation(metaModel.onCellSymbol);
		cellInterpretation = model.getInterpretation(metaModel.cellSymbol);
		forwardLaneInterpretation = model.getInterpretation(metaModel.forwardLaneSymbol);
		reverseLaneInterpretation = model.getInterpretation(metaModel.reverseLaneSymbol);
		intermediateLaneInterpretation = model.getInterpretation(metaModel.intermediateLaneSymbol);
		intendedLaneInterpretation = model.getInterpretation(metaModel.intendedLaneSymbol);
		containingLaneInterpretation = model.getInterpretation(metaModel.containingLaneSymbol);

		neighboringLanesInterpretation = model.getInterpretation(metaModel.neighboringLanesSymbol);

		reverseLaneInterpretation.put(reverseLane1, true);
		intermediateLaneInterpretation.put(intermediateLane1, true);
		forwardLaneInterpretation.put(forwardLane1, true);
		/*intermediateLaneInterpretation.put(intermediateLane2, true);
		reverseLaneInterpretation.put(reverseLane2, true);
		intermediateLaneInterpretation.put(intermediateLane3, true);
		forwardLaneInterpretation.put(forwardLane2, true);
		intermediateLaneInterpretation.put(intermediateLane4, true);
		reverseLaneInterpretation.put(reverseLane3, true);*/

		for(int x = 1; x < lanes.length; x++) {
			neighboringLanesInterpretation.put(Tuple.of(lanes[x].get(0), lanes[x-1].get(0)), true);
			neighboringLanesInterpretation.put(Tuple.of(lanes[x-1].get(0), lanes[x].get(0)), true);
		}


		for(int x = 0; x < grid.length; x++) {
            for(int y = 0; y < grid[x].length; y++) {
				var cell = grid[x][y] = modificationAdapter.createObject();
				cellInterpretation.put(cell, Vector.of(x, y));
				containingLaneInterpretation.put(Tuple.of(cell.get(0), lanes[x].get(0)), true);
			}
		}

		ego = modificationAdapter.createObject();
		car1 = modificationAdapter.createObject();
		car2 = modificationAdapter.createObject();
		car3 = modificationAdapter.createObject();

		carInterpretation.put(ego, 0);
		egoInterpretation.put(ego, true);

		carInterpretation.put(car1, 1);
		carInterpretation.put(car2, 2);
		carInterpretation.put(car3, 3);


		onCellInterpretation.put(Tuple.of(ego.get(0), grid[0][19].get(0)), true);
		onCellInterpretation.put(Tuple.of(car1.get(0), grid[0][17].get(0)), true);
		onCellInterpretation.put(Tuple.of(car2.get(0), grid[0][10].get(0)), true);
		onCellInterpretation.put(Tuple.of(car3.get(0), grid[2][1].get(0)), true);

		intendedLaneInterpretation.put(Tuple.of(ego.get(0), lanes[0].get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car1.get(0), lanes[0].get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car2.get(0), lanes[0].get(0)), true);
		intendedLaneInterpretation.put(Tuple.of(car3.get(0), lanes[2].get(0)), true);

		queryEngine.flushChanges();
	}

	@Override
	public String getInstanceId() {
		return "TrafficSituationDemoInitialScene";
	}
}
