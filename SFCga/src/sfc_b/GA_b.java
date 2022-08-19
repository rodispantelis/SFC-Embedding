package sfc_b;
import java.util.ArrayList;
import java.util.Arrays;

public class GA_b {
	int chromes=6;								//even numbers only
	int generations=4;							//even numbers only
	int supergen=2;								//even numbers or set to 1 for simple GA configuration
	
	Chromosome_b pop[], pop2[], pops[],pops2[];	//population arrays
	int k=0;									//number of parameters
	String params[];							//parameter names
	int parameter_dom[][];						//parameter domains
	String command[];							//command for OS
	String objective;							//set to min or max
	public Best_b best=new Best_b(k);			//preserves best solution on each stage
	Best_b lastbest=new Best_b(k);				//preserves latest best solution
	double min,max,dif=10;						//minimum, maximum fitness and range of fitness in population
	double crossprob=1;							//crossover probability
	double mutprob=1;							//mutation probability
	boolean first=true;							//indicates the generation of first population
	int mapping[];								//mapping of parameter values to the command to OS
	
	network.Network pnet;
	network.VNFgraph cnet;
	public sfc_c.GA_c ga_c;
	
	public GA_b(int chroms, int gnrs, int sgnrs, String params[], String command[],
			double crossprop, double mutprop, int parameter_dom[][], String objective,
			network.VNFgraph cnet, network.Network pnet){
		this.generations=gnrs;
		this.supergen=sgnrs;
		this.chromes=chroms;
		this.params=params;
		this.parameter_dom=parameter_dom;
		this.crossprob=crossprop;
		this.mutprob=mutprop;
		this.command=command;
		this.k=params.length;
		this.mapping=new int[k];
		this.pop=new Chromosome_b[chroms];
		this.pop2=new Chromosome_b[0];
		this.pops=new Chromosome_b[sgnrs];
		this.pops2=new Chromosome_b[sgnrs];
		if(objective.equals("min") || objective.equals("max")) {
			this.objective=objective;
		}
		this.cnet=cnet;
		this.pnet=pnet;
	}
	
	public GA_b() {
		
	}
	
	public void init() {
		//sets and groups structure of procedures
		
		for(int t=0;t<supergen;t++){	
			for(int s=0;s<supergen;s++){
				pop=new Chromosome_b[chromes];
				//System.out.println("----stage#1");
				best=new Best_b(k);
				genpop();
				init2();
				superbest(s);
			}
			
			//For sets, groups, etc
			//System.out.println("----stage#2");
			best=new Best_b(k);
			initializebest();
			pop=pops;
			init2();
			superbest2(t);
		}
		
		if(supergen>1) {
			//System.out.println("----stage#3");
			best=new Best_b(k);
			initializebest();
			pop=pops2;
			init2();
		}
		
		System.out.println("> SetUp:"+Arrays.toString(best.getbestphenotype()));
	}
	
	public void init2(){
		//initialize genetic procedures
		genfit();
		for(int gs=0;gs<generations;gs++){	
			if(dif<1){
				//Do nothing the population has converged
			}else{
				crossover();
				mutation();
				selection();
			}
		}
	}
	
	private void selection(){
		//selection
		if(dif>0){
			ArrayList<Chromosome_b> newpop = new ArrayList<Chromosome_b>();
			newpop.clear();

		while(newpop.size()<chromes){

			double p = Math.random();
			double c=(double)min+(p*(double)dif);
			for(int sel=0;sel<pop.length;sel++){
				if(newpop.size()>(chromes-1)){
				break;
				}else {
					if(objective.equals("min") && pop[sel].getfitness()<c){
						newpop.add(pop[sel]);
					}else if(objective.equals("max") && pop[sel].getfitness()>c){
						newpop.add(pop[sel]);
					}
				}
			}
			
			for(int sel=0;sel<pop2.length;sel++){
				if(newpop.size()>(chromes-1)){
				break;
				}else {
					if(objective.equals("min") && pop2[sel].getfitness()<c){
						newpop.add(pop2[sel]);
					}else if(objective.equals("max") && pop2[sel].getfitness()>c){
						newpop.add(pop2[sel]);
					}
				}
			}
		}
		
		pop=newpop.toArray(new Chromosome_b[chromes]);
		pop2=new Chromosome_b[0];
		}
	}
	
	private void crossover(){
		//crossover
		if(dif>0 && pop.length>1){
		ArrayList<Chromosome_b> crosspop = new ArrayList<Chromosome_b>();
		for(int c=0;c<pop.length;c=c+2){
			double prop = Math.random();
			if(prop<=crossprob){
				double rplace = Math.random()*(pop[0].getgenes().length);
				int place=(int) (Math.round(rplace));
				String s1[]=pop[c].getstringmap();
				String s2[]=pop[c+1].getstringmap();
				Chromosome_b temp1= new Chromosome_b(parameter_dom[0].length);
				Chromosome_b temp2= new Chromosome_b(parameter_dom[0].length);
				temp1.strtomap(s1);
				temp2.strtomap(s2);

				for(int t1=0;t1<place;t1++){
					int temp=temp1.getgenes()[t1];
					temp1.change(t1,temp2.getgenes()[t1]);
					temp2.change(t1,temp);
				}
				
				crosspop.add(temp1);
				crosspop.add(temp2);	
			}
		}	
		pop2=crosspop.toArray(new Chromosome_b[crosspop.size()]);
		genfit2();
		}
	}
	
