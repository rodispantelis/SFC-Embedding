package greedy;

import java.util.ArrayList;

public class Greedy {
	network.Network net;
	int sr;														//size of servers registry
	int servperrack;
	int[] servercnt;
	int duration=0;												//VNF lifecycle duration
	int clustersize;											//size of the cluster of candidate hosts
	int fnodes1=1;												//parameter to define cluster size
	int[] mapping;
	Double fitness;
	network.VNFgraph vnfgraph;
	boolean reject=false;
	boolean printmapping=false;
	boolean solfound=false;
	boolean recompute=false;									//recompute mapping
	int recnt=0, relim=1;										//computing iterations counter and limit
	
	network.Codec cod= new network.Codec();
	String err=" ";												//reason for error
	Double msg0[];

	ArrayList<Double[]> nodes=new ArrayList<Double[]>();		//stores intermediate node sets
	ArrayList<Double[]> nodessort=new ArrayList<Double[]>();	//sorted outputs
	ArrayList<Integer> cluster=new ArrayList<Integer>();		//cluster of nodes to compute mapping
	
	boolean newversion=false;
	
	public Greedy(network.Network net) {
		this.net=net;
		this.servperrack=net.getservperrack();
		this.sr=(net.getservers()*(net.getservers()-1)/2);
	}
	
	public void getrequest(network.VNFgraph vnfg) {
		vnfgraph=vnfg;
	}

	public void compute() {
		//compute mapping
		net.nodecapsort();
		vnfgraph.nodedemsort();
		mapping=new int[vnfgraph.nodes];
		
		for(int im=0;im<vnfgraph.nodes;im++) {
			mapping[im]=-1;
		}
		
		Double[] tempload=new Double[net.getservers()];
		for(int tl=0;tl<tempload.length;tl++) {
			tempload[tl]=0.0;
		}		
		
		for(int m=(vnfgraph.nodes-1);m>(-1);m--) {
			for(int n=(net.getservers()-1);n>(-1);n--) {
				if(mapping[vnfgraph.getsortednode(m)]<0) {
					if(vnfgraph.getnodew()[vnfgraph.getsortednode(m)]<=
							(net.getserver(net.getsortednode(n)).getavailablecpu()
									-tempload[net.getsortednode(n)]) &&
										chkbnd(net.getsortednode(n), vnfgraph.getsortednode(m))
						) {
						mapping[vnfgraph.getsortednode(m)]=net.getsortednode(n);
						tempload[net.getsortednode(n)]+=vnfgraph.getnodew()[vnfgraph.getsortednode(m)];
						break;
					}
				}
			}
		}
		
		for(int ma=0;ma<mapping.length;ma++) {
			if(mapping[ma]<0) {
				reject=true;
			}
		}
	}
	
	public int[] getmapping() {
		return mapping;
	}
	
	public boolean isrejected() {
		return reject;
	}
	
	public boolean inlist(int n, ArrayList<Integer> pnodes) {
		//is node n in list?
		boolean r=false;
		ArrayList<Integer> temp=pnodes;
		
		for(int i=0; i<temp.size();i++) {
			if(n==temp.get(i)) {
				r=true;
				break;
			}
		}
		
		return r;
	}
	
	public void setduration(int d) {
		//VNF lifecylce
		duration=d;
	}
	
	public boolean chkbnd(int snode, int vnode) {
		//check bandwidth constraints
		boolean r=true;
		int[] tmap=mapping;
		tmap[vnode]=snode;
		Double[] tempbandw=new Double[net.links.size()];
		
		int[] tempload=new int[net.getservers()];
		
		for(int n=0; n<vnfgraph.getnodes();n++) {
			if(tmap[n]>(-1)) {
				if((vnfgraph.getnodew()[n]+tempload[net.getserver(tmap[n]).getid()]+
						net.getserver(tmap[n]).getcpuload()) >(net.getserver(tmap[n]).getcpu())) {
					r=false;
					err="CPU constraint";
				}else {
					tempload[net.getserver(tmap[n]).getid()]+=(vnfgraph.getnodew()[n]);
				}
			}
		}
		
		if(r) {
			for(int i=0;i<tempbandw.length;i++) {
				tempbandw[i]=0.0;
			}

			for(int l=0;l<vnfgraph.getgraph().length;l++) {
				if(vnfgraph.getgraph()[l]>0) {
					int[] t1=cod.decoder(l);
					if(tmap[t1[0]]>=0 && tmap[t1[1]]>=0) {
						int[] t2=net.getserverpath(tmap[t1[0]],tmap[t1[1]]);
						for(int tt=0;tt<(t2.length-1);tt++) {
							if(t2[tt]!=t2[tt+1] && t2[tt]>=0 && t2[tt+1]>=0) {
								int tempve=cod.coder(t2[tt],t2[tt+1]);
								tempbandw[tempve]+=(0.0+(vnfgraph.getedgew()[l]/1000.0));
								if((net.links.get(tempve).getload()+tempbandw[tempve]) >
													net.links.get(tempve).getcapacity()) {
										r=false;
										err="Bandwidth constraint";
								}	
							}
						}
					}
				}
			}
		}
		return r;
	}
	
	public void setprintmapping(boolean b){
		//if true mapping will be printed on screen
		printmapping=b;
	}
}






