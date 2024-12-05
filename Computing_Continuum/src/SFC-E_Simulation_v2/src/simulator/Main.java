package simulator;


import java.io.BufferedWriter;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import network.Codec;
import network.FTnetwork;
import network.Hypergraph;
import network.Make;
import services.VNFgraph;
 
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

/** Simulator. */
public class Main extends Thread{
	/** number of domains */
	int domains=0;
	/** array of networks */
	FTnetwork[] nets;
	/** type of network */
	String type;
	/** multi-domain network represented as a hypergraph */
	Hypergraph hg;
	/** network controller */
	//Controller controller;
	/** Edge Vector coder-decoder */
	Codec codec=new Codec();						
	/** path to log files */
	String path="";
	/** path to VNFgraph files */
	String vnfgraphs;							
	/** servers per rack */
	int servperrack=20;				
	/** k parameter; different usage in different topologies */
	int k=6;
	/** The stakeholders of the hypergraph network */
	ArrayList<services.Stakeholder> stakeholders=new ArrayList<services.Stakeholder>();						
	/** VNF lifecycle duration */
	int duration=2160;			
	/** number of iterations */
	int iterations=6000;
	/** node capacity */
	Double nodecapacity=20.0;
	/** name of simulation log file */
	String filename="simulationresult-distr.csv";
	/** hypergraph in Edge Vector Index format*/
	String hypergraph="";
	/** Broker that partitions a VNF-graph */
	services.Broker broker;
	//services.Broker broker2;
	/** parameters file */
	File parametersfile=new File("parameters");	
	/** specify the VNFgraph file names */
	int r1,r2;
	/** parameter to define number of nodes in the cluster */
	int fnodes;
	/** runtime */
	long totalTime;
	/** algorithm mode */
	Double mode=2.0;		//agent modes;1.0 greedy;2.0 train and run network;
							//3.0 train new network in each request; 4.0 run only stored model
	
	/** Maximum SFC size */
	int maxsfcsize=0;
	/** Parameters for random VNF-graph definition. */
	int[] randomparams=new int[5];
	
	/** is spatial constraints defined */
	String setspatial="true";
	
	/** Embedding in hypernode graph network is rejected. */
	boolean hgisrejected=false;
	/** Embedding in at least one of the domains is rejected. */
	boolean domisrejected=false;
	
	int numofstakeholders=1;
	
	String stakeholdersfile="file";
	
	boolean storestats=true;
	
	boolean deletemodels=true;
	
	/** GA parameters */
	
	/** network traffic classification parameter; used in PAGA */
	int netclasses;
	
	/** GA parameter setup #1 ; populations size; supergenerations; crossover and mutation probabilities;
	  	used in hyper graph embedding*/
	int popsize=10, generations=10, supergens=2, crossprob=10, mutprob=10;
	
	/** GA parameter {@link Setup} #2; used in DC embedding */
	int popsize2=10, generations2=10, supergens2=2, crossprob2=10, mutprob2=10;
	/** GA boolean parameters for running PAGA; delete previous setups; heuristic population generation */
	boolean[] boolparams=new boolean[3];
	
	/** deviation from spatial constraints*/
	int sdeviation=0;
	
	public ArrayList<Integer> hgmapping=new ArrayList<Integer>();	
	
	double interdcbandwidth=0.0;
	
	VNFgraph[] subgraphs;
	VNFgraph[] subgraphs2;
	VNFgraph hyperlinksgraph=null;
	VNFgraph hyperlinksgraph2=null;
	int[] hyperlinksmapping=null;
	
	/** Read parameters file and initialize simulation. */
	
	public static void main(String[] args) {
		Main m=new Main();
		
	    if(m.parametersfile.exists()) {
	    	
	    	//if file exists continue
	    	m.setparameters();
	    }else {
	    	System.out.println("Parameter file is not found. \n"
	    			+ "The simulation will run on default parameters.\n");
	    }
	    
		File datafile=new File("data");	
		if(datafile.exists()) {
	    	datafile.delete();
	    }
		
		m.init();
	}
	

