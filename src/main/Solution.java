package main;

public class Solution {
	private int[] array;
	private int[] params;
	private String method;
	String params_setup;

	public Solution(int[] s, int[] p, String m, String p_s) {
		array = s.clone();
		params = p.clone();
		method = m;
		params_setup = p_s;
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

	public String getParamSetup() {
		return params_setup;
	}

	
}
