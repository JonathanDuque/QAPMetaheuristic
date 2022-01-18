# J-PACAS
## A Java Implementation of PACAS framework
This document is a guide with the steps for setting J-PACAS. It is possible to download the project and execute in whatever architecture that supported a shared memory model.


## Introduction
J-PACAS is an implementation in the Java programming language of the framework PACAS, **PA**rameter **C**ontrol **A**daptation for **S**ingle solution metaheuristics
 in a parallel hybrid solver. The complete description of the general framework PACAS and its implementation J-PACAS can be found at the thesis document. Link here...


## System requirements
J-PACAS framework is implemented in Java 11 using the *ForkJoinPool* and *AtomicType* classes to handle the parallelism in a shared memory model. 

We recommended to have installed at least a java 11 version or higher. However, we think that is enough a version of java that supports the classes for handling the parallelism, these are: *ForkJoinPool* and *AtomicType*.


## Download
To download the project is only necessary clone it. To clone used this link: https://github.com/JonathanDuque/QAPMetaheuristic/tree/framework


## Configuration
Once the project is cloned, it is time to configure the parameters for execution.

Inside the project you can find the file AlgorithmConfiguration.java. This file contains all the parameters to setup the algorithm. There are two sets of variables. The first set is related to all those general variables of the framework and the second set has to do with those variables related to the configuration of each type of team.

##### General parameters
The set of general variables includes the problem name, the team size, the total timeout in seconds, and some flags to set if the results are printed in console and/or a CSV file.

The default values for these variables are:

```bash
	final static String problemName = "bur26a.qap";
	final static int totalTimeOut = 300; // time in seconds
	final static int teamSize = 3;// should be multiple of DIFFERENT_MH
	final static boolean printResultInConsole = true;
	final static boolean printResultInCSVFile = true;
```


##### Team parameters
This set of parameters are related to each kind of team available to configure. The type of teams available are: *FixedParamsTeam*, *RandomParamsTeam*, and *AdaptedParamsTeam*. Therefore, there are 3 variables to define the number team of each type, these are: totalAdaptedParamsTeams, totalRandomParamsTeams, and totalFixedParamsTeams.

There are other data related to each team, these variables are: the iteration time, the total adaptations, the request policy, the entry policy and the solution similarity percentage. An example configuration for the *AdaptedParamsTeam* is presented next:

```bash
	final static int totalAdaptedParamsTeams = 2;
	final static int[] iterationTimeAdaptedParamsTeam = { 20000, 15000 };// by iteration in millisecond
	final static int[] totalAdaptationsAdaptedParamsTeam = { 15, 20 };
	final static int[] requestPolicyAdaptedParamsTeam = { SolutionPopulation.REQUEST_RANDOM,
			SolutionPopulation.REQUEST_SAME };
	final static int[] entryPolicyAdaptedParamsTeam = { SolutionPopulation.ENTRY_IF_DIFERENT,
			SolutionPopulation.ENTRY_SAME };
	final static int[] solutionSimilarityPercentageAdaptedParamsTeam = { 33, 33 };
```

The example shows the configuration of two teams of type *AdaptedParamsTeam*. It is also possible to see that there are integer array variables, each position in the array specifies what team of the two teams corresponds the value of the parameter.


##### Request and entry policies
There are 3 types of request policies and 3 types of entry policies.







## Execution

For the execution is necessary generate the .jar file (code.jar)

Example:
Solving the els19.qap  problem, with 30 cores used, parameters adaptation process is triggered every 20000 miliseconds, 15 times.
All the execution has the seed 1 for the generation of random values

```bash
java -jar code.jar els19.qap 30 20000 15 1
```
Even is possible to set these values inside the code. 



## Contribution







