# SATsolver
0. Chemical storage arrangement
A chemical lab in California has to store some chemical solutions in the special containers. There are m
containers available to use. Each container can store up to n chemical bottles. The number of chemicals
needed to be stored is also n.
The chemists know that some chemicals cannot be stored in the same container, otherwise the chemical
reaction might cause an explosion. However, some chemicals must be stored in the same container to
maintain the system equilibrium.
Your task is to help the chemists arrange the storage so that there won’t be any explosion or any drastic
change in the equilibrium inside the container. You will write a program to encode the problem into SAT
and implement SAT solver to solve it.

Example 1
There are 4 chemicals A, B, C, D and 2 special containers. The following constraints apply:
-A cannot be stored with B but may be stored with everything else.
-B must be stored with D.
We can represent the relationships between A, B, C, and D as follows:
Constraint list:
Yes list 
B - D
No list
A - B
Constrain table:
  A  B  C  D
A   -1  0  0
B -1    0  1
C  0 0     0
D  0 1  0 

In this case, a sample solution would be,
Container 1: A C
Container 2: B D

Example 2
There are 5 chemicals E, F, G, H, I and 2 special containers. The following constraints apply:
-E cannot be stored with G.
-H cannot be stored with I.
-F cannot be stored with E.
-F must be stored with I.
-H must be stored with G.
We can represent the relationships between A, B, C, and D as follows:
Constraint list:
Yes listt
F - I
H - G
No list
E - G
H - I
F - E
Constrain table:
   E  F  G  H  I
E    -1 -1  0  0
F -1     0  0  1
G -1  0     1 -1
H  0  0  1    -1
I  0  1  0 -1 
In this case, there is no solution.

SAT Solver:
Now you have both the resolution and the local search algorithms. You can test your program by trying
to solve a simple problem given in the beginning of this assignment. We will test your program with
different problem instances.

Question 8 
We’re going to see how the number of chemicals in the No list (the number of rival chemicals) affect the
difficulty of arrangement.
● Generate 50 random sentences for each setting of n which ranges from 0.02 to 0.2 at an interval
  of 0.02. Fix N, M, f to N=16, M=2, y=0.0. Plot a graph of P(satisfiability) versus n using
  both PLResolution
  and WalkSAT to determine satisfiability. Set p=0.5 and max_flips=100
  for WalkSAT.
● Include the graph in your report and discuss the result. You can use any program to plot the
  graph.
● Which algorithm do you prefer to prove satisfiability? Why?

Question 9
  This time we’ll see how the number of chemicals in the Yes list (the number of mustbetogether
  chemicals) affect the difficulty of arrangement.
● Generate 100 random sentences for each setting of y which ranges from 0.02 to 0.2 at an
  interval of 0.02. Fix N, M, n to N=16, M=2, n=0.05. Plot a graph of P(satisfiability) versus
  y using only WalkSAT to determine satisfiability. Set p=0.5 and max_flips=1000 for
  WalkSAT.
● Include the graph in your report and discuss the result. You can use any program to plot the
  graph.
  
Question 10
Lastly we’ll look at the ratio of clause/symbol of the SAT instance.
● Set y=0.02, n=0.02, p=0.5, max_flips=1000. Generate random instances on the following
  settings of N, M and run WalkSAT to test its satisfiability until 20 satisfiable sentences are
  generated for each setting:
  ○ N=16, M=2
  ○ N=24, M=3
  ○ N=32, M=4
  ○ N=40, M=5
  ○ N=48, M=6
● Record an average runtime it takes to generate 20 satisfiable sentences for each setting. The
  runtime is the number of iterations (the value of i) in the WalkSAT algorithm (referred to AIMA
  Figure 7.18).
● Plot the average runtime versus the average ratio of clause/symbol for each setting.
● Include the graph in your report and discuss the result. Compare your graph with Figure 7.19
  (b).
  
SATSolverEC:
If we add one more constraint which is that the container can only store up to K chemicals where K<N, encode this constraint into a set of CNF clauses. How many clauses do we need? add K as an extra parameter.