	/** Read simulation parameters from "parameters" file. */
 	public void setparameters() {
		try{
	    	Scanner scanner = new Scanner(parametersfile);	
	    	while(scanner.hasNext()){
	    		String[] params= scanner.nextLine().split(" ");
	    		if(!params[0].split("")[0].equals("%") && !params[0].split("")[0].equals("")) {
		    		if(params[0].equals("type")) {
		    			type=params[1];
		    		}else if(params[0].equals("servperrack")) {
		    			servperrack=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("domains")) {
		    			domains=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("hypergraph")) {
		    			hypergraph=params[1];
		    		}else if(params[0].equals("nodecapacity")) {
		    			nodecapacity=Double.parseDouble(params[1]);
		    		}else if(params[0].equals("k")) {
		    			k=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("duration")) {
		    			duration=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("iterations")) {
		    			iterations=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("path")) {
		    			path=params[1];
		    		}else if(params[0].equals("vnfgraphs")) {
		    			vnfgraphs=params[1];
		    		}else if(params[0].equals("r1")) {
		    			r1=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("r2")) {
		    			r2=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("fnodes")) {
		    			fnodes=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("mode")) {
		    			mode=Double.parseDouble(params[1]);
		    		}else if(params[0].equals("maxsfcsize")) {
		    			maxsfcsize=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("randomparams")) {
		    			for(int x=1;x<=randomparams.length;x++) {
		    				randomparams[x-1]=Integer.parseInt(params[x]);
		    			}
		    		}else if(params[0].equals("popsize")) {
		    			popsize=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("generations")) {
		    			generations=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("supergens")) {
		    			supergens=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("crossprob")) {
		    			crossprob=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("mutprob")) {
		    			mutprob=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("popsize2")) {
		    			popsize2=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("generations2")) {
		    			generations2=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("supergens2")) {
		    			supergens2=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("crossprob2")) {
		    			crossprob2=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("mutprob2")) {
		    			mutprob2=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("paga")) {
		    			boolparams[0]=Boolean.parseBoolean(params[1]);
		    		}else if(params[0].equals("deletedb")) {
		    			boolparams[1]=Boolean.parseBoolean(params[1]);
		    		}else if(params[0].equals("interdcbandwidth")) {
		    			interdcbandwidth=Double.parseDouble(params[1]);
		    		}else if(params[0].equals("deletemodels")) {
		    			deletemodels=Boolean.parseBoolean(params[1]);
		    		}else if(params[0].equals("popgenheuristic")) {
		    			boolparams[2]=Boolean.parseBoolean(params[1]);
		    		}else if(params[0].equals("netclasses")) {
		    			netclasses=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("setspatial")) {
		    			setspatial=params[1];
		    		}else if(params[0].equals("stakeholders")) {
		    			numofstakeholders=Integer.parseInt(params[1]);
		    		}else if(params[0].equals("stakeholdersfile")) {
		    			stakeholdersfile=params[1];
		    		}else if(params[0].equals("storestats")) {
		    			if(params[1].equals("true")) {
		    				storestats=true;
		    			}else {
		    				storestats=false;
		    			}
		    		}
	    		}
	    	}
	    	scanner.close();
		}
		catch (IOException e) {
		       e.printStackTrace();
		   }
 	}
 	
 	/** Check parameter validity */
 	public void init() {
 		
 		if(deletemodels) {
 			File storedmodel=new File("model.csv");
 			if(storedmodel.exists() && (mode==2.0 ||  mode==-1)) {
 				storedmodel.delete();
 			}
		
 			File storedmodelhg=new File("model-hg.csv");
 			if(storedmodelhg.exists() && (mode==2.0 ||  mode==-1)) {
 				storedmodelhg.delete();
 			}
 		}
		
 		File storedmodel=new File("deviation.csv");
 			if(storedmodel.exists() && (mode==2.0 ||  mode==-1)) {
				storedmodel.delete();
		}
		
 		if(domains<3 && mode==(-1)) {
			System.out.println("Hybrid Distributed Multiagent Algorithm\n"
					+ "for SFC Embedding across Computing Continuum\n");
			
			System.out.println("ERROR: \t Invalid parameter settings.\n"+
					"Hybrid algorithm requires to use topology of more than 2 DCs.");
 		}else if(boolparams[0]==true && domains>1 && (mode==5 || mode==(-1))){
 			System.out.println("ERROR: \t Invalid parameter settings.\n"+
					"PAGA should be used in single DC topology.");
 		}else {
 			init2();
 		}
 	}
 	
