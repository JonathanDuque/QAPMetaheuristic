package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class WorkerTeam extends GenericTeam {
	private static final long serialVersionUID = 1L;

	public WorkerTeam(int searchers, int iterationTime, int totalAdaptations, QAPData qap, int teamId,
			String parameter_setup, int requestPolicy, int entryPolicy, double solutionSimilarityPercertage) {
		super(searchers, iterationTime, totalAdaptations, qap, teamId, parameter_setup, requestPolicy, entryPolicy,
				solutionSimilarityPercertage);
	}

	@Override
	protected void compute() {
		initializeGlobalVariables();

		// the limits depends to the total iterations i.e total adaptations
		final double[] diversifyPercentageLimit = getDiversifyPercentageLimit(teamConfiguration.getTotalAdaptations());
		final int number_searchers_by_mh = teamConfiguration.getSearchers() / AlgorithmConfiguration.DIFFERENT_MH;

		// these lists and pool are necessary for the executing in parallel
		ForkJoinPool pool = new ForkJoinPool(teamConfiguration.getSearchers());
		List<MultiStartLocalSearch> list_mtls = new ArrayList<>(number_searchers_by_mh);
		List<RobustTabuSearch> list_rots = new ArrayList<>(number_searchers_by_mh);
		List<ExtremalOptimization> list_eo = new ArrayList<>(number_searchers_by_mh);

		parameterControls.generateInitialParamsPopulation(number_searchers_by_mh);
		int current_iteration = 0;

		while (current_iteration < teamConfiguration.getTotalAdaptations() && MainActivity.is_BKS_was_not_found()) {

			for (int i = 0; i < number_searchers_by_mh; i += 1) {
				MultiStartLocalSearch mtls = new MultiStartLocalSearch(qap, i * AlgorithmConfiguration.DIFFERENT_MH);
				RobustTabuSearch rots = new RobustTabuSearch(qap, i * AlgorithmConfiguration.DIFFERENT_MH + 1);
				ExtremalOptimization eo = new ExtremalOptimization(qap, i * AlgorithmConfiguration.DIFFERENT_MH + 2);

				list_mtls.add(mtls);
				list_rots.add(rots);
				list_eo.add(eo);
			}

			// setting environment variables for each method
			for (int i = 0; i < number_searchers_by_mh; i += 1) {
				Params paramsMTLS = parameterControls.selectParameter(AlgorithmConfiguration.MTLS, i);
				Params paramsROTS = parameterControls.selectParameter(AlgorithmConfiguration.ROTS, i);
				Params paramsEO = parameterControls.selectParameter(AlgorithmConfiguration.EO, i);

				list_mtls.get(i).setEnvironment(
						solutionPopulation.requestSolution(list_mtls.get(i).getMetaheuristicId()),
						paramsMTLS.getParams(), teamConfiguration.getIterationTime());
				list_rots.get(i).setEnvironment(
						solutionPopulation.requestSolution(list_rots.get(i).getMetaheuristicId()),
						paramsROTS.getParams(), teamConfiguration.getIterationTime());
				list_eo.get(i).setEnvironment(solutionPopulation.requestSolution(list_eo.get(i).getMetaheuristicId()),
						paramsEO.getParams(), teamConfiguration.getIterationTime());
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
						list_mtls.get(i).getBestSolution(AlgorithmConfiguration.mh_text[AlgorithmConfiguration.MTLS]),
						list_mtls.get(i).getMetaheuristicId(), qap);
				solutionPopulation.entrySolution(
						list_rots.get(i).getBestSolution(AlgorithmConfiguration.mh_text[AlgorithmConfiguration.ROTS]),
						list_rots.get(i).getMetaheuristicId(), qap);
				solutionPopulation.entrySolution(
						list_eo.get(i).getBestSolution(AlgorithmConfiguration.mh_text[AlgorithmConfiguration.EO]),
						list_eo.get(i).getMetaheuristicId(), qap);

				// create array parameters for each metaheuristic
				int[] params_MTLS = new int[3];
				int[] params_ROTS = new int[3];
				int[] params_EO = new int[3];

				// random parameters
				if (getParameterSetup() == MainActivity.setup_text[MainActivity.RANDOM]) {
					params_MTLS = parameterControls.createParameter(AlgorithmConfiguration.MTLS);
					params_ROTS = parameterControls.createParameter(AlgorithmConfiguration.ROTS);
					params_EO = parameterControls.createParameter(AlgorithmConfiguration.EO);
				} else if (getParameterSetup() == MainActivity.setup_text[MainActivity.ADAPTED]) {
					// init_cost, final cost order matter

					// with adaptations is necessary the behavior
					double[] perfomance_mtls = parameterControls
							.getPerfomanceEvaluation(list_mtls.get(i).getMetaheuricticReport());
					double[] perfomance_rots = parameterControls
							.getPerfomanceEvaluation(list_rots.get(i).getMetaheuricticReport());
					double[] perfomance_eo = parameterControls
							.getPerfomanceEvaluation(list_eo.get(i).getMetaheuricticReport());

					// with adaptations
					params_MTLS = parameterControls.adaptParameter(list_mtls.get(i).getParams(), perfomance_mtls,
							AlgorithmConfiguration.MTLS, current_iteration, teamConfiguration.getTotalAdaptations(),
							diversifyPercentageLimit);
					params_ROTS = parameterControls.adaptParameter(list_rots.get(i).getParams(), perfomance_rots,
							AlgorithmConfiguration.ROTS, current_iteration, teamConfiguration.getTotalAdaptations(),
							diversifyPercentageLimit);
					params_EO = parameterControls.adaptParameter(list_eo.get(i).getParams(), perfomance_eo,
							AlgorithmConfiguration.EO, current_iteration, teamConfiguration.getTotalAdaptations(),
							diversifyPercentageLimit);
				} else {
					// fixed parameter, same parameter always
					params_MTLS = list_mtls.get(i).getParams();
					params_ROTS = list_rots.get(i).getParams();
					params_EO = list_eo.get(i).getParams();
				}

				// insert the new parameters into parameters population, each one in the same
				// position
				parameterControls.insertParameter(AlgorithmConfiguration.MTLS, new Params(params_MTLS), i);
				parameterControls.insertParameter(AlgorithmConfiguration.ROTS, new Params(params_ROTS), i);
				parameterControls.insertParameter(AlgorithmConfiguration.EO, new Params(params_EO), i);
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
