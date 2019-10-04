package main.GeneticAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import main.QAPData;

public class GeneticAlgorithm {
	QAPData qap;
	int qap_size;
	Results results;
	Random random;

	public void solve(int pop_size, int generations, int seed, double mutation_probability, QAPData qapData) {
		// first the variables necessary for the execution
		Individual individual1, individual2, bestChild;
		List<Individual> new_generation = new ArrayList<>();
		List<Individual> temp_generation = new ArrayList<>();// temporal generation
		int count_generations = 0;
		qap = qapData;
		qap_size = qap.getSize();
		random = new Random(seed);

		// printing the parameters for the execution
		System.out.println("Tamaño de la población: " + pop_size);
		System.out.println("Número de generaciones: " + generations);
		System.out.println("Probabilidad de mutación: " + mutation_probability);

		// creating the first generation
		new_generation = createFirstGeneration(pop_size);
		Collections.sort(new_generation, compareByFitness);
		System.out.println("\nGeneración inicial");
		printPopulation(new_generation);

		// this cicle finish when complete all generations
		while (count_generations < generations) {
			temp_generation = new ArrayList<>(new_generation);

			for (int i = 0; i < pop_size / 2; i++) {

				individual1 = selectIndividual(temp_generation);// select a random individual
				individual2 = selectIndividual(temp_generation);
				bestChild = getBestOffspring(individual1, individual2, mutation_probability);

				new_generation.remove(pop_size - 1);// delete the last one
				// insert the best child and sure that is different, if not mutate until will be
				insertIndividualIntoPoblation(new_generation, bestChild);

				// order the population by fitness
				Collections.sort(new_generation, compareByFitness);

			}
			Collections.sort(new_generation, compareByFitness);
			count_generations++;
		}
		System.out.println("\nGeneración final");
		printPopulation(new_generation);
		// save the results
		results = populationResults(new_generation, pop_size);

	}

	private Individual getBestOffspring(Individual individual1, Individual individual2, double mp) {
		// mp = mutation_probability
		Random rand = new Random();
		// crossover from 1 until qap_size minus 1
		int point_crossover = rand.nextInt(qap_size - 1) + 1;

		// generate two offspring
		Individual child1 = crossover(individual1, individual2, point_crossover);
		Individual child2 = crossover(individual2, individual1, point_crossover);

		Individual childx = crossoverUX(individual1, individual2);

		//child1.printIndividual();

		// mutate two offspring
		child1 = mutate(child1, mp);
		child2 = mutate(child2, mp);

		// return the best by fitness
		//return getBestIndividual(child1, child2);
		return childx;
	}

	private Individual crossover(Individual father, Individual mother, int point_crossover) {

		// create individual empty
		Individual child = new Individual(qap_size);

		// fill with genes of father until point_crossover
		for (int i = 0; i < point_crossover; i++) {
			child.setGene(i, father.getGene(i));
		}

		// fill with genes of mother from point_crossover
		for (int i = point_crossover; i < qap_size; i++) {
			child.setGene(i, mother.getGene(i));
		}

		// fix individual if necessary
		return fixIndividual(child);
	}

	private Individual crossoverUX(Individual i1, Individual i2) {
		// create individual empty
		Individual child = new Individual(qap_size);

		//System.out.println("\nPadres");
		//i1.printIndividual();
		//i2.printIndividual();
		//child.printIndividual();

		for (int i = 0; i < qap_size; i++) {
			switch ((i + 1) % 4) {
			case 1:
				child.setGene(i, i1.getGene(i));
				break;
			case 2:
				child.setGene(i, i1.getGene(i));
				break;
			case 3:
				child.setGene(i, i2.getGene(i));
				break;
			case 0:
				child.setGene(i, i2.getGene(i));
				break;
			default:
				// code block
			}
		}

		//System.out.println("individuo");

		//child.printIndividual();

		return fixIndividual(child);

	}

	private Individual mutate(Individual individual, double mp) {
		double mutation_number = random.nextDouble();

		// checking if the individual will be mutate
		if (mutation_number <= mp) {
			int pos_geneX, pos_geneY, temp_gene;

			// first decide what genes change randonly
			pos_geneX = random.nextInt(qap_size);// with this value we put the range of number
			do {
				pos_geneY = random.nextInt(qap_size);// check that the position to change are diferent
			} while (pos_geneX == pos_geneY);

			// swapping - making the mutation
			temp_gene = individual.getGene(pos_geneX);
			individual.setGene(pos_geneX, individual.getGene(pos_geneY));
			individual.setGene(pos_geneY, temp_gene);
		}

		return individual;
	}

