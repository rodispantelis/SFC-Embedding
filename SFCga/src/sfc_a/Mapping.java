package sfc_a;

import java.util.ArrayList;

public class Mapping {
	//the mapping between the VNF and the substrate network
	private int mapping[];
	int cnodes, pnodes;			//VNF nodes (cnodes), substrate network nodes (pnodes)
	private double fitness=0;	//fitness for this mapping
	private int[] avcap=new int[2];
	ArrayList<Integer> availnodes = new ArrayList<Integer>();

	public Mapping(){
		this.fitness=10000000;
	}
	
	public Mapping(int cnodes) {
		//generates random mapping objects
		this.cnodes=cnodes;
		mapping=new int[cnodes];
	}
	
	public Mapping(int cnodes, ArrayList<Integer> availnodes) {
		//generates random mapping objects of the nodes of the substrate network
		//that have the available capacity to host the VNFs
		this.cnodes=cnodes;
		this.availnodes=availnodes;
		mapping=new int[cnodes];
		randommapping2();
	}
		
	public Mapping(int cnodes, int vnodes) {
		//generates random mapping objects
		this.cnodes=cnodes;
		this.pnodes=vnodes;
		mapping=new int[cnodes];
		randommapping();
	}
	
	public Mapping(int[] c){
		//initialize the object on given mapping
		mapping=c;
	}
	
	public Mapping(String[] c){
		//initialize object on given mapping
		mapping= new int[c.length];
		for(int s=0;s<c.length;s++){
			mapping[s]=Integer.parseInt(c[s]);
		}
	}
	
	public Mapping(double f, int[] m){
		//initialize the object on given mapping and fitness
		this.fitness=f;
		this.mapping=m;
	}
	 
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
	
	public void strtomap(String[] c){//string to map
		mapping= new int[c.length];
		for(int s=0;s<c.length;s++){
			mapping[s]=Integer.parseInt(c[s]);
		}
	}
	
	//generate random mappings
	
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
	
	public void setavcap(int ind, int c) {
		avcap[ind]=c;
	}
	
	public int[] getavcap() {
		return avcap;
	}
	
	public void setfitness(double newfit){
		fitness=newfit;
	}
	
	public int[] getmapping(){
		int[] t=mapping;
		return t;
	}
	public String[] getstringmap(){//mapping to string
		String[] t= new String[mapping.length];
		for(int k=0;k<mapping.length;k++){
		t[k]=Integer.toString(mapping[k]);
		}
		return t;
	}
	
	public void setmapping(int[] ma){
		mapping=ma;
	}
	
	public double getfitness(){
		return fitness;
	}
	
	public int getmnode(int index){
		return mapping[index];
	}
	
	//change values of the mapping
	public void change(int index, int value){
		mapping[index]=value;
	}
	
	public String[] changem(int index, int value){
		String[] tempm=getstringmap();
		tempm[index]=Integer.toString(value);
		
		return tempm;
	}
}
