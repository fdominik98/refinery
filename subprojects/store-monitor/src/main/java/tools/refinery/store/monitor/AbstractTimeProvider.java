package tools.refinery.store.monitor;

import java.util.LinkedList;
import java.util.Queue;

public abstract class AbstractTimeProvider {

	protected Queue<TimeListener> listeners = new LinkedList<>();

	public void addListener(TimeListener listener){
		listeners.add(listener);
	}

	public abstract int getTime();

}
