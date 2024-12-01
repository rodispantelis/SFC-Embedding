package network;

/*   
Copyright 2024 Panteleimon Rodis

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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

import services.SFC;
import services.VNFgraph;

/** Fat-Tree Network class */

public class FTnetwork {

	/** network type */
	String type;
	/** domain name */
	String domain="";
	/** domain id */
	int id;
	/** parameter k */
	int k;
	/** servers per rack*/
	int servperrack;
	/** number of servers in network */
	int servers;								
	/** server registry */
	public int[][] servreg;	
	/** servers sorted by available capacity */
	int[] sortservac;
	/** racks */
	public ArrayList<Rack> racks=new ArrayList<Rack>();	
	/** switches */
	public ArrayList<Switch> switches=new ArrayList<Switch>();	
	/** links */
	public ArrayList<Link> links=new ArrayList<Link>();	
	/** embedded Serve Function Chains */
	ArrayList<SFC> embeddedSFCs=new ArrayList<SFC>();	
	/** VNF lifecycle duration */
	int duration=0;											
	/** Edge Vector coder-decoder */
	Codec cod=new Codec();
	/** maximum hop count between servers */
	int maxhop=0;		
	/** print on screen embedded SFC statistics */
	boolean printSFCstats=false;	
	/** print on screen network statistics */
	boolean printnetstats=false;
	/** Store statistics in a log file. */
	boolean storestats=true;
	/** last embedding outcome */
	boolean reject=false;	
	/** total number of requests */
	int requests=1;		
	/** successfully embedded requests */
	int successful=0;										
	/** statistics */
	Double band=0.0, cpu=0.0, totalcpu=0.0;		
	/** statistics */
	String results="", avcapacity="";
	DecimalFormat df = new DecimalFormat("#0.000");
	/** path to store log file */
	String path="";
	/** log file name */
	String filename="simulationresult.csv";					
	int lastrequestsize=0;
	/** error message string used for debugging */
	String errmess="";										
	/** embedding vnf revenue */
	Double reqrevenue=0.0;										
	/** embedding cost parameters */
	Double reqcost=0.0;
	/** Simulation iteration, request id */
	int iteration=0;
	
	/** maximum node capacity */
	double maxnodecpu=0.0;
	
	double intrarack=0.0, outerrack=0.0, remintra=0.0, remouter=0.0;
	
	/** domain agent object for distributed multiagent computing; 
	 * create an agent object and cast it here */
	public Object agent;
	
	/** network controller object; create a controller object and cast it here */
	public Object controller;
	
	public FTnetwork() {
		
	}
	
	/** constructor for building Fat-Tree topology network */
	public FTnetwork(String type, int k, int servperrack, int switches, int racks, int links) {
		this.type=type;
		this.k=k;
		this.servperrack=servperrack;
		this.servers=racks*servperrack;
		
		for(int i=0;i<switches;i++) {
			this.switches.add(new Switch(i+servers));
		}
		
		for(int i=0;i<racks;i++) {
			this.racks.add(new Rack(servperrack,i+servers,i));
		}
		
		for(int l=0;l<links;l++) {
			this.links.add(new Link(l, 0, 0, -1));
		}
		
		this.servreg=new int[3][servers*(servers-1)/2];
	}
	
	/** path where the log files are stored **/
	public void setpath(String pth) {
		path=pth;
	}

	/** set log file name */
	public void setfilename(String fnm) {
		filename=fnm;
	}
	
	/** set domain name */
	public void setdomain(String d) {
		domain=d;
	}
	
	/** set domain id */
	public void setid(int a) {
		id=a;
	}
	
	//racks and switches parameters for Fat-Tree topology network

	/** add server intra-rack link */
	public void addserverintlink(int id, int n1, int n2, int type) {
		links.set(cod.coder(n1, n2), new Link(cod.coder(n1, n2), n1, n2, type, true));
	}
	
	/** add server inter-rack link */
	public void addswitchlink(int id, int n1, int n2, int type) {
		links.set(cod.coder(n1, n2), new Link(cod.coder(n1, n2), n1, n2, type, false));
		links.get(cod.coder(n1, n2)).setcapacity(10.0);
	}
	
	/** associate racks and switches */
	public void rack2switch(int rack, int swit) {
		racks.get(rack).ToRswitch=swit+servers;
		switches.get(swit).type="ToR";
	}
	
	/** set pod to rack */
	public void rack2pod(int r, int p) {
		racks.get(r).setpod(p);
	}

	//Routing paths in Fat-Tree topology
	
	public int[] getnodepath(int s1, int s2) {
		return getserverpath(s1, s2);
	}
	
