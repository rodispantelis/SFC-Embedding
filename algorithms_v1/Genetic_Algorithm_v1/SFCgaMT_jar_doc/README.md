# Genetic Algorithm v.1

Genetic Algorithm for SFC-Embedding. Its operation is determined by three editable files.

* **SFCdistrDL.jar**. The GA algorithm implemented in .jar executable.
* **Parameters**. Specify the substrate network topology and the parameters of running the Genetic Algorithm.
* **Settings**. Parameters of running the Parameter Adjustment Genetic Algorithm (PAGA).
* **Profile**. Configuration of the Parameter Adjustment procedure. The boundaries of the produced solutions by PAGA.

Besides network simulation output, the algorithm outputs two more files.
* **adaptationlog.csv**. The adaptation of optimal setup for the operation of the algorithm during simulation.
* **knowledgeDB.csv**. Optimal setups that were computed by Parameter Adjustment Genetic Algorithm (PAGA) during simulation.

run:

```console

java -Xmx4g -jar SFCgaMT.jar

```

[More details.](https://rodispantelis.github.io/SFC-Embedding/DataCenters)