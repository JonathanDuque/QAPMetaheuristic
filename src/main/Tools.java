package main;

import java.text.DecimalFormat;
import java.util.List;

public class Tools {
	public static DecimalFormat DECIMAL_FORMAT_2D = new DecimalFormat("#.##");
	public static DecimalFormat DECIMAL_FORMAT_3D = new DecimalFormat("#.###");

	public static void printArray(int[] array) {
		String array_s = "";
		for (int i : array) {
			array_s = array_s + i + " ";
		}
		System.out.print(array_s + "\n");// +1 because the index in java start with 0

		// System.out.println("\n");
	}

	public static void printArray(double[] array) {
		String array_s = "";
		for (double i : array) {
			System.out.println(i);
			// array_s = array_s + i + " ";
		}
		// System.out.print(array_s + "\n");

	}

	public static void printMatrix(int[][] matrix, String label) {
		System.out.println("\n" + label);
		for (int[] row : matrix) {
			for (int i : row) {
				System.out.print(i + "\t");
			}
			System.out.println("\n");
		}

	}

	public static void printParamsPopulation(List<List<Params>> params_population, int team_id) {
		String params_details = "";

		for (int i = 0; i < params_population.size(); i++) {

			List<Params> list_params = params_population.get(i);

			for (int l = 0; l < list_params.size(); l++) {
				params_details = "Team: " + team_id + " " + MainActivity.mh_text[i];
				String array_s = "";
				for (int j : list_params.get(l).getParams()) {
					params_details = params_details + " " + j;
				}
				System.out.print(params_details + "\n");// +1 because the
																									// index in java
																									// start with 0
				// Tools.printArray(list_params.get(l).getParams());
				// System.out.println("fitnes " + list_params.get(l).getFitness());
			}

		}
	}

	public static void printParamsPopulationOld(List<List<Params>> params_population) {
		for (int i = 0; i < params_population.size(); i++) {
			List<Params> list_params = params_population.get(i);
			System.out.println(MainActivity.mh_text[i]);
			for (int l = 0; l < list_params.size(); l++) {
				Tools.printArray(list_params.get(l).getParams());
				// System.out.println("fitnes " + list_params.get(l).getFitness());
			}

		}
	}

	public static void printSolutionPopulation(List<Solution> p, QAPData qap, int team_id) {

		for (int i = 0; i < p.size(); i++) {
			int[] temp_s = p.get(i).getArray();
			int temp_cost = qap.evaluateSolution(p.get(i).getArray());
			// System.out.println(p.get(i).getMethod());
			qap.printSolution(temp_s, temp_cost, team_id);
			// Tools.printArray(p.get(i).getParams());
			// qap.printSolution(temp_s, temp_cost);
		}

	}

}
