package network;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class VNFgraph {

	int graph[], nodew[], edgew[], nodeacw[], nodesort[];
	public int edges,nodes, maxnodew, maxedgew, minnodew, minedgew;
		
	//default demands in case they are not defined on the VNFgraph
	int defnodew=1, defedgew=100, cpudemand=0, banddemand=0;
	String filename=" ";
	Codec codec= new Codec();
	
	public VNFgraph(String filename){
		this.filename=filename;
		loadEdgeVector(filename);
	}
	
	public void nodedemsort() {
		//sorts nodes in ascending order based on their capacity demands
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
	
	public void demands() {
		//updates maxnodew, maxedgew, minnodew, minedgew values
		nodeacw();
		minnodew=nodew[0];
		maxnodew=nodew[0];
		minedgew=edgew[0];
		maxedgew=nodeacw[0];	
		
		for(int n=1;n<nodew.length;n++) {
			if(nodew[n]<minnodew) {
				minnodew=nodew[n];
			}
			if(nodew[n]>maxnodew) {
				maxnodew=nodew[n];
			}
			
			if(nodeacw[n]>maxedgew) {
				maxedgew=nodeacw[n];
			}
			
			cpudemand+=nodew[n];
		}
		
		for(int m=0;m<edgew.length;m++) {
			if(edgew[m]<minedgew) {
				minedgew=edgew[m];
			}
			banddemand+=edgew[m];
		}
	}
	
	public void nodeacw() {
		//computes the total bandwidth demands of all the virtual links connected on every VNF
		nodeacw= new int[nodes];
		for(int i=0;i<edges;i++) {
			int[] w=codec.decoder(i);
			nodeacw[w[0]]+=edgew[i];
			nodeacw[w[1]]+=edgew[i];
		}
	}
	
	public void loadEdgeVector(String filename){		
		//load graph from file in edge vector format
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
	

	public void loadnodew(String filename){
		//load node demands from file
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
	
	public void loadedgew(String filename){
		//load edge demands from file
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
	
	public int getedges() {
		return edges;
	}
	
	public int getnodes() {
		return nodes;
	}
	
	public int getmaxnodew() {
		return maxnodew;
	}
	
	public int getmaxedgew() {
		return maxedgew;
	}
	
	public int getminnodew() {
		return minnodew;
	}
	
	public int getminedgew() {
		return minedgew;
	}
	
	public int[] getgraph() {
		return graph;
	}
	
	public int[] getnodew() {		//get demands for all nodes
		return nodew;
	}
	
	public int[] getedgew() {		//get demands for all edges
		return edgew;
	}
	
	public int getnodedem(int w) {	//get vnf node demand
		return nodew[w];
	}

	public int getnodeacw(int i) {
		return nodeacw[i];
	}

	public int[] getnodeacw() {
		return nodeacw;
	}
	
	public int cpugetdemand() {
		return cpudemand;
	}
	
	public int getbanddemand() {
		return banddemand;
	}
	
	public int getsortednode(int i){
		return nodesort[i];
	}
}








