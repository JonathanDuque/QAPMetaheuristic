package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity {

	// atomic variable to avoid race condition reading and writing it throw threads
	private static AtomicBoolean no_find_BKS = new AtomicBoolean(true);

	public static void main(String[] args) {

		if (validateAlgorithmConfiguration()) {
			System.out.println(
					"\n******************    All data is correct for execution     ******************************");
		} else {
			return;
		}

		QAPData qap;
		final int totalTeams = AlgorithmConfiguration.totalAdaptedParamsTeams
				+ AlgorithmConfiguration.totalRandomParamsTeams + AlgorithmConfiguration.totalFixedParamsTeams;

		for (int k = 0; k < 1; k += 1) {
			final long start = System.currentTimeMillis();

			System.out.println("\n----------------    Problem: " + AlgorithmConfiguration.problemName
					+ "    -----------------------");
			System.out.println("Total Teams: " + totalTeams);
			System.out.println("execution: " + k);

			final ReadFile readFile = new ReadFile("Data/" + AlgorithmConfiguration.problemName);
			// final ReadFile readFile = new ReadFile("../../Data/" + problem);

			// initialize qap data, flow and distance matrix, format [row][col]
			final int[][] flow = readFile.getFlow(), distance = readFile.getDistance();
			qap = new QAPData(distance, flow, readFile.getTarget());

			ForkJoinPool pool = new ForkJoinPool(totalTeams);
			// these lists are necessary for the executing in parallel of each team
			List<GenericTeam> listTeams = new ArrayList<>(totalTeams);

			double initTime = (System.currentTimeMillis() - start);
			initTime /= 1000.0;

			for (int i = 0; i < AlgorithmConfiguration.totalAdaptedParamsTeams; i += 1) {
				AdaptedParamsTeam adaptedParamsTeam = new AdaptedParamsTeam(qap, i, AlgorithmConfiguration.teamSize,
						AlgorithmConfiguration.iterationTimeAdaptedParamsTeam[i],
						AlgorithmConfiguration.totalAdaptationsAdaptedParamsTeam[i],
						AlgorithmConfiguration.requestPolicyAdaptedParamsTeam[i],
						AlgorithmConfiguration.entryPolicyAdaptedParamsTeam[i],
						AlgorithmConfiguration.solutionSimilarityPercertageAdaptedParamsTeam[i]);
				listTeams.add(adaptedParamsTeam);
			}

			for (int i = 0; i < AlgorithmConfiguration.totalRandomParamsTeams; i += 1) {
				RandomParamsTeam randomParamsTeam = new RandomParamsTeam(qap, i, AlgorithmConfiguration.teamSize,
						AlgorithmConfiguration.iterationTimeRandomParamsTeam[i],
						AlgorithmConfiguration.totalAdaptationsRandomParamsTeam[i],
						AlgorithmConfiguration.requestPolicyRandomParamsTeam[i],
						AlgorithmConfiguration.entryPolicyRandomParamsTeam[i], 0);// 0 because no use
																					// solutionSimilarityPercertage
				listTeams.add(randomParamsTeam);
			}

			for (int i = 0; i < AlgorithmConfiguration.totalFixedParamsTeams; i += 1) {
				FixedParamsTeam fixedParamsTeam = new FixedParamsTeam(qap, i, AlgorithmConfiguration.teamSize,
						AlgorithmConfiguration.iterationTimeFixedParamsTeam[i],
						AlgorithmConfiguration.totalAdaptationsFixedParamsTeam[i],
						AlgorithmConfiguration.requestPolicyFixedParamsTeam[i],
						AlgorithmConfiguration.entryPolicyFixedParamsTeam[i], 0);// 0 because no use
																					// solutionSimilarityPercertage
				listTeams.add(fixedParamsTeam);
			}

			// launch execution in parallel for all teams
			for (int i = 0; i < totalTeams; i += 1) {
				pool.submit(listTeams.get(i));
			}

			// wait for each team
			for (int i = 0; i < totalTeams; i += 1) {
				listTeams.get(i).join();
			}

			// get the results for each team
			List<Solution> listTeamsSolutions = new ArrayList<>();
			for (int i = 0; i < totalTeams; i += 1) {
				Solution solution = listTeams.get(i).getBestTeamSolution();
				listTeamsSolutions.add(solution);
			}

			Solution bestTeamSolution = listTeamsSolutions.get(0);
			int best_cost = qap.evaluateSolution(bestTeamSolution.getArray());
			int team = listTeams.get(0).getTeamId();
			// get the best result
			for (int i = 0; i < listTeamsSolutions.size(); i++) {
				int temp_cost = qap.evaluateSolution(listTeamsSolutions.get(i).getArray());

				if (temp_cost < best_cost) {
					best_cost = temp_cost;
					bestTeamSolution = listTeamsSolutions.get(i);
					team = listTeams.get(i).getTeamId();
				}
			}

			double totalTime = (System.currentTimeMillis() - start);
			totalTime /= 1000.0;

			if (AlgorithmConfiguration.printResultInConsole) {
				printResultInConsole(bestTeamSolution, totalTime, qap, team);
			}

			if (AlgorithmConfiguration.printResultInCSVFile) {
				printResultInFile(bestTeamSolution, totalTime, qap, team, initTime);
			}

			listTeams.clear();
			listTeamsSolutions.clear();
			no_find_BKS.set(true);

		}
		System.out.println(
				"\n**************************      Execution finished     ***********************************");

	}

	// The synchronized keyword ensures that only one thread can enter the method at
	// one time, is one possibility
	public static void findBKS() {
		no_find_BKS.set(false);
	}

	public static boolean is_BKS_was_not_found() {
		return no_find_BKS.get();
	}

	@SuppressWarnings("unused")
	public static boolean validateAlgorithmConfiguration() {

		if (AlgorithmConfiguration.problemName.isEmpty()) {
			System.out.println(
					"\n***************** Please enter a problem name to solve     ********************************");
			return false;
		}

		// checking some conditions for execution
		if ((AlgorithmConfiguration.teamSize % AlgorithmConfiguration.DIFFERENT_MH) != 0) {
			System.out.println(
					"\n***************** Please enter a team size multiple of 3     ********************************");
			return false;
		}

		// TODO validate request and entry policy options
		if (AlgorithmConfiguration.totalAdaptedParamsTeams > 0) {
			if (AlgorithmConfiguration.totalAdaptationsAdaptedParamsTeam == null
					|| AlgorithmConfiguration.iterationTimeAdaptedParamsTeam == null
					|| AlgorithmConfiguration.requestPolicyAdaptedParamsTeam == null
					|| AlgorithmConfiguration.entryPolicyAdaptedParamsTeam == null
					|| AlgorithmConfiguration.solutionSimilarityPercertageAdaptedParamsTeam == null) {
				System.out.println(
						"\n***************** Data for TeamsParamsAdapted: some data have a null value *****************");
				return false;
			}

			if (AlgorithmConfiguration.totalAdaptationsAdaptedParamsTeam.length == AlgorithmConfiguration.totalAdaptedParamsTeams
					&& AlgorithmConfiguration.iterationTimeAdaptedParamsTeam.length == AlgorithmConfiguration.totalAdaptedParamsTeams
					&& AlgorithmConfiguration.requestPolicyAdaptedParamsTeam.length == AlgorithmConfiguration.totalAdaptedParamsTeams
					&& AlgorithmConfiguration.entryPolicyAdaptedParamsTeam.length == AlgorithmConfiguration.totalAdaptedParamsTeams
					&& AlgorithmConfiguration.solutionSimilarityPercertageAdaptedParamsTeam.length == AlgorithmConfiguration.totalAdaptedParamsTeams) {

				for (int i = 0; i < AlgorithmConfiguration.totalAdaptedParamsTeams; i++) {
					if (AlgorithmConfiguration.totalAdaptationsAdaptedParamsTeam[i] <= 0
							|| AlgorithmConfiguration.iterationTimeAdaptedParamsTeam[i] <= 0
							|| AlgorithmConfiguration.solutionSimilarityPercertageAdaptedParamsTeam[i] <= 0) {
						System.out.println(
								"\n***************** Data for TeamsParamsAdapted: Please the values for adaptations number, iteration times and solution "
										+ "similarity percentage should be bigger than 0 ********************************");
						return false;
					}

					if (AlgorithmConfiguration.totalAdaptationsAdaptedParamsTeam[i]
							* AlgorithmConfiguration.iterationTimeAdaptedParamsTeam[i]
							/ 1000.0 != AlgorithmConfiguration.totalTimeOut) {
						System.out.println(
								"\n***************** Data for TeamsParamsAdapted: Please enter the values for adaptations number * iteration time = total time out ***************************");
						return false;

					}
				}

			} else {
				System.out.println(
						"\n***************** Data for TeamsParamsAdapted: some data missing  *****************");
				return false;
			}
		}

		if (AlgorithmConfiguration.totalRandomParamsTeams > 0) {
			if (AlgorithmConfiguration.totalAdaptationsRandomParamsTeam == null
					|| AlgorithmConfiguration.iterationTimeRandomParamsTeam == null
					|| AlgorithmConfiguration.requestPolicyRandomParamsTeam == null
					|| AlgorithmConfiguration.entryPolicyRandomParamsTeam == null) {
				System.out.println(
						"\n***************** Data for RandomParamsTeam: some data have a null value *****************");
				return false;
			}

			if (AlgorithmConfiguration.totalAdaptationsRandomParamsTeam.length == AlgorithmConfiguration.totalRandomParamsTeams
					&& AlgorithmConfiguration.iterationTimeRandomParamsTeam.length == AlgorithmConfiguration.totalRandomParamsTeams
					&& AlgorithmConfiguration.requestPolicyRandomParamsTeam.length == AlgorithmConfiguration.totalRandomParamsTeams
					&& AlgorithmConfiguration.entryPolicyRandomParamsTeam.length == AlgorithmConfiguration.totalRandomParamsTeams) {

				for (int i = 0; i < AlgorithmConfiguration.totalRandomParamsTeams; i++) {
					if (AlgorithmConfiguration.totalAdaptationsRandomParamsTeam[i] <= 0
							|| AlgorithmConfiguration.iterationTimeRandomParamsTeam[i] <= 0) {
						System.out.println(
								"\n***************** Data for RandomParamsTeam: Please the values for adaptations number, and iteration times "
										+ "should be bigger than 0 ********************************");
						return false;
					}

					if (AlgorithmConfiguration.totalAdaptationsRandomParamsTeam[i]
							* AlgorithmConfiguration.iterationTimeRandomParamsTeam[i]
							/ 1000.0 != AlgorithmConfiguration.totalTimeOut) {
						System.out.println(
								"\n***************** Data for TeamParamsRandom: Please enter the values for adaptations number * iteration time = total time out ***************************");
						return false;

					}
				}

			} else {
				System.out
						.println("\n***************** Data for TeamParamsRandom: some data missing  *****************");
				return false;
			}
		}

		if (AlgorithmConfiguration.totalFixedParamsTeams > 0) {
			if (AlgorithmConfiguration.totalAdaptationsFixedParamsTeam == null
					|| AlgorithmConfiguration.iterationTimeFixedParamsTeam == null
					|| AlgorithmConfiguration.requestPolicyFixedParamsTeam == null
					|| AlgorithmConfiguration.entryPolicyFixedParamsTeam == null) {
				System.out.println(
						"\n***************** Data for FixedParamsTeam: some data have a null value *****************");
				return false;
			}

			if (AlgorithmConfiguration.totalAdaptationsFixedParamsTeam.length == AlgorithmConfiguration.totalFixedParamsTeams
					&& AlgorithmConfiguration.iterationTimeFixedParamsTeam.length == AlgorithmConfiguration.totalFixedParamsTeams
					&& AlgorithmConfiguration.requestPolicyFixedParamsTeam.length == AlgorithmConfiguration.totalFixedParamsTeams
					&& AlgorithmConfiguration.entryPolicyFixedParamsTeam.length == AlgorithmConfiguration.totalFixedParamsTeams) {

				for (int i = 0; i < AlgorithmConfiguration.totalFixedParamsTeams; i++) {
					if (AlgorithmConfiguration.totalAdaptationsFixedParamsTeam[i] <= 0
							|| AlgorithmConfiguration.iterationTimeFixedParamsTeam[i] <= 0) {
						System.out.println(
								"\n***************** Data for FixedParamsTeam: Please the values for adaptations number, and iteration times "
										+ "should be bigger than 0 ********************************");
						return false;
					}

					if (AlgorithmConfiguration.totalAdaptationsFixedParamsTeam[i]
							* AlgorithmConfiguration.iterationTimeFixedParamsTeam[i]
							/ 1000.0 != AlgorithmConfiguration.totalTimeOut) {
						System.out.println(
								"\n***************** Data for TeamParamsFixed: Please enter the values for adaptations number * iteration time = total time out ***************************");
						return false;

					}
				}

			} else {
				System.out
						.println("\n***************** Data for TeamParamsFixed: some data missing  *****************");
				return false;
			}
		}

		return true;
	}

	public static void printResultInConsole(Solution bestTeamSolution, double totalTime, QAPData qap, int team) {
		// TODO print more data
		int[] best_solution = bestTeamSolution.getArray();
		int[] best_params = bestTeamSolution.getParams();
		String best_method = bestTeamSolution.getMetaheuristicName();
		int best_cost = qap.evaluateSolution(best_solution);

		final double std = best_cost * 100.0 / qap.getBKS() - 100;

		System.out.println("\n-----------------      Results     -------------------------------");
		System.out.println("Best cost achieved: " + best_cost + " " + Tools.DECIMAL_FORMAT_2D.format(std) + "%");
		System.out.println("Method: " + best_method);
		System.out.println("Parameters:");
		Tools.printArray(best_params);
		System.out.println("team: " + team);
		System.out.println("Total time: " + totalTime + " sec");
	}

	public static void printResultInFile(Solution bestTeamSolution, double totalTime, QAPData qap, int team,
			double initTime) {
		// TODO print more data
		int[] best_solution = bestTeamSolution.getArray();
		int[] best_params = bestTeamSolution.getParams();
		String best_method = bestTeamSolution.getMetaheuristicName();
		int best_cost = qap.evaluateSolution(best_solution);

		final double std = best_cost * 100.0 / qap.getBKS() - 100;

		final String dir_file = "Results/";
		// final String dir_file = "../Result-26/";

		final String file_name = AlgorithmConfiguration.problemName.replace(".qap", "");
		File idea = new File(dir_file + file_name + ".csv");
		FileWriter fileWriter;
		if (!idea.exists()) {
			// if file does not exist, so create and write header

			try {
				fileWriter = new FileWriter(dir_file + file_name + ".csv");
				fileWriter.append("solution");
				fileWriter.append(";");
				fileWriter.append("cost");
				fileWriter.append(";");
				fileWriter.append("deviation");
				fileWriter.append(";");
				fileWriter.append("time");
				fileWriter.append(";");
				fileWriter.append("init_time");
				fileWriter.append(";");
				fileWriter.append("params");
				fileWriter.append(";");
				fileWriter.append("method");
				fileWriter.append(";");
				fileWriter.append("team");
				fileWriter.append("\n");

				fileWriter.flush();
				fileWriter.close();

			} catch (IOException e) {
				System.out.println("Error writing headers");
				e.printStackTrace();
			}
		}

		try {
			fileWriter = new FileWriter(dir_file + file_name + ".csv", true);
			fileWriter.append(Arrays.toString(best_solution));
			fileWriter.append(";");
			fileWriter.append(Integer.toString(best_cost));
			fileWriter.append(";");
			fileWriter.append(Tools.DECIMAL_FORMAT_2D.format(std));
			fileWriter.append(";");
			fileWriter.append(Tools.DECIMAL_FORMAT_3D.format(totalTime));
			fileWriter.append(";");
			fileWriter.append(Tools.DECIMAL_FORMAT_3D.format(initTime));
			fileWriter.append(";");
			fileWriter.append(Arrays.toString(best_params));
			fileWriter.append(";");
			fileWriter.append(best_method);
			fileWriter.append(";");
			fileWriter.append(Integer.toString(team));
			fileWriter.append("\n");

			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Error writing data");
			e.printStackTrace();
		}

	}

}