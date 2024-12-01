package ga;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/** Genetic Algorithm. **/
public class GA {
	int chromes=2;								//even numbers only
	int generations=2;							//even numbers only
	int supergen=2;								//even numbers or set to 1 for simple GA configuration
	
	nnetwork.NNetwork nnet;						//neural network object
	
	Chromosome pop[], pop2[], pops[],pops2[];	//population arrays
	int k=0;									//number of parameters
	String params[];							//parameter names
	int parameter_dom[][];						//parameter domains
	String objective;							//set to min or max
	Best best=new Best(k);						//preserves best solution on each stage
	Best lastbest=new Best(k);					//preserves latest best solution
	double min,max,dif=10;						//minimum, maximum fitness and range of fitness in population
	double crossprob=1;							//crossover probability
	double mutprob=1;							//mutation probability
	boolean first=true;							//indicates the generation of first population
	double divider=1.0;							//divider
	int edges=0;								//number of edges
	int outputs=0;								//number of output nodes
	Double[] input;								//inputs
	String storedmodel="model.csv";				//filename for stored model
	int vnfsize;								//SFC size
	
	String filename, del;						//filename and delimiter
	
	public GA(int chroms, int gnrs, int sgnrs, String params[], String command[],
			double crossprop, double mutprop, int parameter_dom[][], String objective,
			nnetwork.NNetwork nnet, String filename, String del, int edges, int outputs){
		this.generations=gnrs;
		this.supergen=sgnrs;
		this.chromes=chroms;
		this.params=params;
		this.parameter_dom=parameter_dom;
		this.crossprob=crossprop;
		this.mutprob=mutprop;
		this.k=params.length;
		this.pop=new Chromosome[chroms];
		this.pop2=new Chromosome[0];
		this.pops=new Chromosome[sgnrs];
		this.pops2=new Chromosome[sgnrs];
		if(objective.equals("min") || objective.equals("max")) {
			this.objective=objective;
		}
		this.nnet=nnet;
		this.filename=filename;
		this.del=del;
		this.edges=edges;
		this.outputs=outputs;
	}
	
	public GA(int chroms, int gnrs, int sgnrs, String params[], String command[],
			double crossprop, double mutprop, int parameter_dom[][], String objective,
			nnetwork.NNetwork nnet, Double[] input, int edges, int outputs, int vnfsize){
		this.generations=gnrs;
		this.supergen=sgnrs;
		this.chromes=chroms;
		this.params=params;
		this.parameter_dom=parameter_dom;
		this.crossprob=crossprop;
		this.mutprob=mutprop;
		this.k=params.length;
		this.pop=new Chromosome[chroms];
		this.pop2=new Chromosome[0];
		this.pops=new Chromosome[sgnrs];
		this.pops2=new Chromosome[sgnrs];
		if(objective.equals("min") || objective.equals("max")) {
			this.objective=objective;
		}
		this.nnet=nnet;
		this.input=input;
		this.edges=edges;
		this.outputs=outputs;
		this.vnfsize=vnfsize;
	}
	
	public GA() {
		
	}

	/** Initialize Genetic Algorithm. **/
	public void init() {
		
		for(int t=0;t<supergen;t++){	
			for(int s=0;s<supergen;s++){
				pop=new Chromosome[chromes];
				best=new Best(k);
				genpop();
				init2();
				superbest(s);
				
				if(lastbest.getbestfitness()==0.0) {
					best=lastbest;
					break;
				}
			}
			
			if(lastbest.getbestfitness()==0.0) {
				best=lastbest;
				break;
			}

			best=new Best(k);
			initializebest();
			pop=pops;
			init2();
			superbest2(t);
			
		}
		
		if(supergen>1 && best.getbestfitness()!=0.0) {
			best=new Best(k);
			initializebest();
			pop=pops2;
			init2();
		}

		double[] c2=new double[best.getbestphenotype().length];
		for(int i=0;i<best.getbestphenotype().length;i++) {
			c2[i]=best.getbestphenotype()[i]/divider;
		}

		nnet.setew(c2);
	}
	
