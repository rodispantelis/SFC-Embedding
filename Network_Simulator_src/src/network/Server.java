package network;

/** Network Server */

public class Server {
	/** server ID */
	int id;
	/** rack ID */
	int rackid;
	/** total server capacity in GHz */
	Double cpu=0.0;
	/** total CPU load in GHz */
	Double cpuload=0.0;
	/** memory in GBytes */
	Double memory=0.0;
	/** memory load in GBytes */
	Double memload=0.0;
	/** storage in GBytes */
	Double storage=0.0;
	/** storage load in GBytes */
	Double storload=0.0;
	/** VNF counter */
	int vnfcnter=0;
	
	/** agent object for distributed multiagent computing; 
	 * create an agent object and cast it here */
	public Object agent;	
	
	/** construct server by providing rackid and server id */
	
	public Server(int rackid, int id) {
		this.rackid=rackid;
		this.id=id;
	}

	//getters setters
	
	/** set CPU, memory and storage parameters */
	public void setparams(Double cpu, Double memory, Double storage) {
		this.cpu=cpu;
		memload=memory;
		storload=storage;
	}

	/** get id */
	public int getid() {
		return id;
	}
	
	/** get agent object hosted in the server */
	public Object getagent(){
		return agent;
	}
	
	/** add a VNF */
	public void addvnf() {
		vnfcnter++;
	}
	
	/** remove one VNF */
	public void remvnf(int rem) {
		vnfcnter-=rem;
	}
	
	/** set total CPU capacity */
	public void setcpu(Double cap) {
		cpu=cap;
	}
	
	/** get number of embedded VNFs */
	public int getvnfs() {
		return vnfcnter;
	}
	
	/** get rack id */
	public int getrackid() {
		return rackid;
	}
	
	/** get total CPU capacity */
	public Double getcpu() {
		return cpu;
	}
	
	/** get total CPU load */
	public Double getcpuload() {
		return cpuload;
	}
	
	/** get available CPU capacity */
	public Double getavailablecpu() {
		Double r=cpu-cpuload;
		return r;
	}
	
	/** get memory */
	public Double getmemory() {
		return memory;
	}
	
	/** get storage */
	public Double getstorage() {
		return storage;
	}
	
	/** get memory load */
	public Double getmemload() {
		return memload;
	}
	
	/** add cpu load */
	public void addcpuload(Double a){
		cpuload+=a;
	}
	
	/** remove cpu load */
	public void remcpuload(Double a){
		cpuload-=a;
	}
	
	/** add memory load */
	public void addmemload(Double a){
		memload+=a;
	}
	
	/** remove memory load */
	public void remmemload(Double a){
		memload-=a;
	}
	
	/** add storage load */
	public void addstorl(Double a){
		storload+=a;
	}
	
	/** remove storage load */
	public void remstorl(Double a){
		storload-=a;
	}
}
