package main;

public class AlgorithmConfiguration {
	//TODO create readme for this file

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

	// data for team of kind:TeamParamsAdapted
	final static int totalTeamsParamsAdapted = 2;
	final static int[] iterationTimeTeamParamsAdapted = { 20000, 15000 };// by iteration in millisecond
	final static int[] totalAdaptationsTeamParamsAdapted = { 15, 20 };
	final static int[] requestPolicyTeamParamsAdapted = { SolutionPopulation.REQUEST_RANDOM,
			SolutionPopulation.REQUEST_SAME };
	final static int[] entryPolicyTeamParamsAdapted = { SolutionPopulation.ENTRY_IF_DIFERENT,
			SolutionPopulation.ENTRY_SAME };
	final static int[] solutionSimilarityPercertageTeamParamsAdapted = { 33, 33 };

	// data for team of kind: TeamParamsRandom
	final static int totalTeamsParamsRandom = 1;
	final static int[] iterationTimeTeamParamsRandom = { 20000 };
	final static int[] totalAdaptationsTeamParamsRandom = { 15 };
	final static int[] requestPolicyTeamParamsRandom = { SolutionPopulation.REQUEST_RANDOM };
	final static int[] entryPolicyTeamParamsRandom = { SolutionPopulation.ENTRY_IF_DIFERENT };

	// data for team of kind: TeamParamsFixed
	final static int totalTeamsParamsFixed = 0;
	final static int[] iterationTimeTeamParamsFixed = null;
	final static int[] totalAdaptationsTeamParamsFixed = null;
	final static int[] requestPolicyTeamParamsFixed = null;
	final static int[] entryPolicyTeamParamsFixed = null;
}