package main;

import java.util.Scanner;

import main.GeneticAlgorithm.GeneticAlgorithm;
import main.GeneticAlgorithm.Results;

public class MainActivity {

	public static void main(String[] args) {
		final int totalIterations = 100000;
		final String problem = "chr12a.dat";//args[0];
		
		System.out.println("\nProblema: " + problem);
		ReadFile readFile = new ReadFile("qapdata/"+problem);
		final long start = System.currentTimeMillis();

		int[][] flow, distance;
		// distance = Tools.getDataforDistance(problem);
		// flow = Tools.getDataForFlow(problem);
		distance = readFile.getDistance();
		flow = readFile.getFlow();

		// initialize qap data, i.e matrix of flow and distance matrix [row][col]
		QAPData qap = new QAPData(distance, flow);

		// get init solution with constructive method
		Constructive constructive = new Constructive();
		int[] initSolution = constructive.createRandomSolution(qap.getSize(), 1);
		int[] bestSolutionFound = initSolution;// for now this is the best solution
		qap.printSolution(initSolution, "Solución inicial");
		qap.initDeltas(initSolution);

		int method = 1;// showMenuMethod();
		switch (method) {
		case 1:
			MultiStartLocalSearch mutiStartLocalSearch = new MultiStartLocalSearch();
			bestSolutionFound = mutiStartLocalSearch.solve(totalIterations, initSolution, qap, constructive);
			break;

		case 2:
			RobustTabuSearch robustTabuSearch = new RobustTabuSearch();
			bestSolutionFound = robustTabuSearch.solve(totalIterations, initSolution, qap);
			break;

		case 3:
			ExtremalOptimization extremalOptimization = new ExtremalOptimization();
			bestSolutionFound = extremalOptimization.solve(totalIterations, initSolution, qap, -0.5);
			break;

		case 4:
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

		qap.printSolution(bestSolutionFound, "\nMejor Solución");

		printTotalTime(start);

	}



	public static void printTotalTime(long start) {
		// show the total time
		double time = (System.currentTimeMillis() - start);
		time /= 1000;
		System.out.println("\n" + time + " seg");
	}
}
