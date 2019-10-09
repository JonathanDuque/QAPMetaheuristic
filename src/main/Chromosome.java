package main;

import java.util.Arrays;

public class Chromosome {
	 int[] genes;
	private int qap_size;

	public Chromosome(int[] solution, final int[] params, int qap_size) {
		int size = qap_size + 3; //because maximum parameters are 3
		genes = new int[size];
		this.qap_size = qap_size;

		for (int i = 0; i < qap_size; i++) {
			genes[i] = solution[i];
		}
		// chromosome[qap_size] = metaheuristic;

		for (int i = qap_size; i < size; i++) {
			genes[i] = params[i - qap_size];
		}
		//Tools.printArray(genes);
	}

	public int[] getSolution() {
		return Arrays.copyOfRange(genes, 0, qap_size);
	}

	public int[] getParams() {
		return Arrays.copyOfRange(genes, qap_size, qap_size + 4);
	}

	//public Object clone() throws CloneNotSupportedException {
	//	return super.clone();
	//}
}
