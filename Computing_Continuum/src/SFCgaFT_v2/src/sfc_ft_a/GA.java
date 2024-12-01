package sfc_ft_a;
import java.util.ArrayList;

/** Genetic Algorithm */
public class GA extends Thread{
	
	/** Edge Vector codec */
	Codec cod=new Codec();
	/** arrays for storing populations */
	Mapping pop[], pop2[]=new Mapping[0], pops[],pops2[];
	/** mapping GA output */
	Mapping m;
	/** maximum node bandwidth; for FatTree it is 1Gbps */
	int maxnodeb=1000;
	/** VNF graph */
	services.VNFgraph cnet;
	/** substrate network */
	network.FTnetwork pnet;
	/** substrate nodes */
	public int cnodes;
	/** VNF nodes */
	public int pnodes;
	/** iteration id */
	int d=0;
	/** GA parameters */
	double crossprop=1, mutprop=1;
	/** GA parameters */
	int chromes=0, supergen=0, generations=0;			//even numbers only
	/** normalization factors for fitness function; set to 1.0 if not applicable */
	double normaliz=1.0, normaliz2=1.0;
	/** print mappings on screen */
	boolean printmapping=true;
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
	/** Running parameter setup */
	Setup setup;
	/** Check for active threads */
	boolean[] activethreads;

	/** initialize GA with running parameters */
	public GA(int gens, int gnrs, int sgnrs, int cr, int mu) {
		this.generations=gnrs;
		this.supergen=sgnrs;
		this.chromes=gens;
		this.crossprop=cr/100.0;
		this.mutprop=mu/100.0;
	}
	
	/** initialize GA with substrate network object */
	public GA(network.FTnetwork pnet) {
		this.pnet=pnet;
		pnodes=pnet.getservers();
	}
	
	/** load VNF graph */
	public void loadVNFgraph(services.VNFgraph vnf) {
		cnet=vnf;
		cnodes=vnf.getnodes();
	}
	
	/** load GA setup */
	public void loadSetup(Setup s, String dsetup) {
		this.setup=s;
		chromes=s.chromes;
		this.pop=new Mapping[chromes];
		supergen=s.supergens;
		generations=s.generations;
		crossprop=((double)s.crossprob)/100.0;
		mutprop=((double)s.mutprob)/100.0;
		defsetup=dsetup;
	}
	
	/** set true to enable population generation heuristic, set false to disable */
	public void setpopgenheuristic(boolean popgenheur) {
		popgenheuristic=popgenheur;
	}
	
	/** print GA parameters */
	public void printparams(){
	System.out.println("P:"+pnodes+" Chr:"+chromes+" Gens:"+generations+" Sgens:"+supergen
			+" crossprob:"+crossprop+" mutprob:"+mutprop);
	}

