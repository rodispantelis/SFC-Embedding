package sfc_hg_b;

	/** chromosome of the population */

public class Chromosome_b {
	/** the phenotype of the chromosome */
	private int genes[];
	/** fitness for this chromosome */
	private double fitness;
	
	/** generates random parameter values */
	public Chromosome_b(int parameter_dom[][]) {
		this.genes=new int[parameter_dom.length];
		randomgeneration(parameter_dom);
	}
		
	/** initialize the phenotype of this chromosome */
	public Chromosome_b(int d){
		genes=new int[d];
	}
	
	/** initialize this chromosome on given mapping */
	public Chromosome_b(int[] c){
		genes=c;
	}
	
	/** initialize object on given mapping */
	public Chromosome_b(String[] c){
		genes=new int[c.length];
		for(int s=0;s<c.length;s++){
			genes[s]=Integer.parseInt(c[s]);
		}
	}
	
	/** initialize the object on given mapping and fitness */
	public Chromosome_b(int f, int[] m){
		this.fitness=f;
		this.genes=m;
	}
	
	/** string to map */
	public void strtomap(String[] c){
		genes= new int[c.length];
		for(int s=0;s<c.length;s++){
			genes[s]=Integer.parseInt(c[s]);
		}
	}

	/** generate random chromosome values */
	public void randomgeneration(int parameter_dom[][]){
		for(int l=0;l<parameter_dom.length;l++) {
			int range=parameter_dom[l][1]-parameter_dom[l][0]+1;
			int chromes=(int) (Math.random()*range);
			int value=chromes+parameter_dom[l][0];
			genes[l]=value;
		}
	}
		
	/** set fitness */
	public void setfitness(double newfit){
		fitness=newfit;
	}
	
	/** get phenotype */
	public int[] getgenes(){
		int[] t=genes;
		return t;
	}
	
	/** phenotype to string */
	public String[] getstringmap(){
		String[] t= new String[genes.length];
		for(int k=0;k<genes.length;k++){
			t[k]=Integer.toString(genes[k]);
		}
		return t;
	}
	
	/** change one gene */
	public void change(int index, int value){
		genes[index]=value;
	}
	
	/** change one gene and return phenotype */
	public String[] changem(int index, int value){
		String[] tempm=getstringmap();
		tempm[index]=Integer.toString(value);
		return tempm;
	}
	
	/** set phenotype */
	public void setgenes(int[] ma){
		genes=ma;
	}
	
	/** get fitness */
	public double getfitness(){
		return fitness;
	}
}
