package sfc_a;
	
	/** setup for Genetic Algorithm */
public class Setup {

	/** PAGA parameter */
	public double vnf, netstat;
	/** Genetic Algorithm parameter */
	public int chromes, generations, supergens, crossprob, mutprob;

	/** construct Genetic Algorithm parameter setup */
	public Setup(double vnf, double netstat, int chromes, int generations, int supergens, int crossprob, int mutprob) {
		this.vnf=vnf;
		this.netstat=netstat;
		if(chromes<10) {
			chromes=10;
		}
		if(chromes % 2 == 1) {
			chromes++;
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
	
		this.chromes=chromes;
		this.generations=generations;
		this.supergens=supergens;
		this.crossprob=crossprob;
		this.mutprob=mutprob;
	}

	/** get setup */
	public String getSetup() {
		String s=Integer.toString((int)vnf)+","+
			Integer.toString((int)netstat)+","+
			Integer.toString(chromes)+","+
			Integer.toString(generations)+","+
			Integer.toString(supergens)+","+
			Integer.toString(crossprob)+","+
			Integer.toString(mutprob);
		return s;
	}
}
