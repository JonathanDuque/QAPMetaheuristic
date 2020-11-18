import java.util.Arrays;

public class Solution {
	private int[] array;
	private int[] params;
	private String method;

	public Solution(int[] s, int[] p, String m) {
		array = s.clone();
		params = p.clone();
		method = m;
	}
	
	public Solution(int[] solution_array, int qap_size) {
		String[] mh_text = { "MTLS", "ROTS", "EO" };
		array = Arrays.copyOfRange(solution_array, 0, qap_size);
		params = Arrays.copyOfRange(solution_array, qap_size, qap_size + 3);
		method = mh_text[solution_array[qap_size + 3]];
	}

	public int[] getArray() {
		return array.clone();
	}

	public int[] getParams() {
		return params;
	}

	public String getMethod() {
		return method;
	}

	public int[] convertSolutionToArray() {
		int[] solution_array = new int[array.length + params.length + 1];
		for (int i = 0; i < array.length; i++) {
			solution_array[i] = array[i];
		}

		for (int i = 0; i < params.length; i++) {
			solution_array[array.length + i] = params[i];
		}

		switch (method) {
		case "MTLS":
			solution_array[array.length + params.length] = 0;
			break;
		case "ROTS":
			solution_array[array.length + params.length] = 1;
			break;
		case "EO":
			solution_array[array.length + params.length] = 2;
			break;
		}

		return solution_array;

	}
	
}