	/** Run simulation procedure. */
	public void init2() {
		//initialization messages
		String across="for SFC Embedding across Computing Continuum\n";
		
		if(domains==1) {
			across="for SFC Embedding in Data Center\n";
		}
		
		if(mode==5) {
			System.out.println("Genetic Algorithm for SFC Embedding\n"+ across);
		}else if(mode==1) {
			System.out.println("Distributed Multiagent Greedy Algorithm\n"+ across);
		}else if(mode==(-1)){
			System.out.println("Hybrid Distributed Multiagent Algorithm\n"+ across);
		}else {
			System.out.println("Distributed Multiagent Deep Learning Algorithm\n"+across);
			}
		
		System.out.println("SFC duration: "+duration);
		
		nets=new FTnetwork[domains];
		
		System.out.println("Number of Data Centers: "+nets.length+"\n");
		
		//build fat-tree network domains
		for(int dom=0;dom<domains;dom++) {
			
			//generate topology (the same for all domains), set log path and network ID
			Make make=new Make();
			make.makefattree(k, servperrack);
			nets[dom]=make.getnet();
			nets[dom].setpath(path);
			nets[dom].setid(dom);
			nets[dom].setdomain(Integer.toString(dom));
			nets[dom].setfilename("simulationresult-distr-"+dom+".csv");
			File f = new File(path+"simulationresult-distr-"+dom+".csv");
			if(f.exists()) {
				f.delete();
			}
			
			//define node capacity
			for(int s=0;s<nets[dom].getservers();s++) {
				nets[dom].getserver(s).setcpu(nodecapacity);
			}
			//compute network total cpu capacity
			nets[dom].totalcpu();
			//in distributed learning algorithm
			//define the capacity reserved in every server by the agents for their operation
			
			if(mode==2) {
				for(int s=0;s<nets[dom].getservers();s++) {
					nets[dom].getserver(s).addcpuload(0.05);
				}
			}
			
			//generate server registry, used in Unsupervised Deep Learning embedding method
			nets[dom].serverreg();
		}
		
		//print network elements
		System.out.println("Network parameters:");
		System.out.println("type: "+nets[0].gettype()+".k:"+k);
		
		int netracks=0;
		int netservers=0;
		
		for(int c=0;c<nets.length;c++) {
			netracks+=nets[c].getracks();
			netservers+=nets[c].getservperrack()*nets[c].getracks();
		}
		
		System.out.println("racks: "+netracks+"\nservers : "+netservers);
		
		System.out.println("");
		
		//in multi-domain infrastructures generate the multi-domain topology
		//as a Hypernode graph
		if(nets.length>1) {
			Make makehg=new Make();
			makehg.sethypergraph(hypergraph);
			makehg.makehypergraphFT(nets, interdcbandwidth);
			hg=makehg.gethypergraph();

			//log files
			hg.setfilename("simulationresult-distr-"+"HG"+".csv");
			File f = new File(path+"simulationresult-distr-"+"HG"+".csv");
			if(f.exists()) {
				f.delete();
			}
			
			hg.domainreg();
			hg.totalcpu();

		}
		
		//define stakeholders
		
		stakeholders.clear();
		
		if(numofstakeholders>1) {
			setstakeholdersparams();
		}else {
			stakeholders.add(new services.Stakeholder(0, vnfgraphs, "false"));
		}
		
		//Simulation
		
		for(int dt=0;dt<iterations;dt++) {
			
			//choose randomly a stakeholder
			
			int d=dt;
			if(numofstakeholders > 1) {
				double rss=Math.random()*stakeholders.size();
				int rs= (int) rss;
				d=stakeholders.get(rs).getid()+dt;
				vnfgraphs=stakeholders.get(rs).getvnfgraphs();
				setspatial=stakeholders.get(rs).getsetspatial();
			}else if(domains > 1){
				d=stakeholders.get(0).getid()+dt;
				vnfgraphs=stakeholders.get(0).getvnfgraphs();
				setspatial=stakeholders.get(0).getsetspatial();
			}
			
			//generate VNF-graph
			services.VNFgraph vnfgraph;
			
			int r=(int) (Math.random()*r2);
			r+=r1;
			
			if(!vnfgraphs.equals("random")) {
				String sfcset="";
				
				String VNFfile=vnfgraphs+"chain"+r+sfcset+"EV";
				vnfgraph=new services.VNFgraph(VNFfile);
				// set spatial constraints
				if(domains>1) {
					if(setspatial.equals("true")) {
						vnfgraph.setspatial();
					}else if(setspatial.equals("false")) {
						vnfgraph.remspatial();
					}else if(setspatial.equals("random")) {
						vnfgraph.remspatial();
						
						int randpoint=(int) (Math.random()*vnfgraph.getnodes());
						
						if(randpoint==0) {
							randpoint=1;
						}
						
						double randnode=Math.random()*domains;
						
						for(int i1=0;i1<randpoint;i1++) {
							vnfgraph.defspatial((int)randnode, i1);
						}
						
						double randnode2=randnode;
						
						while(randnode2==randnode) {
							randnode2=(Math.random()*domains/*(domains-randnode)*/);
						
							if(randnode2>=vnfgraph.getnodes()) {
								randnode2=vnfgraph.getnodes()-1;
							}
						}
						
						for(int i2=randpoint;i2<vnfgraph.getnodes();i2++) {
							vnfgraph.defspatial((int)randnode2, i2);
						}
					}	
				}
			}else {
				//VNFgraph constructor (vnfs, lowcap, maxcap, lowband, maxband, branchnum)
				vnfgraph=new services.
						VNFgraph(r,randomparams[0],randomparams[1],randomparams[2],randomparams[3],randomparams[4]);
				vnfgraph.remspatial();

				if(setspatial.equals("random")) {
					vnfgraph.remspatial();
					
					int randpoint=(int) (Math.random()*vnfgraph.getnodes());
					
					if(randpoint==0) {
						randpoint=1;
					}
					
					double randnode=Math.random()*domains;

					for(int i1=0;i1<randpoint;i1++) {
						vnfgraph.defspatial((int)randnode, i1);
					}
					
					double randnode2=randnode;
					
					while(randnode2==randnode) {
						randnode2=(Math.random()*domains/*(domains-randnode)*/);
					
						if(randnode2>=vnfgraph.getnodes()) {
							randnode2=vnfgraph.getnodes()-1;
						}
					}
					
					for(int i2=randpoint;i2<vnfgraph.getnodes();i2++) {
						vnfgraph.defspatial((int)randnode2, i2);
					}
				}
			}

			//graph partitioning parameters
			subgraphs=null;
			subgraphs2=null;
			hyperlinksgraph=null;
			hyperlinksgraph2=null;
			hyperlinksmapping=null;
			
			//Embedding in hypernode graph network is rejected.
			hgisrejected=false;
			//Embedding in at least one of the domains is rejected.
			domisrejected=false;

			hgmapping.clear();
			(new printout()).start("\n"+(d+1)+":");
			
			//initiate appropriate method for simulation
			if(domains==1 && mode==5) {
				
				singledomainGAembedding(d, vnfgraph);
				
			} else if(domains>1 && mode==5) {
				
				multidomainGAembedding(d, vnfgraph);
				
			} else if(domains>1 && mode==-1) {
				
				multidomainHybridembedding(d, vnfgraph);
				
			}else if(domains==1) {

				singledomainDDLembedding(d, vnfgraph);
				
			}else if(domains>1) {

				multidomainDDLembedding(d, vnfgraph);
				
			}
			
			//remove embedded SFCs with expired lifetime
			if(domains>1) {
				for(int v=0;v<hg.getnumofembedded();v++) {
					hg.getembeddedSFCs().get(v).reduceduration();
				}
			
				for(int v=0;v<hg.getnumofembedded();v++) {
					if(hg.getembeddedSFCs().get(v).getduration()<=0) {
						(new printout()).start("- removed SFC #"+hg.getembeddedSFCs().get(v).getid());
						hg.delembeddedbyid(hg.getembeddedSFCs().get(v).getid());
					}
				}
			}
			
			//compute deviation for spatial constraints
			if(domains>1 && vnfgraph.hasspatial() && !domisrejected && !hgisrejected) {
				sdeviation=0;
				for(int t=0; t<vnfgraph.getspatial().length;t++) {
					if(vnfgraph.getspatial()[t] != hgmapping.get(t)) {
						sdeviation+=hg.gethopcount(vnfgraph.getspatial()[t],hgmapping.get(t)+1);
					}
				}
				(new storedeviation()).start(d, sdeviation);

			}else if(domains>1){
				sdeviation=-1;
				(new storedeviation()).start(d, sdeviation);
			} 
			else {
				sdeviation=-1;
			}
			
			//store statistics
			if(!domisrejected && !hgisrejected) {
				if(storestats || domains==1) {
					for(int dom=0;dom<domains;dom++) {
						nets[dom].addsuccessful();
						nets[dom].netstats();
						nets[dom].storestats();
					}
				}
				
				if(domains>1) {
					hg.addsuccessful();
					hg.netstats();
					hg.storestats();
				}
				
			}else {
				if(storestats || domains==1) {
					for(int dom=0;dom<domains;dom++) {
						nets[dom].netstats();
						nets[dom].storerejectstats();
					}
				}
				
				if(domains>1) {
					hg.storerejectstats();
				}
			}
		}
	}
	
