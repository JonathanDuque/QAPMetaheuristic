package main;

public class QAPData {

	private int[][] flow, distance;
	private int size;

	// constructor for init data
	public QAPData(int[][] distance, int[][] flow) {
		this.distance = distance;
		this.flow = flow;
		size = distance.length;
	}

	public int getDistanceBetween(int location1, int location2) {
		return distance[location1][location2];
	}

	public int getFlowBetween(int facility1, int facility2) {
		return flow[facility1][facility2];
	}

	// cost function
	public int evalSolution(int[] s) {
		int cost = 0;
		// s[0] = location of facility 0

		// run the matrix only upside
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (flow[row][col] != 0) {
					cost = cost + flow[row][col] * distance[s[row]][s[col]];
				}
			}
		}
		return cost;
	}

	// article A novel multistart hyper-heuristic algorithm on the grid for the
	// quadratic assignment problem
	public int evalMovement(int[] solution, int f1, int f2) {
		int delta = 0;
		for (int k = 0; k < size; k++) {
			if (k != f1 && k != f2) {
				delta = delta + (flow[f2][k] - flow[f1][k])
						* (distance[solution[f2]][solution[k]] - distance[solution[f1]][solution[k]]);
			}
		}

		return delta;
	}

	//TODO revisar esta funcion  
	// function for look the location of any facility
	public int getLocationOfFacility(int[] permutation, int facility) {
		for (int i = 0; i < permutation.length; i++)
			if (permutation[i] == facility)
				return i;
		return -1;
	}

	private int getFacilityOfLocation(int[] permutation, int location) {
		for (int i = 0; i < permutation.length; i++)
			if (permutation[i] == location)
				return i;
		return -1;
	}

	public int getSize() {
		return size;
	}

	public void showData() {
		System.out.println("Matriz de Distancias");
		printMatrix(distance);
		System.out.println("\nMatriz de Flujos");
		printMatrix(flow);
	}

	public void printMatrix(int[][] matrix) {
		for (int[] row : matrix) {
			for (int i : row) {
				System.out.print(i + "\t");
			}
			System.out.println("\n");
		}
	}

	public void printSolutionWithCost(int[] array, String cost) {
		for (int i : array) {
			System.out.print(i + ", ");// +1 because the index in java start with 0
		}
		System.out.println("Costo: " + cost);
	}

	// para imorimir solucion en formato ubicaciones - facilidades
	public void printSolutionInReadFormat(int[] array) {
		String facilities = "Facilidades: ", locations = "Ubicaciones: ";
		for (int i = 0; i < array.length; i++) {
			locations = locations + ((i + 0) + " ");
		}
		System.out.println("\n" + locations);
		for (int l = 0; l < array.length; l++) {
			facilities = facilities + ((getFacilityOfLocation(array, l) + 0) + " ");// +1 because the index in java
																					// start with 0
		}

		System.out.println(facilities);
	}
}
