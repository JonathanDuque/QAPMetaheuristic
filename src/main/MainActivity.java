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
	// static List<List<Integer>> listCost = new ArrayList<>(4);// 4 mh
	private static List<Solution> elite_population;
	private static List<Solution> diverse_population;

	public static void main(String[] args) {
		//final int workers = 4;
		final int workers = Integer.parseInt(args[1]);
		if ((workers % 4) != 0) {
			System.out.println(
					"\n***************** Is necessary workes multiple of 4     ********************************");
			return;
		}

		final String problem = args[0];//"ste36a.qap";
		//final String problem = "ste36a.qap";
		System.out.println("\n*****************    Problem: " + problem + "    ********************************");
		final ReadFile readFile = new ReadFile("Data/" + problem);
		final long start = System.currentTimeMillis();

		// initialize qap data, i.e matrix of flow and distance matrix [row][col]
		final int[][] flow = readFile.getFlow(), distance = readFile.getDistance();
		qap = new QAPData(distance, flow, readFile.getTarget());
		qap_size = qap.getSize();
		random = new Random(1);

		// int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		// System.out.println("Processors : " + numberOfProcessors );

		ForkJoinPool pool = new ForkJoinPool(4);

		final int total_by_mh = workers / 4;
		final int number_by_mh = 5 * total_by_mh;
		final Constructive constructive = new Constructive();
		List<List<Params>> paramsPopulation = generateInitialPopulation(number_by_mh, constructive);

		int generations = 20, count_generations = 0;

		// create parameters for each mh
		Params c_MTLS_1, c_MTLS_2;
		Params c_ROTS_1, c_ROTS_2;
		Params c_EO_1, c_EO_2;
		Params c_GA_1, c_GA_2;

		// create params for each mh
		int[] paramsMTLS = new int[3];
		int[] paramsROTS = new int[3];
		int[] paramsEO = new int[3];
		int[] paramsGA = new int[3];

		/*
		 * for (int i = 0; i < 4; i++) { int initCost =
		 * qap.evalSolution((generation.get(i).get(0).getSolution())); //
		 * System.out.println(""+initCost); List<Integer> cost_by_mh = new
		 * ArrayList<>(); cost_by_mh.add(initCost); listCost.add(cost_by_mh); }
		 */

		// printValues(paramsPopulation);

		// printPopulation(elite_population);
		// System.out.println("\n");
		// printPopulation(diverse_population);

		List<MultiStartLocalSearch> list_mtls = new ArrayList<>();
		List<RobustTabuSearch> list_rots = new ArrayList<>();
		List<ExtremalOptimization> list_eo = new ArrayList<>();
		List<GeneticAlgorithm> list_ga = new ArrayList<>();
		DecimalFormat df2 = new DecimalFormat("#.##");

		while (count_generations < generations && no_find_BKS) {

			for (int i = 0; i < total_by_mh; i += 1) {
				MultiStartLocalSearch mtls = new MultiStartLocalSearch(qap, random.nextInt(), i);
				RobustTabuSearch rots = new RobustTabuSearch(qap, random.nextInt());
				ExtremalOptimization eo = new ExtremalOptimization(qap, random.nextInt());
				GeneticAlgorithm ga = new GeneticAlgorithm(qap, random.nextInt());

				list_mtls.add(mtls);
				list_rots.add(rots);
				list_eo.add(eo);
				list_ga.add(ga);
			}

			// System.out.println( count_generations );
			List<Params> list_params_MTLS = new ArrayList<>(paramsPopulation.get(MTLS));
			List<Params> list_params_ROST = new ArrayList<>(paramsPopulation.get(ROTS));
			List<Params> list_params_EO = new ArrayList<>(paramsPopulation.get(EO));
			List<Params> list_params_GA = new ArrayList<>(paramsPopulation.get(GA));

			for (int i = 0; i < total_by_mh; i += 1) {
				c_MTLS_1 = selectIndividual(list_params_MTLS);
				c_MTLS_2 = selectIndividual(list_params_MTLS);

				c_ROTS_1 = selectIndividual(list_params_ROST);
				c_ROTS_2 = selectIndividual(list_params_ROST);

				c_EO_1 = selectIndividual(list_params_EO);
				c_EO_2 = selectIndividual(list_params_EO);

				c_GA_1 = selectIndividual(list_params_GA);
				c_GA_2 = selectIndividual(list_params_GA);

				// crossover and mutation
				paramsMTLS = crossover(c_MTLS_1.getParams(), c_MTLS_2.getParams(), MTLS); // crossover depending of
																							// method
				paramsMTLS = mutate(paramsMTLS, MTLS, 0.5);

				paramsROTS = crossover(c_ROTS_1.getParams(), c_ROTS_2.getParams(), ROTS); // crossover depending of
																							// method
				paramsROTS = mutate(paramsROTS, ROTS, 0.5);

				paramsEO = crossover(c_EO_1.getParams(), c_EO_2.getParams(), EO); // crossover depending of method
				paramsEO = mutate(paramsEO, EO, 0.5);

				paramsGA = crossover(c_GA_1.getParams(), c_GA_2.getParams(), GA); // crossover depending of method
				paramsGA = mutate(paramsGA, GA, 0.5);

				// printValues(generation);

				// setting variables for each method
				list_ga.get(i).setEnvironment(paramsGA, elite_population); // GA environment is necessary make the first
				list_mtls.get(i).setEnvironment(getSolution(diverse_population), paramsMTLS);
				list_rots.get(i).setEnvironment(getSolution(diverse_population), paramsROTS);
				list_eo.get(i).setEnvironment(getSolution(diverse_population), paramsEO);
			}

			// launch execution in parallel for all workers
			for (int i = 0; i < total_by_mh; i += 1) {
				pool.submit(list_mtls.get(i));
				pool.submit(list_rots.get(i));
				pool.submit(list_eo.get(i));
				pool.submit(list_ga.get(i));
			}

			// wait for each method
			for (int i = 0; i < total_by_mh; i += 1) {
				list_mtls.get(i).join();
				list_rots.get(i).join();
				list_eo.get(i).join();
				list_ga.get(i).join();
			}

			for (int i = 0; i < total_by_mh; i += 1) {
				// insert new parameters individual into generation
				insertIndividual(paramsPopulation.get(MTLS), new Params(paramsMTLS, list_mtls.get(i).getBestCost()),
						MTLS);
				insertSolution(list_mtls.get(i).getSolution());

				insertIndividual(paramsPopulation.get(ROTS), new Params(paramsROTS, list_rots.get(i).getBestCost()),
						ROTS);
				insertSolution(list_rots.get(i).getSolution());

				insertIndividual(paramsPopulation.get(EO), new Params(paramsEO, list_eo.get(i).getBestCost()), EO);
				insertSolution(list_eo.get(i).getSolution());

				Results geneticAlgorithmResult = list_ga.get(i).getResults();
				int[] s_GA = geneticAlgorithmResult.getBestIndividual().getGenes();
				insertIndividual(paramsPopulation.get(GA),
						new Params(paramsGA, geneticAlgorithmResult.getBestFitness()), GA);
				insertSolution(s_GA);

				// listCost.get(GA).add(geneticAlgorithmResult.getBestFitness());

				if (geneticAlgorithmResult.getBestFitness() == qap.getBKS()) {
					findBKS();
				}

			}

			diverse_population.clear();
			int ga_population = random.nextInt(total_by_mh);
			diverse_population = list_ga.get(ga_population).getFinalPopulation();

			count_generations++;

			// System.out.println("Elite");
			// printPopulation(elite_population, df2);
			// System.out.println("Diverse");
			// printPopulation(diverse_population, df2);

			list_mtls.clear();
			list_rots.clear();
			list_eo.clear();
			list_ga.clear();
		}
		//printValues(paramsPopulation);
		/*
		 * for (int i = 0; i < listCost.size(); i++) { System.out.println( (i)+": " +
		 * listCost.get(i)); }
		 */

		System.out.println("Best Solution");
		printPopulation(elite_population, df2);
		//System.out.println("Diverse");
		//printPopulation(diverse_population, df2);

		printTotalTime(start);
		System.out.println("Generations: " + count_generations);

		for (int i = 0; i < paramsPopulation.size(); i++) {
			List<Params> listParams = paramsPopulation.get(i);
			int best = Integer.MAX_VALUE;
			int c = -1;
			for (int l = 0; l < listParams.size(); l++) {
				if (best > listParams.get(l).getScore()) {
					best = listParams.get(l).getScore();
					c = l;
				}
			}

			//printMetaheuristic(i, best, listParams.get(c).getParams(), df2);
		}

		/*
		 * FileWriter fileWriter; try { fileWriter = new FileWriter("results.csv");
		 * fileWriter.append("Generation"); fileWriter.append(";");
		 * fileWriter.append("MultiStart LocalSearch"); fileWriter.append(";");
		 * fileWriter.append("Robust TabuSearch "); fileWriter.append(";");
		 * fileWriter.append("Extremal Optimization"); fileWriter.append(";");
		 * fileWriter.append("Genetic Algorithm"); fileWriter.append("\n");
		 * 
		 * for (int i = 0; i <= count_generations; i++) {
		 * fileWriter.append(Integer.toString(i)); fileWriter.append(";");
		 * fileWriter.append(Integer.toString(listCost.get(MTLS).get(i)));
		 * fileWriter.append(";");
		 * fileWriter.append(Integer.toString(listCost.get(ROTS).get(i)));
		 * fileWriter.append(";");
		 * fileWriter.append(Integer.toString(listCost.get(EO).get(i)));
		 * fileWriter.append(";");
		 * fileWriter.append(Integer.toString(listCost.get(GA).get(i)));
		 * fileWriter.append("\n"); }
		 * 
		 * fileWriter.flush(); fileWriter.close(); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */

	}

	public static List<List<Params>> generateInitialPopulation(int number_by_mh, Constructive constructive) {
		elite_population = new ArrayList<>();

		List<List<Params>> paramsPopulation = new ArrayList<>(4); // because there are 4 different mh

		for (int k = 0; k < 4; k++) {
			List<Params> tempList = new ArrayList<>(number_by_mh);
			for (int i = 0; i < number_by_mh; i++) {
				int[] s = constructive.createRandomSolution(qap_size, (k * number_by_mh + i));
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
				tempList.add(new Params(p, Integer.MAX_VALUE));
				elite_population.add(new Solution(s));
			}

			paramsPopulation.add(tempList);
		}

		diverse_population = new ArrayList<>(elite_population);

		return paramsPopulation;
	}

	public static Params selectIndividual(List<Params> p) {
		Params selected;
		// obtain a number between 0 - size population
		int index = random.nextInt(p.size());
		selected = p.get(index);
		p.remove(index);// delete for no selecting later
		// System.out.println("selected : " + index);

		return selected;
	}

	public static int[] getSolution(List<Solution> p) {
		Solution selected_solution;
		int index = random.nextInt(p.size());
		selected_solution = p.get(index);
		p.remove(index);// delete for no selecting later
		// System.out.println("selected : " + index);

		return selected_solution.getArray();
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

	public static void insertIndividual(List<Params> listParams, Params new_params, final int type) {

		int worst = -1;
		int cost = Integer.MIN_VALUE;
		int temp_score = 0;
		boolean exist = false;

		// this cycle finish until the new params will be different
		/*
		 * do { exist = false; // identify if the new individual is already in the
		 * generation for (Params temp : listParams) {
		 * Tools.printArray(temp.getParams()); Tools.printArray(new_params.getParams());
		 * if (Arrays.equals(temp.getParams(), new_params.getParams())) { exist = true;
		 * break; } } // if exist is necessary mutate if (exist) { new_params = new
		 * Params(mutate(new_params.getParams(), type, 1), new_params.getScore()); }
		 * System.out.println("aca"); } while (exist);
		 */

		for (int i = 0; i < listParams.size(); i++) {
			temp_score = listParams.get(i).getScore();
			if (temp_score > cost) {
				cost = temp_score;
				worst = i;
			}
		}

		listParams.remove(worst);
		listParams.add(new_params);
	}

	private static int[] mutate(int[] s) {
		int posX, posY, temp;

		// first decide what value change randomly
		posX = random.nextInt(qap_size);// with this value we put the range of number
		do {
			posY = random.nextInt(qap_size);// check that the position to change are different
		} while (posX == posY);

		// swapping - making the mutation
		temp = s[posX];
		s[posX] = s[posY];
		s[posY] = temp;
		return s;
	}

	public static void printTotalTime(long start) {
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

	public static void printParams(List<Params> listParams, int i) {
		if (i == -1) {
			System.out.println(i);
			for (int l = 0; l < listParams.size(); l++) {
				Tools.printArray(listParams.get(l).getParams());
				System.out.println("costo " + listParams.get(l).getScore());
			}
		}
	}

	public static void printValues(List<List<Params>> generation) {
		for (int i = 0; i < generation.size(); i++) {
			List<Params> listChromosomes = generation.get(i);
			if (i >= 0) {
				System.out.println(i);
				for (int l = 0; l < listChromosomes.size(); l++) {
					Tools.printArray(listChromosomes.get(l).getParams());
					// System.out.println("costo " + listChromosomes.get(l).getScore());
				}
			}
		}
	}

	public static void findBKS() {
		no_find_BKS = false;
	}

	public static void printMetaheuristic(final int type_mh, final int cost, final int[] p, DecimalFormat df2) {

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
		// Tools.printArray(solution);
		System.out.println("Params " + params_text);

	}

	public static void insertSolution(int[] s) {
		boolean exist;
		// this cycle finish until the new solution will be different
		do {
			exist = false;
			// identify if the new solution is already in the population
			for (Solution temp : elite_population) {
				if (Arrays.equals(temp.getArray(), s)) {
					exist = true;
					break;
				}
			}
			// if exist is necessary mutate
			if (exist) {
				s = mutate(s);
			}

		} while (exist);

		int worst = -1, temp_cost;
		int cost = Integer.MIN_VALUE;

		for (int i = 0; i < elite_population.size(); i++) {
			temp_cost = qap.evalSolution(elite_population.get(i).getArray());
			if (temp_cost > cost) {
				cost = temp_cost;
				worst = i;
			}
		}

		elite_population.remove(worst);
		elite_population.add(new Solution(s));
	}

	public static void printPopulation(List<Solution> p, DecimalFormat df2) {
		int[] best_solution = p.get(0).getArray();
		int best_cost = Integer.MAX_VALUE;
		for (int i = 0; i < p.size(); i++) {
			int[] temp_s = p.get(i).getArray();
			int temp_cost = qap.evalSolution(p.get(i).getArray());
			// qap.printSolution(temp_s, temp_cost);
			if (temp_cost < best_cost) {
				best_solution = temp_s;
				best_cost = temp_cost;
			}
		}

		qap.printSolution(best_solution, best_cost);
		double std = best_cost * 100.0 / qap.getBKS() - 100;
		System.out.println("Cost: " + best_cost + " " + df2.format(std) + "%");
		// Tools.printArray(listSolution.get(i).getArray());

	}
}