package main;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Constructive {
	//before create more than one team this class only used Random instead of ThreadLocalRandom
	//because there were not teams in parallel using random numbers, now is necessary use
	//ThreadLocalRandom given that team in parallel could ask for solutions

	// constructive algorithm for create init solution intelligently
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
			minor = Integer.MAX_VALUE;
			major = Integer.MIN_VALUE;
			counter++;

			// row and col never be iqual, run matrix only upside
			// this for select the locations with minor distance
			for (int row = 0; row < size; row++) {
				for (int col = row + 1; col < size; col++) { // solo recorro una parte de la matriz lo otro es igual

					// check if the location is not assigned
					for (int p = 0; p < size; p++) {
						if ((solution[p] != -1) && (solution[p] == row || solution[p] == col)) {
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
					for (int p = 0; p < size; p++) {
						if (solution[p] != -1 && (p == row || p == col)) {
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
			solution[facilities[0]] = locations[0];
			solution[facilities[1]] = locations[1];

		}

		// when size of matrix is no pair remains one facility for assigned
		if (size % 2 != 0) {
			int the_last_location = -1;
			for (int l = 0; l < size; l++) {
				if (qap.getFacilityOfLocation(solution, l) == -1) {
					the_last_location = l;// this is the location that remains to be assigned.
					break;
				}
			}

			for (int f = 0; f < solution.length; f++) {
				if (solution[f] == -1) {// find the facility that dont have location
					solution[f] = the_last_location;
					break;
				}
			}
		}

		return solution;
	}

	public int[] createRandomSolution(int size, final int seed) {
		//System.out.println("Seed: " + seed);
		int[] s = new int[size];
		ThreadLocalRandom thread_local_rando = ThreadLocalRandom.current();
		//Random random = new Random(seed);

		s[0] = thread_local_rando.nextInt(size);
		
		for (int i = 1; i < size; i++) {
			boolean isEqual = true;
			while (isEqual) {
				isEqual = false;
				int x = thread_local_rando.nextInt(size);
				for (int j = 0; j <i; j++) {
					if (x == s[j]) {
						isEqual = true;
						break;
					}
				}
				if (!isEqual) {
					s[i]=x; 
				}
			}

		}

		return s;
	}

}
