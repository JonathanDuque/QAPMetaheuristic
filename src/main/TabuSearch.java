package main;

import java.util.Arrays;

public class TabuSearch {
	/*
	 * the neighgordhood will be change two facilities, so in total we have the
	 * permutation of n take in 2
	 */
	int[][] tabu;
	int tabuIterations;// iterations tabu for a move
	QAPData qap;

	public int[] execute(int[] initSolution, QAPData qapData, int memoryType) {
		// this initial block define the variable needed
		qap = qapData;
		int n = qap.getSize();
		tabuIterations = n + 1;
		int totalIterations = 25 * n, iterationsCounter = 1, iterationsWithoutImproveCounter=0;
		boolean largeMemory = isLargeMemory(memoryType);

		initTabuMatrix(n);

		int[] bestNeighbor, currentSolution, bestSolution;
		currentSolution = Arrays.copyOf(initSolution, n);
		bestSolution = Arrays.copyOf(initSolution, n);
		int bestCost = qap.evalSolution(initSolution), bestNeighborCost;
		int updateSolutionCounter = 0; // this counter has the value where the best was found

		while (iterationsCounter <= totalIterations) {

			bestNeighbor = getBestNeighbor(currentSolution, iterationsCounter, bestCost, false);
			bestNeighborCost = qap.evalSolution(bestNeighbor);

			// update the best solution found if is the best of the moment
			// at the end this block help to save the best of the best
			if (bestNeighborCost < bestCost) {
				bestSolution = bestNeighbor;
				bestCost = bestNeighborCost;
				updateSolutionCounter = iterationsCounter;// save this iterations for show late
				System.out.println("Pasaron " + iterationsWithoutImproveCounter + " iteraciones sin mejora.");

				iterationsWithoutImproveCounter = 0;
			}
			
			if (largeMemory && iterationsWithoutImproveCounter>15) {
				bestNeighbor = getBestNeighbor(currentSolution, iterationsCounter, bestCost, true);
			}

			// always update the current solution
			currentSolution = bestNeighbor;

			iterationsWithoutImproveCounter++;
			iterationsCounter++;
		}

		System.out.println("Iteración donde se encontro el mejor: " + updateSolutionCounter);

		System.out.println("\nValores finales matriz tabu:");
		qap.printMatrix(tabu);

		return bestSolution;
	}

	// this function simply give the solution no tabu with the minor cost,
	// unless it satisfies the aspiration criteria.
	public int[] getBestNeighbor(int[] currentSolution, int currentIteration, int bestCost, boolean largeMemory) {
		int n = currentSolution.length;
		int[] bestNeighborFound = makeSwap(currentSolution, 0, 1), temporalSolution;
		int posX = 0, posY = 1;

		// this block init best neighbor fount with a permutation without tabu
		for (int position1 = 0; position1 < (n - 1); position1++) {
			boolean found = false;
			for (int position2 = position1 + 1; position2 < n; position2++) {
				bestNeighborFound = makeSwap(currentSolution, position1, position2);
				int facX = bestNeighborFound[position1];
				int facY = bestNeighborFound[position2];
				if (!isTabu(facX, facY, currentIteration)) {
					posX = position1;
					posY = position2;
					found = true;
					break;
				}
			}
			if (found) {
				break;
			}
		}

		int temporalCost, bestNeighborFoundCost = qap.evalSolution(bestNeighborFound);

		// looking in the neigborhood the no tabu best.
		for (int position1 = 0; position1 < (n - 1); position1++) {
			for (int position2 = position1 + 1; position2 < n; position2++) {

				temporalSolution = makeSwap(currentSolution, position1, position2);
				temporalCost = qap.evalSolution(temporalSolution);

				if (temporalCost < bestNeighborFoundCost) {
					int facX = temporalSolution[position1];
					int facY = temporalSolution[position2];

					if (!isTabu(facX, facY, currentIteration)) {
						bestNeighborFound = temporalSolution;
						bestNeighborFoundCost = temporalCost;
						posX = position1;
						posY = position2;
					} else {
						if (satisfyAspitarionCriteria(temporalCost, bestCost)) {
							System.out.println("Se uso criterio de aspiracion en la iteración: " + currentIteration
									+ ", por costo: " + temporalCost);
							bestNeighborFound = temporalSolution;
							bestNeighborFoundCost = temporalCost;
							posX = position1;
							posY = position2;
						}
					}
				}
			}
		}

		// update tabu matrix with values of the best neighbor found
		updateTabuMatrix(currentSolution, posX, posY, currentIteration);

		return bestNeighborFound;
	}

