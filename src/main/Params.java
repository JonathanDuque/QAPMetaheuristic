package main;

public class Params {
	private int[] p;
	private int score;
	private double gain = -1;
	private double distance = -1;

	public Params(final int[] params, int score) {
		p = params.clone();
		this.score = score;
		// Tools.printArray(genes);
	}

	public Params(final int[] params, int score, double[] behavior_mh) {
		p = params.clone();
		this.score = score;
		gain = behavior_mh[0];
		distance = behavior_mh[1];
	}

	public double getDistance() {
		return distance;
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

	public double[] getBehavior() {
		double[] behavior = { gain, distance };
		return behavior;
	}

}
