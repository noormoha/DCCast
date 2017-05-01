# DCCast
Adaptive Tree Selection for Efficient Point to Multipoint Transfers Across Datacenters

This repository provides Java code implementing the algorithms presented in "DCCast: Efficient Point to Multipoint Transfers Across Datacenters". This includes a minimal implementation to represent network topologies and point to multipoint (p2mp) transfers, and to generate transfers with uniform or exponential distribution.

# How does the program work
- It generates a list of P2MP transfers from time 0 up to a user specified time each assigned a unique transfer ID. The arrivals follow the Poisson distribution while transfer sizes follow either uniform or exponential (latter by default).
- A random network topology is then generated (GScale topology can also be used easily. It is included in the code).
- The topology along with requests are supplied to the DCCast algorithm. The algorithm receives requests in an online manner in the order of arrival and processes them. We used the library provided by mouton5000 (https://github.com/mouton5000/DSTAlgoEvaluation) for approximation of minimum weight Steiner Trees upon arrival of each request. When a tree is selected, the program prints the tree and then the request is allocated starting from next timeslot using as much of the available bandwidth over all edges of the tree to finish as early as possible (FCFS policy).
- At the end of each slot, the program prints the schedule for next timeslot.

# How to run the program
- All the necessary dependencies are included (the graphTheory directory).
- The file that contains the main function is [dccast/Dccast.java](dccast/Dccast.java).
- The output is simply printed out to the terminal.

# DCCast algorithm
The main algorithm is implemented in three functions all in [dccast/NetworkP2MPWeighted.java](dccast/NetworkP2MPWeighted.java):
- [routeTraffic](https://github.com/noormoha/DCCast/blob/master/dccast/NetworkP2MPWeighted.java#L199)
