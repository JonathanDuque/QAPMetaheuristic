package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class WorkerTeam extends RecursiveAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private QAPData qap;
	private int qap_size;
	private int total_iterations;
	private final int workers;
	private List<Solution> solution_population;
	private final int MTLS = 0, ROTS = 1, EO = 2;
	final String[] mh_text = { "MTLS", "ROTS", "EO" };
	final int DIFFERENT_MH = 3;
	private ThreadLocalRandom thread_local_random;
	private int execution_time;// by iteration
	private final boolean dynamic_time;
	private final int team_id;
	private Params best_global_params;

	private Solution team_best_solution;

	public WorkerTeam(int workers, int execution_time, int total_iterations, QAPData qap, boolean dynamic_time,
			int team_id) {
		super();

		this.qap = qap;
		qap_size = qap.getSize();
		this.workers = workers;
		this.dynamic_time = dynamic_time;
		this.team_id = team_id;

		System.out.println("\nTeam: " + team_id);
		System.out.println("Threads: " + workers);

		if (dynamic_time) {
			/**** setting data for execution with dynamic times *******/
			int time_out = 300000; // 5 minutes, this data is necessary for some calculus
			this.execution_time = 1000; // this is the first value, after it changes through each iterations
			this.total_iterations = calculateTotalIterations2(time_out);
			System.out.println("Metaheuristic time grow through each iterations");
			System.out.println("Iterations: " + this.total_iterations);
			System.out.println("Time out: " + time_out / 1000.0 + " seconds");
		} else {
			this.execution_time = execution_time;
			this.total_iterations = total_iterations;
			System.out.println("Metaheuristic time: " + execution_time / 1000.0 + " seconds");
			System.out.println("Iterations: " + total_iterations);
			System.out.println("Time out: " + execution_time * total_iterations / 1000.0 + " seconds");
		}

	}

	@Override
	protected void compute() {
		// is necessary init here because in the constructor generate the same random
		// values for the params
		thread_local_random = ThreadLocalRandom.current();

		// int step_time = 1000;
		int current_time = 0, time_out = 300000; // 5 minutes

		// the limits depends to the total iterations
		final double[] diversify_percentage_limit = getDiversifyPercentageLimit(total_iterations);

		ForkJoinPool pool = new ForkJoinPool(workers);
		final Constructive constructive = new Constructive();
		final int number_workes_by_mh = workers / DIFFERENT_MH;

		// these lists are necessary for the executing in parallel
		List<MultiStartLocalSearch> list_mtls = new ArrayList<>(number_workes_by_mh);
		List<RobustTabuSearch> list_rots = new ArrayList<>(number_workes_by_mh);
		List<ExtremalOptimization> list_eo = new ArrayList<>(number_workes_by_mh);

		List<List<Params>> params_population = generateInitialParamsPopulation(number_workes_by_mh);
		best_global_params = new Params(params_population.get(0).get(0).getParams(),
				params_population.get(0).get(0).getFitness(), 0);

		List<List<Params>> best_mh_params_population = new ArrayList<>(DIFFERENT_MH);
		for (int k = 0; k < DIFFERENT_MH; k++) {
			best_mh_params_population.add(new ArrayList<>(params_population.get(k)));
		}

		Tools.printParamsPopulation(best_mh_params_population, team_id);
		solution_population = generateInitialSolutionPopulation(workers, constructive);
		// Tools.printSolutionPopulation(solution_population, qap, team_id);

		// create array parameters for each metaheuristic
		int[] params_MTLS = new int[3];
		int[] params_ROTS = new int[3];
		int[] params_EO = new int[3];

		int current_iteration = 0;

		while (current_iteration < total_iterations && MainActivity.is_BKS_was_not_found()) {

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

				best_global_params = updateBestGlobalParams(behavior_mtls, list_mtls.get(i).getParams(), behavior_rots,
						list_rots.get(i).getParams(), behavior_eo, list_eo.get(i).getParams());
				updateBestParamsByMH(best_mh_params_population, i, behavior_mtls, list_mtls.get(i).getParams(),
						behavior_rots, list_rots.get(i).getParams(), behavior_eo, list_eo.get(i).getParams());
				// System.out.println("Gain: " + best_global_params.getGain() + "\n");

				// params_MTLS = createParam(MTLS);
				// params_ROTS = createParam(ROTS);
				// params_EO = createParam(EO);

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

			/* updating times when is dynamic time */
			if (dynamic_time) {
				current_time += execution_time;
				execution_time = updateExecutionTime2(execution_time, current_time, time_out);
				/*
				 * execution_time += step_time; step_time += 1000;
				 *
				 * if (current_time + execution_time + step_time > time_out) { execution_time =
				 * time_out - current_time; }
				 */
			}

		}

		Tools.printParamsPopulation(best_mh_params_population, team_id);
		// Tools.printSolutionPopulation(solution_population, qap, team_id);

		// create and initiate variables for team results
		int[] best_solution = constructive.createRandomSolution(qap_size, current_iteration);
		int best_cost = qap.evalSolution(best_solution);
		final int[] empty_params = { -1, -1, -1 };
		team_best_solution = new Solution(best_solution, empty_params, "N/A");

		// update final results variables
		for (int i = 0; i < solution_population.size(); i++) {
			int temp_cost = qap.evalSolution(solution_population.get(i).getArray());

			if (temp_cost < best_cost) {
				best_cost = temp_cost;
				team_best_solution = solution_population.get(i);
			}
		}

	}

	public Solution getbestTeamSolution() {
		return team_best_solution;
	}

	public double[] getDiversifyPercentageLimit(int total_iterations) {
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

	public List<List<Params>> generateInitialParamsPopulation(int params_of_each_mh) {
		List<List<Params>> params_population = new ArrayList<>(DIFFERENT_MH); // because there are # DIFFERENT_MH

		for (int k = 0; k < DIFFERENT_MH; k++) {
			List<Params> temp_list_params = new ArrayList<>(params_of_each_mh);
			for (int i = 0; i < params_of_each_mh; i++) {
				int[] p = { 0, 0, 0 }; // parameters array empty

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
				temp_list_params.add(new Params(p, Integer.MAX_VALUE));
			}

			params_population.add(temp_list_params);
		}

		// Tools.printParamsPopulation(params_population, 2);
		return params_population;
	}

	public List<Solution> generateInitialSolutionPopulation(final int total, Constructive constructive) {
		List<Solution> init_solution_population = new ArrayList<>();
		final int[] empty_params = { -1, -1, -1 };

		for (int i = 0; i < total; i++) {
			int[] s = constructive.createRandomSolution(qap_size, i);// i stands for the seed
			init_solution_population.add(new Solution(s, empty_params, "N/A"));
		}

		return init_solution_population;
	}

	public Params selectParam(List<Params> p) {
		Params selected;
		// obtain a number between 0 - size population
		int index = thread_local_random.nextInt(p.size());
		selected = p.get(index);
		p.remove(index);// delete for no selecting later
		// System.out.println("selected : " + index);

		return selected;
	}

	public int[] getSolutionFromList(List<Solution> population) {
		Solution selected_solution;
		final int index = thread_local_random.nextInt(population.size());

		selected_solution = population.get(index);
		population.remove(index);// delete for no selecting later

		return selected_solution.getArray();
	}

	public double[] compareSolution(int init_cost, int final_cost, int[] init_solution, int[] final_solution) {
		// init_cost - 100%
		// init_cost-final_cost - x
		double difference_percentage = (init_cost - final_cost) * 100.0 / init_cost; // or gain

		int distance = 0;
		for (int i = 0; i < init_solution.length; i++) {
			if (init_solution[i] != final_solution[i]) {
				distance++;
			}
		}
		double[] comparison = { difference_percentage, distance };

		return comparison;
	}

	public int[] improveParameter(final int[] parameter, final double[] behavior_mh, final int type,
			final int current_iteration, final int total_iterations, final double[] diversify_percentage_limit) {

		final double[] change_pdf_percentage_limit = { 10, 5, 1, 0.5, 0.3 };
		final double divisor = (float) total_iterations / change_pdf_percentage_limit.length;
		int[] new_params = { 0, 0, 0 };

		// behavior_mh[0] = percentage difference or gain
		// behavior_mh[1] = distance

		// if (behavior_mh[0] > 0) {
		switch (type) {
		case MTLS:
			new_params = createParam(MTLS);
			break;
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
				new_params[0] = 4 * qap_size + thread_local_random.nextInt(16 * qap_size); // 4n to 20n
			}

			if (new_params[0] < 4 * qap_size) {
				new_params[0] = 4 * qap_size + thread_local_random.nextInt(16 * qap_size); // 4n to 20n
			}

			if (new_params[1] > 10 * qap_size * qap_size) {
				new_params[1] = qap_size * qap_size + thread_local_random.nextInt(9 * qap_size * qap_size); // n*n
																											// to
																											// 10*n*n
			}

			if (new_params[1] < qap_size * qap_size) {
				new_params[1] = qap_size * qap_size + thread_local_random.nextInt(9 * qap_size * qap_size); // n*n
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

			if (new_params[0] <= 0) {
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
		// } else {
		// if the solution did not improve, so will be assign a new parameter
		// new_params = createParam(type);
		// }

		return new_params;
	}

	// insert new parameter and remove the worst
	public void insertParameter(List<Params> listParams, Params new_params, final int type) {

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

	public void updateSolutionPopulation(int[] s, int[] params, String method) {

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

	public int[] createParam(int type) {

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

	private int[] mutate(int[] s) {
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

	public int calculateTotalIterations(int time_out) {
		int total_iterations = 0;
		int step_time = 1000, current_time = 0, execution_time = 1000;

		while (current_time < time_out) {
			current_time += execution_time;
			execution_time += step_time;
			step_time += 1000;

			if (current_time + execution_time + step_time > time_out) {
				execution_time = time_out - current_time;
			}

			total_iterations++;

		}

		return total_iterations;
	}

	public int calculateTotalIterations2(int time_out) {
		int total_iterations = 0;
		int current_time = 0, execution_time = 1000;

		while (current_time < time_out) {
			// System.out.println("\nexecution_time: " + execution_time);

			current_time += execution_time;

			if (execution_time < 30000) {
				execution_time *= 2;
			}

			if (current_time + 2 * execution_time > time_out) {
				execution_time = time_out - current_time;
			}

			total_iterations++;

		}

		return total_iterations;
	}

	public int updateExecutionTime2(int execution_time, final int current_time, final int time_out) {
		if (execution_time < 30000) {
			execution_time *= 2;
		}

		if (current_time + 2 * execution_time > time_out) {
			execution_time = time_out - current_time;
		}

		return execution_time;
	}

	public Params updateBestGlobalParams(double[] mtls, int[] params_mtls, double[] rots, int[] params_rots,
			double[] eo, int[] params_eo) {

		// behavior_mh[0] = gain
		// behavior_mh[1] = distance

		int method = -1;
		int[] best_params = { 0, 0, 0 };
		double best_gain = best_global_params.getGain();
		// System.out.println("Gain mtls: " + mtls[0]);
		// System.out.println("Gain rots: " + rots[0]);
		// System.out.println("Gain eo: " + eo[0]);

		if (mtls[0] > best_gain) {
			best_gain = mtls[0];
			method = MTLS;
			best_params = params_mtls.clone();
		}

		if (rots[0] > best_gain) {
			best_gain = rots[0];
			method = ROTS;
			best_params = params_rots.clone();
		}

		if (eo[0] > best_gain) {
			best_gain = eo[0];
			method = EO;
			best_params = params_eo.clone();
		}

		if (method == -1) {
			return best_global_params;
		} else {
			return new Params(best_params, 0, best_gain);
		}
	}

	public void adaptParameterPSO() {
		// function should be implemented
	}

	public void updateBestParamsByMH(List<List<Params>> best_mh_params_population, int mh_index, double[] mtls,
			int[] params_mtls, double[] rots, int[] params_rots, double[] eo, int[] params_eo) {

		// behavior_mh[0] = gain
		List<Params> best_list_params_MTLS = best_mh_params_population.get(MTLS);
		List<Params> best_list_params_ROTS = best_mh_params_population.get(ROTS);
		List<Params> best_list_params_EO = best_mh_params_population.get(EO);

		Params best_params_MTLS = best_list_params_MTLS.get(mh_index); // for this index
		Params best_params_ROTS = best_list_params_ROTS.get(mh_index); // for this index
		Params best_params_EO = best_list_params_EO.get(mh_index); // for this index

		if (best_params_MTLS.getGain() < mtls[0]) {
			best_params_MTLS.setParams(params_mtls);
			best_params_MTLS.setGain(mtls[0]);
			Tools.printArray(params_mtls);
			System.out.println("gain " + mtls[0]);
		}

		if (best_params_ROTS.getGain() < rots[0]) {
			best_params_ROTS.setParams(params_rots);
			best_params_ROTS.setGain(rots[0]);
			Tools.printArray(params_rots);
			System.out.println("gain " + rots[0]);
		}

		if (best_params_EO.getGain() < eo[0]) {
			best_params_EO.setParams(params_eo);
			best_params_EO.setGain(eo[0]);
			Tools.printArray(params_eo);
			System.out.println("gain " + eo[0]);
		}

		return;
	}
}
