package services;

import java.util.ArrayList;
import java.util.Arrays;

public class Broker {
	/** VNF-graphs */
	VNFgraph vnfgraph;
	/** VNF-subgraphs */
	VNFgraph[] subgraphs;
	/** substrate network, hypergraph */
	network.Hypergraph net;
	/** node mapping */
	int[] mapping;
	/** hypernode mapping */
	int[] hypernodemapping;
	/** hyper node VNF-graph */
	VNFgraph hypernodegraph;
	/** aggregated VNF-graph */
	VNFgraph newvnfgraph;
	/** classes of VNFs that have the same constraints */
	ArrayList<Integer> classes=new ArrayList<Integer>();
	/** index of nodes in every class */
	ArrayList<ArrayList<Integer>> classnodes=new ArrayList<ArrayList<Integer>>();
	/** the index of every subgraph node in the VNF-graph */
	ArrayList<Integer> partitioning=new ArrayList<Integer>();
	/** Edge Vector codec */
	network.Codec codec=new network.Codec();
	
	public Broker(VNFgraph vnfgraph, network.Hypergraph net) {
		this.vnfgraph=vnfgraph;
		this.net=net;
	}
	
	public Broker(VNFgraph vnfgraph, VNFgraph[] subgraphs, int[] mapping) {
		this.vnfgraph=vnfgraph;
		this.subgraphs=subgraphs;
		this.mapping=mapping;
	}
	
	/** decomposes a VNF-graph in subgraphs according to given constraints */
	public void decompose(int constraints[]) {
		
		//separate the SFC in classes of VNFs that have the same constraints
		for(int g1=0;g1<vnfgraph.getnodes();g1++) {
			int constraint=isinclass(constraints[g1]);
			if(constraint>(-1)) {
				classnodes.get(constraint).add(g1);
				partitioning.add(g1);
			}else {
				classes.add(constraints[g1]);
				classnodes.add(new ArrayList<Integer>());
				classnodes.get(classnodes.size()-1).add(g1);
				partitioning.add(g1);
			}
		}

		subgraphs=new VNFgraph[classes.size()];
		hypernodegraph=new VNFgraph(new int[subgraphs.length*(subgraphs.length-1)/2],
				new int[subgraphs.length], new int[subgraphs.length*(subgraphs.length-1)/2],
				new int[subgraphs.length], new int[subgraphs.length], 
				new int[subgraphs.length], new int[subgraphs.length]);

		for(int g2=0;g2<classes.size();g2++) {
			int nodes=classnodes.get(g2).size();
			int edges=nodes*(nodes-1)/2;
			int[] nodew=new int[nodes];
			int[] graph=new int[edges];
			int[] edgew=new int[edges];
			int[] spatial=new int[nodes];
			int[] gpartitioning=new int[nodes];
			int[] segflows=new int[nodes];
			int[] segbands=new int[nodes];
			
			for(int n=0;n<nodes;n++) {
				nodew[n]=vnfgraph.getnodew()[classnodes.get(g2).get(n)];
				segflows[n]=vnfgraph.getsegflows()[classnodes.get(g2).get(n)];
				segbands[n]=vnfgraph.getsegbands()[classnodes.get(g2).get(n)];
				spatial[n]=classes.get(g2);
				gpartitioning[n]=partitioning.get(classnodes.get(g2).get(n));
			}

			subgraphs[g2]=new VNFgraph(graph, nodew, edgew, spatial, 
					gpartitioning, segflows, segbands);

		}

		for(int c1=0;c1<partitioning.size();c1++) {
			for(int c2=c1;c2<partitioning.size();c2++) {
				if(c1 != c2) {
					if(vnfgraph.getgraph()[codec.coder(c1, c2)] > 0) {
						if(isinclass(constraints[partitioning.get(c1)]) ==
									isinclass(constraints[partitioning.get(c2)])) {
							
							subgraphs[isinclass(constraints[partitioning.get(c1)])].getgraph()
								[codec.coder(isinarray(subgraphs[isinclass(constraints
								            [partitioning.get(c1)])].getpartitioning(), c1),
								            		isinarray(subgraphs[isinclass(constraints
								            				[partitioning.get(c1)])].getpartitioning(), c2))]
								            						=vnfgraph.getgraph()[codec.coder(c1, c2)];
								
							subgraphs[isinclass(constraints[partitioning.get(c1)])].getedgew()
								[codec.coder(isinarray(subgraphs[isinclass(constraints
								            [partitioning.get(c1)])].getpartitioning(), c1),
								            		isinarray(subgraphs[isinclass(constraints
								            				[partitioning.get(c1)])].getpartitioning(), c2))]
								            						+=vnfgraph.getedgew()[codec.coder(c1, c2)];

						}else {
							hypernodegraph.getgraph()[codec.coder(
										isinclass(constraints[partitioning.get(c1)]),
											isinclass(constraints[partitioning.get(c2)]))]
							        		   =vnfgraph.getgraph()[codec.coder(c1, c2)];
								
							hypernodegraph.getedgew()[codec.coder(
										isinclass(constraints[partitioning.get(c1)]),
											isinclass(constraints[partitioning.get(c2)]))]
							        		   +=vnfgraph.getedgew()[codec.coder(c1, c2)];

							subgraphs[isinclass(constraints[partitioning.get(c1)])]
										.getsegflows()[isinarray(subgraphs[isinclass(
												constraints[partitioning.get(c1)])].getpartitioning(),c1)]
														=vnfgraph.getgraph()[codec.coder(c1, c2)];	
																
							subgraphs[isinclass(constraints[partitioning.get(c2)])]
										.getsegflows()[isinarray(subgraphs[isinclass(
												constraints[partitioning.get(c2)])].getpartitioning(),c2)]
														=vnfgraph.getgraph()[codec.coder(c1, c2)];							
								
							subgraphs[isinclass(constraints[partitioning.get(c1)])]
										.getsegbands()[isinarray(subgraphs[isinclass(
												constraints[partitioning.get(c1)])].getpartitioning(),c1)]
														+=vnfgraph.getedgew()[codec.coder(c1, c2)];	
																
							subgraphs[isinclass(constraints[partitioning.get(c2)])]
										.getsegbands()[isinarray(subgraphs[isinclass(
												constraints[partitioning.get(c2)])].getpartitioning(),c2)]
														+=vnfgraph.getedgew()[codec.coder(c1, c2)];;
								
						}						
					}
				}
			}
		}	
		
		if(subgraphs.length==1) {
			subgraphs[0]=vnfgraph;
			
			int[] temppartitioning=new int[vnfgraph.getnodes()];
			
			subgraphs[0].setpartitioning(temppartitioning);			
		}
	}
		
