package main;

import java.util.Scanner;

import main.GeneticAlgorithm.GeneticAlgorithm;
import main.GeneticAlgorithm.Results;

public class MainActivity {

	public static void main(String[] args) {
		
		//int problem = 3;// showMenu(); 

		System.out.println("\n/*********** DATOS DE EJECUCIÓN DEL ALGORITMO **********/");
		String problem = getProblemName();

		ReadFile readFile = new ReadFile("qapdata/"+problem+ ".dat");
		long start = System.currentTimeMillis();

		int[][] flow, distance;
		//distance = Tools.getDataforDistance(problem);
		//flow = Tools.getDataForFlow(problem);
		distance = readFile.getDistance();
		flow = readFile.getFlow();

		// initialize qap data, i.e matrix of flow and distance matix [row][col]
		QAPData qap = new QAPData(distance, flow);

		// get init solution with constructive method
		Constructive constructive = new Constructive();
		int[] initSolution = constructive.createInitSolution(qap);
		int[] bestSolutionFound = initSolution;// for now this is the best solution
		//System.out.println("Solución inicial");
		//qap.printSolution(initSolution);
		//qap.printSolution2(initSolution);
				
		qap.initDeltas(initSolution);
		
		int method = 3;// showMenuMethod();
		switch (method) {
		case 1:
			LocalSearch localSearch = new LocalSearch();
			bestSolutionFound = localSearch.solve(initSolution, qap);
			break;

		case 2:
			TabuSearch tabuSearch = new TabuSearch();
			bestSolutionFound = tabuSearch.solve(10000, initSolution, qap, false);
			// tabuSearch.showMemories();
			break;

		case 3:
			RobustTabuSearch robustTabuSearch = new RobustTabuSearch();
			bestSolutionFound = robustTabuSearch.solve(100000, initSolution, qap);
			// robustTabuSearch.showMemories();
			break;

		case 4:
			ExtremalOptimization extremalOptimization = new ExtremalOptimization();
			bestSolutionFound = extremalOptimization.solve(100 * qap.getSize(), initSolution, qap, -0.4);
			break;

		case 5:
			GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();
			// pop_size, generations, mutation_probability, QAPData
			geneticAlgorithm.solve(10 * qap.getSize(), 40 * qap.getSize(), 0.5, qap);

			// get the results for the algorithm
			Results geneticAlgorithmResult = geneticAlgorithm.getResults();
			// print the results, the best, the worst and the average population fitness
			System.out.println("\nEl mejor individuo es: ");
			geneticAlgorithmResult.getBestIndividual().printIndividualWithFitness(qap);
			bestSolutionFound = geneticAlgorithmResult.getBestIndividual().getGenes();
			System.out.println("El peor individuo es: ");
			geneticAlgorithmResult.getWorstIndividual().printIndividualWithFitness(qap);
			System.out.println("El valor promedio de la población es: " + geneticAlgorithmResult.getAvg_value());
			break;

		}

		System.out.println("\n\n/*********** MEJOR SOLUCIÓN ENCONTRADA **********/");
		qap.printSolution(bestSolutionFound);
		
		printTotalTime(start);

	}

	

	public static int showMenu() {
		int op;
		do {
			System.out.print("/****** ELIJA EL PROBLEMA A SOLUCIONAR ******/\n");
			System.out.print("\t1. QAP6\n\t2. QAP7\n\t3. QAP8\n\t4. QAP9\n");
			System.out.print("Escriba la opción y presione ENTER: ");
			Scanner in = new Scanner(System.in);

			op = in.nextInt();
		} while (op < 1 || op > 4);

		return op;
	}

	public static int showMenuMethod() {
		int op;
		do {
			System.out.print("/****** ELIJA EL MÉTODO  ******/\n");
			System.out.print("\t1. Local Search\n\t2. Tabu Search\n\t3. Genetic Algorithm\n");
			System.out.print("Escriba la opción y presione ENTER: ");
			Scanner in = new Scanner(System.in);

			op = in.nextInt();
		} while (op < 1 || op > 3);

		return op;
	}

	public static int memoryType() {
		int op;
		do {
			System.out.print("/****** ELIJA EL TIPO DE MEMORIA A USAR ******/\n");
			System.out.print("\t1. Memoria Corta\n\t2. Memoria Larga\n");
			System.out.print("Escriba la opción y presione ENTER: ");
			Scanner in = new Scanner(System.in);

			op = in.nextInt();
		} while (op < 1 || op > 2);

		return op;
	}
	
	public static String getProblemName() {
		String op;
		do {
			
			System.out.print("Escriba la opción y presione ENTER: ");
			Scanner in = new Scanner(System.in);

			op = in.next();
		} while (op.isEmpty());

		return op;
	}

	public static void printTotalTime(long start) {
		// show the total time
		double time = (System.currentTimeMillis() - start);
		time /= 1000;
		System.out.println("\n" + time + " sec");
	}
}
