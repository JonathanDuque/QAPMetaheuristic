package main;

public class TeamConfiguration {
	private final int totalAdaptations;
	private final int searchers;
	private final int teamId;
	private final int iterationTime;
	
	public TeamConfiguration(int totalAdaptations, int searchers, int teamId, int iterationTime) {
		this.totalAdaptations = totalAdaptations;
		this.searchers = searchers;
		this.teamId = teamId;
		this.iterationTime = iterationTime;
		//TODO validate time out of 5 mins
	}

	public int getTotalAdaptations() {
		return totalAdaptations;
	}

	public int getSearchers() {
		return searchers;
	}
	
	public int getTeamId() {
		return teamId;	
	}

	public int getIterationTime() {
		return iterationTime;
	}
	
	public void printTeamConfiguration() {
		System.out.println("\nTeam: " + teamId);
		System.out.println("Threads: " + searchers);
		System.out.println("Metaheuristic time: " + iterationTime / 1000.0 + " seconds");
		System.out.println("Iterations: " + totalAdaptations);
		System.out.println("Time out: " + iterationTime * totalAdaptations / 1000.0 + " seconds");
	}
}
