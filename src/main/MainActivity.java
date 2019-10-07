package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.GeneticAlgorithm.GeneticAlgorithm;
import main.GeneticAlgorithm.Results;

public class MainActivity {
	static int qap_size;
	static Random random;
	static final int MTLS = 0, ROTS = 1, EO = 2, GA = 3;

	public static void main(String[] args) {

		final String problem = "chr12a.dat";// args[0];

		System.out.println("\nProblema: " + problem);
		ReadFile readFile = new ReadFile("qapdata/" + problem);
		final long start = System.currentTimeMillis();

		int[][] flow, distance;
		// distance = Tools.getDataforDistance(problem);
		// flow = Tools.getDataForFlow(problem);
		distance = readFile.getDistance();
		flow = readFile.getFlow();

		// initialize qap data, i.e matrix of flow and distance matrix [row][col]
		QAPData qap = new QAPData(distance, flow);
		qap_size = qap.getSize();
		random = new Random(1);

		// List<Gene> generation = createFirstGeneration(8);
		final int number_by_mh = 4;
		Constructive constructive = new Constructive();
		List<List<Gene>> generation = generateInitialPopulation(number_by_mh, constructive);

		int generations = 1, count_generations = 0;

		Gene i1, i2;
		MultiStartLocalSearch mutiStartLocalSearch = new MultiStartLocalSearch();
		RobustTabuSearch robustTabuSearch = new RobustTabuSearch();
		ExtremalOptimization extremalOptimization = new ExtremalOptimization();
		GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();

		int[] s = new int[qap_size];
		int[] params = new int[4];

		while (count_generations < generations) {
			// System.out.println( count_generations );
			Gene bestGene;
			for (int i = 0; i < generation.size(); i++) {
				List<Gene> g = new ArrayList<>(generation.get(i));
				i1 = selectIndividual(g);
				i2 = selectIndividual(g);
				// crossover and mutation
				params = crossover(i1.getParams(), i2.getParams(), i); // crossover depending of method
				bestGene = i1;

				switch (i) {
				case MTLS:
					// System.out.println("MLTS");
					s = mutiStartLocalSearch.solve(bestGene.getSolution(), params, qap, constructive);
					break;
				case ROTS:
					// System.out.println("ROTS");
					s = robustTabuSearch.solve(bestGene.getSolution(), params, qap);
					break;
				case EO:
					// System.out.println("EO");
					s = extremalOptimization.solve(bestGene.getSolution(), params, qap);
					break;
				case GA:
					// System.out.println("GA");
					// pop_size, generations, mutation_probability, QAPData
					geneticAlgorithm.solve(params, qap);
					Results geneticAlgorithmResult = geneticAlgorithm.getResults();
					s = geneticAlgorithmResult.getBestIndividual().getGenes();
					break;
				}
				
				Gene newGene = new Gene(s, params, qap_size);

				//qap.printSolution(s, "MH " + i);
				//Tools.printArray(bestGene.chromosome);

			}

			count_generations++;
		}

		/*
		 * extremalOptimization.solve(totalIterations, initSolution, qap, -0.5); break;
		 * 
		 * geneticAlgorithm.solve(10 * qap.getSize(), 40 * qap.getSize(), 0.5, qap);
		 * 
		 * // get the results for the algorithm Results geneticAlgorithmResult =
		 * geneticAlgorithm.getResults(); // print the results, the best, the worst and
		 * the average population fitness
		 * System.out.println("\nEl mejor individuo es: ");
		 * geneticAlgorithmResult.getBestIndividual().printIndividualWithFitness(qap);
		 * bestSolutionFound = geneticAlgorithmResult.getBestIndividual().getGenes();
		 * System.out.println("El peor individuo es: ");
		 * geneticAlgorithmResult.getWorstIndividual().printIndividualWithFitness(qap);
		 * System.out.println("El valor promedio de la población es: " +
		 * geneticAlgorithmResult.getAvg_value()); break;
		 * 
		 */

		printTotalTime(start);

	}

	public static List<List<Gene>> generateInitialPopulation(int number_by_mh, Constructive constructive) {
		int[] s;

		List<List<Gene>> generation = new ArrayList<>(4); // because there are 4 different mh

		for (int k = 0; k < 4; k++) {
			List<Gene> g = new ArrayList<>(number_by_mh);
			for (int i = 0; i < number_by_mh; i++) {

				int[] p = { 0, 0, 0, 0 };

				switch (k) {
				case MTLS:
					p[0] = 40 * qap_size * (1 + i);// iterations
					break;
				case ROTS:
					p[0] = 40 * qap_size * (1 + i);// iterarions
					p[1] = k * (i + 1);// seed
					p[2] = 4 * (i + 1); // aspiration factor
					break;
				case EO:
					p[0] = 40 * qap_size * (1 + i);// iterarions
					p[1] = k * (i + 1);// seed
					p[2] = -random.nextInt(1000); // tau*1000
					break;
				case GA:
					p[0] = 10 * qap_size * (1 + i);// iterarions - generations
					p[1] = k * (i + 1);// seed
					p[2] = random.nextInt(1000); // mutation *1000
					p[3] = qap_size * (1 + i) / 2; // population size
					if (qap_size > 60) {
						p[3] = p[3] * 2 / 5;
					}
					break;
				}

				s = constructive.createRandomSolution(qap_size, k);// random.nextInt()
				g.add(new Gene(s, p, qap_size));
			}

			generation.add(g);
		}

		return generation;

	}

	public static Gene selectIndividual(List<Gene> g) {
		Gene selected;
		// obtain a number between 0 - size population
		int index = random.nextInt(g.size());
		selected = g.get(index);
		g.remove(index);// delete for no selecting later

		return selected;
	}

	public static int[] crossover(int[] params1, int[] params2, int mh_tyoe) {
		int[] p = { 0, 0, 0, 0 };
		switch (mh_tyoe) {
		case MTLS:
			p[0] = (params1[0] + params2[1]) / 2; // iterations
			break;
		case ROTS:
			p[0] = params1[0];// iterarions
			p[1] = params2[1];// seed
			p[2] = params2[2]; // aspiration factor
			break;
		case EO:
			p[0] = params1[0];// iterarions
			p[1] = params2[1];// seed
			p[2] = params2[2]; // tau*1000
			break;
		case GA:
			p[0] = params1[0];// iterarions - generations
			p[1] = params1[1];// seed
			p[2] = params2[2]; // mutation *1000
			p[3] = params2[3]; // population size
			break;
		}

		return p;
	}

	public static void printTotalTime(long start) {
		// show the total time
		double time = (System.currentTimeMillis() - start);
		time /= 1000;
		System.out.println("\n" + time + " seg");
	}
}