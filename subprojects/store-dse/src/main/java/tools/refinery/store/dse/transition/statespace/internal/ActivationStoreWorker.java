/*
 * SPDX-FileCopyrightText: 2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.store.dse.transition.statespace.internal;

import tools.refinery.store.dse.transition.Transformation;
import tools.refinery.store.dse.transition.VersionWithObjectiveValue;
import tools.refinery.store.dse.transition.statespace.ActivationStore;

import java.util.List;
import java.util.Random;

public class ActivationStoreWorker {
	final ActivationStore store;
	final List<Transformation> transformations;

	public ActivationStoreWorker(ActivationStore store, List<Transformation> transformations) {
		this.store = store;
		this.transformations = transformations;
	}

	public int[] calculateEmptyActivationSize() {
		int[] result = new int[transformations.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = transformations.get(i).getAllActivationsAsResultSet().size();
		}
		return result;
	}


	public ActivationStore.VisitResultObject fireRandomActivation(VersionWithObjectiveValue thisVersion,
																  Random random) {
		var result = store.getRandomAndMarkAsVisited(thisVersion, random);
		if (result.successfulVisit()) {
			int selectedTransformation = result.transformation();
			int selectedActivation = result.activation();

			Transformation transformation = transformations.get(selectedTransformation);
			var tuple = transformation.getActivation(selectedActivation);

			boolean success = transformation.fireActivation(tuple);
			if (success) {
				return ActivationStore.tovisitresultobject(result, transformation);
			} else {
				return new ActivationStore.VisitResultObject(
						false,
						result.mayHaveMore(),
						transformation,
						tuple);
			}
		}
		return ActivationStore.tovisitresultobject(result, null);
	}
}
