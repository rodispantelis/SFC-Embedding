package nnetwork;
import java.util.ArrayList;

import functions.Bipolarsigm;
import functions.Hypertansigm;
import functions.Linear;
import functions.ReLU;
import functions.Sigm;
import functions.Threshold;

/** Network node. */
public class Node{
	int id;						//node id
	String nodetype;			//node types: input; output; ionode (both I/O)
	String function;			//activation function
	double bias;				//bias
	ArrayList<Integer> inputfrom=new ArrayList<Integer>();
								//set the id's of the nodes to receive input from
	ArrayList<Integer> outputto=new ArrayList<Integer>();
								//set the id's of the nodes to output to
	ArrayList<Double> inputs=new ArrayList<Double>();			
								//input values x*w, x previous layer node, w edge weight
	double cum;					//cumulative inputs+bias
								//activation functions as objects
	Bipolarsigm b=new Bipolarsigm();
	Hypertansigm h=new Hypertansigm();
	Linear l=new Linear();
	ReLU r=new ReLU();
	Sigm s=new Sigm();
	Threshold t=new Threshold();
	
	public Node(int id, String nodetype, String function, double bias) {
		this.id=id;
		this.nodetype=nodetype;
		this.function=function;
		this.bias=bias;
	}
	
	/** Select activation function. */
	public double activation(double d) {
		double res=0;
		
		switch(function) {
		case "linear": 
			res=l.activation(d);
		break;
		case "sigmoid": 
			res=s.activation(d);
		break;
		case "threshold": 
			res=t.activation(d);
		break;
		case "bpsigm": 
			res=b.activation(d);
		break;
		case "relu": 
			res=r.activation(d);
		break;
		case "htf": 
			res=h.activation(d);
		break;
		}
		
		return res;
	}
	
	/** Add input in ArrayList. */
	public void addinput(Double i) {
		inputs.add(i);
	}
	
	/** Get input from ArrayList. */
	public double getinput(int in) {
		return inputs.get(in);
	}
	
	/** Clear inputs. */
	public void clearinputs() {
		inputs.clear();
	}
	
	/** Compute node cumulative output. */
	public void cum() {
		cum=0;
		for(int n=0;n<inputs.size();n++) {
			cum+=inputs.get(n);
		}
	}
	
	/** Add node to receive input from. */
	public void addinfrom(int e) {
		inputfrom.add(e);
	}
	
	/** Add node to output to. */
	public void addoutto(int s) {
		outputto.add(s);
	}
	
	/** Get inputs from connected nodes. */
	public int[] getinputfrom() {
		int[] r=new int[inputfrom.size()];
		for(int i=0;i<inputfrom.size();i++) {
			r[i]=inputfrom.get(i);
		}
		return r;
	}
	
	/** Get outpu to connected nodes. */
	public int[] getoutto() {
		int[] r=new int[outputto.size()];
		for(int i=0;i<outputto.size();i++) {
			r[i]=outputto.get(i);
		}
		return r;
	}
	
	//getters, setters
	
	/** Set node ID. */
	public void setid(int i) {
		id=i;
	}
	
	/** Set node type. */
	public void settype(String t) {
		nodetype=t;
	}
	
	/** Get node ID. */
	public int getid() {
		return id;
	}
	
	/** Get node type. */
	public String gettype() {
		return nodetype;
	}
	
	/** Set bias. */
	public void setbias(double b) {
		bias=b;
	}
	
	/** Get bias. */
	public double getbias() {
		return bias;
	}
	
	/** Get node type. */
	public String getnodetype() {
		return nodetype;
	}
	
	/** Get cumulative output. */
	public double getcum() {
		return cum;
	}
}
