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
		QAPData qap;
		int totalAdaptations;

		String problem;
		int totalSearchers;
		int iterationTime;// by iteration

		switch (args.length) {
		case 5:
			problem = args[0];
			totalSearchers = Integer.parseInt(args[1]);
			iterationTime = Integer.parseInt(args[2]);
			totalAdaptations = Integer.parseInt(args[3]);
			break;
		case 4:
			problem = args[0];
			totalSearchers = Integer.parseInt(args[1]);
			iterationTime = Integer.parseInt(args[2]);
			totalAdaptations = Integer.parseInt(args[3]);
			break;
		case 3:
			problem = args[0];
			totalSearchers = Integer.parseInt(args[1]);
			iterationTime = Integer.parseInt(args[2]);
			totalAdaptations = 15;
			break;
		case 2:
			problem = args[0];
			totalSearchers = Integer.parseInt(args[1]);
			iterationTime = 20000;
			totalAdaptations = 15;
			break;
		case 1:
			problem = args[0];
			totalSearchers = 3;
			iterationTime = 20000;
			totalAdaptations = 15;
			break;
		default:
			problem = "bur26a.qap";
			totalSearchers = 63;
			iterationTime = 20000;
			totalAdaptations = 15;
			break;
		}

		final int teams = 1;
		// problem = args[0];
		totalSearchers = 3;
		iterationTime = 2000;
		totalAdaptations = 15;

		// checking some conditions for execution
		if ((totalSearchers % AlgorithmConfiguration.DIFFERENT_MH) != 0) {
			System.out.println(
					"\n***************** Please enter workers multiple of 3     ********************************");
			return;
		}

		if ((totalSearchers % teams) != 0) {
			System.out.println("\n***************** Please enter teams multiple of " + totalSearchers
					+ "    ********************************");
			return;
		}

		if (((totalSearchers / teams) % AlgorithmConfiguration.DIFFERENT_MH) != 0) {
			System.out.println(
					"\n***************** Threads by team must be multiple of 3     ********************************");
			return;
		}

		for (int k = 0; k < 1; k += 1) {
			final long start = System.currentTimeMillis();

			System.out.println("\n*****************    Problem: " + problem + "    ********************************");
			System.out.println("Total Teams: " + teams);
			System.out.println("execution: " + k);

			final ReadFile readFile = new ReadFile("Data/" + problem);
			// final ReadFile readFile = new ReadFile("../../Data/" + problem);

			// initialize qap data, flow and distance matrix, format [row][col]
			final int[][] flow = readFile.getFlow(), distance = readFile.getDistance();
			qap = new QAPData(distance, flow, readFile.getTarget());

			ForkJoinPool pool = new ForkJoinPool(teams);
			List<GenericTeam> list_teams = new ArrayList<>(teams);// these lists are necessary for the executing in
																	// parallel
																	// of each team

			double initTime = (System.currentTimeMillis() - start);
			initTime /= 1000.0;

			for (int i = 0; i < teams; i += 1) {
				TeamParamsAdapted team1 = new TeamParamsAdapted(totalSearchers / teams, iterationTime, totalAdaptations,
						qap, i, SolutionPopulation.REQUEST_RANDOM, SolutionPopulation.ENTRY_IF_DIFERENT,
						qap.size / 3.0);
				list_teams.add(team1);
			}

			// this line is important if we want to execute other type of team with other
			// setup
			/*
			 * TeamParamsRandom team2 = new TeamParamsRandom(totalSearchers / teams,
			 * iterationTime, totalAdaptations, qap, 1, SolutionPopulation.REQUEST_RANDOM,
			 * SolutionPopulation.ENTRY_IF_DIFERENT, qap.size / 3.0); list_teams.add(team2);
			 * 
			 * TeamParamsFixed team3 = new TeamParamsFixed(totalSearchers / teams,
			 * iterationTime, totalAdaptations, qap, 2, SolutionPopulation.REQUEST_SAME,
			 * SolutionPopulation.ENTRY_SAME, qap.size / 3.0); list_teams.add(team3);
			 */

			// launch execution in parallel for all teams
			for (int i = 0; i < teams; i += 1) {
				pool.submit(list_teams.get(i));
			}

			// wait for each team
			for (int i = 0; i < teams; i += 1) {
				list_teams.get(i).join();
			}

			// get the results for each team
			List<Solution> list_teams_solutions = new ArrayList<>();
			for (int i = 0; i < teams; i += 1) {
				Solution solution = list_teams.get(i).getBestTeamSolution();
				list_teams_solutions.add(solution);
			}

			Solution bestTeamSolution = list_teams_solutions.get(0);
			int best_cost = qap.evaluateSolution(bestTeamSolution.getArray());
			int team = list_teams.get(0).getTeamId();
			// get the best result
			for (int i = 0; i < list_teams_solutions.size(); i++) {
				int temp_cost = qap.evaluateSolution(list_teams_solutions.get(i).getArray());

				if (temp_cost < best_cost) {
					best_cost = temp_cost;
					bestTeamSolution = list_teams_solutions.get(i);
					team = list_teams.get(i).getTeamId();
				}
			}

			double totalTime = (System.currentTimeMillis() - start);
			totalTime /= 1000.0;

			printResultInConsole(bestTeamSolution, problem, totalTime, qap, team);
			printResultInFile(bestTeamSolution, problem, totalTime, qap, team, initTime);

			list_teams.clear();
			list_teams_solutions.clear();
			no_find_BKS.set(true);

		}

	}

	// The synchronized keyword ensures that only one thread can enter the method at
	// one time, is one possibility
	public static void findBKS() {
		no_find_BKS.set(false);
	}

	public static boolean is_BKS_was_not_found() {
		return no_find_BKS.get();
	}

	public static void printResultInConsole(Solution bestTeamSolution, String problem, double totalTime, QAPData qap,
			int team) {
		int[] best_solution = bestTeamSolution.getArray();
		int[] best_params = bestTeamSolution.getParams();
		String best_method = bestTeamSolution.getMetaheuristicName();
		int best_cost = qap.evaluateSolution(best_solution);

		final double std = best_cost * 100.0 / qap.getBKS() - 100;

		System.out.println("\n*****************      Results     ********************************");
		System.out.println("Best cost achieved: " + best_cost + " " + Tools.DECIMAL_FORMAT_2D.format(std) + "%");
		System.out.println("Method: " + best_method);
		System.out.println("Parameters:");
		Tools.printArray(best_params);
		System.out.println("team: " + team);
		System.out.println("Total time: " + totalTime + " sec");
	}

	public static void printResultInFile(Solution bestTeamSolution, String problem, double totalTime, QAPData qap,
			int team, double initTime) {
		int[] best_solution = bestTeamSolution.getArray();
		int[] best_params = bestTeamSolution.getParams();
		String best_method = bestTeamSolution.getMetaheuristicName();
		int best_cost = qap.evaluateSolution(best_solution);

		final double std = best_cost * 100.0 / qap.getBKS() - 100;

		// final String dir_file = "Results/";
		final String dir_file = "../Result-26/";

		final String file_name = problem.replace(".qap", "");
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