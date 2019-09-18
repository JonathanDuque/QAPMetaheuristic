package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class ExtremalOptimization {

	public class Delta {
		public Delta(int cost, int index, int change) {
			this.cost = cost;
			this.index = index;
			this.change = change;
		}

		int cost, index, change;
	}

	float[] pdf;
	Random random;

	public int[] solve(int totalIterations, int[] initSolution, QAPData qap, double tau) {
		int currentIteration = 1, n = qap.getSize(), nextSolutionCost = 0, currentCost, bestCost;
		int[] currentSolution = Arrays.copyOf(initSolution, n), bestSolution, nextSolution;
		int tempDelta, bestDelta;
		List<Delta> deltaList = new ArrayList<>();
		int negative_infinity = (int) Double.NEGATIVE_INFINITY;
		random = new Random(1);// set the seed, 1 in this case
		// TODO implement lambda functions , receive tau parameter
		initPdf(qap.getSize(), tau);

		System.out.println("Solución inicial: ");
		qap.printSolution(initSolution);
		// qap.printSolutionInReadFormat(initSolution);
		currentCost = qap.evalSolution(initSolution);
		bestCost = currentCost;
		bestSolution = initSolution;
		System.out.println("costo: " + currentCost);
		int counterBest = 0;

		while (currentIteration <= totalIterations) {
			// System.out.println("\niter: " + iterationsCounter);

			for (int f1 = 0; f1 < n; f1++) {
				int change = 0;
				bestDelta = negative_infinity;

				for (int f2 = 0; f2 < n; f2++) {
					if (f1 == f2) {
						continue;
					}
					tempDelta = qap.evalMovement(currentSolution, f1, f2);
					// System.out.println("delta: " + tempDelta);

					// if improve
					if (tempDelta > bestDelta) {
						change = f2;
						bestDelta = tempDelta;
						counterBest = 1;
					} else if (tempDelta == bestDelta && random.nextInt(++counterBest) == 0) {
						change = f2;
						bestDelta = tempDelta;
						// System.out.println(" entro");

					}
				}
				deltaList.add(new Delta(bestDelta, f1, change));
				// System.out.println(f1 + " mejor: " + bestDelta + " con " + change +"\n");
			}

			// printDeltas(deltaList);
			Collections.sort(deltaList, compareByCost);
			// printDeltas(deltaList);
			Delta delta = deltaList.get(pdfPick()); // pdf pick gets the index recommended

			nextSolution = makeSwap(currentSolution, delta.index, delta.change);
			nextSolutionCost = currentCost - delta.cost;
			// System.out.println("costo next: " + nextSolutionCost);

			// update the best solution found if is the best of the moment
			// at the end this block help to save the best of the best
			if (nextSolutionCost < bestCost) {
				bestSolution = nextSolution;
				bestCost = nextSolutionCost;
			}

			// always update the current solution and its cost
			currentSolution = nextSolution;
			currentCost = nextSolutionCost;

			currentIteration++;
			deltaList.clear();// delete delta moves

		}

		return bestSolution;
	}

	public int[] makeSwap(int[] permutation, int position1, int position2) {

		int size = permutation.length;
		int[] newPermutation = Arrays.copyOf(permutation, size); // is necessary make a copy because java pass the
																	// arrays by referencia like c
		int temp;

		// change the values
		temp = newPermutation[position1];
		newPermutation[position1] = newPermutation[position2];
		newPermutation[position2] = temp;

		return newPermutation;
	}

	public void initPdf(int size, double tau) {
		pdf = new float[size];
		float sum = 0;
		double y = 0;
		for (int i = 0; i < size; i++) {
			y = Math.exp(tau * (i + 1));
			// System.out.println("f(" + i + ") = " + (float) y);

			pdf[i] = (float) y; // cast because don't need so much decimals
			sum += y;
		}
		for (int i = 0; i < size; i++) {
			pdf[i] /= sum;
		}

		//for (int i = 0; i < size; i++) {
		//	System.out.println("f(" + i + ") = " + pdf[i]);
		//}

		// System.out.println("total " + sum);
	}

	public int pdfPick() {
		double p = random.nextDouble(), fx;
		// System.out.println("p " + p);

		int index = 0;

		while ((fx = pdf[index++]) < p) {
			p -= fx;
		}

		// System.out.println("index " + index);

		return index-1;
	}

	private void printDeltas(List<Delta> listDelta) {
		for (int i = 0; i < listDelta.size(); i++) {
			System.out.println("costo " + i + " " + listDelta.get(i).cost + " cambiar " + listDelta.get(i).index
					+ " por " + listDelta.get(i).change);
		}
	}

	Comparator<Delta> compareByCost = new Comparator<Delta>() {
		@Override
		public int compare(Delta d1, Delta d2) {
			return Integer.compare(d2.cost, d1.cost); // is this way work
		}
	};

}
