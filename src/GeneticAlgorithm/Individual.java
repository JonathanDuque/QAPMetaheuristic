package GeneticAlgorithm;

import java.util.Arrays;

import QAPData;

public class Individual {
	private int[] genes;

	public Individual(int[] genes_array) {
		genes = genes_array;
	}

	public Individual(int amount) {
		genes = new int[amount];
		Arrays.fill(genes, -1);
	}

	public int[] getGenes() {
		return genes;
	}

	public int getGene(int position) {
		return genes[position];
	}

	public void setGene(int position, int value) {
		genes[position] = value;
	}
	
	public void printIndividual() {
		String individual = "";
		for (int i : genes) {
			individual = individual + i + " ";// +1 because the index in java start with 0
		}
		System.out.println(individual);
	}

	public void printIndividualWithFitness(QAPData qap) {
		String individual = "";
		for (int i : genes) {
			individual = individual + (i + 1) + " ";// +1 because the index in java start with 0
		}
		individual = individual + " -> Fitness: " + qap.evalSolution(genes);
		System.out.println(individual);

	}

}