	/** returns the path between two given nodes */
	public int[] getserverpath(int s1, int s2) {
		
		int[] path=null;
		if(s1!=s2) {
			if(s1>=(racks.size()*servperrack) || s2>=(racks.size()*servperrack)) {
				System.out.println("error: invalid server id");
			}else {
				int r1=s1/servperrack;
				int r2=s2/servperrack;
			
				int[] gs=getswitchpath(r1,r2);	
			
				path=new int[getswitchpath(r1,r2).length+2];
				path[0]=s1;
				for(int i=1;i<(path.length-1);i++) {
					path[i]=gs[i-1];
				}

				path[path.length-1]=s2;
			}
		}else {
			path=new int[2];
			path[0]=s1;
			path[1]=s2;
		}
		return path;
	}
	
	/** returns the path between a server and the nearest gateway */
	public int[] getgatewaypath(int n1) {

		int nn1=0,core=0;
			
				int r1=n1/servperrack;
				
				nn1=(racks.get(r1).getpod())*(k/2)+(k*(k/2));
				core=k*k+((racks.get(r1).getpod()))/2/k/4+(nn1/k);
				int[] path={n1, r1+servers, nn1+servers, core+servers};

		return path;
	}
	
	// core path fixed
	/** returns the path between two given switches */
	public int[] getswitchpath(int n1, int n2) {
			
		if(racks.get(n1).gettor()==racks.get(n2).gettor()) {
			int[] a= {n1+servers};
			return a;
		}else if((racks.get(n1).getpod()==racks.get(n2).getpod())){
			
			int[] a= {n1+servers,n1+servers+2*(k/2)*(k/2),n2+servers};
			return a;
			
		}else {	//different pods
			int nn1=0,nn2=0,core=0;
			
			nn1=(racks.get(n1).getpod())*(k/2)+(k*(k/2));
			nn2=(racks.get(n2).getpod())*(k/2)+(k*(k/2));
			core=k*k+((racks.get(n1).getpod())+(racks.get(n2).getpod()))/2/k/4+(nn1/k);
			int[] a= {n1+servers, nn1+servers, core+servers, nn2+servers, n2+servers};
			
			return a;
		}
	}
	
	/** get agent object hosted in the server */
	public Object getagent(){
		return agent;
	}
	
	/** sorts nodes in ascending order based on their available capacity */
	public void nodecapsort() {
		
		Double[] servac=new Double[servers];	//stores servers available capacity
		sortservac=new int[servers];			//stores servers sorted order
		
		for(int d=0;d<servers;d++) {
			servac[d]=getserver(d).getavailablecpu();
		}
		
		int[] temp=new int[servers];
		for(int s=0;s<servers;s++) {
			temp[s]=1;
		}
		
		int min=0;
		
		for(int n1=0;n1<sortservac.length;n1++) {
			for(int n2=0;n2<servers;n2++) {
				if(temp[min]<0) {
					min=n2;
				}
				if(temp[n2]>0) {
					if(servac[n2]<servac[min]) {
					min=n2;
					}
				}
			}
			temp[min]=-1;
			sortservac[n1]=min;
			min=0;
		}
		
		Double[] tt=new Double[servers];
		for(int t1=0;t1<tt.length;t1++) {
			tt[t1]=getserver(sortservac[t1]).getavailablecpu();
		}
	}
	
	/** get single node in sorted list */
	public int getsortednode(int i) {
		return sortservac[i];
	}
	
	/**
	 * server registry
	 * index in edge vector of the paths that interconnect all servers
	 */

	public void serverreg() {
		//[0] server#1, [1] server#2, [2] hop-count of the path connecting #1 and #2
		
		for(int r=0;r<servreg[0].length;r++) {
			servreg[0][r]=r;
		}
		
		for(int l=0;l<servreg[0].length;l++) {
			int[] t1=cod.decoder(l);
			int[] t2=getserverpath(t1[0],t1[1]);
			servreg[0][l]=t1[0];
			servreg[1][l]=t1[1];
			if((t2.length==2 && t2[0]==t2[1])) {
				servreg[2][l]=0;
			}
			servreg[2][l]=(t2.length-2);
			if(servreg[2][l]>maxhop) {
				maxhop=servreg[2][l];				//update maximum hop count
			}	
		}
		servregsort();
	}

	/**
	 * server registry
	 * sort servers registry based on their hop-count on descending order
	 * computationally expensive version
	 */
	public void servregsortold(){
		int[][] servregsort=new int[3][servers*(servers-1)/2];
		int max=0;
		for(int n2=0;n2<servreg[0].length;n2++){
			for(int n3=0;n3<servreg[0].length;n3++){
				if(servreg[2][n3]>=servreg[2][max]){
					max=n3;
				}
			}
			servregsort[0][n2]=servreg[0][max];
			servregsort[1][n2]=servreg[1][max];
			servregsort[2][n2]=servreg[2][max];
			servreg[2][max]=0;
		}
		servreg=servregsort;
	}
	
