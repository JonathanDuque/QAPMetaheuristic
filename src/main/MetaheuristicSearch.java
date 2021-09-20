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
	private final int metaheuristicId;
	private int iterationTime;

	public MetaheuristicSearch(QAPData qapData, int metahueristicId) {
		threadLocalRandom = ThreadLocalRandom.current();

		this.qap = new QAPData(qapData.getDistance(), qapData.getFlow(), qapData.getBKS());
		this.metaheuristicId = metahueristicId;
		qapSize = qap.getSize();
	}

	// always before compute function, is necessary set the environment
	public void setEnvironment(int[] initSolution, int[] params, final int iterationTime) {
		this.params = params.clone();
		this.initSolution = initSolution.clone();
		initCost = qap.evaluateSolution(this.initSolution);
		this.iterationTime = iterationTime;
	}

	@Override
	protected void compute() {
		// TODO Auto-generated method stub
	}

	public int getMetaheuristicId() {
		return metaheuristicId;
	}

	public int[] getInitSolution() {
		return initSolution.clone();
	}

	public Solution getBestSolution(String metaheuristicName) {
		return new Solution(bestSolution.clone(), bestCost, params, metaheuristicName);
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

	public MetaheuricticReport getMetaheuricticReport() {
		return new MetaheuricticReport(initCost, bestCost, initSolution.clone(), bestSolution.clone());
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

	public void setBestSolution(int[] bestSolution) {
		this.bestSolution = bestSolution.clone();
	}

}
