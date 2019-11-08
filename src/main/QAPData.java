package main;

public class QAPData implements Cloneable {

	final private int[][] flow, distance, delta;
	final private int size;
	final private int bks;

	// constructor for init data
	public QAPData(int[][] distance, int[][] flow, int bks) {
		this.distance = distance;
		this.flow = flow;
		size = distance.length;
		delta = new int[size][size];
		this.bks = bks;
	}

	public int[][] getFlow() {
		return flow;
	}

	public int[][] getDistance() {
		return distance;
	}

	public int getBKS() {
		return bks;
	}

	public int getDistanceBetween(int location1, int location2) {
		return distance[location1][location2];
	}

	public int getFlowBetween(int facility1, int facility2) {
		return flow[facility1][facility2];
	}

	public int[] makeSwap(int[] permutation, int i, int j) {
		int temp;

		// change the values
		temp = permutation[i];
		permutation[i] = permutation[j];
		permutation[j] = temp;

		return permutation;
	}

	/********** initialization of current solution value ***********/
	/**************** and matrix of cost of moves *****************/
	public void initDeltas(int[] s) {
		// s[2] = location of facility 2

		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				// current_cost = current_cost + flow[i][j] * distance[s[i]][s[j]];
				// if (i < j) {
				delta[i][j] = compute_delta(s, i, j);
				// }
			}
		}

		//Tools.printMatrix(delta, "Init Deltas");

	}

	public int compute_delta(int[] s, int i, int j) {

		int d = (flow[j][j] - flow[i][i]) * (distance[s[j]][s[j]] - distance[s[i]][s[i]])
				+ (flow[j][i] - flow[i][j]) * (distance[s[j]][s[i]] - distance[s[i]][s[j]]);

		for (int k = 0; k < size; k++) {
			if (k != i && k != j) {
				// asi estaba antes
				// d = d + (flow[j][k] - flow[i][k]) * (distance[s[j]][s[k]] -
				// distance[s[i]][s[k]]);
				d += (flow[j][k] - flow[i][k]) * (distance[s[j]][s[k]] - distance[s[i]][s[k]])
						+ (flow[k][j] - flow[k][i]) * (distance[s[k]][s[j]] - distance[s[k]][s[i]]);
				// d+= (flow[k][i] - flow[k][j]) * (distance[s[k]][s[j]] - distance[s[k]][s[i]])
				// +
				// (flow[i][k] - flow[j][k]) * (distance[s[j]][s[k]] - distance[s[i]][s[k]]);
			}
		}
		return d;
	}

	// cost function
	public int evalSolution(int[] s) {
		int cost = 0;
		// s[0] = location of facility 0

		// run the matrix only upside
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				cost = cost + flow[row][col] * distance[s[row]][s[col]];
			}
		}
		return cost;
	}

	// article A novel multistart hyper-heuristic algorithm on the grid for the
	// quadratic assignment problem
	public int evalMovement(int[] solution, int i, int j) {

		//int delta2 = 0;
				/*(flow[j][j] - flow[i][i]) * (distance[solution[j]][solution[j]] - distance[solution[i]][solution[i]])
		+ (flow[j][i] - flow[i][j]) * (distance[solution[j]][solution[i]] - distance[solution[i]][solution[j]]);*/
		
		/*for (int k = 0; k < size; k++) {
			if (k != i && k != j) {
				delta2 = delta2 + (flow[j][k] - flow[i][k])
						* (distance[solution[j]][solution[k]] - distance[solution[i]][solution[k]]);
			}
		}*/

		//System.out.println(mh + " real  : " + delta2*2  + " matriz  " + delta[i][j]);

		return delta[i][j];
	}

	public void updateDeltas(int[] s, int i_selected, int j_selected) {
		for (int i = 0; i < size - 1; i++) {
			for (int j = i + 1; j < size; j++)
				if (i != i_selected && i != j_selected && j != i_selected && j != j_selected) {
					delta[i][j] = compute_delta_part(s, i, j, i_selected, j_selected);
				} else {
					delta[i][j] = compute_delta(s, i, j);
				}
		}
	}

	public int compute_delta_part(int[] p, int i, int j, int i_selected, int j_selected) {
		return delta[i][j] - ((flow[i_selected][i] - flow[i_selected][j] + flow[j_selected][j] - flow[j_selected][i])
				* (distance[p[j_selected]][p[i]] - distance[p[j_selected]][p[j]] + distance[p[i_selected]][p[j]]
						- distance[p[i_selected]][p[i]])
				+ (flow[i][i_selected] - flow[j][i_selected] + flow[j][j_selected] - flow[i][j_selected])
						* (distance[p[i]][p[j_selected]] - distance[p[j]][p[j_selected]] + distance[p[j]][p[i_selected]]
								- distance[p[i]][p[i_selected]]));
	}

	// function for look the facility of any location
	public int getFacilityOfLocation(int[] permutation, int location) {
		for (int i = 0; i < permutation.length; i++)
			if (permutation[i] == location)
				return i;
		return -1;
	}

	public int getSize() {
		return size;
	}

	public void showData() {
		// Tools.printMatrix(distance, "Matriz de Distancias");
		// Tools.printMatrix(flow, "\nMatriz de Flujos");
		Tools.printMatrix(delta, "\nMatriz de Deltas");
	}

	public void printSolution(int[] array, String label) {

		System.out.println(label);
		System.out.println("Costo: " + evalSolution(array));
		String facilities = "Facilidades: ", locations = "Ubicaciones: ";
		// for (int i = 0; i < array.length; i++) {
		// facilities = facilities + ((i + 1) + " ");
		// }

		// System.out.println(facilities);
		for (int i : array) {
			locations = locations + ((i + 1) + " ");// +1 because the index in java start with 0
		}
		System.out.println(locations);

	}

	// para imorimir solucion en formato ubicaciones - facilidades
	public void printSolution2(int[] array) {
		String facilities = "Facilidades: ", locations = "Ubicaciones: ";
		for (int i = 0; i < array.length; i++) {
			locations = locations + ((i + 1) + " ");
		}
		System.out.println("\n" + locations);
		for (int l = 0; l < array.length; l++) {
			facilities = facilities + ((getFacilityOfLocation(array, l) + 1) + " ");// +1 because the index in java
																					// start with 0
		}

		System.out.println(facilities);
	}
}
