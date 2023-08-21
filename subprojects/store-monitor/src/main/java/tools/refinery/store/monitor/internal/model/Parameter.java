package tools.refinery.store.monitor.internal.model;

import java.util.Objects;

public class Parameter {

	public enum Type {
		STRING,
		INTEGER,
		NONE
	}

	public final Type type;

	public final String name;

	public Parameter(String name) {
		this.name = name;
		this.type = Type.NONE;
	}

	public Parameter(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public boolean isUnbound() {
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object p) {
		if (this == p) return true;
		if (!(p instanceof Parameter)) {
			return false;
		}
		Parameter param = (Parameter) p;
		return Objects.equals(name, param.name) &&
				Objects.equals(type, param.type) &&
				Objects.equals(isUnbound(), param.isUnbound());
	}

	@Override
	public int hashCode() {
		return Objects.hash("Param" + name + isUnbound() + type);
	}
}
