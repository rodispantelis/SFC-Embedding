package sfc_hg_a;
import java.util.ArrayList;
import java.util.Arrays;

/** Instance of the Genetic Algorithm for the parallel computation of supergenerations*/
public class GA2 {
	
	/** Edge Vector codec */
	Codec cod=new Codec();
	/** arrays for storing populations */
	Mapping pop[]=new Mapping[0], pop2[]=new Mapping[0], pops[]=new Mapping[0],pops2[]=new Mapping[0];
	/** mapping GA output */
	Mapping m;
	/** maximum node bandwidth; for FatTree it is 1Gbps */
	int maxnodeb=1000;
	/** VNF graph */
	services.VNFgraph cnet;
	/** substrate network */
	network.Hypergraph pnet;
	/** substrate nodes */
	public int cnodes;
	/** VNF nodes */
	public int pnodes;
	/** GA parameters */
	double crossprop=1, mutprop=1;
	/** GA parameters */
	int chromes=0, supergen=1, generations=0;			//even numbers only
	/** normalization factors for fitness function; set to 1.0 if not applicable */
	double normaliz=1.0, normaliz2=1.0;
	/** set true to use population generation heuristic */
	boolean popgenheuristic=true;
	/** embedded VNF life cycle duration */
	int duration=0;
	/** min max population fitness */
	double min,max;
	/** population fitness range */
	double dif=10;
	/** current best solution */
	Best best=new Best(cnodes);
	/** best solution produce by the supergenerations */
	Best best2=new Best(cnodes);
	/** is last request rejected? */
	boolean rejection=false;
	/** runtime */
	double totalTime;
	/** node capacity load */
	ArrayList<int[]> nodecharge = new ArrayList<int[]>();
	/** edge traffic load */
	ArrayList<int[]> edgecharge = new ArrayList<int[]>();
	/** available nodes; nodes capable to host the minimum capacity VNF of the chain */
	ArrayList<Integer> availnodes = new ArrayList<Integer>();
	/** GA default setup */
	String defsetup="0";

	/** initialize GA with running parameters */
	public GA2(int gens, int gnrs, int sgnrs, int cr, int mu) {
		this.generations=gnrs;
		this.chromes=gens;
		this.crossprop=cr/100.0;
		this.mutprop=mu/100.0;
	}
	
	/** initialize GA with substrate network object */
	public GA2(network.Hypergraph pnet) {
		this.pnet=pnet;
		pnodes=pnet.getdomains();
	}
	
	/** load VNF graph */
	public void loadVNFgraph(services.VNFgraph vnf) {
		cnet=vnf;
		cnodes=vnf.getnodes();
	}
	
	/** load GA setup */
	public void loadSetup(Setup s, String dsetup) {
		chromes=s.chromes;
		this.pop=new Mapping[chromes];
		generations=s.generations;
		crossprop=((double)s.crossprob)/100.0;
		mutprop=((double)s.mutprob)/100.0;
		defsetup=dsetup;
	}
	
	/** set true to enable population generation heuristic, set false to disable */
	public void setpopgenheuristic(boolean popgenheur) {
		popgenheuristic=popgenheur;
	}

	/** initialize Genetic Algorithm */
	public void init(){
		cnet.demands();
		rejection=false;
		
		best=new Best(cnodes);
		best2=new Best(cnodes);
		
		pops2=new Mapping[supergen];
		pops=new Mapping[supergen];

		for(int s=0;s<supergen;s++){
			best=new Best(cnodes);
			genpop();
			init2();
			superbest(s);
		}
	}
	
	/** execute genetic procedures for current generation */
	public void init2(){
		for(int gs=0;gs<generations;gs++){
				crossover();
				multymutation(1);
				selection();
		}
		range();
	}

	/** set SFC lifetime */
	public void setduration(int d) {
		duration=d;
	}
	
	/** is current request rejected? */
	public boolean isrejected() {
		return rejection;
	}
	
	/** update network traffic and CPU load if request is accepted */
	public void updatetraffic() {
		if(best.getbestfitness()<10000000) {
			int du=(int) (Math.random()*duration);
			pnet.setduration(du);
			pnet.embed(cnet, best.getbestmapping());
		}
	}
	
