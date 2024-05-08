package tools.refinery.store.monitor.caseStudies;

import tools.refinery.store.dse.modification.ModificationAdapter;
import tools.refinery.store.model.Model;
import tools.refinery.store.query.ModelQueryAdapter;

public abstract class ModelInitializer {
	protected final ModificationAdapter modificationAdapter;
	protected final ModelQueryAdapter queryEngine;

	public ModelInitializer(Model model) {
		modificationAdapter = model.getAdapter(ModificationAdapter.class);
		queryEngine = model.getAdapter(ModelQueryAdapter.class);
	}

	public abstract String getInstanceId();
}
