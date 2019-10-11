package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class ExtremalOptimization {

	public class Delta {

		public Delta(int cost, int index, int bestMove) {
			this.cost = cost;
			this.index = index;
			this.bestMove = bestMove;
		}

		int cost, index, bestMove;
	}

	float[] pdf;
	Random random;

	public int[] solve(int[] initSolution, int[] params, QAPData qap) {
		int n = qap.getSize(), currentCost, bestCost;
		pdf = new float[n];
		// int currentIteration = 1;
		// int totalIterations = 1000;
		int[] currentSolution = Arrays.copyOf(initSolution, n), bestSolution;
		int tempDelta, bestDelta;
		List<Delta> deltaList = new ArrayList<>();
		int negative_infinity = (int) Double.NEGATIVE_INFINITY;
		random = new Random(MainActivity.getSeed());// set the seed, 1 in this case

		// receive tau parameter
		final double tau = params[0] / 1000.0; // necesario para que la division de decimal
		final int pdf_function_type = params[1];

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
		bestSolution = Arrays.copyOf(initSolution, n);
		qap.initDeltas(initSolution);

		int counterBest = 0;

		final long start = System.currentTimeMillis();
		long time = 0;
		while (time - start < MainActivity.getExecutionTime()) { // execution during 10 milliseconds = 0.01 seconds

			for (int i = 0; i < n; i++) {
				int bestMove = 0;
				bestDelta = negative_infinity;

				for (int j = 0; j < n; j++) {
					if (i == j) {
						continue;
					}
					tempDelta = qap.evalMovement(currentSolution, i, j);

					// if improve
					if (tempDelta > bestDelta) {
						bestMove = j;
						bestDelta = tempDelta;
						counterBest = 1;
					} else if (tempDelta == bestDelta && random.nextInt(++counterBest) == 0) {
						bestMove = j;
						bestDelta = tempDelta;
					}
				}
				deltaList.add(new Delta(bestDelta, i, bestMove));
			}

			Collections.sort(deltaList, compareByCost);
			Delta delta = deltaList.get(pdfPick()); // pdf pick gets the index recommended

			// always update the current solution and its cost
			currentSolution = qap.makeSwap(currentSolution, delta.index, delta.bestMove);
			currentCost = currentCost - delta.cost;
			qap.updateDeltas(currentSolution, delta.index, delta.bestMove);

			// update the best solution found if is the best of the moment at the end this
			// block help to save the best of the best
			if (currentCost < bestCost) {
				bestSolution = Arrays.copyOf(currentSolution, n);
				bestCost = currentCost;
			}

			// currentIteration++;
			deltaList.clear();// delete delta moves
			time = System.currentTimeMillis();

		}
		// System.out.println("EO : " + currentIteration);

		return bestSolution;
	}

	public void initPdfExp(int n, double tau) {
		
		float sum = 0;
		double y = 0;
		for (int i = 0; i < n; i++) {
			y = Math.exp(-tau * (i + 1));
			// System.out.println("f(" + i + ") = " + (float) y);

			pdf[i] = (float) y; // cast because don't need so much decimals
			sum += y;
		}
		for (int i = 0; i < n; i++) {
			pdf[i] /= sum;
		}

		// for (int i = 0; i < size; i++) {
		// System.out.println("f(" + i + ") = " + pdf[i]);
		// }

		// System.out.println("total " + sum);
	}

	public void initPdfPow(int n, double tau) {
		float sum = 0;
		double y = 0;
		for (int i = 0; i < n; i++) {
			y = Math.pow((i + 1), -tau);
			pdf[i] = (float) y; // cast because don't need so much decimals
			sum += y;
		}
		for (int i = 0; i < n; i++) {
			pdf[i] /= sum;
		}
	}

	public void initPdfGamma(int n, double tau) {

	}

	public int pdfPick() {
		double p = random.nextDouble(), fx;
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
