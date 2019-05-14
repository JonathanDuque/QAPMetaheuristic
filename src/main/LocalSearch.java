package main;

import java.util.Arrays;
import java.util.Random;

public class LocalSearch {
	
	public int[] executeSimpleSwap(int[] initSolution, QAPData qap) {
		//this initial block define the variable needed
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
				
		//here find the best solution from de initSolution
		do {
			System.out.println("\nVecindario: " + neighboor);

			improve = false;
			// here evaluate all the neighboorhood
			for (int position1 = 0; position1 < (n - 1); position1++) {
				for (int position2 = position1 + 1; position2 < n; position2++) {
					temporalSolution = makeSwap(bestSolution, position1, position2);
					temporalCost = qap.evalSolution(temporalSolution);
					
					System.out.println("Costo: " + cost + " Costo Vecino: " + temporalCost);
					// decide if take the new solution
					if (temporalCost < cost ) { //
						cost = temporalCost;
						improveSolution = temporalSolution;
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

	public  int[] makeSwap(int[] permutation, int positionToSwap) {
		/*
		 * the neighgordhood will be to make a swap between one facility with the
		 * facility of the right
		 */
		int size = permutation.length;
		int[] newPermutation = Arrays.copyOf(permutation, size);
		int temp;

		if (positionToSwap != size - 1) {
			temp = newPermutation[positionToSwap];
			newPermutation[positionToSwap] = newPermutation[positionToSwap + 1];
			newPermutation[positionToSwap + 1] = temp;
		} else {// this happen when is the last position
			temp = newPermutation[positionToSwap];
			newPermutation[positionToSwap] = newPermutation[0];
			newPermutation[0] = temp;
		}

		return newPermutation;
	}
}