	/** update currently best mapping */
	public void updatebest(){
		
		for(int b=0;b<pop.length;b++){
			if(best.getbestfitness()>pop[b].getfitness()){
				best.setbestmapping(new int[]{0,0});
				best.setbestfitness(pop[b].getfitness());
				best.setbestmapping(pop[b].getmapping());
				best.setavcap(0,pop[b].getavcap()[0]);
			}else if(best.getbestfitness()<0){
				best.setbestmapping(new int[]{0,0});
				best.setbestfitness(-1);
				best.setbestmapping(pop[b].getmapping());
				best.setavcap(0,pop[b].getavcap()[0]);
			}
		}
		
		for(int b=0;b<pop2.length;b++){
			if(best.getbestfitness()>pop2[b].getfitness()){
				best.setbestmapping(new int[]{0,0});
				best.setbestfitness(pop2[b].getfitness());
				best.setbestmapping(pop2[b].getmapping());
				best.setavcap(0,pop2[b].getavcap()[0]);
			}else if(best.getbestfitness()<0){
				best.setbestmapping(new int[]{0,0});
				best.setbestfitness(-1);
				best.setbestmapping(pop2[b].getmapping());
				best.setavcap(0,pop2[b].getavcap()[0]);
			}
		}
	}
	
	/** save generation best solution */
	public void superbest(int in){
		pops[in]=new Mapping(best.getbestmapping());
		pops[in].setavcap(0,best.getavcap()[0]);
	}
	
	/** save supergeneration best solution */
	public void superbest2(int in){
		pops2[in]=new Mapping(best.getbestmapping());
		pops2[in].setavcap(0,best.getavcap()[0]);
	}
		
	/** compute the range in the fitness of the population */
	private void range(){
		
		min=pop[0].getfitness();
		max=pop[0].getfitness();
		for(int ip=0;ip<pop.length;ip++){
			if(pop[ip].getfitness()<min){
				min=pop[ip].getfitness();
			}
			if(pop[ip].getfitness()>max){
				max=pop[ip].getfitness();
			}
		}
		
		for(int ip2=0;ip2<pop2.length;ip2++){
			if(pop2[ip2].getfitness()<min){
				min=pop2[ip2].getfitness();
			}
			if(pop2[ip2].getfitness()>max){
				max=pop2[ip2].getfitness();
			}
		}
		dif=max-min;
	}
	
	/** Selection */
	private void selection(){
		
		if(dif>0.0){
			ArrayList<Mapping> newpop = new ArrayList<Mapping>();//temp new pop storage
			newpop.clear();
			
			//adds in new pop the gene with the best fitness
			while(newpop.size()<chromes){
				double p = Math.random();
				double c=(double)min+(p*(double)dif);
				for(int sel=0;sel<pop.length;sel++){
					if(newpop.size()>(chromes-1)){
						break;
					}else if(pop[sel].getfitness()<c){
						newpop.add(pop[sel]);
					}
				}
			
				for(int sel=0;sel<pop2.length;sel++){
					if(newpop.size()>(chromes-1)){
						break;
					}else if(pop2[sel].getfitness()<c){
						newpop.add(pop2[sel]);
					}
				}
			}

			pop=newpop.toArray(new Mapping[chromes]);
			pop2=new Mapping[0];
		}
	}
	
	/** Crossover */
	private void crossover(){
		
		if(dif>0.0){
			ArrayList<Mapping> crosspop = new ArrayList<Mapping>();
			for(int c=0;c<pop.length && pop.length>=2;c=c+2){
				double prop = Math.random();
				if(prop<=crossprop){
					double rplace = Math.random()*(pop[0].getmapping().length);
					int place=(int) (Math.round(rplace));
					String s1[]=pop[c].getstringmap();
					String s2[]=pop[c+1].getstringmap();
					Mapping temp1= new Mapping();
					Mapping temp2= new Mapping();
					temp1.strtomap(s1);
					temp2.strtomap(s2);
	
					for(int t1=0;t1<place;t1++){
						int temp=temp1.getmapping()[t1];
						temp1.change(t1,temp2.getmapping()[t1]);
						temp2.change(t1,temp);
					}
						crosspop.add(temp1);
						crosspop.add(temp2);
				}
			}	
			
			pop2=crosspop.toArray(new Mapping[crosspop.size()]);
			genfit1(pop2);
		}
	}
	