	/** initialize Genetic Algorithm */
	public void init(){
		
		activethreads=new boolean[supergen];
		for(int aa=0;aa<supergen;aa++) {
			activethreads[aa]=true;
		}
		
		for(int av=0;av<pnet.getservers();av++) {
			if(pnet.getserver(av).getavailablecpu()>=cnet.getminnodew()) {
				availnodes.add(av);
			}
		}
		if(availnodes.size()>1) {	
			rejection=false;

			long startTime = System.nanoTime();
		
			best=new Best(cnodes);
			best2=new Best(cnodes);
			pops=new Mapping[supergen];
		
			pops2=new Mapping[supergen];
		
			for(int t=0;t<supergen;t++){

				for(int s=0;s<supergen;s++){
					activethreads[s]=true;
					(new supergenthread()).start(s);
				}
			
				//check for active threads
				boolean actives=true;
				while(actives) {
					System.out.print("");
					actives=false;
					for(int at=0;at<supergen;at++) {
						if(activethreads[at]) {
							actives=true;
							break;
						}
					}
				}
			
				best=new Best(cnodes);
				pop=pops;
				genfit();
				init2();
				superbest2(t);
			
			}
		
			if(supergen>1) {
				best=new Best(cnodes);
				pop=pops2;
				genfit();
				init2();
			
			}

			long endTime=System.nanoTime();
			totalTime = (endTime - startTime)/1000000;
		
			if(best.getbestfitness()>-1 && best.getbestfitness()<10000000) {
				if(printmapping) {
					System.out.println("GA: "+java.util.Arrays.toString(best.getbestmapping()));	
				}
				rejection=false;
			}else {
				rejection=true;
				System.out.println("\n> Rejection");
			}

			updatetraffic();
		}else {
			best.setbestfitness(10000000);
			rejection=true;
			System.out.println("\n> Rejection");
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

	/** print mapping on screen */
	public void printmapping(boolean b) {
		printmapping=b;
	}
	
	/** set SFC lifetime */
	public void setduration(int d) {
		duration=d;
	}
	
	/** set iteration id */
	public void setid(int a) {
		d=a;
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
				Double t=pnet.getserver(nodes.get(f)).getcpu()-demands.get(f);
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
							int[] t2=pnet.getserverpath(mapping[t1[0]],mapping[t1[1]]);
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
	
	/** compute fitness on given mapping experimental version 1 */
	public void genefitness_experimental1(Mapping m) {
		int[] mapping=m.getmapping();
		double ff=0.0;
		m.setfitness(0);
		boolean reject=false;
		
		reject=pnet.checkembed(cnet, mapping);
		
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
				Double t=pnet.getserver(nodes.get(f)).getcpu()-demands.get(f);
				ff+=t;
			}
		}else {
			ff=10000000;
		}
		
		if(!reject) {
			for(int l=0;l<cnet.getgraph().length;l++) {
				if(cnet.getgraph()[l]>0) {	
					int[] t1=cod.decoder(l);
					int vnfhops=0;
					double minband=0.0;
					
					if(mapping[t1[0]]!=mapping[t1[1]]) {
						int[] t2=pnet.getserverpath(mapping[t1[0]],mapping[t1[1]]);
					
						if(t2.length==2) {
							vnfhops=1;
						}else {
							vnfhops=t2.length-2;
						}
						minband=pnet.getband(t2[0],t2[1]);
	
						for(int tt=1;tt<(t2.length-1);tt++) {
							if(t2[tt]!=t2[tt+1]) {
								if(minband>pnet.getband(t2[tt],t2[tt+1])) {
									minband=pnet.getband(t2[tt],t2[tt+1]);
								}
							}
						}

					double tt=(vnfhops*minband)-(cnet.getedgew()[l]/1000.0);
					ff+=tt;
					}
				}
			}
		}
		
		m.setfitness(ff);
		nodes.clear();
		demands.clear();
	}
		
	/** compute fitness on given mapping experimental version 2 */
	public void genefitness_experimental2(Mapping m) {
		int[] mapping=m.getmapping();
		double ff=0.0;
		m.setfitness(0);
		boolean reject=false;
		
		//check for validity
		reject=pnet.checkembed(cnet, mapping);

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
		
			for(int f=0;f<nodes.size() && !reject;f++) {//System.out.println("f"+f);

				Double t=pnet.getserver(nodes.get(f)).getcpu()-demands.get(f);
				if(t<0.0) {
					reject=true;
					ff=10000000;
					break;
				}else {
				ff+=normaliz*t;
				}
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

						int[] t2=pnet.getserverpath(mapping[t1[0]],mapping[t1[1]]);
						vnfhops=1.0*(t2.length-1);
						minband=pnet.getband(t2[0],t2[1]);

						for(int tt=1;tt<(t2.length-1) && !reject;tt++) {
							if(t2[tt]!=t2[tt+1]) {
								if(minband>pnet.getband(t2[tt],t2[tt+1])) {
									minband=pnet.getband(t2[tt],t2[tt+1]);
								}
							}
						}
						if(minband<(cnet.getedgew()[l]/1000.0)) {
							reject=true;
							ff=10000000;
							break;
						}else {
							ff+=normaliz2*((vnfhops*minband)-(cnet.getedgew()[l]/1000.0));
						}
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
	
	/** Thread for the parallel computation of the GA within supergenerations loop */
	class supergenthread implements Runnable {
		   private Thread t;
		   private String threadName="Supergeneration";
		   int s;
		   
		   public void run() {
			   	//initialize GA for parallel computation
			   	GA2 ga2=new GA2(pnet);
				ga2.setpopgenheuristic(popgenheuristic);
				ga2.setduration(duration);
				ga2.loadVNFgraph(cnet);
				ga2.loadSetup(setup,defsetup);
				ga2.setavnodes(availnodes);
				ga2.init();
				
				//storebest solution
				best=ga2.getbest();
				pops[s]=new Mapping(ga2.getbest().getbestmapping());
				pops[s].setavcap(0,ga2.getbest().getavcap()[0]);
				activethreads[s]=false;
		   }
		   
		   public void start (int sg) {
			   this.s=sg;
		         t = new Thread (this, threadName);
		         t.start ();
		   }
	}
}