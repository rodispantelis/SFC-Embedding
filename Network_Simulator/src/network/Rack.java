package network;

import java.util.ArrayList;

public class Rack {
	int id;
	int pod;													//pod id if defined in network topology
	int ToRswitch;												//ToR switch id
	public ArrayList<Server> servers=new ArrayList<Server>(); 	//ArrayList of servers in the rack
	
	public Rack(int servers, int rackid, int cnt) {
		this.id=rackid;
		for(int i=0;i<servers;i++) {
			this.servers.add(new Server(rackid,(cnt*servers)+i));
			this.servers.get(i).setparams(10.0,20.0,1024.0);	//cpu, memory, storage
		}
	}
	
	//getters setters
	public String getids() {
		return id+" ToR:"+ToRswitch;
	}
	
	public int gettor() {
		return ToRswitch;
	}
	
	public int getservers() {
		return servers.size();
	}
	
	public Server getserver(int s) {
		return servers.get(s);
	}
	
	public void setpod(int p) {
		pod=p;
	}
	
	public int getpod() {
		return pod;
	}
}
