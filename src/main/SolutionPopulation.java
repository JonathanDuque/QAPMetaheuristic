package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SolutionPopulation {
	final static int REQUEST_SAME = 0, REQUEST_RANDOM = 1, REQUEST_BEST = 2;
	final static int ENTRY_SAME = 0, ENTRY_ELITIST = 1, ENTRY_IF_DIFERENT = 2;

	private final int requestPolicy;
	private final int entryPolicy;
	List<Solution> listSolutions;

	public SolutionPopulation(int requestPolicy, int entryPolicy) {
		super();
		this.requestPolicy = requestPolicy;
		this.entryPolicy = entryPolicy;
	}

	public void initSolutionPopulation(final int total, QAPData qap) {
		final Constructive constructive = new Constructive();
		final int[] empty_params = { -1, -1, -1 };
		listSolutions = new ArrayList<>(total);

		for (int i = 0; i < total; i++) {
			int[] s = constructive.createRandomSolution(qap.size);
			listSolutions.add(new Solution(s, qap.evaluateSolution(s), empty_params, "N/A"));
		}
	}

	public int[] requestSolution(int metaheuristicId) {
		Solution selected_solution;
		if (requestPolicy == REQUEST_RANDOM) {
			selected_solution = requestRandomSolution(metaheuristicId);
		} else if (requestPolicy == REQUEST_BEST) {
			selected_solution = getBestSolution();
		} else { // REQUEST_SAME
			selected_solution = requestSameSolution(metaheuristicId);
		}

		return selected_solution.getArray();
	}

	public Solution requestSameSolution(final int metaheuricticId) {
		Solution selected_solution = listSolutions.get(metaheuricticId);
		selected_solution.setMetaheuricticId(metaheuricticId);
		return selected_solution;
	}

	public Solution requestRandomSolution(int metaheuristicId) {
		ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
		Solution selected_solution;
		int index;
		boolean solution_asigned = true;

		do {
			index = threadLocalRandom.nextInt(listSolutions.size());
			selected_solution = listSolutions.get(index);

			if (selected_solution.getMetaheuricticId() == Solution.NO_ID_METAHEURISTIC) {
				solution_asigned = false;
			}
		} while (solution_asigned);

		selected_solution.setMetaheuricticId(metaheuristicId);
		return selected_solution;
	}

	public void entrySolution(Solution solution, int metaheuristicId, QAPData qap) {

		if (entryPolicy == ENTRY_SAME) {
			entrySamePosition(solution, metaheuristicId);
		} else if (entryPolicy == ENTRY_IF_DIFERENT) {
			entryIfDifferent(solution, qap);
		} else {// ENTRY_ELITIST
			entryElitistPolicy(solution);
		}

	}

	public void entrySamePosition(Solution solution, final int index) {
		Solution last_solution = listSolutions.get(index);

		if (last_solution.getCost() >= solution.getCost()) {
			solution.setMetaheuricticId(Solution.NO_ID_METAHEURISTIC);
			listSolutions.set(index, solution);// replace by the better
		}
	}

	public void entryIfDifferent(Solution solution, QAPData qap) {
		boolean exist; // this cycle finish until the new solution will be different
		boolean mutated = false;
		int[] s = solution.getArray();

		do {
			exist = false; // identify if the new solution array is already in the population
			for (Solution temp : listSolutions) {
				if (Arrays.equals(temp.getArray(), s)) {
					exist = true;
					break;
				}
			} // if exist is necessary mutate
			if (exist) {
				s = mutateSolution(s);
				mutated = true;
			}

		} while (exist);

		// identify the worst and replace
		int position = 0;
		int worst_cost = listSolutions.get(position).getCost();

		for (int i = 0; i < listSolutions.size(); i++) {
			int temp_cost = listSolutions.get(i).getCost();

			if (temp_cost > worst_cost) { // if bigger is worst
				worst_cost = temp_cost;
				position = i;
			}
		}

		// if mutated calculate the new cost
		if (mutated) {
			int[] empty_params = { -1, -1, -1 };
			listSolutions.set(position, new Solution(s, qap.evaluateSolution(s), empty_params, "N/A"));
		} else {
			solution.setMetaheuricticId(Solution.NO_ID_METAHEURISTIC);
			listSolutions.set(position, solution);
		}

	}

	public void entryElitistPolicy(Solution solution) {
		int position = 0;
		int worst_cost = listSolutions.get(position).getCost();

		for (int i = 0; i < listSolutions.size(); i++) {
			int temp_cost = listSolutions.get(i).getCost();

			if (temp_cost > worst_cost) { // if bigger is worst
				worst_cost = temp_cost;
				position = i;
			}
		}

		if (worst_cost > solution.getCost()) {
			solution.setMetaheuricticId(Solution.NO_ID_METAHEURISTIC);
			listSolutions.set(position, solution);// replace by one better
		}
	}

	public int[] mutateSolution(int[] solution) {
		ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
		int posX, posY, temp;
		int qap_size = solution.length;

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

	public Solution getBestSolution() {
		Solution bestTeamSolution = listSolutions.get(0);
		int best_cost = bestTeamSolution.getCost();

		// get the better
		for (int i = 0; i < listSolutions.size(); i++) {
			int temp_cost = listSolutions.get(i).getCost();

			if (temp_cost < best_cost) {
				best_cost = temp_cost;
				bestTeamSolution = listSolutions.get(i);
			}
		}

		return bestTeamSolution;
	}

}
