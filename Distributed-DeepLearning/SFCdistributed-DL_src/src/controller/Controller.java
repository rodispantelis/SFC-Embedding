package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/*   
Copyright 2022 Panteleimon Rodis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import java.util.ArrayList;
import java.util.Arrays;

/** Network controller that runs the distributed multi-threaded algorithm. */
public class Controller extends Thread{
	network.Network net;
	/** Size of servers registry. */
	int sr;
	/** Servers per rack. */
	int servperrack;
	/** Measures the cluster nodes sent to each server. */
	int[] servercnt;
	/** VNF lifecycle duration. */
	int duration=0;
	/** Size of the cluster of candidate hosts. */
	int clustersize;
	/** Parameter to define cluster size. */
	int fnodes;		
	/** Produced mapping. */
	int[] mapping= {0};
	/** Solution fitness. */
	Double fitness;
	network.VNFgraph vnfgraph;
	/** Is request rejected? */
	boolean reject=false;
	/** Print mapping? */
	boolean printmapping=true;
	/** Solution found? */
	boolean solfound=false;
	network.Codec cod= new network.Codec();
	/** error message */
	String err=" ";
	/** Messages to the agents. */
	Double msg0[];			
	/** Recompute in case of failure? */
	boolean recompute=false;	
	/** Solution edge weights in Edge Vector indexing. */
	double[] solutionew;	
	/** Current model in use. */
	double[] currentmodel= {0.0};			
	
	/** Agent modes */
	Double mode=2.0;		//agent modes;1.0 greedy;2.0 train and run network
							//;3.0 train new network in each request; 4.0 run only stored model
	/** Maximum SFC size */
	int maxsfcsize=0;	
	
	/** Construct distributed algorithm object on input of network and algorithm mode. */
	public Controller(network.Network net, Double mode) {
		this.mode=mode;
		this.net=net;
		this.servperrack=net.getservperrack();
		this.sr=(net.getservers()*(net.getservers()-1)/2);
		
		for(int s=0;s<net.getservers();s++) {
			net.getserver(s).agent=new Agent();
		}
	}
	
	/** Get request. */
	public void getrequest(network.VNFgraph vnfg) {
		vnfgraph=vnfg;
	}
	
	/** Run distributed algorithm. */
	public void compute() {

		vnfgraph.demands();
		clustersize=fnodes*vnfgraph.getnodes();
		
		//make message that sends size, demands and new VNF-chain to nodes
		msg0=new Double[3+vnfgraph.getnodes()];
			
		msg0[0]=0.0;
		msg0[1]=vnfgraph.getmaxnodew()+0.0;
		msg0[2]=vnfgraph.getnodes()+0.0;
		
		//message is sent
		for(int m=0;m<vnfgraph.getnodes();m++) {
			msg0[m+3]=vnfgraph.getnodew()[m]+0.0;
		}
		
		solfound=false;
		
		//use cluster formation functions distribute1(), distribute3()
		//in order to serve request
		
		if(!solfound) {
			distribute1m();
			computesolution();
		}

		if(!solfound) {
			distribute3m();
			computesolution();
		}
		
		if(!solfound) {
			distribute1();
			computesolution();
		}

		if(!solfound) {
			distribute3();
			computesolution();
		}
		

		
		if(!solfound) {
			reject=true;
		}
		
		//print result on screen
		
		if(printmapping && !reject) {
			System.out.println(Arrays.toString(mapping));
		}		
		
		if(!reject) {
			int du=(int) (Math.random()*duration);
			net.setduration(du);
			net.embed(vnfgraph, mapping);
			String solnew="";
				solnew=Arrays.toString(solutionew);

			if(solnew.length()>5 && mode<4.0) {
				storemodel(vnfgraph.nodes, solnew.subSequence(1, (solnew.length()-1)).toString());
			}
			if(mode<4.0) {
				mode=2.0;
			}
		}else {
			if(mode==2.0) {
				System.out.println(" training...");
				reject=false;
				mode=3.0;
				recompute=true;
				compute();
				mode=2.0;
			}else if(mode<4.0){
				System.out.println(" > Rejection.");
				mode=2.0;
			}else if(mode==4.0){
				System.out.println(" > Rejection.");
			}
		}
		
		if(mode<4.0) {
			mode=2.0;
		}
	}
	
