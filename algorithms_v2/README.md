# SFC-Embedding version 2

Algorithms and simulation tools for Service Function Chain Embedding in Data Centers. version 2-12.2024.

* **[jar](jar)**. Executable .jar and parameter files.

* **[src](src)**. Source code in Eclipse projects.

* **[EV-graphs](EVgraphs)**. Pre-defined Service Function Chains for simulation.

A simple way to test these algorithms is to download the whole repository and run the executable [.jar files](jar), 
from console (use jdk-18 or higher) without changing the file structure.

```console

cd jar

java -Xmx4g -jar SFC-E_Simulation_v2.jar

```

The simulation procedure generates log file named *simulationresult-distr-0.csv*
<details>
<summary>see fields description</summary>

1.Request serial number

2.Hosted VNFs

3.Embedded Service Function Chains

4.Used bandwidth

5.Available bandwidth

6.Used cpu

7.Used servers; servers that host some VNF

8.Remaining cpu

9.Intra-rack traffic

10.Inter-rack traffic

11.Intra-server virtual traffic

12.Is last embedded rejected?

13.Acceptance ratio

14.Request revenue

15.Embedding cost

16.Cost/Revenue ratio

17.Used physical links; links with traffic

18.Size of VNF-graph

19.Remaining intra-rack bandwidth

20.Remaining outer-rack bandwidth

21.Request ID
</details>

[Documentation.](https://rodispantelis.github.io/SFC-Embedding/algorithms_v2/index.html)

