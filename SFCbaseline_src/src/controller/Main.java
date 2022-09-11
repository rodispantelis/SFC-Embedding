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

import network.Codec;
import network.Make;
import network.Network;

/** Network Controller */

public class Main {
	/** network object */
	Network net;
	/** network type */
	String type;
												
	/** network elements */
	int racks=0, switches=0, links=0, servers=0;
	Codec cod=new Codec();						//Edge Vector coder-decoder
	/** path to simulation log files */
	String path="";		
	/** path to VNFgraph files */
	String vnfgraphs;	
	/** parameters file */
	File parametersfile=new File("parameters-baseline");
	/** servers per rack */
	int servperrack;
	/** k parameter; different usage in different topologies */
	int k;
	/** VNF life cycle duration */
	int duration;
	/** number of iterations */
	int iterations;
	/** node CPU capacity */
	Double nodecapacity;
	String filename="simulationresult-base.csv";
	/** specify the VNFgraph file names */
	int r1,r2;									
	
	public static void main(String[] args) {
		Main m=new Main();

	    if(m.parametersfile.exists()) {
	    	//if file exists continue
	    	m.setparameters();
	    }else {
	    	System.out.println("Parameter file is not found. \n"
	    			+ "The simulation will run on default parameters.\n");
	    }
	    
		m.init();
	}

	/** read simulation parameters from file */
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
		    		}
	    		}
	    	}
	    	scanner.close();
		}
		catch (IOException e) {
		       e.printStackTrace();
		   }
 	}
 	
	/** Initialize simulation */
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
		
		File f = new File(path+filename);
		if(f.exists()) {
			f.delete();
		}
		
		//print network elements
		net.printel2();
		
		net.totalcpu();

		System.out.println("Greedy Algorithm for SFC Embedding");
		
		net.shownetstats();
		
		for(int d=0;d<iterations;d++) {
			
			int r=(int) (Math.random()*r2);
			r+=r1;
			String VNFfile=vnfgraphs+"chain"+r+"EV";
			network.VNFgraph vnfgraph=new network.VNFgraph(VNFfile);

			System.out.print((d+1)+":");
			//run algorithm
			Baseline base=new Baseline(net);
			base.setduration(duration);
			base.getrequest(vnfgraph);
			base.compute();
			
			//store statistics on rejection
			if(base.isrejected()) {
				net.storerejectstats();
			}

			//remove embedded SFC if lifetime end is reached
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
}
