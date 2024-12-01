package sfc_hg_c;
import java.util.ArrayList;

/** Genetic Algorithm, used by PAGA for computing the fitness of candidate setups */
public class GA_c {
	
	/** Edge Vector codec */
	Codec_c cod=new Codec_c();
	/** arrays for storing populations */
	Mapping_c pop[], pop2[]=new Mapping_c[0], pops[],pops2[];
	/** mapping GA output */
	public Mapping_c m;
	/** substrate nodes */
	public int cnodes;
	/** VNF nodes */
	public int pnodes;
	/** maximum node bandwidth; for FatTree it is 1Gbps */
	int maxnodeb=1000;
	/** substrate network */
	network.Hypergraph pnet;
	/** VNF-graph */
	services.VNFgraph cnet;
	/** GA parameters */
	double crossprop=1, mutprop=1;
	/** GA parameters */
	int chromes=0, supergen=0, generations=0;		//even numbers only
	/** min max population fitness */
	double min,max;
	/** population fitness range */
	double dif=10;
	/** current best solution */
	public Best_c best=new Best_c(cnodes);
	/** best solution produce by supergenerations */
	Best_c best2=new Best_c(cnodes);
	/** set true to use population generation heuristic */
	boolean popgenheuristic=true;
	/** is last request rejected? */
	boolean rejection=false;
	/** node capacity load */
	ArrayList<int[]> nodecharge = new ArrayList<int[]>();
	/** edge traffic load */
	ArrayList<int[]> edgecharge = new ArrayList<int[]>();
	/** available nodes; nodes capable to host the minimum capacity VNF of the chain */
	ArrayList<Integer> availnodes = new ArrayList<Integer>();
	/** normalization factors for fitness function; set to 1.0 if not applicable */
	double normaliz=1.0, normaliz2=1.0;

	/** construct GA object on input of running parameters, virtual topology and substrate network */
	public GA_c(int chromes, int gnrs, int sgnrs, int cr, int mu, 
			services.VNFgraph incnet, network.Hypergraph inpnet) {
		this.generations=gnrs;
		this.supergen=sgnrs;
		this.chromes=chromes;
		this.pop=new Mapping_c[chromes];
		this.crossprop=cr/100.0;
		this.mutprop=mu/100.0;
		this.cnet=incnet;
		this.pnet=inpnet;
		cnodes=cnet.nodes;
		pnodes=pnet.getdomains();
		pop=new Mapping_c[chromes];
	}
	
	public GA_c() {
		
	}
	
	/** set GA parameters */
	public void setparams(int chrmes, int gnrs, int sgnrs, int cr, int mu){
		chromes=chrmes;
		pop=new Mapping_c[chromes];
		generations=gnrs;
		supergen=sgnrs;
		crossprop=cr/100.0;
		mutprop=mu/100.0;
	}
	
	/** print GA parameters */
	public void printparams(){
	System.out.println("P:"+pnodes+" C:"+cnodes+" Chr:"+chromes+" Gens:"+generations+" Sgens:"+supergen);
	}
	
	/** initialize Genetic Algorithm */
	public void init(){

		best=new Best_c(cnodes);
		best2=new Best_c(cnodes);
		
		pops2=new Mapping_c[supergen];
		for(int t=0;t<supergen;t++){

			pops=new Mapping_c[supergen];
			for(int s=0;s<supergen;s++){
				best=new Best_c(cnodes);
				genpop();
				init2();
				superbest(s);
			}
			best=new Best_c(cnodes);
			pop=pops;
			init2();
			superbest2(t);
		}
		
		if(supergen>1) {
			best=new Best_c(cnodes);
			pop=pops2;
			init2();
		}
		
	}
	