	/** Compute and get the best solution computed by the distributed agents. */
	public void computesolution() {
		//send message to nodes to compute candidate solution nodes
		//long startTime=System.nanoTime();
		Double[] m0={2.0, mode};
		
		int trds=Thread.getAllStackTraces().keySet().size();
		
		for(int c=0;c<net.getservers();c++) {
			(new agentthread(m0, c)).start();			//uncomment for MULTI-THREAD
			//sendmessage(new network.Message(m0), c);	//uncomment for SINGLE THREAD
		}
		
		while(Thread.getAllStackTraces().keySet().size()>trds) {
			//wait for threads running in agents to finish
		}
		
		fitness=-1.0;
		int mod=0;
		
		for(int i1=0;i1<net.getservers();i1++) {
			Agent ag=(Agent) net.getserver(i1).getagent();
			if(ag.getfitness()>=0 && ag.getfitness()<1000.0) {
				if(fitness<0.0 && ag.getfitness()>=0.0) {
					fitness=ag.getfitness();
					mapping=ag.getoutput();
					solfound=true;
					mod=i1;
				}else if(ag.getfitness()<fitness && ag.getfitness()<1000.0) {
					fitness=ag.getfitness();
					mapping=ag.getoutput();
					solfound=true;
					mod=i1;
				}
			}
			if(mapping.length>1 && net.checkembed(vnfgraph, mapping)) {
				fitness=-1.0;
				solfound=false;
			}
		}
		
		if(fitness>=0.0 && fitness<1000.0) {
			Agent ag=(Agent) net.getserver(mod).getagent();
			solutionew=ag.getresmodel();
		}else {
			Agent ag=(Agent) net.getserver(0).getagent();
			currentmodel=ag.getcurrentmodel();
		}
		

	}
	
	/** Cluster formation function #1. */
	public void distribute1() {
		servercnt=new int[net.getservers()];	//measures the cluster nodes sent to each server

		//initialize distributed computation, erase previous VNF-chain data
		for(int s=0;s<net.getservers();s++) {
			sendmessage(new network.Message(msg0), s);
		}
		
		for(int sn=0;sn<net.getservers();sn++) {
			addnode(sn);
		}

		//for every server check if it can host the minimum demand and does not exit maximum VNF demand
		//if so use its adjacent nodes as candidate hosts
		for(int r2=(sr-1);r2>(-1);r2--) {
			int[] t2=net.getservreg(r2);
			//check demands
			if(net.getserver(t2[0]).getavailablecpu()<=vnfgraph.maxnodew &&
					net.getserver(t2[1]).getavailablecpu()<=vnfgraph.maxnodew &&
						(getminband(t2[0], t2[1]).intValue()*1000)>=(vnfgraph.maxacedgew*1.0)) {
							addnodes(t2[0],t2[1]);
			}
		}
		
	}
	
	/** Cluster formation function #1m. */
	public void distribute1m() {
		servercnt=new int[net.getservers()];	//measures the cluster nodes sent to each server

		//initialize distributed computation, erase previous VNF-chain data
		for(int s=0;s<net.getservers();s++) {
			sendmessage(new network.Message(msg0), s);
		}
		
		for(int sn=0;sn<net.getservers();sn++) {
			addnode(sn);
		}

		//for every server check if it can host the minimum demand and does not exit maximum VNF demand
		//if so use its adjacent nodes as candidate hosts
		for(int r2=(sr-1);r2>(-1);r2--) {
			int[] t2=net.getservreg(r2);
			//check demands
			if(net.getserver(t2[0]).getavailablecpu()<=vnfgraph.maxnodew &&
					net.getserver(t2[1]).getavailablecpu()<=vnfgraph.maxnodew &&
						(getminband(t2[0], t2[1]).intValue()*1000)>=(vnfgraph.minedgew*1.0)) {
							addnodes(t2[0],t2[1]);
			}
		}
		
	}
	
	/** Cluster formation function #2. EXPERIMENTAL. */
	public void distribute2() {
		servercnt=new int[net.getservers()];	//measures the cluster nodes sent to each server

		//initialize cluster formation
		for(int s=0;s<net.getservers();s++) {
			sendmessage(new network.Message(msg0), s);
		}
		
		for(int sn=0;sn<net.getservers();sn++) {
			addnode(sn);
		}

		//for every pair of servers check if they can host the maximum demand VNF
		//if so use its adjacent nodes as candidate hosts
		for(int r=(sr-1);r>(-1);r--) {
			int[] t=net.getservreg(r);
			
			if(net.getserver(t[0]).getavailablecpu()>vnfgraph.maxnodew &&
					net.getserver(t[1]).getavailablecpu()>vnfgraph.maxnodew &&
						(getminband(t[0], t[1]).intValue()*1000)>=(vnfgraph.maxacedgew*1.0)) {
							addnodes(t[0],t[1]);
			}
		}
	}
	
