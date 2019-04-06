package main;

import java.util.Arrays;

public class TabuSearch {
	/*
	 * the neighgordhood will be change two facilities, so in total we have the
	 * permutation of n take in 2
	 */
	int[][] tabu;
	int tabuIterations;
	QAPData qap;

	public int[] execute(int[] initSolution, QAPData qapData) {
		qap = qapData;
		// this initial block define the variable needed
		int n = qap.getSize();
		tabuIterations = n + 2;
		int totalIterations = 50 * n, iterationsCounter = 1;// tabuIterations will be the number
															// of a number is tabu
		initTabuMatrix(n);
		int[] bestNeighbor, currentSolution = Arrays.copyOf(initSolution, n),
				bestSolution = Arrays.copyOf(initSolution, n);
		int bestCost = qap.evalSolution(initSolution), bestNeighborCost,
				currentSolutionCost = qap.evalSolution(initSolution);

		int updateSolutionCounter = 0; // this counter has the value where the best was found

		while (iterationsCounter <= totalIterations) {

			bestNeighbor = getBestNeighbor(currentSolution, iterationsCounter, bestCost);
			bestNeighborCost = qap.evalSolution(bestNeighbor);

			if (iterationsCounter <= 2) {
				// MainActivity.printArray(bestNeighbor);
				// MainActivity.printArray(currentSolution);
				// MainActivity.printArray(bestSolution);
				// System.out.println("Mejor vecino: " + bestNeighborCost);
				// System.out.println("Actual: " + currentSolutionCost);
				// System.out.println("LO MEJOR: " + bestCost );
				qap.printMatrix(tabu);
			}

			if (bestNeighborCost < bestCost) {
				bestSolution = bestNeighbor;
				bestCost = bestNeighborCost;
				updateSolutionCounter = iterationsCounter;
			}

			currentSolution = bestNeighbor;
			currentSolutionCost = bestNeighborCost;

			System.out.println("Mejor vecino: " + bestNeighborCost);
			System.out.println("Actual: " + currentSolutionCost);
			System.out.println("LO MEJOR: " + bestCost);
			qap.printSolutionWithCost(currentSolution, currentSolutionCost + " Actual\n\n");

			iterationsCounter++;
		}

		System.out.println("Iteración donde se encontro el mejor: " + updateSolutionCounter);
		System.out.println("Mejor costo: " + bestCost);
		MainActivity.printArray(bestSolution);

		//qap.printMatrix(tabu);

		return bestSolution;
	}

	public int[] getBestNeighbor(int[] currentSolution, int currentIteration, int bestCost) {

		int n = currentSolution.length;
		int[] bestNeighborFound = makeSwap(currentSolution, 0, 1), temporalSolution;
		int posX = 0, posY = 1;

		// this block init best neighbor fount with a value without tabu
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

		// System.out.println("Vecino de Inicio: " + bestNeighborFoundCost);
		qap.printSolutionWithCost(bestNeighborFound, bestNeighborFoundCost + " de inicio");

		// looking in the neigborhooh
		String tabuIndicator;
		System.out.println("\nIteracion: " + currentIteration);

		for (int position1 = 0; position1 < (n - 1); position1++) {
			for (int position2 = position1 + 1; position2 < n; position2++) {

				temporalSolution = makeSwap(currentSolution, position1, position2);
				// MainActivity.printArray(temporalSolution);

				tabuIndicator = "";
				temporalCost = qap.evalSolution(temporalSolution);
				if (isTabu(temporalSolution[position1], temporalSolution[position2], currentIteration)) {
					tabuIndicator = " *  (" + temporalSolution[position1] + "," + temporalSolution[position2] + ")";
				}

				qap.printSolutionWithCost(temporalSolution, temporalCost + tabuIndicator);
				// System.out.println("Vecino: " + temporalCost + tabuIndicator);

				if (temporalCost < bestNeighborFoundCost) {
					int facX = temporalSolution[position1];
					int facY = temporalSolution[position2];

					if (!isTabu(facX, facY, currentIteration)) {
						bestNeighborFound = temporalSolution;
						bestNeighborFoundCost = temporalCost;
						posX = position1;
						posY = position2;
					}

					/*
					 * else { if (satisfyAspitarionCriteria(temporalCost, bestCost)) {
					 * System.out.println("se uso criterio de aspiracion");
					 * 
					 * bestNeighborFound = temporalSolution; bestNeighborFoundCost = temporalCost;
					 * posX = position1; posY = position2; } }
					 */

				}

				// System.out.println("Costo Mejor vecino: " + bestNeighborFoundCost);

			}
		}

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
		System.out.println("Cambio " + facX + " por " + facY);

	}

