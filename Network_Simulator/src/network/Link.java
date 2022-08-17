package network;

/** Network Links */

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
	/** construct Link given id, the nodes it connects and its type */
	
	public Link(int id, int n1, int n2, int type) {
		this.id=id;
		this.linkednodes[0]=n1;
		this.linkednodes[1]=n2;
		this.linktype=type;
		this.capacity=10.0;				//default value for link bandwidth
	}
	
	/** construct Link given id, the nodes it connects, its type 
	 * and determine whether it is an intra-rack link */
	public Link(int id, int n1, int n2, int type, boolean intr) {
		this.id=id;
		this.linkednodes[0]=n1;
		this.linkednodes[1]=n2;
		this.linktype=type;
		this.capacity=1.0;
		this.intrarack=intr;
	}
	
	//getters setters
	
	/** get available bandwidth */
	public Double getavailableband() {
		Double r=capacity-load;
		return r;
	}
	
	/** set link type */
	public void settype(int t) {
		linktype=t;
	}
	
	/** define this link as intra-rack link */
	public void setintrarack() {
		intrarack=true;
	}
	
	/** is it an intra-rack link? */
	public boolean isintrarack() {
		return intrarack;
	}
	
	/** get link type */
	public int gettype() {
		return linktype;
	}
	
	/** set capacity */
	public void setcapacity(Double c) {
		capacity=c;
	}
	
	/** get capacity */
	public Double getcapacity() {
		return capacity;
	}
	
	/** get current traffic load */
	public Double getload() {
		return load;
	}
	
	/** set current traffic load */
	public void addload(Double a){
		load+=a;
	}
	
	/** remove traffic */
	public void remload(Double a){
		load-=a;
	}
	
	/** get link id */
	public int getid() {
		return id;
	}
	
	/** get the nodes connected by the link */
	public int[] getconnected() {
		return linkednodes;
	}
}