	/**
	 * server registry
	 * sort servers registry based on their hop-count on descending order
	 * computationally efficient version using Bucket Sort algorithm
	 */
	public void servregsort(){	
		ArrayList<ArrayList<Integer>> reg=new ArrayList<ArrayList<Integer>>();
		
		for(int m=0;m<maxhop;m++) {
			ArrayList<Integer> temp=new ArrayList<Integer>();
			reg.add(temp);
		}
		
		int[][] servregsort=new int[3][servers*(servers-1)/2];

		for(int n2=0;n2<servreg[0].length;n2++){
			reg.get(servreg[2][n2]-1).add(n2);
		}
		
		int r2=0;
		for(int rn=0;rn<reg.size();rn++) {
			for(int rnn=0;rnn<reg.get(rn).size();rnn++) {
				servregsort[0][r2]=servreg[0][reg.get(rn).get(rnn)];
				servregsort[1][r2]=servreg[1][reg.get(rn).get(rnn)];
				servregsort[2][r2]=servreg[2][reg.get(rn).get(rnn)];
				r2++;
			}
			
		}

		int c=0;
		for(int rss=(servreg[0].length-1);rss>(-1);rss--) {
			servreg[0][c]=servregsort[0][rss];
			servreg[1][c]=servregsort[1][rss];
			servreg[2][c]=servregsort[2][rss];
			c++;
		}
	}
	
	/** print network parameters */
	public void printel() {
		int linkcnt=0;
		for(int s=0;s<links.size();s++) {
			if(links.get(s).linktype>(-1)) {
				linkcnt++;
			}
		}
		
		System.out.println("Network Simulation\n"+type+".k:"+k);
		System.out.println("links: "+linkcnt);
		System.out.println("racks: "+racks.size());
		
		int srs=0;
		for(int s=0;s<racks.size();s++) {
			srs+=racks.get(s).getservers();
		}
		
		System.out.println("servers: "+srs+"\n");
	}
	
	/** print network parameters shorter */
	public void printel2() {
		System.out.println("type: "+type+".k:"+k);
		System.out.println("racks: "+racks.size()+"\nservers per rack: "+servperrack);
	}
	
	/** get one server object */
	public Server getserver(int a) {
		return racks.get(a/servperrack).servers.get(a%servperrack);
	}
	
	/**
	 * add random traffic
	 * when random variable is smaller than prob, random traffic is added to nodes and their links
	 */
	public void generatetraffic(Double prob, Double nodetr, Double edgetr) {
		for(int r=0;r<servreg[0].length;r++) {
			if(Math.random()<prob) {
				getserver(servreg[0][r]).addcpuload(getserver(servreg[0][r]).getcpu()*nodetr);
				getserver(servreg[1][r]).addcpuload(getserver(servreg[1][r]).getcpu()*nodetr);
				int[] temp=getserverpath(servreg[0][r],servreg[1][r]);	
					for(int tt=0;tt<(temp.length-1);tt++) {
						if(temp[tt]!=temp[tt+1]) {
							links.get(cod.coder(temp[tt],temp[tt+1]))
							.addload(links.get(cod.coder(temp[tt],temp[tt+1])).getcapacity()*edgetr);
						}
					}
			}
		}
	}
	
	//getters setters
	
	/** store statistics upon a successful embedding */
	
	public void setstorestats(boolean b) {
		storestats=b;
	}
	
	
	/** set VNF lifecycle duration */
	public void setduration(int d) {
		duration=d;
	}
	
	/** Set iteration. */
	public void setiteration(int s) {
		iteration=s;
	}
	
	/** Set rejection */
	public void setrejection(boolean b) {
		reject=b;
	}
	
	/** Get number of racks. */
	public int getracks() {
		return racks.size();
	}
	
	/** Get iteration. */
	public int getiteration() {
		return iteration;
	}
	
	/** get VNF lifecycle duration */
	public int getduration() {
		return duration;
	}
	
	/** get number of servers */
	public int getservers() {
		return servers;
	}
	
	/** get number of links */
	public int getlinks() {
		return links.size();
	}
	
	/** get network type */
	public String gettype() {
		return type;
	}
	
	/** get server per rack */
	public int getservperrack() {
		return servperrack;
	}
	
	/** get rack id */
	public Rack getrack(int r) {
		return racks.get(r);
	}
	
	/** get single record from server registry */
	public int[] getservreg(int ii) {
		int[] r={servreg[0][ii],servreg[1][ii],servreg[2][ii]};
		return r;
	}
	
	/** getr server registry */
	public int[][] getservregistry(){
		return servreg;
	}
	
	/** get number of embedded VNFs */
	public int getnumofembedded() {
		return embeddedSFCs.size();
	}
	
