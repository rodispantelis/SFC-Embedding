package network;

import java.util.ArrayList;

public class Switch {
	public int id;
	String type=""; 						//edge,core,aggregation, ToR, leaf, spine etc
	ArrayList<Integer> connectedto=new ArrayList<Integer>(); //IDs of connected switches
	
	public Switch(int id) {
		this.id=id;
	}
	
	public void connetto(int c) {
		connectedto.add(c);
	}
	
	public void settype(String s) {
		type=s;
	}
	
	public String getconnections() {
		return id+"|"+type+"|"+connectedto.toString();
	}
	
	public ArrayList<Integer> getcons() {
		return connectedto;
	}
}
