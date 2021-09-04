package main;

import java.util.Arrays;

public class MultiStartLocalSearch extends MetaheuristicSearch {

	private static final long serialVersionUID = 1L;

	public MultiStartLocalSearch(QAPData qapData) {
		super(qapData);
	}

	@Override
	protected void compute() {
		// this initial block define the variable needed
		final int qap_size = getQapSize();
		setBestSolution(Arrays.copyOf(getInitSolution(), qap_size));
		int[] currentSolution = Arrays.copyOf(getInitSolution(), qap_size);
		int temporalDelta, bestDelta, cost = qap.evalSolution(getInitSolution());
		setInitCost(cost);
		setBestCost(cost);

		final boolean random_restart = getParams()[0] == 0 ? true : false; // restart type 0: random restart, 1: swaps
		qap.initDeltas(getInitSolution());

		final Constructive constructive = new Constructive();
		boolean improve = false; // this flag control when the solution no improve and we are in an optimal local
		final long start = System.currentTimeMillis();
		long time = 0;

		// here find the best solution from the initSolution
		while (time - start < getExecutionTime() && MainActivity.is_BKS_was_not_found()) { // execution during
																							// execution_time or until
																							// find bks
			improve = false;
			bestDelta = 0;
			int i_selected = -1, j_selected = -1;

			// here evaluate all the neighborhood
			for (int i = 0; i < (qap_size - 1); i++) {
				for (int j = i + 1; j < qap_size; j++) {
					temporalDelta = qap.evalMovement(currentSolution, i, j);

					// if improve
					if (temporalDelta > bestDelta) {
						i_selected = i;
						j_selected = j;
						bestDelta = temporalDelta;
						improve = true;
					}
				}
			}

			if (improve) {
				cost -= bestDelta;
				currentSolution = qap.makeSwap(currentSolution, i_selected, j_selected);
				if (cost < getBestCost()) {
					setBestCost(cost);
					setBestSolution(Arrays.copyOf(currentSolution, qap_size));

					// if the new solution is the bks the MainActivity should be know
					if (getBestCost() == qap.getBKS()) {
						MainActivity.findBKS();
					}
				}

				qap.updateDeltas(currentSolution, i_selected, j_selected);
			} else {
				improve = false;
				if (random_restart) {
					// start in a new point
					currentSolution = constructive.createRandomSolution(qap_size, getThreadLocalRandom().nextInt());
				} else {
					currentSolution = makeManySwaps(currentSolution, qap);
				}

				qap.initDeltas(currentSolution);
				cost = qap.evalSolution(currentSolution);
			}

			time = System.currentTimeMillis();
		}

	}

	public int[] makeManySwaps(int[] currentSolution, QAPData qap) {
		int max_swaps = Math.floorDiv(getQapSize(), 2); // maybe n is not pair

		int num_swaps = 2 * (getThreadLocalRandom().nextInt(max_swaps - 1) + 1);
		int[] order_swaps = new int[num_swaps];

		order_swaps[0] = getThreadLocalRandom().nextInt(getQapSize());

		for (int i = 1; i < num_swaps; i++) {
			boolean isEqual = true;
			while (isEqual) {
				isEqual = false;

				int x = getThreadLocalRandom().nextInt(getQapSize());
				for (int j = 0; j < i; j++) {
					if (x == order_swaps[j]) {
						isEqual = true;
						break;
					}
				}
				if (!isEqual) {
					order_swaps[i] = x;
				}
			}
		}

		for (int i = 0; i < num_swaps; i += 2) {
			currentSolution = qap.makeSwap(currentSolution, order_swaps[i], order_swaps[i + 1]);
		}

		return currentSolution;
	}

}
