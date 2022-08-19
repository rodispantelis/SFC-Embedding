package sfc_a;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import greedy.Greedy;

public class GA {
	//Genetic Algorithm
	Codec cod=new Codec();
	Mapping pop[], pop2[]=new Mapping[0], pops[],pops2[];	//arrays for storing populations
	Mapping m,mgr;							//mappings: m for GA, mgr for greedy heuristic					
	int maxnodeb=1000;						//maximum node bandwidth, for FatTree=1Gbps
	network.VNFgraph cnet;					//VNF graph
	network.Network pnet;					//substrate network
	int cnodes, pnodes;						//substrate nodes, VNF nodes
	
	//GA parameters
	double crossprop=1;
	double mutprop=1;
	int genes=0;				//even numbers only
	int supergen=0;				//even numbers only
	int generations=0;			//even numbers only

	//normalization factors for fitness function; set to 1.0 if not applicable
	double normaliz=1.0;
	double normaliz2=1.0;
	
	//add greedy heuristic in population
	boolean usegreed=false;
	boolean addmgr=false;

	String path2="C:\\Files\\src\\";
	
	//use population generation heuristic
	boolean popgenheuristic=true;

	int duration=0;
	double min,max;
	double dif=10;
	Best best=new Best(cnodes);
	Best best2=new Best(cnodes);
	Last last=new Last();
	boolean rejection=false;
	double totalTime, grTime;
	ArrayList<int[]> nodecharge = new ArrayList<int[]>();
	ArrayList<int[]> edgecharge = new ArrayList<int[]>();
	ArrayList<Integer> availnodes = new ArrayList<Integer>();
	String defsetup="0";

	public GA(int gens, int gnrs, int sgnrs, int cr, int mu) {
		this.generations=gnrs;
		this.supergen=sgnrs;
		this.genes=gens;
		this.crossprop=cr/100.0;
		this.mutprop=mu/100.0;
	}
	
	public GA(network.Network pnet) {
		this.pnet=pnet;
		pnodes=pnet.getservers();
	}
	
	public void loadVNFgraph(network.VNFgraph vnf) {
		cnet=vnf;
		cnodes=vnf.getnodes();
	}
	
	public void loadSetup(Setup s, String dsetup) {
		genes=s.genes;
		this.pop=new Mapping[genes];
		supergen=s.supergens;
		generations=s.generations;
		crossprop=((double)s.crossprob)/100.0;
		mutprop=((double)s.mutprob)/100.0;
		defsetup=dsetup;
	}
	
	public void printcnetparams(){
	System.out.println("C:"+cnodes);
	}
	
	public void printparams(){
	System.out.println("P:"+pnodes+" Chr:"+genes+" Gens:"+generations+" Sgens:"+supergen
			+" crossprob:"+crossprop+" mutprob:"+mutprop);
	}

	public void init(){
		//initialize Genetic Algorithm
		rejection=false;

		long startTime = System.nanoTime();
		
		if(usegreed) {
			grsim();
		}
		
		best=new Best(cnodes);
		best2=new Best(cnodes);
		
		pops2=new Mapping[supergen];
		for(int t=0;t<supergen;t++){
			
		pops=new Mapping[supergen];
		//System.out.println("level#1");
		for(int s=0;s<supergen;s++){
			best=new Best(cnodes);
			genpop();
			init2();
			superbest(s);
		}
		
		//System.out.println("level#2");;
		best=new Best(cnodes);
		pop=pops;
		genfit();
		init2();
		superbest2(t);
		}
		
		//System.out.println("level#3");//the final
		if(supergen>1) {
			best=new Best(cnodes);
			pop=pops2;
			genfit();
			init2();
		}

		long endTime   = System.nanoTime();
		totalTime = (endTime - startTime)/1000000;
		
		if(best.getbestfitness()>-1 && best.getbestfitness()<10000000) {
			if(usegreed) {
				double fitim=(mgr.getfitness()-best.getbestfitness())/mgr.getfitness();
				System.out.println("GA: "+java.util.Arrays.toString(best.getbestmapping())
											+" | "+fitim);
				fitnessimprovement(fitim);
			}else {
				System.out.println("GA: "+java.util.Arrays.toString(best.getbestmapping())
											+" | "+best.getbestfitness());
			}
					
			rejection=false;
		}else {
			rejection=true;
			System.out.println("\n> Rejection");
		}

		updatetraffic();
	}
	
	public void init2(){
		for(int gs=0;gs<generations;gs++){
				crossover();
				multymutation(1);
				selection();
		}
		range();
	}

	public void setduration(int d) {
		duration=d;
	}
	
