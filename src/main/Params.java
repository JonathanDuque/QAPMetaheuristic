package main;

public class Params {
	private int[] p;

	public Params(final int[] params) {
		p = params.clone();
	}

	public int[] getParams() {
		return p.clone();
	}
}
