package tools.refinery.store.monitor.utils;

public class Vector {
	Vector(int x, int y){
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	public int x;
	public int y;
	public static Vector of(int x, int y) {
		return new Vector(x, y);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vector vector = (Vector) o;
		return x == vector.x && y == vector.y;
	}

	@Override
	public int hashCode() {
		return toString().hashCode() + x + y;
	}
}