	private Individual fixIndividual(Individual individual) {
		List<Integer> missing_genes = new ArrayList<>();
		boolean missing = true;

		// this block identify facilities missing for fix
		for (int facility = 0; facility < qap_size; facility++) {
			for (int i = 0; i < qap_size; i++) {
				if (facility == individual.getGene(i)) {
					missing = false;
					break;
				}
			}

			if (missing) {
				missing_genes.add(facility);
			}
			missing = true;
		}

		// this block replace genes repeated for the missing
		for (int i = 0; i < qap_size; i++) {
			for (int j = 0; j < qap_size; j++) {
				if (i != j && individual.getGene(i) == individual.getGene(j)) {
					Integer tmp = missing_genes.get(missing_genes.size() - 1);
					individual.setGene(j, tmp);
					missing_genes.remove(tmp);
				}
			}
		}

		return individual;
	}

	private Individual selectIndividual(List<Individual> temp_generation) {
		Individual selected;

		// obtain a number between 0 - size population
		int index = random.nextInt(temp_generation.size());

		selected = temp_generation.get(index);
		temp_generation.remove(index);// delete for no selecting later

		return selected;
	}

	private Individual getBestIndividual(Individual individual1, Individual individual2) {
		if (qap.evalSolution(individual1.getGenes()) < qap.evalSolution(individual2.getGenes())) {
			return individual1;
		} else {
			return individual2;
		}
	}

	private void insertIndividualIntoPoblation(List<Individual> generation, Individual new_individual) {
		boolean exist = false;

		// this cicle finish until the new individual will be different
		do {
			exist = false;
			// identify if the new individual is already in the generation
			for (Individual temp : generation) {
				if (areIquals(temp, new_individual)) {
					exist = true;
					break;
				}
			}
			// if exist is neccesary mutate
			if (exist) {
				new_individual = mutate(new_individual, 1);// mutation_probability = 1
			} else {
				generation.add(new_individual);
			}

		} while (exist);
	}

	public Results getResults() {
		return results;
	}

	private Results populationResults(List<Individual> generation, int pop_size) {
		Individual bestIndividual = generation.get(0);
		Individual worstIndividual = generation.get(0);
		int avg_value;
		// double standdev = 0;
		int best_value = qap.evalSolution(bestIndividual.getGenes());
		int worst_value = best_value;

		int fitness_sum = 0;

		// this block save the best and the wort individual
		for (Individual ind : generation) {
			int fitness_val = qap.evalSolution(ind.getGenes());

			if (fitness_val < best_value) {
				bestIndividual = ind;
				best_value = fitness_val;
			}

			if (fitness_val > worst_value) {
				worstIndividual = ind;
				worst_value = fitness_val;
			}

			fitness_sum += fitness_val;
		}

		avg_value = fitness_sum / pop_size;

		// for (Individual ind : generation) {
		// int fitness_val = qap.evalSolution(ind.getGenes());
		// standdev += (fitness_val - avg_value) * (fitness_val - avg_value);
		// }

		// standdev /= pop_size;
		// standdev = Math.sqrt(standdev);

		return new Results(bestIndividual, worstIndividual, avg_value);
	}

	private boolean areIquals(Individual individual1, Individual individual2) {
		int[] array1 = individual1.getGenes().clone();
		int[] array2 = individual2.getGenes().clone();

		// check position by position if they are equals
		for (int i = 0; i < qap.getSize(); i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}

		return true;
	}

	private List<Individual> createFirstGeneration(int pop_size) {
		// create empty list
		List<Individual> start_generation = new ArrayList<>(pop_size);

		for (int i = 0; i < pop_size; i++) {
			// disorder the array
			// Collections.shuffle(seed);

			int[] new_genes = new int[qap_size];

			for (int k = 0; k < qap_size; k++) {
				new_genes[k] = random.nextInt(qap_size);;
			}

			//Individual  individual = mutate(new Individual(new_genes), 1);
			// introduce new different individual
			Individual  individual = fixIndividual(new Individual(new_genes));
			insertIndividualIntoPoblation(start_generation, individual);
		}
		return start_generation;

	}

	private void printPopulation(List<Individual> population) {
		for (int i = 0; i < population.size(); i++) {
			population.get(i).printIndividualWithFitness(qap);
		}
	}

	Comparator<Individual> compareByFitness = new Comparator<Individual>() {
		@Override
		public int compare(Individual i1, Individual i2) {
			return Integer.compare(qap.evalSolution(i1.getGenes()), qap.evalSolution(i2.getGenes()));
		}
	};
}
