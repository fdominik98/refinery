package tools.refinery.store.monitor;

import tools.refinery.store.adapter.ModelStoreAdapter;
import tools.refinery.store.model.Model;
import java.util.Collection;
import java.util.function.Consumer;

public interface ModelMonitorStoreAdapter extends ModelStoreAdapter {
	Collection<Consumer<Model>> getActions();
}
