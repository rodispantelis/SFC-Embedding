package network;

import java.util.ArrayList;

/** network switch */

public class Switch {
	/** switch ID */
	public int id;
	/** switch type: edge,core,aggregation, ToR, leaf, spine etc */ 
	String type="";
	/** IDs of connected nodes */
	ArrayList<Integer> connectedto=new ArrayList<Integer>(); 
	
	/** construct switch, set its id */
	public Switch(int id) {
		this.id=id;
	}
	
	/** add serverx connected to the switch */
	public void connetto(int c) {
		connectedto.add(c);
	}
	
	/** set switch type */
	public void settype(String s) {
		type=s;
	}
	
	/** print connected servers */
	public String getconnections() {
		return id+"|"+type+"|"+connectedto.toString();
	}
	
	/** get connected servers */
	public ArrayList<Integer> getcons() {
		return connectedto;
	}
}
