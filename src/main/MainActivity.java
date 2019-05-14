package main;

import java.util.Scanner;

import main.GeneticAlgorithm.GeneticAlgorithm;
import main.GeneticAlgorithm.Results;

public class MainActivity {

	public static void main(String[] args) {
		// read the problem to solve QAP6, QAP7, QAP8, QAP9
		int option = 1;//showMenu();
	
		System.out.println("\n\n/*********** DATOS DE EJECUCIÓN DEL ALGORITMO **********/");

		int[][] flow, distance;
		distance = getDataforDistance(option);
		flow = getDataForFlow(option);
		// initialize qap data, i.e matrix of flow and distance matix [row][col]
		QAPData qap = new QAPData(distance, flow);
		// qap.showData();
		
		//ReadFile readFile = new ReadFile("test.dat");//"qapdata/chr12a.dat"
		int [] initSolution = {0,1,2,3,4,5};
		
		LocalSearch localSearch = new LocalSearch();
		int []bestSolutionFound = localSearch.executeSimpleSwap(initSolution, qap);
		

		System.out.println("\n\n/*********** MEJOR SOLUCIÓN ENCONTRADA **********/");
		System.out.println("Costo: " + qap.evalSolution(bestSolutionFound));
		System.out.println("Combinación: ");
		printSolution(bestSolutionFound);
		
	
		
	}



	public static void printSolution(int[] array) {
		// System.out.println("Locaciones");
		String locations = "Facilidades: ", facilities = "Ubicaciones: ";
		for (int i = 0; i < array.length; i++) {
			locations = locations + ((i + 1) + " ");
		}
		// System.out.println("\nFacilidades");
		System.out.println(locations);
		for (int i : array) {
			facilities = facilities + ((i + 1) + " ");// +1 because the index in java start with 0
		}
		System.out.println(facilities);
	}

	public static void printArray(int[] array) {
		for (int i : array) {
			System.out.print(i + 1 + "\t");// +1 because the index in java start with 0
		}
		System.out.println("\n");
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

	public static int[][] getDataforDistance(int option) {
		// QAP 6
		int[][] d1 = { { 0, 40, 64, 36, 22, 60 }, { 40, 0, 41, 22, 36, 72 }, { 64, 41, 0, 28, 44, 53 },
				{ 36, 22, 28, 0, 20, 50 }, { 22, 36, 44, 20, 0, 41 }, { 60, 72, 53, 50, 41, 0 } };

		int[][] d2 = { { 0, 35, 71, 99, 71, 75, 41 }, { 35, 0, 42, 80, 65, 82, 47 }, { 71, 42, 0, 45, 49, 79, 55 },
				{ 99, 80, 45, 0, 36, 65, 65 }, { 71, 65, 49, 36, 0, 31, 32 }, { 75, 82, 79, 65, 31, 0, 36 },
				{ 41, 47, 55, 65, 32, 36, 0 } };

		// QAP 8
		int[][] d3 = { { 0, 32, 68, 97, 75, 70, 75, 40 }, { 32, 0, 42, 80, 53, 65, 82, 47 },
				{ 68, 42, 0, 45, 15, 49, 79, 55 }, { 97, 80, 45, 0, 30, 36, 65, 65 }, { 75, 53, 15, 30, 0, 38, 69, 53 },
				{ 70, 65, 49, 36, 38, 0, 31, 32 }, { 75, 82, 79, 65, 69, 31, 0, 36 },
				{ 40, 47, 55, 65, 53, 32, 36, 0 } };

		// QAP 9
		int[][] d4 = { { 0, 32, 68, 97, 75, 70, 75, 40, 24 }, { 32, 0, 42, 80, 53, 65, 82, 47, 29 },
				{ 68, 42, 0, 45, 15, 49, 79, 55, 50 }, { 97, 80, 45, 0, 30, 36, 65, 65, 73 },
				{ 75, 53, 15, 30, 0, 38, 69, 53, 53 }, { 70, 65, 49, 36, 38, 0, 31, 32, 46 },
				{ 75, 82, 79, 65, 69, 31, 0, 36, 56 }, { 40, 47, 55, 65, 53, 32, 36, 0, 19 },
				{ 24, 29, 50, 73, 53, 46, 56, 19, 0 } };

		switch (option) {
		case 1:
			System.out.print("Se solucionará QAP6\n");
			return d1;
		case 2:
			System.out.print("Se solucionará QAP7\n");
			return d2;

		case 3:
			System.out.print("Se solucionará QAP8\n");
			return d3;
		case 4:
			System.out.print("Se solucionará QAP9\n");
			return d4;

		default:
			return d3;
		}

	}

	public static int[][] getDataForFlow(int option) {
		int[][] f1 = { { 0, 1, 1, 2, 0, 0 }, { 1, 0, 0, 0, 0, 2 }, { 1, 0, 0, 0, 0, 1 }, { 2, 0, 0, 0, 3, 0 },
				{ 0, 0, 0, 3, 0, 0 }, { 0, 2, 1, 0, 0, 0 } };

		int[][] f2 = { { 0, 2, 0, 0, 0, 0, 2 }, { 2, 0, 3, 0, 0, 1, 0 }, { 0, 3, 0, 0, 0, 1, 0 },
				{ 0, 0, 0, 0, 3, 0, 1 }, { 0, 0, 0, 3, 0, 0, 0 }, { 0, 1, 1, 0, 0, 0, 0 }, { 2, 0, 0, 1, 0, 0, 0 } };

		int[][] f3 = { { 0, 2, 4, 0, 0, 0, 2, 0 }, { 2, 0, 3, 1, 0, 1, 0, 0 }, { 4, 3, 0, 0, 0, 1, 0, 0 },
				{ 0, 1, 0, 0, 3, 0, 1, 5 }, { 0, 0, 0, 3, 0, 0, 0, 0 }, { 0, 1, 1, 0, 0, 0, 0, 0 },
				{ 2, 0, 0, 1, 0, 0, 0, 4 }, { 0, 0, 0, 5, 0, 0, 4, 0 } };

		int[][] f4 = { { 0, 2, 4, 0, 0, 0, 2, 0, 0 }, { 2, 0, 3, 1, 0, 6, 0, 0, 2 }, { 4, 3, 0, 0, 0, 3, 0, 0, 0 },
				{ 0, 1, 0, 0, 1, 0, 1, 2, 0 }, { 0, 0, 0, 1, 0, 0, 0, 0, 0 }, { 0, 6, 3, 0, 0, 0, 0, 0, 2 },
				{ 2, 0, 0, 1, 0, 0, 0, 4, 3 }, { 0, 0, 0, 2, 0, 0, 4, 0, 0 }, { 0, 2, 0, 0, 0, 2, 3, 0, 0 } };

		switch (option) {
		case 1:
			return f1;
		case 2:
			return f2;
		case 3:
			return f3;
		case 4:
			return f4;
		default:
			return f3;
		}

	}

}
