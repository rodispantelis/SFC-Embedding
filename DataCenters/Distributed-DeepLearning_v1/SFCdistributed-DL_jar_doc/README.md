# Distributed Deep Learning algorithm

DL for SFC-Embedding. Its operation is determined by three editable files.

* **SFCdistrDL.jar**. The DL algorithm implemented in .jar executable.
* **Parameters**. Specify the substrate network topology and the parameters of running the simulation. Provides the choice of running pretrained models or a distributed greedy algorithm for comparison with DL.
* **nnprofile**. The NN architecture and parameters of operation. Defines the type of the network; the number and size of its layers; the activation functions;bias and edge weight domain
* **gasettings**. Parameters of running the training Genetic Algorithm.

Besides network simulation output, the algorithm also outputs the **model.csv** file that includes the models that were generated during simulation.

[More details.](https://rodispantelis.github.io/SFC-Embedding/DataCenters)