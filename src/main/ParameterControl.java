package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ParameterControl {
	private ThreadLocalRandom threadLocalRandom;

	final int qap_size;
	final private double solutionSimilarityPercertage;
	List<List<Params>> listParameters;

	private int[] not_improve = { 0, 0, 0 }; // TODO redefine functionality, in that way is no working

	public ParameterControl(int qap_size, double solutionSimilarityPercertage) {
		super();
		this.solutionSimilarityPercertage = solutionSimilarityPercertage;
		this.qap_size = qap_size;
	}

	public void initThreadLocalRandom() {
		threadLocalRandom = ThreadLocalRandom.current();
	}

	public void generateInitialParamsPopulation(final int params_of_each_mh) {
		listParameters = new ArrayList<>(AlgorithmConfiguration.DIFFERENT_MH); // because there
																				// are #
																				// DIFFERENT_MH

		for (int k = 0; k < AlgorithmConfiguration.DIFFERENT_MH; k++) {
			List<Params> temp_list_params = new ArrayList<>(params_of_each_mh);
			for (int i = 0; i < params_of_each_mh; i++) {
				int[] p = { 0, 0, 0 }; // parameters array empty

				switch (k) {
				case AlgorithmConfiguration.MTLS:
					p[0] = threadLocalRandom.nextInt(2); // restart type 0: random restart, 1: swaps
					break;
				case AlgorithmConfiguration.ROTS:
					p[0] = (threadLocalRandom.nextInt(16) + 4) * qap_size;// tabu duration factor
					p[1] = (threadLocalRandom.nextInt(10) + 1) * qap_size * qap_size;// aspiration factor
					break;
				case AlgorithmConfiguration.EO:
					p[0] = threadLocalRandom.nextInt(100); // tau*100
					p[1] = threadLocalRandom.nextInt(3); // pdf function type
					break;
				}
				temp_list_params.add(new Params(p));
			}

			listParameters.add(temp_list_params);
		}

		// Tools.printParamsPopulation(params_population, 2);
	}

	public int[] createParameter(int type) {

		int[] p = { 0, 0, 0 };

		switch (type) {
		case AlgorithmConfiguration.MTLS:
			p[0] = threadLocalRandom.nextInt(2); // restart type 0: random restart, 1: swaps
			break;
		case AlgorithmConfiguration.ROTS:
			p[0] = threadLocalRandom.nextInt(16 * qap_size) + 4 * qap_size; // 4n to 20n
			p[1] = threadLocalRandom.nextInt(9 * qap_size * qap_size) + qap_size * qap_size; // n*n to 10*n*n
			// same range dokeroglu article
			break;
		case AlgorithmConfiguration.EO:
			p[0] = threadLocalRandom.nextInt(100); // tau*100
			p[1] = threadLocalRandom.nextInt(3); // pdf function type
			break;
		}
		return p;
	}

	// insert adapted parameter at the same position
	public void insertParameter(final int indexMetaheuristicType, Params new_params, final int index) {
		listParameters.get(indexMetaheuristicType).set(index, new_params);
	}

	public Params selectParameter(final int indexMetaheuristicType, final int index) {
		return listParameters.get(indexMetaheuristicType).get(index);
	}

	public int[] adaptParameter(final int[] parameter, final double[] behavior_mh, final int type,
			final int current_iteration, final int totalAdaptations, final double[] diversify_percentage_limit) {

		final double[] change_pdf_percentage_limit = { 10, 5, 1, 0.5, 0.3 };
		final double divisor = (float) totalAdaptations / change_pdf_percentage_limit.length;
		int[] new_params = { 0, 0, 0 };

		// behavior_mh[0] = percentage difference or gain
		// behavior_mh[1] = distance

		// if (behavior_mh[0] > 0) {
		switch (type) {
		case AlgorithmConfiguration.MTLS:
			if (behavior_mh[0] > 0) {
				// case MTLS keep equal
				new_params[0] = parameter[0];
			} else {
				new_params = createParameter(AlgorithmConfiguration.MTLS);
			}
			break;

		case AlgorithmConfiguration.ROTS:
			if (behavior_mh[0] > 0 && behavior_mh[0] <= diversify_percentage_limit[current_iteration]
					&& behavior_mh[1] <= solutionSimilarityPercertage) {
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

		case AlgorithmConfiguration.EO:
			// parameter[0] : tau
			// parameter[1] : probability function

			if (behavior_mh[0] > 0 && behavior_mh[0] <= diversify_percentage_limit[current_iteration]
					&& behavior_mh[1] <= solutionSimilarityPercertage) {
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

	public double[] getPerfomanceEvaluation(MetaheuricticReport report) {
		// init_cost - 100%
		// (init_cost-best_cost) - x

		double difference_percentage = (report.getInitCost() - report.getBestCost()) * 100.0 / report.getInitCost(); // or
																														// gain

		int initSolution[] = report.getInitSolution();
		int bestSolution[] = report.getBestSolution();
		int distance = 0;
		for (int i = 0; i < initSolution.length; i++) {
			if (initSolution[i] != bestSolution[i]) {
				distance++;
			}
		}
		double[] comparison = { difference_percentage, distance };

		return comparison;
	}
}
