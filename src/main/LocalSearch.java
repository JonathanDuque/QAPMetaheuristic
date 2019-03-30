package main;

import java.util.Arrays;
import java.util.Random;

public class LocalSearch {
	
	public int[] execute(int[] initSolution, QAPData qap) {
		//this initial block define the variable needed
		int cost = qap.evalSolution(initSolution);// cost of the seed
		int n = qap.getSize();
		int[] temporalSolution, improveSolution = Arrays.copyOf(initSolution, n),
				bestSolution = Arrays.copyOf(initSolution, n);
		int temporalCost;
		boolean improve = false; // this flag control when the solution no improve and we are in an optimo local
		int neighboor = 1;
				
		//here find the best solution from de initSolution
		do {
			System.out.println("Vecindario: " + neighboor);

			improve = false;
			// here evaluate all the neighboorhood
			for (int positionToSwap = 0; positionToSwap < n; positionToSwap++) {
				temporalSolution = makeSwap(bestSolution, positionToSwap);
				temporalCost = qap.evalSolution(temporalSolution);

				System.out.println("Costo: " + cost + " Costo Vecino: " + temporalCost);
				// decide if take the new solution
				if (temporalCost < cost ) { //
					cost = temporalCost;
					improveSolution = temporalSolution;
					improve = true;
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


	// method for look new solution
	public static int[] localSearch2(int[] permutation) {
		/*
		 * the neighgordhood will be change two facilities, so in total we have the
		 * permutation of n take in 2
		 */

		int size = permutation.length;
		int[] newPermutation = Arrays.copyOf(permutation, size); // is necessary make a copy because java pass the
																	// arrays by referencia like c

		Random r = new Random();
		int posX, posY, temp;

		// first decide what facilities change ramdonly
		posX = r.nextInt(size);// with this value we put the range of number

		do {
			posY = r.nextInt(size);// check that the position to change are diferent
		} while (posX == posY);

		// change the values
		temp = newPermutation[posX];
		newPermutation[posX] = newPermutation[posY];
		newPermutation[posY] = temp;

		// System.out.println("Cambio " + posX + " por " + posY);
		// printArray(permutation);
		// printArray(newPermutation);
		// System.out.println("\n");

		return newPermutation;
	}

}
