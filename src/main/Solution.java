package main;

public class Solution {
	final static int NO_ID_METAHEURISTIC = -1;

	private int[] array;
	private int cost;
	private int[] params;
	private String method;
	private int metaheuricticId;

	public Solution(int[] s, int c, int[] p, String m) {
		array = s.clone();
		params = p.clone();
		method = m;
		cost = c;
		metaheuricticId = NO_ID_METAHEURISTIC;
	}

	public int[] getArray() {
		return array.clone();
	}

	public int[] getParams() {
		return params;
	}

	public String getMethod() {
		return method;
	}

	public int getMetaheuricticId() {
		return metaheuricticId;
	}

	public void setMetaheuricticId(int metaheuricticId) {
		this.metaheuricticId = metaheuricticId;
	}

	public int getCost() {
		return cost;
	}

	// TODO define mutate solution here
}
