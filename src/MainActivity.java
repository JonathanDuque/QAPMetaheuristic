
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import mpi.*;

public class MainActivity {

	static final String[] mh_text = { "MTLS", "ROTS", "EO" };
	static private final int MTLS = 0, ROTS = 1, EO = 2;
	private static List<Solution> solution_population;
	static private int qap_size;

	final static int DIFFERENT_MH = 3;
	private static ThreadLocalRandom thread_local_random;
	// atomic variable to avoid race condition reading and writing it throw threads
	private static AtomicBoolean no_find_BKS = new AtomicBoolean(true);

	public static void main(String[] args) {
		MPI.Init(args);

		QAPData qap;
		int total_iterations;
		final long start = System.currentTimeMillis();
		final String problem;
		// final int total_workers = MPI.COMM_WORLD.Size();
		int execution_time;// by iteration

		/*
		 * switch (args.length) { case 5: problem = args[0]; total_workers =
		 * Integer.parseInt(args[1]); execution_time = Integer.parseInt(args[2]);
		 * total_iterations = Integer.parseInt(args[3]); break; case 4: problem =
		 * args[0]; total_workers = Integer.parseInt(args[1]); execution_time =
		 * Integer.parseInt(args[2]); total_iterations = Integer.parseInt(args[3]);
		 * break; case 3: problem = args[0]; total_workers = Integer.parseInt(args[1]);
		 * execution_time = Integer.parseInt(args[2]); total_iterations = 15; break;
		 * case 2: problem = args[0]; total_workers = Integer.parseInt(args[1]);
		 * execution_time = 20000; total_iterations = 15; break; case 1: problem =
		 * args[0]; total_workers = 3; execution_time = 20000; total_iterations = 15;
		 * break; default: problem = "tai40a.qap"; total_workers = 30; execution_time =
		 * 200; total_iterations = 15; break; }
		 */

		problem = "tai40a.qap";
		execution_time = 200;
		total_iterations = 15;

		final int workers = 3;
		final int team_id = MPI.COMM_WORLD.Rank();
		final int total_teams = MPI.COMM_WORLD.Size();

		if (team_id == 0) {
			System.out.println("\n*****************    Problem: " + problem + "    ********************************");
			System.out.println("Teams: " + total_teams);
			System.out.println("Threads: " + workers);
			System.out.println("Metaheuristic time: " + execution_time / 1000.0 + " seconds");
			System.out.println("Iterations: " + total_iterations);
			System.out.println("Time out: " + execution_time * total_iterations / 1000.0 + " seconds");
			System.out.println("Team: " + team_id);
		}

		final ReadFile readFile = new ReadFile("../Data/" + problem);
		// final ReadFile readFile = new ReadFile("../../Data/" + problem);

		// initialize qap data, flow and distance matrix, format [row][col]
		final int[][] flow = readFile.getFlow(), distance = readFile.getDistance();
		qap = new QAPData(distance, flow, readFile.getTarget());
		qap_size = qap.getSize();
		thread_local_random = ThreadLocalRandom.current();

		final double[] diversify_percentage_limit = getDiversifyPercentageLimit(total_iterations);

		ForkJoinPool pool = new ForkJoinPool(workers);
		final Constructive constructive = new Constructive();
		final int number_workes_by_mh = workers / DIFFERENT_MH;

		// these lists are necessary for the executing in parallel
		List<MultiStartLocalSearch> list_mtls = new ArrayList<>(number_workes_by_mh);
		List<RobustTabuSearch> list_rots = new ArrayList<>(number_workes_by_mh);
		List<ExtremalOptimization> list_eo = new ArrayList<>(number_workes_by_mh);

		List<List<Params>> params_population = generateInitialParamsPopulation(number_workes_by_mh);
		solution_population = generateInitialSolutionPopulation(workers, constructive);

		// create array parameters for each metaheuristic
		int[] params_MTLS = new int[3];
		int[] params_ROTS = new int[3];
		int[] params_EO = new int[3];

		int current_iteration = 0;

		double init_time = (System.currentTimeMillis() - start);
		init_time /= 1000.0;

		// start algorithm solution
		while (current_iteration < total_iterations && is_BKS_was_not_found()) {

			for (int i = 0; i < number_workes_by_mh; i += 1) {
				MultiStartLocalSearch mtls = new MultiStartLocalSearch(qap);
				RobustTabuSearch rots = new RobustTabuSearch(qap);
				ExtremalOptimization eo = new ExtremalOptimization(qap);

				list_mtls.add(mtls);
				list_rots.add(rots);
				list_eo.add(eo);
			}

			List<Params> list_params_MTLS = new ArrayList<>(params_population.get(MTLS));
			List<Params> list_params_ROST = new ArrayList<>(params_population.get(ROTS));
			List<Params> list_params_EO = new ArrayList<>(params_population.get(EO));

			// is necessary a copy because after solution population will be update
			List<Solution> solution_population_copy = new ArrayList<>(solution_population);

			// setting environment variables for each method
			for (int i = 0; i < number_workes_by_mh; i += 1) {
				params_MTLS = selectParam(list_params_MTLS).getParams();
				params_ROTS = selectParam(list_params_ROST).getParams();
				params_EO = selectParam(list_params_EO).getParams();

				list_mtls.get(i).setEnvironment(getSolutionFromList(solution_population_copy), params_MTLS,
						execution_time);
				list_rots.get(i).setEnvironment(getSolutionFromList(solution_population_copy), params_ROTS,
						execution_time);
				list_eo.get(i).setEnvironment(getSolutionFromList(solution_population_copy), params_EO, execution_time);
			}

			// launch execution in parallel for all workers
			for (int i = 0; i < number_workes_by_mh; i += 1) {
				pool.submit(list_mtls.get(i));
				pool.submit(list_rots.get(i));
				pool.submit(list_eo.get(i));
			}

			// wait for each method
			for (int i = 0; i < number_workes_by_mh; i += 1) {
				list_mtls.get(i).join();
				list_rots.get(i).join();
				list_eo.get(i).join();
			}

			solution_population.clear();

			for (int i = 0; i < number_workes_by_mh; i += 1) {
				// init_cost, final cost order matter
				double[] behavior_mtls = compareSolution(list_mtls.get(i).getInitCost(), list_mtls.get(i).getBestCost(),
						list_mtls.get(i).getInitSolution(), list_mtls.get(i).getSolution());
				double[] behavior_rots = compareSolution(list_rots.get(i).getInitCost(), list_rots.get(i).getBestCost(),
						list_rots.get(i).getInitSolution(), list_rots.get(i).getSolution());
				double[] behavior_eo = compareSolution(list_eo.get(i).getInitCost(), list_eo.get(i).getBestCost(),
						list_eo.get(i).getInitSolution(), list_eo.get(i).getSolution());

				params_MTLS = improveParameter(list_mtls.get(i).getParams(), behavior_mtls, MTLS, current_iteration,
						total_iterations, diversify_percentage_limit);
				params_ROTS = improveParameter(list_rots.get(i).getParams(), behavior_rots, ROTS, current_iteration,
						total_iterations, diversify_percentage_limit);
				params_EO = improveParameter(list_eo.get(i).getParams(), behavior_eo, EO, current_iteration,
						total_iterations, diversify_percentage_limit);

				// insert the new parameters into parameters population
				insertParameter(params_population.get(MTLS), new Params(params_MTLS, list_mtls.get(i).getBestCost()),
						MTLS);
				insertParameter(params_population.get(ROTS), new Params(params_ROTS, list_rots.get(i).getBestCost()),
						ROTS);
				insertParameter(params_population.get(EO), new Params(params_EO, list_eo.get(i).getBestCost()), EO);

				// inserts solution into solution population
				updateSolutionPopulation(list_mtls.get(i).getSolution(), params_MTLS, mh_text[MTLS]);
				updateSolutionPopulation(list_rots.get(i).getSolution(), params_ROTS, mh_text[ROTS]);
				updateSolutionPopulation(list_eo.get(i).getSolution(), params_EO, mh_text[EO]);
			}

			list_mtls.clear();
			list_rots.clear();
			list_eo.clear();

			current_iteration++;

			// updating times
			// current_time += execution_time;
			// execution_time = updateExecutionTime2(execution_time, current_time,
			// time_out);
			/*
			 * execution_time += step_time; step_time += 1000;
			 * 
			 * if (current_time + execution_time + step_time > time_out) { execution_time =
			 * time_out - current_time; }
			 */

		}

		// create and initiate variables for each team result
		int[] my_best_solution_array = constructive.createRandomSolution(qap_size, thread_local_random.nextInt());
		int my_best_cost = qap.evalSolution(my_best_solution_array);
		final int[] empty_params = { -1, -1, -1 };
		Solution my_best_solution = new Solution(my_best_solution_array, empty_params, "N/A");

		// update final results variables
		for (int i = 0; i < solution_population.size(); i++) {
			int temp_cost = qap.evalSolution(solution_population.get(i).getArray());

			if (temp_cost < my_best_cost) {
				my_best_cost = temp_cost;
				my_best_solution = solution_population.get(i);
			}
		}

		// best_solution = best_team_solution.getArray();
		// int[] best_params = best_team_solution.getParams();
		// String best_method = best_team_solution.getMethod();
		// best_cost = qap.evalSolution(best_solution);

		// double total_time = (System.currentTimeMillis() - start);
		// total_time /= 1000.0;

		//final double std = my_best_cost * 100.0 / qap.getBKS() - 100;
		// System.out.println("\n***************** Results
		// ********************************");
		//System.out.println("Best cost achieved: " + my_best_cost + " " + Tools.DECIMAL_FORMAT_2D.format(std) + "%");
		// System.out.println("Method: " + best_method);
		// System.out.println("Parameters:");
		// Tools.printArray(best_params);
		// System.out.println("Total time: " + total_time + " sec");

		int tag_message = 99;

		List<Solution> list_teams_solutions = new ArrayList<>();

		if (team_id != 0) {
			// Status status;
			Request request;
			int over = MPI.BSEND_OVERHEAD;
			int solution_size = my_best_solution.convertSolutionToArray().length;
			int real_solution_size = MPI.COMM_WORLD.Pack_size(solution_size, MPI.INT) + over;

			ByteBuffer buf = ByteBuffer.allocateDirect(real_solution_size);
			MPI.Buffer_attach(buf);

			request = MPI.COMM_WORLD.Ibsend(my_best_solution.convertSolutionToArray(), 0, solution_size, MPI.INT, 0,
					tag_message);
			// status = request.Test();

		} else {
			// team 0 gets the results for each team

			list_teams_solutions.add(my_best_solution);
			final int solution_size = my_best_solution.convertSolutionToArray().length;

			for (int t = 1; t < total_teams; t++) {
				// System.out.println("Data from team: " + t);

				int[] team_solution_array = new int[solution_size];
				MPI.COMM_WORLD.Recv(team_solution_array, 0, solution_size, MPI.INT, t, tag_message);
				// System.out.println("solution" + t);

				Solution solution = new Solution(team_solution_array, qap_size);
				list_teams_solutions.add(solution);

			}
		}

		// team 0 calculate the best solution
		if (team_id == 0) {
			Solution best_solution;
			best_solution = list_teams_solutions.get(0);
			int best_cost = qap.evalSolution(best_solution.getArray());

			// get the best result
			for (int i = 0; i < list_teams_solutions.size(); i++) {
				int temp_cost = qap.evalSolution(list_teams_solutions.get(i).getArray());

				if (temp_cost < best_cost) {
					best_cost = temp_cost;
					best_solution = list_teams_solutions.get(i);
				}
			}

			int[] best_solution_array = best_solution.getArray();
			int[] best_params = best_solution.getParams();
			String best_method = best_solution.getMethod();
			best_cost = qap.evalSolution(best_solution_array);

			double total_time = (System.currentTimeMillis() - start);
			total_time /= 1000.0;

			final double std = best_cost * 100.0 / qap.getBKS() - 100;
			System.out.println("\n*****************      Results     ********************************");
			System.out.println("Best cost achieved: " + best_cost + " " + Tools.DECIMAL_FORMAT_2D.format(std) + "%");
			System.out.println("Method: " + best_method);
			System.out.println("Parameters:");
			Tools.printArray(best_params);
			System.out.println("Total time: " + total_time + " sec");

			final String dir_file = "Results/";
			// final String dir_file = "../Result-test-5teams-15-adaptations/";

			final String file_name = problem.replace(".qap", "");
			File idea = new File(dir_file + file_name + ".csv");
			FileWriter fileWriter;
			if (!idea.exists()) { // if file does not exist, so create and write header

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
				fileWriter.append(Arrays.toString(best_solution_array));
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
				fileWriter.append(Integer.toString(team_id));
				fileWriter.append("\n");

				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error writing data");
				e.printStackTrace();
			}

		}

		MPI.Finalize();

	}

