package main;

public class Params {
	int[] p;
	int score;
	double gain;

	public Params(final int[] params, int score) {
		p = params.clone();
		this.score = score;
		// Tools.printArray(genes);
	}

	public Params(final int[] params, int score, double gain) {
		p = params.clone();
		this.score = score;
		this.gain = gain;
	}

	public int[] getParams() {
		return p.clone();
	}

	public int getFitness() {
		return score;
	}

	public double getGain() {
		return gain;
	}

}
