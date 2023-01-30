package tools.refinery.store.query.viatra;

import org.eclipse.viatra.query.runtime.api.ViatraQueryEngineOptions;
import tools.refinery.store.query.ModelQueryStoreAdapter;

public interface ViatraModelQueryStoreAdapter extends ModelQueryStoreAdapter {
	ViatraQueryEngineOptions getEngineOptions();
}