	// The synchronized keyword ensures that only one thread can enter the method at
	// one time, is one possibility
	public static void findBKS() {
		no_find_BKS.set(false);
	}

	public static boolean is_BKS_was_not_found() {
		return no_find_BKS.get();
	}

	public static double[] getDiversifyPercentageLimit(int total_iterations) {
		int total_values = 20;
		final double[] limits = new double[total_values];

		double a = 94.67597;
		double b = 0.31811;
		double c = 0.15699;

		double y = 0;

		for (int x = 0; x < total_values; x++) {
			y = a * Math.exp(-b * (x + 1)) + c;
			// System.out.println("f(" + x + ") = " + (float) y);

			limits[x] = y;
		}

		double m = (double) total_values / total_iterations;

		// System.out.println("f(" + m + ") = " + (float) m);

		final double[] definitive_limits = new double[total_iterations];

		int index;

		for (int x = 0; x < total_iterations; x++) {
			index = (int) Math.round(m * x);
			definitive_limits[x] = limits[index];
			// System.out.println("f(" + x + ") = " + definitive_limits[x]);
		}

		return definitive_limits;
	}

	public static List<List<Params>> generateInitialParamsPopulation(int params_of_each_mh) {
		List<List<Params>> params_population = new ArrayList<>(DIFFERENT_MH); // because there are # DIFFERENT_MH

		for (int k = 0; k < DIFFERENT_MH; k++) {
			List<Params> tempListParams = new ArrayList<>(params_of_each_mh);
			for (int i = 0; i < params_of_each_mh; i++) {
				int[] p = { 0, 0, 0 }; // parameters array

				switch (k) {
				case MTLS:
					p[0] = thread_local_random.nextInt(2); // restart type 0: random restart, 1: swaps
					break;
				case ROTS:
					p[0] = (thread_local_random.nextInt(16) + 4) * qap_size;// 4 * (i + 1) * qap_size;// tabu duration
																			// factor
					p[1] = (thread_local_random.nextInt(10) + 1) * qap_size * qap_size;// 2 * (i + 1) * qap_size *
																						// qap_size; //
					// aspiration factor
					break;
				case EO:
					p[0] = thread_local_random.nextInt(100); // tau*100
					p[1] = thread_local_random.nextInt(3); // pdf function type
					break;
				}
				tempListParams.add(new Params(p, Integer.MAX_VALUE));
			}

			params_population.add(tempListParams);
		}

		// printParamsPopulation(params_population);
		return params_population;
	}

