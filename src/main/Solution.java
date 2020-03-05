package main;

public class Solution {
	private int[] array;
	private int[] params;
	private String method;

	public Solution(int[] s, int[] p, String m) {
		array = s.clone();
		params = p.clone();
		method = m;
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
}