package network;

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
	
	public Server(int rackid, int id) {
		this.rackid=rackid;
		this.id=id;
		this.agent=new network.Agent();
	}

	public void setparams(Double cpu, Double memory, Double storage) {
		this.cpu=cpu;
		memload=memory;
		storload=storage;
	}
	
	//getters setters
	public int getid() {
		return id;
	}
	
	public network.Agent getagent(){
		return agent;
	}
	
	public void addvnf() {
		vnfcnter++;
	}
	
	public void remvnf(int rem) {
		vnfcnter-=rem;
	}
	
	public void setcpu(Double cap) {
		cpu=cap;
	}
	
	public int getvnfs() {
		return vnfcnter;
	}
	
	public int getrackid() {
		return rackid;
	}
	
	public Double getcpu() {
		return cpu;
	}
	
	public Double getcpuload() {
		return cpuload;
	}
	
	public Double getavailablecpu() {
		Double r=cpu-cpuload;
		return r;
	}
	
	public Double getmemory() {
		return memory;
	}
	
	public Double getstorage() {
		return storage;
	}
	
	public Double getmemload() {
		return memload;
	}
	
	//modify capacity server computational load
	public void addcpuload(Double a){
		cpuload+=a;
	}
	
	public void remcpuload(Double a){
		cpuload-=a;
	}
	
	public void addmemload(Double a){
		memload+=a;
	}
	
	public void remmemload(Double a){
		memload-=a;
	}
	
	public void addstorl(Double a){
		storload+=a;
	}
	
	public void remstorl(Double a){
		storload-=a;
	}
	
	//messages to the agent
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
