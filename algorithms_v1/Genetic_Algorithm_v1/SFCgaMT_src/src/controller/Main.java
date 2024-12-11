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

public class Main {
	/** network object */
	Network net;
	/** network type */
	String type;
	
	/** network elements */
	int racks=0, switches=0, links=0, servers=0;
	Codec cod=new Codec();							//Edge Vector coder-decoder
	/** path to store log files */
	String path="";
	/** path to VNFgraph files */
	String vnfgraphs="";
	/** parameters file */
	File parametersfile=new File("parameters");
	
	//simulation parameters
	/** number of iterations */
	int iterations;
	/** specify the VNFgraph file names */
	int r1, r2;
	/** servers per rack */
	int servperrack=10;
	/** k parameter; different usage in different topologies */
	int k=6;							
	/** VNF life cycle duration */
	int duration=0;								
	String filename="simulationresult-GA.csv";
	/** node CPU capacity */
	Double nodecapacity=10.0;
	/** network traffic classification parameter; used in PAGA */
	int netclasses;
	
	/** GA parameters; populations size; supergenerations; crossover and mutation probabilities */
	int popsize=10, generations=10, supergens=2, crossprob=10, mutprob=10;
	/** GA boolean parameters for running PAGA; delete previous setups; heuristic population generation */
	boolean[] boolparams=new boolean[3];
	
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
		    		}else if(params[0].equals("paga")) {
		    			boolparams[0]=Boolean.parseBoolean(params[1]);
		    		}else if(params[0].equals("deletedb")) {
		    			boolparams[1]=Boolean.parseBoolean(params[1]);
		    		}else if(params[0].equals("popgenheuristic")) {
		    			boolparams[2]=Boolean.parseBoolean(params[1]);
		    		}else if(params[0].equals("netclasses")) {
		    			netclasses=Integer.parseInt(params[1]);
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
		
		//set node capacity
		for(int s=0;s<net.getservers();s++) {
			net.getserver(s).setcpu(nodecapacity);
		}


		//delete old log file
		File f = new File(path+filename);
		if(f.exists()) {
			f.delete();
		}
		
		//print network elements
		net.printel2();

		System.out.println("SFC duration: "+duration);
		
		net.totalcpu();
		
		//construct GA main object and set GA parameters
		sfc_a.MainGA mga=new sfc_a.MainGA(net,
				new sfc_a.Setup(0.0, 0.0, popsize, generations, supergens, crossprob, mutprob));
		mga.setduration(duration);
		mga.setiterations(iterations);
		mga.setEVpath(vnfgraphs);
		mga.setr1r2(r1, r2);
		mga.setboolparams(boolparams);
		mga.setnetclasses(netclasses);
		mga.init();
	}
}

