package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class GenericTeam extends RecursiveAction {

	private static final long serialVersionUID = 1L;

	final int MTLS = 0, ROTS = 1, EO = 2;
	final String[] mh_text = { "MTLS", "ROTS", "EO" };
	final int DIFFERENT_MH = 3;
	private int[] not_improve = { 0, 0, 0 };
	private ThreadLocalRandom threadLocalRandom;

	QAPData qap;
	final int qap_size;
	private final int totalAdaptations;
	private final int searchers;
	private final int iterationTime;
	private final int teamId;
	private final boolean cooperative;
	private final String parameterSetup;

	List<Solution> solutionPopulation;
	private Solution bestTeamSolution;

	public GenericTeam(int searchers, int iterationTime, int totalAdaptations, QAPData qap, int teamId,
			boolean cooperative, String parameterSetup) {
		super();

		this.qap = qap;
		qap_size = qap.getSize();
		this.searchers = searchers;

		this.teamId = teamId;
		this.cooperative = cooperative;
		this.parameterSetup = parameterSetup;
		this.totalAdaptations = totalAdaptations;
		this.iterationTime = iterationTime;

		System.out.println("\nTeam: " + teamId);
		System.out.println("Threads: " + searchers);
		System.out.println("Metaheuristic time: " + iterationTime / 1000.0 + " seconds");
		System.out.println("Iterations: " + totalAdaptations);
		System.out.println("Time out: " + iterationTime * totalAdaptations / 1000.0 + " seconds");

	}

	public int getIterationTime() {
		return iterationTime;
	}

	public int getTotalAdaptations() {
		return totalAdaptations;
	}

	public int getSearchers() {
		return searchers;
	}

	public boolean isCooperative() {
		return cooperative;
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
		threadLocalRandom = ThreadLocalRandom.current();
	}

	@Override
	protected void compute() {
		// TODO Auto-generated method stub
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

	public int[] mutateSolution(int[] solution) {
		int posX, posY, temp;

		// first decide what value change randomly
		posX = threadLocalRandom.nextInt(qap_size);// with this value we put the range of number
		do {
			posY = threadLocalRandom.nextInt(qap_size);// check that the position to change are different
		} while (posX == posY);

		// swapping - making the mutation
		temp = solution[posX];
		solution[posX] = solution[posY];
		solution[posY] = temp;
		return solution;
	}

	public List<List<Params>> generateInitialParamsPopulation(int params_of_each_mh) {
		List<List<Params>> params_population = new ArrayList<>(DIFFERENT_MH); // because there are # DIFFERENT_MH

		for (int k = 0; k < DIFFERENT_MH; k++) {
			List<Params> temp_list_params = new ArrayList<>(params_of_each_mh);
			for (int i = 0; i < params_of_each_mh; i++) {
				int[] p = { 0, 0, 0 }; // parameters array empty

				switch (k) {
				case MTLS:
					p[0] = threadLocalRandom.nextInt(2); // restart type 0: random restart, 1: swaps
					break;
				case ROTS:
					p[0] = (threadLocalRandom.nextInt(16) + 4) * qap_size;// 4 * (i + 1) * qap_size;// tabu duration
																			// factor
					p[1] = (threadLocalRandom.nextInt(10) + 1) * qap_size * qap_size;// 2 * (i + 1) * qap_size *
																						// qap_size; //
					// aspiration factor
					break;
				case EO:
					p[0] = threadLocalRandom.nextInt(100); // tau*100
					p[1] = threadLocalRandom.nextInt(3); // pdf function type
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

	public int[] getSolutionFromList(List<Solution> population) {
		Solution selected_solution;
		final int index = threadLocalRandom.nextInt(population.size());

		selected_solution = population.get(index);
		population.remove(index);// delete for no selecting later

		return selected_solution.getArray();
	}

	public int[] getSolutionFromList(List<Solution> population, final int index) {
		Solution selected_solution;
		// System.out.println("selected : " + index);
		selected_solution = population.get(index);

		return selected_solution.getArray();
	}

	public void updateSolutionPopulation(int[] s, int[] params, String method) {

		boolean exist; // this cycle finish until the new solution will be different
		do {
			exist = false; // identify if the new solution is already in the population
			for (Solution temp : solutionPopulation) {
				if (Arrays.equals(temp.getArray(), s)) {
					exist = true;
					break;
				}
			} // if exist is necessary mutate
			if (exist) {
				s = mutateSolution(s);
			}

		} while (exist);

		solutionPopulation.add(new Solution(s, params, method));

	}

	public void updateSolutionPopulation(int[] new_solution, int[] params, String method, final int index,
			final int new_cost) {
		Solution last_solution = solutionPopulation.get(index);

		if (qap.evalSolution(last_solution.getArray()) >= new_cost) {
			solutionPopulation.set(index, new Solution(new_solution, params, method));// replace by the better
		}
	}

	public int[] createParameter(int type) {

		int[] p = { 0, 0, 0 };

		switch (type) {
		case MTLS:
			p[0] = threadLocalRandom.nextInt(2); // restart type 0: random restart, 1: swaps
			break;
		case ROTS:
			p[0] = threadLocalRandom.nextInt(16 * qap_size) + 4 * qap_size; // 4n to 20n
			p[1] = threadLocalRandom.nextInt(9 * qap_size * qap_size) + qap_size * qap_size; // n*n to 10*n*n
			// same range dokeroglu article
			break;
		case EO:
			p[0] = threadLocalRandom.nextInt(100); // tau*100
			p[1] = threadLocalRandom.nextInt(3); // pdf function type
			break;
		}
		return p;
	}

	// insert adapted parameter at the same position
	public void insertParameter(List<Params> listParams, Params new_params, final int type, final int index) {

		listParams.set(index, new_params);
	}

	public Params selectParameter(List<Params> p, final int index) {
		Params selected = p.get(index);

		return selected;
	}

	public int[] improveParameter(final int[] parameter, final double[] behavior_mh, final int type,
			final int current_iteration, final int totalAdaptations, final double[] diversify_percentage_limit) {

		final double[] change_pdf_percentage_limit = { 10, 5, 1, 0.5, 0.3 };
		final double divisor = (float) totalAdaptations / change_pdf_percentage_limit.length;
		int[] new_params = { 0, 0, 0 };

		// behavior_mh[0] = percentage difference or gain
		// behavior_mh[1] = distance

		// if (behavior_mh[0] > 0) {
		switch (type) {
		case MTLS:
			if (behavior_mh[0] > 0) {
				// case MTLS keep equal
				new_params[0] = parameter[0];
			} else {
				new_params = createParameter(MTLS);
			}
			break;

		case ROTS:
			if (behavior_mh[0] > 0 && behavior_mh[0] <= diversify_percentage_limit[current_iteration]
					&& behavior_mh[1] <= qap_size / 3) {
				// is necessary diversify
				new_params[0] = parameter[0] + Math.floorDiv(qap_size, 2);
				new_params[1] = parameter[1] + Math.floorDiv(qap_size * qap_size, 2);
			} else {
				// is necessary intensify
				new_params[0] = parameter[0] - Math.floorDiv(qap_size, 3);
				new_params[1] = parameter[1] - Math.floorDiv(qap_size, 2);
			}

			if (new_params[0] > 20 * qap_size) {
				new_params[0] = 4 * qap_size + threadLocalRandom.nextInt(16 * qap_size); // 4n to 20n
			}

			if (new_params[0] < 4 * qap_size) {
				new_params[0] = 4 * qap_size + threadLocalRandom.nextInt(16 * qap_size); // 4n to 20n
			}

			if (new_params[1] > 10 * qap_size * qap_size) {
				new_params[1] = qap_size * qap_size + threadLocalRandom.nextInt(9 * qap_size * qap_size); // n*n
																											// to
																											// 10*n*n
			}

			if (new_params[1] < qap_size * qap_size) {
				new_params[1] = qap_size * qap_size + threadLocalRandom.nextInt(9 * qap_size * qap_size); // n*n
																											// to
																											// 10*n*n
			}

			break;

		case EO:
			// parameter[0] : tau
			// parameter[1] : probability function

			if (behavior_mh[0] > 0 && behavior_mh[0] <= diversify_percentage_limit[current_iteration]
					&& behavior_mh[1] <= qap_size / 3) {
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
				new_params[0] = threadLocalRandom.nextInt(100); // tau*100
			}

			if (new_params[0] <= 0) {
				new_params[0] = threadLocalRandom.nextInt(100); // tau*100
			}

			if (behavior_mh[0] < change_pdf_percentage_limit[(int) Math.floor(current_iteration / divisor)]) {
				int new_pdf_function;
				do {
					new_pdf_function = threadLocalRandom.nextInt(3);
					new_params[1] = new_pdf_function;
				} while (parameter[1] == new_pdf_function);

			}
			break;
		}
		// } else {
		// if the solution did not improve, so will be assign a new parameter
		// new_params = createParam(type);
		// }
		// System.out.println( mh_text[type] + " Gain: " + behavior_mh[0] + " Counter:"
		// +not_improve[type]);

		if (behavior_mh[0] == 0) {
			not_improve[type]++;
		} else {
			// System.out.println("Gain " + behavior_mh[0] + " for " + mh_text[type]);
			not_improve[type] = 0;
		}

		if (not_improve[type] == 3) {
			// System.out.print("New parameter for " + mh_text[type] + "\n");
			new_params = createParameter(type);
			not_improve[type] = 0;
		}

		// System.out.println( mh_text[type] + " Gain: " +
		// Tools.DECIMAL_FORMAT_2D.format(behavior_mh[0] ) + " Counter:"
		// +not_improve[type]);

		return new_params;
	}

	// se puede sobreescribir
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
