package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.GeneticAlgorithm.GeneticAlgorithm;
import main.GeneticAlgorithm.Results;

public class MainActivity {
	private static int qap_size;
	private static Random random;
	private static final int MTLS = 0, ROTS = 1, EO = 2, GA = 3;
	private static QAPData qap;
	private static final int  execution_time = 10; 

	public static void main(String[] args) {
		final String problem = "chr18a.dat";// args[0];
		System.out.println("\nProblema: " + problem);
		final ReadFile readFile = new ReadFile("qapdata/" + problem);
		final long start = System.currentTimeMillis();

		// initialize qap data, i.e matrix of flow and distance matrix [row][col]
		final int[][] flow = readFile.getFlow(), distance = readFile.getDistance();
		qap = new QAPData(distance, flow);
		qap_size = qap.getSize();
		random = new Random(1);

		// List<Gene> generation = createFirstGeneration(8);
		final int number_by_mh = 4;
		final Constructive constructive = new Constructive();
		List<List<Chromosome>> generation = generateInitialPopulation(number_by_mh, constructive);

		int generations = 25, count_generations = 0;

		Chromosome c1, c2;
		MultiStartLocalSearch mutiStartLocalSearch = new MultiStartLocalSearch();
		RobustTabuSearch robustTabuSearch = new RobustTabuSearch();
		ExtremalOptimization extremalOptimization = new ExtremalOptimization();
		GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();

		int[] s = new int[qap_size];
		int[] params = new int[3];

		while (count_generations < generations) {
			// System.out.println( count_generations );
			Chromosome bestGene;
			for (int i = 0; i < generation.size(); i++) {
				// get a list of metaheuristics of the same type
				List<Chromosome> listChromosomes = new ArrayList<>(generation.get(i));

				
				if (i == -1) {
					System.out.println(i);
					for (int l = 0; l < listChromosomes.size(); l++) {
						Tools.printArray(listChromosomes.get(l).genes);
						System.out.println("costo " + qap.evalSolution(listChromosomes.get(l).getSolution()));
					}
				}

				c1 = selectIndividual(listChromosomes);
				c2 = selectIndividual(listChromosomes);
				// crossover and mutation
				params = crossover(c1.getParams(), c2.getParams(), i); // crossover depending of method
				params = mutate(params, i, 0.5);
				bestGene = c1;

				//long start1 = System.currentTimeMillis();
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
					geneticAlgorithm.solve(params, qap);
					Results geneticAlgorithmResult = geneticAlgorithm.getResults();
					s = geneticAlgorithmResult.getBestIndividual().getGenes();
					break;
				}
				
				//printTotalTime(start1);

				// insert new individual into generation
				Chromosome newGene = new Chromosome(s, params, qap_size);
				insertIndividual(generation.get(i), newGene);
				
				
			}

			count_generations++;
		}

