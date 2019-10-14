package main;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.RecursiveAction;

public class MultiStartLocalSearch extends RecursiveAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Random random;
	private final int n;
	QAPData qap;
	private int[] solution, initSolution;
	private int[] params;

	public MultiStartLocalSearch(QAPData qap, int seed) {
		super();
		this.random = new Random(seed);
		n = qap.getSize();
		try {
			this.qap = (QAPData) qap.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// always before compute function, is neccesary set the enviroment
	public void setEnviroment(int[] initSolution, int[] params) {
		this.params = params.clone();
		this.initSolution = initSolution.clone();
	}

	public int[] getSolution() {
		return solution;
	}

	@Override
	protected void compute() {
		// this initial block define the variable needed
		//System.out.println("MTLS");


		solution = Arrays.copyOf(initSolution, n);
		int[] currentSolution = Arrays.copyOf(initSolution, n);
		boolean improve = false; // this flag control when the solution no improve and we are in an optimo local
		int currentIteration = 0;
		int temporalDelta, bestDelta, cost = qap.evalSolution(initSolution), bestCost;
		bestCost = cost;
		final boolean random_restart = params[0] == 0 ? true : false; // restart type 0: random restart, 1: swaps
		qap.initDeltas(initSolution);
		final Constructive constructive = new Constructive();
		final long start = System.currentTimeMillis();
		long time = 0;

		// here find the best solution from the initSolution
		while (time - start < MainActivity.getExecutionTime()) { // execution during 10 milliseconds = 0.01 seconds
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
				if (cost < bestCost) {
					bestCost = cost;
					solution = Arrays.copyOf(currentSolution, n);
				}

				// qap.printSolution(bestSolution);
				qap.updateDeltas(currentSolution, i_selected, j_selected);

			} else {
				improve = false;
				if (random_restart) {
					// System.out.println("restart");
					// start in a new point
					currentSolution = constructive.createRandomSolution(n, MainActivity.getSeed());
				} else {
					// System.out.println("many swaps");
					currentSolution = makeManySwaps(currentSolution, qap);
				}

				qap.initDeltas(currentSolution);
				cost = qap.evalSolution(currentSolution);
			}
			currentIteration++;
			time = System.currentTimeMillis();

		}
		// System.out.println("MSLS : " + currentIteration);
		//System.out.println("Fin MTLS");
	}

	public int[] solve(int[] initSolution, int[] params, QAPData qap, Constructive constructive) {
		// this initial block define the variable needed

		int[] bestSolution = Arrays.copyOf(initSolution, n), currentSolution = Arrays.copyOf(initSolution, n);
		boolean improve = false; // this flag control when the solution no improve and we are in an optimo local
		int currentIteration = 0;
		int temporalDelta, bestDelta, cost = qap.evalSolution(initSolution), bestCost;
		bestCost = cost;
		final boolean random_restart = params[0] == 0 ? true : false; // restart type 0: random restart, 1: swaps
		// System.out.println(params[0] + " " +random_restart);
		qap.initDeltas(initSolution);
		// qap.showData();

		final long start = System.currentTimeMillis();
		long time = 0;
		// here find the best solution from the initSolution
		while (time - start < MainActivity.getExecutionTime()) { // execution during 10 milliseconds = 0.01 seconds
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
				if (cost < bestCost) {
					bestCost = cost;
					bestSolution = Arrays.copyOf(currentSolution, n);
				}

				// qap.printSolution(bestSolution);
				qap.updateDeltas(currentSolution, i_selected, j_selected);

			} else {
				improve = false;
				if (random_restart) {
					// System.out.println("restart");
					// start in a new point
					currentSolution = constructive.createRandomSolution(n, MainActivity.getSeed());
				} else {
					// System.out.println("many swaps");
					currentSolution = makeManySwaps(currentSolution, qap);
				}

				qap.initDeltas(currentSolution);
				cost = qap.evalSolution(currentSolution);
			}
			currentIteration++;
			time = System.currentTimeMillis();

		}
		// System.out.println("MSLS : " + currentIteration);

		return bestSolution;
	}

	public int[] makeManySwaps(int[] currentSolution, QAPData qap) {
		int max_swaps = Math.floorDiv(n, 2); // maybe n is no pair
		int num_swaps = 2 * (random.nextInt(max_swaps - 1) + 1);

		int[] order_swaps = new int[num_swaps];

		order_swaps[0] = random.nextInt(n);

		for (int i = 1; i < num_swaps; i++) {
			boolean isEqual = true;
			while (isEqual) {
				isEqual = false;
				int x = random.nextInt(n);
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
