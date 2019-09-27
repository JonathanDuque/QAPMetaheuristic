package main;

import java.util.Arrays;
import java.util.Random;

public class MultiStartLocalSearch {

	public int[] solve(int totalIterations, int[] initSolution, QAPData qap, Constructive constructive) {
		// this initial block define the variable needed

		int n = qap.getSize();
		int[] bestSolution = Arrays.copyOf(initSolution, n), currentSolution = Arrays.copyOf(initSolution, n);
		boolean improve = false; // this flag control when the solution no improve and we are in an optimo local
		int currentIteration = 1;
		int temporalDelta, bestDelta, cost = qap.evalSolution(initSolution), bestCost;
		bestCost = cost;

		// here find the best solution from de initSolution
		while (currentIteration < totalIterations) {
			improve = false;
			bestDelta = 0;

			int i_selected = -1, j_selected = -1;
			// here evaluate all the neighboorhood
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
				currentSolution = constructive.createRandomSolution(n, currentIteration);
				qap.initDeltas(currentSolution);
				cost = qap.evalSolution(currentSolution);
			}
			currentIteration++;

		}
		
		return bestSolution;
	}
	
}
