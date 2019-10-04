package main;

public class Gene {
	int[] chromosome;

	public Gene(int[] solution, final int metaheuristic, int qap_size) {
		int size = qap_size + 1;
		chromosome = new int[size];
		for (int i = 0; i < qap_size; i++) {
			chromosome[i] = solution[i];
		}
		chromosome[qap_size] = metaheuristic;

		Tools.printArray(chromosome);
	}

	// int[] solution, params;
	// byte metaheuristic;
}
