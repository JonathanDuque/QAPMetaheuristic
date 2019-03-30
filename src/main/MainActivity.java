package main;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Random;

public class MainActivity {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// first initialize the matrix of flow and distance matix [row][col]

		int[][] flow, distance;

		int option = showMenu();
		distance = getDataforDistance(option);
		flow = getDataForFlow(option);

		int size = distance.length;
		int[] initSolution = new int[size], bestSolutionFound;
		int cost;

		showData(distance, flow);

		initSolution = createInitSolution(distance, flow);// create the initial solution
		cost = evalSolution(initSolution, distance, flow);// cost of the seed
		
		System.out.println("Costo: " + cost);
		System.out.println("Solución inicial: ");
		printSolution(initSolution);//show the initial solution
		
		bestSolutionFound = localSearch(initSolution, distance, flow);
		cost = evalSolution(bestSolutionFound, distance, flow);

		System.out.println("\n\n/*********** MEJOR SOLUCIÓN ENCONTRADA **********/");
		System.out.println("Costo: " + cost);
		System.out.println("Combinación: ");
		printSolution(bestSolutionFound);

	}

	public static int[] localSearch(int[] initSolution, int[][] distance, int[][] flow) {
		//this initial block define the variable needed
		int cost = evalSolution(initSolution, distance, flow);// cost of the seed
		int n = distance.length;
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
				temporalCost = evalSolution(temporalSolution, distance, flow);

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
				printSolution(improveSolution);
				bestSolution = improveSolution;
			} else {
				improve = false;
			}
			
			neighboor++;
			
		} while (improve);

		return bestSolution;
	}

	public static int[] makeSwap(int[] permutation, int positionToSwap) {
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

	// cost function
	public static int evalSolution(int[] s, int[][] distance, int[][] flow) {
		int cost = 0;
		int size = distance.length;
		int locX, locY;

		// run the matrix only upside
		for (int row = 0; row < size; row++) {
			for (int col = row + 1; col < size; col++) {
				if (flow[row][col] != 0) {
					locX = getLocationOfFacility(s, row);
					locY = getLocationOfFacility(s, col);
					cost = cost + flow[row][col] * distance[locX][locY];
				}
			}
		}
		return cost;
	}

	// constructive algorithm for create init solution
	public static int[] createInitSolution(int[][] distance, int[][] flow) {
		//this initial block define the variable needed
		int[] solution = new int[distance.length];
		int size_d = distance.length, size_f = flow.length, minor, major;
		int[] locations = new int[2];
		int[] facilities = new int[2];
		int counter = 0;
		boolean next_d = false, next_f;

		Arrays.fill(solution, -1);// init values with no sense for campare easy

		while (counter < size_d / 2) {
			next_d = false;
			next_f = false;
			minor = 100;
			major = -1;
			counter++;

			// row and col never be iqual, run matrix only upside
			//this for select the locations with minor distance
			for (int row = 0; row < size_d; row++) {
				for (int col = row + 1; col < size_d; col++) { // solo recorro una parte de la matriz lo otro es igual

					// check if the location is not assigned
					for (int p = 0; p < solution.length; p++) {
						if (solution[p] != -1 && (p == row || p == col)) {
							next_d = true;
							break;// out of the for

						} else {
							next_d = false;
						}
					}

					if (next_d) {
						continue; // next the other column
					}

					if (distance[row][col] < minor) {
						minor = distance[row][col];
						locations[0] = row;
						locations[1] = col;
					}
				}
			}

			// row and col never be iqual
			//this for select the facilities wiht major flow
			for (int row = 0; row < size_f; row++) {
				for (int col = row + 1; col < size_f; col++) {

					// check if the facilitie is not used
					for (int p = 0; p < solution.length; p++) {
						if ((solution[p] != -1) && (solution[p] == row || solution[p] == col)) {
							next_f = true;
							break;// out of the for
						} else {
							next_f = false;
						}
					}

					if (next_f) {
						continue; // next the other column
					}

					if (flow[row][col] > major) {
						major = flow[row][col];
						facilities[0] = row;
						facilities[1] = col;
					}
				}
			}

			//here assigned the facilities in the locations found
			solution[locations[0]] = facilities[0];
			solution[locations[1]] = facilities[1];

		}

		// when size of matrix is no pair remains one facility for assigned
		if (size_d % 2 != 0) {
			int the_last_facility = -1;
			for (int f = 0; f < solution.length; f++) {
				if (getLocationOfFacility(solution, f) == -1) {
					// System.out.println("Facilidad sin asignar: " + f);
					the_last_facility = f;// this is the facility that remains to be assigned.
					break;
				}
			}

			for (int l = 0; l < solution.length; l++) {
				if (solution[l] == -1) {// find the location that dont have facility
					// System.out.println("Se asigno en locación: " + l);
					solution[l] = the_last_facility;
					break;
				}
			}
		}

		return solution;
	}

	//function for look the location of any facility
	public static int getLocationOfFacility(int[] permutation, int facility) {
		for (int i = 0; i < permutation.length; i++)
			if (permutation[i] == facility)
				return i;
		return -1;
	}

	public static void printMatrix(int[][] matrix) {
		for (int[] row : matrix) {
			for (int i : row) {
				System.out.print(i + "\t");
			}
			System.out.println("\n");
		}
	}

	public static void printSolution(int[] array) {
		// System.out.println("Locaciones");
		for (int i = 0; i < array.length; i++) {
			System.out.print(i + 1 + "\t");
		}
		// System.out.println("\nFacilidades");
		System.out.println("\n");
		for (int i : array) {
			System.out.print(i + 1 + "\t");// +1 because the index in java start with 0
		}
		System.out.println("\n");
	}

	public static void printArray(int[] array) {
		for (int i : array) {
			System.out.print(i + 1 + "\t");// +1 because the index in java start with 0
		}
		System.out.println("\n");
	}

	public static void showData(int[][] distance, int[][] flow) {
		System.out.println("Matriz de Distancias");
		printMatrix(distance);
		System.out.println("\nMatriz de Flujos");
		printMatrix(flow);
	}

	public static int showMenu() {
		int op;
		do {
			System.out.print("/****** ELIJA EL PROBLEMA A SOLUCIONAR ******/\n");
			System.out.print("\t1. QAP6\n\t2. QAP8\n\t3. QAP9\n");
			System.out.print("Escriba la opción y presione ENTER: ");
			Scanner in = new Scanner(System.in);

			op = in.nextInt();
		} while (op < 1 || op > 3);

		return op;
	}

	public static int[][] getDataforDistance(int option) {
		// QAP 6
		int[][] d1 = { { 0, 40, 64, 36, 22, 60 }, { 40, 0, 41, 22, 36, 72 }, { 64, 41, 0, 28, 44, 53 },
				{ 36, 22, 28, 0, 20, 50 }, { 22, 36, 44, 20, 0, 41 }, { 60, 72, 53, 50, 41, 0 } };

		// QAP 8
		int[][] d2 = { { 0, 32, 68, 97, 75, 70, 75, 40 }, { 32, 0, 42, 80, 53, 65, 82, 47 },
				{ 68, 42, 0, 45, 15, 49, 79, 55 }, { 97, 80, 45, 0, 30, 36, 65, 65 }, { 75, 53, 15, 30, 0, 38, 69, 53 },
				{ 70, 65, 49, 36, 38, 0, 31, 32 }, { 75, 82, 79, 65, 69, 31, 0, 36 },
				{ 40, 47, 55, 65, 53, 32, 36, 0 } };

		// QAP 9
		int[][] d3 = { { 0, 32, 68, 97, 75, 70, 75, 40, 24 }, { 32, 0, 42, 80, 53, 65, 82, 47, 29 },
				{ 68, 42, 0, 45, 15, 49, 79, 55, 50 }, { 97, 80, 45, 0, 30, 36, 65, 65, 73 },
				{ 75, 53, 15, 30, 0, 38, 69, 53, 53 }, { 70, 65, 49, 36, 38, 0, 31, 32, 46 },
				{ 75, 82, 79, 65, 69, 31, 0, 36, 56 }, { 40, 47, 55, 65, 53, 32, 36, 0, 19 },
				{ 24, 29, 50, 73, 53, 46, 56, 19, 0 } };

		switch (option) {
		case 1:
			System.out.print("\nSe solucionará QAP6\n");
			return d1;
		case 2:
			System.out.print("\nSe solucionará QAP8\n");
			return d2;

		case 3:
			System.out.print("\nSe solucionará QAP9\n");
			return d3;

		default:
			return d3;
		}

	}

	public static int[][] getDataForFlow(int option) {
		int[][] f1 = { { 0, 1, 1, 2, 0, 0 }, { 1, 0, 0, 0, 0, 2 }, { 1, 0, 0, 0, 0, 1 }, { 2, 0, 0, 0, 3, 0 },
				{ 0, 0, 0, 3, 0, 0 }, { 0, 2, 1, 0, 0, 0 } };

		int[][] f2 = { { 0, 2, 4, 0, 0, 0, 2, 0 }, { 2, 0, 3, 1, 0, 1, 0, 0 }, { 4, 3, 0, 0, 0, 1, 0, 0 },
				{ 0, 1, 0, 0, 3, 0, 1, 5 }, { 0, 0, 0, 3, 0, 0, 0, 0 }, { 0, 1, 1, 0, 0, 0, 0, 0 },
				{ 2, 0, 0, 1, 0, 0, 0, 4 }, { 0, 0, 0, 5, 0, 0, 4, 0 } };

		int[][] f3 = { { 0, 2, 4, 0, 0, 0, 2, 0, 0 }, { 2, 0, 3, 1, 0, 6, 0, 0, 2 }, { 4, 3, 0, 0, 0, 3, 0, 0, 0 },
				{ 0, 1, 0, 0, 1, 0, 1, 2, 0 }, { 0, 0, 0, 1, 0, 0, 0, 0, 0 }, { 0, 6, 3, 0, 0, 0, 0, 0, 2 },
				{ 2, 0, 0, 1, 0, 0, 0, 4, 3 }, { 0, 0, 0, 2, 0, 0, 4, 0, 0 }, { 0, 2, 0, 0, 0, 2, 3, 0, 0 } };

		switch (option) {
		case 1:
			return f1;
		case 2:
			return f2;
		case 3:
			return f3;
		default:
			return f3;
		}

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
