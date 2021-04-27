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
	
	public static void printParam(Params param, String mh_type, int team_id) {
		String params_details = "Team: " + team_id + " " + mh_type;
		String array_s = "";
		for (int j : param.getParams()) {
			params_details = params_details + " " + j;
		}
		System.out.print(params_details + " Gain: " + Tools.DECIMAL_FORMAT_3D.format(param.getGain()) + " D: " + param.getDistance() + "\n");
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
				System.out.print(params_details + " Gain: " + Tools.DECIMAL_FORMAT_3D.format(list_params.get(l).getGain()) + " D: " + list_params.get(l).getDistance() + " Setup: "+ list_params.get(l).getSetup() + "\n");// +1 because the
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
			int temp_cost = qap.evalSolution(p.get(i).getArray());
			// System.out.println(p.get(i).getMethod());
			qap.printSolution(temp_s, temp_cost, team_id);
			// Tools.printArray(p.get(i).getParams());
			// qap.printSolution(temp_s, temp_cost);
		}

	}

	/*
	 * public static void printMetaheuristic(final int type_mh, final int cost,
	 * final int[] p, DecimalFormat df2) {
	 * 
	 * String params_text = ""; switch (type_mh) { case MTLS:
	 * System.out.println("\nMultiStart LocalSearch Results:"); params_text = (p[0]
	 * == 0) ? "\nRandom Restart" : "\nRestart by swaps"; break; case ROTS:
	 * System.out.println("\nRobust TabuSearch Results:"); params_text =
	 * "\nTabu duration: " + p[0] + "\nAspiration factor: " + p[1]; break; case EO:
	 * System.out.println("\nExtremal Optimization Results:"); params_text =
	 * "\nTau: " + p[0] / 100.0 + "\nPdf function: "; switch (p[1]) { case 0:
	 * params_text += "Exponential"; break; case 1: params_text += "Power"; break;
	 * case 2: params_text += "Gamma"; break; }
	 * 
	 * break; case GA: System.out.println("\nGenetic Algorithm Results:");
	 * params_text = "\nPopulation: " + p[0] + "\nMutation rate: " + p[1] / 1000.0;
	 * params_text += (p[2] == 0) ? "\nCrossover UX" :
	 * "\nCrossover in random point";
	 * 
	 * break; }
	 * 
	 * double std = cost * 100.0 / qap.getBKS() - 100; System.out.println("Cost: " +
	 * cost + " " + df2.format(std) + "%"); System.out.println("Params " +
	 * params_text);
	 * 
	 * }
	 */

}
