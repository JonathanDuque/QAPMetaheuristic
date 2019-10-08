package main;

import java.util.Arrays;
import java.util.Random;

public class RobustTabuSearch {

	int[][] tabuMemory; // this matrix will save the iteration number where a location change is denied
	int tabuDuration;// iterations tabu for a move
	QAPData qap;
	Random random;
	int aspiration;

	public int[] solve(int[] initSolution, int [] params, QAPData qapData) {
		// this initial block define the variable needed
		qap = qapData;
		int n = qap.getSize();
		int aspiration_factor = params[3];
		tabuDuration = aspiration_factor * n;// this 8 is a factor, is possible to change
		aspiration = aspiration_factor * n * n;
		int currentIteration = 1;
		int totalIterations = params[0];
		random = new Random(params[1]);// set the seed
		qap.initDeltas(initSolution);

		//System.out.println("Total iteraciones: " + totalIterations);
		//System.out.println("Iteraciones Tabu para un movimiento: " + tabuDuration);

		initTabuMatrix(n);		

		int[] bestNeighbor, currentSolution, bestSolution;
		currentSolution = Arrays.copyOf(initSolution, n);
		bestSolution = Arrays.copyOf(initSolution, n);
		int bestCost = qap.evalSolution(initSolution), bestNeighborCost;
		int bestFoundCounter = 0; // this counter has the value where the best was found

		// this while find the best solution during totalIterations
		while (currentIteration <= totalIterations) {

			bestNeighbor = getBestNeighbor(currentSolution, currentIteration, bestCost);
			bestNeighborCost = qap.evalSolution(bestNeighbor);
			//qap.printSolution(bestNeighbor);

			// update the best solution found if is the best of the moment
			// at the end this block help to save the best of the best
			if (bestNeighborCost < bestCost) {
				bestSolution = Arrays.copyOf(bestNeighbor, n); 
				bestCost = bestNeighborCost;
				bestFoundCounter = currentIteration;
			}

			// always update the current solution
			currentSolution = bestNeighbor;
			currentIteration++;
		}

		//System.out.println("Iteración donde se encontro el mejor: " + bestFoundCounter);
		return bestSolution;
	}

	// this function simply give the solution no tabu with the minor cost,
	// unless it satisfies the aspiration criteria.
	public int[] getBestNeighbor(int[] currentSolution, int currentIteration, int bestCost) {
		int n = qap.getSize();
		//int[] selectedSolution;
		int i_selected = Integer.MAX_VALUE, j_selected = Integer.MAX_VALUE, maxDelta = Integer.MIN_VALUE;
		int currentCost = qap.evalSolution(currentSolution);

		boolean aspired = false, alreadyAspired = false, autorized = false;

		for (int i = 0; i < n - 1; i++) {
			for (int j = i + 1; j < n; j++) {

				int delta = qap.evalMovement(currentSolution, i, j);
				int newCost = currentCost - delta;

				// check if move is tabu
				autorized = (tabuMemory[i][currentSolution[j]] < currentIteration)
						|| (tabuMemory[j][currentSolution[i]] < currentIteration);

				aspired = (tabuMemory[i][currentSolution[j]] < currentIteration - aspiration)
						|| (tabuMemory[j][currentSolution[i]] < currentIteration - aspiration) || (newCost < bestCost);

				if ((aspired && !alreadyAspired) || /* first move aspired */
						(aspired && alreadyAspired && /* many move aspired */
								(delta >= maxDelta))
						|| /* => take best one */
						(!aspired && !alreadyAspired && /* no move aspired yet */
								(delta >= maxDelta) && autorized)) {

					i_selected = i;
					j_selected = j;
					maxDelta = delta;

					if (aspired) {
						alreadyAspired = true;
					}
				}
			}
		}

		currentSolution = qap.makeSwap(currentSolution, i_selected, j_selected);
		qap.updateDeltas(currentSolution, i_selected, j_selected);

		// update tabu matrix with values of the solution selected
		// random.nextDouble() give decimal between 0 and 1
		int t1 = (int) (Math.pow(random.nextDouble(), 3) * tabuDuration);
		int t2 = (int) (Math.pow(random.nextDouble(), 3) * tabuDuration);

		// make tabu this facilities during certain iterations
		tabuMemory[i_selected][currentSolution[j_selected]] = currentIteration + t1;
		tabuMemory[j_selected][currentSolution[i_selected]] = currentIteration + t2;
		//Tools.printMatrix(tabuMemory, "Tabu Matriz");

		return currentSolution; 

	}

	// init tabu matrix with 0
	public void initTabuMatrix(int size) {
		tabuMemory = new int[size][size];

		// for (int i = 0; i < size; i++)
		// for (int j = 0; j < size; j++)
		// tabuMemory[i][j] = -(size * i + j);

		for (int[] row : tabuMemory) {
			Arrays.fill(row, 0);
		}
	}

	public boolean satisfyAspitarionCriteria(int actualCost, int bestCostFound) {
		return (actualCost < bestCostFound) ? true : false;
	}
}