	/** multiple point Mutation */
	private void multymutation(int d){
		for(int c=0;c<d;c++){
			mutation();
		}
	}
	
	/** Mutation */
	private void mutation(){
		ArrayList<Mapping> newpop = new ArrayList<Mapping>();
		newpop.clear();
		
		boolean m=false;
		for(int c=0;c<pop.length;c++){
			double prop1 = Math.random();
			if(prop1<=mutprop){
				double prop2 = Math.random();
				int p01=(int)(availnodes.size()*prop2);
				int p1=availnodes.get(p01);
				int p2=(int)(cnodes*prop2);
				if(p2 < cnodes) {
					newpop.add(new Mapping(pop[c].changem(p2, p1)));
					m=true;
				}
			}
		}
		if(m){
			for(int in=0;in<pop2.length;in++){
				newpop.add(new Mapping(pop2[in].getstringmap()));
			}
			pop2=newpop.toArray(new Mapping[newpop.size()]);
			genfit1(pop2);
		}
	}
	
	/** determines the nodes that will be used for population generation */
	public void genpop(){
		if(availnodes.size()>0) {
			genpop2();
		}else {
			//no suitable nodes found
			rejection=true;
		}
	}
	
	/** generates population */
	public void genpop2(){
		//the first d genes are generated heuristically and cover search space uniformly
		int d=0;
		
		if(popgenheuristic) {
			d=pnet.getdomains();
		
			if(d>chromes){
				d=chromes*8/10;
			}
		}
		
		for(int t0=0;t0<d;t0++){
			if((pnet.getdomain(t0).getavailablenodecpu()[0])/pnet.getdomain(t0).getservers() >= 
					cnet.cpugetdemand()/cnet.nodes) {
				int[] m=new int[cnodes];
				for(int t=0;t<cnodes;t++){
					m[t]=t0;
				}
				if(t0<pop.length){
					pop[t0]= new Mapping(m);
				}
			}else {
				Mapping map1 = new Mapping(cnodes, availnodes);
				if(t0<pop.length){
					pop[t0]=map1;
				}
			}
		}
			
		//the rest of the population is randomly generated
		for(int g=d; g<pop.length;g++){
			Mapping map = new Mapping(cnodes, availnodes);
			if(g<pop.length){
				pop[g]=map;
			}
		}
		
		genfit1(pop);
	}
	
	/** generation of random population without the use of a heuristic*/
	public void genpop_v1(){
		//generates population randomly
		int d=0;

		for(int g=d; g<chromes;g++){
			Mapping map = new Mapping(cnodes, pnodes);
			if(g<pop.length){
				pop[g]=map;
			}
		}

		genfit();
	}
	
	/** compute fitness for population */
	public void genfit(){
		genfit1(pop);
		genfit1(pop2);
	}
	
	/** compute and store fitness on chromosome */
	public void genfit1(Mapping somepop[]){
		//compute fitness
		
		//delete fitness from pop array
		for(int i00=0;i00<somepop.length;i00++){
			somepop[i00].setfitness(0);
		}
		
		//compute fitness for each chromosome of the population
		for(int i1=0;i1<somepop.length;i1++){
			genefitness(somepop[i1]);
		}		

		range();
		updatebest();
	}

