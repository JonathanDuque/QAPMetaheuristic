# J-PACAS
## A Java Implementation of PACAS framework
This document is a guide with the steps for setting J-PACAS.


## Introduction
J-PACAS is an implementation in the Java programming language of the framework PACAS, **PA**rameter **C**ontrol **A**daptation for **S**ingle solution metaheuristics
 in a parallel hybrid solver. The complete description of the general framework PACAS and its implementation J-PACAS can be found at the thesis document. Link here...


## System requirements





## Configuration




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







