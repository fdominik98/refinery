/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation;

import tools.refinery.evaluation.statespace.EvaluationStore;
import tools.refinery.store.adapter.ModelAdapter;
import tools.refinery.evaluation.internal.ModelEvaluationBuilderImpl;
import tools.refinery.store.representation.Symbol;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface ModelEvaluationAdapter extends ModelAdapter {
	record EvaluationResult (int allVersionNumber, double trajectoryLength, long timeSpan, double diversity){}

	class EvaluationResultContainer extends ArrayList<EvaluationResult> {
		private EvaluationResult calculateMedian(Comparator comp){
			sort(comp);
			var res1 = get(size() / 2);
			if(size() % 2 == 0) {
				var res2 = get(size() / 2 - 1);
				return new EvaluationResult(
						(res1.allVersionNumber + res2.allVersionNumber) / 2,
						(res1.trajectoryLength + res2.trajectoryLength) / 2,
						(res1.timeSpan + res2.timeSpan) / 2,
						(res1.diversity + res2.diversity) / 2
				);
			}
			else {
				return new EvaluationResult(
						res1.allVersionNumber,
						res1.trajectoryLength,
						res1.timeSpan,
						res1.diversity);
			}
		}


		public void toFile(String caseStudyId, String instanceId, int solutions, boolean guided) {
			String guidedString = guided ? "guided" : "random";
			Path timeSpansPath = Paths.get("eval"+ caseStudyId, instanceId, guidedString, "timeSpan",
					solutions + ".txt");
			Path distancesPath = Paths.get("eval"+ caseStudyId, instanceId, guidedString, "distance",
					solutions + ".txt");
			Path infoPath = Paths.get("eval"+ caseStudyId, instanceId, guidedString, "info",
					solutions + ".txt");

			// Ensure that the directory exists
			try {
				Files.createDirectories(timeSpansPath.getParent());
				Files.createDirectories(distancesPath.getParent());
				Files.createDirectories(infoPath.getParent());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			try (PrintWriter writer = new PrintWriter(new FileWriter(timeSpansPath.toFile()))) {
				for (var result : this) {
					writer.println(result.timeSpan);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try (PrintWriter writer = new PrintWriter(new FileWriter(distancesPath.toFile()))) {
				for (var result : this) {
					writer.println(result.diversity);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try (PrintWriter writer = new PrintWriter(new FileWriter(infoPath.toFile()))) {
				writer.println("Median all version number: " + medianByAllVersionNumber().allVersionNumber);
				writer.println("Median trajectory length: " + medianByTrajectoryLength().trajectoryLength);
				writer.println("Median timespan: " + medianByTimeSpan().timeSpan);
				writer.println("Median diversity: " + medianByDiversity().diversity);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public EvaluationResult medianByTimeSpan() {
			return calculateMedian(Comparator.comparingLong(EvaluationResult::timeSpan));
		}

		public EvaluationResult medianByDiversity() {
			return calculateMedian(Comparator.comparingDouble(EvaluationResult::diversity));
		}
		public EvaluationResult medianByAllVersionNumber() {
			return calculateMedian(Comparator.comparingDouble(EvaluationResult::allVersionNumber));
		}
		public EvaluationResult medianByTrajectoryLength() {
			return calculateMedian(Comparator.comparingDouble(EvaluationResult::trajectoryLength));
		}
	}

	ModelEvaluationStoreAdapter getStoreAdapter();

	static ModelEvaluationBuilder builder() {
		return new ModelEvaluationBuilderImpl();
	}

	void start();
	void stop(EvaluationStore evaluationStore);
	void pause();
	void resume();

	EvaluationResult getEvaluationResult(EvaluationStore evaluationStore, List<Symbol<?>> symbols);
}
