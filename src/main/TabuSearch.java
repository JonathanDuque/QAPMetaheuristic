package main;

import java.util.Arrays;

public class TabuSearch {
	/*
	 * the neighgordhood will be change two facilities, so in total we have the
	 * permutation of n take in 2
	 */
	int[][] tabu;
	int tabuIterations;

	
	public int[] execute(int[] initSolution, QAPData qap) {
		// this initial block define the variable needed
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
		int updateSolutionCounter = 0; //this counter has the value where the best was found

		int posX = -1, posY = -1;

		while (iterationsCounter < totalIterations) {

			diferenceCost = 1000000;
			improveCost = currentSolutionCost;

			// looking in the neigborhooh
			for (int position1 = 0; position1 < n; position1++) {
				for (int position2 = position1; position2 < n; position2++) {
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
						if ((temporalCost - currentSolutionCost < diferenceCost) && (temporalCost != currentSolutionCost)) {
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

			//System.out.println("Vieja actual: " + currentSolutionCost);
			
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
			
			
			updatetabuMatrix(currentSolution, posX, posY, iterationsCounter);

			//System.out.println("Mejoro: " + improveCost);
			//System.out.println("Peor: " + noImproveCost);
			//System.out.println("Actual: " + currentSolutionCost);
			//MainActivity.printArray(currentSolution);
			//System.out.println("Ahora la Mejor es: " + bestCost + "\n");
			// qap.printMatrix(tabu);
			iterationsCounter++;

		}

		System.out.println("Numero de iteraciones donde se encontro el mejor: " + updateSolutionCounter);
		System.out.println("Mejor costo: " + bestCost);
		MainActivity.printArray(bestSolution);

		return bestSolution;
	}

	public void updatetabuMatrix(int[] selectedSolution, int posX, int posY, int iterationsCounter) {
		//get the changed facilities 
		int facX = selectedSolution[posX];
		int facY = selectedSolution[posY];
		//make tabu this facilities during certain iterations
		tabu[facX][facY] = iterationsCounter + tabuIterations;
		tabu[facY][facX] = iterationsCounter + tabuIterations;
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

		return newPermutation;

	}

	public boolean isTabu(int p1, int p2, int iteration) {
		if (tabu[p1][p2] > iteration || tabu[p2][p1] > iteration) {
			//System.out.println("ojo es tabu");
			return true;
		} else {
			//System.out.println("no es tabu");
			return false;
		}
	}

	public boolean satisfyAspitarionCriteria(int actualCost, int bestCostFound) {
		return (actualCost < bestCostFound) ? true : false;
	}

	//function for make test
	public int[] execute2(int[] initSolution, QAPData qap) {
		// this initial block define the variable needed
		int n = qap.getSize();
		tabuIterations = n - 1;
		int totalIterations = 10 * n, iterationsCounter = 0;// tabuIterations will be the number
															// of a number is tabu

		int[][] tabu = new int[n][n];
		// init tabu matrix with 0
		for (int[] row : tabu) {
			Arrays.fill(row, 0);
		}

		int improveCost = qap.evalSolution(initSolution), temporalCost,
				currentSolutionCost = qap.evalSolution(initSolution), bestCost = qap.evalSolution(initSolution),
				noImproveCost = qap.evalSolution(initSolution);

		int[] temporalSolution, currentSolution = Arrays.copyOf(initSolution, n),
				improveSolution = Arrays.copyOf(initSolution, n), bestSolution = Arrays.copyOf(initSolution, n),
				noImproveSolution = Arrays.copyOf(initSolution, n);
		int diferenceCost = 1000000;

		int posX = -1, posY = -1;

		while (iterationsCounter < totalIterations) {

			diferenceCost = 1000000;

			improveCost = currentSolutionCost;

			// looking in the neigborhooh
			for (int position1 = 0; position1 < n; position1++) {
				for (int position2 = position1; position2 < n; position2++) {
					temporalSolution = makeSwap(currentSolution, position1, position2);
					temporalCost = qap.evalSolution(temporalSolution);

					if (temporalCost < improveCost) {
						if (!isTabu(position1, position2, iterationsCounter)) {
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
						// MainActivity.printArray(currentSolution);
						// MainActivity.printArray(temporalSolution);

						System.out.println("temporal " + temporalCost + " costo actual " + currentSolutionCost);
						if ((temporalCost - currentSolutionCost < diferenceCost)
								&& (temporalCost != currentSolutionCost)) {
							if (!isTabu(position1, position2, iterationsCounter)) {
								System.out.println("entro al if");

								noImproveSolution = temporalSolution;
								noImproveCost = temporalCost;
								diferenceCost = temporalCost - currentSolutionCost;
								posX = position1;
								posY = position2;
								MainActivity.printArray(noImproveSolution);

							}
						}

						System.out.println("Diferencia " + diferenceCost);

					}
				}
			}

			// uptade tabu matrix
			tabu[posX][posY] = iterationsCounter + tabuIterations;
			tabu[posY][posX] = iterationsCounter + tabuIterations;

			if (improveCost < currentSolutionCost) {
				if (improveCost < bestCost) {
					bestSolution = improveSolution;
					bestCost = improveCost;
				}

				currentSolution = improveSolution;
				currentSolutionCost = improveCost;

			} else {
				System.out.println("Empeoro");
				System.out.println("Costo: " + noImproveCost);
				currentSolution = noImproveSolution;
				currentSolutionCost = noImproveCost;
			}

			System.out.println("Actual Costo: " + currentSolutionCost);
			MainActivity.printArray(currentSolution);

			iterationsCounter++;

		}

		System.out.println("Numero de iteraciones " + iterationsCounter);
		System.out.println("Mejor costo: " + bestCost);
		MainActivity.printArray(bestSolution);

		return bestSolution;
	}
	
}
