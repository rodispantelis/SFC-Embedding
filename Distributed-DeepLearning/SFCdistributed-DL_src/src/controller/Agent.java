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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/** agent that runs on every node of the network */
public class Agent {
	/** cluster nodes */
	ArrayList<Double[]> nodes=new ArrayList<Double[]>();
	/** cluster nodes sorted */
	ArrayList<Double[]> nodessort=new ArrayList<Double[]>();
	/** demands of VNF virtual node */
	ArrayList<Double> vnfdem=new ArrayList<Double>();
	/** temporary solution */
	int[] tempsol; 
	/** solution */
	int[] solution; 
	/** algorithm output */
	int[] output;
	/** output fitness */
	Double fitness;
	/** minimum node demand */
	Double mindem=0.0;
	/** maximum node demand */
	Double maxdem=0.0;
	/** cumulative node demands */
	Double cumdem=0.0;
	/** VNF-graph size */
	int vnfsize=1;
	/** number of cluster nodes inputted to ANN, this enables
	 * the nodes that the ANN accepts as input to be a subset of the cluster nodes */
	int tem1=0;
	/** maximum sfc size equals the output of the algorithm */
	int maxsfcsize=0;
	/** Artificial Neural Network for machine learning algorithm */
	nnetwork.ANNMain m;
	/** number of ANN input nodes */
	int inputs;
	
	double[] resmodel= {0.0};
	double[] currentmodel= {};
	
	/** the computations executed by the agent */
	public void compute(int mod) {
		Double cumcap=0.0;
		for(int ns=0;ns<nodes.size();ns++) {
			cumcap+=nodes.get(ns)[1];
		}
		
		mindem=vnfdem.get(0);
		for(int m=0;m<vnfsize;m++) {
			if(vnfdem.get(m)<mindem) {
				mindem=vnfdem.get(m);
			}
			cumdem+=vnfdem.get(m);
		}
		
		switch(mod) {
		case 1: 
			if(cumcap>=cumdem) {
				compute_greedy();
			}else {
				fitness=-1.0;
			}
		break;
		case 2:
			if(cumcap>=cumdem) {
				compute_runNN();
			}else {
				fitness=-1.0;
			}
		break;
		case 3:
			if(cumcap>=cumdem) {
				compute_trainNN();
			}else {
				fitness=-1.0;
			}
		break;
		case 4:
			if(cumcap>=cumdem) {
				compute_runNN();
			}else {
			fitness=-1.0;
			}
		break;
		}
	}
	
	/** Run neural network. */
	public void compute_runNN() {
		m=new nnetwork.ANNMain();
		inputs=m.getintputs();
		
		fitness=0.0;
		for(int s=0;s<output.length;s++) {
			output[s]=-1;
		}

		tem1=inputs-maxsfcsize;
		Double[] tempS=new Double[tem1+maxsfcsize];

			for(int d1=0;d1<tem1;d1++) {
				tempS[d1]=-1.0;
				if(d1<nodes.size()) {
				tempS[d1]=nodes.get(d1)[1];
				}
			}
			
			for(int d2=tem1;d2<(tem1+maxsfcsize);d2++) {
				tempS[d2]=-1.0;
				if(d2<(vnfdem.size()+tem1)) {
				tempS[d2]=vnfdem.get(d2-tem1);
				}
			}

			Double[][] result=m.runmodel(vnfsize, tempS);
			currentmodel=m.getcurrentmodel();

			fitness=result[1][0];

			if(fitness>=0.0) {
				int[] output2=new int[output.length];
				for(int s=0;s<output.length;s++) {
					if(result[0][s].intValue()<0.0) {
						output[s]=-1;
						output2[s]=-1;
					}else {
						if(nodes.size()<(maxsfcsize+1)) {
							output[s]=-1;
							fitness=-1.0;
						}else {
							output[s]=nodes.get(result[0][s].intValue())[0].intValue();//node IDs
							output2[s]=result[0][s].intValue();//index
						}
					}
				}
			}
		
		for(int s2=0;s2<output.length;s2++) {
			if(output[s2]==(-1)) {
				fitness=-1.0;
				break;
			}
		}
	}
	
	/** Train neural network. */
	public void compute_trainNN() {

		m=new nnetwork.ANNMain();
		inputs=m.getintputs();
		
		fitness=0.0;
		for(int s=0;s<output.length;s++) {
			output[s]=-1;
		}
		
		tem1=inputs-maxsfcsize;
		Double[] tempS=new Double[tem1+maxsfcsize];

			for(int d1=0;d1<tem1;d1++) {
				tempS[d1]=-1.0;
				if(d1<nodes.size()) {
				tempS[d1]=nodes.get(d1)[1];
				}
			}
			
			for(int d2=tem1;d2<(tem1+maxsfcsize);d2++) {
				tempS[d2]=-1.0;
				if(d2<(vnfdem.size()+tem1)) {
				tempS[d2]=vnfdem.get(d2-tem1);
				}
			}
			
			Double[][] result=m.ga_train(tempS, vnfsize);
		
			int[] output2=new int[output.length];
			for(int s=0;s<output.length;s++) {
				if(result[0][s].intValue()<0.0) {
					output[s]=-1;
					output2[s]=-1;
				}else {
					if(result[0].length>=nodes.size()) {
						output[s]=-1;
					}else {
						output[s]=nodes.get(result[0][s].intValue())[0].intValue();//node IDs
						output2[s]=result[0][s].intValue();//index
					}
				}
			}
		
			fitness=result[1][0];

			if(fitness>0.0 && fitness <1000.0) {
				resmodel=m.getnet().getew();
			}
		
		for(int s2=0;s2<output.length;s2++) {
			if(output[s2]==(-1)) {
				fitness=-1.0;
				break;
			}
		}
		

	}
	
