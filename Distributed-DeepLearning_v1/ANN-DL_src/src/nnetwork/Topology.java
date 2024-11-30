package nnetwork;

/** Network topology. */
public class Topology {

	String nettype;				//network type
	int[] ev;					//interconnections in Edge Vector index format
	int[] evdir;				//edge directions in Edge Vector index format
	int[] inodes;				//index of input nodes
	int[] onodes;				//index of output nodes
	int nodes;					//number of nodes in every hidden layer
	int allnodes;				//total number of nodes in network
	int hidden;					//number of hidden layers
	double bias;				//bias
	int upper, lower;			//edge weight upper and lower bounds
	String function, ofunction;	//activation functions for layer nodes and output nodes
	Codec cod=new Codec();
	
	/** Construct network topology. */
	public Topology(String type, int inputs, int outputs, int hidden, int nodes, int allnodes,
			String function, String ofunction, double bias, int upper, int lower) {
		this.allnodes=allnodes;
		this.nettype=type;
		this.function=function;
		this.ofunction=ofunction;
		this.bias=bias;
		this.upper=upper;
		this.lower=lower;
		
		if(type.equals("ff")) {
			//number of nodes per layer
			this.hidden=hidden;
			ev=new int[inputs*nodes+(hidden-1)*nodes*nodes+nodes*outputs];
			evdir=new int[ev.length];
			inodes=new int[inputs];
			onodes=new int[outputs];
			int evindex=0;
		
			//generate topology and store it in EV format
			for(int i=0;i<inputs;i++) {
				inodes[i]=i;
				for(int h1=0;h1<nodes;h1++) {
					ev[evindex]=cod.coder(i,(h1+inputs));
					evdir[evindex]=1;
					evindex++;
				}
			}
		
			for(int h=0;h<hidden;h++) {
				for(int n1=0;n1<nodes;n1++) {
					for(int n2=0;n2<nodes;n2++) {
						if((h+1)<hidden) {
							ev[evindex]=cod.coder((h*nodes+inputs+n1),((h+1)*nodes+n2+inputs));
							evdir[evindex]=1;
							evindex++;
						}
					}
				}
			}
		
			for(int o=0;o<outputs;o++) {
				onodes[o]=allnodes-outputs+o;
			}
			
			for(int l=0;l<nodes;l++) {
				for(int o=0;o<outputs;o++) {
					ev[evindex]=cod.coder(((hidden-1)*nodes+inputs+l),(allnodes-outputs+o));
					evdir[evindex]=1;
					evindex++;
				}
			}
		}
	}
	
	/** Get link weights. */
	public int[] getev() {
		return ev;
	}
	
	/** UNDER DEVELOPMENT */
	public void save2file() {
		
	}
	
	/** Get output nodes. */
	public int[] getonodes() {
		return onodes;
	}
	
	/** Get upper bound of link weights. */
	public int getupper() {
		return upper;
	}
	
	/** Get lower bound of link weights. */
	public int getlower() {
		return lower;
	}
}














