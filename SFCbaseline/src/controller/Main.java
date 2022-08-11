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

	Network net;
	String type="fat-tree";								//type of network
												//network objects
	int racks=0, switches=0, links=0, servers=0;
	Codec cod=new Codec();						//Edge Vector coder-decoder
	String path="";								//Path to simulation log files
	String vnfgraphs;							//Path to VNFgraph files
	File parametersfile=new File("parameters");
	int servperrack;							//servers per rack
	int k;										//k parameter; different usage in different topologies
	int duration;								//VNF life cycle duration
	int iterations;
	Double nodecapacity;
	long totalTime;
	String filename="simulationresult.csv";
	int r1,r2;									//specify the VNFgraph file names
	
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
 	
	public void init() {
		//build fat-tree network
		Make make=new Make();
		make.makefattree(k, servperrack);
		net=make.getnet();
		net.setpath(path);
		
		for(int s=0;s<net.getservers();s++) {
			net.getserver(s).setcpu(nodecapacity);
		}
		
		System.out.println("SFC duration: "+duration);
		
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
			
			Baseline base=new Baseline(net);
			base.setduration(duration);
			base.getrequest(vnfgraph);
			base.compute();
			
			if(base.isrejected()) {
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
	
	public void time() {
		//compute runtime

		Double nodecapacity=10.0;

		//build fat-tree network
		Make make=new Make();
		make.makefattree(k, servperrack);
		net=make.getnet();
		
		for(int s=0;s<net.getservers();s++) {
			net.getserver(s).setcpu(nodecapacity);
		}
		
		for(int s=0;s<net.getservers();s++) {
			net.getserver(s).addcpuload(0.05);
		}
		
		File f = new File(path+"simulationresult.csv");
		if(f.exists()) {
			f.delete();
		}
		
		//print network elements
		net.printel2();
		
		String path="C:\\Files\\src\\EVgraphs\\";

		for(int d=0;d<iterations;d++) {
			
			int r=(int) (Math.random()*0);
			r+=9;
			String VNFfile=path+"chain"+r+"EV";
			network.VNFgraph vnfgraph=new network.VNFgraph(VNFfile);

			totalTime = 0;
			long startTime   = System.nanoTime();
			Baseline base=new Baseline(net);
			base.setprintmapping(false);
			base.setduration(duration);
			base.getrequest(vnfgraph);
			base.compute();
			long endTime   = System.nanoTime();
			totalTime = (endTime - startTime)/1000;
			System.out.print("\n"+totalTime);

			if(base.isrejected()) {
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
	}
}
