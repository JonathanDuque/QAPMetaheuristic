package main;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity {

	static final String[] mh_text = { "MTLS", "ROTS", "EO" };

	final static int DIFFERENT_MH = 3;
	// atomic variable to avoid race condition reading and writing it throw threads
	private static AtomicBoolean no_find_BKS = new AtomicBoolean(true);

	public static void main(String[] args) {
		QAPData qap;
		int total_iterations;
		final long start = System.currentTimeMillis();
		final String problem;
		final int workers;
		int execution_time;// by iteration
	
		switch (args.length) {
		case 5:
			problem = args[0];
			workers = Integer.parseInt(args[1]);
			execution_time = Integer.parseInt(args[2]);
			total_iterations = Integer.parseInt(args[3]);
			break;
		case 4:
			problem = args[0];
			workers = Integer.parseInt(args[1]);
			execution_time = Integer.parseInt(args[2]);
			total_iterations = Integer.parseInt(args[3]);
			break;
		case 3:
			problem = args[0];
			workers = Integer.parseInt(args[1]);
			execution_time = Integer.parseInt(args[2]);
			total_iterations = 15;
			break;
		case 2:
			problem = args[0];
			workers = Integer.parseInt(args[1]);
			execution_time = 20000;
			total_iterations = 15;
			break;
		case 1:
			problem = args[0];
			workers = 3;
			execution_time = 20000;
			total_iterations = 15;
			break;
		default:
			problem = "tai40a.qap";
			workers = 3;
			execution_time = 1000;
			total_iterations = 15;
			break;
		}
		
		if ((workers % DIFFERENT_MH) != 0) {
			System.out.println(
					"\n***************** Please enter workers multiple of 3     ********************************");
			return;
		}

		System.out.println("\n*****************    Problem: " + problem + "    ********************************");
		System.out.println("Threads: " + workers);
		System.out.println("Metaheuristic time: " + execution_time / 1000.0 + " seconds");
		System.out.println("Iterations: " + total_iterations);
		System.out.println("Time out: " + execution_time * total_iterations / 1000.0 + " seconds");

		final ReadFile readFile = new ReadFile("Data/" + problem);
		//final ReadFile readFile = new ReadFile("../../Data/" + problem);

		// initialize qap data, flow and distance matrix, format [row][col]
		final int[][] flow = readFile.getFlow(), distance = readFile.getDistance();
		qap = new QAPData(distance, flow, readFile.getTarget());


		double init_time = (System.currentTimeMillis() - start);
		init_time /= 1000.0;
		
		
		WorkerTeam team = new WorkerTeam( workers, execution_time, total_iterations, qap);
		team.compute();
		Solution best_team_solution = team.getbestTeamSolution();
		
		
		// create and initiate variables for results
		int[] best_solution = best_team_solution.getArray();
		int[] best_params = best_team_solution.getParams();
		String method = best_team_solution.getMethod();
		int best_cost = qap.evalSolution(best_solution);
		
		
		double total_time = (System.currentTimeMillis() - start);
		total_time /= 1000.0;
		System.out.println("Total time: " + total_time + " sec");
		
		final double std = best_cost * 100.0 / qap.getBKS() - 100;
		System.out.println("*****************      Results     ********************************");
		System.out.println("Best cost achieved: " + best_cost + " " + Tools.DECIMAL_FORMAT_2D.format(std) + "%");
		System.out.println("Method: " + method);
		System.out.println("Parameters:");
		Tools.printArray(best_params);

		final String dir_file = "Results/";
		// final String dir_file = "../Result-test-dynamic-time/";

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
				fileWriter.append("deviation ");
				fileWriter.append(";");
				fileWriter.append("time");
				fileWriter.append(";");
				fileWriter.append("init_time");
				fileWriter.append(";");
				fileWriter.append("params");
				fileWriter.append(";");
				fileWriter.append("method");
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
			// solution - cost- deviation - time
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
			fileWriter.append(method);
			fileWriter.append("\n");

			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Error writing data");
			e.printStackTrace();
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