	private void mutation(){
		//mutation
		
		ArrayList<Chromosome_b> newpop = new ArrayList<Chromosome_b>();
		newpop.clear();
		
		boolean m=false;
		for(int c=0;c<pop.length;c++){
			double prop1 = Math.random();
			if(prop1<=mutprob){
				double prop2 = Math.random();
				int p2=(int)(k*prop2);
				
				double prop3 = Math.random();
				int r=parameter_dom[p2][1]-parameter_dom[p2][0];
				int p1=(int)(r*prop3)+parameter_dom[p2][0];
				newpop.add(new Chromosome_b(pop[c].changem(p2, p1)));
				m=true;
			}
		}
		if(m){
			for(int in=0;in<pop2.length;in++){
				newpop.add(new Chromosome_b(pop2[in].getstringmap()));
			}
			pop2=newpop.toArray(new Chromosome_b[newpop.size()]);
			genfit2();
		}
	}

	
	public void genpop(){
		//generates population randomly	
		for(int g=0; g<pop.length;g++){
			Chromosome_b crom = new Chromosome_b(parameter_dom);
			if(g<pop.length){
				pop[g]=crom;
			}
		}
		
		initializebest();
		
		if(first) {
			initializelastbest();
			first=false;
		}
	}
	
	public void initializebest() {
		//initialize the object that stores the best solution in each team and group
		genchromefit(pop[0]);
		best.setbestfitness(pop[0].getfitness());
		best.setbestphenotype(pop[0].getgenes());
	}
	
	public void initializelastbest() {
		//initialize the object that stores the best overall solution
		genchromefit(pop[0]);
		lastbest.setbestfitness(pop[0].getfitness());
		lastbest.setbestphenotype(pop[0].getgenes());
	}
	
	public void printpop() {
		//prints population of the current generation
		for(int p=0;p<pop.length;p++) {
		System.out.println(java.util.Arrays.toString(pop[p].getgenes()));
		}
	}
	
	public void superbest(int in){
		//stores the best solution of the current generation for the next stage of the algorithm
		pops[in]=new Chromosome_b(best.getbestphenotype());
	}
	
	public void superbest2(int in){
		//stores the best solution of the current generation for the next stage of the algorithm
		pops2[in]=new Chromosome_b(best.getbestphenotype());
	}
	
	public void genfit(){
		//computes and stores the fitness of each cromosome
		genfit1();
		genfit2();
	}
	public void genfit1(){
		//computes fitness for pop
		for(int i1=0;i1<pop.length;i1++){
			genchromefit(pop[i1]);
		}
		range();
		updatebest();
	}
	
	public void genfit2(){
		//compute fitness for pop2
		for(int i3=0;i3<pop2.length;i3++){
			genchromefit(pop2[i3]);
		}
		
		range();
		updatebest();
	}
	
	public void genchromefit(Chromosome_b h) {
		//compute and store fitness of a single chromosome
		int[] c=h.getgenes();//parameters for ga_c
		double t=1;
		
		if(c[0] % 2 == 1) {
			c[0]++;
		}
		if(c[1] % 2 == 1) {
			c[1]++;
		}
		if(c[2]>1) {
			if(c[2] % 2 == 1) {
				c[2]++;
			}
		}
		ga_c=new sfc_c.GA_c(c[0], c[1], c[2], c[3], c[4],cnet, pnet);
		ga_c.init();
		t=ga_c.best.getbestfitness();
		h.setfitness(t);		
	}
		
	
	private void range(){
		//computes the range in the fitness of the population
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
	
	public void updatebest() {
		//update best solution
		if(objective.equals("min")){
			updatebestmin();
		}else {
			updatebestmax();
		}
	}
	
	public void updatebestmin(){
		//update best solution when the objective is the minimization
		
		for(int b=0;b<pop.length;b++){
			if(best.getbestfitness()>pop[b].getfitness()){
					best.setbestfitness(pop[b].getfitness());
					best.setbestphenotype(pop[b].getgenes());
					
					if(lastbest.getbestfitness()>best.getbestfitness())
					{
						lastbest.setbestfitness(best.getbestfitness());
						lastbest.setbestphenotype(best.getbestphenotype());
					}
			}
		}
		
		for(int b=0;b<pop2.length;b++){
			if(best.getbestfitness()>pop2[b].getfitness()){
					best.setbestfitness(pop2[b].getfitness());
					best.setbestphenotype(pop2[b].getgenes());
					
					if(lastbest.getbestfitness()>best.getbestfitness())
					{
						lastbest.setbestfitness(best.getbestfitness());
						lastbest.setbestphenotype(best.getbestphenotype());
					}
			}
		}
	}
	
	public void updatebestmax(){
		//update best solution when the objective is the maximization
		for(int b=0;b<pop.length;b++){
			if(best.getbestfitness()<pop[b].getfitness()){
					best.setbestfitness(pop[b].getfitness());
					best.setbestphenotype(pop[b].getgenes());
					
					if(lastbest.getbestfitness()<best.getbestfitness())
					{
						lastbest.setbestfitness(best.getbestfitness());
						lastbest.setbestphenotype(best.getbestphenotype());
					}
			}
		}
		
		for(int b=0;b<pop2.length;b++){
			if(best.getbestfitness()<pop2[b].getfitness()){
					best.setbestfitness(pop2[b].getfitness());
					best.setbestphenotype(pop2[b].getgenes());
					
					if(lastbest.getbestfitness()<best.getbestfitness())
					{
						lastbest.setbestfitness(best.getbestfitness());
						lastbest.setbestphenotype(best.getbestphenotype());
					}
			}
		}
	}
	
	public void printlastbest() {
		//print current overall best solution
		System.out.println("|"+lastbest.getbestfitness()+"| "+Arrays.toString(lastbest.getbestphenotype()));
	}
	
	public void printbest() {
		//print current generation best solution
		System.out.println(">"+best.getbestfitness()+"| "+Arrays.toString(best.getbestphenotype()));
	}
}
