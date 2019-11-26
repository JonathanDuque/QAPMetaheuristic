package main;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

import main.GeneticAlgorithm.GeneticAlgorithm;
import main.GeneticAlgorithm.Results;

public class MainActivity {
	private static int qap_size;
	private static Random random;
	private static final int MTLS = 0, ROTS = 1, EO = 2, GA = 3;
	private static QAPData qap;
	private static final int execution_time = 1000;
	private static boolean no_find_BKS = true;
	static List<List<Integer>> listCost = new ArrayList<>(4);// 4 mh

	public static void main(String[] args) {
		final String problem = args[0]; // "bur26a.qap";
		System.out.println("\n*****************    Problem: " + problem + "    ********************************");
		final ReadFile readFile = new ReadFile("Data/" + problem);
		final long start = System.currentTimeMillis();

		// initialize qap data, i.e matrix of flow and distance matrix [row][col]
		final int[][] flow = readFile.getFlow(), distance = readFile.getDistance();
		qap = new QAPData(distance, flow, readFile.getTarget());
		qap_size = qap.getSize();
		random = new Random(1);

		// int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(4);

		// List<Gene> generation = createFirstGeneration(8);
		final int number_by_mh = 5;
		final Constructive constructive = new Constructive();
		List<List<Chromosome>> generation = generateInitialPopulation(number_by_mh, constructive);

		int generations = 20, count_generations = 0;

		int[] s = new int[qap_size];

		// create chromosome for each mh
		Chromosome c_MTLS_1, c_MTLS_2;
		Chromosome c_ROTS_1, c_ROTS_2;
		Chromosome c_EO_1, c_EO_2;
		Chromosome c_GA_1, c_GA_2;

		// create params for each mh
		int[] paramsMTLS = new int[3];
		int[] paramsROTS = new int[3];
		int[] paramsEO = new int[3];
		int[] paramsGA = new int[3];
		boolean sequential = false;

		for (int i=0; i<4; i++){
			int initCost = qap.evalSolution((generation.get(i).get(0).getSolution()));
			//System.out.println(""+initCost);
			List<Integer> cost_by_mh = new ArrayList<>();
			cost_by_mh.add(initCost);
			listCost.add(cost_by_mh);
		}
		
		
		while (count_generations < generations && no_find_BKS) {

			MultiStartLocalSearch mutiStartLocalSearch = new MultiStartLocalSearch(qap, random.nextInt());
			RobustTabuSearch robustTabuSearch = new RobustTabuSearch(qap, random.nextInt());
			ExtremalOptimization extremalOptimization = new ExtremalOptimization(qap, random.nextInt());
			GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(qap, random.nextInt());

			// System.out.println( count_generations );
			List<Chromosome> listMTLS = new ArrayList<>(generation.get(0));
			List<Chromosome> listROST = new ArrayList<>(generation.get(1));
			List<Chromosome> listEO = new ArrayList<>(generation.get(2));
			List<Chromosome> listGA = new ArrayList<>(generation.get(3));

			c_MTLS_1 = selectIndividual(listMTLS);
			c_MTLS_2 = selectIndividual(listMTLS);

			c_ROTS_1 = selectIndividual(listROST);
			c_ROTS_2 = selectIndividual(listROST);

			c_EO_1 = selectIndividual(listEO);
			c_EO_2 = selectIndividual(listEO);

			c_GA_1 = selectIndividual(listGA);
			c_GA_2 = selectIndividual(listGA);

			// crossover and mutation
			paramsMTLS = crossover(c_MTLS_1.getParams(), c_MTLS_2.getParams(), MTLS); // crossover depending of method
			paramsMTLS = mutate(paramsMTLS, MTLS, 0.5);

			paramsROTS = crossover(c_ROTS_1.getParams(), c_ROTS_2.getParams(), ROTS); // crossover depending of method
			paramsROTS = mutate(paramsROTS, ROTS, 0.5);

			paramsEO = crossover(c_EO_1.getParams(), c_EO_2.getParams(), EO); // crossover depending of method
			paramsEO = mutate(paramsEO, EO, 0.5);

			paramsGA = crossover(c_GA_1.getParams(), c_GA_2.getParams(), GA); // crossover depending of method
			paramsGA = mutate(paramsGA, GA, 0.5);

			if (sequential) {
				s = mutiStartLocalSearch.solve(c_MTLS_1.getSolution(), paramsMTLS, qap, constructive);
				Chromosome newGene = new Chromosome(s, paramsMTLS, qap_size);
				insertIndividual(generation.get(MTLS), newGene);

				s = robustTabuSearch.solve(c_ROTS_1.getSolution(), paramsROTS, qap);
				newGene = new Chromosome(s, paramsROTS, qap_size);
				insertIndividual(generation.get(ROTS), newGene);

				s = extremalOptimization.solve(c_EO_1.getSolution(), paramsEO, qap);
				newGene = new Chromosome(s, paramsEO, qap_size);
				insertIndividual(generation.get(EO), newGene);

				geneticAlgorithm.solve(paramsGA, qap);
				Results geneticAlgorithmResult = geneticAlgorithm.getResults();
				s = geneticAlgorithmResult.getBestIndividual().getGenes();
				newGene = new Chromosome(s, paramsGA, qap_size);
				insertIndividual(generation.get(GA), newGene);
			} else {

				// printValues(generation);

				// setting variables for each method
				mutiStartLocalSearch.setEnviroment(c_MTLS_1.getSolution(), paramsMTLS);
				robustTabuSearch.setEnviroment(c_ROTS_1.getSolution(), paramsROTS);
				extremalOptimization.setEnviroment(c_EO_1.getSolution(), paramsEO);
				geneticAlgorithm.setEnviroment(paramsGA);

				// execution in parallel
				pool.submit(mutiStartLocalSearch);
				pool.submit(robustTabuSearch);
				pool.submit(extremalOptimization);
				pool.submit(geneticAlgorithm);

				mutiStartLocalSearch.join();
				robustTabuSearch.join();
				extremalOptimization.join();
				geneticAlgorithm.join();

				// insert new individual into generation
				Chromosome newChromosomeMTLS = new Chromosome(mutiStartLocalSearch.getSolution(), paramsMTLS, qap_size);
				insertIndividual(generation.get(MTLS), newChromosomeMTLS);

				Chromosome newChromosomeROTS = new Chromosome(robustTabuSearch.getSolution(), paramsROTS, qap_size);
				insertIndividual(generation.get(ROTS), newChromosomeROTS);

				Chromosome newChromosomeEO = new Chromosome(extremalOptimization.getSolution(), paramsEO, qap_size);
				insertIndividual(generation.get(EO), newChromosomeEO);

				Results geneticAlgorithmResult = geneticAlgorithm.getResults();
				int[] s_GA = geneticAlgorithmResult.getBestIndividual().getGenes();
				Chromosome newChromosomeGA = new Chromosome(s_GA, paramsGA, qap_size);
				insertIndividual(generation.get(GA), newChromosomeGA);
				listCost.get(GA).add(geneticAlgorithmResult.getBestFitness());

				if (geneticAlgorithmResult.getBestFitness() == qap.getBKS()) {
					findBKS();
				}

			}

			count_generations++;
		}
		// printValues(generation);
		/*
		 * for (int i = 0; i < listCost.size(); i++) { System.out.println( (i)+": " +
		 * listCost.get(i)); }
		 */

		printTotalTime(start);
		System.out.println("Generations: " + count_generations);
		DecimalFormat df2 = new DecimalFormat("#.##");

		for (int i = 0; i < generation.size(); i++) {
			List<Chromosome> listChromosomes = generation.get(i);
			int best = Integer.MAX_VALUE;
			int c = -1;
			for (int l = 0; l < listChromosomes.size(); l++) {
				if (best > qap.evalSolution(listChromosomes.get(l).getSolution())) {
					best = qap.evalSolution(listChromosomes.get(l).getSolution());
					c = l;
				}
			}

			printMetaheuristic(i, listChromosomes.get(c).getSolution(), best, listChromosomes.get(c).getParams(), df2);
		}

		
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("results.csv");
			fileWriter.append("Generation");
			fileWriter.append(";");
			fileWriter.append("MultiStart LocalSearch");
			fileWriter.append(";");
			fileWriter.append("Robust TabuSearch ");
			fileWriter.append(";");
			fileWriter.append("Extremal Optimization");
			fileWriter.append(";");
			fileWriter.append("Genetic Algorithm");
			fileWriter.append("\n");

			for (int i = 0; i <= count_generations; i++) {
				fileWriter.append(Integer.toString(i));
				fileWriter.append(";");
				fileWriter.append(Integer.toString(listCost.get(MTLS).get(i)));
				fileWriter.append(";");
				fileWriter.append(Integer.toString(listCost.get(ROTS).get(i)));
				fileWriter.append(";");
				fileWriter.append(Integer.toString(listCost.get(EO).get(i)));
				fileWriter.append(";");
				fileWriter.append(Integer.toString(listCost.get(GA).get(i)));
				fileWriter.append("\n");
			}

			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
					p[0] = random.nextInt(2); // restart type 0: random restart, 1: swaps
					break;
				case ROTS:
					p[0] = (random.nextInt(16) + 4) * qap_size;// 4 * (i + 1) * qap_size;// tabu duration factor
					p[1] = (random.nextInt(10) + 1) * qap_size * qap_size;// 2 * (i + 1) * qap_size * qap_size; //
																			// aspiration factor
					break;
				case EO:
					p[0] = random.nextInt(100); // tau*100
					p[1] = random.nextInt(3); // pdf function type
					break;
				case GA:
					p[0] = qap_size + qap_size * random.nextInt(5) / 2;// population size
					if (qap_size > 60) {
						p[0] = p[0] * 2 / 5;
					}
					p[1] = random.nextInt(1000); // mutation *1000
					p[2] = random.nextInt(2);// crossover operator type 0:crossover UX, 1:crossover in a random point

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
		// System.out.println("selected : " + index);

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
			p[0] = params1[0];// tau*100
			p[1] = params2[1];// type pdf
			break;
		case GA:
			p[0] = params1[0];// population size
			p[1] = params2[1];// mutation *1000
			p[2] = params2[2]; // crossover type
			break;
		}