	// init tabu matrix with 0
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
		// System.out.println(tabu[p1][p2] + " iteracopn" + iteration);

		if (tabu[p1][p2] > iteration) {
			// System.out.println("ojo es tabu");
			return true;
		} else {
			// System.out.println("no es tabu");
			return false;
		}
	}

	public boolean satisfyAspitarionCriteria(int actualCost, int bestCostFound) {
		return (actualCost < bestCostFound) ? true : false;
	}

	public int[] executeOld(int[] initSolution, QAPData qapData) {
		// this initial block define the variable needed
		qap = qapData;
		int n = qap.getSize();
		tabuIterations = n - 1;
		int totalIterations = 50 * n, iterationsCounter = 0;// tabuIterations will be the number
															// of a number is tabu
		initTabuMatrix(n);

		int improveCost = qap.evalSolution(initSolution), temporalCost,
				currentSolutionCost = qap.evalSolution(initSolution), bestCost = qap.evalSolution(initSolution),
				noImproveCost = qap.evalSolution(initSolution);

		int[] temporalSolution, currentSolution = Arrays.copyOf(initSolution, n),
				improveSolution = Arrays.copyOf(initSolution, n), bestSolution = Arrays.copyOf(initSolution, n),
				noImproveSolution = Arrays.copyOf(initSolution, n);
		int diferenceCost = 1000000;
		int updateSolutionCounter = 0; // this counter has the value where the best was found

		int posX = -1, posY = -1;

		while (iterationsCounter < totalIterations) {

			diferenceCost = 1000000;
			improveCost = currentSolutionCost;

			// looking in the neigborhooh
			for (int position1 = 0; position1 < (n - 1); position1++) {
				for (int position2 = position1 + 1; position2 < n; position2++) {
					temporalSolution = makeSwap(currentSolution, position1, position2);
					temporalCost = qap.evalSolution(temporalSolution);

					if (temporalCost < improveCost) {
						int facX = temporalSolution[position1];
						int facY = temporalSolution[position2];
						if (!isTabu(facX, facY, iterationsCounter)) {
							improveSolution = temporalSolution;
							improveCost = temporalCost;
							posX = position1;
							posY = position2;
						} else {

							if (satisfyAspitarionCriteria(temporalCost, bestCost)) {
								improveSolution = temporalSolution;
								improveCost = temporalCost;
								posX = position1;
								posY = position2;
							}

						}
					} else {
						if ((temporalCost - currentSolutionCost < diferenceCost)
								&& (temporalCost != currentSolutionCost)) {
							int facX = temporalSolution[position1];
							int facY = temporalSolution[position2];
							if (!isTabu(facX, facY, iterationsCounter)) {
								noImproveSolution = temporalSolution;
								noImproveCost = temporalCost;
								diferenceCost = temporalCost - currentSolutionCost;
								posX = position1;
								posY = position2;
							}
						}

					}
				}
			}


			if (improveCost < currentSolutionCost) {
				currentSolution = improveSolution;
				currentSolutionCost = improveCost;
				if (bestCost > currentSolutionCost) {
					bestSolution = currentSolution;
					bestCost = currentSolutionCost;
					updateSolutionCounter = iterationsCounter;
				}
			} else {
				currentSolution = noImproveSolution;
				currentSolutionCost = noImproveCost;
			}

			updateTabuMatrix(currentSolution, posX, posY, iterationsCounter);
			iterationsCounter++;

		}

		System.out.println("Numero de iteraciones donde se encontro el mejor: " + updateSolutionCounter);
		System.out.println("Mejor costo: " + bestCost);
		MainActivity.printArray(bestSolution);

		return bestSolution;
	}

}
