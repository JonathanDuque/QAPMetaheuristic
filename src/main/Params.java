package main;

public class Params {
	private int[] p;
	private int score;

	public Params(final int[] params, int score) {
		p = params.clone();
		this.score = score;
		// Tools.printArray(genes);
	}

	public int[] getParams() {
		return p.clone();
	}

	public int getFitness() {
		return score;
	}

	public void setParams(int[] p) {
		this.p = p.clone();
	}
}
