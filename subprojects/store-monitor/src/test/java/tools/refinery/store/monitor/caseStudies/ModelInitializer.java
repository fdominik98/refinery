package tools.refinery.store.monitor.caseStudies;

import tools.refinery.store.dse.modification.ModificationAdapter;
import tools.refinery.store.model.Model;

public abstract class ModelInitializer {
	protected final ModificationAdapter modificationAdapter;

	public ModelInitializer(Model model) {
		modificationAdapter = model.getAdapter(ModificationAdapter.class);
	}
}
