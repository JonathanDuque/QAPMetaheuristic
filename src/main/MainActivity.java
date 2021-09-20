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

	static final String[] setup_text = { "Fixed", "Random", "Adapted" };
	static final int FIXED = 0, RANDOM = 1, ADAPTED = 2;

	// atomic variable to avoid race condition reading and writing it throw threads
	private static AtomicBoolean no_find_BKS = new AtomicBoolean(true);

	public static void main(String[] args) {
		QAPData qap;
		int totalAdaptations;

		String problem;
		int total_workers;
		int iterationTime;// by iteration
		String parameter_setup = setup_text[ADAPTED];

		switch (args.length) {
		case 5:
			problem = args[0];
			total_workers = Integer.parseInt(args[1]);
			iterationTime = Integer.parseInt(args[2]);
			totalAdaptations = Integer.parseInt(args[3]);
			break;
		case 4:
			problem = args[0];
			total_workers = Integer.parseInt(args[1]);
			iterationTime = Integer.parseInt(args[2]);
			totalAdaptations = Integer.parseInt(args[3]);
			break;
		case 3:
			problem = args[0];
			total_workers = Integer.parseInt(args[1]);
			iterationTime = Integer.parseInt(args[2]);
			totalAdaptations = 15;
			break;
		case 2:
			problem = args[0];
			total_workers = Integer.parseInt(args[1]);
			iterationTime = 20000;
			totalAdaptations = 15;
			break;
		case 1:
			problem = args[0];
			total_workers = 3;
			iterationTime = 20000;
			totalAdaptations = 15;
			break;
		default:
			problem = "bur26a.qap";
			total_workers = 63;
			iterationTime = 20000;
			totalAdaptations = 15;
			break;
		}

		final int teams = 1;
		problem = args[0];
		total_workers = 63;
		iterationTime = 20000;
		totalAdaptations = 15;

		// checking some conditions for execution
		if ((total_workers % AlgorithmConfiguration.DIFFERENT_MH) != 0) {
			System.out.println(
					"\n***************** Please enter workers multiple of 3     ********************************");
			return;
		}

		if ((total_workers % teams) != 0) {
			System.out.println("\n***************** Please enter teams multiple of " + total_workers
					+ "    ********************************");
			return;
		}

		if (((total_workers / teams) % AlgorithmConfiguration.DIFFERENT_MH) != 0) {
			System.out.println(
					"\n***************** Threads by team must be multiple of 3     ********************************");
			return;
		}

		for (int k = 0; k < 30; k += 1) {
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
			List<WorkerTeam> list_teams = new ArrayList<>(teams);// these lists are necessary for the executing in
																	// parallel
																	// of each team

			double init_time = (System.currentTimeMillis() - start);
			init_time /= 1000.0;

			for (int i = 0; i < teams; i += 1) {
				WorkerTeam team1 = new WorkerTeam(total_workers / teams, iterationTime, totalAdaptations, qap, i,
						parameter_setup, SolutionPopulation.REQUEST_RANDOM, SolutionPopulation.ENTRY_IF_DIFERENT, qap.size/3.0);
				list_teams.add(team1);
			}

			// this line is important if we want to execute teams with other setup
			// WorkerTeam team2 = new WorkerTeam(total_workers / teams, 15000, 20,
			// qap,false, 1);
			// list_teams.add(team2);

			// WorkerTeam team3 = new WorkerTeam(total_workers / teams, 15000, 20, qap,
			// true, 2);
			// list_teams.add(team3);

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

			Solution best_team_solution = list_teams_solutions.get(0);
			int best_cost = qap.evaluateSolution(best_team_solution.getArray());
			int team = 0;

			// get the best result
			for (int i = 0; i < list_teams_solutions.size(); i++) {
				int temp_cost = qap.evaluateSolution(list_teams_solutions.get(i).getArray());

				if (temp_cost < best_cost) {
					best_cost = temp_cost;
					best_team_solution = list_teams_solutions.get(i);
					team = i;
				}
			}

			int[] best_solution = best_team_solution.getArray();
			int[] best_params = best_team_solution.getParams();
			String best_method = best_team_solution.getMetaheuristicName();
			best_cost = qap.evaluateSolution(best_solution);

			double total_time = (System.currentTimeMillis() - start);
			total_time /= 1000.0;

			final double std = best_cost * 100.0 / qap.getBKS() - 100;
			System.out.println("\n*****************      Results     ********************************");
			System.out.println("Best cost achieved: " + best_cost + " " + Tools.DECIMAL_FORMAT_2D.format(std) + "%");
			System.out.println("Method: " + best_method);
			System.out.println("Parameters:");
			Tools.printArray(best_params);
			System.out.println("Total time: " + total_time + " sec");

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
				fileWriter.append(Tools.DECIMAL_FORMAT_3D.format(total_time));
				fileWriter.append(";");
				fileWriter.append(Tools.DECIMAL_FORMAT_3D.format(init_time));
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

}