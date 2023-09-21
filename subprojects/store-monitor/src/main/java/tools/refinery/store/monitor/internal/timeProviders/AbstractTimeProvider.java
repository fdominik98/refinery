package tools.refinery.store.monitor.internal.timeProviders;

import tools.refinery.store.monitor.TimeListener;
import tools.refinery.store.representation.Symbol;
import java.util.LinkedList;
import java.util.Queue;

public abstract class AbstractTimeProvider {
	public final Symbol<Integer> clockSymbol = Symbol.of("Clock", 0, Integer.class);
	protected Queue<TimeListener> listeners = new LinkedList<>();

	public void addListener(TimeListener listener){
		listeners.add(listener);
	}

	public abstract int getTime();

}