	public static List<Solution> generateInitialSolutionPopulation(final int total, Constructive constructive) {
		List<Solution> init_solution_population = new ArrayList<>();
		final int[] empty_params = { -1, -1, -1 };

		for (int i = 0; i < total; i++) {
			int[] s = constructive.createRandomSolution(qap_size, i);
			init_solution_population.add(new Solution(s, empty_params, "N/A"));
		}

		return init_solution_population;
	}

	public static Params selectParam(List<Params> p) {
		Params selected;
		// obtain a number between 0 - size population
		int index = thread_local_random.nextInt(p.size());
		selected = p.get(index);
		p.remove(index);// delete for no selecting later
		// System.out.println("selected : " + index);

		return selected;
	}

	public static int[] getSolutionFromList(List<Solution> population) {
		Solution selected_solution;
		final int index = thread_local_random.nextInt(population.size());

		selected_solution = population.get(index);
		population.remove(index);// delete for no selecting later

		return selected_solution.getArray();
	}

	public static double[] compareSolution(int init_cost, int final_cost, int[] init_solution, int[] final_solution) {
		// init_cost - 100%
		// init_cost-final_cost - x
		double difference_percentage = (init_cost - final_cost) * 100.0 / init_cost;

		int distance = 0;
		for (int i = 0; i < init_solution.length; i++) {
			if (init_solution[i] != final_solution[i]) {
				distance++;
			}
		}
		double[] comparison = { difference_percentage, distance };

		return comparison;
	}

