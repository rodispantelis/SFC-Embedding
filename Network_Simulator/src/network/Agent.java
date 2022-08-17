package network;

import java.util.ArrayList;
import java.util.Arrays;

/** agent that runs on every server in distributed multiagent algorithms */

public class Agent {
	ArrayList<Double[]> nodes=new ArrayList<Double[]>();		//cluster nodes
	ArrayList<Double[]> nodessort=new ArrayList<Double[]>();	//cluster nodes sorted
	ArrayList<Double> vnfdem=new ArrayList<Double>();			//VNF virtual nodes
	int[] tempsol, solution, output;
	Double fitness;
	Double mindem;
	int vnfsize;
	
	/** the computations executed by the agent */
	public void compute() {
		fitness=0.0;
		for(int s=0;s<output.length;s++) {
			output[s]=-1;
		}
		
		mindem=vnfdem.get(0);
		for(int m=0;m<vnfsize;m++) {
			if(vnfdem.get(m)<mindem) {
				mindem=vnfdem.get(m);
			}
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
	
	/** flag0 of incoming messages */
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
	
	/** flag1 of incoming messages */
	public void flag1(Double[] a) {
		//add candidate hosts and their capacities
			Double[] temp2= {a[1],a[2]};
			nodes.add(temp2);
	}

	/** flag2 of incoming messages */
	public void flag2(Double[] a) {
		//compute mapping
		compute();
	}
	
	/** flag3 of incoming messages */
	public void flag3() {
		nodes.clear();
		nodessort.clear();
		solution=new int[1];
		fitness=0.0;
	}

	//getters setters
	/** get computation output */
	public int[] getoutput() {
		return output;
	}
	
	/** get fitness of generated mapping */
	public Double getfitness() {
		return fitness;
	}
	
	/** get nodes that the agent computes */
	public void getnodes() {
		int[] d2=new int[nodes.size()];
		for(int i2=0;i2<d2.length;i2++) {
			d2[i2]=nodes.get(i2)[0].intValue();
		}
		System.out.println(Arrays.toString(d2));
	}
	
	/** get the weight of the nodes that are computed */
	public void getnodew() {
		Double[] d2=new Double[vnfdem.size()];
		for(int i2=0;i2<d2.length;i2++) {
			d2[i2]=vnfdem.get(i2);
		}
		System.out.println(Arrays.toString(d2));
	}
	
	/** get the sorted nodes **/
	public void getnodessort(int index) {
		int[] d2=new int[nodessort.size()];
		for(int i2=0;i2<d2.length;i2++) {
			d2[i2]=nodessort.get(i2)[index].intValue();
		}
		System.out.println(Arrays.toString(d2));
	}
}
