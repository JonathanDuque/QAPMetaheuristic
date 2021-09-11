package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExtremalOptimization extends MetaheuristicSearch {
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

	public ExtremalOptimization(QAPData qapData) {
		super(qapData);
	}

	@Override
	protected void compute() {
		final int qap_size = getQapSize();
		
		int negative_infinity = (int) Double.NEGATIVE_INFINITY;
		int tempDelta, bestDelta, currentCost;
		List<Delta> deltaList = new ArrayList<>();

		// receive tau parameter
		final double tau = getParams()[0] / 100.0; // necessary due to is important that division get a decimal number
		final int pdf_function_type = getParams()[1];
		pdf = new double[qap_size];
		
		switch (pdf_function_type) {
		case 0:
			initPdfExp(qap_size, tau);
			break;
		case 1:
			initPdfPow(qap_size, tau);
			break;
		case 2:
			initPdfGamma(qap_size, tau);
			break;
		}

		int[] currentSolution = Arrays.copyOf(getInitSolution(), qap_size);
		setInitCost(qap.evaluateSolution(getInitSolution()));
		currentCost = getInitCost();
		setBestCost(getInitCost());
		setBestSolution(Arrays.copyOf(getInitSolution(), qap_size));
		qap.initDeltas(getInitSolution());

		int counterBest = 0;

		final long start = System.currentTimeMillis();
		long time = 0;
		while (time - start < getIterationTime() && MainActivity.is_BKS_was_not_found()) {

			for (int i = 0; i < qap_size; i++) {
				int bestMove = -1;
				bestDelta = negative_infinity;

				for (int j = 0; j < qap_size; j++) {
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
					} else if (tempDelta == bestDelta && getThreadLocalRandom().nextInt(++counterBest) == 0) {
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
			if (currentCost < getBestCost()) {
				setBestCost(currentCost);
				setBestSolution(Arrays.copyOf(currentSolution, qap_size));

				// if the new solution is the bks the MainActivity should be know
				if (getBestCost() == qap.getBKS()) {
					MainActivity.findBKS();
				}

			}

			deltaList.clear();// delete delta moves
			time = System.currentTimeMillis();

		}
	}

	public void initPdfExp(int qap_size, double tau) {

		double sum = 0;
		double y = 0;
		for (int i = 0; i < qap_size; i++) {
			y = Math.exp(-tau * (i + 1));

			pdf[i] = y;
			sum += y;
		}
		for (int i = 0; i < qap_size; i++) {
			pdf[i] /= sum;
		}

	}

	public void initPdfPow(int qap_size, double tau) {
		double sum = 0;
		double y = 0;
		for (int i = 0; i < qap_size; i++) {
			y = Math.pow((i + 1), -tau);
			pdf[i] = y;
			sum += y;
		}

		for (int i = 0; i < qap_size; i++) {
			pdf[i] /= sum;
		}
	}

	public void initPdfGamma(int qap_size, double tau) {
		double sum = 0;
		double y = 0;
		// double k = tau;
		double theta = Math.exp(tau);
		double constk = Math.pow(theta, tau) * gamma(tau);
		for (int i = 0; i < qap_size; i++) {
			y = Math.pow(i + 1, tau - 1) * Math.exp(-(i + 1) / theta) / constk;
			pdf[i] = y;
			sum += y;
		}

		for (int i = 0; i < qap_size; i++) {
			pdf[i] /= sum;
		}

	}

	public double gamma(double qap_size) {
		double invn = 1.0 / qap_size;
		double g = (2.506628274631 * Math.sqrt(invn) + 0.208885689552583 * Math.pow(invn, 1.5)
				+ 0.00870357039802431 * Math.pow(invn, 2.5) - (174.210665086855 * Math.pow(invn, 3.5)) / 25920.0
				- (715.642372407151 * Math.pow(invn, 4.5)) / 1244160.0) * Math.exp((-Math.log(invn) - 1) * qap_size);
		return g;
	}

	public int pdfPick() {
		double p = getThreadLocalRandom().nextDouble(), fx;

		int index = 0;

		while ((fx = pdf[index++]) < p) {
			p -= fx;
		}

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
