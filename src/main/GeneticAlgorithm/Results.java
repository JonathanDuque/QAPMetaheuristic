package main.GeneticAlgorithm;

public class Results {
	private final Individual bestIndividual;
	private final Individual worstIndividual;
	private final int avg_value;
	//private final double standdev;

	public Results(Individual bestIndividual, Individual worstIndividual, int avg_value) {
		this.bestIndividual = bestIndividual;
		this.worstIndividual = worstIndividual;
		this.avg_value = avg_value;
		//this.standdev = standdev;
	}

	public Individual getBestIndividual() {
		return bestIndividual;
	}

	public Individual getWorstIndividual() {
		return worstIndividual;
	}

	public int getAvg_value() {
		return avg_value;
	}

}