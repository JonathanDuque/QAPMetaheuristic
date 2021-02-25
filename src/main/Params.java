package main;

public class Params {
	private int[] p;
	private int score;
	private double gain;

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

	public void setParams(int[] p) {
		this.p = p.clone();
	}

	public void setGain(double gain) {
		this.gain = gain;
	}
	

}
