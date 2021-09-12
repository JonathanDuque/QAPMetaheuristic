package main;

import java.util.Arrays;

public class RobustTabuSearch extends MetaheuristicSearch {
	private static final long serialVersionUID = 2L;

	private int[][] tabuMemory; // this matrix will save the iteration number where a location change is denied
	private int tabuDuration;// iterations tabu for a move
	private int aspiration;

	public RobustTabuSearch(QAPData qapData) {
		super(qapData);
	}

	@Override
	protected void compute() {
		// initSolution, initCost, qap, qapSize, threadLocalRandom, iterationTime,
		// and params are already initialized
		setBestCost(getInitCost());
		setBestSolution(Arrays.copyOf(getInitSolution(), getQapSize()));

		// this initial block define the local variable needed
		final int qap_size = getQapSize();
		tabuDuration = getParams()[0];
		aspiration = getParams()[1];
		initTabuMatrix(qap_size);

		int currentIteration = 1;
		int[] bestNeighbor, currentSolution;
		currentSolution = Arrays.copyOf(getInitSolution(), qap_size);
		int bestNeighborCost;

		qap.initDeltas(getInitSolution());

		final long start = System.currentTimeMillis();
		long time = 0;

		// execution this loop during iterationTime or until find bks
		// here find the best solution from the initSolution
		while (time - start < getIterationTime() && MainActivity.is_BKS_was_not_found()) {

			bestNeighbor = getBestNeighbor(currentSolution, currentIteration, getBestCost());
			bestNeighborCost = qap.evaluateSolution(bestNeighbor);

			// update the best solution found if is the best of the moment
			// at the end this block help to save the best of the best
			if (bestNeighborCost < getBestCost()) {
				setBestCost(bestNeighborCost);
				setBestSolution(Arrays.copyOf(bestNeighbor, qap_size));

				// if the new solution is the bks the MainActivity should be know
				if (getBestCost() == qap.getBKS()) {
					MainActivity.findBKS();
				}
			}

			// always update the current solution
			currentSolution = bestNeighbor;
			currentIteration++;
			time = System.currentTimeMillis();
		}
	}

	// this function simply give the solution no tabu with the minor cost,
	// unless it satisfies the aspiration criteria.
	public int[] getBestNeighbor(int[] currentSolution, int currentIteration, int bestCost) {
		int n = qap.getSize();
		// int[] selectedSolution;
		int i_selected = Integer.MAX_VALUE, j_selected = Integer.MAX_VALUE, maxDelta = Integer.MIN_VALUE;
		int currentCost = qap.evaluateSolution(currentSolution);

		boolean aspired = false, alreadyAspired = false, autorized = false;

		for (int i = 0; i < n - 1; i++) {
			for (int j = i + 1; j < n; j++) {

				int delta = qap.evalMovement(currentSolution, i, j);
				int newCost = currentCost - delta;

				// check if move is tabu
				autorized = (tabuMemory[i][currentSolution[j]] < currentIteration)
						|| (tabuMemory[j][currentSolution[i]] < currentIteration);

				aspired = (tabuMemory[i][currentSolution[j]] < currentIteration - aspiration)
						|| (tabuMemory[j][currentSolution[i]] < currentIteration - aspiration) || (newCost < bestCost);

				if ((aspired && !alreadyAspired) || /* first move aspired */
						(aspired && alreadyAspired && /* many move aspired */
								(delta >= maxDelta))
						|| /* => take best one */
						(!aspired && !alreadyAspired && /* no move aspired yet */
								(delta >= maxDelta) && autorized)) {

					i_selected = i;
					j_selected = j;
					maxDelta = delta;

					if (aspired) {
						alreadyAspired = true;
					}
				}
			}
		}

		currentSolution = qap.makeSwap(currentSolution, i_selected, j_selected);
		qap.updateDeltas(currentSolution, i_selected, j_selected);

		// update tabu matrix with values of the solution selected
		// give random decimal between 0 and 1
		int t1 = (int) (Math.pow(getThreadLocalRandom().nextDouble(), 3) * tabuDuration);
		int t2 = (int) (Math.pow(getThreadLocalRandom().nextDouble(), 3) * tabuDuration);

		// make tabu this facilities during certain iterations
		tabuMemory[i_selected][currentSolution[j_selected]] = currentIteration + t1;
		tabuMemory[j_selected][currentSolution[i_selected]] = currentIteration + t2;
		// Tools.printMatrix(tabuMemory, "Tabu Matriz");

		return currentSolution;

	}

	// init tabu matrix with 0
	public void initTabuMatrix(int size) {
		tabuMemory = new int[size][size];

		for (int[] row : tabuMemory) {
			Arrays.fill(row, 0);
		}
	}

	public boolean satisfyAspitarionCriteria(int actualCost, int bestCostFound) {
		return (actualCost < bestCostFound) ? true : false;
	}

}