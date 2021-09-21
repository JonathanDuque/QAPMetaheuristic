package main;

import java.util.concurrent.RecursiveAction;

public class GenericTeam extends RecursiveAction {
	private static final long serialVersionUID = 1L;

	QAPData qap;
	final TeamConfiguration teamConfiguration;
	SolutionPopulation solutionPopulation;
	ParameterControl parameterControls;
	private Solution bestTeamSolution;

	public GenericTeam(QAPData qap, int teamId, int searchers, int iterationTime, int totalAdaptations,
			int requestPolicy, int entryPolicy, int solutionSimilarityPercertage) {
		super();

		// is no possible to use this.qap = qap because is like assign the pointer to
		// qap for each team
		this.qap = new QAPData(qap.getDistance(), qap.getFlow(), qap.getBKS());
		teamConfiguration = new TeamConfiguration(totalAdaptations, searchers, teamId, iterationTime);
		teamConfiguration.printTeamConfiguration();
		solutionPopulation = new SolutionPopulation(requestPolicy, entryPolicy);
		parameterControls = new ParameterControl(qap.getSize(), solutionSimilarityPercertage);
	}

	public Solution getBestTeamSolution() {
		return bestTeamSolution;
	}

	public int getTeamId() {
		return teamConfiguration.getTeamId();
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
