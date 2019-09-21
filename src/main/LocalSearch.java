package main;

import java.util.Arrays;
import java.util.Random;

public class LocalSearch {

	public int[] solve(int[] initSolution, QAPData qap) {
		// this initial block define the variable needed
		int cost = qap.evalSolution(initSolution);// cost of the seed

		System.out.println("Solución inicial: ");
		qap.printSolution(initSolution);
		

		int n = qap.getSize();
		int[] improveSolution = Arrays.copyOf(initSolution, n), bestSolution = Arrays.copyOf(initSolution, n);
		boolean improve = false; // this flag control when the solution no improve and we are in an optimo local
		int neighboor = 1;
		int temporalDelta, bestDelta;

		// here find the best solution from de initSolution
		do {
			System.out.println("\niteración: " + neighboor);

			improve = false;
			bestDelta = 0;
			
			int i_selected=-1, j_selected=-1;
			// here evaluate all the neighboorhood
			for (int i = 0; i < (n - 1); i++) {
				for (int j = i + 1; j < n; j++) {
					temporalDelta = qap.evalMovement(bestSolution, i, j);

					// if improve
					if (temporalDelta > bestDelta) {
						//System.out.println( "Delta " + temporalDelta);

						i_selected = i;
						j_selected = j;
						bestDelta = temporalDelta;
						improve = true;
					}
				}
			}

			if (improve) {
				cost -= bestDelta;
				System.out.println("Costo: " + cost + " Delta " + bestDelta);
				
				System.out.println( "Delta " + qap.evalMovement(bestSolution, i_selected, j_selected));

				bestSolution = makeSwap(bestSolution, i_selected, j_selected);
				qap.printSolution(bestSolution);
				qap.updateDeltas(bestSolution, i_selected, j_selected);
				//qap.printSolution(bestSolution);
				//qap.initDeltas(bestSolution);

				
			} else {
				improve = false;
			}

			neighboor++;
		} while (improve);

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
}

