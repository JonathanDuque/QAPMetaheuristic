package main;

public class Params {
	int[] p;
	int score;

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
}
