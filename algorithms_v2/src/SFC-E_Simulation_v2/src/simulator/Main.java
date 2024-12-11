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
import java.util.Scanner;

import network.Codec;
import network.FTnetwork;
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
	int domains=1;
	/** array of networks */
	FTnetwork[] nets;
	/** type of network */
	String type;
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
	/** The stakeholders of the network */
	ArrayList<services.Stakeholder> stakeholders=new ArrayList<services.Stakeholder>();						
	/** VNF lifecycle duration */
	int duration=2160;			
	/** number of iterations */
	int iterations=6000;
	/** node capacity */
	Double nodecapacity=20.0;
	/** name of simulation log file */
	String filename="simulationresult-distr.csv";
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

	/** Embedding is rejected. */
	boolean domisrejected=false;
	
	int numofstakeholders=1;
	
	String stakeholdersfile="file";
	
	boolean storestats=true;
	
	boolean deletemodels=true;
	
	/** GA parameters */
	
	/** network traffic classification parameter; used in PAGA */
	int netclasses;
	
	/** GA parameter {@link Setup} #2; used in DC embedding */
	int popsize2=10, generations2=10, supergens2=2, crossprob2=10, mutprob2=10;
	/** GA boolean parameters for running PAGA; delete previous setups; heuristic population generation */
	boolean[] boolparams=new boolean[3];
	
	/** deviation from spatial constraints*/
	int sdeviation=0;
	
	public ArrayList<Integer> hgmapping=new ArrayList<Integer>();	
	
	double interdcbandwidth=0.0;
	
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
		    		}else if(params[0].equals("stakeholders")) {
		    			numofstakeholders=Integer.parseInt(params[1]);
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
		
		init2();

 	}
 	
	/** Run simulation procedure. */
	public void init2() {
		//initialization messages

		if(mode==5) {
			System.out.println("Genetic Algorithm for SFC Embedding\n");
		}else if(mode==1) {
			System.out.println("Distributed Multiagent Greedy Algorithm\n");
		}else {
			System.out.println("Distributed Multiagent Deep Learning Algorithm\n");
			}
		
		nets=new FTnetwork[domains];
		
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
		
		//define stakeholders
		
		stakeholders.clear();

		stakeholders.add(new services.Stakeholder(0, vnfgraphs, "false"));

		
		//Simulation
		
		for(int dt=0;dt<iterations;dt++) {

			int d=dt;
			
			//generate VNF-graph
			services.VNFgraph vnfgraph;
			
			int r=(int) (Math.random()*r2);
			r+=r1;
			
			if(!vnfgraphs.equals("random")) {
				String sfcset="";
				
				String VNFfile=vnfgraphs+"chain"+r+sfcset+"EV";
				vnfgraph=new services.VNFgraph(VNFfile);
				// set spatial constraints

			}

			//VNFgraph constructor (vnfs, lowcap, maxcap, lowband, maxband, branchnum)
			vnfgraph=new services.
					VNFgraph(r,randomparams[0],randomparams[1],randomparams[2],randomparams[3],randomparams[4]);
			vnfgraph.remspatial();
			
			//Embedding in at least one of the domains is rejected.
			domisrejected=false;

			hgmapping.clear();
			(new printout()).start("\n"+(d+1)+":");
			
			//initiate appropriate method for simulation
			if(mode==5) {
				
				singledomainGAembedding(d, vnfgraph);
				
			}else{

				singledomainDDLembedding(d, vnfgraph);
				
			}
			
			//store statistics
			if(!domisrejected) {
				if(storestats || domains==1) {
					for(int dom=0;dom<domains;dom++) {
						nets[dom].addsuccessful();
						nets[dom].netstats();
						nets[dom].storestats();
					}
				}

				
			}else {
				if(storestats || domains==1) {
					for(int dom=0;dom<domains;dom++) {
						nets[dom].netstats();
						nets[dom].storerejectstats();
					}
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

