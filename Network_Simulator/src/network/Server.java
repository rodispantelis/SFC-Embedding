package network;

/** Network Server */

public class Server {
	int id;
	int rackid;
	Double cpu=0.0;			//GHz
	Double cpuload=0.0;		//GHz
	Double memory=0.0;		//GBytes
	Double memload=0.0;		//GBytes
	Double storage=0.0;		//GBytes
	Double storload=0.0;	//GBytes
	int vnfcnter=0;			//VNF counter
	network.Agent agent;	//agent object for distributed multiagent computing
	
	/** construct server by providing rackid and server id */
	
	public Server(int rackid, int id) {
		this.rackid=rackid;
		this.id=id;
		this.agent=new network.Agent();
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
	public network.Agent getagent(){
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
	
	/** set CPU */
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
	
	/** get CPU capacity */
	public Double getcpu() {
		return cpu;
	}
	
	/** get CPU load */
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
	
	/** forward messages to the agents */
	public void getmessage(Message m) {
		if(m.msg[0]==1) {
			agent.flag1(m.msg);
		}else if(m.msg[0]==0) {
			agent.flag0(m.msg);
		}else if(m.msg[0]==2) {
			agent.compute();
		}else if(m.msg[0]==3) {
			agent.flag3();
		}
	}
}
