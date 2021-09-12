package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class WorkerTeam extends GenericTeam {
	private static final long serialVersionUID = 1L;

	public WorkerTeam(int searchers, int iterationTime, int totalAdaptations, QAPData qap, int team_id,
			boolean cooperative, String parameter_setup) {
		super(searchers, iterationTime, totalAdaptations, qap, team_id, cooperative, parameter_setup);
	}

	@Override
	protected void compute() {
		initializeGlobalVariables();

		// the limits depends to the total iterations
		final double[] diversify_percentage_limit = getDiversifyPercentageLimit(teamConfiguration.getTotalAdaptations());

		ForkJoinPool pool = new ForkJoinPool(teamConfiguration.getSearchers());
		final Constructive constructive = new Constructive();
		final int number_searchers_by_mh = teamConfiguration.getSearchers() / DIFFERENT_MH;

		// these lists are necessary for the executing in parallel
		List<MultiStartLocalSearch> list_mtls = new ArrayList<>(number_searchers_by_mh);
		List<RobustTabuSearch> list_rots = new ArrayList<>(number_searchers_by_mh);
		List<ExtremalOptimization> list_eo = new ArrayList<>(number_searchers_by_mh);

		List<List<Params>> params_population = generateInitialParamsPopulation(number_searchers_by_mh);
		solutionPopulation = generateInitialSolutionPopulation(teamConfiguration.getSearchers(), constructive);

		// create array parameters for each metaheuristic
		int[] params_MTLS = new int[3];
		int[] params_ROTS = new int[3];
		int[] params_EO = new int[3];

		int current_iteration = 0;

		while (current_iteration < teamConfiguration.getTotalAdaptations() && MainActivity.is_BKS_was_not_found()) {
			List<Solution> solution_population_copy = null;

			for (int i = 0; i < number_searchers_by_mh; i += 1) {
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

			// is necessary a copy because after solution population will be update, used
			// for cooperative version
			if (isCooperative()) {
				solution_population_copy = new ArrayList<>(solutionPopulation);
			}

			// setting environment variables for each method

			// non-cooperative select same solution and same parameter for each mh
			// cooperative select random solution, but same parameter for each mh
			for (int i = 0; i < number_searchers_by_mh; i += 1) {
				params_MTLS = selectParameter(list_params_MTLS, i).getParams();
				params_ROTS = selectParameter(list_params_ROST, i).getParams();
				params_EO = selectParameter(list_params_EO, i).getParams();

				if (isCooperative()) {
					list_mtls.get(i).setEnvironment(getSolutionFromList(solution_population_copy), params_MTLS,
							teamConfiguration.getIterationTime());
					list_rots.get(i).setEnvironment(getSolutionFromList(solution_population_copy), params_ROTS,
							teamConfiguration.getIterationTime());
					list_eo.get(i).setEnvironment(getSolutionFromList(solution_population_copy), params_EO,
							teamConfiguration.getIterationTime());
				} else {
					list_mtls.get(i).setEnvironment(getSolutionFromList(solutionPopulation, i * DIFFERENT_MH),
							params_MTLS, teamConfiguration.getIterationTime());
					list_rots.get(i).setEnvironment(getSolutionFromList(solutionPopulation, i * DIFFERENT_MH + 1),
							params_ROTS, teamConfiguration.getIterationTime());
					list_eo.get(i).setEnvironment(getSolutionFromList(solutionPopulation, i * DIFFERENT_MH + 2),
							params_EO, teamConfiguration.getIterationTime());

				}
			}

			// launch execution in parallel for all workers
			for (int i = 0; i < number_searchers_by_mh; i += 1) {
				pool.submit(list_mtls.get(i));
				pool.submit(list_rots.get(i));
				pool.submit(list_eo.get(i));
			}

			// wait for each method
			for (int i = 0; i < number_searchers_by_mh; i += 1) {
				list_mtls.get(i).join();
				list_rots.get(i).join();
				list_eo.get(i).join();
			}

			// for cooperative version
			if (isCooperative()) {
				solutionPopulation.clear();
			}

			for (int i = 0; i < number_searchers_by_mh; i += 1) {

				// random parameters
				if (getParameterSetup() == MainActivity.setup_text[MainActivity.RANDOM]) {
					params_MTLS = createParameter(MTLS);
					params_ROTS = createParameter(ROTS);
					params_EO = createParameter(EO);
				} else if (getParameterSetup() == MainActivity.setup_text[MainActivity.ADAPTED]) {
					// init_cost, final cost order matter

					// with adaptations is necessary the behavior
					double[] behavior_mtls = compareSolution(list_mtls.get(i).getInitCost(),
							list_mtls.get(i).getBestCost(), list_mtls.get(i).getInitSolution(),
							list_mtls.get(i).getBestSolution());
					double[] behavior_rots = compareSolution(list_rots.get(i).getInitCost(),
							list_rots.get(i).getBestCost(), list_rots.get(i).getInitSolution(),
							list_rots.get(i).getBestSolution());
					double[] behavior_eo = compareSolution(list_eo.get(i).getInitCost(), list_eo.get(i).getBestCost(),
							list_eo.get(i).getInitSolution(), list_eo.get(i).getBestSolution());

					// with adaptations
					params_MTLS = improveParameter(list_mtls.get(i).getParams(), behavior_mtls, MTLS, current_iteration,
							teamConfiguration.getTotalAdaptations(), diversify_percentage_limit);
					params_ROTS = improveParameter(list_rots.get(i).getParams(), behavior_rots, ROTS, current_iteration,
							teamConfiguration.getTotalAdaptations(), diversify_percentage_limit);
					params_EO = improveParameter(list_eo.get(i).getParams(), behavior_eo, EO, current_iteration,
							teamConfiguration.getTotalAdaptations(), diversify_percentage_limit);
				} else {
					// fixed parameter, same parameter always
					params_MTLS = list_mtls.get(i).getParams();
					params_ROTS = list_rots.get(i).getParams();
					params_EO = list_eo.get(i).getParams();
				}

				// insert the new parameters into parameters population, each one in the same
				// position
				insertParameter(params_population.get(MTLS), new Params(params_MTLS, list_mtls.get(i).getBestCost()),
						MTLS, i);
				insertParameter(params_population.get(ROTS), new Params(params_ROTS, list_rots.get(i).getBestCost()),
						ROTS, i);
				insertParameter(params_population.get(EO), new Params(params_EO, list_eo.get(i).getBestCost()), EO, i);

				// inserts solution into solution population
				if (isCooperative()) {
					updateSolutionPopulation(list_mtls.get(i).getBestSolution(), params_MTLS, mh_text[MTLS]);
					updateSolutionPopulation(list_rots.get(i).getBestSolution(), params_ROTS, mh_text[ROTS]);
					updateSolutionPopulation(list_eo.get(i).getBestSolution(), params_EO, mh_text[EO]);
				} else {
					updateSolutionPopulation(list_mtls.get(i).getBestSolution(), params_MTLS, mh_text[MTLS],
							i * DIFFERENT_MH, list_mtls.get(i).getBestCost());
					updateSolutionPopulation(list_rots.get(i).getBestSolution(), params_ROTS, mh_text[ROTS],
							i * DIFFERENT_MH + 1, list_rots.get(i).getBestCost());
					updateSolutionPopulation(list_eo.get(i).getBestSolution(), params_EO, mh_text[EO],
							i * DIFFERENT_MH + 2, list_eo.get(i).getBestCost());
				}

			}

			list_mtls.clear();
			list_rots.clear();
			list_eo.clear();

			current_iteration++;
		}

		// create and initiate variables for team results
		int[] best_solution = constructive.createRandomSolution(qap_size, current_iteration);
		int best_cost = qap.evaluateSolution(best_solution);
		final int[] empty_params = { -1, -1, -1 };
		setBestTeamSolution(new Solution(best_solution, empty_params, "N/A"));

		// update final results variables
		for (int i = 0; i < solutionPopulation.size(); i++) {
			int temp_cost = qap.evaluateSolution(solutionPopulation.get(i).getArray());

			if (temp_cost < best_cost) {
				best_cost = temp_cost;
				setBestTeamSolution(solutionPopulation.get(i));
			}
		}

	}

}
