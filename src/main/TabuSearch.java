package main;

import java.util.Arrays;

public class TabuSearch {
	
	int[][] recentMemoryTabu;	// this matrix will save the iteration number where a facility change is denied
	int[][] frecuencyMemoryTabu;// this matrix save the number of a facility  has been changed
	int tabuIterations;// iterations tabu for a move
	QAPData qap; 

	public int[] execute(int totalIterations, int[] initSolution, QAPData qapData, boolean largeMemory) {
		// this initial block define the variable needed
		qap = qapData;
		int n = qap.getSize();
		tabuIterations = (int) (n+ Math.floor(n/4)) ;
		int iterationsCounter = 1, iterationsWithoutImproveCounter = 0;

		//boolean largeMemory = isLargeMemory(memoryType);
		if (largeMemory) {
			System.out.println("Se usará memoria de largo plazo");
		} else {
			System.out.println("Se usará memoria de corto plazo");
		}

		System.out.println("Total iteraciones: " + totalIterations);
		System.out.println("Iteraciones Tabu para un movimiento: " + tabuIterations);

		initTabuMatrix(n);

		int[] bestNeighbor, currentSolution, bestSolution;
		currentSolution = Arrays.copyOf(initSolution, n);
		bestSolution = Arrays.copyOf(initSolution, n);
		int bestCost = qap.evalSolution(initSolution), bestNeighborCost;
		int updateSolutionCounter = 0; // this counter has the value where the best was found

		System.out.println("\n\n/*********** DATOS DURANTE LA EJECUCIÓN DEL ALGORITMO **********/");

		
		//this while find the best solution during totalIterations
		while (iterationsCounter <= totalIterations) {

			bestNeighbor = getBestNeighbor(currentSolution, iterationsCounter, bestCost);
			bestNeighborCost = qap.evalSolution(bestNeighbor);

			// update the best solution found if is the best of the moment
			// at the end this block help to save the best of the best
			if (bestNeighborCost < bestCost) {
				bestSolution = bestNeighbor;
				bestCost = bestNeighborCost;
				updateSolutionCounter = iterationsCounter;// save this iterations for show late
				System.out.println("Pasaron " + iterationsWithoutImproveCounter + " iteraciones sin mejora. Mejor: " + bestCost);

				iterationsWithoutImproveCounter = 0;
			}

			//only check when is large memory
			if (largeMemory && iterationsWithoutImproveCounter == totalIterations / 10) {
				bestNeighbor = getNeighborWithLowFrecuency(currentSolution, iterationsCounter);
			}

			// always update the current solution
			currentSolution = bestNeighbor;

			iterationsWithoutImproveCounter++;
			iterationsCounter++;
		}

		System.out.println("Iteración donde se encontro el mejor: " + updateSolutionCounter);

		return bestSolution;
	}

	// this function simply give the solution no tabu with the minor cost,
	// unless it satisfies the aspiration criteria.
	public int[] getBestNeighbor(int[] currentSolution, int currentIteration, int bestCost) {
		int n = qap.getSize();
		int[] bestNeighborFound = makeSwap(currentSolution, 0, 1), temporalSolution;
		int posX = 0, posY = 1;

		// this block init best neighbor  with a permutation without tabu
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
		
		/*
		 * the neighgordhood will be change two facilities, so in total we have the
		 * permutation of n take in 2
		 */
		// looking in the neigborhood the no tabu best for current solution.
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
							System.out.println("Se uso criterio de aspiración en la iteración: " + currentIteration
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

	//this function is used for the large memory
	public int[] getNeighborWithLowFrecuency(int[] currentSolution, int currentIteration) {
		int n = qap.getSize();
		int[] neighborWithLowFrecuency = Arrays.copyOf(currentSolution, n);

		int minorFrecuency = 25 * n, temporalFrecuency;
		int facX = -1, facY = -1;

		// this cicle get the minor value of frecuency and its facilities
		for (int row = 0; row < (n - 1); row++) {
			for (int col = row + 1; col < n; col++) {
				temporalFrecuency = frecuencyMemoryTabu[row][col];
				if (temporalFrecuency < minorFrecuency) {
					minorFrecuency = temporalFrecuency;
					facX = row;
					facY = col;
				}
			}
		}

		// get the position of the facilities to change
		int position1 = qap.getLocationOfFacility(neighborWithLowFrecuency, facX);
		int position2 = qap.getLocationOfFacility(neighborWithLowFrecuency, facY);
		int temp;

		// change the values
		temp = neighborWithLowFrecuency[position1];
		neighborWithLowFrecuency[position1] = neighborWithLowFrecuency[position2];
		neighborWithLowFrecuency[position2] = temp;

		// update tabu matrix with values of the best neighbor found
		updateTabuMatrix(neighborWithLowFrecuency, position1, position2, currentIteration);

		return neighborWithLowFrecuency;
	}

	public void updateTabuMatrix(int[] selectedSolution, int posX, int posY, int iterationsCounter) {
		// get the changed facilities
		int facX = selectedSolution[posX];
		int facY = selectedSolution[posY];
		// make tabu this facilities during certain iterations
		recentMemoryTabu[facX][facY] = iterationsCounter + tabuIterations;
		recentMemoryTabu[facY][facX] = iterationsCounter + tabuIterations;

		//update matrix for large memory
		frecuencyMemoryTabu[facX][facY]++;
		frecuencyMemoryTabu[facY][facX]++;
	}

	// init both tabu matrix with 0
	public void initTabuMatrix(int size) {
		recentMemoryTabu = new int[size][size];
		frecuencyMemoryTabu = new int[size][size];

		for (int[] row : recentMemoryTabu) {
			Arrays.fill(row, 0);
		}

		for (int[] row : frecuencyMemoryTabu) {
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

		return newPermutation;
	}

	public boolean isTabu(int p1, int p2, int iteration) {
		return iteration > recentMemoryTabu[p1][p2] ? false : true;
	}

	public boolean satisfyAspitarionCriteria(int actualCost, int bestCostFound) {
		return (actualCost < bestCostFound) ? true : false;
	}

	public void showMemories() {
		System.out.println("\nMatriz tabu:");
		qap.printMatrix(recentMemoryTabu);

		System.out.println("\nMatriz tabu de frecuencia:");
		qap.printMatrix(frecuencyMemoryTabu);
	}
}
