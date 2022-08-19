package sfc_a;

public class Setup {
	//Setup for Genetic Algorithm
public double vnf, netstat;
public int genes, generations, supergens, crossprob, mutprob;

public Setup(double vnf, double netstat, int genes, int generations, int supergens, int crossprob, int mutprob) {
	this.vnf=vnf;
	this.netstat=netstat;
	if(genes<10) {
		genes=10;
	}
	if(genes % 2 == 1) {
		genes++;
	}
	if(generations<4) {
		generations=4;
	}
	if(generations % 2 == 1) {
		generations++;
	}
	if(supergens>1) {
		if(supergens % 2 == 1) {
			supergens++;
		}
	}
	this.genes=genes;
	this.generations=generations;
	this.supergens=supergens;
	this.crossprob=crossprob;
	this.mutprob=mutprob;
}

public String getSetup() {
	String s=Integer.toString((int)vnf)+","+
			Integer.toString((int)netstat)+","+
			Integer.toString(genes)+","+
			Integer.toString(generations)+","+
			Integer.toString(supergens)+","+
			Integer.toString(crossprob)+","+
			Integer.toString(mutprob);
	return s;
}
}
