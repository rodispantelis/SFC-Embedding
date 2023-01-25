package controller;

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

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import network.Make;
import network.Codec;
import network.Network;

/** Coordinates simulation. */
public class Main {
	/** network object */
	Network net;
	/** type of network */
	String type;								
	/** network controller */
	Controller controller;
	Codec cod=new Codec();						//Edge Vector coder-decoder
	/** path to log files */
	String path="";
	/** path to VNFgraph files */
	String vnfgraphs;							
	/** servers per rack */
	int servperrack=20;				
	/** k parameter; different usage in different topologies */
	int k=6;						
	/** VNF lifecycle duration */
	int duration=2160;			
	/** number of iterations */
	int iterations=6000;
	/** node capacity */
	Double nodecapacity=20.0;
	/** name of simulation log file */
	String filename="simulationresult-distr.csv";
	/** parameters file */
	File parametersfile=new File("parameters");	
	/** specify the VNFgraph file names */
	int r1,r2;
	/** parameter to define number of nodes in the cluster */
	int fnodes;
	/** runtime */
	long totalTime;
	/** algorithm mode */
	Double mode=2.0;		//agent modes;1.0 greedy;2.0 train and run network
							//;3.0 train new network in each request; 4.0 run only stored model
	int maxsfcsize=0;
	
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
		File storedmodel=new File("model.csv");
		if(storedmodel.exists() && m.mode==2.0) {
			storedmodel.delete();
	    }
		m.init();
	}

	/** Read simulation parameters from file. */
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
		    		}
		    		else if(params[0].equals("mode")) {
		    			mode=Double.parseDouble(params[1]);
		    		}
		    		else if(params[0].equals("maxsfcsize")) {
		    			maxsfcsize=Integer.parseInt(params[1]);
		    		}
	    		}
	    	}
	    	scanner.close();
		}
		catch (IOException e) {
		       e.printStackTrace();
		   }
 	}
 	
	/** Run simulation procedure. */
	public void init() {
		//build fat-tree network
		Make make=new Make();
		make.makefattree(k, servperrack);
		net=make.getnet();
		net.setpath(path);
		net.setfilename(filename);
		
		for(int s=0;s<net.getservers();s++) {
			net.getserver(s).setcpu(nodecapacity);
		}
		
		System.out.println("SFC duration: "+duration);
		
		for(int s=0;s<net.getservers();s++) {
			net.getserver(s).addcpuload(0.05);
		}
		
		File f = new File(path+filename);
		if(f.exists()) {
			f.delete();
		}
		
		//print network elements
		net.printel2();
		
		net.serverreg();
		net.totalcpu();

		System.out.println("Distributed Multiagent Algorithm for SFC Embedding");
		
		net.shownetstats();
		
		System.out.println("");
		
		//Simulation
		
		for(int d=0;d<iterations;d++) {

			String sfcset="";
			int r=(int) (Math.random()*r2);
			r+=r1;
			String VNFfile=vnfgraphs+"chain"+r+sfcset+"EV";
			network.VNFgraph vnfgraph=new network.VNFgraph(VNFfile);
			
			System.out.print((d+1)+":");
			
			net.controller=new Controller(net, mode);
			controller=(Controller) net.getcontroller();
			controller.setclustersize(fnodes);
			controller.setduration(duration);
			controller.getrequest(vnfgraph);
			//controller.setmaxsfcsize(r1+r2-1);
			controller.setmaxsfcsize(maxsfcsize);
			controller.compute();

			if(controller.isrejected()) {
				net.storerejectstats();
			}

			for(int v=0;v<net.getnumofembedded();v++) {
				net.getembeddedSFCs().get(v).reduceduration();
				if(net.getembeddedSFCs().get(v).getduration()<=0) {
					net.delembedded(v);
					System.out.println("- removed SFC #"+v);
				}
			}
		}
		net.shownetstats();
	}
	
	/** Compute runtime. */
	public void time() {

	}
}

