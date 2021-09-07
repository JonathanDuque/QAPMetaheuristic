package main;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class MetaheuristicSearch extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	private ThreadLocalRandom threadLocalRandom;

	QAPData qap;
	private final int qapSize;

	private int[] bestSolution, initSolution;
	private int bestCost, initCost;

	private int[] params;
	private int iterationTime;

	public MetaheuristicSearch(QAPData qapData) {
		threadLocalRandom = ThreadLocalRandom.current();

		this.qap = new QAPData(qapData.getDistance(), qapData.getFlow(), qapData.getBKS());
		qapSize = qap.getSize();
	}

	// always before compute function, is necessary set the environment
	public void setEnvironment(int[] initSolution, int[] params, final int iterationTime) {
		this.params = params.clone();
		this.initSolution = initSolution.clone();
		this.iterationTime = iterationTime;
	}

	@Override
	protected void compute() {
		// TODO Auto-generated method stub
	}

	public int[] getInitSolution() {
		return initSolution.clone();
	}

	public int[] getBestSolution() {
		return bestSolution.clone();
	}

	public int[] getParams() {
		return params;
	}

	public int getBestCost() {
		return bestCost;
	}

	public int getInitCost() {
		return initCost;
	}

	public int getIterationTime() {
		return iterationTime;
	}

	public int getQapSize() {
		return qapSize;
	}
	
	public ThreadLocalRandom getThreadLocalRandom() {
		return threadLocalRandom;
	}

	public void setBestCost(int bestCost) {
		this.bestCost = bestCost;
	}

	public void setInitCost(int initCost) {
		this.initCost = initCost;
	}

	public void setParams(int[] params) {
		this.params = params;
	}

	public void setBestSolution(int[] bestSolution) {
		this.bestSolution = bestSolution.clone();
	}

}
