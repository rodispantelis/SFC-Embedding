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

import network.Codec;
import network.Make;
import network.Network;

public class Main {

	Network net;
	String type;								//type of network
												//network objects
	int racks=0, switches=0, links=0, servers=0;
	Codec cod=new Codec();						//Edge Vector coder-decoder
	String path="C:\\Files\\src\\results\\";
	int servperrack=20;							//servers per rack
	int k=4;									//k parameter; different usage in different topologies
	int duration=0;								//VNF life cycle duration
	String filename="simulationresult.csv";
	Double nodecapacity=20.0;
	long totalTime;
	
	public static void main(String[] args) {
		Main m=new Main();
		m.init();

		//m.time();
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
		
		duration=(int)(0.095*nodecapacity*net.getservers());
		System.out.println("SFC duration: "+duration);

		File f = new File(path+filename);
		if(f.exists()) {
			f.delete();
		}
		
		//print network elements
		net.printel2();
		
		net.serverreg();
		net.totalcpu();
		
		//net.generatetraffic((20.0/(net.getservers()*(net.getservers()-1)/2)), 0.05, 0.05);
		
		sfc_a.MainGA mga=new sfc_a.MainGA(net);
		mga.setduration(duration);
		mga.init();
		
	}
}

