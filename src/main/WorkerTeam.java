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
	int team_id;

	private Solution team_best_solution;

	public WorkerTeam(int workers, int execution_time, int total_iterations, QAPData qap, int team_id) {
		super();

		this.qap = qap;
		qap_size = qap.getSize();
		this.workers = workers;
		this.execution_time = execution_time;
		this.total_iterations = total_iterations;
		this.team_id = team_id;

		System.out.println("\nTeam: " + team_id);
		System.out.println("Threads: " + workers);
		System.out.println("Metaheuristic time: " + execution_time / 1000.0 + " seconds");
		System.out.println("Iterations: " + this.total_iterations);
		System.out.println("Time out: " + total_iterations * execution_time / 1000.0 + " seconds");
	}

	@Override
	protected void compute() {
		thread_local_random = ThreadLocalRandom.current();
		// TODO Auto-generated method stub
		ForkJoinPool pool = new ForkJoinPool(workers);
		final Constructive constructive = new Constructive();

		final int number_workes_by_mh = workers / DIFFERENT_MH;
		// final int params_of_each_mh = 3 * number_workes_by_mh;

		// these lists are necessary for the executing in parallel
		List<MultiStartLocalSearch> list_mtls = new ArrayList<>(number_workes_by_mh);
		List<RobustTabuSearch> list_rots = new ArrayList<>(number_workes_by_mh);
		List<ExtremalOptimization> list_eo = new ArrayList<>(number_workes_by_mh);

		// List<List<Params>> params_population_one =
		// generateInitialParamsPopulation(params_of_each_mh);
		List<List<Params>> params_population = generateInitialParamsPopulation(number_workes_by_mh);
		// Tools.printParamsPopulation(params_population, team_id);
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

				params_MTLS = selectParam(list_params_MTLS, i).getParams();
				params_ROTS = selectParam(list_params_ROST, i).getParams();
				params_EO = selectParam(list_params_EO, i).getParams();

				list_mtls.get(i).setEnvironment(getSolution(solution_population_copy), params_MTLS, execution_time);
				list_rots.get(i).setEnvironment(getSolution(solution_population_copy), params_ROTS, execution_time);
				list_eo.get(i).setEnvironment(getSolution(solution_population_copy), params_EO, execution_time);
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

				params_MTLS = list_mtls.get(i).getParams();
				params_ROTS = list_rots.get(i).getParams();
				params_EO = list_eo.get(i).getParams();
				// this version parameters don't improve

				// inserts solution into solution population
				updateSolutionPopulation(list_mtls.get(i).getSolution(), params_MTLS, mh_text[MTLS]);
				updateSolutionPopulation(list_rots.get(i).getSolution(), params_ROTS, mh_text[ROTS]);
				updateSolutionPopulation(list_eo.get(i).getSolution(), params_EO, mh_text[EO]);
			}

			// System.out.println("Solution Population");
			// printSolutionPopulation(solution_population);

			list_mtls.clear();
			list_rots.clear();
			list_eo.clear();

			current_iteration++;
		}
		// System.out.println("\nDespues" );
		// Tools.printParamsPopulation(params_population);
		// Tools.printSolutionPopulation(solution_population, qap, team_id);

		// create and initiate variables for results
		int[] best_solution = constructive.createRandomSolution(qap_size);
		int best_cost = qap.evalSolution(best_solution);
		int[] empty_params = { -1, -1, -1 };
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

	public Solution getBestTeamSolution() {
		return team_best_solution;
	}

	public List<List<Params>> generateInitialParamsPopulation(int params_of_each_mh) {
		List<List<Params>> params_population = new ArrayList<>(DIFFERENT_MH); // because there are # DIFFERENT_MH

		for (int k = 0; k < DIFFERENT_MH; k++) {
			List<Params> tempListParams = new ArrayList<>(params_of_each_mh);
			for (int i = 0; i < params_of_each_mh; i++) {
				int[] p = { 0, 0, 0 }; // params array

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

	public List<Solution> generateInitialSolutionPopulation(final int total, Constructive constructive) {
		List<Solution> init_solution_population = new ArrayList<>();
		final int[] empty_params = { -1, -1, -1 };

		for (int i = 0; i < total; i++) {
			int[] s = constructive.createRandomSolution(qap_size);
			init_solution_population.add(new Solution(s, empty_params, "N/A"));
		}

		return init_solution_population;
	}

	// select random parameter
	public Params selectParam(List<Params> p) {
		Params selected;
		// obtain a number between 0 - size population
		int index = thread_local_random.nextInt(p.size());
		selected = p.get(index);
		p.remove(index);// delete for no selecting later
		// System.out.println("selected : " + index);

		return selected;
	}

	// select always the same parameter
	public Params selectParam(List<Params> p, final int index) {
		Params selected = p.get(index);

		return selected;
	}

	public int[] getSolution(List<Solution> p) {
		Solution selected_solution;
		final int index = thread_local_random.nextInt(p.size());

		selected_solution = p.get(index);
		p.remove(index);// delete for no selecting later

		return selected_solution.getArray();
	}

	public void updateSolutionPopulation(int[] s, int[] params, String method) {
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

}