	/** Single DC DDL algorithm */
	private void singledomainDDLembedding(int d, VNFgraph vnfgraph) {
		int dom=0;
		
		//spatial constraints do not apply in single DC substrate networks
		vnfgraph.remspatial();
		//set the controller and compute VNF-graph mapping
		nets[dom].controller=new controller.DCController(nets[dom], mode);
		controller.DCController controller=(controller.DCController) nets[dom].getcontroller();
		controller.setclustersize(fnodes);
		controller.setduration(duration);
		controller.getrequest(vnfgraph);
		controller.setiteration(d);
		controller.setmaxsfcsize(maxsfcsize);
		controller.compute();

		if(controller.isrejected()) {
			domisrejected=true;
		}else {
			domisrejected=false;
		}

		//remove embedded SFCs with expired lifetime
		for(int v=0;v<nets[dom].getnumofembedded();v++) {
			nets[dom].getembeddedSFCs().get(v).reduceduration();
				
			if(domains==1) {
				if(nets[dom].getembeddedSFCs().get(v).getduration()<=0) {
					System.out.println("- removed SFC #"+nets[dom].getembeddedSFCs().get(v).getid());
					nets[dom].delembeddedbyid(nets[dom].getembeddedSFCs().get(v).getid());
				}
			}
		}
	}
	
