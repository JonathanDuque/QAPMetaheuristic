package main;

public class Gene {
	int[] chromosome;

	public Gene(int[] solution, final int metaheuristic, final int[] params, int qap_size) {
		int size = qap_size + 5;
		chromosome = new int[size];
		for (int i = 0; i < qap_size; i++) {
			chromosome[i] = solution[i];
		}
		chromosome[qap_size] = metaheuristic;

		for (int i = qap_size + 1; i < size; i++) {
			chromosome[i] = params[i - qap_size - 1];
		}

		Tools.printArray(chromosome);
	}

	// int[] solution, params;
	// byte metaheuristic;
}