	public boolean isrejected() {
		return rejection;
	}
	
	public void grsim() {
		//compute Greedy mapping
		Greedy greed=new Greedy(pnet);
		greed.getrequest(cnet);
		greed.compute();			
		
		if(!greed.isrejected() && usegreed) {
			int[] basemapping=greed.getmapping();
			mgr=new Mapping(basemapping);
			addmgr=true;
		}else {
			addmgr=false;
		}
	}
	
	public void updatetraffic() {
		if(best.getbestfitness()<10000000) {
			int du=(int) (Math.random()*duration);
			pnet.setduration(du);
			pnet.embed(cnet, best.getbestmapping());
		}
	}
	
	public void updatebest(){
		//update currently best mapping
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
	
	public void superbest(int in){
		pops[in]=new Mapping(best.getbestmapping());
		pops[in].setavcap(0,best.getavcap()[0]);
	}
	
	public void superbest2(int in){
		pops2[in]=new Mapping(best.getbestmapping());
		pops2[in].setavcap(0,best.getavcap()[0]);
	}
		
	private void range(){
		//compute the range in the fitness of the population
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
	
	private void selection(){
		//selection
		if(dif>0.0){
			ArrayList<Mapping> newpop = new ArrayList<Mapping>();//temp new pop storage
			newpop.clear();
			
		//adds in new pop the gene with the best fitness
		while(newpop.size()<genes){
				double p = Math.random();
				double c=(double)min+(p*(double)dif);
				for(int sel=0;sel<pop.length;sel++){
					if(newpop.size()>(genes-1)){
						break;
					}else if(pop[sel].getfitness()<c){
						newpop.add(pop[sel]);
					}
				}
			
				for(int sel=0;sel<pop2.length;sel++){
					if(newpop.size()>(genes-1)){
						break;
					}else if(pop2[sel].getfitness()<c){
						newpop.add(pop2[sel]);
					}
			}
		}

		pop=newpop.toArray(new Mapping[genes]);

		pop2=new Mapping[0];
		}
	}
	
	private void crossover(){
		//crossover
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
	
	private void multymutation(int d){
		for(int c=0;c<d;c++){
			mutation();
		}
	}
	
	private void mutation(){
		//mutation
		
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
	
	public void genpop(){
		//determine available nodes and store them in an ArrayList
		//nodes the available capacity of which is larger than the minimum demand of the VNFs
		
		availnodes.clear();
		
		for(int s=0;s<pnet.getservers();s++) {
			if(pnet.getserver(s).getavailablecpu()>=cnet.getminnodew()) {
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
	
	public void genpop2(){
		//generates population
		
		//the first genes are generated heuristically and cover search space uniformly
		
		int d=0;
		
		if(popgenheuristic) {
			d=pnet.getservers();
		
			if(d>genes){
				d=genes*8/10;
			}
		}
		
		for(int t0=0;t0<d;t0++){
			if(pnet.getserver(t0).getavailablecpu()>=cnet.getminnodew()) {
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

		if(addmgr && usegreed) {
				pop[pop.length-1]=mgr;
			addmgr=false;
		}
		
		genfit1(pop);
	}
	
	public void genpop_original(){
		//generates population		
		int d=0;

		for(int g=d; g<genes;g++){
			Mapping map = new Mapping(cnodes, pnodes);
			if(g<pop.length){
				pop[g]=map;
			}
		}

		if(addmgr && usegreed) {
				pop[pop.length-1]=mgr;
			addmgr=false;
		}
		genfit();
	}
	
	public void genfit(){
		genfit1(pop);
		genfit1(pop2);
	}
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
	
	public void genefitness_experimental1(Mapping m) { //nice version t2.length-2
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
							if(pnet.getserver(mapping[t1[0]]).getrackid()==
									pnet.getserver(mapping[t1[1]]).getrackid()) {
								vnfhops=1.0;
							}else {
								vnfhops=1.0*(t2.length-2);
							}
	
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
	
	public double gettotaltime() {
		return totalTime;
	}
	
	public void printsuper(){
		//print super best
		for(int pr=0;pr < pops.length;pr++){
			System.out.println(java.util.Arrays.toString(pops[pr].getmapping())+"|"+pops[pr].getfitness());
		}		
	}

	public void fitnessimprovement(double dev1) {
		//store to file fitness improvement of the greedy mapping to the final output
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
		try {
			try {
				fw = new FileWriter(path2+"results\\fitnessimprovement.csv",true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.println(dev1);
			pw.flush();
		}finally {
	        try {
	             pw.close();
	             bw.close();
	             fw.close();
	        } catch (IOException io) { 
	        	}
	    }
	}
}