	/** Single DC GA algorithm */
	private void singledomainGAembedding(int d, VNFgraph vnfgraph) {
		int dom=0;

		nets[dom].totalcpu();
		nets[dom].setiteration(d);
		
		//spatial constraints do not apply in single DC substrate networks
		vnfgraph.remspatial();
		
		//construct GA main object and set GA parameters
		sfc_ft_a.DCcontroller controller=new sfc_ft_a.DCcontroller(nets[dom],
				new sfc_ft_a.Setup(0.0, 0.0, popsize2, generations2, supergens2, crossprob2, mutprob2),
						vnfgraph);
		 
		if(boolparams[0]==true) {
		  for (Thread t : Thread.getAllStackTraces().keySet()) {
			  if (t.getName().equals("parameter adjustment")) {
				  controller.getwait().setwait(true);
			  }
		  }
		}

		//set the controller and compute VNF-graph mapping
		controller.setduration(duration);
		controller.setiterations(iterations);
		controller.setEVpath(vnfgraphs);
		controller.setr1r2(r1, r2);
		controller.setboolparams(boolparams);
		controller.setnetclasses(netclasses);
		controller.setid(d);
		controller.init();
		
		boolparams[1]=false;
		
		if(controller.isrejected()) {
			domisrejected=true;
		}else {
			domisrejected=false;
		}
		
		//remove SFCs with expired lifetime
		for(int v=0;v<nets[dom].getnumofembedded();v++) {
			nets[dom].getembeddedSFCs().get(v).reduceduration();
		}
		
		for(int v=0;v<nets[dom].getnumofembedded();v++) {
			if(nets[dom].getembeddedSFCs().get(v).getduration()<=0) {
				System.out.println("- removed SFC #"+nets[dom].getembeddedSFCs().get(v).getid());
				nets[dom].delembeddedbyid(nets[dom].getembeddedSFCs().get(v).getid());
			}
		}
	}
	
