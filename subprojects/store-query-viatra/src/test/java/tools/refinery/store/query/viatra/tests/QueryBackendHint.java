package tools.refinery.store.query.viatra.tests;

import org.eclipse.viatra.query.runtime.matchers.backend.QueryEvaluationHint;

/**
 * Overrides {@link QueryEvaluationHint#toString()} for pretty names in parametric test names.
 */
class QueryBackendHint extends QueryEvaluationHint {
	public QueryBackendHint(BackendRequirement backendRequirementType) {
		super(null, backendRequirementType);
	}

	@Override
	public String toString() {
		return switch (getQueryBackendRequirementType()) {
			case UNSPECIFIED -> "default";
			case DEFAULT_CACHING -> "incremental";
			case DEFAULT_SEARCH -> "localSearch";
			default -> throw new IllegalStateException("Unknown BackendRequirement");
		};
	}
}
