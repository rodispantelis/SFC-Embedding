package network;

import java.util.ArrayList;

/** Network Rack */

public class Rack {
	int id;
	int pod;													//pod id if defined in network topology
	int ToRswitch;												//ToR switch id
	public ArrayList<Server> servers=new ArrayList<Server>(); 	//ArrayList of servers in the rack
	
	/** construct a rack by providing number of servers, rackid
	 * and number of racks already in the network */
	public Rack(int servers, int rackid, int cnt) {
		this.id=rackid;
		for(int i=0;i<servers;i++) {
			this.servers.add(new Server(rackid,(cnt*servers)+i));
			this.servers.get(i).setparams(10.0,20.0,1024.0);	//cpu, memory, storage
		}
	}
	
	/** print rack and TOR switch ids */
	public String getids() {
		return id+" ToR:"+ToRswitch;
	}
	
	/** get TOR switch id */
	public int gettor() {
		return ToRswitch;
	}
	
	/** get number of servers */
	public int getservers() {
		return servers.size();
	}
	
	/** get a single server */
	public Server getserver(int s) {
		return servers.get(s);
	}
	
	/** set pod id */
	public void setpod(int p) {
		pod=p;
	}
	
	/** get pod id */
	public int getpod() {
		return pod;
	}
}