	/** multi-domain DDL algorithm */
	private void multidomainDDLembedding(int d, VNFgraph vnfgraph) {

		graphpartitioning(vnfgraph, vnfgraph.getspatial());
		subgraphs=broker.getsubgraphs();
		hyperlinksgraph=broker.gethypernodegraph();

		int[] hyperlinksmapping=new int[subgraphs.length];
		ArrayList<int[]> mappings=new ArrayList<int[]>();
		ArrayList<int[][]> DCmappings=new ArrayList<int[][]>();
		
		for(int s=0;s<subgraphs.length;s++) {

			for(int ns=0;ns<nets.length;ns++) {
				nets[ns].controller=new controller.DCController(nets[ns], mode);
			}

			//set the controller and compute VNF-graph mapping
			hg.HPcontroller=new controller.HyperController(hg, mode);
			
			controller.HyperController HPcontroller=(controller.HyperController) hg.getcontroller();
			HPcontroller.setprintmapping(false);
			HPcontroller.setsubgraphid(s);
			HPcontroller.setclustersize(fnodes);
			HPcontroller.setduration(duration);
			HPcontroller.getrequest(subgraphs[s]);
			HPcontroller.setiteration(d);
			HPcontroller.setmaxsfcsize(maxsfcsize);
			HPcontroller.compute();

			if(HPcontroller.isrejected() && subgraphs[s].hasspatial()) {
				subgraphs[s].remspatial();
				//set the controller and compute VNF-graph mapping
				HPcontroller=(controller.HyperController) hg.getcontroller();
				HPcontroller=new controller.HyperController(hg, mode);
				HPcontroller.setprintmapping(false);
				HPcontroller.setsubgraphid(s);
				HPcontroller.setclustersize(fnodes);
				HPcontroller.setduration(duration);
				HPcontroller.getrequest(subgraphs[s]);
				HPcontroller.setiteration(d);
				HPcontroller.setmaxsfcsize(maxsfcsize);
				HPcontroller.compute();

			}
		
			if(HPcontroller.isrejected()) {
				hgisrejected=true;
				hg.delembeddedbyid(d);
				break;

			}else {
				graphpartitioning(subgraphs[s], HPcontroller.getmapping());
				subgraphs2=broker.getsubgraphs();
				
				if(subgraphs2.length==1) {
					subgraphs2[0]=subgraphs[s];
				}

				for(int s2=0;s2<subgraphs2.length;s2++) {		
					//set the controller and compute VNF-subgraph mapping
					int dom=HPcontroller.getmapping()[subgraphs2[s2].getpartitioning()[0]];
					controller.DCController DCcontroller=(controller.DCController) nets[dom].getcontroller();
					DCcontroller.setnetid(dom);
					DCcontroller.setprintmapping(false);
					DCcontroller.setclustersize(fnodes);
					DCcontroller.setduration(duration);
					DCcontroller.getrequest(subgraphs2[s2]);
					DCcontroller.setspatial(false);
					DCcontroller.setiteration(d);
					DCcontroller.setmaxsfcsize(maxsfcsize);
					DCcontroller.compute();

					if(DCcontroller.isrejected()) {
						hg.delembeddedbyid(d);
						domisrejected=true;
						DCmappings.clear();
						break;
					}else {
						DCmappings.add(new int[][] {DCcontroller.getmapping(),{dom}});
					}

					if(subgraphs2.length>1 && s2>0) {
						//do nothing
					}else {
						mappings.add(HPcontroller.getmapping());
					}
					if(s2<hyperlinksmapping.length) {
						hyperlinksmapping[s2]=HPcontroller.getmapping()[0];
					}
					subgraphs2[s2].setmapping(HPcontroller.getmapping());
				}
			}
		}

		
		if(!domisrejected && !hgisrejected) {

			StringBuilder str = new StringBuilder();
			
			hg.embed(hyperlinksgraph, hyperlinksmapping);
			
			str.append("[");
			for(int t=0;t<mappings.size();t++) {
				for(int t2=0;t2<mappings.get(t).length;t2++) {
					str.append(mappings.get(t)[t2]);
					hgmapping.add(mappings.get(t)[t2]);
					if(t<(mappings.size()-1) || t2<(mappings.get(t).length-1)) {
						str.append(", ");
					}
				}
			}
			str.append("]\n");
			

			str.append("-------\n");
			
			//print mappings in DCs
			for(int dcm=0;dcm<DCmappings.size();dcm++) {
				str.append("#"+DCmappings.get(dcm)[1][0]+Arrays.toString(DCmappings.get(dcm)[0])+"\n");
			}

			(new printout()).start(str.toString());

		}else {
			(new printout()).start("> Rejection.");
		}
	}
	
