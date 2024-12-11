package services;

public class Stakeholder {
	int id=0;
	String vnfgraphs="/EVgraphs/";
	String setspatial="true";
	
	public Stakeholder(int id, String vnfgraphs, String setspatial) {
		this.id=id;
		this.vnfgraphs=vnfgraphs;
		this.setspatial=setspatial;
	}

	public int getid() {
		return id;
	}
	
	public String getvnfgraphs() {
		return vnfgraphs;
	}
	
	public String getsetspatial() {
		return setspatial;
	}
}
