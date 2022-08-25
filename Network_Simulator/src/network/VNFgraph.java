package network;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/** VNF forwarding graph class */

public class VNFgraph {

	int graph[], nodew[], edgew[], nodeacw[], nodesort[];
	public int edges,nodes, maxnodew, maxedgew, maxacedgew, minnodew, minedgew;
	public int capdemand=0, banddemands=0;
		
	//default demands in case they are not defined on the VNFgraph
	int defnodew=1, defedgew=100, cpudemand=0, banddemand=0;
	String filename=" ";
	Codec codec= new Codec();
	
	public VNFgraph(String filename){
		this.filename=filename;
		loadEdgeVector(filename);
	}
	
	/** sorts nodes in ascending order based on their capacity demands */
	public void nodedemsort() {
		nodesort=new int[nodes];
		int[] temp=new int[nodes];
		for(int s=0;s<nodes;s++) {
			temp[s]=1;
		}
		
		int min=0;
		
		for(int n1=0;n1<nodesort.length;n1++) {
			for(int n2=0;n2<nodes;n2++) {
				if(temp[min]<0) {
					min=n2;
				}
				if(temp[n2]>0) {
					if(nodew[n2]<nodew[min]) {
					min=n2;
					}
				}
			}
			temp[min]=-1;
			nodesort[n1]=min;
			min=0;
		}
	}
	
	/** updates minimum and maximum demand values */
	public void demands() {
		
		nodeacw();
		minnodew=nodew[0];
		maxnodew=nodew[0];
		minedgew=edgew[0];
		maxedgew=edgew[0];
		maxacedgew=nodeacw[0];	
		
		for(int n=1;n<nodew.length;n++) {
			if(nodew[n]<minnodew) {
				minnodew=nodew[n];
			}
			if(nodew[n]>maxnodew) {
				maxnodew=nodew[n];
			}
			
			if(nodeacw[n]>maxacedgew) {
				maxacedgew=nodeacw[n];
			}
			
			cpudemand+=nodew[n];
		}
		
		for(int m=0;m<edgew.length;m++) {
			if(edgew[m]<minedgew) {
				minedgew=edgew[m];
			}
			if(edgew[m]>maxedgew) {
				maxedgew=edgew[m];
			}
			banddemand+=edgew[m];
		}
	}
	
	/** computes the total bandwidth demands of all the virtual links connected on every VNF */
	public void nodeacw() {
		nodeacw= new int[nodes];
		for(int i=0;i<edges;i++) {
			int[] w=codec.decoder(i);
			nodeacw[w[0]]+=edgew[i];
			nodeacw[w[1]]+=edgew[i];
		}
	}
	
	
	/** load graph from file in edge vector format */
	public void loadEdgeVector(String filename){		
		ArrayList<Integer> tempev = new ArrayList<>();
		int max=1;
		
		try{
			File file = new File (filename);
	    	Scanner scanner = new Scanner(file);

	    	while(scanner.hasNext()){
	    		String[] tokens= scanner.nextLine().split(",");
	    		if(tokens.length==1){
	    			int tev = Integer.parseInt(tokens[0]);
	    			tempev.add(tev);
	    		}
	    		else{
	    			for(int t=0;t<tokens.length;t++){
	    				int tev = Integer.parseInt(tokens[t]);
	    				tempev.add(tev);
	    			}
	    		}
	    	}
	    	scanner.close();
		    
	    	int nd[]=codec.decoder(1+tempev.size());
			if((nd[0]+1)>max){
				max=(nd[0]);
			}
			if((nd[1]+1)>max){
				max=(nd[1]);
			}
	    	
	    	nodes=max;
	    	edges=nodes*(nodes-1)/2;
			graph=new int[edges];

			if(new File (filename+"-nodes").isFile()){
				loadnodew(filename+"-nodes");
			}else{
				nodew=new int[nodes];
				for(int nn=0;nn<nodes;nn++){
					nodew[nn]=defnodew;
				}
			}
				
			if(new File (filename+"-edges").isFile()){
					loadedgew(filename+"-edges");
				}else{
					edgew=new int[edges];
					for(int nn=0;nn<edges;nn++){
						edgew[nn]=defedgew;
					}
				}
			
		}
		catch (IOException e) {
		       e.printStackTrace();
		   }
		for(int c1=0;c1<edges;c1++){
			graph[c1]=tempev.get(c1);
		}
	}
	