	/** multi-domain GA algorithm */
	private void multidomainGAembedding(int d, VNFgraph vnfgraph) {
		ArrayList<int[]> mappings=new ArrayList<int[]>();		
		ArrayList<int[][]> DCmappings=new ArrayList<int[][]>();

		hg.totalcpu();
		
		for(int ns=0;ns<nets.length;ns++) {
			nets[ns].setstorestats(storestats);
			nets[ns].totalcpu();
		}

		//set the controller and compute VNF-graph mapping
		sfc_hg_a.HPcontroller HPcontroller=new sfc_hg_a.HPcontroller(hg,
				new sfc_hg_a.Setup(0.0, 0.0, popsize, generations, supergens, crossprob, mutprob),
					vnfgraph);
			
		HPcontroller.setduration(duration);
		HPcontroller.setiterations(iterations);
		HPcontroller.setr1r2(r1, r2);
		HPcontroller.setboolparams(boolparams);
		HPcontroller.setnetclasses(netclasses);
		HPcontroller.setid(d);
		HPcontroller.printmapping(false);
		HPcontroller.init();

		if(HPcontroller.isrejected()) {
			hgisrejected=true;
			hg.delembeddedbyid(d);
			domisrejected=true;
		}else {
			graphpartitioning(vnfgraph, HPcontroller.getmapping());
		
			subgraphs=broker.getsubgraphs();

			for(int s2=0;s2<subgraphs.length;s2++) {

				int dom=HPcontroller.getmapping()[subgraphs[s2].getpartitioning()[0]];
					
				//set the controller and compute VNF-subgraph mapping
				sfc_ft_a.DCcontroller DCcontroller=new sfc_ft_a.DCcontroller(nets[dom],
						new sfc_ft_a.Setup(0.0, 0.0, popsize2, generations2, supergens2, crossprob2, mutprob2),
								subgraphs[s2]);
				
				DCcontroller.setduration(duration);
				DCcontroller.setiterations(iterations);
				DCcontroller.setr1r2(r1, r2);
				DCcontroller.setboolparams(boolparams);
				DCcontroller.setnetclasses(netclasses);
				DCcontroller.setid(d);
				DCcontroller.printmapping(false);
				DCcontroller.init();

				if(DCcontroller.isrejected()) {
					hg.delembeddedbyid(d);
					domisrejected=true;
					DCmappings.clear();
					break;
				}else {
					DCmappings.add(new int[][] {DCcontroller.getmapping(),{dom}});
				}

				if(subgraphs.length>1 && s2>0) {
					//do nothing
				}else {
					mappings.add(HPcontroller.getmapping());
				}

				subgraphs[s2].setmapping(HPcontroller.getmapping());
			}
		}


		if(!domisrejected && !hgisrejected) {
			//print computed mapping
			StringBuilder str = new StringBuilder();
			str.append("[");
			for(int t=0;t<mappings.size();t++) {
				for(int t2=0;t2<mappings.get(t).length;t2++) {
					str.append(mappings.get(t)[t2]);
					hgmapping.add(mappings.get(t)[t2]);
					if(t<(mappings.size()-1) || t2<(mappings.get(t).length-1)) {
						str.append(", ");
					}
				}
			}
			str.append("] \n");
			str.append("-------\n");
			
			//print mappings in DCs
			for(int dcm=0;dcm<DCmappings.size();dcm++) {
				str.append("#"+DCmappings.get(dcm)[1][0]+Arrays.toString(DCmappings.get(dcm)[0])+"\n");
			}
			
			(new printout()).start(str.toString());
			
		}else {
			(new printout()).start("> Rejection.");
		}
	}
	
	/** multi-domain DDL algorithm */
	private void multidomainHybridembedding(int d, VNFgraph vnfgraph) {

		ArrayList<int[]> mappings=new ArrayList<int[]>();
		
		ArrayList<int[][]> DCmappings=new ArrayList<int[][]>();
		
		for(int ns=0;ns<nets.length;ns++) {
			nets[ns].setstorestats(storestats);
			nets[ns].totalcpu();
		}

		//set the controller and compute VNF-graph mapping
		sfc_hg_a.HPcontroller hpcontroller=new sfc_hg_a.HPcontroller(hg,
				new sfc_hg_a.Setup(0.0, 0.0, popsize, generations, supergens, crossprob, mutprob),
					vnfgraph);
			
		hpcontroller.setduration(duration);
		hpcontroller.setiterations(iterations);
		hpcontroller.setr1r2(r1, r2);
		hpcontroller.setboolparams(boolparams);
		hpcontroller.setnetclasses(netclasses);
		hpcontroller.setid(d);
		hpcontroller.printmapping(false);
		hpcontroller.init();
			
		if(hpcontroller.isrejected()) {
			hgisrejected=true;
			hg.delembeddedbyid(d);
			domisrejected=true;
		}else {
			graphpartitioning(vnfgraph, hpcontroller.getmapping());
				
			subgraphs=broker.getsubgraphs();

			for(int s2=0;s2<subgraphs.length;s2++) {
					
				int dom=hpcontroller.getmapping()[subgraphs[s2].getpartitioning()[0]];
				//set the controller and compute VNF-subgraph mapping
				controller.DCController dccontroller=new controller.DCController(nets[dom], 2.0);
				dccontroller.setnetid(dom);
				dccontroller.setprintmapping(false);
				dccontroller.setclustersize(fnodes);
				dccontroller.setduration(duration);
				dccontroller.getrequest(subgraphs[s2]);
				dccontroller.setspatial(false);
				dccontroller.setiteration(d);
				dccontroller.setmaxsfcsize(maxsfcsize);
				dccontroller.compute();
				if(dccontroller.isrejected()) {
					hg.delembeddedbyid(d);
					domisrejected=true;
					DCmappings.clear();
					break;
				}else {
					DCmappings.add(new int[][] {dccontroller.getmapping(),{dom}});
				}

				if(subgraphs.length>1 && s2>0) {
					//do nothing
				}else {
					mappings.add(hpcontroller.getmapping());
				}

				subgraphs[s2].setmapping(hpcontroller.getmapping());
			}
		}
		
		if(!domisrejected && !hgisrejected) {
			
			StringBuilder str = new StringBuilder();

			for(int t=0;t<mappings.size();t++) {
				str.append(Arrays.toString(mappings.get(t))+"\n-------\n");
				for(int t2=0;t2<mappings.get(t).length;t2++) {
					hgmapping.add(mappings.get(t)[t2]);
				}
			}

			//print mappings in DCs
			for(int dcm=0;dcm<DCmappings.size();dcm++) {
				str.append("#"+DCmappings.get(dcm)[1][0]+Arrays.toString(DCmappings.get(dcm)[0])+"\n");
			}
			
			(new printout()).start(str.toString());
			
		}else {
			(new printout()).start("> Rejection.");

		}
	}
	
