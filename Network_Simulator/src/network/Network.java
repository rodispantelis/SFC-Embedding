package network;

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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Network {
	
	String type;											//network type
	int k, servperrack,servers;								//parameter k; servers per rack; all servers
	int[][] servreg;										//server registry
	int[] sortservac;										//servers sorted by available capacity
	public ArrayList<Rack> racks=new ArrayList<Rack>();		//racks
	ArrayList<Switch> switches=new ArrayList<Switch>();		//switches
	public ArrayList<Link> links=new ArrayList<Link>();		//links
	ArrayList<SFC> embeddedSFCs=new ArrayList<SFC>();		//embedded VNFs
	int duration=0;											//VNF lifecycle duration
	Codec cod=new Codec();									//Edge Vector coder-decoder
	int embchains=0;										//embedded chains counter
	int maxhop=0;											//max and min hop count between servers
	boolean printSFCstats=false;							//print on screen embedded SFC statistics
	boolean printnetstats=false;							//print on screen network statistics
	boolean reject=false;									//last embedding outcome
	int requests=0;											//number of requests
	
	Double band=0.0, cpu=0.0, totalcpu=0.0;					//statistics
	String results="", avcapacity="";
	DecimalFormat df = new DecimalFormat("#0.000");
	String path="";											//path to store log file
	String filename="simulationresult.csv";					//log file name
	int lastrequestsize=0;
	String errmess="";										//error message string
	
	public Network() {
		
	}
	
	public Network(String type, int k, int servperrack, int switches, int racks, int links) {
		//constructor for building Fat-Tree topology network
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
	
	public void setpath(String pth) {
		//path where the log files are stored
		path=pth;
	}

	public void setfilename(String fnm) {
		//set log file name
		filename=fnm;
	}
	
	//racks and switches parameters for Fat-Tree topology network
	
	public void addserverlink(int id, int n1, int n2, int type) {
		links.set(cod.coder(n1, n2), new Link(cod.coder(n1, n2), n1, n2, type, true));
	}
	
	public void addlink(int id, int n1, int n2, int type) {
		links.set(cod.coder(n1+servers, n2+servers), 
				new Link(cod.coder(n1+servers, n2+servers), n1+servers, n2+servers, type));
	}
	
	public void rack2switch(int rack, int swit) {
		racks.get(rack).ToRswitch=swit+servers;
		switches.get(swit).type="ToR";
	}
	
	public void rack2pod(int r, int p) {
		racks.get(r).setpod(p);
	}
	
	//set switch parameters
	
	public void addswitch(int id) {
		switches.add(new Switch(id+servers));
	}
	
	public void switch2switchedge(int switfrom, int switto, String stype) {
		switches.get(switfrom).connetto(switto+servers);
		switches.get(switto).settype(stype);
	}
	
	public void switch2switch(int switfrom, int switto, String stype) {
		switches.get(switfrom).connetto(switto+servers);
		switches.get(switto).settype(stype);
	}
	
	public void switch2switch(int switfrom, int switto) {
		switches.get(switfrom).connetto(switto+servers);
	}
	
	//Routing paths
	
	public int[] getserverpath(int s1, int s2) {
		//returns the path between two given nodes
		
		int[] path=null;
		if(s1>=(racks.size()*servperrack) || s2>=(racks.size()*servperrack)) {
			System.out.println("error: invalid server id");
		}else {
			int r1=s1/servperrack;
			int r2=s2/servperrack;
			
			int[] gs=getswitchpath(r1,r2);
			
			if(r1!=r2) {
				path=new int[getswitchpath(r1,r2).length+2];
				path[0]=s1;
				for(int i=1;i<(path.length-1);i++) {
					path[i]=gs[i-1];
				}
			}else {
				path=new int[2];
				path[0]=s1;
			}
			path[path.length-1]=s2;
		}
		return path;
	}
	
	public int[] getswitchpath(int n1, int n2) {
		//returns the path between two given switches
		
		if(racks.get(n1).gettor()==racks.get(n2).gettor()) {
			int[] a= {n1+servers,n2+servers};
			return a;
		}else if((racks.get(n1).getpod()==racks.get(n2).getpod())){
			int nn1=0,nn2=0;
			if((n1%(k/2))>(k/2)) {
				nn1=1;
			}
			if((n2%(k/2))>(k/2)) {
				nn2=1;
			}
			nn1+=racks.size()+(n1/(k/2));
			nn2+=racks.size()+(n2/(k/2));
			if(nn1==nn2) {
				int[] a= {n1+servers,nn1+servers,n2+servers};
				return a;
			}else {
				int[] a= {n1+servers,nn1+servers,(nn1+k*(k/2)+servers),nn2+servers,n2+servers};
				return a;
			}
		}else {	//different pods
			int nn1=0,nn2=0,nnn1=0,nnn2=0,core=0;
			if((n1%(k/2))>(k/2)) {
				nn1=1;
			}
			if((n2%(k/2))>(k/2)) {
				nn2=1;
			}
			
			nn1+=racks.size()+(n1/(k/2));
			nn2+=racks.size()+(n2/(k/2));
			
			if((nn1%(k/2))>0) {
				nnn1-=nn1%(k/2);
			}
			
			if((nn2%(k/2))>0) {
				nnn2-=nn2%(k/2);
			}
			nnn1+=racks.size()+(n1/(k/2))+k*(k/2);
			nnn2+=racks.size()+(n2/(k/2))+k*(k/2);
			
			core=racks.size()+2*k*(k/2)+nn1%(k/2);		
			
			int[] a= {n1+servers,nn1+servers,nnn1+servers,core+servers,nnn2+servers,nn2+servers,n2+servers};
			return a;
		}
	}
	
	public void nodecapsort() {
		//sorts nodes in ascending order based on their available capacity
		
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
	
	public int getsortednode(int i) {
		return sortservac[i];
	}
	
	public void serverreg() {
		//server registry
		//index in edge vector of the paths that interconnect all servers
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
			servreg[2][l]=(t2.length-1);
			if(servreg[2][l]>maxhop) {
				maxhop=servreg[2][l];				//update maximum hop count
			}	
		}
		servregsort();
	}
	
	public void servregsortold(){	//computationally expensive version
		//sort servers registry based on their hop-count on descending order
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
	
	public void servregsort(){	//computationally efficient version
		//using Bucket Sort algorithm in order to
		//sort servers registry based on their hop-count on descending order
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
	
	//print network parameters
	public void printel() {
		int linkcnt=0;
		for(int s=0;s<links.size();s++) {
			if(links.get(s).linktype>(-1)) {
				linkcnt++;
			}
		}
		
		System.out.println("Network Simulation\n"+type+".k:"+k);
		System.out.println("switches: "+switches.size());
		System.out.println("links: "+linkcnt);
		System.out.println("racks: "+racks.size());
		
		int srs=0;
		for(int s=0;s<racks.size();s++) {
			srs+=racks.get(s).getservers();
		}
		
		System.out.println("servers: "+srs+"\n");
	}
	
	public void printel2() {		
		System.out.println("Network Simulation\n"+type+".k:"+k);
		System.out.println("racks:"+racks.size()+"\nservers per rack:"+servperrack+"\n");
	}
	
	public Server getserver(int a) {
		//get one server from one rack
		return racks.get(a/servperrack).servers.get(a%servperrack);
	}
	
	public void generatetraffic(Double prob, Double nodetr, Double edgetr) {
		//add random traffic
		//when random variable is smaller than prob, random traffic is added to nodes and their links

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
	public void setduration(int d) {
		duration=d;
	}
	
	public int getduration() {
		return duration;
	}
	
	public int getservers() {
		return servers;
	}
	
	public int getlinks() {
		return links.size();
	}
	
	public String gettype() {
		return type;
	}
	
	public int getservperrack() {
		return servperrack;
	}
	
	public Rack getrack(int r) {
		return racks.get(r);
	}
	
	public int[] getservreg(int ii) {
		int[] r={servreg[0][ii],servreg[1][ii],servreg[2][ii]};
		return r;
	}
	
	public int[][] getservregistry(){
		return servreg;
	}
	
	public int getnumofembedded() {
		return embeddedSFCs.size();
	}
	
	public Double getband(int a, int b) {
		//return the available bandwidth of the link connecting nodes a and b
		return links.get(cod.coder(a, b)).getavailableband();
	}
	
	public boolean isrejected() {
		return reject;
	}
	
	public String geterrmess() {
		return errmess;
	}
	
	public boolean checkembed(VNFgraph vnfgraph, int[] mapping) {///////////////
		//embedding of input VNF-graph based on generated mapping
		boolean invalid=false;

		lastrequestsize=vnfgraph.getnodes();

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
		
		ArrayList<Integer> intempbandw=new ArrayList<Integer>();
		ArrayList<Double> tempbandw=new ArrayList<Double>();
		
		if(!invalid) {
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
								tempbandw.add(0.0);
								index=tempbandw.size()-1;
							}
								
				tempbandw.set(index, (tempbandw.get(index)+(vnfgraph.getedgew()[l]/1000.0)));
							
							if((links.get(tempve).getload()+tempbandw.get(index)) > 
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
		tempbandw.clear();
		
		return invalid;
	}
	
	
	public void embed(VNFgraph vnfgraph, int[] mapping) {
		//embedding of input VNF-graph based on generated mapping
		reject=false;
		requests++;
		lastrequestsize=vnfgraph.getnodes();

		Double vnftr=0.0;	//traffic that the VNF adds to the network
		Double inserver=0.0;
		int hops=0;
		
		//revenue of VNF
		Double revenue=0.0, noderev=0.0, linkrev=0.0;
		Double wn=1.0;		//weight in the sum of node revenue
		Double wl=1.0;		//weight in the sum of link revenue

		//check for cpu capacity constraints
		
		int[] tempload=new int[servers];
		
		for(int n=0; n<vnfgraph.getnodes();n++) {
			if((vnfgraph.getnodew()[n]+tempload[getserver(mapping[n]).getid()]+
					getserver(mapping[n]).getcpuload()) >(getserver(mapping[n]).getcpu())) {
				reject=true;
				System.out.println("CPU constraints. node:"+getserver(mapping[n]).getid());
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
								System.out.println("Bandwidth constraints in "+t2[tt]+"-"+t2[tt+1]
										+" nodes: "+mapping[t1[0]]+"-"+mapping[t1[1]]);
							}	
						}
					}
				
				}
			}
		}
	
		//if constraints met then add cpu load
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
					int vnfhops=0;
					int[] t1=cod.decoder(l);
					int[] t2=getserverpath(mapping[t1[0]],mapping[t1[1]]);
				
					for(int tt=0;tt<(t2.length-1);tt++) {
						if(t2[tt]!=t2[tt+1]) {
							links.get(cod.coder(t2[tt],t2[tt+1])).addload(vnfgraph.getedgew()[l]/1000.0);
							vnftr+=0.0+vnfgraph.getedgew()[l];
							Double[] temp= {cod.coder(t2[tt],t2[tt+1])+0.0,(vnfgraph.getedgew()[l]/1000.0)};
							embeddedband.add(temp);
						}else {
							inserver+=(vnfgraph.getedgew()[l]/1000.0);
						}
					}
					if((t2.length==2 && t2[0]==t2[1])) {
						vnfhops=0;
					}
					else {
						vnfhops=(t2.length-1);
					}
					hops+=vnfhops;
				}
			}
		
			for(int nr=0;nr<vnfgraph.getnodew().length;nr++) {
				noderev+=vnfgraph.getnodew()[nr];
			}
		
			for(int nl=0;nl<vnfgraph.getedgew().length;nl++) {
				linkrev+=vnfgraph.getedgew()[nl]/1000.0;
			}
		
			//sum of embedded VNF-chains
			embchains++;
			
			//revenue of VNF, it accumulates all the node and link capacity demands
			revenue=(wn*noderev)+(wl*linkrev);
			embeddedSFCs.add(new SFC(embeddedcpu, embeddedband, duration, vnfgraph.banddemand, inserver));
		}else{
			System.out.println("Embedding rejected.");
		}
		
		if(printSFCstats) {
			vnfstats(revenue, noderev, vnftr, hops);
		}
		netstats();
		storestats();
	}
	
	public ArrayList<SFC> getembeddedSFCs(){
		return embeddedSFCs;
	}
	
	public SFC getvnf(int i) {
		return embeddedSFCs.get(i);
	}
	
	public void totalcpu() {
		for(int i=0;i<getservers();i++) {
			totalcpu+=getserver(i).getcpu();
		}
	}
	
	public Double gettotalcpu() {
		return totalcpu;
	}
	
	public Double getusedcpu() {
		return cpu;
	}
	
	public void storerejectstats() {
		requests++;
		reject=true;
		netstats();
		storestats();
	}
	
	public void storestats() {

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
	
	public void delembedded(int rvnf) {
		Double[] t0=new Double[embeddedSFCs.get(rvnf).getcpu().size()];
		for(int i=0;i<t0.length;i++) {
			getserver(embeddedSFCs.get(rvnf).getcpu().get(i)[0].intValue())
				.remcpuload(embeddedSFCs.get(rvnf).getcpu().get(i)[1]);
			getserver(embeddedSFCs.get(rvnf).getcpu().get(i)[0].intValue()).remvnf(1);
		}
		
		Double[] t1=new Double[embeddedSFCs.get(rvnf).getband().size()];
		for(int i=0;i<t1.length;i++) {
			links.get(embeddedSFCs.get(rvnf).getband().get(i)[0].intValue())
				.remload(embeddedSFCs.get(rvnf).getband().get(i)[1]);
		}
		
		embeddedSFCs.remove(rvnf);
		embchains--;
	}
	
	public void vnfstats(Double revenue, Double noderev, Double vnftr, int hops) {
		System.out.println("\n");
		System.out.println("VNF revenue:\t\t"+revenue);
		
		//cost of embedding, it accumulates computational load and traffic added to the network
		Double cost=noderev+vnftr;
		System.out.println("VNF cost:\t\t"+cost);
		System.out.println("VNF hops:\t\t"+hops);
		
	}
	
	public void shownetstats() {
		printnetstats=true;
		netstats();
		printnetstats=false;
	}
	
	public void netstats() {
		//network statistics
		//link traffic and CPU load
		band=0.0;
		cpu=0.0;
		Double rcpu=0.0, rband=0.0, intrarack=0.0, outerrack=0.0;
		int l=0,a=0,v=0;
		Double inserver=0.0;

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
				Double tb=links.get(s3).getload();
				rband+=links.get(s3).getavailableband();
				band+=tb;
				if(tb>0) {
					l++;
				}
				if(links.get(s3).isintrarack()) {
					intrarack+=links.get(s3).getload();
				}else {
					outerrack+=links.get(s3).getload();
				}
		}

		for(int e=0;e<embeddedSFCs.size();e++) {
			inserver+=embeddedSFCs.get(e).getinserver();
		}
		
		results=Integer.toString(requests)+";"+							//request serial number
				Integer.toString(v)+";"+								//Hosted VNFs
				Integer.toString(embchains)+";"+						//Embedded Service Function Chains
				df.format(band)+";"+									//Used bandwidth
				Integer.toString(l)+";"+								//links number
				df.format(cpu)+";"+										//Used cpu
				Integer.toString(a)+";"+								//used servers
				df.format(rcpu)+";"+									//Remaining cpu
				df.format(intrarack)+";"+								//intrarrack traffic
				df.format(outerrack)+";"+								//outerrack traffic
				df.format((inserver))+";"+								//in-server virtual traffic
				reject;													//is last embedded rejected?
		
		avcapacity=Double.toString(rcpu/getservers());					//network available capacity
		
		if(printnetstats) {
		System.out.println("\nNetwork statistics\n"+""
				+"Hosted VNFs:\t\t"+v+"\n"
				+"Embedded VNF-chains:\t"+embchains+"\n"
				+"Used bandwidth:\t\t"+df.format(band)+" Gbps in "+l+" links\n"
				+"Remaining bandwidth:\t"+rband+" Gbps\n"
				+"Used cpu:\t\t"+df.format(cpu)+" GHz"+" in "+a+" servers\n"
				+"Remaining cpu:\t\t"+df.format(rcpu)+" GHz\n"
				+"Intra-rack traffic:\t"+df.format(intrarack)+" Gbps\n"
				+"Outer-rack traffic:\t"+df.format(outerrack)+" Gbps\n"
				+"In-server virtual traffic:\t"
				+df.format(inserver)+" Gbps\n");
		}
	}

	public String getavcapacity() {
		return avcapacity;
	}
	
	public void vnfstats() {
		//network statistics
		//link traffic and CPU load

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




