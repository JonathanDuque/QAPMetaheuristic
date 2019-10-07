package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.GeneticAlgorithm.GeneticAlgorithm;

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

		int generations = 0, count_generations = 0;

		Gene g1, g2;
		MultiStartLocalSearch mutiStartLocalSearch = new MultiStartLocalSearch();
		RobustTabuSearch robustTabuSearch = new RobustTabuSearch();
		ExtremalOptimization extremalOptimization = new ExtremalOptimization();
		GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();

		while (count_generations < generations) {
			// System.out.println( count_generations );
			Gene bestGene;
			for (int i = 0; i < generation.size(); i++) {
				List<Gene> g = generation.get(i);
				g1 = selectIndividual(g);
				g2 = selectIndividual(g);
				// crossover and mutation
				bestGene = g1;

				switch (i) {
				case MTLS:
					// System.out.println("MLTS");
					// int[] s = mutiStartLocalSearch.solve(bestGene.getSolution(),
					// bestGene.getParams(), qap, constructive);
					break;
				case ROTS:
					// System.out.println("ROTS");
					// robustTabuSearch.solve(bestGene.getSolution(), bestGene.getParams(), qap);
					break;
				case EO:
					// System.out.println("EO");
					// extremalOptimization.solve(bestGene.getSolution(), bestGene.getParams(),
					// qap);
					break;
				case GA:
					// System.out.println("GA");
					// pop_size, generations, mutation_probability, QAPData
					// geneticAlgorithm.solve(bestGene.getParams(), qap);
					break;
				}

			}

			count_generations++;
		}

		// qap.printSolution(initSolution, "Solución inicial");

		/*
		 * 
		 * case 3: ExtremalOptimization extremalOptimization = new
		 * ExtremalOptimization(); bestSolutionFound =
		 * extremalOptimization.solve(totalIterations, initSolution, qap, -0.5); break;
		 * 
		 * case 4: GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(); //
		 * pop_size, generations, mutation_probability, QAPData
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
		 * }
		 */

		// qap.printSolution(bestSolutionFound, "\nMejor Solución");

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
					p[0] = 40 * qap_size * (1 + i);;// iterarions
					p[1] = k * (i + 1);// seed
					p[2] = -random.nextInt(1000); // tau*1000
					break;
				case GA: // Ga
					p[0] = 10 * qap_size * (1 + i);// iterarions - generations
					p[1] = k * (i + 1);// seed
					p[2] = random.nextInt(1000); // mutation *1000
					p[3] = qap_size * (1 + i); // population size
					break;
				}

				s = constructive.createRandomSolution(qap_size, random.nextInt());
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
		return selected;
	}

	/*
	 * public static List <Gene> createFirstGeneration( int pop_size) { final int
	 * MTLS = 0, ROTS = 1, EO = 2, GA = 3; List<Gene> generation = new
	 * ArrayList<>(pop_size);
	 * 
	 * Constructive constructive = new Constructive(); int[] s; for (int k = 0; k <
	 * pop_size; k++) { int[] p = { 0, 0, 0, 0 };
	 * 
	 * switch (k % 4) { case MTLS: p[0] = 100;// iterations break; case ROTS: p[0] =
	 * 1000;// iterarions p[1] = k;// seed p[2] = 8; // aspiration factor break;
	 * case EO: p[0] = 10000;// iterarions p[1] = k;// seed p[2] = 5; // tau*10
	 * break; case GA: // Ga p[0] = 10000;// iterarions - generations p[1] = k;//
	 * seed p[2] = 8; // mutation *10 p[3] = 50; // population size break; }
	 * 
	 * s = constructive.createRandomSolution(qap_size, k); generation.add(new
	 * Gene(s, k % 4, p, qap_size)); }
	 * 
	 * return generation; }
	 */

	public static void printTotalTime(long start) {
		// show the total time
		double time = (System.currentTimeMillis() - start);
		time /= 1000;
		System.out.println("\n" + time + " seg");
	}
}