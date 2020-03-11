Problems are from various sources, 
mainly from QAPLIB http://anjos.mgi.polymtl.ca/qaplib/inst.html
which provides file in .dat format:

SIZE
flow/weight matrix (SIZE x SIZE values)
distance matrix (SIZE x SIZE values)

I have extended this format (.qap), only changing the first line (so most code remain compatible since the first line is treated separately). New format is

SIZE  OPT  BKS

    if OPT < 0 then OPT is unknown but a lower bound is known to be -OPT
    BKS: the best known solution

flow/weight matrix (SIZE x SIZE values)
distance matrix (SIZE x SIZE values)



file .sln are solutions (provided by QAPLIB or others)
SIZE COST
vector of SIZE values


RAND problems (Microarray design)
http://www.rahmannlab.de/research/microarray-design

used in: SIMD tabu search for the quadratic assignment problem with graphics
hardware acceleration (Weihang Zhua; James Currya; Alberto Marqueza)

refered as 


MA144 and 6x6bl,...

Border Length Minimization

Chip    Name                                    Dim(n)  Best known solution     Other solutions
size

6x6     RAND-S-6x6-36-25-AFFY-00_rand_rand_bl      36    3,296 (GATS)             3,304 (RTL-2)    3,352 (GRASP-PR)
7x7     RAND-S-7x7-49-25-AFFY-00_rand_rand_bl      49    4,564 (GATS)             4,580 (RTL-1)    4,660 (GRASP-PR)
8x8     RAND-S-8x8-64-25-AFFY-00_rand_rand_bl      64    6,048 (GATS)             6,080 (RTL-1)    6,200 (GRASP-PR)
9x9     RAND-S-9x9-81-25-AFFY-00_rand_rand_bl      81    7,644 (GATS)             7,900 (GRASP-PR)
10x10   RAND-S-10x10-100-25-AFFY-00_rand_rand_bl  100    9,432 (GATS)             9,684 (GRASP-PR)
11x11   RAND-S-11x11-121-25-AFFY-00_rand_rand_bl  121   11,640 (GATS)            12,032 (GRASP-PR)
12x12   RAND-S-12x12-144-25-AFFY-00_rand_rand_bl  144   13,832 (GATS)            14,196 (GRASP-PR)


Conflict Index Minimization

Chip    Name                                    Dim(n)  Best known solution     Other solutions
size

6x6     RAND-S-6x6-36-25-AFFY-00_rand_rand_ci      36   169,016,907 (GATS)       169,925,219 (GRASP-PR)
7x7     RAND-S-7x7-49-25-AFFY-00_rand_rand_ci      49   237,077,377 (GATS)       238,859,844 (GRASP-PR)
8x8     RAND-S-8x8-64-25-AFFY-00_rand_rand_ci      64   326,696,412 (GATS)       327,770,071 (GRASP-PR)
9x9     RAND-S-9x9-81-25-AFFY-00_rand_rand_ci      81   428,682,120 (GATS)       434,317,170 (GRASP-PR)
10x10   RAND-S-10x10-100-25-AFFY-00_rand_rand_ci  100   525,401,670 (GATS)       532,573,788 (GRASP-PR)
11x11   RAND-S-11x11-121-25-AFFY-00_rand_rand_ci  121   658,317,466 (GATS)       664,137,090 (GRASP-PR)
12x12   RAND-S-12x12-144-25-AFFY-00_rand_rand_ci  144   803,379,686 (GATS)       813,127,758 (GRASP-PR)
pages de codes
http://www.seas.upenn.edu/qaplib/codes.html
http://www.adaptivebox.net/CILib/code/qapcodes_link.html





other  instances

Taillard
http://mistic.heig-vd.ch/taillard/problemes.dir/qap.dir/qap.html


chip: hard
http://gi.cebitec.uni-bielefeld.de/comet/chiplayout/qap




 http://www.proin.ktu.lt/~gintaras/qproblem.html (all_inst.zip)
Instxx
size n 	Optimal value
20 	81536
30 	271092
40 	837900
50 	1840356
60 	2967464
70 	5815290
80 	6597966
100 	15008994
150 	58352664
200 	75405684 






Grey instances (http://mistic.heig-vd.ch/taillard/problemes.dir/qap.dir/taillard_gris.dir/)
Comparison of Iterative Searches for the Quadratic Assignment Problem - Taillard 1995

The files grey16_16-base.dat (resp. grey8_8-base.dat) may be used to create many
other instances. Let m be a positive integer lower than 256 (resp. 64).
The first matrix of these files is replaced by a matrix with coefficients :

f[i,j] = 1 if i<m and j<m (i, j = 0, ..., m-1)
       = 0 otherwise

These new problems are named grey16_16_m (grey8_8_m).

Good solutions of many of these generic problems are given in the files
grey8_8.sols and grey16_16.sols

The format of these file is :
m, solution value, solution (i.e. permutation)
