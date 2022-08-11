package network;

public class Link {

	int id;
	int[] linkednodes=new int[2];		//IDs of the nodes that the link connects
	int linktype;						//0 for bidirectional, 1 input only, 2 output only
										//-1 for invalid or not operational
	boolean intrarack=false;			//is it an intra-rack link?
	Double capacity=0.0;				//Gbps
	Double load=0.0;					//Gbps
	
	public Link() {
		
	}
	
	public Link(int id, int n1, int n2, int type) {
		this.id=id;
		this.linkednodes[0]=n1;
		this.linkednodes[1]=n2;
		this.linktype=type;
		this.capacity=10.0;				//default value for link bandwidth
	}
	
	public Link(int id, int n1, int n2, int type, boolean inter) {
		this.id=id;
		this.linkednodes[0]=n1;
		this.linkednodes[1]=n2;
		this.linktype=type;
		this.capacity=1.0;
		this.intrarack=inter;
	}
	
	//getters setters
	public Double getavailableband() {
		Double r=capacity-load;
		return r;
	}
	
	public void settype(int t) {
		linktype=t;
	}
	
	public void setintrarack() {
		intrarack=true;
	}
	
	public boolean isintrarack() {
		return intrarack;
	}
	
	public int gettype() {
		return linktype;
	}
	
	public void setcapacity(Double c) {
		capacity=c;
	}
	
	public Double getcapacity() {
		return capacity;
	}
	
	public Double getload() {
		return load;
	}
	
	public void addload(Double a){
		load+=a;
	}
	
	public void remload(Double a){
		load-=a;
	}
	
	public int getid() {
		return id;
	}
	
	public int[] getconnected() {
		return linkednodes;
	}
}
