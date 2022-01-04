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
To download the project is only neccesary clone it. To clone used this link: https://github.com/JonathanDuque/QAPMetaheuristic.git


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







