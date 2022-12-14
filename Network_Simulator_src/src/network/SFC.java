package network;

import java.util.ArrayList;

/** Service Function Chain */

public class SFC {
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
	
	/** modify VNF lifecycle */
	public void increaseduration() {
		duration++;
	}
	
	/** reduce VNF lifecycle */
	public void reduceduration() {
		duration--;
	}
}