	/** return the available bandwidth of the link connecting given nodes */
	public Double getband(int a, int b) {
		return links.get(cod.coder(a, b)).getavailableband();
	}
	
	/** is request rejected? */
	public boolean isrejected() {
		return reject;
	}
	
	/** get error message */
	public String geterrmess() {
		return errmess;
	}
	
	/** get network controller */
	public Object getcontroller() {
		return controller;
	}
	
	
	public Double getintrarack() {
		return intrarack;
	}
		
	public Double getouterrack() {
		return outerrack;
	}
	
	public Double getremintra() {
		return remintra;
	}
	
	public Double getremouter() {
		return remouter;
	}

	/** Add request in successful counter. 
	 * Use if automatic store of statistics is inactive. */
	public void addsuccessful() {
		successful++;
	}
	
	/** check the validity of embedding of input VNF-graph based on generated mapping */
	public boolean checkembed(VNFgraph vnfgraph, int[] mapping) {

		boolean invalid=false;
		errmess="";
		
		//check for cpu capacity constraints
		
		int[] tempload=new int[servers];
		
		for(int n=0; n<vnfgraph.getnodes();n++) {
			if((vnfgraph.getnodew()[n]+tempload[getserver(mapping[n]).getid()]+
					getserver(mapping[n]).getcpuload()) >(getserver(mapping[n]).getcpu())) {
				invalid=true;
				errmess="CPU constraints. node:"+getserver(mapping[n]).getid();
			}else {
				tempload[getserver(mapping[n]).getid()]+=(vnfgraph.getnodew()[n]);
			}
		}
		
		tempload=null;	
		
		
		//check for bandwidth constraints
		Double[] tempbandw=new Double[links.size()];
		
		for(int i=0;i<tempbandw.length;i++) {
			tempbandw[i]=0.0;
		}

		if(!invalid && vnfgraph.hasspatial()) {
			for(int l=0;l<vnfgraph.getgraph().length;l++) {
				if(vnfgraph.getgraph()[l]>0) {
					int[] t1=cod.decoder(l);
					int[] t2=getserverpath(mapping[t1[0]],mapping[t1[1]]);
				
					for(int tt=0;tt<(t2.length-1);tt++) {
						if(t2[tt]!=t2[tt+1]) {
							
							int tempve=cod.coder(t2[tt],t2[tt+1]);
							tempbandw[tempve]+=(0.0+(vnfgraph.getedgew()[l]/1000.0));
							
							if((links.get(tempve).getload()+tempbandw[tempve]) > 
										links.get(tempve).getcapacity()) {
								invalid=true;
								errmess=errmess+("Bandwidth constraints in "+t2[tt]+"-"+t2[tt+1]
										+" nodes: "+mapping[t1[0]]+"-"+mapping[t1[1]]);
							}	
						}
					}
				}
			}
			
			for(int i=0;i<vnfgraph.getsegbands().length;i++) {
				if(vnfgraph.getsegflows()[i]>0) {
					int[] t2=getgatewaypath(mapping[i]);

					for(int tt=0;tt<(t2.length-1);tt++) {
						if(t2[tt]!=t2[tt+1]) {
							
							int tempve=cod.coder(t2[tt],t2[tt+1]);
							tempbandw[tempve]+=(0.0+(vnfgraph.getsegbands()[i]/1000.0));
								
							if((links.get(tempve).getload()+tempbandw[tempve]) > 
										links.get(tempve).getcapacity()) {
								invalid=true;
								errmess=errmess+("Bandwidth constraints in flow from node "+mapping[i]+
										" to Gateway");
							}	
						}
					}
				}
			}		
		}
		
		
		ArrayList<Integer> intempbandw=new ArrayList<Integer>();
		ArrayList<Double> tempbandw1=new ArrayList<Double>();
		
		if(!invalid && !vnfgraph.hasspatial()) {
			for(int l=0;l<vnfgraph.getgraph().length;l++) {
				if(vnfgraph.getgraph()[l]>0) {
					int[] t1=cod.decoder(l);
					int[] t2=getserverpath(mapping[t1[0]],mapping[t1[1]]);
				
					for(int tt=0;tt<(t2.length-1);tt++) {
						if(t2[tt]!=t2[tt+1]) {
							
							int tempve=cod.coder(t2[tt],t2[tt+1]);
							
							boolean found=false;
							int index=0;
							for(int ii=0;ii<intempbandw.size();ii++) {
								if(intempbandw.get(ii)==tempve) {
									index=ii;
									found=true;
									break;
								}
							}
							
							if(!found) {
								intempbandw.add(tempve);
								tempbandw1.add(0.0);
								index=tempbandw1.size()-1;
							}
								
				tempbandw1.set(index, (tempbandw1.get(index)+(vnfgraph.getedgew()[l]/1000.0)));
							
							if((links.get(tempve).getload()+tempbandw1.get(index)) > 
										links.get(tempve).getcapacity()) {
								invalid=true;
								errmess=errmess+"Bandwidth constraints in "+t2[tt]+"-"+t2[tt+1]
										+" nodes: "+mapping[t1[0]]+"-"+mapping[t1[1]];
							}	
						}
					}
				
				}
			}
		}
		
		intempbandw.clear();
		tempbandw1.clear();
		

		return invalid;
	}
	