	/** generates mapping for the whole request from subgraph mappings */
	public int[] aggregate() {
		
		return mapping;
	}
	
	/** composes graph partitions in one VNF-graph */ 
	public VNFgraph compose() {
		ArrayList<Integer> graphnodes=new ArrayList<Integer>();
		ArrayList<Integer> graphnodeweights=new ArrayList<Integer>();
		ArrayList<Integer> graphspatial=new ArrayList<Integer>();
		ArrayList<Integer> graphpartitioning=new ArrayList<Integer>();
		
		for(int s=0;s<subgraphs.length;s++) {
			for(int g=0;g<subgraphs[s].getnodes();g++) {
				graphnodes.add(subgraphs[s].getpartitioning()[g]);
				graphnodeweights.add(subgraphs[s].getnodew()[g]);
				graphspatial.add(subgraphs[s].getspatial()[g]);
				graphpartitioning.add(subgraphs[s].getpartitioning()[g]);
			}
		}
		
		int[] aggnodes=new int[graphnodes.size()];
		int[] agggraph=new int[graphnodes.size()*(graphnodes.size()-1)/2];
		int[] aggnodew=new int[graphnodes.size()];
		int[] aggedgew=new int[graphnodes.size()];
		int[] aggspatial=new int[graphnodes.size()];
		int[] aggpartitioning=new int[graphnodes.size()];
		int[] aggsegflows=new int[graphnodes.size()];
		int[] aggsegbands=new int[graphnodes.size()];
		
		for(int n=0;n<graphnodes.size();n++) {
			aggnodes[graphpartitioning.get(n)]=graphnodes.get(n);
			aggnodew[graphpartitioning.get(n)]=graphnodeweights.get(n);
			aggspatial[graphpartitioning.get(n)]=graphspatial.get(n);
			aggpartitioning[graphpartitioning.get(n)]=graphpartitioning.get(n);
		}
		
		newvnfgraph=new VNFgraph(agggraph, aggnodew, aggedgew, aggspatial, 
				aggpartitioning, aggsegflows, aggsegbands);

		return newvnfgraph;
	}
	
	public VNFgraph[] getsubgraphs() {
		return subgraphs;
	}
	
	public VNFgraph gethypernodegraph() {
		return hypernodegraph;
	}
	
	public int[] gethypernodemapping() {
		return hypernodemapping;
	}
	
	public int isinclass(int i) {
		int found=-1;
		
		for(int g3=0;g3<classes.size();g3++) {
			if(classes.get(g3)==i) {
				found=g3;
				break;
			}
		}
		
		return found;
	}
	
	public int isinarray(int a[], int b) {
		int found=-1;
		
		for(int g3=0;g3<a.length;g3++) {
			if(a[g3]==b) {
				found=g3;
				break;
			}
		}
		
		return found;
	}
	
	public void printclasses() {
		System.out.println("classes: ");
		for(int g=0;g<classes.size();g++) {
			int[] temp=new int[classnodes.get(g).size()];
			for(int s2=0;s2<classnodes.get(g).size();s2++) {
				temp[s2]=classnodes.get(g).get(s2);
			}
			System.out.println(classes.get(g)+": "+Arrays.toString(temp));
		}
	}
	
	public void printsegments() {
		System.out.println("subgraphs: ");
		for(int i=0;i<subgraphs.length;i++) {
			System.out.println(Arrays.toString(subgraphs[i].getnodew())+"\t\t\t"+
					Arrays.toString(subgraphs[i].getsegflows())+"\t\t\t"+
					Arrays.toString(subgraphs[i].getsegbands())+"\t\t\t"
							+Arrays.toString(subgraphs[i].getspatial())+"\t\t\t"
								+Arrays.toString(subgraphs[i].getedgew()));
		}
	}
	
	public void printaggregated() {
		System.out.println("new VNFgraph: ");
			System.out.println(Arrays.toString(newvnfgraph.getnodew())+"\n"+
					Arrays.toString(newvnfgraph.getspatial())+"\t"+
					Arrays.toString(newvnfgraph.getpartitioning()));
	}
	
}
