package network;

import java.util.ArrayList;

/** network switch */

public class Switch {
	public int id;
	String type=""; 						//edge,core,aggregation, ToR, leaf, spine etc
	ArrayList<Integer> connectedto=new ArrayList<Integer>(); //IDs of connected switches
	
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
