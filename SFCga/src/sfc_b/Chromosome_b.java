package sfc_b;
public class Chromosome_b {
	//a chromosome of the population
	private int genes[];	//mapping is the phenotypo of the chromosome
	private double fitness;	//fitness for this chromosome
	
	public Chromosome_b(int parameter_dom[][]) {
		this.genes=new int[parameter_dom.length];
		//generates random parameter values
		randomgeneration(parameter_dom);
	}
		
	public Chromosome_b(int d){
		//initialize the phenotype of this chromosome
		genes=new int[d];
	}
	
	public Chromosome_b(int[] c){
		//initialize this chromosome on given mapping
		genes=c;
	}
	
	public Chromosome_b(String[] c){
		//initialize object on given mapping
		genes=new int[c.length];
		for(int s=0;s<c.length;s++){
			genes[s]=Integer.parseInt(c[s]);
		}
	}
	
	public Chromosome_b(int f, int[] m){
		//initialize the object on given mapping and fitness
		this.fitness=f;
		this.genes=m;
	}
	
	public void strtomap(String[] c){//string to map
		genes= new int[c.length];
		for(int s=0;s<c.length;s++){
			genes[s]=Integer.parseInt(c[s]);
		}
	}

	public void randomgeneration(int parameter_dom[][]){//generate random chromosome values
		
		for(int l=0;l<parameter_dom.length;l++) {
			int range=parameter_dom[l][1]-parameter_dom[l][0]+1;
			int chromes=(int) (Math.random()*range);
			int value=chromes+parameter_dom[l][0];
			genes[l]=value;
		}
	}
		
	public void setfitness(double newfit){
		fitness=newfit;
	}
	public int[] getgenes(){
		int[] t=genes;
		return t;
	}
	public String[] getstringmap(){//mapping to string
		String[] t= new String[genes.length];
		for(int k=0;k<genes.length;k++){
		t[k]=Integer.toString(genes[k]);
		}
		return t;
	}
	
	public void change(int index, int value){//change one value of the mapping
		genes[index]=value;
	}
	
	public String[] changem(int index, int value){
		String[] tempm=getstringmap();
		tempm[index]=Integer.toString(value);

		return tempm;
	}
	
	public void setgenes(int[] ma){
		genes=ma;
	}
	
	public double getfitness(){
		return fitness;
	}
}