	/** compute fitness on given mapping */
	public void genefitness(Mapping m) {
		//compute and store fitness in input mapping
		int[] mapping=m.getmapping();
		double ff=0.0;
		m.setfitness(0);
		boolean reject=false;
		
		//check for validity
		if(cnet.getnodes()==mapping.length) {
			reject=pnet.checkembed(cnet, mapping);
		}else {
			reject=true;
		}

		ArrayList<Integer> nodes=new ArrayList<Integer>();
		ArrayList<Double> demands=new ArrayList<Double>();
		
		if(!reject) {
			for(int i=0;i<mapping.length;i++) {
				boolean found=false;
				for(int n=0;n<nodes.size() && !found;n++) {
					if(nodes.get(n)==mapping[i]) {
						demands.set(n, (demands.get(n)+cnet.getnodew()[i]));
						found=true;
					}
				}
				if(!found) {
					nodes.add(mapping[i]);
					demands.add(cnet.getnodew()[i]*1.0);
				}
			}
		
			for(int f=0;f<nodes.size() && !reject;f++) {
				Double t=pnet.getdomain(nodes.get(f)).gettotalcpu()-demands.get(f);
				ff+=normaliz*t;
			}
		}else {
			ff=10000000;
		}
		
		if(!reject) {
			for(int l=0;l<cnet.getgraph().length;l++) {
				if(cnet.getgraph()[l]>0) {	
					int[] t1=cod.decoder(l);
					double vnfhops=0.0;
					double minband=0.0;
					
					if(mapping[t1[0]]!=mapping[t1[1]]) {
						int[] t2=pnet.getdomainpath(mapping[t1[0]],mapping[t1[1]]);
						vnfhops=1.0*(t2.length-2);
						minband=pnet.getband(t2[0],t2[1]);
						
						for(int tt=1;tt<(t2.length-1) && !reject;tt++) {
							if(t2[tt]!=t2[tt+1]) {
								if(minband>pnet.getband(t2[tt],t2[tt+1])) {
									minband=pnet.getband(t2[tt],t2[tt+1]);
								}
							}
						}

						double tff=normaliz2*((vnfhops*minband)-(cnet.getedgew()[l]/1000.0));
						ff+=tff;
					}
				}
			}
		}

		// spatial constraints
		if(!reject) {
			for(int h=0;h<m.getmapping().length;h++) {
				if(cnet.getspatial()[h] >= 0 && (cnet.getspatial()[h] != m.getmapping()[h])) {
					ff+=(pnet.gethopcount(cnet.getspatial()[h], m.getmapping()[h]) +2 -1)*10000.0;
				}
			}
		}

		if(!reject) {
			m.setfitness(ff);
		}else {
			m.setfitness(10000000);
		}

		nodes.clear();
		demands.clear();
	}
	
	public void printbest() {
		System.out.println(best.getbestfitness()+"|"+Arrays.toString(best.getbestmapping()));
	}
	
	/** node is in available nodes */
	public boolean isinavailable(int a) {
		boolean res=false;
		
		for(int av=0;av<availnodes.size();av++) {
			if(a==availnodes.get(av)) {
				res=true;
			}
		}
		
		return res;
	}
	
	/** print population */
	public void printgenes(){
		//print genes
		for(int pr=0;pr < pop.length;pr++){
			System.out.println(java.util.Arrays.toString(pop[pr].getmapping())+"|"+pop[pr].getfitness());
		}
		
		System.out.println("---");
		if(pop2.length>0){
			for(int pr2=0;pr2 < pop2.length;pr2++){
				System.out.println(java.util.Arrays.toString(pop2[pr2].getmapping())+"|"+pop2[pr2].getfitness());
			}
		}
	}
	
	/** get runtime */
	public double gettotaltime() {
		return totalTime;
	}
	
	/** print super best */
	public void printsuper(){
		for(int pr=0;pr < pops.length;pr++){
			System.out.println(java.util.Arrays.toString(pops[pr].getmapping())+"|"+pops[pr].getfitness());
		}		
	}
	
	//getters setters
	
	/** get available nodes */
	public void setavnodes(ArrayList<Integer> a) {
		availnodes=a;
	}
	
	public Best getbest() {
		return best;
	}
	
	public Best getbest2() {
		return best2;
	}
	
	public Mapping[] getpop(){
		return pop;
	}
	
	public Mapping[] getpop2(){
		return pop2;
	}
	
	public Mapping[] getpops(){
		return pops;
	}
	
	public Mapping[] getpops2(){
		return pops2;
	}
}