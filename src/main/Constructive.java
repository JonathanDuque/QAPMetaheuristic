package main;

import java.util.Arrays;

public class Constructive {

	// constructive algorithm for create init solution
	public int[] createInitSolution(QAPData qap) {
		// this initial block define the variable needed
		int size = qap.getSize(), minor, major;
		int[] solution = new int[size];
		int[] locations = new int[2];
		int[] facilities = new int[2];
		int counter = 0;
		boolean next_d = false, next_f;

		Arrays.fill(solution, -1);// init values with no sense for campare easy

		while (counter < size / 2) {
			next_d = false;
			next_f = false;
			minor = 100;
			major = -1;
			counter++;

			// row and col never be iqual, run matrix only upside
			// this for select the locations with minor distance
			for (int row = 0; row < size; row++) {
				for (int col = row + 1; col < size; col++) { // solo recorro una parte de la matriz lo otro es igual

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

					if (qap.getDistanceBetween(row, col) < minor) {
						minor = qap.getDistanceBetween(row, col);
						locations[0] = row;
						locations[1] = col;
					}
				}
			}

			// row and col never be iqual
			// this for select the facilities wiht major flow
			for (int row = 0; row < size; row++) {
				for (int col = row + 1; col < size; col++) {

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

					if (qap.getFlowBetween(row, col) > major) {
						major = qap.getFlowBetween(row, col);
						facilities[0] = row;
						facilities[1] = col;
					}
				}
			}

			// here assigned the facilities in the locations found
			solution[locations[0]] = facilities[0];
			solution[locations[1]] = facilities[1];

		}

		// when size of matrix is no pair remains one facility for assigned
		if (size % 2 != 0) {
			int the_last_facility = -1;
			for (int f = 0; f < solution.length; f++) {
				if (qap.getLocationOfFacility(solution, f) == -1) {
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

}
