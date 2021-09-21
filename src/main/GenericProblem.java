package main;

public class GenericProblem{

	final int size;
	final int bks;
	
	public GenericProblem(int size, int bks) {
		this.size = size;
		this.bks = bks;
	}

	public int getBKS() {
		return bks;
	}

	public int getSize() {
		return size;
	}

	// cost function
	public int evaluateSolution(int[] s) {
		return -1;
	}

	public int[] makeSwap(int[] permutation, int i, int j) {
		int temp;

		// change the values
		temp = permutation[i];
		permutation[i] = permutation[j];
		permutation[j] = temp;

		return permutation;
	}

}
