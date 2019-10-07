package main;

import java.util.Arrays;

public class Gene /*implements Cloneable*/ {
	int[] chromosome;
	int qap_size;

	public Gene(int[] solution, final int[] params, int qap_size) {
		int size = qap_size + 4;
		chromosome = new int[size];
		this.qap_size = qap_size;

		for (int i = 0; i < qap_size; i++) {
			chromosome[i] = solution[i];
		}
		// chromosome[qap_size] = metaheuristic;

		for (int i = qap_size; i < size; i++) {
			chromosome[i] = params[i - qap_size];
		}
		//Tools.printArray(chromosome);
	}

	public int[] getSolution() {
		return Arrays.copyOfRange(chromosome, 0, qap_size);
	}

	public int[] getParams() {
		return Arrays.copyOfRange(chromosome, qap_size, qap_size + 4);
	}

	//public Object clone() throws CloneNotSupportedException {
	//	return super.clone();
	//}
}
