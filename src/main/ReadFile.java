package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ReadFile {

	private int[][] flow, distance;
	private int target;

	public ReadFile(String fileName) {
		Scanner scanner;

		try {
			scanner = new Scanner(new File(fileName));

			int n = Integer.parseInt(scanner.next());
			scanner.next(); //skip a data no necessary
			target = Integer.parseInt(scanner.next());
			//System.out.println(target);

			flow = new int[n][n];
			distance = new int[n][n];

			for (int row = 0; row < n; row++) {
				for (int col = 0; col < n; col++) {
					flow[row][col] = Integer.parseInt(scanner.next());
				}
			}

			for (int row = 0; row < n; row++) {
				for (int col = 0; col < n; col++) {
					distance[row][col] = Integer.parseInt(scanner.next());
				}
			}

			scanner.close();

			// printMatrix(flow);
			// printMatrix(distance);

		} catch (FileNotFoundException e) {
			System.out.println("Error al leer el archivo");
			e.printStackTrace();
		}

	}

	public void printMatrix(int[][] matrix) {
		for (int[] row : matrix) {
			for (int i : row) {
				System.out.print(i + "\t");
			}
			System.out.println("\n");
		}
	}

	public int[][] getFlow() {
		return flow;
	}

	public int[][] getDistance() {
		return distance;
	}
	
	public int getTarget() {
		return target;
	}

}