	/** Cluster formation function #3. */
	public void distribute3() {
		servercnt=new int[net.getservers()];	//measures the cluster nodes sent to each server
		
		//initialize distributed computation, erase previous VNF-chain data		
		for(int s=0;s<net.getservers();s++) {
			sendmessage(new network.Message(msg0), s);
		}
		
		for(int sn=0;sn<net.getservers();sn++) {
			addnode(sn);
		}
		
		//for every pair of servers check demands
		//if so use its adjacent nodes as candidate hosts
		for(int r=(sr-1);r>(-1);r--) {
			int[] t=net.getservreg(r);
			
			if(net.getserver(t[0]).getavailablecpu()>=vnfgraph.minnodew &&
					net.getserver(t[1]).getavailablecpu()>=vnfgraph.minnodew &&
						(getminband(t[0], t[1]).intValue()*1000)>=(vnfgraph.maxacedgew*1.0)) {
							addnodes(t[0],t[1]);
			}
		}
	}
	
	/** Cluster formation function #3m. */
	public void distribute3m() {
		servercnt=new int[net.getservers()];	//measures the cluster nodes sent to each server
		
		//initialize distributed computation, erase previous VNF-chain data		
		for(int s=0;s<net.getservers();s++) {
			sendmessage(new network.Message(msg0), s);
		}
		
		for(int sn=0;sn<net.getservers();sn++) {
			addnode(sn);
		}
		
		//for every pair of servers check demands
		//if so use its adjacent nodes as candidate hosts
		for(int r=(sr-1);r>(-1);r--) {
			int[] t=net.getservreg(r);
			
			if(net.getserver(t[0]).getavailablecpu()>=vnfgraph.minnodew &&
					net.getserver(t[1]).getavailablecpu()>=vnfgraph.minnodew &&
						(getminband(t[0], t[1]).intValue()*1000)>=(vnfgraph.minedgew*1.0)) {
							addnodes(t[0],t[1]);
			}
		}
	}
	
	/** Request rejected? */
	public boolean isrejected() {
		return reject;
	}
	
	/** Is node in list? */
	public boolean inlist(int n, ArrayList<Integer> pnodes) {
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
	
	/** Get minimum available bandwidth between two nodes. */
	private Double getminband(int a, int b) {
		int[] temp=net.getserverpath(a, b);
		Double minband=net.getband(temp[0], temp[1]);
		
		for(int t=2;t<temp.length;t++) {
			if(minband>net.getband(temp[t-1], temp[t])) {
				minband=net.getband(temp[t-1], temp[t]);
			}
		}
		return minband;
	}
	
	/** Add another node to cluster by sending message to the agent. */
	private void addnode(int a) {
			Double[] h2={(1+0.0), (a+0.0),net.getserver(a).getavailablecpu()};
			sendmessage(new network.Message(h2), a);
			servercnt[a]++;
	}
	
	/** Set VNF lifecyle. */
	public void setduration(int d) {
		duration=d;
	}
	
	/** Set cluster size. */
	public void setclustersize(int fnodes1) {
		fnodes=fnodes1;
	}

	/** Set maximum node size. */
	public void setmaxsfcsize(int maxsize) {
		Double[] msg4={4.0, maxsize*1.0};
		for(int s=0;s<net.getservers();s++) {
			sendmessage(new network.Message(msg4), s);
		}
		maxsfcsize=maxsize;
	}
	
	/** Send candidate hosts to distributed functions. */
	private void addnodes(int a, int b) {
		if(servercnt[b]<clustersize) {
			Double[] h1={(1+0.0), (a+0.0),net.getserver(a).getavailablecpu()};
			sendmessage(new network.Message(h1), b);
			servercnt[b]++;
		}
		
		if(servercnt[a]<clustersize) {
			Double[] h2={(1+0.0), (b+0.0),net.getserver(b).getavailablecpu()};
			sendmessage(new network.Message(h2), a);
			servercnt[a]++;
		}
	}
	
	/** Send messages to servers. */
	private void sendmessage(network.Message m, int server) {
		Agent agen=(Agent) net.getserver(server).getagent();
		agen.getmessage(m);
	}
	
	/** Check bandwidth. */
	public boolean chkbnd(int snode, int vnode) {
		
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
	
	/** Set true to print the output on screen. */
	public void setprintmapping(boolean b){
		printmapping=b;
	}
	
	/** Store model. */
	public void storemodel(int sz, String b) {

        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;

		try {
			try {
				fw = new FileWriter("model.csv",true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.println(sz+"|"+b);
			pw.flush();
		}finally {
	        try {
	             pw.close();
	             bw.close();
	             fw.close();
	        } catch (IOException io) { 
	        	}
		}
	}
	
	
	/** Thread that runs agent. */
	class agentthread implements Runnable {
		   private Thread t;
		   private String threadName="agent";
		   network.Message nm;
		   int c;
		   
		   public agentthread(Double[] m0, int c) {
			   this.c=c;
			   this.nm=new network.Message(m0);
		   }
		   
		   public void run() {
			   sendmessage(nm, c);
			   
		   }
		   
		   public void start () {
		         t = new Thread (this, threadName);
		         t.start ();
		   }
	}
}






