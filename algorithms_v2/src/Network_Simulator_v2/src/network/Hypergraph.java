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

/** Multi-domain Hypergraph representation class */
public class Hypergraph {

	/** domains */
	FTnetwork[] nets;
	/** type of domain networks */
	String type;
	/** domain name */
	String domain="";
	/** parameter k */
	int k;
	/** servers per domain*/
	int servperdomain;
	/** number of domains */
	int domains;
	/** domain registry */
	public int[][] domreg;
	/** servers sorted by available capacity */
	int[] sortservac;
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
	/** Store statistics in log a file. */
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
	/** error message string */
	String errmess="";										
	/** embedding vnf revenue */
	Double reqrevenue=0.0;										
	/** embedding cost parameters */
	Double reqcost=0.0;
	/** cost / reveneu ratio */
	Double c2r=0.0;
	/** Simulation iteration, request id */
	int iteration=0;
	/** network controller object; create a controller object and cast it here */
	public Object HPcontroller;
	
	public Hypergraph() {
		
	}
	
	/** constructor for building multi-DC infrastructures with Fat-Tree topology DCs */
	public Hypergraph(String type, FTnetwork[] nets, int[] evi, double interlinkcap, int links) {
		this.type=type;
		this.nets=nets;
		this.domains=nets.length;
		this.type=type;
		for(int l=0; l<links;l++) {
			this.links.add(new Link(l, 0, 0, -1));
		}
		
		for(int e=0;e<evi.length;e++) {
			 int[] temp=cod.decoder(evi[e]);
			 this.links.set(evi[e], new Link(evi[e], temp[0], temp[1], 0));
			 this.links.get(evi[e]).setcapacity(interlinkcap);
		 }
		
		this.domreg=new int[3][nets.length*(nets.length-1)/2];
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
	
	public int gethopcount(int a, int b) {
		int hops=0;
			for(int d=0; d<domreg[0].length;d++) {
				if((domreg[0][d]==a && domreg[1][d]==b) || (domreg[1][d]==a && domreg[0][d]==b)) {
					hops=domreg[2][d];
					break;
				}
			}
		
		return hops;
	}
	
	//produces the shortest path between two DCs
	public int[] domainpath(int s1, int s2) {
		int[] path={-1};

		// stores all routes computed during search for shortest path
		ArrayList<ArrayList<Integer>> routes=new ArrayList<ArrayList<Integer>>();
		
		if(s1!=s2) { //if starting node and destination are not the same
			// add starting node in the route
			routes.add(new ArrayList<Integer>());
			routes.get(0).add(s1);
			boolean found=false;
		
			// if destination is not found, for every formed route check all the network links
			// for every network link that is adjacent to a route generate a new route which includes this link
			// until the desired path is formed
			for(int r=0;r<routes.size() && !found;r++) {
				for(int l=0;l<links.size() && !found;l++) {
					int[] cons=links.get(l).getconnected();
					if(cons[0]!=cons[1]) {
						if(routes.get(r).get(routes.get(r).size()-1)==cons[1]
								&& !member(routes.get(r), cons[0])) {
							ArrayList<Integer> templist=new ArrayList<Integer>(routes.get(r));

							templist.add(cons[0]);
							routes.add(templist);

							if((cons[0])==s2) {
								found=true;
							}
						}
					
						if(routes.get(r).get(routes.get(r).size()-1)==cons[0]
								&& !member(routes.get(r), cons[1])) {				
							ArrayList<Integer> templist=new ArrayList<Integer>(routes.get(r));
					
							templist.add(cons[1]);
							routes.add(templist);
			
							if((cons[1])==s2) {
								found=true;
							}
						}
					}
				}
			}
		
			for(int rs=0;rs<routes.size();rs++) {
				if(routes.get(rs).get(routes.get(rs).size()-1)==s2) {
					path=new int[routes.get(rs).size()];
					for(int i=0;i<routes.get(rs).size();i++) {
						path[i]=routes.get(rs).get(i);
					}

					break;
				}
			}
		
		}else {
			// if starting node and destination are the same just add them to the path
			path=new int[2];
			path[0]=s1;
			path[1]=s2;
		}

		return path;
	}
		
		/** check membership in a Arraylist */
	public boolean member(ArrayList<Integer> list, int a) {
		boolean res=false;
		
		for(int ls=0;ls<list.size();ls++) {
			if(list.get(ls)==a) {
				res=true;
			}
		}
		return res;
	}
	
	/** sorts nodes in ascending order based on their available capacity */
	public void nodecapsort() {
		
		Double[] nodeac=new Double[getdomains()];	//stores servers available capacity
		sortservac=new int[getdomains()];			//stores servers sorted order
		
		for(int d=0;d<getdomains();d++) {
			nodeac[d]=getdomain(d).getavailablenodecpu()[0];
		}
		
		int[] temp=new int[getdomains()];
		for(int s=0;s<getdomains();s++) {
			temp[s]=1;
		}
		
		int min=0;
		
		for(int n1=0;n1<sortservac.length;n1++) {
			for(int n2=0;n2<getdomains();n2++) {
				if(temp[min]<0) {
					min=n2;
				}
				if(temp[n2]>0) {
					if(nodeac[n2]<nodeac[min]) {
					min=n2;
					}
				}
			}
			temp[min]=-1;
			sortservac[n1]=min;
			min=0;
		}
		
		Double[] tt=new Double[getdomains()];
		for(int t1=0;t1<tt.length;t1++) {
			tt[t1]=getdomain(sortservac[t1]).getavailablenodecpu()[0];
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

	public void domainreg() {
		//[0] server#1, [1] server#2, [2] hop-count of the path connecting #1 and #2
		
		for(int r=0;r<domreg[0].length;r++) {
			domreg[0][r]=r;
		}
		
		for(int l=0;l<domreg[0].length;l++) {
			int[] t1=cod.decoder(l);
			int[] t2=domainpath(t1[0],t1[1]);
			domreg[0][l]=t1[0];
			domreg[1][l]=t1[1];
			
			if((t2.length==2 && t2[0]==t2[1])) {
				domreg[2][l]=0;
			}
			
			domreg[2][l]=(t2.length-2);
			
			if(domreg[2][l]>maxhop) {
				maxhop=domreg[2][l];				//update maximum hop count
			}	
		}
		domregsort();
	}
	
	/** server registry
	 * sort servers registry based on their hop-count on descending order
	 * computationally efficient version using Bucket Sort algorithm
	 */
	public void domregsort(){	
		ArrayList<ArrayList<Integer>> reg=new ArrayList<ArrayList<Integer>>();
		
		for(int m=0;m<(maxhop+1);m++) {
			ArrayList<Integer> temp=new ArrayList<Integer>();
			reg.add(temp);
		}

		int[][] servregsort=new int[3][getdomains()*(getdomains()-1)/2];

		for(int n2=0;n2<domreg[0].length;n2++){
			reg.get(domreg[2][n2]).add(n2);
		}
		
		int r2=0;
		for(int rn=0;rn<reg.size();rn++) {
			for(int rnn=0;rnn<reg.get(rn).size();rnn++) {
				servregsort[0][r2]=domreg[0][reg.get(rn).get(rnn)];
				servregsort[1][r2]=domreg[1][reg.get(rn).get(rnn)];
				servregsort[2][r2]=domreg[2][reg.get(rn).get(rnn)];
				r2++;
			}
			
		}

		int c=0;
		for(int rss=(domreg[0].length-1);rss>(-1);rss--) {
			domreg[0][c]=servregsort[0][rss];
			domreg[1][c]=servregsort[1][rss];
			domreg[2][c]=servregsort[2][rss];
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
		System.out.println("domains: "+nets+"\n");
	}
	
	/** print network parameters shorter */
	public void printel2() {
		String dname="";
		if(domain.length()>0) {
			dname="Domain "+domain;
		}
		System.out.println(dname+"\n"+type+".k:"+k);
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
		
	/** Get iteration. */
	public int getiteration() {
		return iteration;
	}
	
	/** get VNF lifecycle duration */
	public int getduration() {
		return duration;
	}
	
	/** get number of servers */
	public int getdomains() {
		return domains;
	}
	
	/** get number of links */
	public int getlinks() {
		return links.size();
	}
	
	/** get network type */
	public String gettype() {
		return type;
	}
	
	public ArrayList<Link> getnetlinks() {
		return links;
	}
	
	/** get server per domain */
	public int getservperdomain() {
		return servperdomain;
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
		return HPcontroller;
	}
	
	/** get FT domain */
	public FTnetwork getdomain(int a) {
		return nets[a];
	}
	
	/** get domain path */
	public int[] getdomainpath(int a, int b) {
		return domainpath(a,b);
	}
	
	/** get single record from server registry */
	public int[] getdomreg(int ii) {
		int[] r={domreg[0][ii],domreg[1][ii],domreg[2][ii]};
		return r;
	}
	
	/** getr server registry */
	public int[][] getdomregistry(){
		return domreg;
	}
	
	/** Add request in successful counter. 
	 * Use if automatic store of statistics is inactive. */
	public void addsuccessful() {
		successful++;
	}
	
	/** check the validity of embedding of input VNF-graph based on generated mapping */
	public boolean checkembed(VNFgraph vnfgraph, int[] mapping) {
		
		//check for cpu capacity constraints
		
		boolean invalid=false;
		errmess="";
		int[] tempload=new int[domains];
		
		for(int n=0; n<vnfgraph.getnodes();n++) {
			double tvl=vnfgraph.getnodew()[n]+tempload[nets[mapping[n]].getid()];
			
			if(tvl >(nets[mapping[n]].getavailablenodecpu()[0])) {
				invalid=true;
				errmess=errmess+("CPU constraints. node:"+nets[mapping[n]].getid());
			}else {
				tempload[nets[mapping[n]].getid()]+=(vnfgraph.getnodew()[n]);
			}
		}
		
		//check for bandwidth constraints
		Double[] tempbandw=new Double[links.size()];
		
		for(int i=0;i<tempbandw.length;i++) {
			tempbandw[i]=0.0;
		}

		if(!invalid) {
			for(int l=0;l<vnfgraph.getgraph().length;l++) {
				if(vnfgraph.getgraph()[l]>0) {
					int[] t1=cod.decoder(l);
					int[] t2=domainpath(mapping[t1[0]],mapping[t1[1]]);
				
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
		}

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
		
		int[] tempload=new int[domains];
		
		
		for(int n=0; n<vnfgraph.getnodes();n++) {
			double tvl=vnfgraph.getnodew()[n]+tempload[nets[mapping[n]].getid()];
			if(tvl >(nets[mapping[n]].getavailablenodecpu()[0])) {
				reject=true;
				errmess=errmess+("CPU constraints. node:"+nets[mapping[n]].getid());
			}else {
				tempload[nets[mapping[n]].getid()]+=(vnfgraph.getnodew()[n]);
			}
		}
		
		//check for bandwidth constraints
		Double[] tempbandw=new Double[links.size()];
		
		for(int i=0;i<tempbandw.length;i++) {
			tempbandw[i]=0.0;
		}

		if(!reject) {
			for(int l=0;l<vnfgraph.getgraph().length;l++) {
				if(vnfgraph.getgraph()[l]>0) {
					int[] t1=cod.decoder(l);
					int[] t2=domainpath(mapping[t1[0]],mapping[t1[1]]);
				
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
		}
	
		//if constraints met then add cpu load
		if(!reject) {
			
			ArrayList<Double[]> embeddedcpu=new ArrayList<Double[]>();//cpu demands of embedded VNFs
			ArrayList<Double[]> embeddedband=new ArrayList<Double[]>();//bandwidth demands of embedded VNFs
	
		//and bandwidth
			for(int l=0;l<vnfgraph.getgraph().length;l++) {
				if(vnfgraph.getgraph()[l]>0) {
					int vnfhops=0;Double tvnftr=0.0;
					int[] t1=cod.decoder(l);
					int[] t2=domainpath(mapping[t1[0]],mapping[t1[1]]);

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
		for(int i=0;i<getdomains();i++) {
			totalcpu+=nets[i].gettotalcpu();
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
		
		for(int i0=0;i0<embeddedSFCs.size();i0++) {
			if(embeddedSFCs.get(i0).getid()==sfcid) {
				rvnf=sfcid;
				if(rvnf>(-1)) {
					for(int n=0;n<nets.length;n++) {
						nets[n].delembeddedbyid(sfcid);
					}
		
					for(int i1=(embeddedSFCs.size()-1);i1>=0;i1--) {
						if(embeddedSFCs.get(i1).getid()==sfcid) {
							Double[] t1=new Double[embeddedSFCs.get(i1).getband().size()];
								for(int i=0;i<t1.length;i++) {
									links.get(embeddedSFCs.get(i1).getband().get(i)[0].intValue())
										.remload(embeddedSFCs.get(i1).getband().get(i)[1]);
								}
								embeddedSFCs.remove(i1);
						}
					}
				}
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
	
	/** network statistics link traffic and CPU load */
	public void netstats() {

		band=0.0;
		cpu=0.0;
		Double rcpu=0.0;
		Double rband=0.0, intrarack=0.0, outerrack=0.0, remintra=0.0, remouter=0.0;
		int l=0,ul=0,a=0,v=0;

		//statistics for the whole infrastructure
		for(int ns=0;ns<nets.length;ns++) {
			nets[ns].netstats();
			int racks=nets[ns].getracks();
			for(int s1=0;s1<racks;s1++) {
				for(int s2=0;s2<nets[ns].getservperrack();s2++) {
					Double ta=nets[ns].racks.get(s1).servers.get(s2).getcpuload();
					cpu+=ta;
					rcpu+=nets[ns].racks.get(s1).servers.get(s2).getavailablecpu();
					if(ta>0) {
						a++;
						v+=nets[ns].racks.get(s1).servers.get(s2).getvnfs();
					}
				}
			}
		
			intrarack+=nets[ns].getintrarack();
			remintra+=nets[ns].getremintra();
			outerrack+=nets[ns].getouterrack();
			remouter+=nets[ns].getremouter();
			
			reqrevenue+=nets[ns].getreqrevenue();
			reqcost+=nets[ns].getreqcost();
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
			}
		}

		String embresult="accepted";
		
		if(reject) {
			embresult="rejected";
		}
		
		c2r=0.0;
		
		if(!reject) {
			if(reqrevenue>0) {
				c2r=reqcost/reqrevenue;
			}else {
				c2r=0.0;
			}
		}

		results=Integer.toString(requests)+";"+					//1.Request serial number
				Integer.toString(v)+";"+						//2.Hosted VNFs
				df.format(band)+";"+							//3.Used bandwidth
				df.format(rband)+";"+							//4.Available bandwidth
				df.format(cpu)+";"+								//5.Used cpu
				Integer.toString(a)+";"+						//6.Used servers; servers that host some VNF
				df.format(rcpu)+";"+							//7.Remaining cpu
				df.format(intrarack)+";"+						//8.Intra-rack traffic
				df.format(outerrack)+";"+						//9.Inter-rack traffic
				embresult+";"+									//10.Is last embedded rejected?
				df.format((successful*1.0)/(requests*1.0))+";"+	//11.Acceptance ratio
				df.format(reqrevenue)+";"+						//12.Request revenue
				df.format(reqcost)+";"+							//13.Embedding cost
				df.format(c2r)+";"+								//14.Cost/Revenue ratio
				df.format(remintra)+";"+						//15.Remaining intra-rack bandwidth
				df.format(remouter)+";"+						//16.Remaining inter-rack bandwidth
				(iteration+1);									//17.Request ID
		
		avcapacity=Double.toString(rcpu/getdomains());			//network available capacity
		
		String dname="";
		if(domain.length()>0) {
			dname="for domain "+domain;
		}
		
		if(printnetstats) {
			System.out.println("\nNetwork statistics "+dname+"\n"+""
				+"Hosted VNFs:\t\t"+v+"\n"
				+"Embedded VNF-chains:\t"+embeddedSFCs.size()+"\n"
				+"Used bandwidth:\t\t"+df.format(band)+" Gbps in "+l+" links\n"
				+"Remaining bandwidth:\t"+df.format(rband)+" Gbps "+ul+" un. links\n"
				+"Rem. intra-rack band.:\t"+df.format(remintra)+" Gbps\n"
				+"Rem. outer-rack band.:\t"+df.format(remouter)+" Gbps\n"
				+"Intra-rack traffic:\t"+df.format(intrarack)+" Gbps\n"
				+"Outer-rack traffic:\t"+df.format(outerrack)+" Gbps\n"
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
	
	/** print links */
	public void printlinks() {
		double[] pr=new double[links.size()];
		for(int p=0;p<links.size();p++) {
			pr[p]=links.get(p).getavailableband();
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