	/** execute genetic procedures for current generation */
	public void init2(){
		genfit();
		for(int gs=0;gs<generations;gs++){	
			if(dif<1){
			}else{
				crossover();
				multymutation(1);
				selection();
			}
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
				best.setbestfitness(pop[b].getfitness());
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
				best.setbestfitness(pop2[b].getfitness());
				best.setbestmapping(pop2[b].getmapping());
				best.setavcap(0,pop2[b].getavcap()[0]);
			}
		}
	}
	
	/** save generation best solution */
	public void superbest(int in){
		pops[in]=new Mapping_c(best.getbestmapping());
		pops[in].setavcap(0,best.getavcap()[0]);
	}
	
	/** save supergeneration best solution */
	public void superbest2(int in){
		pops2[in]=new Mapping_c(best.getbestmapping());
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

		if(dif>0){
			ArrayList<Mapping_c> newpop = new ArrayList<Mapping_c>();//temp new pop storage
			newpop.clear();

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
			
			pop=newpop.toArray(new Mapping_c[chromes]);
			pop2=new Mapping_c[0];
		}
	}
	
	/** Crossover */
	private void crossover(){

		if(dif>0){
			ArrayList<Mapping_c> crosspop = new ArrayList<Mapping_c>();
			for(int c=0;c<pop.length;c=c+2){
				double prop = Math.random();
				if(prop<=crossprop){
					double rplace = Math.random()*(pop[0].getmapping().length);
					int place=(int) (Math.round(rplace));
					String s1[]=pop[c].getstringmap();
					String s2[]=pop[c+1].getstringmap();
					Mapping_c temp1= new Mapping_c();
					Mapping_c temp2= new Mapping_c();
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
			
			pop2=crosspop.toArray(new Mapping_c[crosspop.size()]);
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
		
		ArrayList<Mapping_c> newpop = new ArrayList<Mapping_c>();
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
					newpop.add(new Mapping_c(pop[c].changem(p2, p1)));
					m=true;
				}
			}
		}
		if(m){
			for(int in=0;in<pop2.length;in++){
				newpop.add(new Mapping_c(pop2[in].getstringmap()));
			}
			pop2=newpop.toArray(new Mapping_c[newpop.size()]);
			genfit1(pop2);
		}
	}
	
	/** determines the nodes that will be used for population generation */
	public void genpop(){
		//determine available nodes and store them in an ArrayList
		//nodes the available capacity of which is larger than the minimum demand of the VNFs
		availnodes.clear();
		
		for(int s=0;s<pnet.getdomains();s++) {
			if(pnet.getdomain(s).getavailablenodecpu()[0] >= cnet.getminnodew()) {
				availnodes.add(s);
			}
		}
		
		if(availnodes.size()>0) {
			genpop2();
		}else {
			//no suitable nodes found
			rejection=true;
		}
	}
	
	/** generates population */
	public void genpop2(){
		//generates population
		//the first genes are generated heuristically and cover search space uniformly
		int d=0;
		
		if(popgenheuristic) {
			d=pnet.getdomains();
		
			if(d>chromes){
				d=chromes*8/10;
			}
		}
		
		for(int t0=0;t0<d;t0++){
			if(pnet.getdomain(t0).getavailablenodecpu()[0] >= cnet.getminnodew()) {
				int[] m=new int[cnodes];
				for(int t=0;t<cnodes;t++){
					m[t]=t0;
				}
				if(t0<pop.length){
					pop[t0]= new Mapping_c(m);
				}
			}else {
				Mapping_c map1 = new Mapping_c(cnodes, availnodes);
				if(t0<pop.length){
					pop[t0]=map1;
				}
			}
		}
			
		//the rest of the population is randomly generated
		for(int g=d; g<pop.length;g++){
			Mapping_c map = new Mapping_c(cnodes, availnodes);
			if(g<pop.length){
				pop[g]=map;
			}
		}
		
		genfit1(pop);
	}
	
	/** compute fitness for population */
	public void genfit(){
		genfit1(pop);
		genfit1(pop2);
	}
	
	/** compute and store fitness on chromosome */
	public void genfit1(Mapping_c somepop[]){
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
	public void genefitness(Mapping_c m){ //last working
		int[] mapping=m.getmapping();
		int ff=0;
		m.setfitness(0);
		boolean reject=false;
		
		//check for validity
		reject=pnet.checkembed(cnet, mapping);
		
		//System.out.println(1);
		
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
		

		if(!reject) {
			m.setfitness(ff);
		}else {
			m.setfitness(10000000);
		}
		
		nodes.clear();
		demands.clear();
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
	
	/** print super best */
	public void printsuper(){
		//print super best
		for(int pr=0;pr < pops.length;pr++){
			System.out.println(java.util.Arrays.toString(pops[pr].getmapping())+"|"+pops[pr].getfitness());
		}		
	}

}