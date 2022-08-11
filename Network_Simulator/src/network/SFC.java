package network;

import java.util.ArrayList;

public class SFC {
	//Service Function Chains
	
	ArrayList<Double[]> embeddedcpu=new ArrayList<Double[]>();		//cpu demands of embedded VNFs
	ArrayList<Double[]> embeddedband=new ArrayList<Double[]>();		//bandwidth demands of embedded VNFs
	int duration=0;													//VNF lifecycle duration
	int banddemand=0;												//bandwidth demands
	Double inserver=0.0;											//in-server virtual traffic
	
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
	
	//getters for embedded VNF demands
	public ArrayList<Double[]> getcpu(){
		return embeddedcpu;
	}
	
	public ArrayList<Double[]> getband(){
		return embeddedband;
	}
	
	public int getduration() {
		return duration;
	}
	
	public int getbanddemand() {
		return banddemand;
	}
	
	public Double getinserver() {
		return inserver;
	}
	
	//modify VNF lifecycle
	public void increaseduration() {
		duration++;
	}
	
	public void reduceduration() {
		duration--;
	}
}
