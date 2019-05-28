package main;

import java.util.Arrays;
import java.util.Random;

public class LocalSearch {

	public int[] execute(int[] initSolution, QAPData qap) {
		// this initial block define the variable needed
		int cost = qap.evalSolution(initSolution);// cost of the seed

		System.out.println("Solución inicial: ");
		MainActivity.printSolution(initSolution);
		System.out.println("costo: " + cost);

		int n = qap.getSize();
		int[] temporalSolution, improveSolution = Arrays.copyOf(initSolution, n),
				bestSolution = Arrays.copyOf(initSolution, n);
		int temporalCost;
		boolean improve = false; // this flag control when the solution no improve and we are in an optimo local
		int neighboor = 1;
		int delta = 0;

		// here find the best solution from de initSolution
		do {
			System.out.println("\nVecindario: " + neighboor);

			improve = false;
			// here evaluate all the neighboorhood
			for (int position1 = 0; position1 < (n - 1); position1++) {
				for (int position2 = position1 + 1; position2 < n; position2++) {
					delta = qap.evalMovement(bestSolution, position1, position2);

					// if improve
					if (delta > 0) {
						improveSolution = makeSwap(bestSolution, position1, position2);
						cost = cost - delta;
						improve = true;
					}
				}
			}

			if (improve) {
				System.out.println("Mejoró: " + cost);
				MainActivity.printSolution(improveSolution);
				bestSolution = improveSolution;
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

/*
 * temporalSolution = makeSwap(bestSolution, position1, position2); temporalCost
 * = qap.evalSolution(temporalSolution); //temporalCost =
 * qap.evalMovement(bestSolution, position1, position2);
 * 
 * //System.out.println("Costo: " + cost + " Costo Vecino: " + temporalCost); //
 * decide if take the new solution if (temporalCost < cost ) { // cost =
 * temporalCost; improveSolution = temporalSolution; improve = true; }
 */
