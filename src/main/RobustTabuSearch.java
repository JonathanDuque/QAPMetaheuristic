package main;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class RobustTabuSearch extends RecursiveAction {
	private static final long serialVersionUID = 2L;
	private int[][] tabuMemory; // this matrix will save the iteration number where a location change is denied
	private int tabuDuration;// iterations tabu for a move
	private QAPData qap;
	private ThreadLocalRandom thread_local_random;
	private int aspiration;
	final int n;
	private int[] solution, init_solution;
	private int[] params;
	private int best_cost, init_cost;
	private int execution_time;

	public RobustTabuSearch(QAPData qapData) {
		super();
		thread_local_random = ThreadLocalRandom.current();
		
		this.qap = new QAPData(qapData.getDistance(), qapData.getFlow(), qapData.getBKS());
		n = qap.getSize();
	}

	// always before compute function, is necessary set the environment
	public void setEnvironment(int[] initSolution, int[] params, final int execution_time) {
		this.params = params.clone();
		this.init_solution = initSolution.clone();
		this.execution_time = execution_time;
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
	
	public int getBestCost() {
		return best_cost;
	}
	
	public int getInitCost() {
		return init_cost;
	}

	@Override
	protected void compute() {
		// this initial block define the variable needed
		tabuDuration = params[0];// 8*n, this 8 is a factor, is possible to change
		aspiration = params[1]; // 5 * n * n
		int currentIteration = 1;
		// int totalIterations = 1000;
		qap.initDeltas(init_solution);
		// qap.showData();

		initTabuMatrix(n);

		int[] bestNeighbor, currentSolution;
		currentSolution = Arrays.copyOf(init_solution, n);
		solution = Arrays.copyOf(init_solution, n);
		int bestNeighborCost;
		init_cost = qap.evalSolution(init_solution);
		best_cost = init_cost;
		// int bestFoundCounter = 0; // this counter has the value where the best was
		
		//System.out.println("ROTS");
		//Tools.printArray(currentSolution);

		final long start = System.currentTimeMillis();
		long time = 0;
		// this while find the best solution during totalIterations or until BKS will be found
		while (time - start < execution_time  && MainActivity.is_BKS_was_not_found()) {

			bestNeighbor = getBestNeighbor(currentSolution, currentIteration, best_cost);
			bestNeighborCost = qap.evalSolution(bestNeighbor);
			// qap.printSolution(bestNeighbor);

			// update the best solution found if is the best of the moment
			// at the end this block help to save the best of the best
			if (bestNeighborCost < best_cost) {
				solution = Arrays.copyOf(bestNeighbor, n);
				best_cost = bestNeighborCost;
				// bestFoundCounter = currentIteration;
				
				//if the new solution is the bks the MainActivity should be know
				if (best_cost == qap.getBKS()) {
					MainActivity.findBKS();
				}
			}

			// always update the current solution
			currentSolution = bestNeighbor;
			currentIteration++;
			time = System.currentTimeMillis();
		}
		
		//MainActivity.listCost.get(1).add(bestCost);

		//System.out.println("Fin ROTS");
		//System.out.println("ROTS : " + bestCost);
		//System.out.println("ROTS2 : " + qap.evalSolution(solution));
	}
	// this function simply give the solution no tabu with the minor cost,
	// unless it satisfies the aspiration criteria.
	public int[] getBestNeighbor(int[] currentSolution, int currentIteration, int bestCost) {
		int n = qap.getSize();
		// int[] selectedSolution;
		int i_selected = Integer.MAX_VALUE, j_selected = Integer.MAX_VALUE, maxDelta = Integer.MIN_VALUE;
		int currentCost = qap.evalSolution(currentSolution);

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
		int t1 = (int) (Math.pow(thread_local_random.nextDouble(), 3) * tabuDuration);
		int t2 = (int) (Math.pow(thread_local_random.nextDouble(), 3) * tabuDuration);

		// make tabu this facilities during certain iterations
		tabuMemory[i_selected][currentSolution[j_selected]] = currentIteration + t1;
		tabuMemory[j_selected][currentSolution[i_selected]] = currentIteration + t2;
		// Tools.printMatrix(tabuMemory, "Tabu Matriz");

		return currentSolution;

	}

	// init tabu matrix with 0
	public void initTabuMatrix(int size) {
		tabuMemory = new int[size][size];

		// for (int i = 0; i < size; i++)
		// for (int j = 0; j < size; j++)
		// tabuMemory[i][j] = -(size * i + j);

		for (int[] row : tabuMemory) {
			Arrays.fill(row, 0);
		}
	}

	public boolean satisfyAspitarionCriteria(int actualCost, int bestCostFound) {
		return (actualCost < bestCostFound) ? true : false;
	}

}