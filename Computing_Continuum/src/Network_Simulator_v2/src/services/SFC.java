package services;

import java.util.ArrayList;

/** Embedded Service Function Chain */

public class SFC {
	/** SFC ID */
	int id=0;
	/** cpu demands of embedded VNFs */
	ArrayList<Double[]> embeddedcpu=new ArrayList<Double[]>();
	/** bandwidth demands of embedded VNFs */
	ArrayList<Double[]> embeddedband=new ArrayList<Double[]>();
	/** VNF lifecycle duration */
	int duration=0;
	/** bandwidth demands */ 
	int banddemand=0;												
	/** in-server virtual traffic */
	Double inserver=0.0;
	
	public SFC(ArrayList<Double[]> embeddedcpu, ArrayList<Double[]> embeddedband) {
		this.embeddedcpu=embeddedcpu;
		this.embeddedband=embeddedband;
	}
	
	public SFC(ArrayList<Double[]> embeddedcpu, ArrayList<Double[]> embeddedband, 
			int duration, int banddemand, Double inserver) {
		this.embeddedcpu=embeddedcpu;
		this.embeddedband=embeddedband;
		this.duration=duration;
		this.banddemand=banddemand;
		this.inserver=inserver;
	}
	
	public SFC(ArrayList<Double[]> embeddedcpu, ArrayList<Double[]> embeddedband, 
			int duration, int banddemand, Double inserver, int id) {
		this.embeddedcpu=embeddedcpu;
		this.embeddedband=embeddedband;
		this.duration=duration;
		this.banddemand=banddemand;
		this.inserver=inserver;
		this.id=id;
	}
	
	/** get id */
	public int getid() {
		return id;
	}
	
	/** get capacity demands */
	public ArrayList<Double[]> getcpu(){
		return embeddedcpu;
	}
	
	/** get bandwidth demands*/
	public ArrayList<Double[]> getband(){
		return embeddedband;
	}
	
	/** get duration lifecycle */
	public int getduration() {
		return duration;
	}
	
	/** get in-server virtual traffic */
	public int getbanddemand() {
		return banddemand;
	}
	
	/** get in-server virtual traffic */
	public Double getinserver() {
		return inserver;
	}
	
	/** set id */
	public void setid(int s) {
		id=s;
	}
	
	/** modify VNF lifecycle */
	public void increaseduration() {
		duration++;
	}
	
	/** reduce VNF lifecycle */
	public void reduceduration() {
		duration--;
	}
}