		// Tools.printArray(p);

		return p;
	}

	public static int[] mutate(int[] params, int mh_type, double mp) {
		double mutation_number = random.nextDouble();

		int[] p = params.clone();
		// Tools.printArray(p);
		if (mutation_number <= mp) {
			final int param;
			switch (mh_type) {
			case MTLS:
				p[0] = random.nextInt(2); // restart type
				break;
			case ROTS:
				// getting what parameter to mutate
				param = random.nextInt(2);
				// System.out.println("param:" +param);
				switch (param) {
				case 0:
					p[param] = random.nextInt(16 * qap_size) + 4 * qap_size; // 4n to 20n
					break;
				case 1:
					p[param] = random.nextInt(9 * qap_size * qap_size) + qap_size * qap_size; // n*n to 10*n*n same
																								// range dokeroglu
																								// article
					break;
				}
				break;
			case EO:
				param = random.nextInt(2);
				// System.out.println("param:" +param);
				switch (param) {
				case 0:
					p[param] = random.nextInt(100); // tau*1000
					break;
				case 1:
					p[param] = random.nextInt(3); // pdf function type
					break;
				}

				break;
			case GA:
				param = random.nextInt(3);
				// System.out.println("param:" +param);
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

		// Tools.printArray(p);
		return p;

	}

	public static void insertIndividual(List<Chromosome> g, Chromosome new_individual) {

		int worst = -1;
		int cost = Integer.MIN_VALUE;
		int temp_cost = 0;
		boolean exist = false;

		// this cycle finish until the new chromosome will be different
		do {
			exist = false;
			// identify if the new individual is already in the generation
			for (Chromosome temp : g) {
				if (areIquals(temp, new_individual)) {
					exist = true;
					break;
				}
			}
			// if exist is necessary mutate
			if (exist) {
				new_individual = mutate(new_individual);
			}

		} while (exist);

		for (int i = 0; i < g.size(); i++) {
			temp_cost = qap.evalSolution(g.get(i).getSolution());
			if (temp_cost > cost) {
				cost = temp_cost;
				worst = i;
			}
		}

		g.remove(worst);
		g.add(new_individual);
	}

	private static Chromosome mutate(Chromosome new_individual) {
		int pos_geneX, pos_geneY, temp_gene;

		// first decide what genes change randomly
		pos_geneX = random.nextInt(qap_size);// with this value we put the range of number
		do {
			pos_geneY = random.nextInt(qap_size);// check that the position to change are different
		} while (pos_geneX == pos_geneY);

		// swapping - making the mutation
		temp_gene = new_individual.genes[pos_geneX];
		new_individual.genes[pos_geneX] = new_individual.genes[pos_geneY];
		new_individual.genes[pos_geneY] = temp_gene;
		return new_individual;
	}

	private static boolean areIquals(Chromosome individual1, Chromosome individual2) {
		int[] array1 = individual1.getSolution();
		int[] array2 = individual2.getSolution();
		// check position by position if they are equals
		/*
		 * for (int i = 0; i < qap.getSize(); i++) { if (array1[i] != array2[i]) {
		 * return false; } }
		 */

		return Arrays.equals(array1, array2);
	}

	public static void printTotalTime(long start) {
		// show the total time
		double time = (System.currentTimeMillis() - start);
		time /= 1000;
		System.out.println("\nTotal time: " + time + " sec");
	}

	public static int getSeed() {
		return random.nextInt();
	}

	public static int getExecutionTime() {
		return execution_time;
	}

	private static int getNCores() {
		String ncoresStr = System.getenv("COURSERA_GRADER_NCORES");
		if (ncoresStr == null) {
			return Runtime.getRuntime().availableProcessors();
		} else {
			return Integer.parseInt(ncoresStr);
		}
	}

	public static void printChromosomes(List<Chromosome> listChromosomes, int i) {
		if (i == -1) {
			System.out.println(i);
			for (int l = 0; l < listChromosomes.size(); l++) {
				Tools.printArray(listChromosomes.get(l).genes);
				System.out.println("costo " + qap.evalSolution(listChromosomes.get(l).getSolution()));
			}
		}
	}

	public static void printValues(List<List<Chromosome>> generation) {
		for (int i = 0; i < generation.size(); i++) {
			List<Chromosome> listChromosomes = generation.get(i);
			if (i >= 0) {
				System.out.println(i);
				for (int l = 0; l < listChromosomes.size(); l++) {
					Tools.printArray(listChromosomes.get(l).genes);
					System.out.println("costo " + qap.evalSolution(listChromosomes.get(l).getSolution()));
				}
			}
		}
	}

	public static void findBKS() {
		// System.out.println(from);
		no_find_BKS = false;
	}

	public static void printMetaheuristic(final int type_mh, final int[] solution, final int cost, final int[] p,
			DecimalFormat df2) {

		String params_text = "";
		switch (type_mh) {
		case MTLS:
			System.out.println("\nMultiStart LocalSearch Results:");
			params_text = (p[0] == 0) ? "\nRandom Restart" : "\nRestart by swaps";
			break;
		case ROTS:
			System.out.println("\nRobust TabuSearch Results:");
			params_text = "\nTabu duration: " + p[0] + "\nAspiration factor: " + p[1];
			break;
		case EO:
			System.out.println("\nExtremal Optimization Results:");
			params_text = "\nTau: " + p[0] / 100.0 + "\nPdf function: ";
			switch (p[1]) {
			case 0:
				params_text += "Exponential";
				break;
			case 1:
				params_text += "Power";
				break;
			case 2:
				params_text += "Gamma";
				break;
			}

			break;
		case GA:
			System.out.println("\nGenetic Algorithm Results:");
			params_text = "\nPopulation: " + p[0] + "\nMutation rate: " + p[1] / 1000.0;
			params_text += (p[2] == 0) ? "\nCrossover UX" : "\nCrossover in random point";

			break;
		}

		double std = cost * 100.0 / qap.getBKS() - 100;
		System.out.println("Cost: " + cost + " " + df2.format(std) + "%");
		Tools.printArray(solution);
		System.out.println("Params " + params_text);

	}

}