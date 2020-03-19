package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity {
	private static int qap_size;
	private static Random random;
	private static final int MTLS = 0, ROTS = 1, EO = 2, GA = 3;
	private static String[] mh_text = { "MTLS", "ROTS", "EO", "GA" };
	private static int DIFFERENT_MH = 3;
	private static QAPData qap;
	private static int execution_time = 10000;
	// atomic variable to avoid race condition reading and writing it throw threads
	private static AtomicBoolean no_find_BKS = new AtomicBoolean(true);
	private static List<Solution> solution_population;

	public static void main(String[] args) {
		final int generations;
		final long start = System.currentTimeMillis();
		final String problem;
		final int workers;
		final int global_seed;

		switch (args.length) {
		case 5:
			problem = args[0];
			workers = Integer.parseInt(args[1]);
			execution_time = Integer.parseInt(args[2]);
			generations = Integer.parseInt(args[3]);
			global_seed = Integer.parseInt(args[4]);
			break;
		case 4:
			problem = args[0];
			workers = Integer.parseInt(args[1]);
			execution_time = Integer.parseInt(args[2]);
			generations = Integer.parseInt(args[3]);
			global_seed = 1;
			break;
		case 3:
			problem = args[0];
			workers = Integer.parseInt(args[1]);
			execution_time = Integer.parseInt(args[2]);
			generations = 15;
			global_seed = 1;
			break;
		case 2:
			problem = args[0];
			workers = Integer.parseInt(args[1]);
			execution_time = 20000;
			generations = 15;
			global_seed = 1;
			break;
		case 1:
			problem = args[0];
			workers = 3;
			execution_time = 20000;
			generations = 15;
			global_seed = 1;
			break;
		default:
			problem = "tai40a.qap";
			workers = 3;
			execution_time = 20000;
			generations = 15;
			global_seed = 1;
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
		System.out.println("Generations: " + generations);
		System.out.println("Seed for random values: " + global_seed + "\n");

		final ReadFile readFile = new ReadFile("Data/" + problem);

		// initialize qap data, i.e matrix of flow and distance matrix [row][col]
		final int[][] flow = readFile.getFlow(), distance = readFile.getDistance();
		qap = new QAPData(distance, flow, readFile.getTarget());
		qap_size = qap.getSize();
		random = new Random(global_seed);

		ForkJoinPool pool = new ForkJoinPool(workers);
		final Constructive constructive = new Constructive();

		final int number_workes_by_mh = workers / DIFFERENT_MH;
		final int params_of_each_mh = 3 * number_workes_by_mh;

		List<List<Params>> params_population_one = generateInitialParamsPopulation(params_of_each_mh);
		List<List<Params>> params_population = generateEmptyParamsPopulation(params_of_each_mh);
		solution_population = generateInitialSolutionPopulation(workers, constructive);

		// create params class for each mh
		Params p_MTLS_1, p_MTLS_2;
		Params p_ROTS_1, p_ROTS_2;
		Params p_EO_1, p_EO_2;
		// Params p_GA_1, p_GA_2;

		// create array params for each mh
		int[] paramsMTLS = new int[3];
		int[] paramsROTS = new int[3];
		int[] paramsEO = new int[3];
		// int[] paramsGA = new int[3];

		// these lists are necessary for executing in parallel
		List<MultiStartLocalSearch> list_mtls = new ArrayList<>(number_workes_by_mh);
		List<RobustTabuSearch> list_rots = new ArrayList<>(number_workes_by_mh);
		List<ExtremalOptimization> list_eo = new ArrayList<>(number_workes_by_mh);
		// List<GeneticAlgorithm> list_ga = new ArrayList<>();

		int count_generations = 0;

		// create and initiate variables for results
		int[] best_solution = constructive.createRandomSolution(qap_size, count_generations);
		int[] best_params = { -1, -1, -1 };
		String method = "";
		int best_cost = qap.evalSolution(best_solution);

		double init_time = (System.currentTimeMillis() - start);
		init_time /= 1000.0;
		// System.out.println("Initiate time: " + init_time + " sec");

		while (count_generations < params_of_each_mh / number_workes_by_mh && no_find_BKS.get()) {
			for (int i = 0; i < number_workes_by_mh; i += 1) {
				MultiStartLocalSearch mtls = new MultiStartLocalSearch(qap, random.nextInt());
				RobustTabuSearch rots = new RobustTabuSearch(qap, random.nextInt());
				ExtremalOptimization eo = new ExtremalOptimization(qap, random.nextInt());

				list_mtls.add(mtls);
				list_rots.add(rots);
				list_eo.add(eo);
			}

			List<Params> list_params_MTLS = params_population_one.get(MTLS);
			List<Params> list_params_ROST = params_population_one.get(ROTS);
			List<Params> list_params_EO = params_population_one.get(EO);

			// is necessary a copy because after solution population will be update
			List<Solution> solution_population_copy = new ArrayList<>(solution_population);

			for (int i = 0; i < number_workes_by_mh; i += 1) {
				paramsMTLS = selectParam(list_params_MTLS).getParams();
				paramsROTS = selectParam(list_params_ROST).getParams();
				paramsEO = selectParam(list_params_EO).getParams();

				// setting variables for each method
				list_mtls.get(i).setEnvironment(getSolution(solution_population_copy), paramsMTLS);
				list_rots.get(i).setEnvironment(getSolution(solution_population_copy), paramsROTS);
				list_eo.get(i).setEnvironment(getSolution(solution_population_copy), paramsEO);
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

			// save the results of each method
			for (int i = 0; i < number_workes_by_mh; i += 1) {
				paramsMTLS = list_mtls.get(i).getParams();
				paramsROTS = list_rots.get(i).getParams();
				paramsEO = list_eo.get(i).getParams();

				params_population.get(MTLS).add(new Params(paramsMTLS, list_mtls.get(i).getBestCost()));
				params_population.get(ROTS).add(new Params(paramsROTS, list_rots.get(i).getBestCost()));
				params_population.get(EO).add(new Params(paramsEO, list_eo.get(i).getBestCost()));

				// update solution population
				updateSolutionPopulation(list_mtls.get(i).getSolution(), paramsMTLS, mh_text[MTLS]);
				updateSolutionPopulation(list_rots.get(i).getSolution(), paramsROTS, mh_text[ROTS]);
				updateSolutionPopulation(list_eo.get(i).getSolution(), paramsEO, mh_text[EO]);
			}

			count_generations++;

			list_mtls.clear();
			list_rots.clear();
			list_eo.clear();

			// System.out.println("Solution Population");
			// printSolutionPopulation(solution_population);

		}

		while (count_generations < generations && no_find_BKS.get()) {

			for (int i = 0; i < number_workes_by_mh; i += 1) {
				MultiStartLocalSearch mtls = new MultiStartLocalSearch(qap, random.nextInt());
				RobustTabuSearch rots = new RobustTabuSearch(qap, random.nextInt());
				ExtremalOptimization eo = new ExtremalOptimization(qap, random.nextInt());

				list_mtls.add(mtls);
				list_rots.add(rots);
				list_eo.add(eo);
			}

			// System.out.println( count_generations );
			List<Params> list_params_MTLS = new ArrayList<>(params_population.get(MTLS));
			List<Params> list_params_ROST = new ArrayList<>(params_population.get(ROTS));
			List<Params> list_params_EO = new ArrayList<>(params_population.get(EO));

			// is necessary a copy because after solution population will be update
			List<Solution> solution_population_copy = new ArrayList<>(solution_population);

			for (int i = 0; i < number_workes_by_mh; i += 1) {
				p_MTLS_1 = selectParam(list_params_MTLS);
				p_MTLS_2 = selectParam(list_params_MTLS);

				p_ROTS_1 = selectParam(list_params_ROST);
				p_ROTS_2 = selectParam(list_params_ROST);

				p_EO_1 = selectParam(list_params_EO);
				p_EO_2 = selectParam(list_params_EO);

				// crossover and mutation are method dependent
				paramsMTLS = crossover(p_MTLS_1.getParams(), p_MTLS_2.getParams(), MTLS);
				paramsMTLS = mutate(paramsMTLS, MTLS, 0.5);

				paramsROTS = crossover(p_ROTS_1.getParams(), p_ROTS_2.getParams(), ROTS);
				paramsROTS = mutate(paramsROTS, ROTS, 0.5);

				paramsEO = crossover(p_EO_1.getParams(), p_EO_2.getParams(), EO);
				paramsEO = mutate(paramsEO, EO, 0.5);

				// setting variables for each method
				list_mtls.get(i).setEnvironment(getSolution(solution_population_copy), paramsMTLS);
				list_rots.get(i).setEnvironment(getSolution(solution_population_copy), paramsROTS);
				list_eo.get(i).setEnvironment(getSolution(solution_population_copy), paramsEO);
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
				paramsMTLS = list_mtls.get(i).getParams();
				paramsROTS = list_rots.get(i).getParams();
				paramsEO = list_eo.get(i).getParams();

				// insert the new parameters into params population
				insertParam(params_population.get(MTLS), new Params(paramsMTLS, list_mtls.get(i).getBestCost()), MTLS);
				insertParam(params_population.get(ROTS), new Params(paramsROTS, list_rots.get(i).getBestCost()), ROTS);
				insertParam(params_population.get(EO), new Params(paramsEO, list_eo.get(i).getBestCost()), EO);

				// inserts solution into solution population
				updateSolutionPopulation(list_mtls.get(i).getSolution(), paramsMTLS, mh_text[MTLS]);
				updateSolutionPopulation(list_rots.get(i).getSolution(), paramsROTS, mh_text[ROTS]);
				updateSolutionPopulation(list_eo.get(i).getSolution(), paramsEO, mh_text[EO]);
			}

			// diverse_population.clear();
			// int ga_population = random.nextInt(number_workes_by_mh);
			// diverse_population = list_ga.get(ga_population).getFinalPopulation();

			count_generations++;

			// System.out.println("Solution Population");
			// printSolutionPopulation(solution_population);

			list_mtls.clear();
			list_rots.clear();
			list_eo.clear();

		}

		// update final results variables
		for (int i = 0; i < solution_population.size(); i++) {
			int[] temp_s = solution_population.get(i).getArray();
			int temp_cost = qap.evalSolution(solution_population.get(i).getArray());
			if (temp_cost < best_cost) {
				best_solution = temp_s;
				best_cost = temp_cost;
				best_params = solution_population.get(i).getParams();
				method = solution_population.get(i).getMethod();
			}
		}

		final double std = best_cost * 100.0 / qap.getBKS() - 100;
		System.out.println("Best cost achieved: " + best_cost + " " + Tools.DECIMAL_FORMAT_2D.format(std) + "%");
		System.out.println("Method: " + method);
		System.out.println("Parameters:");
		Tools.printArray(best_params);

		double total_time = (System.currentTimeMillis() - start);
		total_time /= 1000.0;
		System.out.println("Total time: " + total_time + " sec");
		// System.out.println("Generations: " + count_generations);

		final String dir_file = "Results/";

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
				fileWriter.append("generations");
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
			// solution - cost- deviation - time - generations
			fileWriter.append(Arrays.toString(best_solution));
			fileWriter.append(";");
			fileWriter.append(Integer.toString(best_cost));
			fileWriter.append(";");
			fileWriter.append(Tools.DECIMAL_FORMAT_2D.format(std));
			fileWriter.append(";");
			fileWriter.append(Tools.DECIMAL_FORMAT_3D.format(total_time));
			fileWriter.append(";");
			fileWriter.append(Integer.toString(count_generations));
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

	public static List<Solution> generateInitialSolutionPopulation(final int total, Constructive constructive) {
		List<Solution> init_solution_population = new ArrayList<>();
		final int[] empty_params = { -1, -1, -1 };

		for (int i = 0; i < total; i++) {
			int[] s = constructive.createRandomSolution(qap_size, i);
			init_solution_population.add(new Solution(s, empty_params, "N/A"));
		}

		return init_solution_population;
	}

	public static List<List<Params>> generateInitialParamsPopulation(int params_of_each_mh) {
		List<List<Params>> params_population = new ArrayList<>(DIFFERENT_MH); // because there are # DIFFERENT_MH

		for (int k = 0; k < DIFFERENT_MH; k++) {
			List<Params> tempListParams = new ArrayList<>(params_of_each_mh);
			for (int i = 0; i < params_of_each_mh; i++) {
				int[] p = { 0, 0, 0 }; // params array

				switch (k) {
				case MTLS:
					p[0] = random.nextInt(2); // restart type 0: random restart, 1: swaps
					break;
				case ROTS:
					p[0] = (random.nextInt(16) + 4) * qap_size;// 4 * (i + 1) * qap_size;// tabu duration factor
					p[1] = (random.nextInt(10) + 1) * qap_size * qap_size;// 2 * (i + 1) * qap_size * qap_size; //
																			// aspiration factor
					break;
				case EO:
					p[0] = random.nextInt(100); // tau*100
					p[1] = random.nextInt(3); // pdf function type
					break;
				/*
				 * case GA: p[0] = qap_size + qap_size * random.nextInt(5) / 2;// population
				 * size if (qap_size > 60) { p[0] = p[0] * 2 / 5; } p[1] = random.nextInt(1000);
				 * // mutation *1000 p[2] = random.nextInt(2);// crossover operator type
				 * 0:crossover UX, 1:crossover in a random point
				 * 
				 * break;
				 */
				}
				tempListParams.add(new Params(p, Integer.MAX_VALUE));
			}

			params_population.add(tempListParams);
		}

		// printParamsPopulation(params_population);
		return params_population;
	}

	public static List<List<Params>> generateEmptyParamsPopulation(int params_of_each_mh) {
		List<List<Params>> params_population_empty = new ArrayList<>(DIFFERENT_MH); // because there are # DIFFERENT_MH

		for (int k = 0; k < DIFFERENT_MH; k++) {
			List<Params> tempListParams = new ArrayList<>(params_of_each_mh);
			params_population_empty.add(tempListParams);
		}

		return params_population_empty;
	}

	public static Params selectParam(List<Params> p) {
		Params selected;
		// obtain a number between 0 - size population
		int index = random.nextInt(p.size());
		selected = p.get(index);
		p.remove(index);// delete for no selecting later
		// System.out.println("selected : " + index);

		return selected;
	}

	public static int[] getSolution(List<Solution> p) {
		Solution selected_solution;
		final int index = random.nextInt(p.size());

		selected_solution = p.get(index);
		p.remove(index);// delete for no selecting later

		return selected_solution.getArray();
	}

	public static int[] crossover(int[] params1, int[] params2, int mh_type) {
		int[] p = { 0, 0, 0 };
		switch (mh_type) {
		case MTLS:
			p[0] = params1[0]; // restart type
			break;
		case ROTS:
			p[0] = params1[0];// tabu duration factor
			p[1] = params2[1]; // aspiration factor
			break;
		case EO:
			p[0] = params1[0];// tau*100
			p[1] = params2[1];// type pdf
			break;
		case GA:
			p[0] = params1[0];// population size
			p[1] = params1[1];// mutation *1000
			p[2] = params2[2]; // crossover type
			break;
		}

		// Tools.printArray(p);

		return p;
	}

	public static int[] mutate(int[] params, int mh_type, double mp) {
		double mutation_number = random.nextDouble();

		int[] p = params.clone();
		// Tools.printArray(p);
		if (mutation_number <= mp) {
			final int param;
			switch (mh_type) {
			case MTLS:
				p[0] = random.nextInt(2); // restart type 0 or 1
				break;
			case ROTS:
				// getting what parameter to mutate
				param = random.nextInt(2);
				// System.out.println("param:" +param);
				switch (param) {
				case 0:
					p[param] = random.nextInt(16 * qap_size) + 4 * qap_size; // 4n to 20n
					break;
				case 1:
					p[param] = random.nextInt(9 * qap_size * qap_size) + qap_size * qap_size; // n*n to 10*n*n same
																								// range dokeroglu
																								// article
					break;
				}
				break;
			case EO:
				param = random.nextInt(2);
				// System.out.println("param:" +param);
				switch (param) {
				case 0:
					p[param] = random.nextInt(100); // tau*100
					break;
				case 1:
					p[param] = random.nextInt(3); // pdf function type
					break;
				}

				break;
			case GA:
				param = random.nextInt(3);
				// System.out.println("param:" +param);
				switch (param) {
				case 0:
					p[param] = qap_size + qap_size * random.nextInt(5) / 2;// population size
					if (qap_size > 60) {
						p[param] = p[param] * 2 / 5;
					}
					break;
				case 1:
					p[param] = random.nextInt(1000); // mutation *1000
					break;
				case 2:
					p[param] = random.nextInt(2);// crossover operator type
					break;
				}
				break;
			}
		}

		// Tools.printArray(p);
		return p;

	}

	// insert new params and remove the worst
	public static void insertParam(List<Params> listParams, Params new_params, final int type) {

		int worst = -1;
		int cost = Integer.MIN_VALUE;
		int temp_score = 0;
		boolean exist = false;

		// is no possible mutate because for example MTLS only has two options 0 0 0 and
		// 1 0 0

		// this cycle finish until the new params will be different
		/*
		 * do { exist = false; // identify if the new individual is already in the
		 * generation for (Params temp : listParams) {
		 * Tools.printArray(temp.getParams()); Tools.printArray(new_params.getParams());
		 * if (Arrays.equals(temp.getParams(), new_params.getParams())) { exist = true;
		 * break; } } // if exist is necessary mutate if (exist) { new_params = new
		 * Params(mutate(new_params.getParams(), type, 1), new_params.getScore()); }
		 * System.out.println("aca"); } while (exist);
		 */

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

	private static int[] mutate(int[] s) {
		int posX, posY, temp;

		// first decide what value change randomly
		posX = random.nextInt(qap_size);// with this value we put the range of number
		do {
			posY = random.nextInt(qap_size);// check that the position to change are different
		} while (posX == posY);

		// swapping - making the mutation
		temp = s[posX];
		s[posX] = s[posY];
		s[posY] = temp;
		return s;
	}

	public static int getSeed() {
		return random.nextInt();
	}

	public static int getExecutionTime() {
		return execution_time;
	}

	public static void printParamsPopulation(List<List<Params>> params_population) {
		for (int i = 0; i < params_population.size(); i++) {
			List<Params> list_params = params_population.get(i);
			System.out.println(mh_text[i]);
			for (int l = 0; l < list_params.size(); l++) {
				Tools.printArray(list_params.get(l).getParams());
				// System.out.println("fitnes " + list_params.get(l).getFitness());
			}

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

	public static void printMetaheuristic(final int type_mh, final int cost, final int[] p, DecimalFormat df2) {

		String params_text = "";
		switch (type_mh) {
		case MTLS:
			System.out.println("\nMultiStart LocalSearch Results:");
			params_text = (p[0] == 0) ? "\nRandom Restart" : "\nRestart by swaps";
			break;
		case ROTS:
			System.out.println("\nRobust TabuSearch Results:");
			params_text = "\nTabu duration: " + p[0] + "\nAspiration factor: " + p[1];
			break;
		case EO:
			System.out.println("\nExtremal Optimization Results:");
			params_text = "\nTau: " + p[0] / 100.0 + "\nPdf function: ";
			switch (p[1]) {
			case 0:
				params_text += "Exponential";
				break;
			case 1:
				params_text += "Power";
				break;
			case 2:
				params_text += "Gamma";
				break;
			}

			break;
		case GA:
			System.out.println("\nGenetic Algorithm Results:");
			params_text = "\nPopulation: " + p[0] + "\nMutation rate: " + p[1] / 1000.0;
			params_text += (p[2] == 0) ? "\nCrossover UX" : "\nCrossover in random point";

			break;
		}

		double std = cost * 100.0 / qap.getBKS() - 100;
		System.out.println("Cost: " + cost + " " + df2.format(std) + "%");
		// Tools.printArray(solution);
		System.out.println("Params " + params_text);

	}

	public static void updateSolutionPopulation(int[] s, int[] params, String method) {
		// solution_population.add(new Solution(s, params, method));

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

		/*
		 * int worst = -1, temp_cost; int cost = Integer.MIN_VALUE;
		 * 
		 * for (int i = 0; i < solution_population.size(); i++) { temp_cost =
		 * qap.evalSolution(solution_population.get(i).getArray()); if (temp_cost >
		 * cost) { cost = temp_cost; worst = i; } }
		 */

		// solution_population.remove(worst);
		solution_population.add(new Solution(s, params, method));

	}

	public static void printSolutionPopulation(List<Solution> p) {

		for (int i = 0; i < p.size(); i++) {
			int[] temp_s = p.get(i).getArray();
			int temp_cost = qap.evalSolution(p.get(i).getArray());
			// System.out.println(p.get(i).getMethod());
			qap.printSolution(temp_s, temp_cost);
			// Tools.printArray(p.get(i).getParams());
			// qap.printSolution(temp_s, temp_cost);
		}

	}
}