	public void updateTabuMatrix(int[] selectedSolution, int posX, int posY, int iterationsCounter) {
		// get the changed facilities
		int facX = selectedSolution[posX];
		int facY = selectedSolution[posY];
		// make tabu this facilities during certain iterations
		tabu[facX][facY] = iterationsCounter + tabuIterations;
		tabu[facY][facX] = iterationsCounter + tabuIterations;
	}

	/*
	 * this matrix will save the iteration number where a facility change is denied
	 * init tabu matrix with 0
	 */
	public void initTabuMatrix(int size) {
		tabu = new int[size][size];
		for (int[] row : tabu) {
			Arrays.fill(row, 0);
		}
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

		// MainActivity.printSolution(newPermutation);
		return newPermutation;
	}

	public boolean isTabu(int p1, int p2, int iteration) {
		return iteration > tabu[p1][p2] ? false : true;
	}

	public boolean satisfyAspitarionCriteria(int actualCost, int bestCostFound) {
		return (actualCost < bestCostFound) ? true : false;
	}

	public boolean isLargeMemory(int memoryType) {
		switch (memoryType) {
		case 1:
			return false;
		case 2:
			return true;
		default:
			return false;
		}

	}

	// copy
	public int[] execute2(int[] initSolution, QAPData qapData) {
		// this initial block define the variable needed
		qap = qapData;
		int n = qap.getSize();
		tabuIterations = n + 1;
		int totalIterations = 25 * n, iterationsCounter = 1;

		initTabuMatrix(n);

		int[] bestNeighbor, currentSolution, bestSolution;
		currentSolution = Arrays.copyOf(initSolution, n);
		bestSolution = Arrays.copyOf(initSolution, n);
		int bestCost = qap.evalSolution(initSolution), bestNeighborCost;
		int updateSolutionCounter = 0; // this counter has the value where the best was found

		while (iterationsCounter <= totalIterations) {

			bestNeighbor = getBestNeighbor(currentSolution, iterationsCounter, bestCost,false);
			bestNeighborCost = qap.evalSolution(bestNeighbor);

			// update the best solution found if is the best of the moment
			// at the end this block help to save the best of the best
			if (bestNeighborCost < bestCost) {
				bestSolution = bestNeighbor;
				bestCost = bestNeighborCost;
				updateSolutionCounter = iterationsCounter;// save this iterations for show late
			}

			// always update the current solution
			currentSolution = bestNeighbor;

			iterationsCounter++;
		}

		System.out.println("Iteración donde se encontro el mejor: " + updateSolutionCounter);

		System.out.println("\nValores finales matriz tabu:");
		qap.printMatrix(tabu);

		return bestSolution;
	}

	/*
	 * System.out.println("Vecino de Inicio: " + bestNeighborFoundCost);
	 * qap.printSolutionWithCost(bestNeighborFound, bestNeighborFoundCost +
	 * " de inicio"); String tabuIndicator; //System.out.println("\nIteracion: " +
	 * currentIteration);
	 * 
	 * if (isTabu(temporalSolution[position1], temporalSolution[position2],
	 * currentIteration)) { tabuIndicator = " *  (" + temporalSolution[position1] +
	 * "," + temporalSolution[position2] + ")"; }
	 */
}
