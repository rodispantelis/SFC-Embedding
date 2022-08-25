package sfc_c;
import java.util.ArrayList;
import sfc_a.Mapping;

public class GA_c {
	//Genetic Algorithm
	Codec_c cod=new Codec_c();
	Mapping_c pop[], pop2[]=new Mapping_c[0], pops[],pops2[];
	public Mapping_c m,mgr;	//For greedy
	public int cnodes, pnodes;
	int maxnodeb=1000;		//maximum node bandwidth, for FatTree=1Gbps
	network.Network pnet;
	network.VNFgraph cnet;
	double crossprop=1;
	double mutprop=1;
	int genes=0;			//even numbers only
	int supergen=0;			//even numbers only
	int generations=0;		//even numbers only
	double min,max,dif=10;
	public Best_c best=new Best_c(cnodes);
	Best_c best2=new Best_c(cnodes);
	Last_c last=new Last_c();
	String path=" ";
	String celnetfile=path+" ";
	String phynetfile=path+" ";
	String celnetfileEV=path+" ";
	String phynetfileEV=path+" ";
	
	//use population generation heuristic
	boolean popgenheuristic=true;

	boolean rejection=false;
	int[] exresult= {1};
	boolean loadex=false;
	double totalTime, grTime;
	ArrayList<int[]> nodecharge = new ArrayList<int[]>();
	ArrayList<int[]> edgecharge = new ArrayList<int[]>();
	ArrayList<Integer> availnodes = new ArrayList<Integer>();
	
	double normaliz=1.0;
	double normaliz2=1.0;///////////////////////////////////////

	
	public GA_c(int gens, int gnrs, int sgnrs, int cr, int mu, 
			network.VNFgraph incnet, network.Network inpnet) {
		this.generations=gnrs;
		this.supergen=sgnrs;
		this.genes=gens;
		this.pop=new Mapping_c[genes];
		this.crossprop=cr/100.0;
		this.mutprop=mu/100.0;
		this.cnet=incnet;
		this.pnet=inpnet;
		cnodes=cnet.nodes;
		pnodes=pnet.getservers();
		pop=new Mapping_c[genes];
	}
	
	public GA_c() {
		
	}
	
	public void setparams(int gens, int gnrs, int sgnrs, int cr, int mu){
		genes=gens;
		pop=new Mapping_c[genes];
		generations=gnrs;
		supergen=sgnrs;
		crossprop=cr/100.0;
		mutprop=mu/100.0;
	}
	
	public void printcnetparams(){
	System.out.println("C:"+cnodes);
	}
	
	public void printparams(){
	System.out.println("P:"+pnodes+" C:"+cnodes+" Chr:"+genes+" Gens:"+generations+" Sgens:"+supergen);
	}
	
	public void init(){
	      
		long startTime = System.nanoTime();
		
		best=new Best_c(cnodes);
		best2=new Best_c(cnodes);
		
		pops2=new Mapping_c[supergen];
		for(int t=0;t<supergen;t++){

			pops=new Mapping_c[supergen];
			//System.out.println("level#1");
			for(int s=0;s<supergen;s++){
				best=new Best_c(cnodes);
				genpop();
				init2();
				superbest(s);
			}
			//System.out.println("level#2");
			best=new Best_c(cnodes);
			pop=pops;
			init2();
			superbest2(t);
		}
		
		if(supergen>1) {
			//System.out.println("level#3");
			//the final
			best=new Best_c(cnodes);
			pop=pops2;
			init2();
		}
		
		long endTime   = System.nanoTime();
		totalTime = (endTime - startTime)/1000000;
	}
	
	public void init2(){
		//System.out.println("init2");
		//initiate algorithm
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
	
	public void superbest(int in){
		pops[in]=new Mapping_c(best.getbestmapping());
		pops[in].setavcap(0,best.getavcap()[0]);
	}
	
	public void superbest2(int in){
		pops2[in]=new Mapping_c(best.getbestmapping());
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
		if(dif>0){
			ArrayList<Mapping_c> newpop = new ArrayList<Mapping_c>();//temp new pop storage
			newpop.clear();

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
		pop=newpop.toArray(new Mapping_c[genes]);
		pop2=new Mapping_c[0];
		}
	}
	
	private void crossover(){
		//crossover
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
	
	private void multymutation(int d){
		for(int c=0;c<d;c++){
			mutation();
		}
	}
	
	private void mutation(){
		//mutation
		
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
	
	public void genfit(){
		genfit1(pop);
		genfit1(pop2);
	}
	public void genfit1(Mapping_c somepop[]){
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
		
			for(int f=0;f<nodes.size() && !reject;f++) {//System.out.println("f"+f);
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
							double tff=
									normaliz2*((vnfhops*minband)-(cnet.getedgew()[l]/1000.0));
						
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
	
	public void printsuper(){
		//print super best
		for(int pr=0;pr < pops.length;pr++){
			System.out.println(java.util.Arrays.toString(pops[pr].getmapping())+"|"+pops[pr].getfitness());
		}		
	}

}