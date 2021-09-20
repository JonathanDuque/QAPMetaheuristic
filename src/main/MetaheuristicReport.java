package main;

public class MetaheuristicReport {
	final private int initCost, bestCost;
	final private  int[] initSolution,  bestSolution;
	
	public MetaheuristicReport(int initCost, int bestCost, int[] initSolution, int[] bestSolution) {
		super();
		this.initCost = initCost;
		this.bestCost = bestCost;
		this.initSolution = initSolution;
		this.bestSolution = bestSolution;
	}

	public int getInitCost() {
		return initCost;
	}

	public int getBestCost() {
		return bestCost;
	}

	public int[] getInitSolution() {
		return initSolution;
	}

	public int[] getBestSolution() {
		return bestSolution;
	}

}