	public static int[] improveParameter(final int[] parameter, final double[] behavior_mh, final int type,
			final int current_iteration, final int total_iterations, final double[] diversify_percentage_limit) {

		final double[] change_pdf_percentage_limit = { 10, 5, 1, 0.5, 0.3 };
		final double divisor = (float) total_iterations / change_pdf_percentage_limit.length;
		int[] new_params = { 0, 0, 0 };

		// behavior_mh[0] = percentage difference
		// behavior_mh[1] = distance

		if (behavior_mh[0] > 0) {
			switch (type) {
			// case MTLS keep equal
			case ROTS:
				if (behavior_mh[0] <= diversify_percentage_limit[current_iteration] && behavior_mh[1] <= qap_size / 3) {
					// is necessary diversify
					new_params[0] = parameter[0] + Math.floorDiv(qap_size, 2);
					new_params[1] = parameter[1] + Math.floorDiv(qap_size * qap_size, 2);
				} else {
					// is necessary intensify
					new_params[0] = parameter[0] - Math.floorDiv(qap_size, 3);
					new_params[1] = parameter[1] - Math.floorDiv(qap_size, 2);
				}

				if (new_params[0] > 20 * qap_size) {
					new_params[0] = thread_local_random.nextInt(16 * qap_size) + 4 * qap_size; // 4n to 20n
				}

				if (new_params[1] > 10 * qap_size * qap_size) {
					new_params[1] = thread_local_random.nextInt(9 * qap_size * qap_size) + qap_size * qap_size; // n*n
																												// to
																												// 10*n*n
				}

				break;

			case EO:
				// parameter[0] : tau
				// parameter[1] : probability function

				if (behavior_mh[0] <= diversify_percentage_limit[current_iteration] && behavior_mh[1] <= qap_size / 3) {
					// is necessary diversify
					switch (parameter[1]) {
					case 2:// gamma tau: 0 to 1 means intensify to diversify
						new_params[0] = parameter[0] + 6;
						break;
					default:
						// Exponential tau: 0 to 1 means diversify to intensify
						// Power tau: 0 to 1 means diversify to intensify
						new_params[0] = parameter[0] - 6;
						break;
					}
				} else {
					// is necessary intensify
					switch (parameter[1]) {
					case 2:// gamma tau: 0 to 1 means intensify to diversify
						new_params[0] = parameter[0] - 6;
						break;
					default:
						// Exponential tau: 0 to 1 means diversify to intensify
						// Power tau: 0 to 1 means diversify to intensify
						new_params[0] = parameter[0] + 6;
						break;
					}
				}

				if (new_params[0] > 100) {
					new_params[0] = thread_local_random.nextInt(100); // tau*100
				}

				if (behavior_mh[0] < change_pdf_percentage_limit[(int) Math.floor(current_iteration / divisor)]) {
					int new_pdf_function;
					do {
						new_pdf_function = thread_local_random.nextInt(3);
						new_params[1] = new_pdf_function;
					} while (parameter[1] == new_pdf_function);

				}
				break;
			}
		} else {
			// if the solution did not improve, so will be assign a new parameter
			new_params = createParam(type);
		}

		return new_params;
	}

