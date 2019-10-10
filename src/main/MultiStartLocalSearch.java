package main;

import java.util.Arrays;
import java.util.Random;

public class MultiStartLocalSearch {

	public int[] solve(int[] initSolution, int[] params, QAPData qap, Constructive constructive) {
		// this initial block define the variable needed
		int n = qap.getSize();
		int[] bestSolution = Arrays.copyOf(initSolution, n), currentSolution = Arrays.copyOf(initSolution, n);
		boolean improve = false; // this flag control when the solution no improve and we are in an optimo local
		int currentIteration = 0;
		int temporalDelta, bestDelta, cost = qap.evalSolution(initSolution), bestCost;
		bestCost = cost;
		// int totalIterations = 1000; //params[0];
		qap.initDeltas(initSolution);
		// qap.showData();

		final long start = System.currentTimeMillis();
		long time = 0;
		// here find the best solution from the initSolution
		while (time - start < MainActivity.getExecutionTime()) { // execution during 10 milliseconds = 0.01 seconds
			improve = false;
			bestDelta = 0;

			int i_selected = -1, j_selected = -1;
			// here evaluate all the neighborhood
			for (int i = 0; i < (n - 1); i++) {
				for (int j = i + 1; j < n; j++) {
					temporalDelta = qap.evalMovement(currentSolution, i, j);

					// if improve
					if (temporalDelta > bestDelta) {
						i_selected = i;
						j_selected = j;
						bestDelta = temporalDelta;
						improve = true;
					}
				}
			}

			if (improve) {
				cost -= bestDelta;
				currentSolution = qap.makeSwap(currentSolution, i_selected, j_selected);
				if (cost < bestCost) {
					bestCost = cost;
					bestSolution = Arrays.copyOf(currentSolution, n);
				}

				// qap.printSolution(bestSolution);
				qap.updateDeltas(currentSolution, i_selected, j_selected);

			} else {
				improve = false;
				// start in a new point
				currentSolution = constructive.createRandomSolution(n, MainActivity.getSeed());
				qap.initDeltas(currentSolution);
				cost = qap.evalSolution(currentSolution);
			}
			currentIteration++;
			time = System.currentTimeMillis();

		}
		//System.out.println("MSLS : " + currentIteration);

		return bestSolution;
	}

}
