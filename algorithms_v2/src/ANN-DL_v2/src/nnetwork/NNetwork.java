package nnetwork;

import java.util.ArrayList;

/** Implements the neural network. */
public class NNetwork {
	Topology topo;					//network topology
	String nettype;					//network type
	Node[] net;						//network nodes
	double[] ew;					//edge weights in Edge Vector indexing
	ArrayList<Double> output=new ArrayList<Double>();
	Double sqerr=0.0;
	Codec cod=new Codec();
	int clustersize=0;
	//int spatial=0;
	
	/** Construct network on input topology. */
	public NNetwork(Topology topo) {
		//network parameters
		this.topo=topo;
		this.nettype=topo.nettype;
		this.net=new Node[topo.allnodes];
		this.ew=new double[topo.ev.length];

		//initialize network nodes
		//input nodes
		for(int i=0;i<topo.inodes.length;i++) {
			net[i]=new Node(i,"input",topo.function, topo.bias);
		}
		
		//hidden layer nodes
		for(int o=(topo.inodes.length);o<(topo.allnodes-topo.onodes.length);o++) {
			net[o]=new Node(o,"ionode",topo.function, topo.bias);
		}
		
		//output nodes
		for(int a=(topo.allnodes-topo.onodes.length);a<topo.allnodes;a++) {
			net[a]=new Node(a,"output",topo.ofunction, topo.bias);
		}

		for(int n=0;n<topo.ev.length;n++) {
				int[] temp=cod.decoder(topo.ev[n]);
				
				switch(topo.evdir[n]) {
				case 1: 
					net[temp[0]].addoutto(temp[1]);
					net[temp[1]].addinfrom(temp[0]);
				break;
				case 2: 
					net[temp[1]].addoutto(temp[0]);
					net[temp[0]].addinfrom(temp[1]);
				break;
				case 3: 
					net[temp[0]].addoutto(temp[1]);
					net[temp[1]].addinfrom(temp[0]);
					net[temp[1]].addoutto(temp[0]);
					net[temp[0]].addinfrom(temp[1]);
				break;
			}
		}
		
		//assign random values in network edges
		
		for(int e=0;e<topo.ev.length;e++) {
				ew[e]=Math.random()*topo.allnodes;
				if(Math.random()<0.5) {
					ew[e]=-ew[e];
				}
		}
	}
	
	/** Run neural network. */
	public void runff(Double[] tset) {

		for(int in=0;in<topo.inodes.length;in++) {
			net[topo.inodes[in]].clearinputs();
		}
		
		int l=tset.length;
		for(int i=0;i<l;i++) {
			net[topo.inodes[i]].addinput(tset[i]);
		}

		forward();
	}
		
	/** Forward propagation. */
	public void forward() {
		output.clear();
		while(output.size()<topo.onodes.length) {
			for(int n=0;n<net.length;n++) {
				if(net[n].inputs.size()>0) {
					net[n].cum();
					double temp=0;
					if(net[n].outputto.size()>0) {
						for(int r=0;r<net[n].outputto.size();r++) {
							//edge weight multiplied with output of previous layer node
							int tt1=cod.coder(net[n].id, net[n].outputto.get(r));
							int tt2=0;
							
							for(int i=0;i<topo.ev.length;i++) {
								if(tt1==topo.ev[i]) {
									tt2=i;
									break;
								}
							}
							temp=net[n].cum*ew[tt2];
							if(!net[n].gettype().equals("input")) {
								//add bias in x*w input in ionode
									temp+=net[n].getbias();
								}
							//then the activation function is computed
							temp=net[n].activation(temp);
							//added as input in the next layer
							net[net[n].outputto.get(r)].inputs.add(temp);
						}
						net[n].inputs.clear();
					}else {
						net[n].inputs.clear();
						output.add(net[n].activation(net[n].cum));
					}
				}
			}
		}
	}
	
	/** Compute fitness. */
	public Double fitness(Double[] trec) {
		Double fit=0.0;

		int[] out=new int[output.size()];
		Double[] trec2=new Double[trec.length];
		
		for(int m=0;m<trec2.length;m++) {
			trec2[m]=0.0;
		}

		for(int i=0;i<output.size();i++) {
			out[i]=output.get(i).intValue();
		}

		for(int c=0;c<out.length;c++) {
			if(out[c]>=clustersize/* || out[c]<0*/) {
				fit+=1000000;
			}else if(trec[trec.length-output.size()+c]<0 && out[c]>=0) {
				fit+=1000000;
			}else if(trec[trec.length-output.size()+c]>=0 && out[c]<0){
				fit+=1000000;
			}else if(out[c]>=0 && trec[out[c]]>=0){
				trec2[out[c]]+=trec[trec.length-output.size()+c];
			}
		}
		
		for(int d=0;d<trec.length;d++) {
			if(trec2[d]>0) {
				Double tt=trec[d]-trec2[d];
				if(tt<0.0) {
					fit+=1000000;
				}else {
					fit+=tt;
				}
			}
		}
		
		return fit;
	}
	
	/** Compute square error. */
	public Double sqerror(Double[] tset, int outputs) {	
		int inputs=tset.length-outputs;
		Double[] ex=new Double[outputs];
		for(int i1=0;i1<outputs;i1++) {
			ex[i1]=tset[inputs+i1];
		}

		Double er=0.0;
		for(int i=0;i<outputs;i++) {
			er+=Math.pow(ex[i]-output.get(i),2);
		}
		er=er/outputs;

		return er;
	}
	
	/** set size of substrate nodes cluster */
	public void setclustersize(int a) {
		clustersize=a;
	}
	
	/** Get topology. */
	public Topology gettopo() {
		return topo;
	}
	
	/** Print NN output. */
	public double[] getoutput() {
		double[] gout=new double[output.size()];
		for(int w=0;w<output.size();w++) {
			gout[w]=output.get(w);
		}
		return gout;
	}
	
	/** Set link weights. */
	public void setew(double[] newew) {
		ew=newew;
	}

	/** Get link weights. */
	public double[] getew() {
		return ew;
	}
}

