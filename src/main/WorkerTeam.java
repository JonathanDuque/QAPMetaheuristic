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
			String parameter_setup, int requestPolicy, int entryPolicy) {
		super(searchers, iterationTime, totalAdaptations, qap, team_id, parameter_setup, requestPolicy, entryPolicy);
	}

	@Override
	protected void compute() {
		initializeGlobalVariables();

		// the limits depends to the total iterations i.e total adaptations
		final double[] diversify_percentage_limit = getDiversifyPercentageLimit(
				teamConfiguration.getTotalAdaptations());
		final int number_searchers_by_mh = teamConfiguration.getSearchers() / DIFFERENT_MH;

		// these lists and pool are necessary for the executing in parallel
		ForkJoinPool pool = new ForkJoinPool(teamConfiguration.getSearchers());
		List<MultiStartLocalSearch> list_mtls = new ArrayList<>(number_searchers_by_mh);
		List<RobustTabuSearch> list_rots = new ArrayList<>(number_searchers_by_mh);
		List<ExtremalOptimization> list_eo = new ArrayList<>(number_searchers_by_mh);
		List<List<Params>> params_population = generateInitialParamsPopulation(number_searchers_by_mh);

		// create array parameters for each metaheuristic
		int[] params_MTLS = new int[3];
		int[] params_ROTS = new int[3];
		int[] params_EO = new int[3];

		int current_iteration = 0;

		while (current_iteration < teamConfiguration.getTotalAdaptations() && MainActivity.is_BKS_was_not_found()) {

			for (int i = 0; i < number_searchers_by_mh; i += 1) {
				MultiStartLocalSearch mtls = new MultiStartLocalSearch(qap, i * DIFFERENT_MH);
				RobustTabuSearch rots = new RobustTabuSearch(qap, i * DIFFERENT_MH + 1);
				ExtremalOptimization eo = new ExtremalOptimization(qap, i * DIFFERENT_MH + 2);

				list_mtls.add(mtls);
				list_rots.add(rots);
				list_eo.add(eo);
			}

			List<Params> list_params_MTLS = new ArrayList<>(params_population.get(MTLS));
			List<Params> list_params_ROST = new ArrayList<>(params_population.get(ROTS));
			List<Params> list_params_EO = new ArrayList<>(params_population.get(EO));

			// setting environment variables for each method
			for (int i = 0; i < number_searchers_by_mh; i += 1) {
				params_MTLS = selectParameter(list_params_MTLS, i).getParams();
				params_ROTS = selectParameter(list_params_ROST, i).getParams();
				params_EO = selectParameter(list_params_EO, i).getParams();

				list_mtls.get(i).setEnvironment(
						solutionPopulation.requestSolution(list_mtls.get(i).getMetaheuristicId()), params_MTLS,
						teamConfiguration.getIterationTime());
				list_rots.get(i).setEnvironment(
						solutionPopulation.requestSolution(list_rots.get(i).getMetaheuristicId()), params_ROTS,
						teamConfiguration.getIterationTime());
				list_eo.get(i).setEnvironment(solutionPopulation.requestSolution(list_eo.get(i).getMetaheuristicId()),
						params_EO, teamConfiguration.getIterationTime());
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

			for (int i = 0; i < number_searchers_by_mh; i += 1) {
				// entry solution into solution population depending the entry policy
				solutionPopulation.entrySolution(
						new Solution(list_mtls.get(i).getBestSolution(), list_mtls.get(i).getBestCost(),
								list_mtls.get(i).getParams(), mh_text[MTLS]),
						list_mtls.get(i).getMetaheuristicId(), qap);
				solutionPopulation.entrySolution(
						new Solution(list_rots.get(i).getBestSolution(), list_rots.get(i).getBestCost(),
								list_rots.get(i).getParams(), mh_text[ROTS]),
						list_rots.get(i).getMetaheuristicId(), qap);
				solutionPopulation
						.entrySolution(
								new Solution(list_eo.get(i).getBestSolution(), list_eo.get(i).getBestCost(),
										list_eo.get(i).getParams(), mh_text[EO]),
								list_eo.get(i).getMetaheuristicId(), qap);

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

			}

			list_mtls.clear();
			list_rots.clear();
			list_eo.clear();

			current_iteration++;
		}

		// create and initiate variables for team results
		setBestTeamSolution(solutionPopulation.getBestSolution());
	}
}