	/** embed input VNF-graph based on generated mapping */
	public void embed(VNFgraph vnfgraph, int[] mapping) {

		reject=false;
		lastrequestsize=vnfgraph.getnodes();

		Double vnftr=0.0;	//traffic that the VNF adds to the network
		Double inserver=0.0;
		int hops=0;
		
		//revenue of VNF
		Double revenue=0.0, noderev=0.0, linkrev=0.0;
		Double wn=1.0;		//weight in the sum of node revenue
		Double wl=0.5;		//weight in the sum of link revenue
		Double wc=0.5;		//weight in the sum of link cost

		//check for cpu capacity constraints
		
		int[] tempload=new int[servers];
		
		for(int n=0; n<vnfgraph.getnodes();n++) {
			if((vnfgraph.getnodew()[n]+tempload[getserver(mapping[n]).getid()]+
					getserver(mapping[n]).getcpuload()) >(getserver(mapping[n]).getcpu())) {
				reject=true;
				errmess=errmess+("CPU constraints. node:"+getserver(mapping[n]).getid());
			}else {
				tempload[getserver(mapping[n]).getid()]+=(vnfgraph.getnodew()[n]);
			}
		}
		
		tempload=null;
		
		//check for bandwidth constraints
		Double[] tempbandw=new Double[links.size()];
		
		for(int i=0;i<tempbandw.length;i++) {
			tempbandw[i]=0.0;
		}

		if(!reject) {
			for(int l=0;l<vnfgraph.getgraph().length;l++) {
				if(vnfgraph.getgraph()[l]>0) {
					int[] t1=cod.decoder(l);
					int[] t2=getserverpath(mapping[t1[0]],mapping[t1[1]]);
				
					for(int tt=0;tt<(t2.length-1);tt++) {
						if(t2[tt]!=t2[tt+1]) {
							
							int tempve=cod.coder(t2[tt],t2[tt+1]);
							tempbandw[tempve]+=(0.0+(vnfgraph.getedgew()[l]/1000.0));
							
							if((links.get(tempve).getload()+tempbandw[tempve]) > 
										links.get(tempve).getcapacity()) {
								reject=true;
								errmess=errmess+("Bandwidth constraints in "+t2[tt]+"-"+t2[tt+1]
										+" nodes: "+mapping[t1[0]]+"-"+mapping[t1[1]]);
							}	
						}
					}
				}
			}
			
			if(vnfgraph.hasspatial()) {
				for(int i=0;i<vnfgraph.getsegbands().length;i++) {
					if(vnfgraph.getsegflows()[i]>0) {
						int[] t2=getgatewaypath(mapping[i]);

						for(int tt=0;tt<(t2.length-1);tt++) {
							if(t2[tt]!=t2[tt+1]) {
								
								int tempve=cod.coder(t2[tt],t2[tt+1]);
								tempbandw[tempve]+=(0.0+(vnfgraph.getsegbands()[i]/1000.0));
								
								if((links.get(tempve).getload()+tempbandw[tempve]) > 
											links.get(tempve).getcapacity()) {
									reject=true;
									errmess=errmess+("Bandwidth constraints in flow from node "+mapping[i]+
											" to Gateway");
								}	
							}
						}
					}
				}
			}			
		}
	
		//if constraints are valid then add cpu load
		if(!reject) {
			
			ArrayList<Double[]> embeddedcpu=new ArrayList<Double[]>();//cpu demands of embedded VNFs
			ArrayList<Double[]> embeddedband=new ArrayList<Double[]>();//bandwidth demands of embedded VNFs

			for(int n=0; n<vnfgraph.getnodes();n++) {
				getserver(mapping[n]).addcpuload(0.0+vnfgraph.getnodew()[n]);
				getserver(mapping[n]).addvnf();
				Double[] temp= {mapping[n]+0.0,vnfgraph.getnodew()[n]+0.0};
				embeddedcpu.add(temp);
			}
			
		//and bandwidth
			for(int l=0;l<vnfgraph.getgraph().length;l++) {
				if(vnfgraph.getgraph()[l]>0) {
					int vnfhops=0;Double tvnftr=0.0;
					int[] t1=cod.decoder(l);
					int[] t2=getserverpath(mapping[t1[0]],mapping[t1[1]]);

					for(int tt=0;tt<(t2.length-1);tt++) {
						if(t2[tt]!=t2[tt+1]) {
							links.get(cod.coder(t2[tt],t2[tt+1])).addload(vnfgraph.getedgew()[l]/1000.0);
							tvnftr+=0.0+(vnfgraph.getedgew()[l]/1000.0);//demands of embedded virtual link
							Double[] temp= {cod.coder(t2[tt],t2[tt+1])+0.0,(vnfgraph.getedgew()[l]/1000.0)};
							embeddedband.add(temp);
						}else {
							inserver+=(vnfgraph.getedgew()[l]/1000.0);
						}
					}
					
					vnfhops=(t2.length-2);
					hops+=vnfhops;
					vnftr+=tvnftr*vnfhops;	//demands*hop-count
				}
				
				
				if(vnfgraph.hasspatial()) {
					int vnfhops=0;Double tvnftr=0.0;
					for(int i=0;i<vnfgraph.getsegbands().length;i++) {
						if(vnfgraph.getsegflows()[i]>0) {
							int[] t21=getgatewaypath(mapping[i]);

							for(int tt=0;tt<(t21.length-1);tt++) {
								if(t21[tt]!=t21[tt+1]) {
									
									links.get(cod.coder(t21[tt],t21[tt+1])).addload(vnfgraph.getsegbands()[i]/1000.0);
									tvnftr+=0.0+(vnfgraph.getedgew()[l]/1000.0);//demands of embedded virtual link
									
									Double[] temp= {cod.coder(t21[tt],t21[tt+1])+0.0,(vnfgraph.getsegbands()[i]/1000.0)};
									embeddedband.add(temp);	
								}
							}
							
							vnfhops=(t21.length-2);
							hops+=vnfhops;
							vnftr+=tvnftr*vnfhops;	//demands*hop-count
						}
					}
				}

			}
		
			for(int nr=0;nr<vnfgraph.getnodew().length;nr++) {
				noderev+=vnfgraph.getnodew()[nr];
			}
		
			//compute virtual link revenue
			for(int nl=0;nl<vnfgraph.getedgew().length;nl++) {
				if(vnfgraph.getgraph()[nl]==0) {		//if there is no link
					
				}else if(vnfgraph.getgraph()[nl]==3){	//if link is bidirectional
					linkrev+=2.0*vnfgraph.getedgew()[nl]/1000.0;
				}else {									//if link is single direction
					linkrev+=0.0+(vnfgraph.getedgew()[nl]/1000.0);
				}
			}
			
			//revenue of VNF, it accumulates all the node and link capacity demands
			revenue=(wn*noderev)+(wl*linkrev);
			embeddedSFCs.add(new SFC(embeddedcpu, embeddedband, duration, vnfgraph.getbanddemand(), inserver, iteration));
			reqrevenue=revenue;

			//cost accumulates embedding cost of all nodes and substrate link
			reqcost=(wn*noderev)+(wc*vnftr);
		}else{
			errmess=errmess+("Embedding rejected.");
		}
		
		if(reject) {
			reqrevenue=0.0;
			reqcost=0.0;
		}
		
		if(printSFCstats) {
			vnfstats(revenue, noderev, vnftr, hops);
		}
	}
	