	/** Initialize genetic procedures. **/
	public void init2(){
		
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
	
	/** Selection **/
	private void selection(){

		if(dif>0){
			ArrayList<Chromosome> newpop = new ArrayList<Chromosome>();
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
		
			pop=newpop.toArray(new Chromosome[chromes]);
			pop2=new Chromosome[0];
		}
	}
	
	/** Crossover **/
	private void crossover(){

		if(dif>0){
		ArrayList<Chromosome> crosspop = new ArrayList<Chromosome>();
		for(int c=0;c<pop.length;c=c+2){
			double prop = Math.random();
			if(prop<=crossprob){
				double rplace = Math.random()*(pop[0].getgenes().length);
				int place=(int) (Math.round(rplace));
				String s1[]=pop[c].getstringmap();
				String s2[]=pop[c+1].getstringmap();
				Chromosome temp1= new Chromosome(parameter_dom[0].length);
				Chromosome temp2= new Chromosome(parameter_dom[0].length);
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
		pop2=crosspop.toArray(new Chromosome[crosspop.size()]);
		genfit2();
		}
	}
	
	/** Mutation **/
	private void mutation(){

		ArrayList<Chromosome> newpop = new ArrayList<Chromosome>();
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
				newpop.add(new Chromosome(pop[c].changem(p2, p1)));
				m=true;
			}
		}
		if(m){
			for(int in=0;in<pop2.length;in++){
				newpop.add(new Chromosome(pop2[in].getstringmap()));
			}
			pop2=newpop.toArray(new Chromosome[newpop.size()]);
			genfit2();
		}
	}

	/** Generates population randomly. **/
	public void genpop(){
		for(int g=0; g<pop.length;g++){
			Chromosome crom = new Chromosome(parameter_dom, edges);
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
	
	/** Read model from file. **/
	public double[] readmodel(int vnfsize) {
		File md=new File(storedmodel);
		String[] params= {};
		double[] mod={};
	
		if(md.exists()) {
			try{
		    	Scanner scanner = new Scanner(md);
   
		    	while(scanner.hasNext()){
		    		String s=scanner.nextLine();
		    		String[] params0=s.split("|");
		    		if(params0[0].equals(Integer.toString(vnfsize))) {
		    			String s2=s.subSequence(2, (s.length())).toString();
		    			params= s2.split(",");
		    		}
		    	}
		    	scanner.close();
			}
			catch (IOException e) {
			       e.printStackTrace();
			   }
		}
		
		if(params.length>0) {
			mod=new double[params.length];
		
			for(int p=0;p<params.length;p++) {
				mod[p]=Double.parseDouble(params[p]);
			}
		}
		
		return mod;
	}
	
	/** Initialize the object that stores the best solution in each team and group. **/
	public void initializebest() {
		genchromefit(pop[0]);
		best.setbestfitness(pop[0].getfitness());
		best.setbestphenotype(pop[0].getgenes());
	}
	
	/** Initialize the object that stores the best overall solution. **/
	public void initializelastbest() {
		genchromefit(pop[0]);
		lastbest.setbestfitness(pop[0].getfitness());
		lastbest.setbestphenotype(pop[0].getgenes());
	}
	
	/** Prints population of the current generation. **/
	public void printpop() {
		for(int p=0;p<pop.length;p++) {
			System.out.println(java.util.Arrays.toString(pop[p].getgenes()));
		}
	}
	
	/** Stores the best solution of the current generation for the next stage of the algorithm. **/
	public void superbest(int in){
		pops[in]=new Chromosome(best.getbestphenotype());
	}
	
	/** Stores the best solution of the current generation for the next stage of the algorithm. **/
	public void superbest2(int in){
		pops2[in]=new Chromosome(best.getbestphenotype());
	}
	
	/** Computes and stores the fitness of each chromosome. **/
	public void genfit(){
		genfit1();
		genfit2();
	}
	
	/** Computes fitness for pop. **/
	public void genfit1(){
		for(int i1=0;i1<pop.length;i1++){
			genchromefit(pop[i1]);
		}

		range();
		updatebest();
	}
	
	/** Compute fitness for pop2. **/
	public void genfit2(){
		for(int i3=0;i3<pop2.length;i3++){
			genchromefit(pop2[i3]);
		}
		
		range();
		updatebest();
	}
	
	/** Computes and store fitness of a single chromosome. **/
	public void genchromefit(Chromosome h) {
		//store link weights as an double array
		int[] c=h.getgenes();
		double[] c2=new double[c.length];
		for(int i=0;i<c.length;i++) {
			c2[i]=c[i]/divider;
		}
	
		Double t=0.0;
		
		//set the link weights encoded in the chromosome in the network
		nnet.setew(c2);
		//run neural network
		nnet.runff(input);
		//update its fitness
		t=nnet.fitness(input);
		h.setfitness(t);		
	}
		

	/** Computes the range in the fitness of the population. **/
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
	
	/** Update best solution. **/
	public void updatebest() {
		if(objective.equals("min")){
			updatebestmin();
		}else {
			updatebestmax();
		}
	}
	
	/** Update best solution when the objective is the minimization of fitness function. **/
	public void updatebestmin(){
		
		for(int b=0;b<pop.length;b++){
			if(best.getbestfitness()>pop[b].getfitness()){
					best.setbestfitness(pop[b].getfitness());
					best.setbestphenotype(pop[b].getgenes());
					
					if(lastbest.getbestfitness()>best.getbestfitness()){
						lastbest.setbestfitness(best.getbestfitness());
						lastbest.setbestphenotype(best.getbestphenotype());
						printlastbest();
					}
			}
		}
		
		for(int b=0;b<pop2.length;b++){
			if(best.getbestfitness()>pop2[b].getfitness()){
					best.setbestfitness(pop2[b].getfitness());
					best.setbestphenotype(pop2[b].getgenes());
					
					if(lastbest.getbestfitness()>best.getbestfitness()){
						lastbest.setbestfitness(best.getbestfitness());
						lastbest.setbestphenotype(best.getbestphenotype());
						printlastbest();
					}
			}
		}
	}
	
	/** Update best solution when the objective is the maximization of fitness function. **/
	public void updatebestmax(){
		
		for(int b=0;b<pop.length;b++){
			if(best.getbestfitness()<pop[b].getfitness()){
					best.setbestfitness(pop[b].getfitness());
					best.setbestphenotype(pop[b].getgenes());
					
					if(lastbest.getbestfitness()<best.getbestfitness()){
						lastbest.setbestfitness(best.getbestfitness());
						lastbest.setbestphenotype(best.getbestphenotype());
						printlastbest();
					}
			}
		}
		
		for(int b=0;b<pop2.length;b++){
			if(best.getbestfitness()<pop2[b].getfitness()){
					best.setbestfitness(pop2[b].getfitness());
					best.setbestphenotype(pop2[b].getgenes());
					
					if(lastbest.getbestfitness()<best.getbestfitness()){
						lastbest.setbestfitness(best.getbestfitness());
						lastbest.setbestphenotype(best.getbestphenotype());
						printlastbest();
					}
			}
		}
	}
	
	/** Print current overall best solution. **/
	public void printlastbest() {
		double[] c2=new double[lastbest.getbestphenotype().length];
		for(int i=0;i<lastbest.getbestphenotype().length;i++) {
			c2[i]=lastbest.getbestphenotype()[i]/divider;
		}
	}

	/** Print current generation best solution. **/
	public void printbest() {
		System.out.println(">"+best.getbestfitness());
	}
	
	/** Store model. **/
	public void storemodel(String b) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;

		try {
			try {
				fw = new FileWriter(storedmodel,true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.println(b);
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
