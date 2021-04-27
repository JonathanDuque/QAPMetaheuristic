package main;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class MultiStartLocalSearch extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	private ThreadLocalRandom thread_local_random;
	private final int n;
	
	QAPData qap;
	private int[] solution, init_solution;
	private int[] params;
	private String params_setup;
	private int best_cost, init_cost;
	int execution_time ;
	
	public MultiStartLocalSearch(QAPData qapData) {
		super();
		thread_local_random = ThreadLocalRandom.current();

		this.qap = new QAPData(qapData.getDistance(), qapData.getFlow(),qapData.getBKS());
		n = qap.getSize();
	}

	// always before compute function, is necessary set the environment
	public void setEnvironment(int[] initSolution, int[] params, String params_setup, final int execution_time) {
		this.params = params.clone();
		this.init_solution = initSolution.clone();
		this.execution_time = execution_time;
		this.params_setup = params_setup;
	}

	public int[] getInitSolution() {
		return init_solution;
	}

	public int[] getSolution() {
		return solution;
	}

	public int[] getParams() {
		return params;
	}
	
	public String getParamSetup() {
		return params_setup;
	}

	public int getBestCost() {
		return best_cost;
	}

	public int getInitCost() {
		return init_cost;
	}

	@Override
	protected void compute() {
		// this initial block define the variable needed
		solution = Arrays.copyOf(init_solution, n);
		int[] currentSolution = Arrays.copyOf(init_solution, n);
		boolean improve = false; // this flag control when the solution no improve and we are in an optimal local
		//int currentIteration = 0;
		int temporalDelta, bestDelta, cost = qap.evalSolution(init_solution);
		init_cost = cost;
		best_cost = cost;
		final boolean random_restart = params[0] == 0 ? true : false; // restart type 0: random restart, 1: swaps
		// System.out.println(params[0] + " " +random_restart);
		qap.initDeltas(init_solution);
		
		//System.out.println("\nMTLS init");
		//Tools.printArray(currentSolution);
		//Tools.printArray(params);

		final Constructive constructive = new Constructive();
		final long start = System.currentTimeMillis();
		long time = 0;

		// here find the best solution from the initSolution
		while (time - start < execution_time && MainActivity.is_BKS_was_not_found()) { // execution during execution_time or until find bks
			improve = false;
			bestDelta = 0;

			int i_selected = -1, j_selected = -1;
			// here evaluate all the neighborhood
			for (int i = 0; i < (n - 1); i++) {
				for (int j = i + 1; j < n; j++) {
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
				if (cost < best_cost) {
					best_cost = cost;
					solution = Arrays.copyOf(currentSolution, n);
					//if the new solution is the bks the MainActivity should be know
					if (best_cost == qap.getBKS()) {
						MainActivity.findBKS();
					}
				}

				// qap.printSolution(bestSolution);
				qap.updateDeltas(currentSolution, i_selected, j_selected);

			} else {
				improve = false;
				if (random_restart) {
					// start in a new point
					currentSolution = constructive.createRandomSolution(n, thread_local_random.nextInt());
				} else {
					// System.out.println("many swaps");
					currentSolution = makeManySwaps(currentSolution, qap);
				}

				qap.initDeltas(currentSolution);
				cost = qap.evalSolution(currentSolution);
			}
			//currentIteration++;
			time = System.currentTimeMillis();

		}
		
		//System.out.println(name + " " + currentIteration );
		//System.out.println("MTLS final");
		//Tools.printArray(solution);
	}

	public int[] makeManySwaps(int[] currentSolution, QAPData qap) {
		int max_swaps = Math.floorDiv(n, 2); // maybe n is not pair
	
		int num_swaps = 2 * (thread_local_random.nextInt(max_swaps - 1) + 1);
		int[] order_swaps = new int[num_swaps];

		order_swaps[0] = thread_local_random.nextInt(n);

		for (int i = 1; i < num_swaps; i++) {
			boolean isEqual = true;
			while (isEqual) {
				isEqual = false;

				int x = thread_local_random.nextInt(n);
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
		// Tools.printArray(currentSolution);
		// Tools.printArray(order_swaps);

		for (int i = 0; i < num_swaps; i += 2) {
			currentSolution = qap.makeSwap(currentSolution, order_swaps[i], order_swaps[i + 1]);
		}

		// Tools.printArray(currentSolution);

		return currentSolution;
	}
	
	

}