	// insert new parameter and remove the worst
	public static void insertParameter(List<Params> listParams, Params new_params, final int type) {

		int worst = -1;
		int cost = Integer.MIN_VALUE;
		int temp_score = 0;

		for (int i = 0; i < listParams.size(); i++) {
			temp_score = listParams.get(i).getFitness();
			if (temp_score > cost) {
				cost = temp_score;
				worst = i;
			}
		}

		listParams.remove(worst);
		listParams.add(new_params);
	}

	public static void updateSolutionPopulation(int[] s, int[] params, String method) {

		boolean exist; // this cycle finish until the new solution will be different
		do {
			exist = false; // identify if the new solution is already in the population
			for (Solution temp : solution_population) {
				if (Arrays.equals(temp.getArray(), s)) {
					exist = true;
					break;
				}
			} // if exist is necessary mutate
			if (exist) {
				s = mutate(s);
			}

		} while (exist);

		solution_population.add(new Solution(s, params, method));

	}

	public static int[] createParam(int type) {

		int[] p = { 0, 0, 0 };

		switch (type) {
		case MTLS:
			p[0] = thread_local_random.nextInt(2); // restart type 0: random restart, 1: swaps
			break;
		case ROTS:
			p[0] = thread_local_random.nextInt(16 * qap_size) + 4 * qap_size; // 4n to 20n
			p[1] = thread_local_random.nextInt(9 * qap_size * qap_size) + qap_size * qap_size; // n*n to 10*n*n
			// same range dokeroglu article
			break;
		case EO:
			p[0] = thread_local_random.nextInt(100); // tau*100
			p[1] = thread_local_random.nextInt(3); // pdf function type
			break;
		}
		return p;
	}

	private static int[] mutate(int[] s) {
		int posX, posY, temp;

		// first decide what value change randomly
		posX = thread_local_random.nextInt(qap_size);// with this value we put the range of number
		do {
			posY = thread_local_random.nextInt(qap_size);// check that the position to change are different
		} while (posX == posY);

		// swapping - making the mutation
		temp = s[posX];
		s[posX] = s[posY];
		s[posY] = temp;
		return s;
	}

}