	/** load node demands from file */
	public void loadnodew(String filename){
		ArrayList<Integer> tempev = new ArrayList<>();
		try{
			File file = new File (filename);
	    	Scanner scanner = new Scanner(file);    	
	    	while(scanner.hasNext()){
	    		String[] tokens= scanner.nextLine().split(",");
	    		if(tokens.length==1){
	    			int tev = Integer.parseInt(tokens[0]);
	    			tempev.add(tev);
	    		}
	    		else{
	    			for(int t=0;t<tokens.length;t++){
	    				int tev = Integer.parseInt(tokens[t]);
	    				tempev.add(tev);
	    			}
	    		}
	    	}

	    	scanner.close();
	    	nodew= new int[nodes];
			for(int c1=0;c1<nodes;c1++){
				nodew[c1]=tempev.get(c1);
			}
			
		}
		catch (IOException e) {
		       e.printStackTrace();
		   }
	}
	
	/** load edge demands from file */
	public void loadedgew(String filename){
		ArrayList<Integer> tempev = new ArrayList<>();
		try{
			File file = new File (filename);
	    	Scanner scanner = new Scanner(file);
	    	
	    	while(scanner.hasNext()){
	    		String[] tokens= scanner.nextLine().split(",");
	    		if(tokens.length==1){
	    			int tev = Integer.parseInt(tokens[0]);
	    			tempev.add(tev);
	    		}
	    		else{
	    			for(int t=0;t<tokens.length;t++){
	    				int tev = Integer.parseInt(tokens[t]);
	    				tempev.add(tev);
	    			}
	    		}
	    	}

	    	scanner.close();
	    	edgew=new int[edges];
			for(int c1=0;c1<edges;c1++){
				edgew[c1]=tempev.get(c1);
			}
			
		}
		catch (IOException e) {
		       e.printStackTrace();
		   }
	}
	
	//getters and setters
	
	/** get number of edges */
	public int getedges() {
		return edges;
	}
	
	/** get number of nodes */
	public int getnodes() {
		return nodes;
	}
	
	/** get maximum capacity demand */
	public int getmaxnodew() {
		return maxnodew;
	}
	
	/** get maximum bandwidth demand */
	public int getmaxacedgew() {
		return maxacedgew;
	}
	
	/** get minimum capacity demand */
	public int getminnodew() {
		return minnodew;
	}
	
	/** get minimum bandwidth demands */
	public int getminedgew() {
		return minedgew;
	}
	
	/** get graph in Edge Vector format */
	public int[] getgraph() {
		return graph;
	}
	
	/** get capacity demands for all nodes */
	public int[] getnodew() {
		return nodew;
	}
	
	/** get bandwidth demands for all edges */
	public int[] getedgew() {
		return edgew;
	}
	
	/** get demand for single node */
	public int getnodedem(int w) {	//get vnf node demand
		return nodew[w];
	}

	/** get the total bandwidth demands form the links connected in given node */
	public int getnodeacw(int i) {
		return nodeacw[i];
	}

	/** computes the total bandwidth demands for all nodes */
	public int[] getnodeacw() {
		return nodeacw;
	}
	
	/** get total capacity demand */
	public int cpugetdemand() {
		return cpudemand;
	}
	
	/** get total bandwidth demand */
	public int getbanddemand() {
		return banddemand;
	}
	
	/** get sorted nodes */
	public int getsortednode(int i){
		return nodesort[i];
	}
}