	/** Compute distributed greedy algorithm. */
	public void compute_greedy() {

		fitness=0.0;
		for(int s=0;s<output.length;s++) {
			output[s]=-1;
		}
		
		nodessort.clear();

		//sort cluster nodes
		int min=0;
		for(int n2=0;n2<nodes.size();n2++){
			for(int n3=0;n3<nodes.size();n3++){
				if(nodes.get(n3)[1]<nodes.get(min)[1]){
					min=n3;
				}
			}
			
			Double[] t1=nodes.get(min);
			nodessort.add(t1);

			Double[] t2= {-1.0, 1000.0};
			nodes.set(min, t2);
		}

		for(int i1=0;i1<nodessort.size();i1++) {
			while(nodessort.get(i1)[1]>=mindem) {
				int a=-1;
				Double b=100000.0;	
				for(int i2=0;i2<vnfsize;i2++) {
					if(output[i2]>(-1)) {
					//go on
					}else {
						if((nodessort.get(i1)[1]-vnfdem.get(i2))>0.0) {
							if(b>nodessort.get(i1)[1]-vnfdem.get(i2)) {
								b=nodessort.get(i1)[1]-vnfdem.get(i2);
								a=i2;
							}
						}
					}
				}
				if(a==-1) {
					break;
				}else {
					output[a]=nodessort.get(i1)[0].intValue();
					fitness+=nodessort.get(i1)[1]-vnfdem.get(a);
					Double[] temp= {nodessort.get(i1)[0], (nodessort.get(i1)[1]-vnfdem.get(a))};
					nodessort.set(i1, temp);
				}
			}
		}
		
		for(int s2=0;s2<output.length;s2++) {
			if(output[s2]==(-1)) {
				fitness=-1.0;
				break;
			}
		}
	}
	
	/** Get solution model. */
	public double[] getresmodel() {
		return resmodel;
	}
	
	/** Store data in a file. */
	public void storedatainfile(String dt) {
		String path="";
		String filename="data";
		
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
			pw.println(dt);//Adds an end to the line
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
	
	/** Flag0 of incoming messages. */
	public void flag0(Double[] a) {
		//initialize function
		nodes.clear();
		nodessort.clear();
		vnfdem.clear();
		fitness=0.0;
		vnfsize=a[2].intValue();
		output=new int[vnfsize];
		solution=new int[vnfsize];
		tempsol=new int[vnfsize];
		
		for(int i=3;i<a.length;i++) {
			vnfdem.add(a[i]);
		}
	}
	
	/** Flag1 of incoming messages. */
	public void flag1(Double[] a) {
		//add candidate hosts and their capacities
			Double[] temp2= {a[1],a[2]};
			nodes.add(temp2);
	}

	/** Flag2 of incoming messages. */
	public void flag2(int a) {
		//compute mapping
		compute(a);
	}
	
	/** Flag3 of incoming messages. */
	public void flag3() {
		nodes.clear();
		nodessort.clear();
		solution=new int[1];
		fitness=0.0;
	}

	/** Flag4 of incoming messages. */
	public void flag4(int a) {
		maxsfcsize=a;
	}
	
	//getters setters
	/** Get computation output. */
	public int[] getoutput() {
		return output;
	}
	
	/** Get fitness of generated mapping. */
	public Double getfitness() {
		return fitness;
	}
	
	/** Get current model. */
	public double[] getcurrentmodel() {
		return currentmodel;
	}
	
	/** Get nodes that the agent computes. */
	public void getnodes() {
		int[] d2=new int[nodes.size()];
		for(int i2=0;i2<d2.length;i2++) {
			d2[i2]=nodes.get(i2)[0].intValue();
		}
		System.out.println(Arrays.toString(d2));
	}
	
	/** Get the weight of the nodes that are computed. */
	public void getnodew() {
		Double[] d2=new Double[vnfdem.size()];
		for(int i2=0;i2<d2.length;i2++) {
			d2[i2]=vnfdem.get(i2);
		}
		System.out.println(Arrays.toString(d2));
	}
	
	/** Get the sorted nodes. **/
	public void getnodessort(int index) {
		int[] d2=new int[nodessort.size()];
		for(int i2=0;i2<d2.length;i2++) {
			d2[i2]=nodessort.get(i2)[index].intValue();
		}
		System.out.println(Arrays.toString(d2));
	}
	
	/** Get cluster sized. **/
	public int getclustersize() {
		return nodes.size();
	}
	
	/** Messages from controller. */
	public void getmessage(network.Message m) {
		if(m.getmessage()[0]==1) {
			flag1(m.getmessage());
		}else if(m.getmessage()[0]==0) {
			flag0(m.getmessage());
		}else if(m.getmessage()[0]==2) {
			flag2(m.getmessage()[1].intValue());
		}else if(m.getmessage()[0]==3) {
			flag3();
		}else if(m.getmessage()[0]==4) {
			flag4(m.getmessage()[1].intValue());
		}
	}
	
	/** Set maximum sfc size equals the output of the algorithm. */
	public void setmaxsfcsize(int s) {
		maxsfcsize=s;
	}
}
