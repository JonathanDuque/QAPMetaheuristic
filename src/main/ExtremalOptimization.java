package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class ExtremalOptimization extends RecursiveAction {
	private static final long serialVersionUID = 3L;

	public class Delta {

		public Delta(int cost, int index, int bestMove) {
			this.cost = cost;
			this.index = index;
			this.bestMove = bestMove;
		}

		int cost, index, bestMove;
	}

	double[] pdf;
	private ThreadLocalRandom thread_local_random;
	private final int n;
	QAPData qap;
	private int[] solution, initSolution;
	private int[] params;
	private int bestCost;

	public ExtremalOptimization(QAPData qapData) {
		super();
		this.thread_local_random =  ThreadLocalRandom.current();

		this.qap = new QAPData(qapData.getDistance(), qapData.getFlow(), qapData.getBKS());
		n = qap.getSize();
	}

	// always before compute function, is necessary set the environment
	public void setEnvironment(int[] initSolution, int[] params) {
		this.params = params.clone();
		this.initSolution = initSolution.clone();
	}

	public int[] getSolution() {
		return solution;
	}

	public int getBestCost() {
		return bestCost;
	}

	@Override
	protected void compute() {
		int currentCost;
		pdf = new double[n];
		// int currentIteration = 1;
		// int totalIterations = 1000;
		int[] currentSolution = Arrays.copyOf(initSolution, n);
		int tempDelta, bestDelta;
		List<Delta> deltaList = new ArrayList<>();
		int negative_infinity = (int) Double.NEGATIVE_INFINITY;

		// System.out.println("EO");
		// Tools.printArray(currentSolution);

		// receive tau parameter
		final double tau = params[0] / 100.0; // necesario para que la division de decimal

		final int pdf_function_type = params[1];
		// System.out.println("\ntau: " + tau + " pfd type: "+ pdf_function_type);

		switch (pdf_function_type) {
		case 0:
			initPdfExp(n, tau);
			break;
		case 1:
			initPdfPow(n, tau);
			break;
		case 2:
			initPdfGamma(n, tau);
			break;
		}

		currentCost = qap.evalSolution(initSolution);
		bestCost = currentCost;
		solution = Arrays.copyOf(initSolution, n);
		qap.initDeltas(initSolution);

		int counterBest = 0;

		final long start = System.currentTimeMillis();
		long time = 0;
		while (time - start < MainActivity.getExecutionTime() && MainActivity.is_BKS_was_not_found()) {

			for (int i = 0; i < n; i++) {
				int bestMove = -1;
				bestDelta = negative_infinity;

				for (int j = 0; j < n; j++) {
					if (i == j) {
						continue;
					}
					if (j > i) {
						tempDelta = qap.evalMovement(currentSolution, i, j);
					} else {
						tempDelta = qap.evalMovement(currentSolution, j, i);
					}

					// if improve
					if (tempDelta > bestDelta) {
						bestMove = j;
						bestDelta = tempDelta;
						counterBest = 1;
					} else if (tempDelta == bestDelta && thread_local_random.nextInt(++counterBest) == 0) {
						bestMove = j;
						bestDelta = tempDelta;
					}
				}
				deltaList.add(new Delta(bestDelta, i, bestMove));
			}

			Collections.sort(deltaList, compareByCost);
			// printDeltas(deltaList);

			Delta delta = deltaList.get(pdfPick()); // pdf pick gets the index recommended

			// always update the current solution and its cost
			currentSolution = qap.makeSwap(currentSolution, delta.index, delta.bestMove);
			currentCost -= delta.cost;
			qap.updateDeltas(currentSolution, delta.index, delta.bestMove);

			// update the best solution found if is the best of the moment at the end this
			// block help to save the best of the best
			// System.out.println("best: " + bestCost);
			// System.out.println("curr: " + currentCost );
			// System.out.println("delta: " + delta.cost + "\n");

			if (currentCost < bestCost) {
				solution = Arrays.copyOf(currentSolution, n);
				bestCost = currentCost;

				// if the new solution is the bks the MainActivity should be know
				if (bestCost == qap.getBKS()) {
					MainActivity.findBKS();
				}

			}

			// currentIteration++;
			deltaList.clear();// delete delta moves
			time = System.currentTimeMillis();

		}

		// MainActivity.listCost.get(2).add(bestCost);
		// System.out.println("bestCost: " + bestCost);
		// System.out.println("solution: " + qap.evalSolution(solution) + "\n");
		// System.out.println("Fin EO");

	}

	public void initPdfExp(int n, double tau) {

		double sum = 0;
		double y = 0;
		for (int i = 0; i < n; i++) {
			y = Math.exp(-tau * (i + 1));
			// System.out.println("f(" + i + ") = " + (float) y);

			pdf[i] = y;
			sum += y;
		}
		for (int i = 0; i < n; i++) {
			pdf[i] /= sum;
		}
		// System.out.println("total " + sum + "\n");

		// for (int i = 0; i < n; i++) {
		// System.out.println("f(" + i + ") = " + pdf[i]);
		// }

	}

	public void initPdfPow(int n, double tau) {
		double sum = 0;
		double y = 0;
		for (int i = 0; i < n; i++) {
			y = Math.pow((i + 1), -tau);
			// System.out.println("f(" + i + ") = " + (float) y);
			pdf[i] = y;
			sum += y;
		}

		for (int i = 0; i < n; i++) {
			pdf[i] /= sum;
		}

		// System.out.println("total " + sum + "\n");

		// for (int i = 0; i < n; i++) {
		// System.out.println("f(" + i + ") = " + pdf[i]);
		// }
	}

	public void initPdfGamma(int n, double tau) {

		double sum = 0;
		double y = 0;
		// double k = tau;
		double theta = Math.exp(tau);
		double constk = Math.pow(theta, tau) * gamma(tau);
		for (int i = 0; i < n; i++) {
			y = Math.pow(i + 1, tau - 1) * Math.exp(-(i + 1) / theta) / constk;
			// System.out.println("f(" + i + ") = " + (float) y);
			pdf[i] = y;
			sum += y;
		}

		for (int i = 0; i < n; i++) {
			pdf[i] /= sum;
		}

		// pdf[0]-= 0.0000000000000002;

		// System.out.println("total " + sum + "\n");
		// for (int i = 0; i < n; i++) {
		// System.out.println("f(" + i + ") = " + pdf[i]);
		// }
	}

	public double gamma(double n) {
		double invn = 1.0 / n;
		double g = (2.506628274631 * Math.sqrt(invn) + 0.208885689552583 * Math.pow(invn, 1.5)
				+ 0.00870357039802431 * Math.pow(invn, 2.5) - (174.210665086855 * Math.pow(invn, 3.5)) / 25920.0
				- (715.642372407151 * Math.pow(invn, 4.5)) / 1244160.0) * Math.exp((-Math.log(invn) - 1) * n);
		return g;
	}

	public int pdfPick() {
		double p = thread_local_random.nextDouble(), fx;
		// System.out.println("p " + p);

		int index = 0;

		while ((fx = pdf[index++]) < p) {
			p -= fx;
		}

		// System.out.println("index " + index);

		return index - 1;
	}

	private void printDeltas(List<Delta> listDelta) {
		for (int i = 0; i < listDelta.size(); i++) {
			System.out.println("costo " + i + " " + listDelta.get(i).cost + " cambiar " + listDelta.get(i).index
					+ " por " + listDelta.get(i).bestMove);
		}
	}

	Comparator<Delta> compareByCost = new Comparator<Delta>() {
		@Override
		public int compare(Delta d1, Delta d2) {
			return Integer.compare(d2.cost, d1.cost); // is this way work
		}
	};

}
