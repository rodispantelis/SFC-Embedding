package ga;

/** A chromosome of the population. **/
public class Chromosome {
	/** The phenotypo of the chromosome. **/
	private int genes[];
	/** Fitness for this chromosome. **/
	private double fitness;	
	
	/** Generates random parameter values. **/
	public Chromosome(int parameter_dom[][], int edges) {
		this.genes=new int[edges];
		randomgeneration(parameter_dom);
	}
		
	/** Initialize the phenotype of this chromosome. **/
	public Chromosome(int d){
		genes=new int[d];
	}
	
	/** Initialize this chromosome on given mapping. **/
	public Chromosome(int[] c){
		genes=c;
	}
	
	/** Initialize this chromosome on given mapping. **/
	public Chromosome(double[] c){
		genes=new int[c.length];
		for(int g=0;g<c.length;g++) {
			genes[g]=(int) c[g];
		}
	}
	
	/** Initialize object on given mapping. **/
	public Chromosome(String[] c){
		genes=new int[c.length];
		for(int s=0;s<c.length;s++){
			genes[s]=Integer.parseInt(c[s]);
		}
	}
	
	/** Initialize the object on given phenotype and fitness **/
	public Chromosome(int f, int[] m){
		this.fitness=f;
		this.genes=m;
	}
	
	/** String to map. **/
	public void strtomap(String[] c){
		genes= new int[c.length];
		for(int s=0;s<c.length;s++){
			genes[s]=Integer.parseInt(c[s]);
		}
	}

	/** Generate random chromosome values. **/
	public void randomgeneration(int parameter_dom[][]){
		int range=parameter_dom[0][1]-parameter_dom[0][0]+1;
		for(int l=0;l<genes.length;l++) {
			int chromes=(int) (Math.random()*range);
			int value=chromes+parameter_dom[0][0];
			genes[l]=value;
		}
	}
		
	/** Change one value of the chromosome. **/
	public void change(int index, int value){
		genes[index]=value;
	}
	
	/** Change one value of the chromosome and return phenotype. **/
	public String[] changem(int index, int value){
		String[] tempm=getstringmap();
		tempm[index]=Integer.toString(value);
		return tempm;
	}
	
	/** Get phenotype. **/
	public int[] getgenes(){
		int[] t=genes;
		return t;
	}
	
	/** Get phenotype as string array **/
	public String[] getstringmap(){
		String[] t= new String[genes.length];
		for(int k=0;k<genes.length;k++){
			t[k]=Integer.toString(genes[k]);
		}
		return t;
	}
	
	/** Set fitness. **/
	public void setfitness(double newfit){
		fitness=newfit;
	}
	
	/** Set phenotype. **/
	public void setgenes(int[] ma){
		genes=ma;
	}
	
	/** Get fitness. **/
	public double getfitness(){
		return fitness;
	}
}
