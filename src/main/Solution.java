package main;

public class Solution {
	private int[] array;
	
	public Solution (int [] s) {
		array = s.clone();
	}

	public int[] getArray() {
		return array.clone();
	}
}
