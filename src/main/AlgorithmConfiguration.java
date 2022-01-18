package main;

public class AlgorithmConfiguration {
	// TODO create delta parameter for each metaheuristic

	// fixed data for the algorithm
	final static int MTLS = 0, ROTS = 1, EO = 2;
	final static String[] mh_text = { "MTLS", "ROTS", "EO" };
	final static int DIFFERENT_MH = 3;

	// custom variable data for user execution
	// general custom data
	final static String problemName = "bur26a.qap";
	final static int totalTimeOut = 300; // time in seconds
	final static int teamSize = 3;// should be multiple of DIFFERENT_MH
	final static boolean printResultInConsole = true;
	final static boolean printResultInCSVFile = true;

	// data for team of type: AdaptedParamsTeam
	final static int totalAdaptedParamsTeams = 2;
	final static int[] iterationTimeAdaptedParamsTeam = { 20000, 15000 };// by iteration in millisecond
	final static int[] totalAdaptationsAdaptedParamsTeam = { 15, 20 };
	final static int[] requestPolicyAdaptedParamsTeam = { SolutionPopulation.REQUEST_RANDOM,
			SolutionPopulation.REQUEST_SAME };
	final static int[] entryPolicyAdaptedParamsTeam = { SolutionPopulation.ENTRY_IF_DIFERENT,
			SolutionPopulation.ENTRY_SAME };
	final static int[] solutionSimilarityPercentageAdaptedParamsTeam = { 33, 33 };

	// data for team of type: RandomParamsTeam
	final static int totalRandomParamsTeams = 1;
	final static int[] iterationTimeRandomParamsTeam = { 20000 };
	final static int[] totalAdaptationsRandomParamsTeam= { 15 };
	final static int[] requestPolicyRandomParamsTeam = { SolutionPopulation.REQUEST_RANDOM };
	final static int[] entryPolicyRandomParamsTeam = { SolutionPopulation.ENTRY_IF_DIFERENT };

	// data for team of type: FixedParamsTeam
	final static int totalFixedParamsTeams = 0;
	final static int[] iterationTimeFixedParamsTeam= null;
	final static int[] totalAdaptationsFixedParamsTeam = null;
	final static int[] requestPolicyFixedParamsTeam = null;
	final static int[] entryPolicyFixedParamsTeam = null;
}