		printTotalTime(start);

	}

	public static List<List<Chromosome>> generateInitialPopulation(int number_by_mh, Constructive constructive) {

		List<List<Chromosome>> generation = new ArrayList<>(4); // because there are 4 different mh

		for (int k = 0; k < 4; k++) {
			List<Chromosome> g = new ArrayList<>(number_by_mh);
			int[] s;
			s = constructive.createRandomSolution(qap_size, k);// random.nextInt()
			for (int i = 0; i < number_by_mh; i++) {

				int[] p = { 0, 0, 0 };

				switch (k) {
				case MTLS:
					p[0] = random.nextInt(2); // restart type
					break;
				case ROTS:
					p[0] = (random.nextInt(16)+4)*qap_size;//4 * (i + 1) * qap_size;// tabu duration factor
					p[1] = (random.nextInt(10)+1)*qap_size*qap_size;//2 * (i + 1) * qap_size * qap_size; // aspiration factor
					break;
				case EO:
					p[0] = -random.nextInt(1000); // tau*1000
					p[1] = random.nextInt(3); // pdf function type
					break;
				case GA:
					p[0] = qap_size + qap_size * random.nextInt(5) / 2;// population size
					if (qap_size > 60) {
						p[0] = p[0] * 2 / 5;
					}
					p[1] = random.nextInt(1000); // mutation *1000
					p[2] = random.nextInt(2);// crossover operator type

					break;
				}
				g.add(new Chromosome(s, p, qap_size));
			}

			generation.add(g);
		}

		return generation;

	}

	public static Chromosome selectIndividual(List<Chromosome> g) {
		Chromosome selected;
		// obtain a number between 0 - size population
		int index = random.nextInt(g.size());
		selected = g.get(index);
		g.remove(index);// delete for no selecting later
		//System.out.println("selected : " + index);

		return selected;
	}

	public static int[] crossover(int[] params1, int[] params2, int mh_type) {
		int[] p = { 0, 0, 0 };
		switch (mh_type) {
		case MTLS:
			p[0] = params1[0]; // restart type
			break;
		case ROTS:
			p[0] = params1[0];// tabu duration factor
			p[1] = params2[1]; // aspiration factor
			break;
		case EO:
			p[0] = params1[0];// tau*1000
			p[1] = params2[1];// type pdf
			break;
		case GA:
			p[0] = params1[0];// population size
			p[1] = params2[1];// mutation *1000
			p[2] = params2[2]; // crossover type
			break;
		}

		//Tools.printArray(p);

		return p;
	}

	public static int[] mutate(int[] params, int mh_type, double mp) {
		double mutation_number = random.nextDouble();

		int[] p = params.clone();
		if (mutation_number <= mp) {
			final int param;
			switch (mh_type) {
			case MTLS:
				p[0] = random.nextInt(2); // restart type
				break;
			case ROTS:
				// getting what parameter to mutate
				param = random.nextInt(2);
				//System.out.println("param:" +param);
				switch (param) {
				case 0:
					p[param] = random.nextInt(16 * qap_size ) + 4*qap_size ; //4n to 20n
					break;
				case 1:
					p[param] = random.nextInt(9 * qap_size * qap_size) + qap_size * qap_size; //n*n to 10*n*n same range dokeroglu article
					break;
				}
				break;
			case EO:
				param = random.nextInt(2);
				//System.out.println("param:" +param);
				switch (param) {
				case 0:
					p[param] = -random.nextInt(1000); // tau*1000
					break;
				case 1:
					p[param] = random.nextInt(3); // pdf function type
					break;
				}

				break;
			case GA:
				param = random.nextInt(3);
				//System.out.println("param:" +param);
				switch (param) {
				case 0:
					p[param] = qap_size + qap_size * random.nextInt(5) / 2;// population size
					if (qap_size > 60) {
						p[param] = p[param] * 2 / 5;
					}
					break;
				case 1:
					p[param] = random.nextInt(1000); // mutation *1000
					break;
				case 2:
					p[param] = random.nextInt(2);// crossover operator type
					break;
				}
				break;
			}
		}
		
		//Tools.printArray(p);
		return p;

	}

	public static void insertIndividual(List<Chromosome> g, Chromosome newIndividual) {

		int worst = -1;
		int cost = Integer.MIN_VALUE;
		int temp_cost = 0;
		for (int i = 0; i < g.size(); i++) {
			temp_cost = qap.evalSolution(g.get(i).getSolution());
			if (temp_cost > cost) {
				cost = temp_cost;
				worst = i;
			}
		}
		g.remove(worst);
		g.add(newIndividual);
	}

	public static void printTotalTime(long start) {
		// show the total time
		double time = (System.currentTimeMillis() - start);
		time /= 1000;
		System.out.println("\n" + time + " seg");
	}

	public static int getSeed() {
		return random.nextInt();
	}

	public static int getExecutionTime() {
		return execution_time;
	}

	/*
	 * // get init solution with constructive method Constructive constructive = new
	 * Constructive(); int[] initSolution =
	 * constructive.createRandomSolution(qap.getSize(), 1); int[] bestSolutionFound
	 * = initSolution;// for now this is the best solution
	 * qap.printSolution(initSolution, "Solución inicial");
	 * 
	 * int[] params = {380,1,500,80};
	 * 
	 * int method = 4;// showMenuMethod(); switch (method) { case 1:
	 * MultiStartLocalSearch mutiStartLocalSearch = new MultiStartLocalSearch();
	 * bestSolutionFound = mutiStartLocalSearch.solve( initSolution, params,qap,
	 * constructive); break;
	 * 
	 * case 2: RobustTabuSearch robustTabuSearch = new RobustTabuSearch();
	 * bestSolutionFound = robustTabuSearch.solve( initSolution,params, qap); break;
	 * 
	 * case 3: ExtremalOptimization extremalOptimization = new
	 * ExtremalOptimization(); bestSolutionFound = extremalOptimization.solve(
	 * initSolution,params, qap); break;
	 * 
	 * case 4: GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(); //
	 * pop_size, generations, mutation_probability, QAPData
	 * geneticAlgorithm.solve(params, qap);
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
	 * 
	 * qap.printSolution(bestSolutionFound, "\nMejor Solución");
	 */

}