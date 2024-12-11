package sfc_c;

import java.util.ArrayList;

	/** the mapping between the VNF and the substrate network */
public class Mapping_c {

	/** stores the mapping, 
	 * the value in mapping[i] is the ID of the substrate node that hosts vistrual node i */
	private int mapping[];
	/** VNF nodes (cnodes) */
	int cnodes;
	/** substrate network nodes (pnodes) */
	int pnodes;
	/** fitness for this mapping */
	private double fitness=0;
	/** available capacity */
	private int[] avcap=new int[2];
	/** available nodes, 
	 * substrate nodes that have enough available capacity to host the minimum demand VNF */
	ArrayList<Integer> availnodes = new ArrayList<Integer>();

	public Mapping_c(){
		this.fitness=10000000;
	}
	
	/** constructs empty mapping object **/
	public Mapping_c(int cnodes) {
		this.cnodes=cnodes;
		mapping=new int[cnodes];
	}
	
	/** constructs random mapping objects using the nodes of the substrate network
	* that have the available capacity to host the minimum demand VNF */
	public Mapping_c(int cnodes, ArrayList<Integer> availnodes) {
		this.cnodes=cnodes;
		this.availnodes=availnodes;
		mapping=new int[cnodes];
		randommapping2();
	}
	
	/** constructs random mapping objects, no constraints are considered */
	public Mapping_c(int cnodes, int vnodes) {
		this.cnodes=cnodes;
		this.pnodes=vnodes;
		mapping=new int[cnodes];
		randommapping();
	}
		
	/** initialize the object on given mapping */
	public Mapping_c(int[] c){
		mapping=c;
	}
	
	/** initialize object on given mapping */
	public Mapping_c(String[] c){
		//initialize object on given mapping
		mapping= new int[c.length];
		for(int s=0;s<c.length;s++){
			mapping[s]=Integer.parseInt(c[s]);
		}
	}
	
	/** initialize the object on given mapping and fitness */
	public Mapping_c(int f, int[] m){
		//initialize the object on given mapping and fitness
		this.fitness=f;
		this.mapping=m;
	}
	 
	/** mapping statistics */
	public void stats(){
		int temp[]=mapping.clone();
		int cnt=0;
		for(int i=0;i<mapping.length;i++) {
			boolean c=true;
			for(int j=(i+1);j<mapping.length;j++) {
				if(temp[j]>0 && mapping[i]==mapping[j]) {
					cnt++;
					temp[j]=-1;
					if(c) {
						cnt++;
						c=false;
					}
				}
			}
		}
		avcap[0]=mapping.length;	
		avcap[1]=cnt;
	}
	
	/** string to map */
	public void strtomap(String[] c){//string to map
		mapping= new int[c.length];
		for(int s=0;s<c.length;s++){
			mapping[s]=Integer.parseInt(c[s]);
		}
	}
	
	/** generate random mapping, no constraints are considered */
	public void randommapping(){
		for(int ii=0;ii<mapping.length;ii++){
			mapping[ii]=-1;
		}
		for(int v=0;v<mapping.length;v++){
			int rand;
			do{
			double randomm = Math.random()*(pnodes);
			rand=(int) (Math.round(randomm)-1);
			}while(rand<0);
			mapping[v]=rand;
		}
	}
	
	/** generate random mapping, considering node available capacity */
	public void randommapping2(){
		for(int ii=0;ii<mapping.length;ii++){
			mapping[ii]=-1;
		}
		for(int v=0;v<mapping.length;v++){
			int rand;
			do{
			double randomm = Math.random()*(availnodes.size());
			rand=(int) (Math.round(randomm)-1);
			}while(rand<0);
			mapping[v]=availnodes.get(rand);
		}
	}
	
	//getters setters
	/** set available capacity */
	public void setavcap(int ind, int c) {
		avcap[ind]=c;
	}
	
	/** get available capacity */
	public int[] getavcap() {
		return avcap;
	}
	
	/**set fitness */
	public void setfitness(double newfit){
		fitness=newfit;
	}
	
	/** get mapping */
	public int[] getmapping(){
		int[] t=mapping;
		return t;
	}
	
	/** mapping to string */
	public String[] getstringmap(){//mapping to string
		String[] t= new String[mapping.length];
		for(int k=0;k<mapping.length;k++){
		t[k]=Integer.toString(mapping[k]);
		}
		return t;
	}
	
	/** set mapping */
	public void setmapping(int[] ma){
		mapping=ma;
	}
	
	/** get fitness */
	public double getfitness(){
		return fitness;
	}
	
	/** change values of the mapping */
	public void change(int index, int value){//change one value of the mapping
		mapping[index]=value;
	}
	
	/** change values of the mapping and return mapping */
	public String[] changem(int index, int value){
		String[] tempm=getstringmap();
		tempm[index]=Integer.toString(value);
		return tempm;
	}
}