	/** Partition VNF-graph into subgraphs on given mapping*/
	private void graphpartitioning(VNFgraph g, int[] m) {
		broker=new services.Broker(g, hg);
		broker.decompose(m);
	}
	
	/** Print embedded SFCs */
	public void printsfcs(int d) {
		System.out.println(nets[0].getembeddedSFCs().size());
		for(int i=0;i<nets[0].getembeddedSFCs().size();i++) {
			System.out.println(">"+d+":"+nets[0].getembeddedSFCs().get(i).getid());
		}
	}
	
	/** set stakeholders parameters */
	public void setstakeholdersparams() {
		
		String[] params=new String[1];
	
		try{
			File stfile=new File(stakeholdersfile);
	    	Scanner scanner = new Scanner(stfile);

	    	while(scanner.hasNext()){
	    		params=scanner.nextLine().split(",");
	    		
	    		stakeholders.add(new services.Stakeholder(Integer.parseInt(params[0]),params[1],params[2]));
	    	}
		
	    	scanner.close();
		}
		catch (IOException e) {
	       e.printStackTrace();
	   }
	}
	
	/** Compute runtime. */
	public void time() {
		totalTime=0;
		iterations=1;
		long startTime=System.nanoTime();
		init();
		long endTime=System.nanoTime();
		totalTime=(endTime - startTime)/1000;
		System.out.print("\n"+totalTime);
	}
	
	/** Thread for printing messages in output */
	class printout implements Runnable {
		   private Thread t;
		   private String threadName="Supergeneration";
		   String s;
		   
		   public void run() {
			   System.out.println(s);
		   }
		   
		   public void start (String s) {
			   this.s=s;
		         t = new Thread (this, threadName);
		         t.start ();
		   }
	}
	
	/** Thread for printing messages in output */
	class printinline implements Runnable {
		   private Thread t;
		   private String threadName="Supergeneration";
		   String s;
		   
		   public void run() {
			   System.out.print(s);
		   }
		   
		   public void start (String s) {
			   this.s=s;
		         t = new Thread (this, threadName);
		         t.start ();
		   }
	}
	
	/** Thread for storing constraint deviation */
	class storedeviation implements Runnable {
		   private Thread t;
		   private String threadName="storedeviation";
		   int iter;
		   int dev;
		   
		   public void run() {
		        FileWriter fw = null;
		        BufferedWriter bw = null;
		        PrintWriter pw = null;
		        
				try {
					try {
						fw = new FileWriter("deviation.csv",true);
					} catch (IOException e) {
						e.printStackTrace();
					}
					bw = new BufferedWriter(fw);
					pw = new PrintWriter(bw);
					pw.println((iter+1)+";"+dev);
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
		   
		   public void start (int iter, int dev) {
			   this.iter=iter;
			   this.dev=dev;
		         t = new Thread (this, threadName);
		         t.start ();
		   }
	}
}