	/** get embedded SFCs */
	public ArrayList<SFC> getembeddedSFCs(){
		return embeddedSFCs;
	}
	
	/** get single embedded SFC */
	public SFC getvnf(int i) {
		return embeddedSFCs.get(i);
	}
	
	/** compute total CPU */
	public void totalcpu() {
		totalcpu=0.0;
		for(int i=0;i<getservers();i++) {
			totalcpu+=getserver(i).getcpu();
		}
	}
	
	/** get total CPU */
	public Double gettotalcpu() {
		return totalcpu;
	}
	
	/** get used CPU */
	public Double getusedcpu() {
		return cpu;
	}
	
	/** get link */
	public Link getlink(int tt) {
		return links.get(tt);
	}
	
	/** get id */
	public int getid() {
		return id;
	}
	
	/** store network statistics after rejecting a request */
	public void storerejectstats() {
		reject=true;
		reqrevenue=0.0;
		reqcost=0.0;
		netstats();
		if(storestats) {
			storestats();
		}
	}
	
	/** store network statistics */
	public void storestats() {
		requests++;
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;

		try {
			try {
				fw = new FileWriter(path+filename,true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.println(results);//Adds an end to the line
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
	
	/** remove embedded SFC for multi-domain infrastructures */
	public void delembeddedbyid(int sfcid) {
		
		int rvnf=-1;
		
		for(int i=0;i<embeddedSFCs.size();i++) {
			if(embeddedSFCs.get(i).getid()==sfcid) {
				rvnf=i;

				int t0=embeddedSFCs.get(rvnf).getcpu().size();
			
				for(int i1=0;i1<t0;i1++) {
					getserver(embeddedSFCs.get(rvnf).getcpu().get(i1)[0].intValue())
						.remcpuload(embeddedSFCs.get(rvnf).getcpu().get(i1)[1]);
					getserver(embeddedSFCs.get(rvnf).getcpu().get(i1)[0].intValue()).remvnf(1);
				}
		
				int t1=embeddedSFCs.get(rvnf).getband().size();
				for(int i2=0;i2<t1;i2++) {
					links.get(embeddedSFCs.get(rvnf).getband().get(i2)[0].intValue())
						.remload(embeddedSFCs.get(rvnf)
								.getband().get(i2)[1]);
				}
		
				embeddedSFCs.remove(rvnf);
			}
		}
	}
	
	
	/** cost of embedding, it accumulates computational load and traffic added to the network */
	public void vnfstats(Double revenue, Double noderev, Double vnftr, int hops) {
		System.out.println("\n");
		System.out.println("VNF revenue:\t\t"+revenue);
		
		Double cost=noderev+vnftr;
		System.out.println("VNF cost:\t\t"+cost);
		System.out.println("VNF hops:\t\t"+hops);
		
	}
	
	/** print network statistics */
	public void shownetstats() {
		printnetstats=true;
		netstats();
		printnetstats=false;
	}
	
	/** get available cpu */
	public double[] getavailablenodecpu() {
		double rcpu=0.0;
		maxnodecpu=0.0;
		
		for(int s1=0;s1<racks.size();s1++) {
			for(int s2=0;s2<servperrack;s2++) {
				rcpu+=racks.get(s1).servers.get(s2).getavailablecpu();
				if(racks.get(s1).servers.get(s2).getavailablecpu() > maxnodecpu) {
					maxnodecpu=racks.get(s1).servers.get(s2).getavailablecpu();
				}
			}
		}

		return new double[]{rcpu, maxnodecpu};
	}
	
	/** get available cpu; considers the servers that can host the minimum demand VNF*/
	public double[] getminavailablenodecpu(double m) {
		double rcpu=0.0;
		maxnodecpu=0.0;
		
		for(int s1=0;s1<racks.size();s1++) {
			for(int s2=0;s2<servperrack;s2++) {
				if(racks.get(s1).servers.get(s2).getavailablecpu()>= m) {
					rcpu+=racks.get(s1).servers.get(s2).getavailablecpu();
				}
				if(racks.get(s1).servers.get(s2).getavailablecpu() >= maxnodecpu) {
					maxnodecpu=racks.get(s1).servers.get(s2).getavailablecpu();
				}
			}
		}

		return new double[]{rcpu, maxnodecpu};
	}
	
	/** get cpu availability, check if a request can be served
	 * if not the function returns the available capacity for the request */
	public double[] getavailablenodecpu(double a, double m) {//System.out.println(a);
		if(a<0) {
			return  getminavailablenodecpu(m);
		}else {
			double av=getminavailablenodecpu(m)[0];
		
			if(av<a) {
				return getminavailablenodecpu(m);
			} else {
				return new double[]{a, maxnodecpu};
			}
		}
	}
	
	/** network statistics link traffic and CPU load */
	public void netstats() {

		band=0.0;
		cpu=0.0;
		Double rcpu=0.0;
		Double rband=0.0; 
		intrarack=0.0;
		outerrack=0.0;
		remintra=0.0;
		remouter=0.0;
		int l=0,ul=0,a=0,v=0;
		Double inserver=0.0;

		//total number of hosted VNFs in all servers
		for(int s1=0;s1<racks.size();s1++) {
			for(int s2=0;s2<servperrack;s2++) {
				Double ta=racks.get(s1).servers.get(s2).getcpuload();
				cpu+=ta;
				rcpu+=racks.get(s1).servers.get(s2).getavailablecpu();
				if(ta>0) {
					a++;
					v+=racks.get(s1).servers.get(s2).getvnfs();
				}
			}
		}
		
		for(int s3=0;s3<links.size();s3++) {
			Double tb=0.0;
			if(links.get(s3).gettype()>(-1)) {
				tb=links.get(s3).getload();
				rband+=links.get(s3).getavailableband();
				band+=tb;
			
				if(tb>0) {
					l++;
				}else {
					ul++;
				}
				if(links.get(s3).isintrarack()) {
					intrarack+=links.get(s3).getload();
					remintra+=links.get(s3).getavailableband();
				}else {
					outerrack+=links.get(s3).getload();
					remouter+=links.get(s3).getavailableband();
				}
			}
		}

		for(int e=0;e<embeddedSFCs.size();e++) {
			inserver+=embeddedSFCs.get(e).getinserver();
		}
		
		String embresult="accepted";
		
		if(reject) {
			embresult="rejected";
		}
		
		Double c2r=0.0;
		
		if(!reject) {
			c2r=reqcost/reqrevenue;
		}

		results=Integer.toString(requests)+";"+					//1.Request serial number
				Integer.toString(v)+";"+						//2.Hosted VNFs
				Integer.toString(getnumofembedded())+";"+		//3.Embedded Service Function Chains
				df.format(band)+";"+							//4.Used bandwidth
				df.format(rband)+";"+							//5.Available bandwidth
				df.format(cpu)+";"+								//6.Used cpu
				Integer.toString(a)+";"+						//7.Used servers; servers that host some VNF
				df.format(rcpu)+";"+							//8.Remaining cpu
				df.format(intrarack)+";"+						//9.Intra-rack traffic
				df.format(outerrack)+";"+						//10.Inter-rack traffic
				df.format((inserver))+";"+						//11.Intra-server virtual traffic
				embresult+";"+									//12.Is last embedded rejected?
				df.format((successful*1.0)/(requests*1.0))+";"+	//13.Acceptance ratio
				df.format(reqrevenue)+";"+						//14.Request revenue
				df.format(reqcost)+";"+							//15.Embedding cost
				df.format(c2r)+";"+								//16.Cost/Revenue ratio
				Integer.toString(l)+";"+						//17.Used physical links; links with traffic
				Integer.toString(lastrequestsize)+";"+			//18.Size of VNF-graph
				df.format(remintra)+";"+						//19.Remaining intra-rack bandwidth
				df.format(remouter)+";"+						//20.Remaining outer-rack bandwidth
				(iteration+1);									//21.Request ID
		
		avcapacity=Double.toString(rcpu/getservers());			//network available capacity
		
		String dname="";
		if(domain.length()>0) {
			dname="for domain "+domain;
		}
		
		if(printnetstats) {
			System.out.println("\nNetwork statistics "+dname+"\n"+""
				+"Hosted VNFs:\t\t"+v+"\n"
				+"Embedded VNF-chains:\t"+getnumofembedded()+"\n"
				+"Used bandwidth:\t\t"+df.format(band)+" Gbps in "+l+" links\n"
				+"Remaining bandwidth:\t"+df.format(rband)+" Gbps "+ul+" un. links\n"
				+"Rem. intra-rack band.:\t"+df.format(remintra)+" Gbps\n"
				+"Rem. outer-rack band.:\t"+df.format(remouter)+" Gbps\n"
				+"Intra-rack traffic:\t"+df.format(intrarack)+" Gbps\n"
				+"Outer-rack traffic:\t"+df.format(outerrack)+" Gbps\n"
				+"Intra-server traffic:\t"+df.format(inserver)+" Gbps\n"
				+"Used cpu:\t\t"+df.format(cpu)+" GHz"+" in "+a+" servers\n"
				+"Remaining cpu:\t\t"+df.format(rcpu)+" GHz\n"
				+"acceptance ratio:\t"
				+df.format((successful*1.0)/(requests*1.0)));
		}
	}

	/** get available capacity */
	public String getavcapacity() {
		return avcapacity;
	}
	
	/** get request revenue */
	public double getreqrevenue() {
		return reqrevenue;
	}
	
	/** get request cost */
	public double getreqcost() {
		return reqcost;
	}

			
	/** print links */
	public void printlinks() {
		double[] pr=new double[links.size()];
		for(int p=0;p<links.size();p++) {
			pr[p]=links.get(p).gettype();
		}
			System.out.println(java.util.Arrays.toString(pr));
	}
	
	/** network statistics link traffic and CPU load */
	public void vnfstats() {
		int a=0;
		
		for(int vs=0;vs<embeddedSFCs.size();vs++) {
			a+=embeddedSFCs.get(vs).getcpu().size();
		}

		for(int vs0=0;vs0<embeddedSFCs.size();vs0++) {
			for(int vs1=0;vs1<embeddedSFCs.get(vs0).getcpu().size();vs1++) {
				cpu+=embeddedSFCs.get(vs0).getcpu().get(vs1)[1];
			}
			
			for(int vs2=0;vs2<embeddedSFCs.get(vs0).getband().size();vs2++) {
				band+=embeddedSFCs.get(vs0).getband().get(vs2)[1];;
			}
		}
		
		System.out.println("\nVNF statistics\n"+""
				+"Hosted VNFs:\t\t"+a+"\n"
				+"Embedded VNF-chains:\t"+embeddedSFCs.size()+"\n"
				+"Used bandwidth:\t\t"+band+" Gbps"+"\n"
				+"Used cpu:\t\t"+cpu+" GHz");
	}
}




