package main;

import java.util.concurrent.RecursiveAction;

public class GenericTeam extends RecursiveAction {
	private static final long serialVersionUID = 1L;

	QAPData qap;
	final TeamConfiguration teamConfiguration;
	SolutionPopulation solutionPopulation;
	ParameterControl parameterControls;

	private Solution bestTeamSolution;

	private final String parameterSetup;

	public GenericTeam(int searchers, int iterationTime, int totalAdaptations, QAPData qap, int teamId,
			String parameterSetup, int requestPolicy, int entryPolicy, double solutionSimilarityPercertage) {
		super();

		this.qap = qap;
		this.parameterSetup = parameterSetup;

		teamConfiguration = new TeamConfiguration(totalAdaptations, searchers, teamId, iterationTime);
		teamConfiguration.printTeamConfiguration();
		solutionPopulation = new SolutionPopulation(requestPolicy, entryPolicy);
		parameterControls = new ParameterControl(qap.getSize(), solutionSimilarityPercertage);
	}

	public String getParameterSetup() {
		return parameterSetup;
	}

	public Solution getBestTeamSolution() {
		return bestTeamSolution;
	}

	public void setBestTeamSolution(Solution bestTeamSolution) {
		this.bestTeamSolution = bestTeamSolution;
	}

	public void initializeGlobalVariables() {
		// is necessary init here because in the constructor generate the same random
		// values for the params
		solutionPopulation.initSolutionPopulation(teamConfiguration.getSearchers(), qap);
		parameterControls.initThreadLocalRandom();
	}

	@Override
	protected void compute() {
		// TODO Auto-generated method stub
	}

	// TODO set is possible to override
	public double[] getDiversifyPercentageLimit(int totalAdaptations) {
		int total_values = 20;
		final double[] limits = new double[total_values];

		double a = 94.67597;
		double b = 0.31811;
		double c = 0.15699;

		// double a = 8.46744;
		// double b = 0.55246;
		// double c = 0.122;

		double y = 0;

		for (int x = 0; x < total_values; x++) {
			y = a * Math.exp(-b * (x + 1)) + c;
			// System.out.println("f(" + x + ") = " + (float) y);

			limits[x] = y;
		}

		double m = (double) total_values / totalAdaptations;

		// System.out.println("f(" + m + ") = " + (float) m);

		final double[] definitive_limits = new double[totalAdaptations];

		int index;

		for (int x = 0; x < totalAdaptations; x++) {
			index = (int) Math.round(m * x);
			definitive_limits[x] = limits[index];
			// System.out.println("f(" + x + ") = " + definitive_limits[x]);
		}

		return definitive_limits;
	}

}
